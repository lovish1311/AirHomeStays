package com.airhomestays.app.ui.booking

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.airhomestays.app.*
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentBookingStep1Binding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.EnableAlpha
import com.airhomestays.app.util.invisible
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.withModels
import com.canhub.cropper.*
import com.airhomestays.app.ui.listing.ListingDetailsViewModel
import com.airhomestays.app.ui.listing.ListingNavigator
import com.shaon2016.propicker.pro_image_picker.ProPicker
import com.yalantis.ucrop.UCrop
import dagger.android.support.DaggerAppCompatActivity
import net.gotev.uploadservice.*
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.data.UploadNotificationConfig
import net.gotev.uploadservice.data.UploadNotificationStatusConfig
import net.gotev.uploadservice.exceptions.UploadError
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject

const val RC_LOCATION_PERM: Int = 123

class Step4Fragment: BaseFragment<FragmentBookingStep1Binding, ListingDetailsViewModel>(), EasyPermissions.PermissionCallbacks {

    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_booking_step1
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    lateinit var mBinding: FragmentBookingStep1Binding
    var avatar: String? = null
    private  val REQUEST_SELECT_IMAGE = 1
    private  val REQUEST_CROP_IMAGE = 2
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!

        initView()
        setUp()
    }

    private fun initView() {
        mBinding.tvListingCheckAvailability.text = resources.getString(R.string.next).toLowerCase().capitalize()
        mBinding.rlListingPricedetails.invisible()

        mBinding.tvListingCheckAvailability.onClick {
            if (viewModel.avatar.value == null) {
                showToast(getString(R.string.add_your_profile_photo))
            } else {
               viewModel.navigator.openBillingActivity(true)
            }

        }
        mBinding.ivNavigateup.onClick {  baseActivity?.onBackPressedDispatcher?.onBackPressed()  }
    }

    private fun setUp() {
        mBinding.rlBooking.withModels {
            viewholderBookingSteper {
                id("sd123")
                title(resources.getString(R.string.add_your_profile_photo))
                info(resources.getString(R.string.profile_photo_desc))
                infoVisibility(true)
                paddingBottom(true)
            }

            viewholderBookingUploadPhoto {
                id(987654)
                img(viewModel.avatar.value)
                onClick(View.OnClickListener {
                    askCameraPermission()
                })
            }
        }
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    private fun askCameraPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            if (EasyPermissions.hasPermissions(
                    requireActivity(),
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA)) {
                showImagePicDialog()

            } else {
                EasyPermissions.requestPermissions(
                    requireActivity(),
                    "Grant Permission to access your gallery and photos",
                    com.airhomestays.app.ui.profile.edit_profile.RC_LOCATION_PERM,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA)
            }
        }else{
            if (EasyPermissions.hasPermissions(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA)) {
                showImagePicDialog()

            } else {
                EasyPermissions.requestPermissions(
                    requireActivity(),
                    "Grant Permission to access your gallery and photos",
                    com.airhomestays.app.ui.profile.edit_profile.RC_LOCATION_PERM, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA)
            }
        }

    }
    private fun showImagePicDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Pick Image From")
        builder.setItems(options) { dialog, which ->
            if (which == 0) {
                showcamera()
            } else if (which == 1) {
                pickFromGallery()
            }
        }
        builder.create().show()
    }
    fun showcamera(){
        ProPicker.with(this)
            .cameraOnly()
            .crop()
            .start { resultCode, data ->
                if (resultCode == DaggerAppCompatActivity.RESULT_OK && data != null) {
                    val picker = ProPicker.getPickerData(data)
                    validateFileSize(picker?.uri?.path!!)

                }
            }
    }
    fun pickFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    private fun uploadMultipart(uri: String) {
        try {
            viewModel.isLoading.set(true)
            val config = UploadNotificationConfig(id.toString(),false, UploadNotificationStatusConfig(
                "Upload","uploading"
            ), UploadNotificationStatusConfig(
                "Upload","uploading"
            ), UploadNotificationStatusConfig(
                "Uploaded","uploaded"
            ), UploadNotificationStatusConfig(
                "Failed","Failed"
            ), )

            MultipartUploadRequest(context!!, Constants.WEBSITE + Constants.uploadPhoto)
                .addFileToUpload(uri, "file")
             //   .setBasicAuth(Constants.USERNAME,Constants.PASSWORD)
                .addHeader("auth", viewModel.getAccessToken())
                .setMaxRetries(2)
                .subscribe(context = context!!, lifecycleOwner = this , delegate = object :
                    RequestObserverDelegate {
                    override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                        viewModel.isLoading.set(true)
                    }

                    override fun onSuccess(
                        context: Context,
                        uploadInfo: UploadInfo,
                        serverResponse: ServerResponse
                    ) {
                        val convertedObject =
                            Gson().fromJson(serverResponse?.bodyString, JsonObject::class.java)
                        if (convertedObject.get("status").asInt == 200) {
                            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                                viewModel.isLoading.set(false)
                                val array = convertedObject.get("file").asJsonObject
                                Timber.tag("fileName").d(array.get("filename").asString)
                                viewModel.avatar.value=array.get("filename").asString
                                viewModel.setPictureInPref(array.get("filename").asString)
                                mBinding.rlBooking.requestModelBuild()
                                mBinding.tvListingCheckAvailability.EnableAlpha(true)
                            }, 3000)
                        }else if (convertedObject.get("status").asInt == 400){
                            viewModel.isLoading.set(false)
                            viewModel.navigator.showSnackbar(resources.getString(R.string.upload_failed),convertedObject.get("errorMessage").toString())
                        }
                    }
                    override fun onError(
                        context: Context,
                        uploadInfo: UploadInfo,
                        exception: Throwable
                    ) {
                        when (exception) {
                            is UserCancelledUploadException -> {
                            }

                            is UploadError -> {
                            }

                            else -> {
                            }
                        }
                    }

                    override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
                        viewModel.isLoading.set(false)
                    }

                    override fun onCompletedWhileNotObserving() {
                        // do your thing
                    }
                })
        } catch (exc: Exception) {
            Timber.tag("AndroidUploadService").e(exc)
            showError()
            viewModel.isLoading.set(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_OK == resultCode && data != null) {
            when (requestCode) {

                AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE -> {
                    Log.d("ProfileFragment", "Camera Permission Denied!!")
                }
            }
        }
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == DaggerAppCompatActivity.RESULT_OK) {
            data?.data?.let { uri ->
                val sourceUri = uri

                val destinationFileName = "cropped_image_" + UUID.randomUUID().toString()
                // This is the destination URI where the cropped image will be saved
                val destinationUri = Uri.fromFile(File(activity?.cacheDir, "$destinationFileName.jpg"))
                 val options=UCrop.Options()
                options.setCircleDimmedLayer(true)
                val uCrop = UCrop.of(sourceUri, destinationUri).withOptions(options)
                uCrop.start(context!!,this, REQUEST_CROP_IMAGE)

            }
        } else if (requestCode == REQUEST_CROP_IMAGE && resultCode == DaggerAppCompatActivity.RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            // Handle the cropped image here
            //resultUri.path
            validateFileSize(resultUri?.path!!)
        }
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        cropImage = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {

                val uriFilePath = result.getUriFilePath(requireContext()) // optional usage
                validateFileSize(uriFilePath!!)

            } else {
                // an error occurred
                val exception = result.error
                Timber.e(exception,"Picker error")
                showToast(exception?.message ?: "Picker error")
            }
        }
    }


    private fun validateFileSize(uri: String) {
        val file = File(uri)
        val fileSizeInBytes = file.length()
        val fileSizeInKB = fileSizeInBytes / 1024
        val fileSizeInMB = fileSizeInKB / 1024
        if (fileSizeInMB <= Constants.PROFILEIMAGESIZEINMB) {
            try {
                if(baseActivity?.isNetworkConnected!!) {
                    uploadMultipart(uri)
                } else {
                    showToast(resources.getString(R.string.currently_offline))
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            showToast(resources.getString(R.string.image_size_is_too_large))
        }
    }

    override fun onDestroyView() {
        mBinding.rlBooking.adapter = null
        super.onDestroyView()
    }

    override fun onRetry() {

    }
    override fun onResume() {
        super.onResume()
        viewModel.clearStatusBar(requireActivity())
    }
}
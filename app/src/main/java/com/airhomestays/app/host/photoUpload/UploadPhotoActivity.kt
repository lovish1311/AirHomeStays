package com.airhomestays.app.host.photoUpload

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.airhomestays.app.*
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentUploadListphotoBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.host.step_two.RC_LOCATION_PERM
import com.airhomestays.app.util.*
import com.airhomestays.app.vo.PhotoList
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import net.gotev.uploadservice.*
import net.gotev.uploadservice.data.UploadInfo
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
import javax.inject.Inject


class UploadPhotoActivity : BaseActivity<HostFragmentUploadListphotoBinding, Step2ViewModel>(),
    EasyPermissions.PermissionCallbacks,
    Step2Navigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentUploadListphotoBinding
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_upload_listphoto
    override val viewModel: Step2ViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(Step2ViewModel::class.java)
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    var isReCreated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this

        if (viewModel.uiMode == null)
            viewModel.uiMode = resources.configuration.uiMode
        else
            isReCreated = true

        mBinding.pgBar.progress = 50
        mBinding.rvListPhotos.gone()
        mBinding.tvNext.gone()
        initView()
        subscribeToLiveData()
        initPicker()
        if (isReCreated) {
            viewModel.photoList.observe(this) {
                setup(it!!);
            }
        }


    }

    private fun initView() {
        viewModel.setInitialValuesFromIntent(intent)

        mBinding.uploadToolbar.ivNavigateup.onClick { onBackPressed() }
        mBinding.tvNext.onClick {
            replaceFragment(AddListTitleFragment(), "AddListPhoto")
        }
        mBinding.rvListPhotos.layoutManager = GridLayoutManager(this, 2)
        viewModel.getListDetailsStep2()
    }

    private fun setup(it: ArrayList<PhotoList>) {
        try {
            mBinding.chips.apply {
                paddingBottom = true
                photos = true
                title = false
                description = false
                photosClick = (View.OnClickListener {

                })
                titleClick = (View.OnClickListener {
                    viewModel.navigator.navigateToScreen(Step2ViewModel.NextScreen.LISTTITLE)
                })
                descriptionClick = (View.OnClickListener {
                    viewModel.navigator.navigateToScreen(Step2ViewModel.NextScreen.LISTDESC)
                })
            }
            mBinding.rvListPhotos.withModels {

                viewholderAddListingPhotos {
                    id("add_photos")
                    onClick(View.OnClickListener {
                        askCameraPermission()
                    })
                }
                it.forEachIndexed { index, s ->
                    viewholderAddListing {
                        id(getString(R.string.photo) + s.refId)
                        url(s.name)
                        isRetry(s.isRetry)
                        isLoading(s.isLoading)
                        onClick(View.OnClickListener {

                        })
                        onRetryClick { _ ->
                            viewModel.retryPhotos(s.refId)
                            uploadMultipartSeperate(s.name!!, s.refId)
                        }
                        deleteClick(View.OnClickListener {
                            viewModel.retryCalled = "delete-${s.name}"
                            viewModel.deletephoto(s.name!!)
                        })
                        if (viewModel.getCoverPhotoId() == s.id) {
                            onSelected(true)
                        } else {
                            onSelected(false)
                        }
                        if (it.size == 1) {
                            onSelected(true)
                        }
                        if (viewModel.coverPhoto.value == -2) {
                            if (index == 0) {
                                onSelected(true)
                            }
                        }
                        onClickq(View.OnClickListener {
                            viewModel.coverPhoto.value = s.id
                            this@withModels.requestModelBuild()
                        })
                    }
                }
            }
        } catch (E: java.lang.Exception) {
            showError()
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()

    }


    fun uploadMultipartSeperate(uri: String, id: String) {
        try {

            val a = MultipartUploadRequest(this, Constants.WEBSITE + Constants.uploadListPhoto)
            a.setMethod("POST")
            //  a.setBasicAuth(Constants.USERNAME,Constants.PASSWORD)
            a.addFileToUpload(Uri.parse(uri).path!!, "file")
            a.addHeader("auth", viewModel.getAccessToken())
            a.addParameter("listId", viewModel.listID.value.toString())
            //a.setNotificationConfig { context, uploadId -> config }
            a.setMaxRetries(2)
            a.subscribe(context = baseContext, lifecycleOwner = this, delegate = object :
                RequestObserverDelegate {
                override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                }

                override fun onSuccess(
                    context: Context,
                    uploadInfo: UploadInfo,
                    serverResponse: ServerResponse
                ) {
                    Timber.tag("errorMessage").d(
                        "upload - onComplete - %d, response - %s, id - %s",
                        uploadInfo?.progressPercent,
                        serverResponse?.bodyString,
                        uploadInfo?.uploadId
                    )
                    viewModel.setCompleted(serverResponse.bodyString!!)
                }

                override fun onError(
                    context: Context,
                    uploadInfo: UploadInfo,
                    exception: Throwable
                ) {
                    viewModel.setError(uploadInfo.uploadId)
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
                    // do your thing
                }

                override fun onCompletedWhileNotObserving() {
                    // do your thing
                }
            })

        } catch (exc: Exception) {
            // Log.e("FragmentActivity.TAG", exc.message, exc)
        }
    }

    fun subscribeToLiveData() {
        mBinding.tvNext.text = getString(R.string.skip_for_now)
        viewModel.photoList.observe(this, Observer {
            it?.let {
                if (!viewModel.title.get().isNullOrEmpty() && !viewModel.desc.get()
                        .isNullOrEmpty() && !it.isNullOrEmpty()
                ) {
                    mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
                    mBinding.tvRightsideText.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.colorPrimary
                        )
                    )
                    mBinding.tvRightsideText.visibility = View.VISIBLE
                    mBinding.tvRightsideText.setOnClickListener {
                        if (viewModel.checkFilledData()) {
                            it.disable()
                            viewModel.retryCalled = "update"
                            viewModel.updateStep2()
                        }
                    }

                } else {

                    mBinding.tvRightsideText.visibility = View.GONE
                    mBinding.chips.chips2.gone()


                }
                mBinding.visiblity = true
                if (it.size > 0) {
                    viewModel.photoListSize = 1
                    mBinding.tvNext.text = getString(R.string.next)
                    mBinding.tvNext.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white_photo_story
                        )
                    )
                    mBinding.tvNext.setBackgroundResource(R.drawable.curve_button_red)
                } else {
                    viewModel.coverPhoto.value = -2
                    viewModel.photoListSize = 0
                    mBinding.tvNext.setText(getString(R.string.skip_for_now))
                }
                if (mBinding.rvListPhotos.adapter == null) {
                    if (viewModel.isListActive) {
                        mBinding.rvListPhotos.visible()
                        mBinding.tvNext.visible()
                    } else {
                        mBinding.rvListPhotos.gone()
                        mBinding.tvNext.gone()
                    }
                    setup(it)
                } else {
                    mBinding.rvListPhotos.requestModelBuild()
                }
                mBinding.rvListPhotos.requestModelBuild()

            }
        })
        viewModel.step2Result.observe(this, Observer {
            it?.let {
                if (!viewModel.title.get().isNullOrEmpty() && !viewModel.desc.get()
                        .isNullOrEmpty() && !viewModel.photoList.value.isNullOrEmpty()
                ) {
                    mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
                    mBinding.tvRightsideText.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.colorPrimary
                        )
                    )
                    mBinding.chips.chips2.visible()
                    mBinding.tvRightsideText.setOnClickListener {
                        if (viewModel.checkFilledData()) {
                            it.disable()
                            viewModel.retryCalled = "update"
                            viewModel.updateStep2()
                        }
                    }

                }
            }
        })
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    private fun askCameraPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            if (EasyPermissions.hasPermissions(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA
                )
            ) {
                pickImage13()
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Permission to access your gallery and photos",
                    com.airhomestays.app.ui.profile.edit_profile.RC_LOCATION_PERM,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA
                )
            }
        } else {
            if (EasyPermissions.hasPermissions(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            ) {
                pickImage()
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Permission to access your gallery and photos",
                    com.airhomestays.app.ui.profile.edit_profile.RC_LOCATION_PERM,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Timber.tag("ProfileFragment").d("Camera Permission Denied!!")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Timber.tag("ProfileFragment").d("Camera Permission Denied!!")
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}


    private fun pickImage() {
        FilePickerBuilder.Companion.instance
            .setMaxCount(10)
            .setActivityTheme(R.style.AppTheme)
            .setActivityTitle(getString(R.string.select_list_photos))
            .enableImagePicker(true)
            .enableVideoPicker(false)
            .enableCameraSupport(false)
            .showFolderView(false)
            .showGifs(false)
            .enableSelectAll(false)
            .pickPhoto(this)
    }

    private fun pickImage13() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    }

    private fun initPicker() {
        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uris != null) {
                    val photoPaths = ArrayList<Uri>()
                    photoPaths.addAll(uris!!)
                    validateFileSize(photoPaths)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val photoPaths = ArrayList<String>()
                    if (data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA) != null)
                        photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)!!)
                    val uriPaths =
                        data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                    if (uriPaths != null && uriPaths.isNotEmpty())
                        validateFileSize(uriPaths)
                }
            }
        }
    }


    private fun validateFileSize(photoPaths: ArrayList<Uri>) {
        var noOfFilesLargeInSize = 0
        val toUploadPhotos = ArrayList<PhotoList>()
        photoPaths.forEach {
            val file = File(ContentUriUtils.getFilePath(this, it))
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            if (fileSizeInMB <= Constants.LISTINGIMAGESIZEINMB) {
                ContentUriUtils.getFilePath(this, it).let {
                    val list = viewModel.addPhoto(it!!)
                    toUploadPhotos.add(list)
                    uploadMultipartSeperate(list.name!!, list.refId)
                }

            } else {
                noOfFilesLargeInSize++
            }
        }
        if (noOfFilesLargeInSize == 1) {
            showToast(getString(R.string.image_size_is_too_large))
        } else if (noOfFilesLargeInSize > 1) {
            showToast("$noOfFilesLargeInSize " + getString(R.string.image_size_is_too_large))
        }
        viewModel.addPhotos(toUploadPhotos)
    }


    private fun addFragment(fragment: Fragment, tag: String) {
        addFragmentToActivityAnim(mBinding.flStepTwo.id, fragment, tag)
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        replaceFragmentInActivity(mBinding.flStepTwo.id, fragment, tag)
    }

    fun popFragment(fragment: Fragment, tag: String) {
        replaceFragmentToActivity(mBinding.flStepTwo.id, fragment, tag)
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onBackPressed() {
        hideKeyboard()
        val myFrag = supportFragmentManager.findFragmentByTag("Photos")
        val myFrag2 = supportFragmentManager.findFragmentByTag("AddListPhoto")
        val myFrag3 = supportFragmentManager.findFragmentByTag("ListDesc")
        if (myFrag != null && myFrag.isVisible) {
            super.finish()
        } else if (myFrag2 != null && myFrag2.isVisible) {
            supportFragmentManager.findFragmentByTag("AddListPhoto")
                ?.let { supportFragmentManager.beginTransaction().remove(it).commit() }
        } else if (myFrag3 != null && myFrag3.isVisible) {
            popFragment(AddListTitleFragment(), "AddListPhoto")
        } else {
            super.finish()
        }
    }


    fun openFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_right, R.anim.slide_left)
            .replace(mBinding.flStepTwo.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun navigateToScreen(screen: Step2ViewModel.NextScreen, vararg params: String?) {
        when (screen) {
            Step2ViewModel.NextScreen.UPLOAD -> {
                supportFragmentManager.findFragmentByTag("AddListPhoto")
                    ?.let { supportFragmentManager.beginTransaction().remove(it).commit() }
                supportFragmentManager.findFragmentByTag("ListDesc")
                    ?.let { supportFragmentManager.beginTransaction().remove(it).commit() }
            }

            Step2ViewModel.NextScreen.LISTTITLE -> {
                replaceFragment(AddListTitleFragment(), "AddListPhoto")
            }

            Step2ViewModel.NextScreen.LISTDESC -> {
                if (viewModel.title.get()!!.trim().isNullOrEmpty()) {
                    showSnackbar(
                        getString(R.string.please_add_a_title_to_your_list),
                        getString(R.string.add_title)
                    )
                } else {
                    hideKeyboard()
                    replaceFragment(AddListDescFragment(), "ListDesc")
                }
            }

            Step2ViewModel.NextScreen.FINISH -> {
                this.finish()
            }

            else -> {}
        }
    }

    override fun navigateBack(backScreen: Step2ViewModel.BackScreen) {
        hideKeyboard()
        when (backScreen) {
            Step2ViewModel.BackScreen.UPLOAD -> {
                supportFragmentManager.findFragmentByTag("AddListPhoto")
                    ?.let { supportFragmentManager.beginTransaction().remove(it).commit() }
                supportFragmentManager.findFragmentByTag("ListDesc")
                    ?.let { supportFragmentManager.beginTransaction().remove(it).commit() }
            }

            Step2ViewModel.BackScreen.LISTTITLE -> {
                popFragment(AddListTitleFragment(), "AddListPhoto")
            }

            Step2ViewModel.BackScreen.LISTDESC -> popFragment(AddListDescFragment(), "ListDesc")
            Step2ViewModel.BackScreen.COVER -> {}
            Step2ViewModel.BackScreen.FINISH -> {}
            Step2ViewModel.BackScreen.APIUPDATE -> {}
        }
    }

    override fun onRetry() {
        if (viewModel.retryCalled.equals("")) {
            viewModel.getListDetailsStep2()
        } else if (viewModel.retryCalled.contains("delete")) {
            val text = viewModel.retryCalled.split("-")
            viewModel.deletephoto(text[1])
        } else {
            viewModel.updateStep2()
        }
    }

    override fun show404Page() {
        showToast(getString(R.string.list_not_available))
        this.finish()
    }
}
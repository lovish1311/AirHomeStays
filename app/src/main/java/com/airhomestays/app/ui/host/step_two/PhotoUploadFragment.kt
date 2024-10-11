package com.airhomestays.app.ui.host.step_two

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentUploadListphotoBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.viewholderAddListing
import com.airhomestays.app.vo.PhotoList
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import net.gotev.uploadservice.*
import net.gotev.uploadservice.data.UploadNotificationConfig
import net.gotev.uploadservice.data.UploadNotificationStatusConfig
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class PhotoUploadFragment : BaseFragment<HostFragmentUploadListphotoBinding, StepTwoViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentUploadListphotoBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_upload_listphoto
    override val viewModel: StepTwoViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepTwoViewModel::class.java)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
        viewModel.showListPhotos1()
    }

    private fun initView() {
        mBinding.setOnClick {
            askCameraPermission()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setup(it: ArrayList<PhotoList>) {
        mBinding.rvListPhotos.withModels {
            it.forEachIndexed { index, s ->
                viewholderAddListing {
                    id("photo$index")
                    url(s.name)
                    isRetry(s.isRetry)
                    isLoading(s.isLoading)
                    onClick(View.OnClickListener {

                    })
                    deleteClick(View.OnClickListener {

                    })
                }
            }
        }
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    private fun askCameraPermission() {
        if (EasyPermissions.hasPermissions(
                baseActivity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        ) {
            pickImage()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Grant Permission to access your gallery and photos",
                RC_LOCATION_PERM, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }
    }

    private fun pickImage() {
        FilePickerBuilder.Companion.instance
            .setMaxCount(10)
            .setActivityTheme(R.style.AppTheme)
            .setActivityTitle("Select List Photos")
            .enableImagePicker(true)
            .enableVideoPicker(false)
            .enableCameraSupport(true)
            .showFolderView(false)
            .showGifs(false)
            .enableSelectAll(false)
            .pickPhoto(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val photoPaths = ArrayList<String>()
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)!!)
                    val list = ArrayList<PhotoList>()
                    photoPaths.forEachIndexed { _, s ->
                        list.add(
                            PhotoList(
                                UUID.randomUUID().toString(),
                                null,
                                s,
                                null,
                                null,
                                isRetry = false,
                                isLoading = true
                            )
                        )
                    }
                    val lss = viewModel.toUploadphotoList.value
                    lss?.addAll(list)
                    viewModel.toUploadphotoList.value = lss
                    val photoList = viewModel.photoList.value
                    photoList?.addAll(list)
                    viewModel.photoList.value = photoList
                }
            }
        }
    }

    private fun uploadMultipart(uri: PhotoList) {
        try {
            val config = UploadNotificationConfig(
                id.toString(), false,
                UploadNotificationStatusConfig(
                    "Upload", "uploading"
                ),
                UploadNotificationStatusConfig(
                    "Upload", "uploading"
                ),
                UploadNotificationStatusConfig(
                    "Uploaded", "uploaded"
                ),
                UploadNotificationStatusConfig(
                    "Failed", "Failed"
                ),
            )
            MultipartUploadRequest(baseActivity!!, Constants.WEBSITE + Constants.uploadListPhoto)
                .addFileToUpload(Uri.parse(uri.name).path!!, "file")
                .addHeader(
                    "auth",
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjVmZmU3MzgwLTViODUtMTFlOS04OGFhLTM5MzVjYWRmMGY2YyIsImVtYWlsIjoiZ29rdWxAcmFkaWNhbHN0YXJ0LmNvbSIsImlhdCI6MTU1NzM4NjU3MSwiZXhwIjoxNTcyOTM4NTcxfQ.3Q3s2qP2JlSP39pwBumiDeO7lhfeXE2D2StMbUqZczk"
                )
                .addParameter("listId", "733")
                //.setNotificationConfig { context, uploadId ->  config}
                .setMaxRetries(1)
                .startUpload()
        } catch (exc: Exception) {
            Timber.tag("AndroidUploadService").e(exc)
            showError()
        }
    }

    fun subscribeToLiveData() {
        viewModel.photoList.observe(viewLifecycleOwner, Observer {
            it?.let {
                mBinding.visiblity = true
                setup(it)
            }
        })
        viewModel.toUploadphotoList.observe(viewLifecycleOwner, Observer {
            it?.let { photo ->
                if (photo.isNotEmpty()) {
                    uploadMultipart(photo[0])
                }
            }
        })
    }

    override fun onRetry() {
        viewModel.showListPhotos1()
    }
}
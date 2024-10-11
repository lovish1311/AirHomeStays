package com.airhomestays.app.host.photoUpload

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.airhomestays.app.ManageListingStepsMutation
import com.airhomestays.app.R
import com.airhomestays.app.RemoveListPhotosMutation
import com.airhomestays.app.Step2ListDetailsQuery
import com.airhomestays.app.UpdateListingStep2Mutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.PhotoList
import java.io.File
import java.util.UUID
import javax.inject.Inject


class Step2ViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<Step2Navigator>(dataManager,resourceProvider){

    init {
        dataManager.clearHttpCache()
    }
    var uiMode: Int? = null

    enum class NextScreen {
        COVER,
        LISTTITLE,
        LISTDESC,
        FINISH,
        APIUPDATE,
        UPLOAD
    }

    enum class BackScreen {
        COVER,
        LISTTITLE,
        LISTDESC,
        FINISH,
        APIUPDATE,
        UPLOAD
    }

    val listID = MutableLiveData<String?>()
    var coverPhoto = MutableLiveData<Int?>()
    val photoList = MutableLiveData<ArrayList<PhotoList>?>()
    val step2Result = MutableLiveData<Step2ListDetailsQuery.Results?>()
    val title = ObservableField("")
    val desc = ObservableField("")
    var isListActive = false
    var retryCalled = ""
    var isStep2 = false
    var photoListSize=0
    fun setInitialValuesFromIntent(intent: Intent) {
        try {
            val initData = intent.getStringExtra("listID") as String
            listID.value = initData
        } catch (e :KotlinNullPointerException) {
            navigator.showError()
        }
    }

    private fun parseData(results: List<Step2ListDetailsQuery.Result?>) {
        val list = ArrayList<PhotoList>()
        results.forEachIndexed { _, result ->
            list.add(PhotoList(
                    UUID.randomUUID().toString(),
                    result?.id,
                    result?.name,
                    result?.type,
                    result?.isCover
            ))
        }
        photoList.value = list
    }

    fun addPhotos(photoPaths: ArrayList<PhotoList>) {
        val list = photoList.value
        photoPaths.forEach {
            list?.add(it)
        }

        photoList.value = list
    }

    fun addPhoto(photoPaths: String): PhotoList {
        val referId = UUID.randomUUID().toString()
        return PhotoList(
                referId,
                -1,
                photoPaths,
                "",
                0,
                isRetry = false,
                isLoading = true
        )
    }

    fun deletephoto(filename: String) {
        val buildQuery = RemoveListPhotosMutation(
            listId = listID.value!!.toInt(),
            name = filename.toOptional()
        )

        compositeDisposable.add(dataManager.doRemoveListPhotos(buildQuery)
            .doOnSubscribe { }
            .doFinally { }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.removeListPhotos
                    if (data?.status == 200) {
                        deletePhotoInList(filename)
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("")
                    } else if (data?.status == 400) {
                        navigator.showToast(data.errorMessage.toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { handleException(it) })
        )
    }

    private fun deletePhotoInList(filename: String) {
        val list = photoList.value
        for ( i in 0 until list!!.size) {
            if (list[i].name == filename) {
                list.removeAt(i)
                break
            }
        }
        photoList.value = list
    }

    fun getListDetailsStep2() {
        val buildQuery = Step2ListDetailsQuery(
            listId= listID.value!!,
            listIdInt = listID.value!!.toInt(),
            preview = true.toOptional()
        )
        compositeDisposable.add(dataManager.doGetStep2ListDetailsQuery(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data?.getListingDetails
                    val photoData = response.data?.showListPhotos
                    if (data?.status == 200) {
                        retryCalled = ""
                        isListActive = true
                        title.set(data.results?.title)
                        desc.set(data.results?.description)
                        coverPhoto.value = data.results?.coverPhoto
                        step2Result.value = data.results
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("")
                    } else {
                        isListActive = false
                        navigator.show404Page()
                    }
                    if (photoData!!.status == 200) {
                        parseData(photoData.results!!)
                    } else {
                        photoList.value = java.util.ArrayList()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, {
                handleException(it)
            })
        )
    }

    fun updateStep2() {
        val buildQuery = UpdateListingStep2Mutation(
            title = title.get()?.trim().toOptional(),
            coverPhoto = coverPhoto.value.toOptional(),
            description = desc.get()?.trim().toOptional(),
            id = listID.value!!.toInt().toOptional(),
        )
        compositeDisposable.add(dataManager.doUpdateListingStep2(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                val data = response.data?.updateListingStep2
                try {
                    if (data?.status == 200) {
                        retryCalled = ""
                        updateStepDetails()
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("")
                    } else {
                        navigator.showSnackbar(
                            resourceProvider.getString(R.string.error),
                            data?.errorMessage!!
                        )
                        navigator.navigateToScreen(NextScreen.FINISH)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, { handleException(it) })
        )

    }

    fun updateStepDetails() {
        val buildQuery = ManageListingStepsMutation(
            listId = listID.value.toString(),
            currentStep= 2
        )
        compositeDisposable.add(dataManager.doManageListingSteps(buildQuery)
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                val data = response.data?.manageListingSteps
                try {
                    if (data?.status == 200) {
                        navigator.navigateToScreen(NextScreen.FINISH)
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("")
                    } else {
                        navigator.showError()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { handleException(it) })
        )
    }

    fun getCoverPhotoId(): Int? {
        return coverPhoto.value
    }

    fun getListAddedStatus(): Boolean {
        return photoList.value.isNullOrEmpty() &&!title.get().isNullOrEmpty()&& !desc.get().isNullOrEmpty()
        }

    fun setCompleted(bodyAsString: String) {
        val convertedObject = Gson().fromJson(bodyAsString, JsonObject::class.java)
        if (convertedObject.get("status").asInt == 200) {
            val array = convertedObject.get("files").asJsonArray
            val s = photoList.value
            array.forEach {
                val obj = it.asJsonObject
                val originalname = obj["originalname"].asString
                val fileName = obj["filename"].asString
                val id = obj["id"].asString
                s?.forEach {
                    val file = File(Uri.parse(it.name).path)
                    if (file.name == originalname ) {
                        it.isLoading = false
                        it.name = fileName
                        it.id = id.toInt()
                    }
                }
            }
            photoList.value = s
        }else if (convertedObject.get("status").asInt == 400){
            navigator.showSnackbar(resourceProvider.getString(R.string.upload_failed),convertedObject.get("errorMessage").toString())
        }
    }

    fun setError(uploadId: String?) {
        val s = photoList.value
        s?.forEachIndexed { index, photoList ->
            if (uploadId == photoList.refId) {
                photoList.isRetry = true
                photoList.isLoading = false
            }
        }
        photoList.value = s
    }

    fun retryPhotos(uploadId: String?) {
        val s = photoList.value
        s?.forEachIndexed { index, photoList ->
            if (uploadId == photoList.refId) {
                photoList.isRetry = false
                photoList.isLoading = true
            }
        }
        photoList.value = s
    }

    fun checkFilledData(): Boolean{
        if (title.get()?.trim().isNullOrEmpty()) {
            navigator.showSnackbar( resourceProvider.getString(R.string.please_add_a_title_to_your_list), resourceProvider.getString(R.string.add_title))
        } else {
            if (desc.get()?.trim().isNullOrEmpty()) {
                navigator.showSnackbar(resourceProvider.getString(R.string.please_add_a_description_to_your_list), resourceProvider.getString(R.string.add_description))
            }else{
                return true
            }
        }
        return false
    }
}
package com.airhomestays.app.ui.host.hostListing

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.GetDefaultSettingQuery
import com.airhomestays.app.GetSecureSiteSettingsQuery
import com.airhomestays.app.ManageListingsQuery
import com.airhomestays.app.ManagePublishStatusMutation
import com.airhomestays.app.R
import com.airhomestays.app.RemoveListingMutation
import com.airhomestays.app.RemoveMultiPhotosMutation
import com.airhomestays.app.SubmitForVerificationMutation
import com.airhomestays.app.ViewListingDetailsQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.ManageList
import org.json.JSONArray
import javax.inject.Inject

class HostListingViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<HostListingNavigator>(dataManager, resourceProvider) {

    lateinit var manageListing: MutableLiveData<List<ManageListingsQuery.Result?>?>
    var allList = MutableLiveData<ArrayList<ManageList>?>()
    var temp = MutableLiveData<ArrayList<ManageList>>()
    var listingDetails = MutableLiveData<ViewListingDetailsQuery.Results?>()
    var retryCalled = ""
    val publishBoolean = ObservableField(true)
    var removeListRes = ArrayList<HashMap<String, String>>()
    var emptyList = MutableLiveData<Int>()

    fun loadListing(): MutableLiveData<ArrayList<ManageList>?> {
        if (!::manageListing.isInitialized) {
            manageListing = MutableLiveData()
            defaultSettingsInCache()
        }
        if (allList.value == null) {
            emptyList.value = 0
        }
        return allList
    }


    private fun setSiteName() {
        val request = GetSecureSiteSettingsQuery(
            type = "site_settings".toOptional(),
            securityKey = resourceProvider.getString(R.string.security_key)
        )

        compositeDisposable.add(dataManager.clearHttpCache()
            .flatMap { dataManager.doGetSecureSettingApiCall(request).toObservable() }
            .performOnBackOutOnMain(scheduler)
            .subscribe {
                try {
                    if (it.data?.getSecureSiteSettings?.results?.get(0)?.name == "siteName") {
                        dataManager.siteName =
                            it.data?.getSecureSiteSettings?.results?.get(0)?.name
                    }
                    dataManager.listingApproval = it.data?.getSecureSiteSettings?.results!!.find {
                        it?.name == "listingApproval"
                    }?.value?.toIntOrNull() ?: 0
                    for (item in it.data?.getSecureSiteSettings?.results!!) {
                        if (item?.name == "phoneNumberStatus") {
                            dataManager.phoneNoType = item.value
                        }
                    }
                    getList()
                } catch (e: Exception) {
                    navigator.showToast(e.message.toString())
                }
            })
    }

    fun submitForVerification(status: String, listId: Int) {
        val query = SubmitForVerificationMutation(
            id = listId,
            listApprovalStatus = status.toOptional()
        )

        compositeDisposable.add(dataManager.submitForVerification(query)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    Handler(Looper.getMainLooper()).postDelayed({
                        publishBoolean.set(true)
                    }, 1000)
                    val data = response.data!!.submitForVerification
                    if (data?.status == 200) {
                        retryCalled = ""
                        setPublishStatus(listId, false)
                        getList()

                    } else if (data!!.status == 400) {
                        navigator.showToast(data.errorMessage.toString())
                    } else {
                        navigator.openSessionExpire("")
                    }


                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }, {

                it.printStackTrace()
                handleException(it)
            })
        )
    }

    fun defaultSettingsInCache() {
        val request = GetDefaultSettingQuery()

        compositeDisposable.add(dataManager.clearHttpCache()
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .flatMap { dataManager.doGetDefaultSettingApiCall(request).toObservable() }
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                {
                    setSiteName()
                },
                {

                }
            ))
    }


    fun getList() {
        val buildQuery = ManageListingsQuery()
        compositeDisposable.add(dataManager.getManageListings(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .subscribe({ response ->
                val data = response.data?.manageListings!!
                try {
                    if (data.status == 200) {
                        dataManager.isHostOrGuest = true
                        retryCalled = ""
                        if (data.results.isNullOrEmpty()) {
                            emptyList.value = 1
                            navigator.hideLoading()
                            navigator.showNoListMessage()
                        } else {
                            emptyList.value = 2
                            manageListing.value = data.results
                            setData()
                        }


                    } else if (data.status == 500) {
                        navigator.openSessionExpire("")
                    } else {
                        navigator.hideLoading()
                        navigator.showNoListMessage()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, {
                handleException(it)
            }
            )
        )

    }

    fun setData() {
        var inprogress = ArrayList<ManageList>()
        manageListing.value!!.forEachIndexed { index, result ->
            var listPhoto = ""
            var listTitle = ""
            if (result!!.listPhotos!!.size > 0) {
                if (result.coverPhoto != null) {
                    result.listPhotos!!.forEachIndexed { index, listPhotos ->
                        if (listPhotos?.id!! == result.coverPhoto!!) {
                            listPhoto = result?.listPhotos!![index]?.name!!
                        }
                    }
                    if (listPhoto.isNullOrEmpty()) {
                        listPhoto = result?.listPhotos!![0]?.name!!
                    }
                } else {
                    listPhoto = result?.listPhotos!![0]?.name!!
                }
            }
            if (result.title != null) {
                listTitle = result.title!!.trim().replace("\\s+", " ")
            }
            var roomtype = ""
            if (result.settingsData!!.size > 0) {
                if (result.settingsData!![0]?.listsettings?.itemName == null) {
                    roomtype = "Room Type"
                } else {
                    roomtype = result.settingsData!![0]?.listsettings!!.itemName!!
                }
            }
            var step1 = ""
            var step2 = ""
            var step3 = ""
            if (result.listingSteps == null) {
                step1 = "active"
                step2 = "inactive"
                step3 = "inactive"
            } else {
                step1 = result.listingSteps!!.step1!!
                step2 = result.listingSteps!!.step2!!
                step3 = result.listingSteps!!.step3!!
            }
            val list = ManageList(
                id = result.id!!,
                title = listTitle,
                imageName = listPhoto,
                isReady = result.isReady!!,
                isPublish = result.isPublished!!,
                step1Status = step1,
                step2Status = step2,
                step3Status = step3,
                location = result.city,
                roomType = roomtype,
                created = result.lastUpdatedAt!!,
                listApprovelStatus = result.listApprovalStatus
            )
            inprogress.add(list)
        }
        allList.value = inprogress
    }

    private fun setPublishStatus(id: Int, action: Boolean) {
        val listValues = allList.value
        for (i in 0 until listValues!!.size) {
            if (listValues[i].id == id) {

                listValues[i].isPublish = action
            }
        }

        allList.value = listValues
    }

    fun publishListing(action: String, listId: Int, pos: Int) {
        var buildQuery = ManagePublishStatusMutation(
            listId = listId,
            action = action
            )
        compositeDisposable.add(dataManager.doManagePublishStatus(buildQuery)
            .doOnSubscribe { }
            .doFinally { }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    Handler(Looper.getMainLooper()).postDelayed({
                        publishBoolean.set(true)
                    }, 1000)
                    val data = response.data!!.managePublishStatus!!
                    if (data.status == 200) {
                        retryCalled = ""
                        if (action.equals("unPublish")) {
                            setPublishStatus(listId, false)
                        } else {
                            setPublishStatus(listId, true)
                        }
                    } else if (data!!.status == 400) {
                        navigator.showToast(data.errorMessage.toString())
                    } else {
                        navigator.openSessionExpire("")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }, {

                it.printStackTrace()
                handleException(it)
            })
        )
    }

    fun getListingDetails(listId: Int) {
        val buildQuery = ViewListingDetailsQuery(
            listId = listId,
            preview = true.toOptional()
        )

        compositeDisposable.add(dataManager.doListingDetailsApiCall(buildQuery)
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data?.viewListing
                    if (data!!.status == 200) {
                        retryCalled = ""
                        listingDetails.value = data.results
                        navigator.showListDetails()
                    } else if (data!!.status == 400) {
                        navigator.show404Screen()
                    } else {
                        navigator.openSessionExpire("")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { handleException(it) }
            )
        )
    }

    fun removeList(listId: Int, pos: Int, from: String) {
        val buildQuery = RemoveListingMutation(
            listId = listId
        )
        compositeDisposable.add(dataManager.doRemoveListingMutation(buildQuery)
            .doOnSubscribe { }
            .doFinally { }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.removeListing
                    retryCalled = ""
                    if (data!!.status == 200) {

                        val removePhotos = data.results
                        if (removePhotos!!.size > 0) {
                            removePhotos!!.forEachIndexed { index, result ->
                                val temp = HashMap<String, String>()
                                temp.put("name", result?.name.toString())
                                temp.put("id", result?.id.toString())
                                removeListRes.add(temp)
                            }
                            removeListPhotos(listId)
                        } else {
                            removeListEntry(listId)
                        }
                    } else if (data!!.status == 400) {
                        navigator.showToast(data.errorMessage.toString())
                    } else {
                        navigator.openSessionExpire("")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }, {

                it.printStackTrace()
                handleException(it)
            })
        )
    }

    private fun removeListEntry(id: Int) {
        val entireList = allList.value
        for (i in 0 until entireList!!.size) {
            if (entireList[i].id == id) {
                entireList.removeAt(i)
                break
            }
        }
        allList.value = entireList
    }

    fun removeListPhotos(listId: Int) {
        val json = JSONArray(removeListRes)
        val buildQuery = RemoveMultiPhotosMutation(
            photos = json.toString().toOptional()
        )
        compositeDisposable.add(dataManager.doRemoveMultiPhotosMutation(buildQuery)
            .doOnSubscribe { }
            .doFinally { }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.removeMultiPhotos
                    if (data!!.status == 200) {
                        removeListEntry(listId)
                    } else if (data!!.status == 400) {
                        navigator.showToast(data.errorMessage.toString())
                    } else {
                        navigator.openSessionExpire("")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }, {

                it.printStackTrace()
                handleException(it)
            })
        )

    }

    fun listRefresh() {
        getList()
    }


}
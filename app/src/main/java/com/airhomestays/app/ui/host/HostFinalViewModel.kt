package com.airhomestays.app.ui.host

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.GetDefaultSettingQuery
import com.airhomestays.app.GetSecureSiteSettingsQuery
import com.airhomestays.app.ManagePublishStatusMutation
import com.airhomestays.app.R
import com.airhomestays.app.ShowListingStepsQuery
import com.airhomestays.app.SubmitForVerificationMutation
import com.airhomestays.app.ViewListingDetailsQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import javax.inject.Inject

class HostFinalViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<HostFinalNavigator>(dataManager,resourceProvider) {

    val listId = ObservableField("")
    lateinit var stepsSummary : MutableLiveData<ShowListingStepsQuery.Results?>
    var step1Status : String = ""
    var step2Status : String = ""
    var step3Status : String = ""
    var isPublish = ObservableField(false)
    var listApprovelStatus = ObservableField<String?>()
    var isReady = ObservableField(false)
    var listingDetails = MutableLiveData<ViewListingDetailsQuery.Results?>()
    val yesNostr = ObservableField("")
    val street = ObservableField("")
    val country = ObservableField("")
    val countryCode = ObservableField("")
    val buildingName = ObservableField("")
    val city = ObservableField("")
    val state = ObservableField("")
    val zipcode = ObservableField("")
    val bathroomCapacity = ObservableField<String>()
    val lat = ObservableField("")
    val lng = ObservableField("")
    val publishBoolean=ObservableField(true)
    var isPhotoAdded : Boolean = false

    var retryCalled = ""

    fun getStepDetails() : MutableLiveData<ShowListingStepsQuery.Results?>{
        if(!::stepsSummary.isInitialized) {
            stepsSummary = MutableLiveData()
            defaultSettingsInCache()
        }
        return stepsSummary
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
                } catch (e: Exception) {
                    navigator.showToast(e.message.toString())
                }
            })
    }
    fun submitForVerification(status : String) {
        val query = SubmitForVerificationMutation(
                id = listId.get()!!.toInt(),
                listApprovalStatus = status.toOptional()
        )

        compositeDisposable.add(dataManager.submitForVerification(query)
                .doOnSubscribe {  }
                .doFinally {  }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        Handler(Looper.getMainLooper()).postDelayed({
                            publishBoolean.set(true)
                        },1000)
                        val data = response.data!!.submitForVerification
                        if(data?.status == 200){
                            retryCalled=""
                            isPublish.set(false)
                            getStepsSummary()

                        }else if(data!!.status == 400){
                            navigator.showToast(data.errorMessage.toString())
                        }else{
                            navigator.openSessionExpire("")
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()

                    } }, {

                    it.printStackTrace()
                    handleException(it)
                } )
        )
    }

    fun defaultSettingsInCache() {
        val request = GetDefaultSettingQuery()

        compositeDisposable.add(dataManager.clearHttpCache()
                .doOnSubscribe { setIsLoading(true)  }
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

    fun getStepsSummary() {
        val buildQuery = ShowListingStepsQuery(
                listId = listId.get().toString()
                )
        compositeDisposable.add(dataManager.doShowListingSteps(buildQuery)
                .doOnSubscribe { setIsLoading(true)  }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data!!.showListingSteps
                        if (data!!.status == 200) {
                            retryCalled=""
                            isPublish.set(data!!.results!!.listing!!.isPublished!!)
                            listApprovelStatus.set(data!!.results!!.listing!!.listApprovalStatus)
                            isReady.set(data!!.results!!.listing!!.isReady!!)
                            step1Status = data!!.results!!.step1!!
                            step2Status = data!!.results!!.step2!!
                            step3Status = data!!.results!!.step3!!
                            isPhotoAdded = data!!.results!!.isPhotosAdded!!
                            stepsSummary.value = data!!.results

                        } else if(data.status == 500) {
                                navigator.openSessionExpire("")
                        } else {
                            data.errorMessage?.let {
                                navigator.show404Screen()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    } }, {

                    it.printStackTrace()
                    handleException(it)
                } )
        )
    }





    fun publishListing(action:String){
        val buildQuery = ManagePublishStatusMutation(
                listId = listId.get()!!.toInt(),
                action = action
        )
        compositeDisposable.add(dataManager.doManagePublishStatus(buildQuery)
                .doOnSubscribe {  }
                .doFinally {  }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        Handler(Looper.getMainLooper()).postDelayed({
                            publishBoolean.set(true)
                        },1000)
                        val data = response.data!!.managePublishStatus!!
                        if(data.status == 200){
                            retryCalled=""
                            if(action.equals("unPublish")) {
                                isPublish.set(false)
                            }
                            else{
                                isPublish.set(true)
                            }
                             getStepsSummary()
                        }else if(data!!.status == 400){
                            navigator.showToast(data.errorMessage.toString())
                        }else{
                            navigator.openSessionExpire("")
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()

                    } }, {

                    it.printStackTrace()
                    handleException(it)
                } )
        )
    }

    fun getListingDetails() {
            val buildQuery = ViewListingDetailsQuery(
                    listId = listId.get()!!.toInt(),
                    preview = true.toOptional()
            )

            compositeDisposable.add(dataManager.doListingDetailsApiCall(buildQuery)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( { response ->
                        try {
                            val data = response.data?.viewListing
                            if (data!!.status == 200) {
                                retryCalled=""
                                listingDetails.value = data.results
                                navigator.showListDetails()

                            } else if(data!!.status == 500) {
                                (navigator as BaseNavigator).openSessionExpire("")
                            }
                            else {
                                navigator.show404Screen()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, { handleException(it) }
                    )
            )
    }

}
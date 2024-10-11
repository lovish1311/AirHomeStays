package com.airhomestays.app.ui.profile.trustAndVerify

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.CodeVerificationMutation
import com.airhomestays.app.GetProfileQuery
import com.airhomestays.app.R
import com.airhomestays.app.SendConfirmEmailQuery
import com.airhomestays.app.SocialLoginVerifyMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.vo.ProfileDetails
import javax.inject.Inject

class TrustAndVerifyViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<TrustAndVerifyActivity>(dataManager,resourceProvider){

    val code = ObservableField("")
    val email = ObservableField("")
    val profileDetails = MutableLiveData<ProfileDetails?>()
    val loadedApis = MutableLiveData<ArrayList<Int>?>()

    val verificationType = MutableLiveData<String>()

    init {
        loadedApis.value = arrayListOf()
    }

    fun getProfileDetails() {
        val buildQuery = GetProfileQuery()
        compositeDisposable.add(dataManager.clearHttpCache()
                .flatMap { dataManager.doGetProfileDetailsApiCall(buildQuery).toObservable() }
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        removeApiTolist(0)
                        val data = response.data!!.userAccount
                        if (data?.status == 200) {
                            saveData(data.result!!)
                        } else if (data?.status == 500) {
                            navigator.openSessionExpire("TrustVM")
                        } else {
                            navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    it.printStackTrace()
                    addApiTolist(0)
                    handleException(it)
                } )
        )
    }

    private fun saveData(data: GetProfileQuery.Result) {
        try {
            profileDetails.value = ProfileDetails(
                    userName = data.firstName + " " + data.lastName,
                    createdAt = data.createdAt,
                    picture = data.picture,
                    email = data.email,
                    emailVerification = data.verification?.isEmailConfirmed,
                    idVerification = data.verification?.isIdVerification,
                    googleVerification= data.verification?.isGoogleConnected,
                    fbVerification = data.verification?.isFacebookConnected,
                    phoneVerification = data.verification?.isPhoneVerified,
                    userType = data.loginUserType,
                    addedList = data.isAddedList
            )

            // Save data in pref
            dataManager.currentUserProfilePicUrl = data.picture
            dataManager.currentUserFirstName = data.firstName
            dataManager.currentUserLastName = data.lastName
            dataManager.isEmailVerified = data.verification?.isEmailConfirmed
            dataManager.isIdVerified = data.verification?.isIdVerification
            dataManager.isGoogleVerified = data.verification?.isGoogleConnected
            dataManager.isFBVerified = data.verification?.isFacebookConnected
            dataManager.isPhoneVerified = data.verification?.isPhoneVerified
            dataManager.currentUserCreatedAt = data.createdAt
            dataManager.currentUserEmail = data.email
            dataManager.currentUserType = data.loginUserType
            dataManager.isListAdded = data.isAddedList

        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun sendVerifyEmail() {
        val buildQuery = SendConfirmEmailQuery()
        compositeDisposable.add(dataManager.sendConfirmationEmail(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        removeApiTolist(1)
                        val data = response.data!!.resendConfirmEmail
                        if (data!!.status == 200) {
                            navigator.moveToPreviousScreen()
                            navigator.showToast(resourceProvider.getString(R.string.confirmation_link_is_sent_to_your_email))
                        } else if(data.status == 500) {
                            navigator.openSessionExpire("TrustVM")
                        } else {
                            data.errorMessage?.let {
                                navigator.showToast(it)
                            } ?: navigator.showToast(resourceProvider.getString(R.string.invalid_link))
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                } }, {
                    addApiTolist(1)
                    it.printStackTrace()
                    handleException(it)
                } )
        )
    }

    fun sendConfirmCode() {
        try {
            val mutate = CodeVerificationMutation(
                    email = email.get()!!.toString(),
                    token = code.get()!!.toString()
            )

            compositeDisposable.add(dataManager.ConfirmCodeApiCall(mutate)
                    .performOnBackOutOnMain(scheduler)
                    .doOnSubscribe { setIsLoading(true) }
                    .doFinally { setIsLoading(false) }
                    .subscribe( { response ->
                        try {
                            removeApiTolist(2)
                            val data = response.data!!.emailVerification
                            if (data!!.status == 200) {
                                editProfileDetails("email", true)
                                getProfileDetails()
                                navigator.showToast(resourceProvider.getString(R.string.your_email_is_confirmed))
                            } else if(data.status == 500) {
                                navigator.openSessionExpire("TrustVM")
                            } else {
                                navigator.show404Error("w")
                                data.errorMessage?.let {
                                    navigator.showToast(it)
                                } ?: navigator.showToast(resourceProvider.getString(R.string.invalid_link))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navigator.showError()
                        }
                    }, {
                        addApiTolist(2)
                        handleException(it)
                    } )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun socialLoginVerify(actionType : String , type : String) {
        val buildQuery = SocialLoginVerifyMutation(
                actionType = actionType,
                verificationType = type
        )

        compositeDisposable.add(dataManager.SocialLoginVerify(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .subscribe({ response ->
                    try {
                        removeApiTolist(3)
                        val data = response.data!!.socialVerification
                        if (data!!.status == 200) {
                            if(type == "facebook") {
                                if(actionType == "true") {
                                    editProfileDetails("facebook", true)
                                    dataManager.isFBVerified = true
                                    verificationType.value = "facebook"
                                    navigator.showToast(resourceProvider.getString(R.string.facebook_connected))
                                }
                                else {
                                    editProfileDetails("facebook", false)
                                    dataManager.isFBVerified = false
                                    verificationType.value = "facebook"
                                    navigator.showToast(resourceProvider.getString(R.string.facebook_disconnected))
                                }
                            } else {
                                if(actionType == "true") {
                                    editProfileDetails("google", true)
                                    dataManager.isGoogleVerified = true
                                    verificationType.value = "google"
                                    navigator.showToast(resourceProvider.getString(R.string.google_connected))
                                }
                                else {
                                    editProfileDetails("google", false)
                                    dataManager.isGoogleVerified = false
                                    verificationType.value = "google"
                                    navigator.showToast(resourceProvider.getString(R.string.google_disconnected))
                                }
                            }
                        } else if (data.status == 500){
                            navigator.openSessionExpire("TrustVM")
                        } else{
                            navigator.showError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    addApiTolist(3)
                    handleException(it, true)
                }))
    }

    fun editProfileDetails(type: String, flag: Boolean) {
        val data = profileDetails.value
        data?.let {
            if (type == "facebook") {
                it.fbVerification = flag
            } else if (type == "google") {
                it.googleVerification = flag
            } else {
                it.emailVerification = flag
            }
        }
        profileDetails.value = data
    }

    fun addApiTolist(id: Int) {
        val api = loadedApis.value
        api?.add(id)
        loadedApis.value = api
    }

    fun removeApiTolist(id: Int) {
        val api = loadedApis.value
        api?.remove(id)
        loadedApis.value = api
    }

}
package com.airhomestays.app.ui.profile

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.Constants
import com.airhomestays.app.GetDefaultSettingQuery
import com.airhomestays.app.GetProfileQuery
import com.airhomestays.app.GetSecureSiteSettingsQuery
import com.airhomestays.app.LogoutMutation
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.ProfileDetails
import javax.inject.Inject


class ProfileViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<ProfileNavigator>(dataManager, resourceProvider) {

    lateinit var profileDetails: MutableLiveData<ProfileDetails>
    val loading = MutableLiveData<Boolean>()
    var langauge = ""

    init {
        loading.value = true
    }

    fun loadProfileDetails(): MutableLiveData<ProfileDetails> {
        if (!::profileDetails.isInitialized) {
            profileDetails = MutableLiveData()
            getDataFromPref()
        }
        return profileDetails
    }

    fun getDataFromPref() {
        profileDetails.value = ProfileDetails(
            userName = dataManager.currentUserFirstName,
            createdAt = dataManager.currentUserCreatedAt,
            picture = dataManager.currentUserProfilePicUrl,
            emailVerification = dataManager.isEmailVerified,
            idVerification = dataManager.isIdVerified,
            googleVerification = dataManager.isGoogleVerified,
            fbVerification = dataManager.isFBVerified,
            phoneVerification = dataManager.isPhoneVerified,
            email = dataManager.currentUserEmail,
            userType = dataManager.currentUserType,
            addedList = dataManager.isListAdded
        )
    }

    fun getProfileDetails() {
        navigator.showProfileDetails()

        val buildQuery = GetProfileQuery()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
            .doOnSubscribe { loading.postValue(false) }
            .doFinally { loading.postValue(true) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.userAccount
                    if (data!!.status == 500) {
                        if (dataManager.isUserLoggedIn()) navigator.openSessionExpire("")
                    }

                    responseValidation(
                        data?.status!!,
                        action = { saveData(data.result!!) },
                        data.errorMessage.toString()
                    )
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }

    private fun saveData(data: GetProfileQuery.Result) {
        try {


            profileDetails.value = ProfileDetails(
                userName = data.firstName,
                createdAt = data.createdAt,
                picture = data.picture,
                email = data.email,
                emailVerification = data.verification?.isEmailConfirmed,
                idVerification = data.verification?.isIdVerification,
                googleVerification = data.verification?.isGoogleConnected,
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
            if (data.dateOfBirth.isNullOrEmpty()) {
                dataManager.isDOB = false
            } else {
                dataManager.isDOB = true
            }

            navigator.showProfileDetails()
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun signOut(context: Context) {
        val buildQuery = LogoutMutation(
            deviceType = Constants.deviceType,
            deviceId = dataManager.firebaseToken.toString()
            )

        compositeDisposable.add(dataManager.doLogoutApiCall(buildQuery)

            .doFinally {
                afterSignOut(context)
            }
            .performOnBackOutOnMain(scheduler)
            .subscribe({
                afterSignOut(context)
                navigator.navigateToSplash()
            }, {
                afterSignOut(context)
                navigator.navigateToSplash()
            })
        )
    }

    private fun afterSignOut(context: Context) {
        dataManager.setUserAsLoggedOut()
    }

    fun defaultSettingsInCache() {
        val request = GetDefaultSettingQuery()

        compositeDisposable.add(dataManager.clearHttpCache()
            .flatMap { dataManager.doGetDefaultSettingApiCall(request).toObservable() }
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                {
                    setCurrency(it.data?.currency)
                    setSiteName()
                    setLanguage()
                    navigator.navigateToLogin()
                },
                {
                }
            ))
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

    private fun setCurrency(it: GetDefaultSettingQuery.Currency?) {
        try {
            if (it?.status == 200) {
                if (dataManager.currentUserCurrency == null) {
                    dataManager.currentUserCurrency = it.result!!.base!!
                }
                dataManager.currencyBase = it.result!!.base
                dataManager.currencyRates = it.result!!.rates
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
        }
    }

    private fun setLanguage() {
        try {
            if (dataManager.currentUserLanguage == null) {
                dataManager.currentUserLanguage = langauge

            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
        }
    }

}
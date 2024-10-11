package com.airhomestays.app.ui.auth

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.airhomestays.app.Constants
import com.airhomestays.app.ForgotPasswordVerificationQuery
import com.airhomestays.app.GetProfileQuery
import com.airhomestays.app.R
import com.airhomestays.app.SignupMutation
import com.airhomestays.app.SocialLoginQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Event
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.FromDeeplinks
import com.airhomestays.app.vo.Outcome
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<AuthNavigator>(dataManager, resourceProvider) {

    val showPassword = ObservableField(false)

    val firstName = ObservableField("")
    val lastName = ObservableField("")
    val email = ObservableField("")
    val password = ObservableField("")
    val dateOfBirth = ObservableField("")
    val registerType = MutableLiveData<String>()
    val profilePic = MutableLiveData<String>()
    val movetohome = MutableLiveData<Boolean>()
    val appTheme = ObservableField("")

    val token = MutableLiveData<String>().apply { value = "" }
    val resetEmail = MutableLiveData<String>().apply { value = "" }
    val deeplinks = MutableLiveData<Int>().apply { value = -1 }

    val currentScreen = MutableLiveData<AuthViewModel.Screen>()

    val generateFirebase = MutableLiveData<String>()
    val fireBaseResponse: LiveData<Event<Outcome<String>>>? = generateFirebase.switchMap {
        dataManager.generateFirebaseToken()
    }

    init {
        dataManager.generateFirebaseToken()
    }

    fun validateDetails(): Boolean {
        return try {
            (!(!firstName.get()!!.isNotEmpty() || !lastName.get()!!.isNotEmpty() ||
                    !email.get()!!.isNotEmpty() || !dataManager.firebaseToken!!.isNotEmpty()))
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            false
        }
    }

    fun validateDetailForEmail(): Boolean {
        return try {
            (!(!password.get()!!.isNotEmpty() || !dateOfBirth.get()!!.isNotEmpty()))
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            false
        }
    }

    fun validateFirebase(): Boolean {
        return dataManager.firebaseToken != null
    }

    fun validateForgotPassToken(): Boolean {
        return !(resetEmail.value.isNullOrEmpty() && token.value.isNullOrEmpty())
    }

    fun isFromDeeplink(): Boolean {
        return !resetEmail.value.isNullOrEmpty() && !token.value.isNullOrEmpty()
    }

    fun signupUser() {
        val buildQuery = SignupMutation(
            firstName = firstName.get().toOptional(),
            lastName = lastName.get().toOptional(),
            email = email.get()!!,
            password = password.get()!!,
            dateOfBirth = dateOfBirth.get().toOptional(),
            deviceId = dataManager.firebaseToken!!,
            deviceType = Constants.deviceType,
            deviceDetail = "".toOptional(),
            registerType = Constants.registerTypeEMAIL.toOptional()
        )

        compositeDisposable.add(dataManager.doSignupApiCall(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response.data?.createUser?.status == 200) {
                        val data = response.data!!.createUser!!.result
                        saveDataInPrefForEmail(data!!)
                        navigator.navigateScreen(Screen.MOVETOHOME)
                    } else {
                        navigator.showSnackbar(
                            resourceProvider.getString(R.string.sign_up_error),
                            response.data?.createUser?.errorMessage.toString()
                        )
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, { handleException(it) })
        )
    }

    fun socialLogin(registerType: String) {
        val buildQuery = SocialLoginQuery(
            firstName = firstName.get().toOptional(),
            lastName = lastName.get().toOptional(),
            email = email.get()!!,
            profilePicture = profilePic.value.toOptional(),
            deviceId =dataManager.firebaseToken!!,
            deviceType =Constants.deviceType,
            deviceDetail ="".toOptional(),
            registerType = registerType.toOptional(),
        )

        compositeDisposable.add(dataManager.doSocailLoginApiCall(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response.data?.userSocialLogin?.status == 200) {
                        val data = response.data!!.userSocialLogin
                        val loginMode = if (registerType == Constants.registerTypeFB) {
                            DataManager.LoggedInMode.LOGGED_IN_MODE_FB
                        } else {
                            DataManager.LoggedInMode.LOGGED_IN_MODE_GOOGLE
                        }
                        saveDataInPrefForSocial(data!!.result, loginMode)
                        if (isFromDeeplink() && deeplinks.value == FromDeeplinks.EmailVerification.ordinal) {
                            navigator.navigateScreen(Screen.MOVETOEMAILVERIFY)
                        } else {
                            navigator.navigateScreen(Screen.MOVETOHOME_DARK_MODE)
                        }
                    } else if (response.data?.userSocialLogin?.status == 500) {
                        if ("blocked" in (response.data?.userSocialLogin?.errorMessage ?: "")) {
                            navigator.showToast(
                                response.data?.userSocialLogin?.errorMessage ?: ""

                            )
                        }
                    } else {
                        navigator.showToast(
                            response.data?.userSocialLogin?.errorMessage!!
                        )
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, { handleException(it) })
        )
    }

    fun tokenVerification() {
        val buildQuery = ForgotPasswordVerificationQuery(
            email = resetEmail.value!!,
            token = token.value!!
        )

        compositeDisposable.add(dataManager.doForgotPasswordVerificationApiCall(buildQuery)
            .doOnSubscribe { setIsLoading(true); dataManager.isUserFromDeepLink = true }
            .doFinally { setIsLoading(false); dataManager.isUserFromDeepLink = false }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response?.data?.verifyForgotPassword?.errorMessage == null) {
                        navigator.showSnackbar(
                            resourceProvider.getString(R.string.reset_password_error),
                            resourceProvider.getString(R.string.reset_password_error_desc)
                        )
                    } else {
                        navigator.showToast(
                            response?.data?.verifyForgotPassword?.errorMessage.toString()
                        )
                    }

                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                }
            }, {
                //handleException(it)
            })
        )
    }

    fun showPassword() {
        showPassword.set(showPassword.get()?.not())
    }

    fun getLoginstatus(): Boolean {
        return dataManager.currentUserLoggedInMode != DataManager.LoggedInMode.LOGGED_IN_MODE_LOGGED_OUT.type
    }

    private fun saveDataInPrefForEmail(data: SignupMutation.Result?) {
        try {
            val userCurrency: String? = if (data?.user?.preferredCurrency == null) {
                dataManager.currencyBase!!
            } else {
                data.user?.preferredCurrency!!
            }
            dataManager.updateUserInfo(
                data?.userToken,
                data?.userId,
                DataManager.LoggedInMode.LOGGED_IN_MODE_SERVER,
                data?.user?.firstName + " " + data?.user?.lastName,
                null,
                data?.user?.picture,
                userCurrency,
                data?.user?.preferredLanguage,
                data?.user?.createdAt
            )
            dataManager.updateVerification(
                data?.user?.verification?.isPhoneVerified!!,
                data.user?.verification?.isEmailConfirmed!!,
                data.user?.verification?.isIdVerification!!,
                data.user?.verification?.isGoogleConnected!!,
                data.user?.verification?.isFacebookConnected!!
            )
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    private fun saveDataInPrefForSocial(
        data: SocialLoginQuery.Result?,
        loginMode: DataManager.LoggedInMode
    ) {
        try {
            val userCurrency: String? = if (data?.user?.preferredCurrency == null) {
                dataManager.currencyBase!!
            } else {
                data?.user?.preferredCurrency!!
            }
            dataManager.updateVerification(
                data?.user?.verification?.isPhoneVerified,
                data?.user?.verification?.isEmailConfirmed,
                data?.user?.verification?.isIdVerification,
                data?.user?.verification?.isGoogleConnected,
                data?.user?.verification?.isFacebookConnected
            )
            dataManager.updateUserInfo(
                data?.userToken,
                data?.userId,
                loginMode,
                data?.user?.firstName + " " + data?.user?.lastName,
                null,
                data?.user?.picture,
                userCurrency,
                data?.user?.preferredLanguage,
                data?.user?.createdAt
            )
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            dataManager.setUserAsLoggedOut()
            navigator.showError()
        }
    }

    fun setFirstName(value: String?) {
        firstName.set(value)
    }

    fun setLastName(value: String?) {
        lastName.set(value)
    }

    fun setEmail(value: String?) {
        email.set(value)
    }

    fun setPassword(value: String?) {
        password.set(value)
    }

    fun setBirthday(value: String?) {
        dateOfBirth.set(value)
    }

    enum class LottieProgress {
        NORMAL,
        LOADING,
        CORRECT
    }

    enum class Screen {
        SIGNUP,
        NAME,
        EMAIL,
        PASSWORD,
        BIRTHDAY,
        LOGIN,
        LOGINWITHPARAM,
        FORGOTPASSWORD,
        CHANGEPASSWORD,
        REMOVEALLBACKSTACK,
        POPUPSTACK,
        FB,
        GOOGLE,
        HOME,
        MOVETOHOME,
        MOVETOHOME_DARK_MODE,
        MOVETOEMAILVERIFY,
        AuthScreen,
        PHONENUMBER,
        CODE,
        CREATELIST
    }

    fun clearCompositeDisposal() {
        compositeDisposable.clear()
    }

    fun resetValues() {
        firstName.set("")
        lastName.set("")
        email.set("")
        password.set("")
        dateOfBirth.set("")
        registerType.value = ""
        profilePic.value = ""
    }

    fun getProfileDetails() {

        val buildQuery = GetProfileQuery()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.userAccount
                    if (data!!.status == 500) {
                        if (dataManager.isUserLoggedIn()) navigator.openSessionExpire("")
                    }

                    if (!data.result?.appTheme.isNullOrEmpty()) {
                        appTheme.set(data.result?.appTheme)
                        movetohome.value = true
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }
}
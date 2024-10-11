package com.airhomestays.app.ui.auth.name

import androidx.databinding.ObservableField
import com.airhomestays.app.CheckEmailExistsQuery
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.SignupMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.auth.AuthNavigator
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import javax.inject.Inject

class NameCreationViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<AuthNavigator>(dataManager, resourceProvider) {

    val firstName = ObservableField("")
    val lastName = ObservableField("")
    val email = ObservableField("")
    val isEmailValid = ObservableField(false)
    val password = ObservableField("")
    val emailError = ObservableField(false)
    val passwordError = ObservableField(false)
    val showPassword = ObservableField(false)
    var isbirthdaySelected = false
    val dob = ObservableField<Array<Int>>()
    val dobString = ObservableField("")
    val yearLimit = ObservableField<Array<Int>>()

    val lottieProgress =
        ObservableField<AuthViewModel.LottieProgress>(AuthViewModel.LottieProgress.NORMAL)


    init {
        dob.set(Utils.get18YearLimit())
    }

    fun checkEmail() {
        navigator.hideSnackbar()
        navigator.hideKeyboard()
        if (Utils.isValidEmail(email.get()!!)) {
            if (password.get()!!.isBlank() || password.get()!!.length < 8) {
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.password_error),
                    resourceProvider.getString(R.string.invalid_password_desc)
                )
            } else {
                if (isbirthdaySelected) {
                    lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
                    emailVerification()
                } else {
                    navigator.showSnackbar(
                        resourceProvider.getString(R.string.invalid_birth),
                        resourceProvider.getString(R.string.invalid_birth_desc)
                    )
                }
            }

        } else {
            navigator.showSnackbar(
                resourceProvider.getString(R.string.invalid_email),
                resourceProvider.getString(R.string.invalid_email_desc)
            )
        }
    }

    fun onEmailTextChanged() {
        emailError.set(false)
    }

    private fun emailVerification() {
        val buildQuery = CheckEmailExistsQuery(email = email.get()!!)

        compositeDisposable.add(dataManager.doEmailVerificationApiCall(buildQuery)
            .performOnBackOutOnMain(scheduler)
            .doOnSubscribe {
                setIsLoading(true)
                lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
            }
            .doFinally { setIsLoading(false) }
            .subscribe({ response ->
                try {
                    if (response.data?.validateEmailExist?.status == 200) {
                        lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
                        isEmailValid.set(true)
                        signupUser()
                    } else if (response.data?.validateEmailExist?.status == 500) {
                        isEmailValid.set(false)
                        navigator.openSessionExpire("NameCreationVM")
                    } else {
                        isEmailValid.set(false)
                        lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                        navigator.showToast(
                            response.data?.validateEmailExist?.errorMessage.toString()
                        )
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                handleException(it)
            })
        )
    }


    fun onPasswordTextChanged() {
        if (passwordError.get()!!) {
            navigator.hideSnackbar()
        }
        passwordError.set(false)
    }

    fun showPassword() {
        showPassword.set(showPassword.get()?.not())
    }


    fun showError() {
        navigator.showSnackbar(
            resourceProvider.getString(R.string.birthday_error),
            resourceProvider.getString(R.string.to_sign_up)
        )
    }


    fun signupUser() {
        var dateOfBirth = ""
        try {
            dateOfBirth = dob.get()!![1].plus(1).toString() + "-" + dob.get()!![0].toString() + "-" + dob.get()!![2].toString()
        } catch (e: Exception) {
            e.printStackTrace()

        }

        val buildQuery = SignupMutation(
            firstName = firstName.get().toOptional(),
            lastName = lastName.get().toOptional(),
            email = email.get()!!,
            password = password.get()!!,
            dateOfBirth = dateOfBirth.toOptional(),
            deviceId = dataManager.firebaseToken!!,
            deviceType = Constants.deviceType,
            deviceDetail = "".toOptional(),
            registerType = Constants.registerTypeEMAIL.toOptional(),
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
                        navigator.navigateScreen(AuthViewModel.Screen.MOVETOHOME)
                    } else if (response.data?.createUser?.status == 500) {
                        navigator.openSessionExpire("")
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

    private fun saveDataInPrefForEmail(data: SignupMutation.Result?) {
        try {
            val userCurrency: String? = if (data?.user?.preferredCurrency == null) {
                dataManager.currencyBase!!
            } else {
                data.user?.preferredCurrency
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
                data.user.verification.isEmailConfirmed!!,
                data.user.verification.isIdVerification!!,
                data.user.verification.isGoogleConnected!!,
                data.user.verification.isFacebookConnected!!
            )
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }
}

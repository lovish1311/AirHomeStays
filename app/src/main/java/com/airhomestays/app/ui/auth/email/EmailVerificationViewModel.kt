package com.airhomestays.app.ui.auth.email

import androidx.databinding.ObservableField
import com.airhomestays.app.CheckEmailExistsQuery
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.auth.AuthNavigator
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import javax.inject.Inject

class EmailVerificationViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<AuthNavigator>(dataManager, resourceProvider) {

    val email = ObservableField("")
    val emailError = ObservableField(false)
    val lottieProgress =
        ObservableField<AuthViewModel.LottieProgress>(AuthViewModel.LottieProgress.NORMAL)

    fun checkEmail() {
        navigator.hideSnackbar()
        navigator.hideKeyboard()
        if (Utils.isValidEmail(email.get()!!)) {
            lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
            emailVerification()
        } else {
            emailError.set(true)
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
        val buildQuery = CheckEmailExistsQuery(
            email = email.get()!!
        )

        compositeDisposable.add(dataManager.doEmailVerificationApiCall(buildQuery)
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    if (response.data?.validateEmailExist?.status == 200) {
                        lottieProgress.set(AuthViewModel.LottieProgress.CORRECT)
                        navigator.navigateScreen(AuthViewModel.Screen.PASSWORD, email.get()!!)
                    } else if (response.data?.validateEmailExist?.status == 500) {
                        navigator.openSessionExpire("EmailVerifyVM")
                    } else {
                        lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                        navigator.showSnackbar(
                            resourceProvider.getString(R.string.email_already_exists),
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

}
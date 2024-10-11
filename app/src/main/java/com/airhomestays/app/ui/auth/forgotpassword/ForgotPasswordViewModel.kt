package com.airhomestays.app.ui.auth.forgotpassword

import androidx.databinding.ObservableField
import com.airhomestays.app.ForgotPasswordMutation
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.auth.AuthNavigator
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import javax.inject.Inject

class ForgotPasswordViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    private val resourceProvider: ResourceProvider
) : BaseViewModel<AuthNavigator>(dataManager, resourceProvider) {

    val email = ObservableField("")
    val lottieProgress =
        ObservableField<AuthViewModel.LottieProgress>(AuthViewModel.LottieProgress.NORMAL)

    fun emailValidation() {
        navigator.hideSnackbar()
        navigator.hideKeyboard()
        try {
            if (email.get()!!.isNotEmpty()) {
                forgotPassword()
            } else {
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.invalid_email),
                    resourceProvider.getString(R.string.invalid_email_desc)
                )
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    private fun forgotPassword() {
        val buildQuery = ForgotPasswordMutation(
            email =email.get()!!
        )


        compositeDisposable.add(dataManager.doForgotPasswordApiCall(buildQuery)
            .doOnSubscribe {
                setIsLoading(true)
                lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
            }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.userForgotPassword
                    if (data?.status == 200) {
                        lottieProgress.set(AuthViewModel.LottieProgress.CORRECT)
                        navigator.showToast(
                            resourceProvider.getString(
                                R.string.link_msg,
                                email.get()!!
                            )
                        )
                        navigator.navigateScreen(AuthViewModel.Screen.AuthScreen)
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("")
                    } else {
                        lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                        navigator.showToast(data?.errorMessage!!)
                        navigator.navigateScreen(AuthViewModel.Screen.REMOVEALLBACKSTACK)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                    lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                }
            }, {
                handleException(it)
                lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
            })
        )
    }

}
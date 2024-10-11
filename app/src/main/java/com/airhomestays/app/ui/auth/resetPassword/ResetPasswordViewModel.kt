package com.airhomestays.app.ui.auth.resetPassword

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.R
import com.airhomestays.app.ResetPasswordMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.auth.AuthNavigator
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ResetPasswordViewModel @Inject constructor(
    dataManager: DataManager,
    val resourceProvider: ResourceProvider,
    private val scheduler: Scheduler
) : BaseViewModel<AuthNavigator>(dataManager, resourceProvider) {

    init {
        dataManager.isUserFromDeepLink = true
    }

    val password = ObservableField("")
    val confirmPassword = ObservableField("")
    val lottieProgress =
        ObservableField<AuthViewModel.LottieProgress>(AuthViewModel.LottieProgress.NORMAL)

    val showPassword = ObservableField(false)
    val showPassword1 = ObservableField(false)

    val email = MutableLiveData<String>()
    val token = MutableLiveData<String>()

    fun showPassword() {
        showPassword.set(showPassword.get()?.not())
    }

    fun showPassword1() {
        showPassword1.set(showPassword1.get()?.not())
    }

    fun validateData() {
        try {
            if (email.value!!.isNotEmpty() &&
                password.get()!!.isNotEmpty() &&
                token.value!!.isNotEmpty()
            ) {
                if(password.get()!!.isBlank() || password.get()!!.length < 8){
                    navigator.showSnackbar(
                        resourceProvider.getString(R.string.password_error),
                        resourceProvider.getString(R.string.invalid_password_desc)
                    )
                }else{
                    if (password.get().equals(confirmPassword.get())) {
                        navigator.hideKeyboard()
                        resetPassword()
                    } else {
                        navigator.showSnackbar(
                            "",
                            resourceProvider.getString(R.string.password_limit_error)
                        )
                    }



                }


            } else {
                navigator.showSnackbar("", resourceProvider.getString(R.string.invalid_password))
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun resetPassword() {
        lottieProgress.set(AuthViewModel.LottieProgress.LOADING)
        val buildQuery = ResetPasswordMutation(
            email = email.value!!,
            password = password.get()!!,
            token = token.value!!
        )

        compositeDisposable.add(dataManager.doResetPasswordApiCall(buildQuery)
            .delay(5, TimeUnit.SECONDS)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.updateForgotPassword
                    if (data?.status == 200) {
                        lottieProgress.set(AuthViewModel.LottieProgress.CORRECT)
                        navigator.showToast(resourceProvider.getString(R.string.success_reset_msg))
                        dataManager.setUserAsLoggedOut()
                        navigator.navigateScreen(AuthViewModel.Screen.AuthScreen)
                    } else if (data?.status == 500) {
                        lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                        if (data.errorMessage == null)
                            navigator.showSnackbar(
                                resourceProvider.getString(R.string.reset_password_error),
                                resourceProvider.getString(R.string.reset_password_error_desc)
                            )
                        else navigator.showToast(data.errorMessage.toString())
                    } else {
                        lottieProgress.set(AuthViewModel.LottieProgress.NORMAL)
                        navigator.showToast(data?.errorMessage!!)
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

    override fun onCleared() {
        dataManager.isUserFromDeepLink = false
        super.onCleared()
    }
}
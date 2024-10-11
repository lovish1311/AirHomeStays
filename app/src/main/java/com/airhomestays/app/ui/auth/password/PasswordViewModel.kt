package com.airhomestays.app.ui.auth.password

import androidx.databinding.ObservableField
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.auth.AuthNavigator
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import javax.inject.Inject

class PasswordViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<AuthNavigator>(dataManager, resourceProvider) {

    val password = ObservableField("")
    val passwordError = ObservableField(false)
    val showPassword = ObservableField(false)

    fun checkPassword() {
        try {
            navigator.hideKeyboard()
            if (password.get()!!.trim().length > 7) {
                navigator.navigateScreen(AuthViewModel.Screen.BIRTHDAY, password.get())
            } else {
                passwordError.set(true)
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.password_error),
                    resourceProvider.getString(R.string.password_limit)
                )
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
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

}
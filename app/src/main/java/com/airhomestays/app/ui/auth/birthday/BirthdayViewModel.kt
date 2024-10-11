package com.airhomestays.app.ui.auth.birthday

import androidx.databinding.ObservableField
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.auth.AuthNavigator
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.resource.ResourceProvider
import javax.inject.Inject

class BirthdayViewModel @Inject constructor(
    dataManager: DataManager,
    private val resourceProvider: ResourceProvider
) : BaseViewModel<AuthNavigator>(dataManager, resourceProvider) {

    val dob = ObservableField<Array<Int>>()
    val yearLimit = ObservableField<Array<Int>>()

    init {
        dob.set(Utils.get18YearLimit())
    }

    fun showError() {
        navigator.showSnackbar(
            resourceProvider.getString(R.string.birthday_error),
            resourceProvider.getString(R.string.to_sign_up)
        )
    }

    fun signUpUser() {
        try {
            navigator.navigateScreen(
                AuthViewModel.Screen.HOME,
                dob.get()!![1].plus(1)
                    .toString() + "-" + dob.get()!![0].toString() + "-" + dob.get()!![2].toString()
            )
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

}
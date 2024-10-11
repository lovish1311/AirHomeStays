package com.airhomestays.app.ui.profile.confirmPhonenumber

import com.airhomestays.app.ui.base.BaseNavigator

interface ConfirmPhnoNavigator : BaseNavigator {

    fun navigateScreen(PHScreen: ConfirmPhnoViewModel.PHScreen, vararg params: String?)

}
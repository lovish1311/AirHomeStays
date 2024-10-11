package com.airhomestays.app.host.photoUpload

import com.airhomestays.app.ui.base.BaseNavigator

interface Step2Navigator : BaseNavigator {

    fun navigateToScreen(screen : Step2ViewModel.NextScreen, vararg params: String?)

    fun navigateBack(backScreen : Step2ViewModel.BackScreen)

    fun show404Page()


}
package com.airhomestays.app.ui.host.step_one

import com.airhomestays.app.ui.base.BaseNavigator

interface StepOneNavigator : BaseNavigator {

    fun navigateScreen(NextScreen: StepOneViewModel.NextScreen)

    fun navigateBack(BackScreen : StepOneViewModel.BackScreen)

    fun show404Page()

}
package com.airhomestays.app.ui.host.step_two

import com.airhomestays.app.ui.base.BaseNavigator

interface StepTwoNavigator : BaseNavigator {

    fun navigateToScreen(screen : StepTwoViewModel.NextScreen,vararg params: String?)

}
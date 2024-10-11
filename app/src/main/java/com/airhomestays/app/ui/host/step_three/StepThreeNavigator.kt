package com.airhomestays.app.ui.host.step_three

import com.airhomestays.app.ui.base.BaseNavigator

interface StepThreeNavigator : BaseNavigator {

    fun navigateToScreen(screen : StepThreeViewModel.NextStep)

    fun navigateBack(backScreen : StepThreeViewModel.BackScreen)

    fun show404Page()
}
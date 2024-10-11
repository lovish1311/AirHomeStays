package com.airhomestays.app.ui.profile.about

import com.airhomestays.app.ui.base.BaseNavigator


interface AboutNavigator : BaseNavigator{

    fun navigateScreen(OpenScreen: AboutViewModel.OpenScreen, vararg params: String?)
}
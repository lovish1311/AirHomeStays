package com.airhomestays.app.ui.profile.setting

import com.airhomestays.app.ui.base.BaseNavigator

interface SettingsNavigator : BaseNavigator  {

    fun openSplashScreen()

    fun navigateToSplash()

    fun setLocale(key: String)

    fun finishActivity()

}
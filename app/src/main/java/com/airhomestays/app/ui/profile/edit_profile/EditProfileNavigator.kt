package com.airhomestays.app.ui.profile.edit_profile

import com.airhomestays.app.ui.base.BaseNavigator


interface EditProfileNavigator: BaseNavigator {

    fun openEditScreen()

    fun openSplashScreen()

    fun moveToBackScreen()

    fun showLayout()

    fun setLocale(key: String)
}
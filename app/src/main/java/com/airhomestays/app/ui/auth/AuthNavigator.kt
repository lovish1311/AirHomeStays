package com.airhomestays.app.ui.auth

import com.airhomestays.app.ui.base.BaseNavigator

interface AuthNavigator: BaseNavigator {

    fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?)

}
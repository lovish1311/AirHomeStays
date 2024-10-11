package com.airhomestays.app.ui.profile.trustAndVerify

import com.airhomestays.app.ui.base.BaseNavigator

interface TrustAndVerifyNavigator : BaseNavigator{

    fun show404Error(message : String )

    fun navigateToSplash()

    fun moveToPreviousScreen()
}
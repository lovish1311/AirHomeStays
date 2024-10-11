package com.airhomestays.app.ui.auth.signup

import com.airhomestays.app.ui.auth.AuthViewModel

interface SignupNavigator {

/*    fun onCreateAccountButtonClick()

    fun onFacebookButtonClick()

    fun onGoogleButtonClick()

    fun onLoginButtonClick()

    fun onCloseButtonClick()
    */

    fun onButtonClick(screen: AuthViewModel.Screen)

}
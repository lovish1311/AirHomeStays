package com.airhomestays.app.ui.profile.manageAccount

import com.airhomestays.app.ui.base.BaseNavigator


interface ManageAccountNavigator : BaseNavigator{
    fun navigateScreen(OpenScreen: ManageAccountViewModel.OpenScreen, vararg params: String?)
    fun closeDialog()
}
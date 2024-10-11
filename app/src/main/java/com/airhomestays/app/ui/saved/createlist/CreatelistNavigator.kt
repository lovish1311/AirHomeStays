package com.airhomestays.app.ui.saved.createlist

import com.airhomestays.app.ui.base.BaseNavigator

interface CreatelistNavigator: BaseNavigator {

    fun navigate(isLoadSaved: Boolean)

}
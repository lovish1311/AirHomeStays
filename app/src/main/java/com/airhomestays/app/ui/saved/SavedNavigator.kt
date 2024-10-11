package com.airhomestays.app.ui.saved

import com.airhomestays.app.ui.base.BaseNavigator

interface SavedNavigator : BaseNavigator {

    fun moveUpScreen()

    fun showEmptyMessageGroup()

    fun reloadExplore()
}
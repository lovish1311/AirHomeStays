package com.airhomestays.app.ui

import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import javax.inject.Inject

class AuthTokenViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<BaseNavigator>(dataManager,resourceProvider) {

    init {
        dataManager.setUserAsLoggedOut()
    }
}
package com.airhomestays.app.ui.dobcalendar

import android.annotation.SuppressLint
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import javax.inject.Inject

@SuppressLint("LogNotTimber")
class BirthdayViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<BaseNavigator>(dataManager,resourceProvider) {

}
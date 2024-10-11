package com.airhomestays.app.ui.inbox

import com.airhomestays.app.SendMessageMutation
import com.airhomestays.app.ui.base.BaseNavigator

interface InboxNavigator: BaseNavigator {

    fun moveToBackScreen()

    fun addMessage(text: SendMessageMutation.Results)

    fun openBillingActivity()

    fun openListingDetails()

    fun hideTopView(msg:String)
}
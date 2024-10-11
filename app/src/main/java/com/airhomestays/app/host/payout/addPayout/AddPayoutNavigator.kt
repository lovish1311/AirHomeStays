package com.airhomestays.app.host.payout.addPayout

import com.airhomestays.app.ui.base.BaseNavigator


interface AddPayoutNavigator: BaseNavigator {

    fun moveToScreen(screen: AddPayoutActivity.Screen)
}
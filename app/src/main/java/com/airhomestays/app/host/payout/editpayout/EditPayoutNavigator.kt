package com.airhomestays.app.host.payout.editpayout

import com.airhomestays.app.ui.base.BaseNavigator

interface EditPayoutNavigator: BaseNavigator {

    fun disableCountrySearch(flag: Boolean)
    fun moveToScreen(screen: EditPayoutActivity.Screen)
}
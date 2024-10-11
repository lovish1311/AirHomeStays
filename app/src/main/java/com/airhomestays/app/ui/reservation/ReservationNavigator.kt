package com.airhomestays.app.ui.reservation

import com.airhomestays.app.ui.base.BaseNavigator

interface ReservationNavigator: BaseNavigator {

    fun navigateToScreen(screen : Int)

    fun show404Page()

}
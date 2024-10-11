package com.airhomestays.app.ui.profile.review

import android.view.View
import com.airhomestays.app.ui.base.BaseNavigator

interface ReviewNavigator : BaseNavigator{

    fun moveToScreen(screen: ReviewViewModel.ViewScreen)

    fun openWriteReview(reservationId: Int)

    fun show404Page()
}
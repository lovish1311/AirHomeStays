package com.airhomestays.app.ui.listing

import com.airhomestays.app.ui.base.BaseNavigator

interface ListingNavigator : BaseNavigator {

    fun openBillingActivity(isProfilePresent: Boolean)

    fun openPriceBreakdown()

    fun removeSubScreen()

    fun show404Screen()

    fun showReportScreen()
}
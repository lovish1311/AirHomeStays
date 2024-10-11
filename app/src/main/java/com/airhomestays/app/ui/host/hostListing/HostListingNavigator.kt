package com.airhomestays.app.ui.host.hostListing

import com.airhomestays.app.ui.base.BaseNavigator

interface HostListingNavigator : BaseNavigator {
    fun show404Screen()

    fun showListDetails()

    fun showNoListMessage()

    fun hideLoading()
}
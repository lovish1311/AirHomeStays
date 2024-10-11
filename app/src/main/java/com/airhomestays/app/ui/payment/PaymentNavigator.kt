package com.airhomestays.app.ui.payment

import com.airhomestays.app.ui.base.BaseNavigator

interface PaymentNavigator: BaseNavigator {

    fun moveToReservation(id: Int)

    fun finishScreen()

    fun moveToPayPalWebView(redirectUrl: String)

    fun moveToRazorPay(amount: String, orderId: String)
}
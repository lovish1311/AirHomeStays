package com.airhomestays.app.ui.payment.razorpay

data class RazorPayRequest(
    val amount: Double,
    val currency: String
)
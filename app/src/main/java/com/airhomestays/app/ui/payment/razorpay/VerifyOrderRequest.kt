package com.airhomestays.app.ui.payment.razorpay

data class VerifyOrderRequest(
    val razorpay_payment_id: String,
    val razorpay_order_id: String,
    val razorpay_signature: String,
    val reservation_id: Int,
    val total_amount: Double,
)
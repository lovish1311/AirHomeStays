package com.airhomestays.app.data.remote

import com.airhomestays.app.ui.payment.razorpay.RazorPayRequest
import com.airhomestays.app.ui.payment.razorpay.RazorPayResponse
import com.airhomestays.app.ui.payment.razorpay.VerifyOrderRequest
import com.airhomestays.app.ui.payment.razorpay.VerifyOrderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RazorPayService {
    @POST("createOrder")
    suspend fun createOrder(@Body request: RazorPayRequest): Response<RazorPayResponse>

    @POST("verifyPayment")
    suspend fun verifyOrder(@Body request: VerifyOrderRequest): Response<VerifyOrderResponse>
}
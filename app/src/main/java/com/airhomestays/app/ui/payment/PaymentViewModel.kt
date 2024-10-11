package com.airhomestays.app.ui.payment

import android.content.Intent
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.exception.ApolloNetworkException
import com.airhomestays.app.ConfirmPayPalExecuteMutation
import com.airhomestays.app.ConfirmReservationMutation
import com.airhomestays.app.CreateReservationMutation
import com.airhomestays.app.GetCurrenciesListQuery
import com.airhomestays.app.GetPaymentMethodsQuery
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.remote.RetrofitInstance
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.ui.payment.razorpay.RazorPayRequest
import com.airhomestays.app.ui.payment.razorpay.VerifyOrderRequest
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.BillingDetails
import com.airhomestays.app.vo.Outcome
import com.stripe.android.Stripe
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PaymentViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<PaymentNavigator>(dataManager, resourceProvider) {

    val stripeCard = MutableLiveData<Card?>()
    val billingDetails = MutableLiveData<BillingDetails>()
    val msg = MutableLiveData<String>()
    val token = MutableLiveData<String?>()
    var razorPayOrderId = MutableLiveData<String?>()
    var selectedPaymentType = 0
    var selectedCurrency = ObservableField("")
    val paymentIntentSecret = MutableLiveData<String>()
    val stripeReqAdditionAction = MutableLiveData<String>()
    val paymentIntentLiveData = MutableLiveData<String>()
    val reservationId = MutableLiveData<Int>()
    var currencies: MutableLiveData<List<GetCurrenciesListQuery.Result?>> = MutableLiveData()
    val stripeResponse: LiveData<Outcome<Token>>? = stripeCard.switchMap() {
        it?.let { it1 -> dataManager.createToken(it1) }
    }
    lateinit var stripe: Stripe

    lateinit var paymentMethods: MutableLiveData<List<GetPaymentMethodsQuery.Result?>?>

    init {
        selectedCurrency.set(resourceProvider.getString(R.string.currency))
    }

    fun loadPayoutMethods(): MutableLiveData<List<GetPaymentMethodsQuery.Result?>?> {
        if (!::paymentMethods.isInitialized) {
            paymentMethods = MutableLiveData()
            getPayoutMethods()
        }
        return paymentMethods
    }

    fun initData(intent: Intent?) {
        intent?.let {
            billingDetails.value = intent.getParcelableExtra("billingDetails")
            msg.value = intent.getStringExtra("msg")
        }
    }

    fun validateToken() {
        token.value?.let {
            if (it.isNotEmpty()) {
                createReservation(it)

            }
        } ?: navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
    }

    fun createReservation(stripeToken: String) {
        try {
            val query = CreateReservationMutation(
                cardToken = "",
                basePrice = billingDetails.value!!.basePrice,
                bookingType = billingDetails.value!!.bookingType.toOptional(),
                checkIn = billingDetails.value!!.checkIn,
                checkOut = billingDetails.value!!.checkOut,
                cleaningPrice = billingDetails.value!!.cleaningPrice,
                currency = billingDetails.value!!.currency,
                discount = billingDetails.value!!.discount.toOptional(),
                discountType = billingDetails.value!!.discountLabel.toOptional(),
                guestServiceFee = billingDetails.value!!.guestServiceFee.toOptional(),
                guests = billingDetails.value!!.guest,
                pets = billingDetails.value!!.pets,
                infants = billingDetails.value!!.infantCount,
                visitors = billingDetails.value!!.visitors,
                hostServiceFee = billingDetails.value!!.hostServiceFee.toOptional(),
                listId = billingDetails.value!!.listId,
                total = billingDetails.value!!.total,
                message = msg.value!!,
                paymentType = selectedPaymentType.toOptional(),
                convCurrency = getUserCurrency(),
                averagePrice = billingDetails.value!!.averagePrice.toOptional(),
                nights = billingDetails.value!!.nights.toOptional(),
                specialPricing = billingDetails.value!!.specialPricing,
                paymentCurrency = selectedCurrency.get().toOptional(),
                threadId = billingDetails.value!!.threadId.toOptional(),
                petPrice = billingDetails.value!!.pets * billingDetails.value!!.petPrice.toDouble(),
                infantPrice = billingDetails.value!!.infantCount * billingDetails.value!!.infantPrice.toDouble(),
                visitorsPrice = billingDetails.value!!.visitors * billingDetails.value!!.visitorPrice.toDouble(),
                additionalPrice = billingDetails.value!!.additionalGuest * billingDetails.value!!.additionalGuestPrice,
                additionalGuest = billingDetails.value!!.additionalGuest,
                razorpayOrderId = razorPayOrderId.value!!,
                razorpayPaymentId =  stripeToken

            )

            compositeDisposable.add(dataManager.createReservation(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({
                    try {
                        token.value = null
                        if (it.data?.createReservation?.status == 200) {
                            if (selectedPaymentType == 1) {
                                if (it.data?.createReservation?.redirectUrl != null) {
                                    setIsLoading(true)
                                    reservationId.value =
                                        it.data?.createReservation?.reservationId ?: 0
                                    navigator.moveToPayPalWebView(
                                        it.data?.createReservation?.redirectUrl ?: ""
                                    )
                                } else {
                                    navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                                }
                            } else {
                               /* navigator.moveToReservation(
                                    it!!.data!!.createReservation!!.results!!.id!!
                                )*/
                                postVerifyOrderApi(stripeToken,billingDetails.value?.razorPayOrderID!!,"", it!!.data!!.createReservation!!.results!!.id!!,billingDetails.value?.total!!)
                            }
                        } else if (it.data?.createReservation?.status == 500) {
                            navigator.openSessionExpire("PaymentVM")
                        } else {
                            if (it.data?.createReservation?.errorMessage == null) {
                                if (it.data?.createReservation
                                        ?.requireAdditionalAction == true
                                ) {
                                    paymentIntentSecret.value =
                                        it.data?.createReservation?.paymentIntentSecret!!
                                    reservationId.value =
                                        it.data?.createReservation?.reservationId!!

                                    stripeReqAdditionAction.value = "1"
                                } else {
                                    stripeReqAdditionAction.value = "0"
                                }
                            } else {
                                navigator.showSnackbar(
                                    "",
                                    it.data?.createReservation?.errorMessage!!
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, {
                    if (it is ApolloNetworkException) {
                        navigator.showOffline()
                    } else {
                        navigator.showError()
                        navigator.finishScreen()
                    }
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun confirmReservation(PaymentIntentId: String) {
        val query = ConfirmReservationMutation(
            reservationId = reservationId.value!!,
            paymentIntentId =PaymentIntentId
        )
        compositeDisposable.add(dataManager.confirmReservation(query)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({
                try {
                    print(it.data)
                    if (it.data?.confirmReservation?.status == 200) {
                        navigator.moveToReservation(
                            it!!.data!!.confirmReservation!!.results!!.id!!
                        )
                    } else if (it.data?.confirmReservation?.status == 500) {
                        navigator.openSessionExpire("PaymentVM")
                    } else {
                        navigator.showToast(it.data?.confirmReservation?.errorMessage.toString())
                        if (it.data?.confirmReservation?.requireAdditionalAction == true) {
                            paymentIntentSecret.value =
                                it.data?.confirmReservation?.paymentIntentSecret!!
                            reservationId.value = it.data?.confirmReservation?.reservationId!!
                            stripeReqAdditionAction.value = "1"
                        } else {
                            stripeReqAdditionAction.value = "0"
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, {
                if (it is ApolloNetworkException) {
                    navigator.showOffline()
                } else {
                    navigator.showError()
                    navigator.finishScreen()
                }
            })
        )
    }

    fun getCurrency() {
        val query = GetCurrenciesListQuery()
        compositeDisposable.add(dataManager.getCurrencyList(query)
            .performOnBackOutOnMain(scheduler)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .subscribe({ response ->
                val data = response.data!!.getCurrencies
                if (data?.status == 200) {
                    currencies.value = data.results!!
                } else if (data?.status == 500) {
                    if (data.errorMessage == null)
                        navigator.showError()
                    else navigator.showToast(data.errorMessage.toString())
                }
            }, {
                handleException(it)
            })
        )
    }

    fun confirmPayPalPayment(paymentId: String, payerId: String) {
        if (paymentId != "" && payerId != "") {
            val query = ConfirmPayPalExecuteMutation(
                paymentId = paymentId,
                payerId = payerId
            )

            compositeDisposable.add(dataManager.confirmPayPalPayment(query)
                .performOnBackOutOnMain(scheduler)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .subscribe({ response ->
                    val result = response.data?.confirmPayPalExecute
                    when {
                        result?.status == 200 -> {
                            if (result.reservationId != null) {
                                navigator.moveToReservation(result.reservationId!!)
                            } else {
                                navigator.showToast(resourceProvider.getString(R.string.reservation_id_not_found))
                            }
                        }

                        result?.status == 400 -> {
                            result.errorMessage.let {
                                if (it != null) {
                                    navigator.showToast(it)
                                } else {
                                    navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                                }
                            }
                        }

                        else -> {
                            navigator.showError()
                        }
                    }
                }, {
                    handleException(it)
                })
            )

        } else {
            navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
        }
    }

    fun getPayoutMethods() {
        val query = GetPaymentMethodsQuery()
        compositeDisposable.add(dataManager.getPayoutsMethod(query)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.getPaymentMethods
                    if (data!!.status == 200) {
                        paymentMethods.value = data.results

                    } else {
                        if (data.errorMessage == null) {
                            navigator.showError()
                        } else {
                            navigator.showToast(data.errorMessage.toString())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }

    fun postRazorPayAPI(amount: Double, currency: String) {
        val request = RazorPayRequest(amount, currency)

        // Use a coroutine to make the API call
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.createOrder(request)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                  razorPayOrderId.value = responseBody?.data?.id ?: ""
                    Log.d("API Response", "Order ID: ${razorPayOrderId.value}")

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                    navigator.moveToRazorPay((amount).toString(), razorPayOrderId.value!!)
                        billingDetails.value?.razorPayOrderID = razorPayOrderId.value!!
                    }
                } else {
                    Log.e("API Error", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API Exception", "Error: ${e.localizedMessage}")
            }
        }
    }

    fun postVerifyOrderApi(razorPayPaymentId: String, razorPayOrderId: String, razorPaySignature: String, reservationId: Int, totalAmount: Double) {
        val request = VerifyOrderRequest(razorPayPaymentId, razorPayOrderId, razorPaySignature, reservationId, totalAmount)

        // Use a coroutine to make the API call
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.verifyOrder(request)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    var  orderId = responseBody?.status?: ""
                    Log.d("API Response", "verifyOrder: $orderId")
                    navigator.moveToReservation(
                        reservationId
                    )

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                      //  navigator.moveToRazorPay((amount*100).toString(),orderId)
                    }
                } else {
                    Log.e("API Error", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API Exception", "Error: ${e.localizedMessage}")
            }
        }
    }



}
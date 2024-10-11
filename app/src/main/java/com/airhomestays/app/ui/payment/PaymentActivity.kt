package com.airhomestays.app.ui.payment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityPaymentBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.reservation.ReservationActivity
import com.airhomestays.app.util.onClick
import com.airhomestays.app.vo.Outcome
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.core.exception.APIConnectionException
import com.stripe.android.model.StripeIntent
import java.lang.ref.WeakReference
import javax.inject.Inject

class PaymentActivity : BaseActivity<ActivityPaymentBinding, PaymentViewModel>(), PaymentNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityPaymentBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_payment
    override val viewModel: PaymentViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(PaymentViewModel::class.java)
    private lateinit var stripe: Stripe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        viewModel.initData(intent)
        mBinding.btnPay.onClick {
            creatingToken()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.stripeResponse?.observe(this, Observer {
            it?.let { outCome ->
                when (outCome) {
                    is Outcome.Error -> {
                        if (outCome.e is APIConnectionException) {
                            showOffline()
                        } else {
                            showToast(resources.getString(R.string.something_went_wrong))
                        }
                    }

                    is Outcome.Success -> {
                        viewModel.token.value = outCome.data.id
                        viewModel.validateToken()
                    }

                    is Outcome.Progress -> {
                        viewModel.isLoading.set(outCome.loading)
                    }

                    is Outcome.Failure -> {}
                }
            }
        })

        viewModel.stripeReqAdditionAction?.observe(this, Observer {
            it?.let { outCome ->
                if (outCome == "1") {
                    stripe.handleNextActionForPayment(this, viewModel.paymentIntentSecret.value!!)
                }
            }
        })
    }

    private fun creatingToken() {
        val weakActivity = WeakReference<Activity>(this)
        val randomNumber = Math.random()
        /*        val card = mBinding.cardInputWidget.card
                if(card == null ){
                    Toast.makeText(this, resources.getString(R.string.enter_correct_card_details), Toast.LENGTH_LONG).show()
                }else{
                    val params = card.toPaymentMethodsParams()
                    viewModel.setIsLoading(true)

                    stripe = Stripe(applicationContext, Constants.stripePublishableKey)
                    stripe.createPaymentMethod(params, "Idempotency-Key: $randomNumber",null,object : ApiResultCallback<PaymentMethod> {
                        // Create PaymentMethod failed
                        override fun onError(e: Exception) {
                            viewModel.setIsLoading(false)
                            print("Payment failed")
                            viewModel.navigator.showToast("Payment failed")
                        }
                        override fun onSuccess(result: PaymentMethod) {
                            // Create a PaymentIntent on the server with a PaymentMethod
                            viewModel.setIsLoading(false)
                            print("Created PaymentMethod")
                            creatingToken(result.id, null)
                        }
                    })
                }*/
    }

    private fun creatingToken(paymentMethod: String?, paymentIntent: String?) {

        if (paymentMethod != null) {
            viewModel.createReservation(paymentMethod.toString())
        } else {
            viewModel.confirmReservation(paymentIntent.toString())
        }
    }

    override fun onRetry() {
        creatingToken()
    }

    override fun moveToReservation(id: Int) {
        val intent = Intent(this, ReservationActivity::class.java)
        intent.putExtra("type", 3)
        intent.putExtra("reservationId", id)
        intent.putExtra("userType", "Guest")
        setResult(32, intent)
        startActivity(intent)
        finish()
    }

    override fun finishScreen() {
        val intent = Intent()
        setResult(32, intent)
        //startActivity(intent)
        finish()
    }

    override fun moveToPayPalWebView(redirectUrl: String) {

    }

    override fun moveToRazorPay(amount: String, orderId: String) {
      Log.e("razorpay",amount.toString())
    }

    override fun onBackPressed() {
        if (viewModel.isLoading.get().not()) {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                val paymentIntent = result.intent
                when (paymentIntent.status) {
                    StripeIntent.Status.Succeeded -> viewModel.confirmReservation(paymentIntent.toString())
                    StripeIntent.Status.RequiresPaymentMethod ->
                        viewModel.navigator.showToast("Payment failed")

                    StripeIntent.Status.RequiresConfirmation -> {
                        creatingToken(null, paymentIntent.id)
                    }

                    else -> viewModel.navigator.showToast("Payment failed")
                }
            }

            override fun onError(e: Exception) {
                viewModel.navigator.showToast(e.message!!)
            }
        })
    }
}
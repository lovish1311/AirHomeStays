package com.airhomestays.app.ui.payment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.*
import com.airhomestays.app.databinding.ActivityPaymentBinding
import com.airhomestays.app.ui.WebViewActivity
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.reservation.ReservationActivity
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.onClick
import com.airhomestays.app.vo.PaymentType
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.StripeIntent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import timber.log.Timber
import com.airhomestays.app.util.*
import com.airhomestays.app.util.Utils.Companion.getQueryMap
import com.airhomestays.app.util.binding.BindingAdapters
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.temporal.TemporalAmount
import javax.inject.Inject


class PaymentTypeActivity : BaseActivity<ActivityPaymentBinding, PaymentViewModel>(), PaymentNavigator,
    PaymentResultListener {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

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
    lateinit var amount: String

    var paymentTypeArray = ArrayList<PaymentType>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        paymentTypeArray.add(PaymentType("Paypal", 1))
        paymentTypeArray.add(PaymentType("Stripe", 2))
        viewModel.initData(intent)
        viewModel.getCurrency()
        initView()
    }

    private fun initView() {
        mBinding.ivNavigateup.onClick {
            if (viewModel.isLoading.get().not()) {
                this.onBackPressed()
            }
        }

        mBinding.btnPay.onClick {
            if (viewModel.selectedPaymentType == 0) {
                viewModel.navigator.showSnackbar(getString(R.string.select_payment_type), getString(R.string.please_select_payment_type_to_continue))
            } else {
                if (viewModel.selectedPaymentType == 1) {
                    if (viewModel.selectedCurrency.get() != getString(R.string.currency)) {
                        viewModel.createReservation("")
                    } else {
                        viewModel.navigator.showSnackbar(getString(R.string.currency), getString(R.string.please_select_currency))
                    }
                }else if (viewModel.selectedPaymentType == 2) {
                    val total = viewModel.billingDetails.value?.total ?: 0.0
                    val multipliedTotal = BigDecimal(total).multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP)
                    viewModel.postRazorPayAPI(multipliedTotal.toDouble(),"INR")
                    /* stripe = Stripe(applicationContext, Constants.stripePublishableKey)
                     viewModel.stripe=stripe
                     addFragment(StripePaymentFragment(), "STRIPE_FRAGMENT")*/
                }
            }
        }

        subscribeToLiveData()
    }

    private fun setRazorPay(amount: String){
        Checkout.preload(applicationContext)
        // on below line we are
        // initializing razorpay account
        val checkout = Checkout()

        // on the below line we have to see our id.
        // checkout.setKeyID("rzp_test_ySqOhBgupFu1xq")
        checkout.setKeyID("rzp_live_rSubgkgENfYNid")

        // set image
        checkout.setImage(R.drawable.razor_pay)

        // initialize json object
        val obj = JSONObject()
        try {
            // to put name
            //   obj.put("name", "Home")

            // put description
            //   obj.put("description", "Test payment")

            // to set theme color
            obj.put("theme.color", "")

            // put the currency
            obj.put("currency", "INR")

            // put amount
            obj.put("amount", amount)

            // put mobile number
            //   obj.put("prefill.contact", "9284064503")

            // put email
//            obj.put("prefill.email", "")

            // open razorpay to checkout activity
            checkout.open(this@PaymentTypeActivity, obj)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadPayoutMethods().observe(this, Observer {
            it?.let { result ->
                setUp(result)
            }
        })
    }
    private fun setUp(result: List<GetPaymentMethodsQuery.Result?>) {
        if(result.size<2){
            viewModel.selectedPaymentType=result[0]?.paymentType!!
        }else{
            viewModel.selectedPaymentType=2
        }
        mBinding.rvPaymentType.withModels {
            result.forEachIndexed { index, paymentType ->
                if(paymentType?.isEnable!!){
                    if(paymentType?.paymentType==2) {
                        viewholderSelectPaymentType {

                            id("paymentType--$index")
//                            if (paymentType.paymentType == 1) {
//                                text("Paypal")
//                            } else {
                                text("Pay Now")
//                            }
                            visible(true)
                            viewModel(viewModel)
                            drawable(R.drawable.razor_pay)
                            onClick(View.OnClickListener {
                                val total = viewModel.billingDetails.value?.total ?: 0.0
                                val multipliedTotal = BigDecimal(total).multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP)
                                viewModel.postRazorPayAPI(multipliedTotal.toDouble(),"INR")
                            })

//                            if (viewModel.selectedPaymentType == 1) {
//                                if (paymentType.paymentType == 1)
//                                    visible(true)
//                                viewModel(viewModel)
//                                selectedCurrency(viewModel.selectedCurrency)
//                                onCurrencyClick(View.OnClickListener {
//                                    PaymentDialogOptionsFragment.newInstance("paymentCurrency")
//                                        .show(supportFragmentManager, "paymentCurrency")
//                                })
//                            }

//                            if (paymentType.paymentType == 1) {
//                                drawable(R.drawable.ic_paypal)
//                            } else {
//                                drawable(R.drawable.razor_pay)
//                            }
//                            onClick(View.OnClickListener {
//                                viewModel.selectedPaymentType = paymentType.paymentType!!
//                                this@withModels.requestModelBuild()
//                            })
//                            isChecked(viewModel.selectedPaymentType == paymentType.paymentType!!)

                        }
                        if (index == 0 && result.size > 1) {
                            viewholderDivider {
                                id("idDivider")
                            }
                        }
                    }
                }
            }
        }
    }


    private fun addFragment(fragment: Fragment, tag: String?) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
            .add(mBinding.fragFrame.id, fragment, tag)
            .addToBackStack(null)
            .commit()
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
        finish()
    }

    override fun moveToPayPalWebView(redirectUrl: String) {
        if(redirectUrl.isNotEmpty()){
            WebViewActivity.openWebViewActivityForResult(104,this, redirectUrl, "PayPalPayment-104")
        }else{
            viewModel.navigator.showToast(getString(R.string.return_url_not_fount))
        }
    }

    override fun moveToRazorPay(amount: String,orderId: String) {
        setRazorPay(amount)
    }

    override fun onBackPressed() {
        if (viewModel.isLoading.get().not()) {
            super.onBackPressed()
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.setIsLoading(false)
        if(resultCode!=0){
            if(resultCode==104){
                if(data?.getStringArrayExtra("url").toString().contains("/cancel".toRegex())){
                    viewModel.navigator.showToast(getString(R.string.payment_failed))
                }else{
                    data?.getStringExtra("url")?.let {
                        val map = getQueryMap(it)
                        val paymentId= map?.get("token")
                        val payerId = map?.get("PayerID")
                        viewModel.confirmPayPalPayment(paymentId?:"",payerId?:"")
                    }
                }
            }else if(resultCode==107){
                viewModel.navigator.showToast(getString(R.string.payment_cancelled))
            }else{
                stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
                    override fun onSuccess(result: PaymentIntentResult) {
                        val paymentIntent = result.intent
                        when (paymentIntent.status) {
                            StripeIntent.Status.Succeeded -> viewModel.confirmReservation(paymentIntent.toString())
                            StripeIntent.Status.RequiresPaymentMethod ->
                                viewModel.navigator.showToast("Payment failed")
                            StripeIntent.Status.RequiresConfirmation -> {
                                viewModel.paymentIntentLiveData.value = paymentIntent.id
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
    }


    override fun onRetry() {
    }

    override fun onPaymentSuccess(p0: String?) {
        Log.e("onPaymentSuccess",p0.toString())
        viewModel.billingDetails.value!!.razorPayPaymentID = p0.toString()
        viewModel.createReservation(p0.toString())
        //viewModel.postVerifyOrderApi(p0!!,viewModel.razorPayOrderId.value!!,"",viewModel.reservationId.value!!,viewModel.billingDetails.value?.total!!)
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Log.e("onPaymentError",p0.toString())
    }
}
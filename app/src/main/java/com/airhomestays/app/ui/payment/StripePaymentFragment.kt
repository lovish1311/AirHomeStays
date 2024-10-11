package com.airhomestays.app.ui.payment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentStripePaymentBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import com.airhomestays.app.vo.Outcome
import com.stripe.android.ApiResultCallback
import com.stripe.android.core.exception.APIConnectionException
import com.stripe.android.model.PaymentMethod
import timber.log.Timber
import javax.inject.Inject

class StripePaymentFragment : BaseFragment<FragmentStripePaymentBinding, PaymentViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: FragmentStripePaymentBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_stripe_payment
    override val viewModel: PaymentViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(PaymentViewModel::class.java)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        );
        mBinding = viewDataBinding!!
        if (isAdded) {
            subscribeToLiveData()
        }
        mBinding.cardInputWidget.setCardHint("4242 4242 4242 4242")
        mBinding.btnPay.onClick {
            creatingToken()
        }
        mBinding.ivNavigateup.onClick {
            baseActivity?.onBackPressedDispatcher?.onBackPressed()
        }
    }


    private fun subscribeToLiveData() {
        viewModel.selectedPaymentType = 2
        viewModel.stripeResponse?.observe(viewLifecycleOwner, Observer {
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

        viewModel.stripeReqAdditionAction.observe(viewLifecycleOwner, Observer {
            it?.let { outCome ->
                if (outCome == "1") {
                    viewModel.stripe.handleNextActionForPayment(
                        requireActivity(),
                        viewModel.paymentIntentSecret.value!!
                    )
                }
            }
        })

        viewModel.paymentIntentLiveData.observe(viewLifecycleOwner, Observer {
            it?.let { outCome ->
                creatingToken(null, outCome)
            }
        })

    }


    private fun creatingToken() {
        val randomNumber = Math.random()
        val card = mBinding.cardInputWidget.paymentMethodCreateParams
        if (card == null) {
            Toast.makeText(
                activity,
                resources.getString(R.string.enter_correct_card_details),
                Toast.LENGTH_LONG
            ).show()
        } else {
            viewModel.setIsLoading(true)
            viewModel.stripe.createPaymentMethod(
                card,
                "Idempotency-Key: $randomNumber",
                null,
                object : ApiResultCallback<PaymentMethod> {
                    override fun onError(e: Exception) {
                        viewModel.setIsLoading(false)
                        print("Payment failed")
                        viewModel.navigator.showToast("Payment failed ${e.message}")
                        Timber.e(e, "payment ")
                    }

                    override fun onSuccess(result: PaymentMethod) {
                        viewModel.setIsLoading(false)
                        creatingToken(result.id, null)
                    }
                })
        }
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
}
package com.airhomestays.app.host.payout.addPayout

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentAddPayoutAccountDetailsBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderPayoutAccountInfo
import com.stripe.android.Stripe
import javax.inject.Inject

class PayoutAccountInfoFragment :
    BaseFragment<FragmentAddPayoutAccountDetailsBinding, AddPayoutViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_add_payout_account_details
    override val viewModel: AddPayoutViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(AddPayoutViewModel::class.java)
    lateinit var mBinding: FragmentAddPayoutAccountDetailsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.stripe = Stripe(requireContext(), Constants.stripePublishableKey)
        initView()
    }

    private fun initView() {
        mBinding.btnNext.text = baseActivity!!.resources.getString(R.string.next)
        mBinding.btnNext.onClick {
            if (viewModel.checkAccountInfo()) {
              //  viewModel.navigator.moveToScreen(AddPayoutActivity.Screen.PAYOUTTYPE)
                viewModel.navigator.moveToScreen(AddPayoutActivity.Screen.BANKDETAILS)
            }
        }
        viewModel.loadPayment().observe(requireActivity(), Observer{
            it?.let { result ->

            }
        })

        mBinding.rlAddPayout.withModels {
            viewholderPayoutAccountInfo {
                id(14)
                country(viewModel.country)
                city(viewModel.city)
                address1(viewModel.address1)
                address2(viewModel.address2)
                state(viewModel.state)
                zip(viewModel.zip)
            }
        }
    }


    override fun onRetry() {

    }
}
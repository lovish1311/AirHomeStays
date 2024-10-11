package com.airhomestays.app.host.payout.addPayout

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentAddBankAccountBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.viewHolderAddBankDetail
import com.airhomestays.app.viewholderPayoutAccountInfo
import javax.inject.Inject

class AddBankAccountDetailFragment :
    BaseFragment<FragmentAddBankAccountBinding, AddPayoutViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_add_bank_account
    override val viewModel: AddPayoutViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(AddPayoutViewModel::class.java)
    lateinit var mBinding: FragmentAddBankAccountBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
    }
    private fun initView() {
        if (viewModel.checkAccountInfo()) {

        }
     //   viewModel.getSecPayment()
        mBinding.btnNext.text = baseActivity!!.resources.getString(R.string.next)
        viewModel.loadPayment().observe(requireActivity(), Observer{
            it?.let { result ->
                if (result.getSecPayment?.result?.accountNumber!=null||result.getSecPayment?.result?.accountNumber!=""){
                    viewModel.isUpdateSec = true
                    mBinding.btnNext.text = baseActivity!!.resources.getString(R.string.update)
                }else{
                    viewModel.isUpdateSec = false
                }
            }
        })
        viewModel.updateSecPayment.observe(viewLifecycleOwner, Observer {
            it.let {

                if (it){
                 baseActivity?.finish()
                }
            }
        })
        mBinding.btnNext.onClick {
            if (viewModel.isUpdateSec) {
                viewModel.updateSecPayment()
            }else{
                viewModel.createSecPayment()
            }
           // viewModel.getSecPayment()
          //

        }

        mBinding.rlAddBankAccount.withModels {
            viewHolderAddBankDetail {
                id(14)
                accountHolder(viewModel.accountHolder)
                accountNumber(viewModel.accountBank)
                confirmAccountNumber(viewModel.cnfAccountBank)
                mobileNumber(viewModel.mobileNumber)
                accountType(viewModel.accountType)
                ifscCode(viewModel.ifscCode)
                gstNumber(viewModel.gstNumber)
                panNumber(viewModel.panNumber)

            }
        }
    }
    override fun onRetry() {

    }
}
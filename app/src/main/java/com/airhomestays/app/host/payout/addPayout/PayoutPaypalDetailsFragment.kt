package com.airhomestays.app.host.payout.addPayout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.*
import com.airhomestays.app.databinding.FragmentAddPayoutAccountDetailsBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.profile.currency.CurrencyDialog
import com.airhomestays.app.util.RxBus
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.onClick
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

class PayoutPaypalDetailsFragment :
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
    private var eventCompositeDisposal = CompositeDisposable()
    lateinit var fragment: DialogFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        initRxBus()
    }

    private fun initRxBus() {
        eventCompositeDisposal.add(RxBus.listen(Array<String>::class.java)
            .subscribe { event ->
                event?.let {
                    viewModel.currency.set(it[0]) // paypal currency dialog's selection
                    fragment.dismiss()
                    mBinding.rlAddPayout.requestModelBuild()
                }
            })
    }

    private fun initView() {
        mBinding.btnNext.text = baseActivity!!.resources.getString(R.string.finish)
        mBinding.btnNext.onClick { checkDetails() }
        mBinding.rlAddPayout.withModels {
            viewholderListingDetailsDesc {
                id("df")
                desc(baseActivity!!.resources.getString(R.string.paypal_desc))
            }
            viewholderPayoutPaypalDetails {
                id(14)
                email(viewModel.email)
                currency(viewModel.currency)
                payoutCurrency(viewModel.currency.get())
                if (BindingAdapters.getCurrencySymbol(viewModel.currency.get()) == viewModel.currency.get()) {
                    payoutCurrency(viewModel.currency.get())
                } else {
                    payoutCurrency(BindingAdapters.getCurrencySymbol(viewModel.currency.get()) + " " + viewModel.currency.get())
                }
                currencyClick { _ ->
                    viewModel.currency.get()?.let {
                        fragment = CurrencyDialog.newInstance(it)
                        (fragment as CurrencyDialog).show(childFragmentManager)
                    }
                }
            }
        }
    }

    fun checkDetails() {
        if (viewModel.checkPaypalInfo() && viewModel.checkAccountInfo()) {
            viewModel.addPayout(1)
        }
    }

    override fun onDestroy() {
        viewModel.email.set("")
        viewModel.currency.set("")
        if (!eventCompositeDisposal.isDisposed) eventCompositeDisposal.dispose()
        super.onDestroy()
    }

    override fun onRetry() {
        checkDetails()
    }
}
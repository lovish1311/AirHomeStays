package com.airhomestays.app.ui.host.step_three.localLaws

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentLocalLawsBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.step_three.StepThreeViewModel
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderItineraryTextBold
import com.airhomestays.app.viewholderItineraryTextNormal
import com.airhomestays.app.viewholderUserName2
import javax.inject.Inject

class LocalLawsFragment : BaseFragment<HostFragmentLocalLawsBinding, StepThreeViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentLocalLawsBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_local_laws
    override val viewModel: StepThreeViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.pgBar.progress = 100
        mBinding.tvNext.text = getString(R.string.finish)
        mBinding.localLawsToolbar.rlToolbarRightside.gone()
        if (viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            mBinding.tvRightsideText.setOnClickListener {
                if (viewModel.checkPrice() && viewModel.checkDiscount() && viewModel.checkTripLength()&&viewModel.checkTax()) {
                    it.disable()
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        } else {
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.chip3.gone()
        }
        mBinding.localLawsToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        subscribeToLiveData()

        mBinding.tvNext.setOnClickListener {
            if(viewModel.checkTax()){
                viewModel.retryCalled = "update"
                viewModel.updateListStep3("add")
            }

        }
    }

    fun subscribeToLiveData() {
        setChips()
        mBinding.rvLocalLaws.withModels {
            viewholderUserName2 {
                id("header")
                name(getString(R.string.local_laws))
                isBgNeeded(true)
                paddingBottom(true)
            }
            viewholderItineraryTextBold {
                id("subHeader")
                isBgNeeded(true)
                text(getString(R.string.local_laws_text))
                paddingTop(true)
            }

            viewholderItineraryTextNormal {
                id("content")
                text(getString(R.string.local_laws_content))
                paddingTop(true)
                paddingBottom(false)
            }

        }
    }

    private fun setChips() {
        mBinding.chips.apply {
            paddingTop = true
            guestReq = false
            houseRules = false
            reviewGuestBook = false
            advanceNotice = false
            bookingWindow = false
            minMaxNights = false
            pricing = false
            discount = false
            booking = false
            localLaws = true
            guestReqClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.GUESTREQUEST)
            })
            houseRulesClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.HOUSERULE)
            })
            reviewGuestBookClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.GUESTBOOK)
            })
            advanceNoticeClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.GUESTNOTICE)
            })
            bookingWindowClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.BOOKWINDOW)
            })
            minMaxNightsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.TRIPLENGTH)
            })
            pricingClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.LISTPRICE)
            })
            discountClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.DISCOUNTPRICE)
            })
            bookingClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.INSTANTBOOK)
            })
            localLawsClick = (View.OnClickListener {

            })
        }
    }

    override fun onRetry() {
    }
}
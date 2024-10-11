package com.airhomestays.app.ui.host.step_three.instantBook

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentInstantBookBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.step_three.StepThreeViewModel
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderDividerPadding
import com.airhomestays.app.viewholderItineraryTextNormal
import com.airhomestays.app.viewholderRadioTextSub
import com.airhomestays.app.viewholderTips
import com.airhomestays.app.viewholderUserName2
import com.airhomestays.app.viewholderUserNormalText
import javax.inject.Inject

class InstantBookFragment : BaseFragment<HostFragmentInstantBookBinding,StepThreeViewModel>(){

    @Inject lateinit var mViewModelFactory : ViewModelProvider.Factory
    lateinit var mBinding : HostFragmentInstantBookBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_instant_book
    override val viewModel: StepThreeViewModel
        get() = ViewModelProvider(baseActivity!!,mViewModelFactory).get(StepThreeViewModel::class.java)


    private var isSelected = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.pgBar.progress = 60
        if(viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            mBinding.tvRightsideText.setOnClickListener {
                if(viewModel.checkPrice() && viewModel.checkDiscount()  && viewModel.checkTripLength()&&viewModel.checkTax()) {
                    it.disable()
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        }else{
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.chip3.gone()
        }
        mBinding.instantBookToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        subscribeToLiveData()
    }

    fun subscribeToLiveData(){
        setChips()
        mBinding.rvInstantBook.withModels {
            viewholderUserName2 {
                id("header")
                name(getString(R.string.instant_booking))
                isBgNeeded(true)
                paddingTop(true)
                paddingBottom(true)
            }
            viewholderUserNormalText {
                id("subText")
                text(getString(R.string.instant_book_title))
                isBgNeeded(true)
                paddingTop(false)
                paddingBottom(true)
            }
            viewholderItineraryTextNormal {
                id(477)
                paddingTop(true)
                text(getString(R.string.instant_book_text))
            }
            viewholderUserNormalText {
                id("whoBooked")
                text(getString(R.string.who_booked))
                paddingTop(true)
                paddingBottom(false)
            }

            viewholderRadioTextSub {
                id("1")
                text(getString(R.string.auto_approval_text))
                subText(getString(R.string.auto_sub))
                radioVisibility(viewModel.selectArray[0])
                onClick(View.OnClickListener {
                    viewModel.bookingType = "instant"
                    selector(0)  })
                direction(false)
            }

            viewholderDividerPadding {
                id("div3")
            }

            viewholderRadioTextSub {
                id("2")
                text(getString(R.string.not_auto_approve))
                radioVisibility(viewModel.selectArray[1])
                onClick(View.OnClickListener {
                    viewModel.bookingType = "request"
                    selector(1)  })
                paddingBottom(true)
                direction(false)
            }
           viewholderTips {
               id(88)
               tips(getString(R.string.tips_eight))
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
            booking = true
            localLaws = false



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

            })
            localLawsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.LAWS)
            })
            guestReqClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.GUESTREQUEST)
            })
        }
    }

    private fun selector(index: Int) {
        viewModel.selectArray.forEachIndexed { i: Int, _: Boolean ->
            viewModel.selectArray[i] = index == i
            isSelected = true
        }
        mBinding.rvInstantBook.requestModelBuild()
    }

    override fun onRetry() {

    }
}
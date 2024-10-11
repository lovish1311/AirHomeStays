package com.airhomestays.app.ui.host.step_three.guestBooking

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.GetListingSettingQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentGuestBookingBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.step_three.StepThreeViewModel
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderDivider
import com.airhomestays.app.viewholderGuestReq
import com.airhomestays.app.viewholderUserName2
import com.airhomestays.app.viewholderUserNormalText
import com.airhomestays.app.viewholderViewPadding
import javax.inject.Inject

class GuestBookingFragment : BaseFragment<HostFragmentGuestBookingBinding, StepThreeViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentGuestBookingBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_guest_booking
    override val viewModel: StepThreeViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.guestBookToolbar.rlToolbarRightside.gone()
        mBinding.pgBar.progress = 30
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
        mBinding.guestBookToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }


        subscribeToLiveData()
    }

    fun subscribeToLiveData() {
        viewModel.listSettingArray.observe(viewLifecycleOwner, Observer {
            it?.let { bookingList ->
                setUI(bookingList.reviewGuestBook!!)
            }

        })
    }

    fun setUI(it: GetListingSettingQuery.ReviewGuestBook) {
        val reviewArray = it.listSettings
        setChips()
        mBinding.rvGuestBook.withModels {

            viewholderUserName2 {
                id("header")
                name(getString(R.string.how_book))
                isBgNeeded(true)
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderUserNormalText {
                id("subText")
                text(getString(R.string.how_book_sub))
                paddingTop(false)
                paddingBottom(true)
                isBgNeeded(true)
            }

            reviewArray?.forEachIndexed { index, s ->
                viewholderGuestReq {
                    id("req" + index)
                    text(s?.itemName)
                    isVerified(true)
                    direction(true)
                }
                viewholderDivider {
                    id("divider $index")
                }
            }

            viewholderUserNormalText {
                id("subText")
                text(getString(R.string.meet_req))
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderViewPadding {
                id("padding")
            }
        }
    }

    private fun setChips() {
        mBinding.chips.apply {
            paddingTop = false
            guestReq = false
            houseRules = false
            reviewGuestBook = true
            advanceNotice = false
            bookingWindow = false
            minMaxNights = false
            pricing = false
            discount = false
            booking = false
            localLaws = false
            guestReqClick = (View.OnClickListener {
                viewModel.navigator.navigateToScreen(StepThreeViewModel.NextStep.GUESTREQUEST)
            })

            localLawsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.LAWS)
            })

            reviewGuestBookClick = (View.OnClickListener {

            })
            houseRulesClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.HOUSERULE)
            })
            advanceNoticeClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.GUESTNOTICE)
            })
            bookingWindowClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.BOOKWINDOW)
            })
            minMaxNightsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.TRIPLENGTH)
            })
            pricingClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.LISTPRICE)
            })
            discountClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.DISCOUNTPRICE)
            })
            bookingClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.INSTANTBOOK)
            })
        }
    }

    override fun onRetry() {

    }
}
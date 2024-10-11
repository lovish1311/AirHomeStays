package com.airhomestays.app.ui.host.step_three.discount

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentDiscountPriceBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.step_three.StepThreeViewModel
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderBgBottomsheet
import com.airhomestays.app.viewholderDiscount
import com.airhomestays.app.viewholderTips
import com.airhomestays.app.viewholderUserName2
import java.util.Locale
import javax.inject.Inject

class DiscountPriceFragment : BaseFragment<HostFragmentDiscountPriceBinding, StepThreeViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentDiscountPriceBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_discount_price
    override val viewModel: StepThreeViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepThreeViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.pgBar.progress = 40
        mBinding.discountPriceToolbar.rlToolbarRightside.gone()
        if (viewModel.isListAdded) {
            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            mBinding.tvRightsideText.setOnClickListener {
                if (viewModel.checkDiscount() && viewModel.checkTripLength() && viewModel.checkPrice()) {
                    it.disable()
                    viewModel.retryCalled = "update"
                    viewModel.updateListStep3("edit")
                }
            }
        } else {
            mBinding.tvRightsideText.visibility = View.GONE
            mBinding.chips.chip3.gone()
        }
        mBinding.discountPriceToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        subscribeToLiveData()
    }

    fun subscribeToLiveData() {
        setChips()
        mBinding.rvDiscountPrice.withModels {
            viewholderUserName2 {
                id("header")
                name(getString(R.string.discounts))
                isBgNeeded(true)
                paddingTop(true)
                paddingBottom(false)
            }
            viewholderBgBottomsheet { id(37) }


            viewholderDiscount {
                id("weeklyDis")
                text(viewModel.weekDiscount)
                title(getString(R.string.weekly_discount))
                inputType(false)
                paddingBottom(false)
                hint(getString(R.string.discount_hint))
                onBind { model, view, position ->
                    var textView=view.dataBinding.root.findViewById<TextView>(R.id.discount)
                    val isRTL = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL

                    if (isRTL) {
                        textView.scaleX = -1f
                        textView.textScaleX = -1f
                    } else {
                        textView.scaleX = 1f
                        textView.textScaleX = 1f
                    }

                }
            }

            viewholderDiscount {
                id("monthlyDis")
                hint(getString(R.string.discount_hint))
                title(getString(R.string.monthly_discount))
                inputType(false)
                paddingBottom(true)
                text(viewModel.monthDiscount)
                onBind { model, view, position ->
                    var textView=view.dataBinding.root.findViewById<TextView>(R.id.discount)
                    val isRTL = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL

                    if (isRTL) {
                        textView.scaleX = -1f
                        textView.textScaleX = -1f
                    } else {
                        textView.scaleX = 1f
                        textView.textScaleX = 1f
                    }

                }
            }
            viewholderTips {
                id(77)
                tips(getString(R.string.tips_seven))
                paddingStart(true)
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
            discount = true
            booking = false
            localLaws = false

            houseRulesClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.HOUSERULE)
            })
            advanceNoticeClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.GUESTNOTICE)
            })

            minMaxNightsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.TRIPLENGTH)
            })
            pricingClick = (View.OnClickListener {
                viewModel?.navigator?.navigateBack(StepThreeViewModel.BackScreen.LISTPRICE)
            })
            discountClick = (View.OnClickListener {

            })
            guestReqClick = (View.OnClickListener {
                viewModel.navigator.navigateToScreen(StepThreeViewModel.NextStep.GUESTREQUEST)
            })
            bookingWindowClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.BOOKWINDOW)
            })
            reviewGuestBookClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.GUESTBOOK)
            })
            bookingClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.INSTANTBOOK)
            })
            localLawsClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.LAWS)
            })
            guestReqClick = (View.OnClickListener {
                viewModel?.navigator?.navigateToScreen(StepThreeViewModel.NextStep.GUESTREQUEST)
            })
        }
    }

    override fun onRetry() {
    }
}
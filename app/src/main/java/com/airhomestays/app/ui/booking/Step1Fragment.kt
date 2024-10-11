package com.airhomestays.app.ui.booking

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airhomestays.app.*
import com.airhomestays.app.databinding.FragmentBookingStep1Binding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.vo.BillingDetails
import javax.inject.Inject

class Step1Fragment : BaseFragment<FragmentBookingStep1Binding, BookingViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_booking_step1
    override val viewModel: BookingViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(BookingViewModel::class.java)
    lateinit var mBinding: FragmentBookingStep1Binding
    private var mCurrentState = State.IDLE
    private lateinit var epoxyVisibilityTracker: EpoxyVisibilityTracker

    enum class State {
        EXPANDED,
        IDLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
        ViewCompat.setElevation(mBinding.toolbarListingDetails, 10F)
    }

    private fun initView() {
        mBinding.tvListingCheckAvailability.onClick {
            (baseActivity as BookingActivity).navigateToScreen(2)
        }
        mBinding.tvListingPriceBreakdown.gone()
        mBinding.ivNavigateup.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        mBinding.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(mBinding.rlBooking)
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeToLiveData() {
        viewModel.billingDetails.observe(viewLifecycleOwner, Observer {
            it?.let { billingDetails ->
                setUp(billingDetails)
                mBinding.tvListingPrice.text =
                    viewModel.getCurrencySymbol() + Utils.formatDecimal(billingDetails.total) +
                            " " + resources.getString(R.string.for_) + " " + billingDetails.nights + resources.getQuantityString(
                        R.plurals.night_count,
                        billingDetails.nights
                    )
            }
        })
    }

    private fun setUp(it: BillingDetails) {
        mBinding.rlBooking.withModels {
            viewholderBookingSteper {
                id("steper")
                if (it.isProfilePresent) {
                    step(resources.getString(R.string.step_1_of_3))
                } else {
                    step(resources.getString(R.string.step_1_of_4))
                }
                title(resources.getString(R.string.book_your_stay))
                infoVisibility(false)
                paddingTop(true)
                paddingBottom(true)
                onVisibilityChanged { _, _, percentVisibleHeight, _, _, _ ->
                    mCurrentState = if (percentVisibleHeight < 35) {

                        State.EXPANDED
                    } else {
                        State.IDLE
                    }
                }
            }

            viewholderBookingDateInfo {
                id("booking_date")
                if (resources.getBoolean(R.bool.is_left_to_right_layout).not()) {
                    ltrDirection(false)
                } else {
                    ltrDirection(true)
                }
                checkIn(it.checkIn)
                checkOut(it.checkOut)
            }
            viewholderDivider {
                id("divider1")
            }

            if (it.houseRule.isNotEmpty()) {
                viewholderListingDetailsSectionHeader {
                    id("sectionheader")
                    header(resources.getString(R.string.please_keep_in_mind))
                }

                it.houseRule.forEachIndexed { index, s ->
                    viewholderListingDetailsDesc {
                        id("desc $index")
                        desc(s)
                        size(16.toFloat())
                        paddingTop(true)
                        paddingBottom(true)
                    }
                    viewholderDivider {
                        id("divider2 $index")
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        mBinding.rlBooking.adapter = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        epoxyVisibilityTracker.detach(mBinding.rlBooking)
        super.onDestroy()
    }

    override fun onRetry() {

    }
}
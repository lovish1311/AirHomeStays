package com.airhomestays.app.ui.booking

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceManager
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentBookingStep1Binding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.ui.payment.PaymentTypeActivity
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderBookingSteper
import com.airhomestays.app.viewholderBookingSummaryListing
import com.airhomestays.app.viewholderDivider
import com.airhomestays.app.viewholderListingDetailsDesc
import com.airhomestays.app.viewholderPricebreakSummary
import com.airhomestays.app.vo.BillingDetails
import java.util.Locale
import javax.inject.Inject


class ReviewAndPayFragment : BaseFragment<FragmentBookingStep1Binding, BookingViewModel>() {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.rlListingPricedetails.gone()
        mBinding.tvListingCheckAvailability.gone()
        mBinding.btnBooking.visible()
        mBinding.btnBooking.text = getString(R.string.add_payment)
        mBinding.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.btnBooking.onClick {
            val intent = Intent(context, PaymentTypeActivity::class.java)
            intent.putExtra("billingDetails", viewModel.billingDetails.value)
            intent.putExtra("msg", viewModel.msg.get()!!.trim())
            intent.putExtra("listDetails", viewModel.listDetails)
            startActivityForResult(intent, 55)
        }
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.billingDetails.observe(viewLifecycleOwner, Observer {
            it?.let { billingDetails ->
                setUp(billingDetails)
            }
        })
    }

    private fun setUp(it: BillingDetails) {
        mBinding.rlBooking.withModels {
            viewholderBookingSteper {
                id("step 4")
                if (it.isProfilePresent) {
                    step(resources.getString(R.string.step_3_of_3))
                } else {
                    step(resources.getString(R.string.step_4_of_4))
                }
                title(resources.getString(R.string.review_and_pay))
                infoVisibility(false)
                paddingTop(true)
                paddingBottom(false)
            }

            viewholderBookingSummaryListing {
                val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val langType = preferences.getString("Locale.Helper.Selected.Language", "en")
                id("bookingSummary")
                date(
                    Utils.getMonth(
                        langType!!,
                        it.checkIn
                    ) + " " + it.checkIn.split("-")[2] + " - " + Utils.getMonth(
                        langType,
                        it.checkOut
                    ) + " " + it.checkOut.split("-")[2] + ", " + it.guest + resources.getQuantityString(
                        R.plurals.guest_count,
                        it.guest
                    )
                )
                imageClick(View.OnClickListener {
                    Utils.clickWithDebounce(it) {
                        ListingDetails.openListDetailsActivity(
                            requireContext(),
                            viewModel.listDetails
                        )
                    }
                })
                image(it.image)
                title(it.title.trim().replace("\\s+", " "))
            }

            viewholderDivider {
                id("viewholder_divider - 1")
            }

            viewholderPricebreakSummary {
                id("PriceBreakSummary")
                val isRTL =
                    TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL
                val baseLTR =
                    viewModel.getCurrencySymbol() + Utils.formatDecimal(it.averagePrice) + " x " + it.nights
                val baseRTL =
                    it.nights.toString() + " x " + viewModel.getCurrencySymbol() + Utils.formatDecimal(
                        it.averagePrice
                    )
                val ngt = resources.getQuantityString(R.plurals.night_count, it.nights ?: 0);

                basePrice(if (isRTL) baseRTL else baseLTR)
                basePriceNights(ngt)
                basePriceRight(viewModel.getCurrencySymbol() + Utils.formatDecimal(it.priceForDays))


                if (it.isSpecialPriceAssigned == null) {
                    spIconVisible(false)
                } else {
                    if (it.isSpecialPriceAssigned) {
                        spIconVisible(true)
                        onBind { _, view, _ ->
                            val imgView =
                                view.dataBinding.root.findViewById<ImageView>(R.id.specialPriceIcon)
                            val pricingLay =
                                view.dataBinding.root.findViewById<LinearLayout>(R.id.spl_pricing_layout)
                            imgView.setOnClickListener {
                                pricingLay.visible()
                                val handler = Handler(Looper.getMainLooper())
                                val r = Runnable { pricingLay.gone() }
                                handler.postDelayed(r, 3000)
                            }
                        }
                    } else {
                        spIconVisible(false)
                    }
                }

                if (it.cleaningPrice > 0) {
                    cleaningPrice(viewModel.getCurrencySymbol() + Utils.formatDecimal(it.cleaningPrice))
                    cleaningPriceVisibility(true)
                } else {
                    cleaningPriceVisibility(false)
                }

                if (it.guestServiceFee > 0) {
                    servicePrice(viewModel.getCurrencySymbol() + Utils.formatDecimal(it.guestServiceFee))
                    servicePriceVisibility(true)
                } else {
                    servicePriceVisibility(false)
                }
                if (it.discountLabel != null && it.discount!! > 0) {
                    discountVisibility(true)
                    val str = it.discountLabel!!
                    val strArray = str.split(" ")
                    val builder = StringBuilder()
                    for (s in strArray) {
                        val cap = s.substring(0, 1).toUpperCase() + s.substring(1)
                        builder.append(cap + " ")
                    }
                    discountText(builder.toString())
                    discountPrice("-" + viewModel.getCurrencySymbol() + Utils.formatDecimal(it.discount!!))
                    totalPrice(viewModel.getCurrencySymbol() + Utils.formatDecimal(it.total))
                } else {
                    discountVisibility(false)
                    totalPrice(viewModel.getCurrencySymbol() + Utils.formatDecimal(it.total))
                }
            }

            viewholderDivider {
                id("viewholder_divider - 2")
            }

            viewholderListingDetailsDesc {
                id("DetailsDesc - Cancellation")
                val text =
                    "<b>" + resources.getString(R.string.cancellation_policy_with_dot) + it.cancellation + "</b>"
                desc(Utils.fromHtml(text).toString())
                paddingTop(true)
            }

            viewholderListingDetailsDesc {
                id("DetailsDesc - CancellationContent")
                desc(it.cancellationContent)
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderDivider {
                id("viewholder_divider - 3")
            }

            viewholderListingDetailsDesc {
                id("DetailsDesc - TermsAndPolicy")
                desc(resources.getString(R.string.review_cancel_desc))
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderDivider {
                id("viewholder_divider - 4")
            }
        }
    }

    override fun onDestroyView() {
        mBinding.rlBooking.adapter = null
        super.onDestroyView()
    }

    override fun onRetry() {

    }
}

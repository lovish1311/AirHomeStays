package com.airhomestays.app.ui.reservation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.GetReservationQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentBookingStep1Binding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderBookingDateInfo
import com.airhomestays.app.viewholderCenterText
import com.airhomestays.app.viewholderDivider
import com.airhomestays.app.viewholderDividerPadding
import com.airhomestays.app.viewholderDividerPaddingTop
import com.airhomestays.app.viewholderItineraryText
import com.airhomestays.app.viewholderItineraryTextBold
import com.airhomestays.app.viewholderItineraryTextLeftRight
import com.airhomestays.app.viewholderItineraryTextLeftRightBlack
import com.airhomestays.app.viewholderItineraryTextNormal
import com.airhomestays.app.viewholderPricebreakSummary
import java.util.Locale
import javax.inject.Inject


class ReceiptFragment : BaseFragment<FragmentBookingStep1Binding, ReservationViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_booking_step1
    override val viewModel: ReservationViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(ReservationViewModel::class.java)
    lateinit var mBinding: FragmentBookingStep1Binding
    private var userType = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        baseActivity!!.intent?.extras?.let {
            userType = it.getString("userType")!!
        }
        mBinding.rlListingBottom.gone()
        mBinding.idHeaderTitle.visible()
        mBinding.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.rlBooking.gone()
        viewModel.reservation.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let { reservation ->
                setup(reservation)
                mBinding.rlBooking.visible()
            }
        })
    }

    private fun setup(reservation: GetReservationQuery.Results) {
        mBinding.rlBooking.withModels {
            viewholderItineraryText {
                id("createdAt")
                viewModel.reservation.value?.createdAt?.let {
                    text(Utils.epochToDate(it.toLong(), Utils.getCurrentLocale(requireContext())!!))
                }
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderCenterText {
                id("Receipt # " + reservation.id)
                text(resources.getString(R.string.receipt) + ": # " + reservation.id)
                large(true)
                paddingBottom(true)
            }
            viewholderDividerPaddingTop {
                id("divider1")
            }

            viewholderItineraryTextBold {
                id("Name")
                text(resources.getString(R.string.name))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(false)
            }

            viewholderItineraryTextNormal {
                id("User Name")
                text(reservation.guestData?.firstName)
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(false)
            }

            viewholderItineraryTextBold {
                id("Reservation")
                text(resources.getString(R.string.confirmation_code))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryTextNormal {
                id("Reservation code")
                text(reservation.confirmationCode.toString())
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(false)
            }

            viewholderItineraryTextBold {
                id("Travel Destination")
                text(resources.getString(R.string.travel_destination))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryTextNormal {
                id("Madurai")
                text(reservation.listData?.city)
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(false)
            }

            viewholderItineraryTextBold {
                id("Duration")
                text(resources.getString(R.string.duration))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryTextNormal {
                id("nights")
                text(
                    reservation.nights.toString() + " " + resources.getQuantityString(
                        R.plurals.night_count,
                        reservation.nights!!
                    )
                )
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(false)
            }

            viewholderItineraryTextBold {
                id("Accommodation Type")
                text(resources.getString(R.string.accommodation_type))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryTextNormal {
                id("Shared Room")
                text(reservation.listData?.roomType)
                isRed(false)
                large(false)
                paddingBottom(true)
                paddingTop(false)
            }

            viewholderDividerPadding {
                id("divider2")
            }

            viewholderItineraryTextBold {
                id("Accommodation Address")
                text(resources.getString(R.string.accommodation_address))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryTextNormal {
                id("address")
                text(
                    reservation.listData?.street + ", " +
                            reservation.listData?.city + ", " +
                            reservation.listData?.state + ", " +
                            reservation.listData?.country + ", " +
                            reservation.listData?.zipcode
                )
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(false)
            }

            viewholderItineraryTextBold {
                id("Accommodation Host")
                text(resources.getString(R.string.accommodation_host))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryTextNormal {
                id("Host Name")
                text(reservation.hostData?.firstName)
                isRed(false)
                large(false)
                paddingBottom(true)
                paddingTop(false)
            }

            viewholderDividerPadding {
                id("divider3")
            }

            viewholderBookingDateInfo {
                id("booking_date")
                paddingBottom(true)
                try {

                    if (resources.getBoolean(R.bool.is_left_to_right_layout).not()) {
                        ltrDirection(false)
                    } else {
                        ltrDirection(true)
                    }

                    checkIn(
                        Utils.epochToDate(
                            reservation.checkIn!!.toLong(),
                            Utils.getCurrentLocale(requireContext())!!
                        )
                    )
                    checkOut(
                        Utils.epochToDate(
                            reservation.checkOut!!.toLong(),
                            Utils.getCurrentLocale(requireContext())!!
                        )
                    )
                    if (reservation.checkInStart == "Flexible" && reservation.checkInEnd == "Flexible") {
                        startTime(getString(R.string.flexible_check_in_time))
                    } else if (reservation.checkInStart != "Flexible" && reservation.checkInEnd == "Flexible") {
                        val sTime = when {
                            BindingAdapters.timeConverter(reservation.checkInStart!!) == "0AM" -> {
                                "12AM"
                            }

                            BindingAdapters.timeConverter(reservation.checkInStart!!) == "0PM" -> {
                                "12PM"
                            }

                            else -> {
                                BindingAdapters.timeConverter(reservation.checkInStart!!)
                            }
                        }
                        startTime("${getString(R.string.from)} $sTime")
                    } else if (reservation.checkInStart == "Flexible" && reservation.checkInEnd != "Flexible") {
                        val eTime = when {
                            BindingAdapters.timeConverter(reservation.checkInEnd!!) == "0AM" -> {
                                "12AM"
                            }

                            BindingAdapters.timeConverter(reservation.checkInEnd!!) == "0PM" -> {
                                "12PM"
                            }

                            else -> {
                                BindingAdapters.timeConverter(reservation.checkInEnd!!)
                            }
                        }
                        startTime("${getString(R.string.upto)} $eTime")
                    } else if (reservation.checkInStart != "Flexible" && reservation.checkInEnd != "Flexible") {
                        val sTime = when {
                            BindingAdapters.timeConverter(reservation.checkInStart!!) == "0AM" -> {
                                "12AM"
                            }

                            BindingAdapters.timeConverter(reservation.checkInStart!!) == "0PM" -> {
                                "12PM"
                            }

                            else -> {
                                BindingAdapters.timeConverter(reservation.checkInStart!!)
                            }
                        }

                        val eTime = when {
                            BindingAdapters.timeConverter(reservation.checkInEnd!!) == "0AM" -> {
                                "12AM"
                            }

                            BindingAdapters.timeConverter(reservation.checkInEnd!!) == "0PM" -> {
                                "12PM"
                            }

                            else -> {
                                BindingAdapters.timeConverter(reservation.checkInEnd!!)
                            }
                        }
                        startTime("$sTime - $eTime")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                timeVisibility(true)
            }

            viewholderDivider {
                id("divider5")
            }

            viewholderPricebreakSummary {
                id("summary")
                try {

                    val isRTL =
                        TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL
                    if (viewModel.reservationComplete.value!!.convertedIsSpecialAverage != null) {
                        val baseLTR =
                            viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertedIsSpecialAverage!!)
                                .toString() + " x " + reservation.nights
                        val baseRTL =
                            viewModel.getCurrencySymbol() + reservation.nights + " x " + Utils.formatDecimal(
                                viewModel.reservationComplete.value!!.convertedIsSpecialAverage!!
                            ).toString()
                        val ngt = resources.getQuantityString(
                            R.plurals.night_count,
                            viewModel.reservation.value!!.nights!! ?: 0
                        );
                        basePrice(if (isRTL) baseRTL else baseLTR)
                        basePriceNights(ngt)
                        basePriceRight(
                            viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertedTotalNightsAmount!!)
                                .toString()
                        )
                    } else {
                        val baseLTR =
                            viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertedBasePrice!!)
                                .toString() + " x " + reservation.nights + resources.getQuantityString(
                                R.plurals.night_count,
                                viewModel.reservation.value!!.nights!!
                            )
                        val baseRTL = resources.getQuantityString(
                            R.plurals.night_count,
                            viewModel.reservation.value!!.nights ?: 0
                        ) + " " + viewModel.getCurrencySymbol() + reservation.nights + " x " + Utils.formatDecimal(
                            viewModel.reservationComplete.value!!.convertedBasePrice!!
                        ).toString()

                        val basePr = if (isRTL) baseRTL else baseLTR
                        basePrice(
                            basePr
                        )
                        basePriceRight(
                            viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertedTotalNightsAmount!!)
                                .toString()
                        )
                    }

                    if (viewModel.reservationComplete.value!!.convertedIsSpecialAverage == null) {
                        spIconVisible(false)
                    } else {
                        if (viewModel.reservationComplete.value!!.convertedIsSpecialAverage != viewModel.reservationComplete.value!!.convertedBasePrice) {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (viewModel.reservationComplete.value!!.convertedCleaningPrice != null && viewModel.reservationComplete.value!!.convertedCleaningPrice!! > 0.0) {
                    cleaningPrice(
                        viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertedCleaningPrice!!)
                            .toString()
                    )
                    cleaningPriceVisibility(true)
                } else {
                    cleaningPriceVisibility(false)
                }

                    if (userType == "Host") {

                        if (viewModel.gst.value != null && viewModel.panNo.value != null) {
                            discountGstText("GST TCS – U/S 52 of GST for GST Number")
                            val subtotal =
                                viewModel.reservationComplete.value!!.convertTotalWithGuestServiceFee!! - viewModel.reservationComplete.value!!.convertedTaxPrice!!
                            discount(
                                viewModel.getCurrencySymbol() + Utils.formatDecimal(subtotal * 0.001)
                                    .toString()
                            )
                            gstNumber(viewModel.gst.value)
                            discountGstVisibility(true)
                        } else if(viewModel.gst.value != null && viewModel.panNo.value == null){
                            discountGstText("GST TCS – U/S 52 of GST for GST Number-")
                            val subtotal =
                                viewModel.reservationComplete.value!!.convertTotalWithGuestServiceFee!! - viewModel.reservationComplete.value!!.convertedTaxPrice!!
                            discount(
                                viewModel.getCurrencySymbol() + Utils.formatDecimal(subtotal * 0.001)
                                    .toString()
                            )
                            gstNumber(viewModel.gst.value)
                            discountGstVisibility(true)
                        }
                        else if(viewModel.gst.value == null && viewModel.panNo.value != null){
                            discountGstText("Tax Withholding for India Income for PAN Number-")
                            val subtotal =
                                viewModel.reservationComplete.value!!.convertTotalWithGuestServiceFee!! - viewModel.reservationComplete.value!!.convertedTaxPrice!!
                            discount(
                                viewModel.getCurrencySymbol() + Utils.formatDecimal(subtotal * 0.001)
                                    .toString()
                            )
                            gstNumber(viewModel.gst.value)
                            discountGstVisibility(true)
                        }else {
                            discountGstText("Tax Withholding for India Income -")
                            val subtotal =
                                viewModel.reservationComplete.value!!.convertTotalWithGuestServiceFee!! - viewModel.reservationComplete.value!!.convertedTaxPrice!!
                            discount(
                                viewModel.getCurrencySymbol() + Utils.formatDecimal(subtotal * 0.05)
                                    .toString()
                            )
                            gstNumber(viewModel.gst.value)
                            discountGstVisibility(true)
                        }
                    }else{
                        discountGstVisibility(false)
                }


                if (viewModel.reservationComplete.value!!.results?.infants!= null && viewModel.reservationComplete.value!!.results?.infants!! >0.0) {
                    infantPrice(viewModel.getCurrencySymbol() + Utils.formatDecimal( viewModel.reservation.value!!.infantPrice!! * viewModel.reservation.value?.nights!!))
                    infantPriceVisibility(true)
                } else {
                    infantPriceVisibility(false)
                }
                if (viewModel.reservationComplete.value!!.results?.pets!= null && viewModel.reservationComplete.value!!.results?.pets!! >0.0) {
                    petPrice(viewModel.getCurrencySymbol() + Utils.formatDecimal( viewModel.reservation.value!!.petPrice!!.toDouble() * viewModel.reservation.value?.nights!!))
                    petPriceVisibility(true)
                } else {
                    petPriceVisibility(false)
                }
                if (viewModel.reservationComplete.value!!.results?.additionalGuest!= null && viewModel.reservationComplete.value!!.results?.additionalGuest!! >0.0) {
                    additionalPrice(viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservation.value!!.additionalPrice!! * viewModel.reservation.value?.nights!!))
                    additionalPriceVisibility(true)
                } else {
                    additionalPriceVisibility(false)
                }
                if (viewModel.reservationComplete.value!!.results?.visitors!= null && viewModel.reservationComplete.value!!.results?.visitors!! >0.0) {
                    visitorPrice(viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservation.value!!.visitorsPrice!!))
                    visitorPriceVisibility(true)
                } else {
                    visitorPriceVisibility(false)
                }

                if (userType == "Host") {
                    if (viewModel.reservationComplete.value!!.convertedHostServiceFee != null && viewModel.reservationComplete.value!!.convertedHostServiceFee!! > 0.0) {
                        servicePrice(
                            "- " + viewModel.getCurrencySymbol() + Utils.formatDecimal(
                                viewModel.reservationComplete.value!!.convertedHostServiceFee!!
                            ).toString()
                        )
                        servicePriceVisibility(true)
                    } else {
                        servicePriceVisibility(false)
                    }
                } else {
                    if (viewModel.reservationComplete.value!!.convertedGuestServicefee != null && viewModel.reservationComplete.value!!.convertedGuestServicefee!! > 0.0) {
                        servicePrice(
                            viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertedGuestServicefee!!)
                                .toString()
                        )
                        servicePriceVisibility(true)
                    } else {
                        servicePriceVisibility(false)
                    }

                }
                if (viewModel.reservationComplete.value!!.convertedTaxPrice != null && viewModel.reservationComplete.value!!.convertedTaxPrice != 0.0) {
                    taxesPrice(viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertedTaxPrice!!)
                        .toString())
                    taxesPriceVisibility(true)
                } else {
                    taxesPriceVisibility(false)
                }
//total price
                if (userType == "Host") {
                    if (viewModel.reservationComplete.value!!.convertedTotalWithHostServiceFee!! > 0.0)
                        totalPrice(
                            viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertedTotalWithHostServiceFee!!)
                                .toString()
                        )
                    else
                        totalPrice(viewModel.getCurrencySymbol() + "0.0")
                } else {
                    totalPrice(
                        viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertTotalWithGuestServiceFee!!)
                            .toString()
                    )
                }

                if (reservation.discountType != null && viewModel.reservationComplete.value!!.convertedDiscount!! > 0) {
                    discountVisibility(true)
                    val str = reservation.discountType!!
                    val strArray = str.split(" ")
                    val builder = StringBuilder()
                    for (s in strArray) {
                        val cap = s.substring(0, 1).toUpperCase() + s.substring(1)
                        builder.append("$cap ")
                    }
                    discountText(builder.toString())
                    val disc =
                        viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertedDiscount!!)
                    discountPrice("-" + disc)
                } else {
                    discountVisibility(false)
                }
            }
//payment received
            viewholderItineraryTextLeftRightBlack {
                id("Billing")
                rightSide(resources.getString(R.string.payment_received))

                if (viewModel.reservationComplete.value!!.convertedGuestServicefee != null && viewModel.reservationComplete.value!!.convertedGuestServicefee!! > 0) {
                    if (userType.equals("Host")) {
                        if (viewModel.reservationComplete.value!!.convertedTotalWithHostServiceFee!! > 0.0)
                            leftSide(
                                viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertedTotalWithHostServiceFee!!)
                                    .toString()
                            )
                        else
                            leftSide(viewModel.getCurrencySymbol() + "0.0")
                    } else {
                        leftSide(
                            viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertTotalWithGuestServiceFee!!)
                                .toString()
                        )
                    }
                } else {
                    leftSide(
                        viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertTotalWithGuestServiceFee!!)
                            .toString()
                    )
                }
                paddingBottom(true)
            }

            viewholderItineraryTextLeftRight {
                id("Billing date")
                rightSide(
                    Utils.epochToDate(
                        reservation.updatedAt!!.toLong(),
                        Utils.getCurrentLocale(requireContext())!!
                    )
                )
                paddingBottom(true)
            }

            viewholderDivider {
                id("divider5")
            }

            viewholderItineraryTextLeftRight {
                id("desc")
                leftSide(resources.getString(R.string.app_name) + " " + resources.getString(R.string.receipt_desc))
                paddingTop(true)
                paddingBottom(true)
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
package com.airhomestays.app.ui.listing.pricebreakdown

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.*
import com.airhomestays.app.databinding.FragmentListingPricebreakdownBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.listing.AdditionalGuestFragment
import com.airhomestays.app.ui.listing.InfantFragment
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.ui.listing.ListingDetailsViewModel
import com.airhomestays.app.ui.listing.PetsFragment
import com.airhomestays.app.ui.listing.amenities.AmenitiesBottomFragment
import com.airhomestays.app.ui.listing.cancellation.CancellationFragment
import com.airhomestays.app.ui.listing.guest.GuestFragment
import com.airhomestays.app.util.*
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.vo.ListingInitData
import timber.log.Timber
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.round


class PriceBreakDownFragment: BaseFragment<FragmentListingPricebreakdownBinding, ListingDetailsViewModel>() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_pricebreakdown
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    private lateinit var mBinding: FragmentListingPricebreakdownBinding

    var startDate: String = ""
    var endDate: String = ""
    var guestCount: String = ""
    lateinit var dateGuest : ListingDetails.PriceBreakDown
    val array = ArrayList<String>()
    var bookRedirection=0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.viewModel = viewModel
        if (viewModel.initialValue.value?.bookingType =="request"&& !viewModel.inboxIntent.value!!){
            mBinding.btnBook.text = getText(R.string.request_to_book)
        }else {
            mBinding.btnBook.text = getText(R.string.add_payment)
        }
        mBinding.breakDownToolbar.tvToolbarHeading .text= baseActivity!!.resources.getString(R.string.review_and_pay)
        mBinding.breakDownToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.btnBook.onClick {
            if (viewModel.billingCalculation.value != null) {
//                if (viewModel.msg.get()!!.trim().isEmpty()) {
//                    showToast( baseActivity!!.resources.getString(R.string.please_enter_the_message))
//                } else {
                hideKeyboard()
                if (viewModel.listingDetails.value!!.userId != viewModel.getUserId()) {
                    if (viewModel.initialValue.value?.bookingType =="request"&&!viewModel.inboxIntent.value!!){
                        viewModel.createRequestToBook()
                    }else {
                        viewModel.checkVerification()
                    }
                } else {
                    Toast.makeText(context,  baseActivity!!.resources.getString(R.string.you_cannot_book_your_own_list), Toast.LENGTH_LONG).show()
                }
//                }
            } else {
                showSnackbar( baseActivity!!.resources.getString(R.string.info),  baseActivity!!.resources.getString(R.string.please_select_another_date_to_book))
            }
        }

        bookRedirection=activity?.intent!!.getIntExtra("inboxBook",0)
        if (bookRedirection==1){
            viewModel.inboxIntent.value=true
        }
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.loadInitialValues(activity?.intent!!).observe(viewLifecycleOwner, Observer {
            it?.let { initValues ->   setup(initValues)

            }
        })

        viewModel.dateGuestCount.observe(viewLifecycleOwner, Observer {
            it?.let { dateGuestCount ->
                dateGuest = dateGuestCount
                mBinding.rlListingPricebreakdown.requestModelBuild()
            }
        })
        viewModel.billingCalculation.observe(viewLifecycleOwner, Observer {
            mBinding.rlListingPricebreakdown.requestModelBuild()
        })

        viewModel.listingDetails.observe(viewLifecycleOwner, Observer {
                listingDetails -> listingDetails?.let {
            it.houseRules?.forEachIndexed { _, t: ViewListingDetailsQuery.HouseRule? ->
                t?.itemName?.let { rules ->
                    array.add(rules)
                }
            }
        }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    private fun setup(it: ListingInitData) = try {
        mBinding.rlListingPricebreakdown.withModels {
            viewholderPricebreakListinfo {
                id("info")

                type(it.roomType+" / "+it.beds+" "+ baseActivity!!.resources.getQuantityString(R.plurals.caps_bed_count,it.beds))
                img(it.photo[0])
                title(it.title)

                viewModel.initialValue.value?.let {
                    if (viewModel.listingDetails.value?.listingData?.guestBasePrice!=null && it.guestCount > viewModel.listingDetails.value!!.listingData?.guestBasePrice!!) {
                        viewModel.additionalGuest?.let { additionalGuest ->
                            additionalGuest.set(
                                (it.guestCount - (viewModel.listingDetails.value!!.listingData?.guestBasePrice
                                    ?: 0.0).toInt()).toString()
                            )
                            if (it.guestCount != 1) {
                                it.additionalGuestCount =
                                    it.guestCount - (viewModel.listingDetails.value!!.listingData?.guestBasePrice
                                        ?: 0.0).toInt()
                            }
                        }
                    }else{
                        viewModel.additionalGuest.set("0")
                    }
                    /* if (viewModel.additionalGuest.get()!=null) {
                         viewModel.additionalGuest.set((it.guestCount - viewModel.listingDetails.value!!.listingData?.guestBasePrice!!.toInt()).toString())
                         if (it.guestCount != 1) {
                             it.additionalGuestCount =
                                 it.guestCount - viewModel.listingDetails.value!!.listingData?.guestBasePrice!!.toInt()
                         }
                     }else{
                         viewModel.additionalGuest.set("0")
                     }*/

                }

                if (viewModel.isListingDetailsInitialized()) {
                    price(
                        viewModel.getCurrencySymbol() + Utils.formatDecimal(
                            viewModel.getConvertedRate(
                                viewModel.listingDetails.value?.listingData?.currency!!,
                                viewModel.listingDetails.value?.listingData?.basePrice!!
                                    .toDouble()
                            )
                        )
                    )
                }
                var ratingCount = 0
                if (it.ratingStarCount != null && it.ratingStarCount!= 0 && it.ratingStarCount!= null && it.ratingStarCount != 0) {
                    val roundOff = round(it.ratingStarCount!!.toDouble() / it.reviewCount!!.toDouble())
                    Timber.d("ratingCount ${round(it.ratingStarCount!!.toDouble() / it.ratingStarCount!!.toDouble())}")
                    ratingCount = roundOff.toInt()
                    reviewsStarRating(viewModel.listingDetails.value?.reviewsCount?:0)
                } else {
                    ratingCount = 0
                }
                if (viewModel.reviewCount.value != null) {
                    reviewsCount(ratingCount)
                } else {
                    reviewsCount(0)
                }
            }

            viewholderDividerPadding {
                id("divider1")
            }

            viewholderReviewPayCheckin {
                id("checkinout")

                if (bookRedirection==1){
                    isEdit(true)
                }

                if (viewModel.billingCalculation.value!!.petLimit != 0.0 && viewModel.billingCalculation.value!!.petLimit != null) {
                    isPetVisible(true)
                } else {
                    isPetVisible(false)
                }
                if(viewModel.billingCalculation.value?.additionalPrice != 0.0 && viewModel.billingCalculation.value?.additionalPrice != null){
                    isAdditionalGuestVisible(true)
                }else{
                    isAdditionalGuestVisible(false)
                }
                if(viewModel.billingCalculation.value!!.infantPrice != 0.0 && viewModel.billingCalculation.value!!.infantPrice != null){
                    isInfantVisible(true)
                }else{
                    isInfantVisible(false)
                }
                if(viewModel.billingCalculation.value!!.visitorsPrice != 0.0 && viewModel.billingCalculation.value!!.visitorsPrice != null){
                    isVisitorGuestVisible(true)
                }else{
                    isVisitorGuestVisible(false)
                }
                if (getCalenderMonth(viewModel.startDate.value)== baseActivity!!.resources.getString(R.string.add_date)){
                    checkIn(getCalenderMonth(viewModel.startDate.value))
                    checkOut("")
                }else{
                    checkIn(getCalenderMonth(viewModel.startDate.value)+" - ")
                    checkOut(getCalenderMonth(viewModel.endDate.value))
                }

                guest(it.guestCount-it.additionalGuestCount)
                additionalGuest(it.additionalGuestCount)
                pet(it.petCount)
                infant(it.infantCount)
                visitor(it.visitors)


                checkInOnClick(View.OnClickListener { Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openAvailabilityActivity()}})
                checkOutOnClick(View.OnClickListener {Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openAvailabilityActivity()}})
                guestOnClick(View.OnClickListener { Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openFragment(GuestFragment())}})
                petOnClick(View.OnClickListener { Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openFragment(PetsFragment())}})
                infantOnClick(View.OnClickListener { Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openFragment(InfantFragment())}})
                visitorOnClick(View.OnClickListener { Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openFragment(AdditionalGuestFragment())}})
            }

            viewholderDividerPadding {
                id("divider2")
            }
            viewholderHeaderSmall {
                id("Tellus")
                header( baseActivity!!.resources.getString(R.string.tell_your_host_about_your_trip))
            }

            viewholderBookingMsgHost {
                id("viewholderBookingMsgHost")
                msg(viewModel.msg)
                onBind { _, view, _ ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.requestFocus()
                    editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    editText.setOnTouchListener(View.OnTouchListener { v, event ->
                        if (editText.hasFocus()) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                            when (event.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_SCROLL -> {
                                    v.parent.requestDisallowInterceptTouchEvent(true)
                                    return@OnTouchListener true
                                }
                            }
                        }
                        false
                    })
                }
                onUnbind { _, view ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.setOnTouchListener(null)
                }
            }

            if (viewModel.isLoading.get()) {
                viewholderLoader {
                    id("viewListingLoading")
                    isLoading(true)
                }
            } else {
                if (viewModel.billingCalculation.value != null) {
                    viewholderPricebreakSummary {
                        id("summary")
                        val isRTL= TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())==View.LAYOUT_DIRECTION_RTL
                        val baseRTL = viewModel.billingCalculation.value!!.nights.toString() + " x " + BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.averagePrice!!)

                        val baseLTR = BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.averagePrice!!) + " x " + viewModel.billingCalculation.value!!.nights
                        val ngt =  baseActivity!!.resources.getQuantityString(R.plurals.night_count, viewModel.billingCalculation.value!!.nights ?: 0);
                        basePrice(if(isRTL) baseRTL else baseLTR)
                        basePriceNights( ngt)
                        basePriceRight(BindingAdapters.getCurrencySymbol(it.selectedCurrency) +
                                Utils.formatDecimal(viewModel.billingCalculation.value!!.priceForDays!!))

                        if(viewModel.billingCalculation.value!!.isSpecialPriceAssigned!!) {
                            spIconVisible(true)
                            onBind { _, view, _ ->
                                val imgView = view.dataBinding.root.findViewById<ImageView>(R.id.specialPriceIcon)
                                val pricingLay = view.dataBinding.root.findViewById<LinearLayout>(R.id.spl_pricing_layout)
                                imgView.setOnClickListener {
                                    pricingLay.visible()
                                    val handler = Handler(Looper.getMainLooper())
                                    val r = Runnable {  pricingLay.gone() }
                                    handler.postDelayed(r, 3000)
                                }

                            }
                        }else{
                            spIconVisible(false)
                        }
                        if (viewModel.billingCalculation.value!!.cleaningPrice != null && viewModel.billingCalculation.value!!.cleaningPrice!! > 0) {
                            cleaningPrice(BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.cleaningPrice!!))
                            cleaningPriceVisibility(true)
                        } else {
                            cleaningPriceVisibility(false)
                        }

                        if (viewModel.billingCalculation.value!!.guestServiceFee != null && viewModel.billingCalculation.value!!.guestServiceFee != 0.0) {
                            servicePrice(BindingAdapters.getCurrencySymbol(viewModel.initialValue.value!!.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.guestServiceFee!!))
                            servicePriceVisibility(true)
                        } else {
                            servicePriceVisibility(false)
                        }
                        if (viewModel.billingCalculation.value!!.taxPrice != null && viewModel.billingCalculation.value!!.taxPrice != 0.0) {
                            taxesPrice(BindingAdapters.getCurrencySymbol(viewModel.initialValue.value!!.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.gstPrice!!))
                            taxesPriceVisibility(true)
                        } else {
                            taxesPriceVisibility(false)
                        }

                        if (it.infantCount!= null && it.infantCount !=0) {
                            infantPrice(BindingAdapters.getCurrencySymbol(viewModel.initialValue.value!!.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.infantPrice!!* it.infantCount*viewModel.billingCalculation.value!!.nights!!))
                            infantPriceVisibility(true)
                        } else {
                            infantPriceVisibility(false)
                        }
                        if (it.petCount != null && it.petCount != 0) {
                            petPrice(BindingAdapters.getCurrencySymbol(viewModel.initialValue.value!!.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.petPrice!!* it.petCount.toDouble()*viewModel.billingCalculation.value!!.nights!!))
                            petPriceVisibility(true)
                        } else {
                            petPriceVisibility(false)
                        }
                        if (it.additionalGuestCount != null && it.additionalGuestCount != 0) {
                            additionalPrice(BindingAdapters.getCurrencySymbol(viewModel.initialValue.value!!.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.additionalPrice!! * it.additionalGuestCount*viewModel.billingCalculation.value!!.nights!!))
                            additionalPriceVisibility(true)
                        } else {
                            additionalPriceVisibility(false)
                        }
                        if (it.visitors != null && it.visitors != 0) {
                            visitorPrice(BindingAdapters.getCurrencySymbol(viewModel.initialValue.value!!.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.visitorsPrice!! * it.visitors ))
                            visitorPriceVisibility(true)
                        } else {
                            visitorPriceVisibility(false)
                        }

                        if (viewModel.billingCalculation.value!!.discountLabel != null && viewModel.billingCalculation.value!!.discount!! > 0) {
                            discountVisibility(true)
                            val str = viewModel.billingCalculation.value!!.discountLabel!!
                            val strArray = str.split(" ")
                            val builder = StringBuilder()
                            for (s in strArray) {
                                val cap = s.substring(0, 1).toUpperCase() + s.substring(1)
                                builder.append(cap + " ")
                            }
                            discountText(builder.toString())
                            discountPrice("-" + BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.discount!!))
                            totalPrice(BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal((viewModel.billingCalculation.value!!.total!!)))
                        } else {
                            discountVisibility(false)
                            totalPrice(BindingAdapters.getCurrencySymbol(it.selectedCurrency) + Utils.formatDecimal(viewModel.billingCalculation.value!!.total!!))
                        }
                    }
                } else {
                    viewholderCenterTextPlaceholder {
                        id("ExploreBilling - No Dates")
                        header(  baseActivity!!.resources.getString(R.string.no_dates_available_to_book_please_select_another_date))
                        large(false)
                    }
                }
            }
            viewholderHeaderSmall {
                id("cancellation")
                header( baseActivity!!.resources.getString(R.string.cancellation_policy))
            }
            if (viewModel.isListingDetailsInitialized()){
                viewholderReviewAndPaySpanText {
                    id("DetailsDesc - CancellationContent")
                    span("")
                    spanColor( baseActivity!!.resources.getColor(R.color.colorPrimary))
                    desc( "Cancellation policy"+" is "+"'"+viewModel.listingDetails.value!!.listingData?.cancellation?.policyName
                            +"'"+" and you can "+viewModel.listingDetails.value!!.listingData?.cancellation?.policyContent)
                    var end = 0
                    if(viewModel.listingDetails.value!!.listingData?.cancellation?.policyName=="Strict")
                        end = 30
                    else
                        end = 32
                    start(24)
                    end(end)
                    clickListener(View.OnClickListener {
                        (baseActivity as ListingDetails).openFragment(CancellationFragment())
                    })
                    paddingTop(true)
                    paddingBottom(true)
                }}
            viewholderDivider {
                id("viewholder_divider - 3")
            }

            viewholderReviewAndPaySpanText{
                id("DetailsDesc - TermsAndPolicy")
                span("")
                spanColor( baseActivity!!.resources.getColor(R.color.colorPrimary))
                desc("I agree to the House rules, Cancellation Policy, and the Guest Refund Policy." +
                        " I also agree to pay the total amount shown, which includes service fees.")
                start(15)
                end(26)
                paddingTop(true)
                paddingBottom(true)
                clickListener(View.OnClickListener {
                    Utils.clickWithDebounce(it){
                        val bottomSheet = AmenitiesBottomFragment.newInstance("House rules")
                        bottomSheet.show(childFragmentManager, "bottomSheetFragment")
                    }

                })
            }

        }
    } catch (e: KotlinNullPointerException) {
        e.printStackTrace()
    }

    private fun getCalenderMonth(dateString: String?): String {
        try {
            return if (dateString != null && dateString != "0" && dateString.isNotEmpty()) {
                val startDateArray: List<String> = dateString.split("-")
                val year: Int = startDateArray[0].toInt()
                val month = startDateArray[1].toInt()
                val date = startDateArray[2].toInt()
                val decimalFormat=DecimalFormat("00")
                val datef= decimalFormat.format(date)
                datef.lowercase(Locale.US)

                val monthPattern = SimpleDateFormat("MMM dd", Locale.US)
                val cal = Calendar.getInstance()
                cal.set(year, month - 1, date)
                monthPattern.format(cal.time)
            } else {
                getString(R.string.add_date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    override fun onRetry() {
        viewModel.getBillingCalculation()
    }

    override fun onResume() {
        super.onResume()
        viewModel.clearStatusBar(requireActivity())
    }

}
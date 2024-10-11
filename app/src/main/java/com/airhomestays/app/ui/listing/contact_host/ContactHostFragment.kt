package com.airhomestays.app.ui.listing.contact_host

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.*
import com.airhomestays.app.databinding.FragmentListingPricebreakdownBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.ui.listing.ListingDetailsViewModel
import com.airhomestays.app.ui.listing.guest.GuestFragment
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.onClick
import com.airhomestays.app.vo.ListingInitData
import timber.log.Timber
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.round

class ContactHostFragment: BaseFragment<FragmentListingPricebreakdownBinding, ListingDetailsViewModel>() {

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
    lateinit var dateGuest: ListingDetails.PriceBreakDown

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.viewModel = viewModel
        initView()
        subscribeToLiveData()
        if(activity is ListingDetails){
            (activity as ListingDetails).changeStatusBarColor(R.color.black)
        }

    }

    private fun initView() {
        mBinding.btnBook.text = resources.getString(R.string.send_message)
        mBinding.breakDownToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.breakDownToolbar.tvToolbarHeading.text= getString(R.string.contact_host)
        mBinding.btnBook.onClick {
            if (viewModel.startDate.value.isNullOrEmpty() || viewModel.endDate.value.isNullOrEmpty()||viewModel.startDate.value=="0") {
                showToast(resources.getString(R.string.please_select_date))
            } else if (viewModel.initialValue.value?.guestCount == 0) {
                showToast(resources.getString(R.string.please_select_guest))
            } else if (viewModel.msg.get()!!.trim().isNullOrEmpty()) {
                showToast(resources.getString(R.string.please_enter_the_message))
            } else {
                hideKeyboard()
                viewModel.retryCalled = "contact"
                viewModel.contactHost()
            }
        }
    }

    private fun subscribeToLiveData() {
        viewModel.initialValue.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let { initValue ->
                setup(initValue)
            }
        })

        viewModel.dateGuestCount.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let { dateDiscount ->
                dateGuest = dateDiscount
                viewModel.getBillingCalculation()
                mBinding.rlListingPricebreakdown.requestModelBuild()
            }
        })
        viewModel.startDate.observe(viewLifecycleOwner,androidx.lifecycle.Observer {
            it.let {
                mBinding.rlListingPricebreakdown.requestModelBuild()
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setup(it: ListingInitData) {
        mBinding.rlListingPricebreakdown.withModels {
            viewholderPricebreakListinfo {
                id("info")
                type(it.roomType)
                img(it.photo[0])
                title(it.title)
                price(it.price)
                imageClick(View.OnClickListener {
                    Utils.clickWithDebounce(it){ListingDetails.openListDetailsActivity(requireContext(),viewModel.initialValue.value!!)}
                })
                var ratingCount = 0
                if (it.ratingStarCount != null && it.ratingStarCount!= 0 && it.ratingStarCount!= null && it.ratingStarCount != 0) {
                    val roundOff = round(it.ratingStarCount!!.toDouble() / it.reviewCount!!.toDouble())
                    Timber.d("ratingCount ${round(it.ratingStarCount!!.toDouble() / it.ratingStarCount!!.toDouble())}")
                    ratingCount = roundOff.toInt()
                    reviewsStarRating(viewModel.listingDetails.value!!.reviewsCount)
                } else {
                    ratingCount = 0
                }
                reviewsCount(ratingCount)
            }
            viewholderDividerPadding {
                id("divider1")
            }
            viewholderReviewPayCheckin {
                id("checkinout")
                if (getCalenderMonth(viewModel.startDate.value)==resources.getString(R.string.add_date)){
                    checkIn(getCalenderMonth(viewModel.startDate.value))
                    checkOut("")
                }
                else if(viewModel.startDate.value!="0"&&viewModel.endDate.value!="0"){

                    checkIn(getCalenderMonth(viewModel.startDate.value)+" - ")
                    checkOut(getCalenderMonth(viewModel.endDate.value))
                }
                else {
                    checkIn(resources.getString(R.string.add_date))
                    checkOut("")
                }

                guest(it.guestCount)
                additionalGuest(it.additionalGuestCount)
                pet(it.petCount)
                infant(it.infantCount)
                visitor(it.visitors)

               checkInOnClick(View.OnClickListener {Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openAvailabilityActivity()}})
                checkOutOnClick(View.OnClickListener {Utils.clickWithDebounce(it){(baseActivity as ListingDetails).openAvailabilityActivity()}})
                guestOnClick(View.OnClickListener {
                    Utils.clickWithDebounce(it){
                        hideKeyboard()
                        (baseActivity as ListingDetails).openFragment(GuestFragment())
                    }
                })
            }
            viewholderDividerPadding {
                id("divider2")
            }
            viewholderHeaderSmall {
                id("Tellus")
                header(getString(R.string.your_message))
            }
            viewholderBookingMsgHost {
                id("dsfg")
                msg(viewModel.msg)
                hint(true)
                onBind { _, view, _ ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
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
        }
    }

    private fun getCalenderMonth(dateString: String?): String {
        try {
            return if (dateString != null && dateString != "0") {
                val startDateArray: List<String> = dateString.split("-")
                val year: Int = startDateArray[0].toInt()
                val month = startDateArray[1].toInt()
                val date = startDateArray[2].toInt()
                var decimalFormat= DecimalFormat("00")
                var datef= decimalFormat.format(date)

                val monthPattern = SimpleDateFormat("MMM", Locale.ENGLISH)
                val cal = Calendar.getInstance()
                cal.set(year, month - 1, date)
                monthPattern.format(cal.time) + " " + datef
            } else {
                resources.getString(R.string.add_date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return resources.getString(R.string.add_date)
        }
    }

    override fun onRetry() {

    }
    override fun onResume() {
        super.onResume()
        viewModel.clearStatusBar(requireActivity())
    }
}
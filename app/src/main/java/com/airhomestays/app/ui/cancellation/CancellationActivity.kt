package com.airhomestays.app.ui.cancellation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.CancellationDataQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentCancellationBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.ui.user_profile.UserProfileActivity
import com.airhomestays.app.util.*
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.vo.ListingInitData
import dagger.android.DispatchingAndroidInjector
import java.util.Locale
import javax.inject.Inject
import kotlin.math.round

class CancellationActivity : BaseActivity<FragmentCancellationBinding, CancellationViewModel>(),
    CancellationNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentCancellationBinding
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_cancellation
    override val viewModel: CancellationViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(CancellationViewModel::class.java)
    private var reservationId = 0
    private var userType = ""
    var hostProfileID = 0
    var intentType = 0
    var beds = 0

    companion object {
        @JvmStatic
        fun openCancellationActivity(
            activity: Activity,
            reservationId: Int,
            hostID: Int,
            userType: String,
            intentType: Int
        ) {
            val intent = Intent(activity, CancellationActivity::class.java)
            intent.putExtra("reservationId", reservationId)
            intent.putExtra("userType", userType)
            intent.putExtra("hostID", hostID)
            intent.putExtra("type", intentType)
            activity.startActivityForResult(intent, 5)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this

        initView()
        subscribeToLiveData()

    }

    private fun initView() {
        intent?.extras?.let {
            reservationId = it.getInt("reservationId")
            userType = it.getString("userType")!!
            hostProfileID = it.getInt("hostID")
            intentType = it.getInt("type")
        }
        mBinding.actionBar.tvToolbarHeading.text = getString(R.string.cancel_your_reservation)
        mBinding.actionBar.ivNavigateup.onClick { finish() }
        mBinding.scroll.gone()
        mBinding.spanTv.onClick {
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_up,
                    R.anim.slide_down,
                    R.anim.slide_up,
                    R.anim.slide_down
                )
                .add(mBinding.flProfile.id, CancellationPolicy())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadCancellationDetails(reservationId, userType).observe(this, Observer {
            it?.let { details ->
                setDetails(details)
            }
        })
        viewModel.loadListingDetails().observe(this, Observer {
            it?.let { detail ->
                viewModel.beds.value = detail.beds!!
                if (viewModel.beds.value == 0) {
                    mBinding.type = detail.roomType
                } else {
                    mBinding.type =
                        detail.roomType + " / " + viewModel.beds.value + resources.getQuantityString(
                            R.plurals.caps_bed_count,
                            viewModel.beds.value!!
                        )

                }

                mBinding.span = detail.listingData?.cancellation?.policyName

                mBinding.desc3 =
                    detail.listingData?.cancellation?.policyContent?.lowercase(Locale.getDefault())

            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setDetails(details: CancellationDataQuery.Results) {

        try {
            with(mBinding) {
                val checkIn = Utils.getMonth1(details.checkIn!!)
                val checkOut = Utils.getMonth1(details.checkOut!!)
                this.listDate = "$checkIn - $checkOut"
                this.listTitle = details.listTitle
                this.image = details.listData?.listPhotos!![0]?.name
                this.title = details.listData?.title

                this.desc = getString(R.string.cancellation_policy) + " is "

                this.desc2 = " and you can "

                var reviewsCount = 0f
                if (details.listData!!.reviewsStarRating != null && details.listData!!
                        .reviewsStarRating != 0 && details.listData!!
                        .reviewsStarRating != null && details.listData!!
                        .reviewsStarRating != 0
                ) {
                    val roundOff = round(
                        details.listData!!.reviewsStarRating!!.toDouble() / details.listData!!
                            .reviewsCount!!.toDouble()
                    )
                    reviewsCount = roundOff.toFloat()
                    this.reviewsCount = reviewsCount.toInt()
                    mBinding.tvListingRating.visible()
                    this.reviewsStarRating = round(
                        details.listData?.reviewsStarRating!!
                            .toDouble() / reviewsCount.toDouble()
                    ).toInt()
                } else {
                    reviewsCount = 0f
                    this.reviewsCount = reviewsCount.toInt()
                    mBinding.tvListingRating.gone()
                }

                mBinding.tvListingReview.text = reviewsCount.toString()


                details.nonRefundableNightPrice?.let {
                    if (it == 0.0) {
                        mBinding.rlNonRefund.gone()
                        this.nonrefundablePrice = "0"
                    } else {
                        mBinding.rlNonRefund.visible()
                        if (it != null) {
                            if (it > 0.0)
                                this.nonrefundablePrice =
                                    viewModel?.getCurrencySymbol() + " " + Utils.formatDecimal(it)
                            else
                                this.nonrefundablePrice = viewModel?.getCurrencySymbol() + " 0.0"
                        } else {
                            this.nonrefundablePrice = "0"
                        }
                    }
                }
                if (this.nonrefundablePrice == null) {
                    this.nonrefundablePrice = "0"
                }
                if (details.refundToGuest!! > 0.0) {
                    this.refundablePrice =
                        viewModel?.getCurrencySymbol() + " " + Utils.formatDecimal(details.refundToGuest!!)
                } else {
                    this.refundablePrice = viewModel?.getCurrencySymbol() + " 0"
                }
                this.guestCount = details.guests!!.toString() + resources.getQuantityString(
                    R.plurals.guest_count,
                    details.guests!!
                )
                if (details.startedIn!! > 1) {
                    this.startedDay = details.startedIn!!
                        .toString() + resources.getQuantityString(
                        R.plurals.day_count,
                        details.startedIn!!
                    )
                } else {
                    this.startedDay =
                        details.startedIn!!.toString() + resources.getString(R.string.day)
                }
                this.stayingFor = details.stayingFor!!.toInt()
                    .toString() + resources.getQuantityString(
                    R.plurals.night_count,
                    Math.round(details.stayingFor!!).toInt()
                ) + " - " + "$checkIn to $checkOut"
                if (userType.equals("host")) {
                    mBinding.rlNonRefund.visible()
                    this.listName = details.guestName
                    this.hostorGuest = "Guest"
                    this.tvTellYou.text = getString(R.string.tell_you, "me")
                    this.listImage = details.guestProfilePicture
                    this.etMsg.hint = getString(R.string.tell_you, "guest")
                    mBinding.rlRefund.visibility = View.GONE
                    mBinding.tvCostNights.visibility = View.VISIBLE
                    mBinding.tvNonRefundable.text = resources.getString(R.string.missed_earnings)
                    val currency = viewModel?.getCurrencySymbol() + " " + Utils.formatDecimal(
                        viewModel?.getConvertedRate(
                            details.listData!!.listingData?.currency!!,
                            details.isSpecialPriceAverage!!.toDouble()
                        )!!
                    )

                    mBinding.tvCostNights.text = currency + " x " + details.stayingFor!!.toInt()
                        .toString() + resources.getQuantityString(
                        R.plurals.night_count,
                        Math.round(details.stayingFor!!).toInt()
                    )


                    mBinding.tvNonRefundPrice.background = getDrawable(R.drawable.strike_bg)
                    mBinding.tvRefundedCost.visibility = View.GONE
                } else {
                    if (this.nonrefundablePrice.equals("0") || this.nonrefundablePrice!!.contains("-")) {
                        mBinding.rlNonRefund.gone()
                    } else {
                        mBinding.rlNonRefund.visible()
                        mBinding.tvNonRefundPrice.background = getDrawable(R.drawable.strike_bg)
                    }


                    this.listName = details.hostName
                    this.hostorGuest = "Host"
                    this.tvTellYou.text = getString(R.string.tell_you, "me")
                    this.listImage = details.hostProfilePicture
                    this.etMsg.hint = getString(R.string.tell_you, "host")
                    mBinding.btnKeepReservation.text = getString(R.string.keep_trip)
                    mBinding.btnYourReservation.text = getString(R.string.cancel_your_trip)

                    if (details.refundToGuest != null && details.refundToGuest!! > 0.0) {
                        mBinding.rlRefund.visibility = View.VISIBLE
                        mBinding.tvRefundedCost.visibility = View.VISIBLE
                    } else {
                        mBinding.rlRefund.visibility = View.GONE
                        mBinding.tvRefundedCost.visibility = View.GONE
                    }

                    mBinding.tvCostNights.visibility = View.INVISIBLE
                    mBinding.tvRefundedCost.text =
                        resources.getString(R.string.you_will_be_refunded_with_the_above_cost)
                }
                this.setCancelClickListener {
                    if (this.etMsg.text.trim().isNotEmpty()) {
                        (viewModel as CancellationViewModel).retryCalled =
                            "cancel-${this.etMsg.text.trim()}"
                        (viewModel as CancellationViewModel).cancelReservation(
                            this.etMsg.text.trim().toString(), reservationId
                        )
                    } else {
                        showToast(resources.getString(R.string.enter_msg))
                    }
                }
                this.setListClickListener {
                    try {
                        val currency =
                            BindingAdapters.getCurrencySymbol(viewModel!!.getUserCurrency()) + CurrencyUtil.getRate(
                                base = viewModel!!.getCurrencyBase(),
                                to = viewModel!!.getUserCurrency(),
                                from = details.listData!!.listingData!!.currency!!,
                                rateStr = viewModel!!.getCurrencyRates(),
                                amount = details.listData!!.listingData?.basePrice!!
                                    .toDouble()
                            ).toString()
                        val photo = ArrayList<String>()
                        photo.add(details.listData!!.listPhotos!![0]?.name!!)
                        ListingDetails.openListDetailsActivity(
                            view1.context, ListingInitData(
                                details.listData!!.title!!,
                                photo,
                                details.listData!!.id!!,
                                details.listData!!.roomType!!,
                                details.listData!!.reviewsStarRating,
                                details.listData!!.reviewsCount,
                                currency,
                                0,
                                selectedCurrency = viewModel!!.getUserCurrency(),
                                currencyBase = viewModel!!.getCurrencyBase(),
                                currencyRate = viewModel!!.getCurrencyRates(),
                                startDate = "0",
                                endDate = "0",
                                bookingType = details.listData!!.bookingType!!
                            )
                        )
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                    }
                }
                this.setKeepClickListener {
                    if (intentType == 1) {
                        finish()
                    } else {
                        moveBackScreen()
                    }

                }
                this.setImageClick {
                    UserProfileActivity.openProfileActivity(view1.context, hostProfileID)
                }
                mBinding.scroll.visible()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    override fun moveBackScreen() {
        val intent = Intent()
        setResult(56, intent)
        finish()
    }

    override fun onRetry() {
        if (viewModel.retryCalled.equals("")) {
            viewModel.getCancellationDetails(reservationId, userType)
        } else {
            var text = viewModel.retryCalled.split("-")
            viewModel.cancelReservation(text[1], reservationId)
        }
    }

}
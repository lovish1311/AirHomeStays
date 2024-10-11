package com.airhomestays.app.ui.reservation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.*
import com.airhomestays.app.databinding.FragmentBookingStep1Binding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.inbox.msg_detail.NewInboxMsgActivity
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.ui.saved.SavedBotomSheet
import com.airhomestays.app.ui.user_profile.UserProfileActivity
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.vo.InboxMsgInitData
import com.airhomestays.app.vo.ListingInitData
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.round

class ItineraryFragment : BaseFragment<FragmentBookingStep1Binding, ReservationViewModel>() {

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
    private lateinit var location: LatLng

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.rlListingBottom.gone()
        mBinding.ivNavigateup.setImageResource(R.drawable.ic_left_arrow)
        mBinding.idHeaderTitle.visibility = View.VISIBLE
        mBinding.idHeaderTitle.text = getString(R.string.itinerary_title)
        if (viewModel.type == 3) {
            baseActivity!!.onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        println("Back button pressed")
                        val intent = Intent(baseActivity!!, HomeActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        baseActivity!!.finish()
                    }
                })
        }
        mBinding.ivNavigateup.onClick {
            if (viewModel.type == 3) {
                val intent = Intent(baseActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                baseActivity?.finish()
            } else
                baseActivity?.onBackPressed()
        }
        viewModel.reservation.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let { reservationDetails ->
                setup(reservationDetails)
            }
        })
    }

    private fun setup(it: GetReservationQuery.Results) {
        try {
            if (it.listData?.lat!!.isNaN().not() && it.listData?.lng!!.isNaN().not()) {
                location = LatLng(it.listData?.lat!!, it.listData?.lng!!)
            }
        } catch (e: Exception) {
            showError()
        }
        mBinding.rlBooking.withModels {

            viewholderItineraryText {
                id("text2")
                text(resources.getString(R.string.reservation_code) + ": #${it.confirmationCode}")
                large(false)
                isRed(false)
                paddingTop(false)
                isBold(false)
                clickListener(View.OnClickListener { })
            }
            viewholderItineraryText {
                id("text1")
                text(resources.getString(R.string.you_are_going_to) + " ${it.listData?.city}!")
                large(true)
                isRed(false)
                isBold(true)
                paddingTop(true)
            }
            var ratingCount = ""
            if (it.listData?.reviewsStarRating != null && it.listData
                    ?.reviewsStarRating != 0 && it.listData
                    ?.reviewsCount != null && it.listData?.reviewsCount != 0
            ) {
                ratingCount = round(
                    it.listData?.reviewsStarRating!!.toDouble() / it.listData
                        ?.reviewsCount!!.toDouble()
                ).toInt().toString()
                Timber.d(
                    "ratingCount ${
                        round(
                            it.listData?.reviewsStarRating!!.toDouble() / it.listData
                                ?.reviewsCount!!.toDouble()
                        )
                    }"
                )
            } else {
                ratingCount = ""
            }
            viewholderItinenaryListinfo {
                id("listInfo")
                type(resources.getString(R.string.shared_room))
                if (it.listData != null && it.listData!!.title != null) {
                    title(it.listData?.title!!.trim().replace("\\s+", " "))
                }
                reviewsCount(it.listData?.reviewsCount)
                img(it.listData?.listPhotoName)
                imageClick { _ ->
                    try {
                        val currency = viewModel.getCurrencySymbol() + viewModel.getConvertedRate(
                            it.currency!!,
                            it.basePrice!!.toDouble()
                        ).toString()
                        val photo = ArrayList<String>()
                        photo.add(it.listData!!.listPhotoName!!)
                        ListingDetails.openListDetailsActivity(
                            requireContext(), ListingInitData(
                                it.listData!!.title!!,
                                photo,
                                it.listData!!.id!!,
                                it.listData!!.roomType!!,
                                it.listData!!.reviewsStarRating,
                                it.listData!!.reviewsCount,
                                currency,
                                0,
                                selectedCurrency = viewModel.getUserCurrency(),
                                currencyBase = viewModel.getCurrencyBase(),
                                currencyRate = viewModel.getCurrencyRates(),
                                startDate = "0",
                                endDate = "0",
                                bookingType = it.listData!!.bookingType!!,
                                isWishList = it.listData!!.wishListStatus!!
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showError()
                    }
                }
                rating(ratingCount)
                reviewsStarRating(it.listData?.reviewsStarRating)
                location(
                    "${it.listData?.roomType} / ${
                        it.listData?.beds
                    } ${
                        resources.getQuantityString(
                            R.plurals.caps_bed_count,
                            it.listData?.beds!!
                        )
                    }"
                )
                wishListStatus(it.listData?.wishListStatus)
                isOwnerList(it.listData?.isListOwner)
                val listPhotoName = it.listData?.listPhotoName
                val id = it.listId
                val wishListGroupCount = it.listData?.wishListGroupCount
                heartClickListener(View.OnClickListener {
                    try {
                        val bottomSheet = SavedBotomSheet.newInstance(
                            id!!,
                            listPhotoName!!,
                            false,
                            wishListGroupCount
                        )
                        bottomSheet.show(childFragmentManager, "bottomSheetFragment")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })
            }
            viewholderDivider {
                id("divider3jj")
            }

            viewholderBookingDateInfo {
                id("booking_date")
                try {
                    if (resources.getBoolean(R.bool.is_left_to_right_layout).not()) {
                        ltrDirection(false)
                    } else {
                        ltrDirection(true)
                    }
                    paddingBottom(true)
                    checkIn(
                        Utils.epochToDate(
                            it.checkIn!!.toLong(),
                            Utils.getCurrentLocale(requireContext())!!
                        )
                    )
                    checkOut(
                        Utils.epochToDate(
                            it.checkOut!!.toLong(),
                            Utils.getCurrentLocale(requireContext())!!
                        )
                    )
                    if (it.checkInStart == "Flexible" && it.checkInEnd == "Flexible") {
                        startTime(getString(R.string.flexible_check_in_time))
                    } else if (it.checkInStart != "Flexible" && it.checkInEnd == "Flexible") {
                        val sTime = when {
                            BindingAdapters.timeConverter(it.checkInStart!!) == "0AM" -> {
                                "12AM"
                            }

                            BindingAdapters.timeConverter(it.checkInStart!!) == "0PM" -> {
                                "12PM"
                            }

                            else -> {
                                BindingAdapters.timeConverter(it.checkInStart!!)
                            }
                        }
                        startTime("${getString(R.string.from)} $sTime".toUpperCase())
                    } else if (it.checkInStart == "Flexible" && it.checkInEnd != "Flexible") {
                        val eTime = when {
                            BindingAdapters.timeConverter(it.checkInEnd!!) == "0AM" -> {
                                "12AM"
                            }

                            BindingAdapters.timeConverter(it.checkInEnd!!) == "0PM" -> {
                                "12PM"
                            }

                            else -> {
                                BindingAdapters.timeConverter(it.checkInEnd!!)
                            }
                        }
                        startTime("${getString(R.string.upto)} $eTime".toUpperCase())
                    } else if (it.checkInStart != "Flexible" && it.checkInEnd != "Flexible") {
                        val sTime = when {
                            BindingAdapters.timeConverter(it.checkInStart!!) == "0AM" -> {
                                "12AM"
                            }

                            BindingAdapters.timeConverter(it.checkInStart!!) == "0PM" -> {
                                "12PM"
                            }

                            else -> {
                                BindingAdapters.timeConverter(it.checkInStart!!)
                            }
                        }

                        val eTime = when {
                            BindingAdapters.timeConverter(it.checkInEnd!!) == "0AM" -> {
                                "12AM"
                            }

                            BindingAdapters.timeConverter(it.checkInEnd!!) == "0PM" -> {
                                "12PM"
                            }

                            else -> {
                                BindingAdapters.timeConverter(it.checkInEnd!!)
                            }
                        }
                        startTime("$sTime - $eTime".toUpperCase())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                timeVisibility(true)
            }

            viewholderDivider {
                id("divider3")
            }

            viewholderItineraryTextBold {
                id("Billing")
                text(resources.getString(R.string.billing))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryTextLeftRight {
                id("Billing_text")
                try {
                    rightSide(
                        it.nights.toString() + " " + resources.getQuantityString(
                            R.plurals.night_count,
                            it.nights!!
                        )
                    )
                    leftSide(
                        viewModel.getCurrencySymbol() + Utils.formatDecimal(viewModel.reservationComplete.value!!.convertTotalWithGuestServiceFee!!)
                            .toString()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                paddingTop(true)
                paddingBottom(true)
            }

            viewholderItineraryText {
                id("text3")
                text(resources.getString(R.string.view_receipt))
                large(false)
                isRed(true)
                paddingTop(false)
                paddingBottom(true)
                clickListener(View.OnClickListener {
                    (baseActivity as ReservationActivity).navigateToScreen(9)
                })
            }

            viewholderDivider {
                id("divider54")
            }

            viewholderItineraryTextBold {
                id("Address")
                text(resources.getString(R.string.address))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryTextLeftRight {
                id("addressText")
                paddingBottom(true)
                paddingTop(true)
                rightSide(
                    it.listData?.street + ", " +
                            it.listData?.city + ", " +
                            it.listData?.state + ", " +
                            it.listData?.country + ", " +
                            it.listData?.zipcode
                )
            }

            viewholderItineraryText {
                id("viewListing")
                text(resources.getString(R.string.view_Listing))
                isRed(true)
                large(false)
                paddingBottom(true)
                paddingTop(false)
                clickListener(View.OnClickListener { view ->
                    Utils.clickWithDebounce(view) {
                        try {
                            val currency =
                                viewModel.getCurrencySymbol() + viewModel.getConvertedRate(
                                    it.currency!!,
                                    it.basePrice!!.toDouble()
                                ).toString()
                            val photo = ArrayList<String>()
                            photo.add(it.listData!!.listPhotoName!!)
                            ListingDetails.openListDetailsActivity(
                                requireContext(), ListingInitData(
                                    it.listData!!.title!!,
                                    photo,
                                    it.listData!!.id!!,
                                    it.listData!!.roomType!!,
                                    it.listData!!.reviewsStarRating,
                                    it.listData!!.reviewsCount,
                                    currency,
                                    0,
                                    selectedCurrency = viewModel.getUserCurrency(),
                                    currencyBase = viewModel.getCurrencyBase(),
                                    currencyRate = viewModel.getCurrencyRates(),
                                    startDate = "0",
                                    endDate = "0",
                                    bookingType = it.listData!!.bookingType!!,
                                    isWishList = it.listData!!.wishListStatus!!
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showError()
                        }
                    }
                })
            }

            viewholderItineraryTextBold {
                id("direction")
                text(resources.getString(R.string.get_direction))
                isRed(true)
                large(false)
                paddingBottom(false)
                paddingTop(true)

                clickListener(View.OnClickListener { view ->
                    Utils.clickWithDebounce(view) {
                        openGoogleMapsForDirections(location)
                        //(baseActivity as ReservationActivity).navigateToScreen(10)
                    }
                })
            }

            viewholderDivider {
                id("divider4")
            }

            viewholderItineraryTextBold {
                id("Host")
                text(resources.getString(R.string.host))
                isRed(false)
                large(false)
                paddingBottom(false)
                paddingTop(true)
            }

            viewholderItineraryAvatar {
                id("avatar")
                avatarImg(it.hostData?.picture)
                isNameVisible(true)
                list(it.hostData?.firstName)
                text(resources.getString(R.string.message_host))
                onBind { model, view, position ->
                    if (viewModel.dataManager.currentUserId == it.hostId) {
                        val txtView =
                            view.dataBinding.root.findViewById<TextView>(R.id.message_host)
                        txtView.gone()
                    }
                }
                isRed(true)
                large(false)
                avatarClick(View.OnClickListener { view ->
                    Utils.clickWithDebounce(view) {
                        UserProfileActivity.openProfileActivity(
                            requireContext(),
                            it.hostData?.profileId!!
                        )
                    }
                })
                clickListener(View.OnClickListener { view ->
                    Utils.clickWithDebounce(view) {
                        try {
                            NewInboxMsgActivity.openInboxMsgDetailsActivity(
                                baseActivity!!, InboxMsgInitData(
                                    threadId = it.messageData?.id!!,
                                    guestId = it.guestData?.userId.toString(),
                                    guestName = it.guestData?.firstName!!,
                                    guestPicture = it.guestData?.picture,
                                    hostId = it.hostData?.userId.toString(),
                                    hostName = it.hostData?.firstName!!,
                                    hostPicture = it.hostData?.picture,
                                    senderID = it.guestData?.profileId!!,
                                    receiverID = it.hostData?.profileId!!,
                                    listID = it.listId
                                )
                            )
                        } catch (e: KotlinNullPointerException) {
                            e.printStackTrace()
                            showError()
                        }
                    }
                })
            }

            viewholderItineraryText {
                id("phoneNumber")
                text(it.hostData?.phoneNumber?:"")
                isRed(true)
                large(false)
                paddingBottom(false)
                paddingTop(true)

                clickListener(View.OnClickListener { view ->
                    Utils.clickWithDebounce(view) {
                        val phone = it.hostData!!.phoneNumber
                        val intent =
                            Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                        startActivity(intent)
                    }
                })

            }
        }
    }

    override fun onDestroyView() {
        mBinding.rlBooking.adapter = null
        super.onDestroyView()
    }

    private fun openGoogleMapsForDirections(location: LatLng) {
        val uri = "http://maps.google.com/maps?daddr=${location.latitude},${location.longitude}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            // Handle the case when Google Maps is not installed
            showToast("Google Maps is not installed on this device.")
        }
    }
    override fun onRetry() {

    }

}
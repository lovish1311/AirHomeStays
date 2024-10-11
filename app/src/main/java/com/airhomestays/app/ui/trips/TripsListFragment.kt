package com.airhomestays.app.ui.trips

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import com.airhomestays.app.BR
import com.airhomestays.app.GetAllReservationQuery
import com.airhomestays.app.R
import com.airhomestays.app.ViewholderTripsListBindingModel_
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.data.remote.paging.Status
import com.airhomestays.app.databinding.FragmentTripsListBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.cancellation.CancellationActivity
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.inbox.msg_detail.NewInboxMsgActivity
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.ui.reservation.ReservationActivity
import com.airhomestays.app.ui.trips.contactus.ContactSupport
import com.airhomestays.app.util.CurrencyUtil
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.epoxy.CustomSpringAnimation
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.vo.InboxMsgInitData
import com.airhomestays.app.vo.ListingInitData
import timber.log.Timber
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"

class TripsListFragment : BaseFragment<FragmentTripsListBinding, TripsViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentTripsListBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_trips_list
    override val viewModel: TripsViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(TripsViewModel::class.java)
    private lateinit var pagingController: TripsListController
    private var param1: String? = null
    private var menuVisible = ArrayList<PopupMenu>()
    lateinit var openListingDetailResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            TripsListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
        openListingDetailResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        mBinding.shimmer.visible()
        mBinding.ltLoadingView.gone()
        mBinding.shimmer.startShimmer()
        CustomSpringAnimation.spring(mBinding.rvTripsList)
        initController()
        mBinding.image.setImageDrawable(resources.getDrawable(R.drawable.ic_no_result))
        viewModel.let {
            if (param1 == "upcoming") {
                subscribeToLiveData1()
            } else {
                mBinding.tvNoReservation.text = getString(R.string.no_previous_trip)
                mBinding.tvStartPlanning.text =
                    getString(R.string.start_planning_your_next_adventure_on_appname)
                subscribeToLiveData()
            }
        }
        mBinding.btnExplore.onClick {
            (baseActivity as HomeActivity).viewDataBinding?.let {
                it.vpHome.setCurrentItem(0, false)
            }
        }
    }

    private fun initController() {
        try {
            pagingController = TripsListController(
                viewModel.getCurrencyBase(),
                viewModel.getCurrencyRates(),
                viewModel.getUserCurrency()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun refresh() {
        if (::mViewModelFactory.isInitialized) {
            if (mBinding.rvTripsList.adapter != null) {
                mBinding.rvTripsList.gone()
                mBinding.ltLoadingView.visible()
                mBinding.shimmer.visible()
            }
            if (param1 == "upcoming") {
                viewModel.upcomingTripRefresh()
            } else {
                viewModel.tripRefresh()
                mBinding.tvNoReservation.text = getString(R.string.no_previous_trip)
                mBinding.tvStartPlanning.text =
                    getString(R.string.start_planning_your_next_adventure_on_appname)
            }
            viewModel.upcomingTripRefresh()
        }
    }

    override fun onRetry() {
        viewModel.loadTrips("")
        viewModel.loadUpcomingTrips("")
    }

    private fun subscribeToLiveData() {
        viewModel.loadTrips(param1!!).observe(
            viewLifecycleOwner,
            Observer<PagedList<GetAllReservationQuery.Result>> { pagedList ->
                pagedList?.let {
                    if (mBinding.rvTripsList.adapter == pagingController.adapter) {
                        pagingController.submitList(it)
                    } else {
                        mBinding.rvTripsList.adapter = pagingController.adapter
                        pagingController.submitList(it)
                    }
                }
            })

        viewModel.networkState.observe(viewLifecycleOwner, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.EXPIRED -> {
                        if (viewModel.dataManager.isUserLoggedIn()) openSessionExpire("")
                    }

                    NetworkState.SUCCESSNODATA -> {
                        mBinding.ltLoadingView.gone()
                        mBinding.shimmer.gone()
                        mBinding.rvTripsList.gone()
                        mBinding.llNoResult.visible()
                    }

                    NetworkState.LOADING -> {
                        pagingController.isLoading = false
                        mBinding.ltLoadingView.visible()
                        mBinding.shimmer.visible()
                        mBinding.llNoResult.gone()
                        mBinding.rvTripsList.gone()
                    }

                    NetworkState.LOADED -> {
                        mBinding.rvTripsList.visible()
                        mBinding.ltLoadingView.gone()
                        mBinding.shimmer.gone()
                        pagingController.isLoading = false
                        mBinding.llNoResult.gone()
                    }

                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            mBinding.ltLoadingView.visible()
                            mBinding.shimmer.visible()
                            mBinding.rvTripsList.gone()
                            viewModel.handleException(it.msg!!)
                        }
                    }
                }
            }
        })
    }

    private fun subscribeToLiveData1() {
        param1?.let {
            viewModel.loadUpcomingTrips(it).observe(
                viewLifecycleOwner,
                Observer<PagedList<GetAllReservationQuery.Result>> { pagedList ->
                    pagedList?.let {
                        if (isDetached.not()) {
                            if (mBinding.rvTripsList.adapter == pagingController.adapter) {
                                pagingController.submitList(it)
                            } else {
                                mBinding.rvTripsList.adapter = pagingController.adapter
                                pagingController.submitList(it)
                            }
                        }
                    }
                })
        }

        viewModel.upcomingNetworkState.observe(viewLifecycleOwner, Observer {
            it?.let { networkState ->
                when (networkState) {
                    NetworkState.SUCCESSNODATA -> {
                        mBinding.ltLoadingView.gone()
                        mBinding.shimmer.gone()
                        mBinding.rvTripsList.gone()
                        mBinding.llNoResult.visible()
                    }

                    NetworkState.LOADING -> {
                        pagingController.isLoading = false
                        mBinding.llNoResult.gone()
                        mBinding.shimmer.visible()
                        mBinding.rvTripsList.gone()
                        mBinding.ltLoadingView.visible()
                    }

                    NetworkState.LOADED -> {
                        pagingController.isLoading = false
                        mBinding.llNoResult.gone()
                        mBinding.ltLoadingView.gone()
                        mBinding.shimmer.gone()
                        Handler(Looper.getMainLooper()).postDelayed(
                            Runnable {
                                mBinding.rvTripsList.visible()
                            }, 200
                        )
                    }

                    else -> {
                        if (networkState.status == Status.FAILED) {
                            pagingController.isLoading = false
                            mBinding.rvTripsList.gone()
                            mBinding.ltLoadingView.visible()
                            mBinding.shimmer.visible()
                            it.msg?.let { thr ->
                                viewModel.handleException(thr)
                            } ?: viewModel.handleException(Throwable())
                        }
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        mBinding.rvTripsList.adapter = null
        super.onDestroyView()
    }

    fun currencyConverter(
        base: String,
        rate: String,
        userCurrency: String,
        currency: String,
        total: Double
    ): String {
        return BindingAdapters.getCurrencySymbol(userCurrency) + Utils.formatDecimal(
            CurrencyUtil.getRate(
                base = base,
                to = userCurrency,
                from = currency,
                rateStr = rate,
                amount = total
            )
        )
    }

    inner class TripsListController(val base: String, val rate: String, val userCurrency: String) :
        PagedListEpoxyController<GetAllReservationQuery.Result>() {

        var isLoading = false
            set(value) {
                if (value != field) {
                    field = value
                    requestModelBuild()
                }
            }

        override fun buildItemModel(
            currentPosition: Int,
            item: GetAllReservationQuery.Result?
        ): EpoxyModel<*> {

            try {
                var cancelVisibility = false
                var itineraryVisibility = false
                var messageVisibility = false
                var supportVisibility = false
                var receiptVisibility = true

                if (item?.reservationState != null && item.listData != null) {
                    if (item?.reservationState!! == "pending" && item.listData!!
                            .bookingType == "request"
                    ) {
                        cancelVisibility = false
                        itineraryVisibility = false
                    }
                    if (item?.listData != null && (item.reservationState!! == "approved")) {
                        cancelVisibility = true
                        itineraryVisibility = true
                    }
                    if (item.reservationState!! == "declined" || item.reservationState!! == "cancelled") {
                        itineraryVisibility = false
                        cancelVisibility = false
                    }
                }

                var title: String? = null
                var namePrice: String? = null
                var street: String? = null
                var address: String? = null
                var price: String? = null

                if (item != null) {
                    if (item.listData != null) {
                        if (item.listData != null && item.listData!!.title != null) {
                            title = item.listData?.title!!.trim().replace("\\s+", " ")
                        }
                        try {
                            namePrice = item.hostData?.firstName
                            price = currencyConverter(
                                base,
                                rate,
                                userCurrency,
                                item.currency!!,
                                item.total!! + item.guestServiceFee!!
                            )
                        } catch (E: Exception) {
                            E.printStackTrace()
                        }
                        address = item.listData?.street + ", " +
                                item.listData?.city + ", " +
                                item.listData?.state + ", " +
                                item.listData?.country + " " +
                                item.listData?.zipcode
                        messageVisibility = true
                    } else {
                        supportVisibility = true
                        receiptVisibility = false
                        itineraryVisibility = false
                    }
                }

                var date = ""
                try {
                    date = "(" + Utils.epochToDateInbox(
                        item!!.checkIn!!.toLong(),
                        Utils.getCurrentLocale(requireContext())!!
                    ) + " - " + Utils.epochToDateInbox(
                        item.checkOut!!.toLong(),
                        Utils.getCurrentLocale(requireContext())!!
                    ) + ")"
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                var status = ""
                try {
                    status = item!!.reservationState!!.capitalize()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                var phone:Boolean=false

                if(!item?.hostData!!.verifiedPhoneNumber.isNullOrEmpty()){
                        if (status.equals("Completed") || status.equals("Approved")){
                            phone=true
                        }
                }

                return ViewholderTripsListBindingModel_()
                    .id("viewholder- ${item!!.id}")
                    .status(status)
                    .title(title)
                    .date(date)
                    .image(item.hostData!!.picture)
                    .namePrice(namePrice)
                    .price(price)
                    .phoneNumber(item.hostData!!.verifiedPhoneNumber)
                    .phoneNumberVisible(phone)
                    .email(item.hostUser!!.email)
                    .street(street)
                    .address(address)
                    .statusCancelled(supportVisibility)
                    .onMenuClick(View.OnClickListener {
                        initiatePopupWindow(it, item)
                    })
                    .onPhoneNumberClick(View.OnClickListener {
                        val phone = item.hostData!!.verifiedPhoneNumber
                        val intent =
                            Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                        startActivity(intent)
                    })
                    .onEmailClick(View.OnClickListener {
                        val email = item.hostUser!!.email
                        val emailIntent = Intent(
                            Intent.ACTION_SENDTO, Uri.parse(
                                "mailto:$email"
                            )
                        )
                        startActivity(emailIntent)

                    })
                    .onSupportClick(View.OnClickListener {
                        try {
                            ContactSupport.newInstance(item!!.id!!, item.listId!!)
                                .show(childFragmentManager)
                        } catch (e: KotlinNullPointerException) {
                            e.printStackTrace()
                        }
                    })
                    .onClick(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            if (supportVisibility.not()) {
                                try {
                                    val currency =
                                        BindingAdapters.getCurrencySymbol(userCurrency) + CurrencyUtil.getRate(
                                            base = base,
                                            to = userCurrency,
                                            from = item.listData!!.listingData?.currency!!,
                                            rateStr = rate,
                                            amount = item.listData!!.listingData?.basePrice!!
                                                .toDouble()
                                        ).toString()
                                    val photo = ArrayList<String>()
                                    photo.add(item.listData!!.listPhotoName!!)
                                    val listingInitData = ListingInitData(
                                        item.listData!!.title!!,
                                        photo,
                                        item.listData!!.id!!,
                                        item.listData!!.roomType!!,
                                        item.listData!!.reviewsStarRating,
                                        item.listData!!.reviewsCount,
                                        currency,
                                        0,
                                        selectedCurrency = userCurrency,
                                        currencyBase = base,
                                        currencyRate = rate,
                                        startDate = "0",
                                        endDate = "0",
                                        bookingType = item.listData!!.bookingType!!
                                    )
                                    val intent = Intent(context, ListingDetails::class.java)
                                    intent.putExtra("listingInitData", listingInitData)
                                    openListingDetailResultLauncher.launch(intent)
                                } catch (e: KotlinNullPointerException) {
                                    e.printStackTrace()
                                    showToast(resources.getString(R.string.something_went_wrong))
                                }
                            }
                        }
                    })
            } catch (e: Exception) {
                Timber.e(e, "CRASH")
            }
            return ViewholderTripsListBindingModel_()
        }

        override fun addModels(models: List<EpoxyModel<*>>) {
            try {
                super.addModels(models)
            } catch (e: Exception) {
                Timber.e(e, "CRASH")
            }
        }

        init {
            isDebugLoggingEnabled = true
        }

        override fun onExceptionSwallowed(exception: RuntimeException) {
            throw exception
        }

        private fun initiatePopupWindow(
            anchor: View,
            item: GetAllReservationQuery.Result?
        ): PopupWindow? {
            val mInflater =
                baseActivity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = mInflater.inflate(R.layout.trips_more_list, null)

            val mDropdown = PopupWindow(
                layout,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                true
            )
            mDropdown.isClippingEnabled = true
            try {
                var cancelVisibility = false
                var itineraryVisibility = false
                var messageVisibility = false
                var receiptVisibility = true

                if (item?.reservationState != null && item.listData != null) {
                    if (item.reservationState!! == "pending" && item.listData!!
                            .bookingType == "request"
                    ) {
                        cancelVisibility = false
                        itineraryVisibility = false
                    }
                    if (item.listData != null && (item.reservationState!! == "approved")) {
                        cancelVisibility = true
                        itineraryVisibility = true
                    }
                    if (item.reservationState!! == "declined" || item.reservationState!! == "cancelled") {
                        itineraryVisibility = false
                        cancelVisibility = false
                    }
                }

                if (item?.listData != null) {
                    messageVisibility = true
                } else {
                    receiptVisibility = false
                    itineraryVisibility = false
                }

                val message: TextView = layout.findViewById<View>(R.id.message) as TextView
                val itinerary: TextView = layout.findViewById<View>(R.id.itinerary) as TextView
                val receipt: TextView = layout.findViewById<View>(R.id.receipt) as TextView
                val cancel: TextView = layout.findViewById<View>(R.id.cancel) as TextView
                message.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.ic_chat),
                    null,
                    null,
                    null
                )
                itinerary.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.ic_task),
                    null,
                    null,
                    null
                )
                receipt.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.ic_itinerary),
                    null,
                    null,
                    null
                )
                cancel.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.ic_cancel),
                    null,
                    null,
                    null
                )
                cancel.visibility = if (cancelVisibility) View.VISIBLE else View.GONE
                itinerary.visibility = if (itineraryVisibility) View.VISIBLE else View.GONE
                message.visibility = if (messageVisibility) View.VISIBLE else View.GONE
                receipt.visibility = if (receiptVisibility) View.VISIBLE else View.GONE
                message.text = getString(R.string.message)
                itinerary.text = getString(R.string.itinerary)
                receipt.text = getString(R.string.receipt)
                cancel.text = getString(R.string.cancel)

                //If you want to add any listeners to your textviews, these are two //textviews.
                message.setOnClickListener {
                    try {
                        mDropdown.dismiss()
                        if (item!!.messageData?.id != null) {
                            NewInboxMsgActivity.openInboxMsgDetailsActivity(
                                baseActivity!!, InboxMsgInitData(
                                    threadId = item!!.messageData?.id!!,
                                    guestId = item.guestData?.userId!!,
                                    guestName = item.guestData?.firstName!!,
                                    guestPicture = item.guestData?.picture,
                                    hostId = item.hostData?.userId!!,
                                    hostName = item.hostData?.firstName!!,
                                    hostPicture = item.hostData?.picture,
                                    senderID = item?.guestData!!.profileId!!,
                                    receiverID = item?.hostData!!.profileId!!,
                                    listID = item.listId
                                )
                            )
                        } else {

                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        showError()
                    }
                }
                itinerary.setOnClickListener {
                    if (item!!.id != null) {
                        mDropdown.dismiss()
                        val intent = Intent(context, ReservationActivity::class.java)
                        intent.putExtra("type", 1)
                        intent.putExtra("reservationId", item!!.id)
                        intent.putExtra("userType", "Guest")
                        startActivity(intent)
                    } else {
                    }
                }
                receipt.setOnClickListener {
                    try {
                        mDropdown.dismiss()
                        val intent = Intent(context, ReservationActivity::class.java)
                        intent.putExtra("type", 2)
                        intent.putExtra("reservationId", item?.id)
                        intent.putExtra("userType", "Guest")
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                cancel.setOnClickListener {
                    try {
                        mDropdown.dismiss()
                        CancellationActivity.openCancellationActivity(
                            baseActivity!!,
                            item?.id!!,
                            item.hostData!!.profileId!!,
                            "guest",
                            0
                        )
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                    }
                }
                layout.measure(
                    View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED
                )

                mDropdown.showAsDropDown(anchor, -145, 5, Gravity.START)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return mDropdown
        }

    }
}
package com.airhomestays.app.ui.explore.map

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.adapter.ListingAdapter
import com.airhomestays.app.adapter.getCurrencyRateOneTotal
import com.airhomestays.app.databinding.FragmentListingOnMapBinding
import com.airhomestays.app.ui.auth.AuthActivity
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.explore.ExploreFragment
import com.airhomestays.app.ui.explore.ExploreViewModel
import com.airhomestays.app.ui.explore.OneTotalPriceBottomSheet
import com.airhomestays.app.ui.explore.filter.FilterFragment
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.saved.SavedBotomSheet
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.vo.Listing
import com.airhomestays.app.vo.ListingInitData
import com.airhomestays.app.vo.OneTotalPrice
import com.airhomestays.app.vo.SearchListing
import timber.log.Timber
import javax.inject.Inject


private const val ARG_PARAM1 = "param1"
private const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

open class ListingMapFragment : BaseFragment<FragmentListingOnMapBinding, ExploreViewModel>(),
    OnMapReadyCallback {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_on_map
    override val viewModel: ExploreViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(ExploreViewModel::class.java)
    lateinit var mBinding: FragmentListingOnMapBinding
    private lateinit var getMap: GoogleMap
    private var carouselHeight: Int = 0
    var searchListing = ArrayList<SearchListing>()
    var selectedListing: Listing? = null
    private var currentListingPosition: Int = 0
    private var previousMarker: Marker? = null
    private val markerList: ArrayList<Marker> = ArrayList()
    private var scrollPosition: Int? = null
    private var state: Boolean = true
    var iconFactory: IconGenerator? = null

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
            ListingMapFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            scrollPosition = it.getInt(ARG_PARAM1)
        }
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mBinding = viewDataBinding!!
        mBinding.mapView.onCreate(mapViewBundle)
        mBinding.mapView.getMapAsync(this)
        (baseActivity as HomeActivity).hideBottomNavigation()
        iconFactory = IconGenerator(context)
        initView()
        subscribeToLiveData()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        mBinding.rlToolbarNavigateup.onClick {
            baseActivity?.onBackPressedDispatcher?.onBackPressed()
        }

        mBinding.containerSwipeGuesture.setOnTouchListener(object :
            OnSwipeTouchListener(requireContext()) {
            override fun onSwipeTop() {
                revealListing()
            }

            override fun onSwipeBottom() {
                hideListing()
            }
        })

    }


    private fun subscribeToLiveData() {

        viewModel.searchPageResult12.observe(viewLifecycleOwner, Observer {
            it?.let {
                initListing(it)
                searchListing.addAll(it)
                if (::getMap.isInitialized)
                onMapReady(getMap)
            }
        })

        viewModel.filterCount.observe(viewLifecycleOwner, Observer {
            it?.let { filterCount ->
                Log.e("TAG filterCount", filterCount.toString())
                if (filterCount > 0) {
                    mBinding.tvMapBadge.visible()
                } else {
                    mBinding.tvMapBadge.gone()
                }
            }
        })
    }

    private fun initListing(it: ArrayList<SearchListing>) {
        try {
           viewModel.listingList.clear()
            for (i in 0 until it.size) {
                if (viewModel.listingList.size == scrollPosition) {
                    addListing(it[i], true)
                } else {
                    addListing(it[i], false)
                }
            }

            setSimilarListingAdapter()
            mBinding.rvExploreListingMap.layoutManager?.scrollToPosition(scrollPosition!!)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addListing(item: SearchListing, selected: Boolean) {
        try {
            var images = ""
            if (item.coverPhoto != 0) {
                for (i in 0 until item.listPhotos.size) {
                    if (item.coverPhoto == item.listPhotos[i].id) {
                        images = (item.listPhotos[i].name)
                        break
                    }
                }
            } else {
                images = item.listPhotos[0].name
            }

            var currency = viewModel.getCurrencySymbol() + Utils.formatDecimal(
                viewModel.getConvertedRate(
                    item.currency,
                    item.basePrice
                )
            )

            val oneTotal = getCurrencyRateOneTotal(
                item,
                ListingInitData(
                    startDate = viewModel.getStartDate(),
                    endDate = viewModel.getEndDate(),
                    guestCount = viewModel.getPersonCapacity(),
                    selectedCurrency = viewModel.getUserCurrency(),
                    currencyBase = viewModel.getCurrencyBase(),
                    currencyRate = viewModel.getCurrencyRates()

                ),
            )

            var reviewStarCount: Int? = 0
            var reviewsCount = ""
            item.reviewsStarRating?.let {
                reviewStarCount = it
            }
            item.reviewsCount?.let {
                reviewsCount = it.toString()
            }

            viewModel.listingList.add(
                Listing(
                    images,
                    item.roomType!!,
                    item.title,
                    currency,
                    reviewStarCount!!,
                    reviewsCount,
                    "",
                    item.lat,
                    item.lng,
                    selected,
                    beds = item.beds,
                    id = item.id,
                    isWishList = item.wishListStatus!!,
                    bookingType = item.bookingType,
                    isOwnerList = item.isListOwner!!,
                    per_night = " " + resources.getString(R.string.per_night),
                    OneTotalprice = OneTotalPrice(
                        averagePrice = item.oneTotalPrice.averagePrice,
                        nights = item.oneTotalPrice.nights,
                        cleaningPrice = item.oneTotalPrice.cleaningPrice,
                        serviceFee = item.oneTotalPrice.serviceFee,
                        Total = item.oneTotalPrice.Total,
                        Daytotal = item.oneTotalPrice.Daytotal
                    ),
                    oneTotalpricechecked = viewModel.isoneTotalPriceChecked.value ?: false,
                    oneTotalPrice = oneTotal
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val vto = mBinding.rvExploreListingMap.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mBinding.rvExploreListingMap.let {
                    carouselHeight = mBinding.rvExploreListingMap.height
                    if (::getMap.isInitialized && viewModel.listingList.size > 0) {
                        if (carouselHeight > 0) {
                            val mapStyleOptions =
                                MapStyleOptions.loadRawResourceStyle(
                                    requireContext(),
                                    R.raw.map_style
                                )
                            getMap.setMapStyle(mapStyleOptions)
                            getMap.setPadding(0, 0, 0, mBinding.rvExploreListingMap.height)
                            getMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        viewModel.listingList[0].lat,
                                        viewModel.listingList[0].long
                                    ), 15f
                                )
                            )
                            val obs = mBinding.carouselAndCoordinatorContainer.viewTreeObserver
                            obs.removeOnGlobalLayoutListener(this)
                        }
                    }
                }
            }
        })
        mBinding.llMapFilter.onClick {
            ((baseActivity as HomeActivity).pageAdapter.getCurrentFragment() as ExploreFragment).openFragment(
                FilterFragment(),
                "filter"
            )
        }
        mBinding.rvExploreListingMap.smoothScrollToPosition(0)
    }

    private fun setSimilarListingAdapter() {
        viewModel.isGoogleLoaded = true
        val snapHelper = PagerSnapHelper()
        if (mBinding.rvExploreListingMap.onFlingListener == null) {
            snapHelper.attachToRecyclerView(mBinding.rvExploreListingMap)
        }
        try {
            val mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            mLayoutManager.isSmoothScrollbarEnabled = true
            with(mBinding.rvExploreListingMap) {
                layoutManager = mLayoutManager
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                adapter = ListingAdapter(
                    viewModel.listingList,
                    searchListing,
                    ListingInitData(
                        startDate = viewModel.getStartDate(),
                        endDate = viewModel.getEndDate(),
                        guestCount = viewModel.getPersonCapacity(),
                        selectedCurrency = viewModel.getUserCurrency(),
                        currencyBase = viewModel.getCurrencyBase(),
                        currencyRate = viewModel.getCurrencyRates()

                    ),
                    clickListener = { it ->
                        if (viewModel.loginStatus == 0) {
                            openAuthActivity()
                        } else {
                            val bottomSheet = SavedBotomSheet.newInstance(it.id, it.image, false, 0)
                            bottomSheet.show(childFragmentManager, "bottomSheetFragment")
                        }

                    },
                    priceClickListner = { item, _, listinginitdata ->
                        if (viewModel.isoneTotalPriceChecked.value!!) {
                            OneTotalPriceBottomSheet.newInstance( item.oneTotalPrice, listinginitdata, item)
                                .show(childFragmentManager, "onetotalprice")
                        }

                    },
                    isOneTotalpricechecked = viewModel.isoneTotalPriceChecked.value!!
                )
            }

            ((mBinding.rvExploreListingMap.itemAnimator) as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations =
                false

            mBinding.rvExploreListingMap.addOnScrollListener(object :
                androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                var currentState: Int = 0
                override fun onScrollStateChanged(
                    recyclerView: androidx.recyclerview.widget.RecyclerView,
                    newState: Int
                ) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE == newState) {
                        val position =
                            (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                        if (position != -1) {
                            if (currentState != position) {
                                changeMarkerOnScrolledListing(position)
                                for (i in 0 until viewModel.listingList.size) {
                                    viewModel.listingList[i].selected = i == position
                                }
                                recyclerView.adapter?.notifyDataSetChanged()
                            }
                            currentState =
                                (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun changeMarkerOnScrolledListing(position: Int) {
        try {
            makePreviousMarkerWhite()
            currentListingPosition = position
            Timber.d("Old LatLng ${selectedListing!!.lat}, ${selectedListing!!.long}")
            selectedListing = viewModel.listingList[currentListingPosition]
            makeCurrentMarkerGreen(markerList[position])
            previousMarker = markerList[position]
            revealListing()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        getMap = googleMap
        Handler(Looper.getMainLooper()).postDelayed(
            {
                mBinding.ll.gone()
            }, 200
        )
        val iconFactory = IconGenerator(context)
        for (i in 0 until viewModel.listingList.size) {
            if (i == 0) {
                iconFactory.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                iconFactory.setBackground(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_marker_white
                    )
                )
                iconFactory.setTextAppearance(R.style.BaseText)
            } else {
                iconFactory.setColor(ContextCompat.getColor(requireContext(), R.color.white))
                iconFactory.setBackground(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_marker_white
                    )
                )
                iconFactory.setTextAppearance(R.style.BaseText1)
            }
            if (viewModel.isoneTotalPriceChecked.value == true) {
                if (viewModel.listingList[i].oneTotalPrice.length == 2) {
                    addIcon(
                        iconFactory,
                        "  " + viewModel.listingList[i].oneTotalPrice,
                        LatLng(viewModel.listingList[i].lat, viewModel.listingList[i].long),
                        i.toString()
                    )
                } else {
                    addIcon(
                        iconFactory,
                        viewModel.listingList[i].oneTotalPrice,
                        LatLng(viewModel.listingList[i].lat, viewModel.listingList[i].long),
                        i.toString()
                    )
                }
            } else {
                if (viewModel.listingList[i].price.length == 2) {
                    addIcon(
                        iconFactory,
                        "  " + viewModel.listingList[i].price,
                        LatLng(viewModel.listingList[i].lat, viewModel.listingList[i].long),
                        i.toString()
                    )
                } else {
                    addIcon(
                        iconFactory,
                        viewModel.listingList[i].price,
                        LatLng(viewModel.listingList[i].lat, viewModel.listingList[i].long),
                        i.toString()
                    )
                }
            }


        }
        getMap.setOnMapClickListener {
            hideListing()
        }
        getMap.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                hideListing()
            }
        }
        getMap.setOnMarkerClickListener { marker ->
            if (isNetworkConnected) {
                makePreviousMarkerWhite()
                currentListingPosition = marker.tag.toString().toInt()
                selectedListing = viewModel.listingList[currentListingPosition]
                makeCurrentMarkerGreen(marker)
                previousMarker = marker
                revealListing()
            }
            false
        }
        if (viewModel.listingList.size > 0) {
            carouselHeight = mBinding.rvExploreListingMap.height
            if (::getMap.isInitialized && viewModel.listingList.size > 0) {
                if (carouselHeight > 0) {
                    getMap.setPadding(0, 0, 0, mBinding.rvExploreListingMap.height)
                    getMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                viewModel.listingList[0].lat,
                                viewModel.listingList[0].long
                            ), 15f
                        )
                    )
                }
            }
        }
    }

    private fun makePreviousMarkerWhite() {
        val iconFactory = IconGenerator(context)
        iconFactory.setColor(ContextCompat.getColor(requireContext(), R.color.white))
        iconFactory.setTextAppearance(R.style.BaseText1)
        iconFactory.setBackground(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_marker_white
            )
        )
        if (viewModel.isoneTotalPriceChecked.value == true) {
            if (selectedListing?.oneTotalPrice.toString().length == 2) {
                previousMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("  " + selectedListing?.oneTotalPrice)))
            } else {
                previousMarker?.setIcon(
                    BitmapDescriptorFactory.fromBitmap(
                        iconFactory.makeIcon(
                            selectedListing?.oneTotalPrice
                        )
                    )
                )
            }
        } else {
            if (selectedListing?.price.toString().length == 2) {
                previousMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("  " + selectedListing?.price)))
            } else {
                previousMarker?.setIcon(
                    BitmapDescriptorFactory.fromBitmap(
                        iconFactory.makeIcon(
                            selectedListing?.price
                        )
                    )
                )
            }
        }

        previousMarker?.zIndex = 0.0f
    }

    private fun makeCurrentMarkerGreen(marker: Marker) {
        iconFactory?.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        iconFactory?.setBackground(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_marker_blue
            )
        )
        iconFactory?.setTextAppearance(R.style.BaseText)
        if (viewModel.isoneTotalPriceChecked.value == true) {
            if (selectedListing?.oneTotalPrice.toString().length == 2) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory!!.makeIcon("  " + selectedListing?.oneTotalPrice)))
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory!!.makeIcon(selectedListing?.oneTotalPrice)))
            }
        } else {
            if (selectedListing?.price.toString().length == 2) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory!!.makeIcon("  " + selectedListing?.price)))
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory!!.makeIcon(selectedListing?.price)))
            }
        }


        marker.zIndex = 1.0f
    }

    private fun hideListing() {
        state = false
        mBinding.carouselAndCoordinatorContainer.animate()
            .translationY(mBinding.rvExploreListingMap.height.toFloat()).duration = 300
    }

    private fun revealListing() {
        state = true
        try {
            Timber.d("New LatLng ${selectedListing!!.lat}, ${selectedListing!!.long}")
            mBinding.rvExploreListingMap.smoothScrollToPosition(currentListingPosition)
            mBinding.carouselAndCoordinatorContainer.animate()
                .translationY(0F)
                .setDuration(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                    }
                })
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    selectedListing!!.lat,
                    selectedListing!!.long
                ), 15f
            )
            getMap.animateCamera(cameraUpdate)
            Timber.d("New LatLng ${selectedListing!!.lat}, ${selectedListing!!.long}")
        } catch (e: Exception) {
            Timber.d("New LatLng error")
            e.printStackTrace()
        }
    }

    private fun addIcon(
        iconFactory: IconGenerator,
        text: CharSequence,
        position: LatLng,
        tag: String
    ) {
        try {
            val markerOptions = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text)))
                .position(position)
                .anchor(iconFactory.anchorU, iconFactory.anchorV)
            if (tag == "0") {
                previousMarker = getMap.addMarker(markerOptions)
                previousMarker!!.tag = tag
                selectedListing = viewModel.listingList[0]
                makeCurrentMarkerGreen(previousMarker!!)
                markerList.add(previousMarker!!)
            } else {
                val marker = getMap.addMarker(markerOptions)
                marker?.tag = tag
                markerList.add(marker!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mBinding.mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mBinding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mBinding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mBinding.mapView.onStop()
    }

    override fun onPause() {
        mBinding.mapView.onPause()
        super.onPause()
    }


    override fun onDestroy() {
        mBinding.mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mBinding.mapView.onLowMemory()
    }

    override fun onRetry() {

    }

    private fun openAuthActivity() {
        AuthActivity.openActivity(requireActivity(), "Home")
    }
}

open class OnSwipeTouchListener(ctx: Context) : View.OnTouchListener {

    private val gestureDetector: GestureDetector

    companion object {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100
    }

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {


        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1!!.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        result = true
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }


    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}

    open fun onSwipeTop() {}

    open fun onSwipeBottom() {}

}
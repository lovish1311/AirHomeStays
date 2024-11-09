package com.airhomestays.app.ui.explore


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceManager
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.airbnb.epoxy.Carousel
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.airhomestays.app.*
import com.airhomestays.app.databinding.FragmentExplore1Binding
import com.airhomestays.app.ui.auth.AuthActivity
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.explore.filter.FilterFragment
import com.airhomestays.app.ui.explore.map.ListingMapFragment
import com.airhomestays.app.ui.explore.search.SearchLocationFragment
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.ui.saved.SavedBotomSheet
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.util.*
import com.airhomestays.app.util.Utils.Companion.getColor
import com.airhomestays.app.util.Utils.Companion.getCurrentLocale
import com.airhomestays.app.util.epoxy.CustomSpringAnimation
import com.airhomestays.app.util.epoxy.listingPopularCarousel
import com.airhomestays.app.vo.DefaultListing
import com.airhomestays.app.vo.HomeType
import com.airhomestays.app.vo.ListingInitData
import com.airhomestays.app.vo.SearchListing
import com.yongbeom.aircalendar.AirCalendarDatePickerActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round
import java.text.NumberFormat
import java.util.Locale



class ExploreFragment : BaseFragment<FragmentExplore1Binding, ExploreViewModel>(),
    ExploreNavigator {

    private var isFromLocationSearch: Boolean = false

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_explore1
    override val viewModel: ExploreViewModel
        get() = ViewModelProvider(
            baseActivity!!, mViewModelFactory
        ).get(ExploreViewModel::class.java)
    lateinit var mBinding: FragmentExplore1Binding
    private var recommend = ArrayList<DefaultListing>()
    private var mostViewed = ArrayList<DefaultListing>()
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var toLoadInRecommend = false
    private lateinit var snapHelperFactory: Carousel.SnapHelperFactory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this


        initView()
        subscribeToLiveData()

        if (viewModel.filterCount.value == null || viewModel.filterCount.value == 0) {
            if (!(baseActivity as HomeActivity).isReCreated) {
                viewModel.getPopular()
                viewModel.getexploreLists()
            }
        } else {
            mBinding.filterCard.enable()
            searchForListing()
        }
        if (!(baseActivity as HomeActivity).isReCreated) {
            viewModel.getRoomTypes()
        } else {
            if (viewModel.selectedPosition.value != -1) {
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.homeType.elementAt(viewModel.selectedPosition.value!!).selected = true
                    mBinding.tabFl.scrollToPosition(viewModel.selectedPosition.value!!)
                    if (mBinding.tabFl.adapter != null) {
                        mBinding.tabFl.requestModelBuild()
                    }
                }, 1000)
            }
        }
        if ((baseActivity as HomeActivity).isReCreated) viewModel.currentFragment = ""

    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initView() {
        mBinding.headerTitle.visible()
        mBinding.tabFl.gone()
        mBinding.exploreLl.elevation = 0f
        mBinding.exploreLl.setBackgroundColor(getColor(requireContext(), R.color.explore_header_bg))
        mBinding.shimmerFrameLayout.startShimmer()
        mBinding.shimmerRecommended.startShimmer()
        mBinding.shimmerBanner.startShimmer()
        mBinding.shimmerMost.startShimmer()
        mBinding.filterCard.disable()
        CustomSpringAnimation.spring(mBinding.rvExploreEpoxy)
        val currentLocale: Locale = Utils.getCurrentLocale(requireContext())!!
        mBinding.locale = currentLocale
        snapHelperFactory = object : Carousel.SnapHelperFactory() {
            override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                return CustomSnapHelper()
            }
        }
        onClickListeners()

    }


    private fun onClickListeners() {
        mBinding.searchIconIv.onClick {
            if (viewModel.searchResult.value == true) {
                reset()
            } else {
                (baseActivity as HomeActivity).hideBottomNavigation()
                openFragment(SearchLocationFragment(), "search")
            }

        }
        mBinding.searchTv.onClick {
            (baseActivity as HomeActivity).hideBottomNavigation()
            openFragment(SearchLocationFragment(), "search")
        }

        mBinding.ibExploreLocation.onClick {
            viewModel.searchPageResult12.value?.let {
                if (it.isNotEmpty()) {
                    val scrollPosition = 0
                    Log.d("check","4")
                    openFragment(ListingMapFragment.newInstance(scrollPosition), "map")
                }
            }
        }
        mBinding.filterCard.onClick {
            (baseActivity as HomeActivity).hideBottomNavigation()
            openFragment(FilterFragment(), "filter")
        }

    }

    override fun disableIcons() {
        with(mBinding) {
            ibExploreLocation.enable()
        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        viewModel.currentFragment = tag
        childFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down
        ).add(mBinding.flExploreFragment.id, fragment, tag).addToBackStack(null).commit()
    }

    private fun checkLocationForSearch(location: String) {
        isFromLocationSearch = true
        viewModel.location.value = location
        mBinding.headerTitle.gone()
        mBinding.tabFl.visible()
        mBinding.exploreLl.elevation = 4f
        mBinding.exploreLl.setBackgroundColor(
            getColor(
                requireContext(), R.color.search_location_header_bg
            )
        )

    }

    private fun setUp() {
        mBinding.rvExploreEpoxy.withModels {
            mBinding.filterCard.enable()
            if (viewModel.popularLocations?.isNotEmpty() == true) {
                viewholderListingHeader {
                    id(23333)
                    text(getString(R.string.popular_location))
                    locale(getCurrentLocale(requireContext()))
                }

                listingPopularCarousel {
                    id("popular")
                    padding(Carousel.Padding.dp(10, 20, 17, 10, 15))
                    Carousel.setDefaultGlobalSnapHelperFactory(snapHelperFactory)
                    models(mutableListOf<ViewholderPopularLocationItemBindingModel_>().apply {
                        viewModel.popularLocations?.forEach { poploc ->
                            add(
                                ViewholderPopularLocationItemBindingModel_().id(poploc?.id)
                                    .imgURL(poploc?.image).title(poploc?.location)
                                    .onLocationClick(View.OnClickListener {
                                        poploc?.locationAddress
                                            ?.let { it1 -> checkLocationForSearch(it1) }
                                    })
                            )
                        }
                    })
                }
            }
            if (recommend.size > 0 || mostViewed.size > 0) {
                if (recommend.size > 0) {
                    mBinding.headerTitle.visible()
                    mBinding.tabFl.gone()
                    mBinding.exploreLl.elevation = 0f
                    mBinding.exploreLl.setBackgroundColor(
                        getColor(
                            requireContext(), R.color.explore_header_bg
                        )
                    )

                    viewholderListingHeader {
                        id(23)
                        text(getString(R.string.recommed_viewed))
                        locale(getCurrentLocale(requireContext()))
                    }
                    listingSimilarCarousel {
                        id("carousel")
                        padding(Carousel.Padding.dp(10, 20, 17, 10, 15))
                        Carousel.setDefaultGlobalSnapHelperFactory(snapHelperFactory)
                        models(mutableListOf<ViewholderListingBindingModel_>().apply {
                            recommend.forEachIndexed { index, item ->
                                val baseGuestValue = item.personCapacity
                                val sleeps = resources.getString(R.string.total_guest) + "- $baseGuestValue"
                                var bedrooms = item.bedrooms
                                bedrooms = if (bedrooms.toInt() > 1) "$bedrooms bedrooms" else "$bedrooms bedroom"
                                val guestBasePrice = item.guestBasePrice
                                val additionalPrice = item.additionalPrice
                                var maxGuest = 0;
                                if(guestBasePrice>0){
                                    maxGuest = guestBasePrice;
                                }else{
                                    maxGuest = baseGuestValue;
                                }
                                val additionalGuest = baseGuestValue - maxGuest;
                                var additionalString = "";
                                var visibility = View.GONE
                                if (additionalPrice != null) {
                                    if(additionalGuest > 0 && additionalPrice > 0){
                                        val additionalPriceInt = additionalPrice.toInt();
                                        visibility = View.VISIBLE
                                        var guestString = "";
                                        if(additionalGuest > 1) guestString = "guests"
                                        else guestString = "guest"
                                        additionalString = "$additionalGuest additional $guestString @$additionalPriceInt/person"
                                    }
                                }


                                val priceValue = viewModel.getConvertedRate(item.currency, item.basePrice)
                                val formattedPrice = NumberFormat.getInstance(Locale.getDefault()).format(priceValue)
                                val currency = viewModel.getCurrencySymbol() + formattedPrice + "";

                                val maxGuestString = if (maxGuest > 1) "for $maxGuest guests" else "for $maxGuest guest"

                                var ratingCount = ""
                                if (item.reviewsStarRating != null && item.reviewsStarRating != 0 && item.reviewsCount != null && item.reviewsCount != 0) {
                                    ratingCount =
                                        round(item.reviewsStarRating.toDouble() / item.reviewsCount.toDouble()).toInt()
                                            .toString()
                                    Timber.d("ratingCount ${round(item.reviewsStarRating.toDouble() / item.reviewsCount.toDouble())}")
                                } else {
                                    ratingCount = ""
                                }
                                var bedsCount = ""
                                bedsCount = if (item.beds > 0) {
                                    val stringBeds = item.beds.toDouble().toString().split(".")
                                    if (stringBeds[1] == "0") {
                                        "${stringBeds[0]}${
                                            resources.getQuantityString(
                                                R.plurals.caps_bed_count, item.beds
                                            )
                                        }"
                                    } else {
                                        "${item.beds}${
                                            resources.getQuantityString(
                                                R.plurals.caps_bed_count, item.beds
                                            )
                                        }"
                                    }
                                } else {
                                    ""
                                }

                                add(ViewholderListingBindingModel_().id("mostViewed - ${item.id}")
                                    .title(item.title.trim().replace("\\s+", " "))
                                    .roomType(item.roomType).image(item.listPhotoName)
                                    .bookingType(item.bookingType)
                                    .locale(getCurrentLocale(requireContext()))
                                    .reviewsCount(item.reviewsCount).ratingsCount(ratingCount)
                                    .bedsCount(bedsCount).reviewsStarRating(item.reviewsStarRating)
                                    .currency(currency).maxGuestString(maxGuestString)
                                    .bedroomsRecommend(bedrooms).sleepsRecommend(sleeps)
                                    .additionalGuestRecommend(additionalString).visibilityRecommend(visibility)
                                    .wishListStatus(item.wishListStatus)
                                    .isOwnerList(item.isListOwner)
                                    .heartClickListener(View.OnClickListener {
                                        Utils.clickWithDebounce(it) {
                                            if (viewModel.loginStatus == 0) openAuthActivity()
                                            else {
                                                val bottomSheet = SavedBotomSheet.newInstance(
                                                    item.id,
                                                    item.listPhotoName!!,
                                                    false,
                                                    item.wishListGroupCount
                                                )
                                                bottomSheet.show(
                                                    childFragmentManager, "bottomSheetFragment"
                                                )
                                            }
                                        }

                                    }).clickListener(View.OnClickListener {
                                        Utils.clickWithDebounce(it) {
                                            val photo = ArrayList<String>()
                                            photo.add(item.listPhotoName!!)
                                            ListingDetails.openListDetailsActivity(
                                                it.findViewById(R.id.iv_item_listing_similar_image),
                                                requireActivity(),
                                                ListingInitData(
                                                    item.title,
                                                    photo,
                                                    item.id,
                                                    item.roomType!!,
                                                    item.reviewsStarRating,
                                                    item.reviewsCount,
                                                    currency,
                                                    0,
                                                    selectedCurrency = viewModel.getUserCurrency(),
                                                    currencyBase = viewModel.getCurrencyBase(),
                                                    currencyRate = viewModel.getCurrencyRates(),
                                                    startDate = viewModel.getStartDate(),
                                                    endDate = viewModel.getEndDate(),
                                                    bookingType = recommend.get(index).bookingType,
                                                    minGuestCount = viewModel.getMinGuestCount(),
                                                    maxGuestCount = viewModel.getMaxGuestCount(),
                                                    isWishList = item.wishListStatus!!
                                                )
                                            )
                                        }
                                    })
                                )
                            }
                        })
                    }
                }
                viewholderBecomeAHostBanner {
                    id("hostBanner")
                    imgUrl(
                        Constants.banner + viewModel.exploreLists1.value?.getImageBanner?.result
                            ?.image
                    )
                    val isRTL =
                        TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL
                    onBind { _, view, _ ->
                        val imgView = view.dataBinding.root.findViewById<TextView>(R.id.txt_hosting)
                        if (isRTL) imgView.textDirection = 4
                    }
                    val spString = SpannableString(
                        viewModel.exploreLists1.value?.getImageBanner?.result
                            ?.title + " " + viewModel.exploreLists1.value?.getImageBanner?.result
                            ?.description
                    )
                    spString.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                view!!.context, R.color.colorAccent
                            )
                        ),
                        0,
                        viewModel.exploreLists1.value?.getImageBanner?.result?.title!!.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    hostingPercentage(spString)
                    buttonText(viewModel.exploreLists1.value?.getImageBanner?.result?.buttonLabel)
                    onClick(View.OnClickListener {
                        Utils.clickWithDebounce(it) {
                            if (viewModel.loginStatus == 0) openAuthActivity()
                            else {
                                if (viewModel.dataManager.isHostOrGuest) {
                                    viewModel.dataManager.isHostOrGuest = false
                                    navigateToSplash()
                                } else {
                                    viewModel.dataManager.isHostOrGuest = true
                                    navigateToSplash()
                                }
                            }
                        }
                    })
                }


                if (mostViewed.size > 0) {
                    viewholderListingHeader {
                        id(231)
                        text(getString(R.string.most_viewed))
                        locale(getCurrentLocale(requireContext()))
                    }
                    mostViewed.forEachIndexed { index, item ->
                        val baseGuestValue = item.personCapacity;
                        val guestBasePrice = item.guestBasePrice
                        val additionalPrice = item.additionalPrice
                        var maxGuest = 0;
                        if(guestBasePrice>0){
                            maxGuest = guestBasePrice;
                        }else{
                            maxGuest = baseGuestValue;
                        }
                        val additionalGuest = baseGuestValue - maxGuest;
                        val sleeps = "Sleeps - $baseGuestValue"
                        val priceValue = viewModel.getConvertedRate(item.currency, item.basePrice)
                        val formattedPrice = NumberFormat.getInstance(Locale.getDefault()).format(priceValue)
                        val currency = viewModel.getCurrencySymbol() + formattedPrice + "";

                        val maxGuestString = if (maxGuest > 1) "for $maxGuest guests" else "for $maxGuest guest"

                        var ratingCount = ""
                        if (item.reviewsStarRating != null && item.reviewsStarRating != 0 && item.reviewsCount != null && item.reviewsCount != 0) {
                            ratingCount =
                                round(item.reviewsStarRating.toDouble() / item.reviewsCount.toDouble()).toInt()
                                    .toString()
                            Timber.d("ratingCount ${round(item.reviewsStarRating.toDouble() / item.reviewsCount.toDouble())}")
                        } else {
                            ratingCount = ""
                        }

                        var bedsCount = ""
                        bedsCount = if (item.beds > 0) {
                            val stringBeds = item.beds.toDouble().toString().split(".")
                            if (stringBeds[1] == "0") {
                                "${stringBeds[0]}${
                                    resources.getQuantityString(
                                        R.plurals.caps_bed_count, item.beds
                                    )
                                }"
                            } else {
                                "${item.beds}${
                                    resources.getQuantityString(
                                        R.plurals.caps_bed_count, item.beds
                                    )
                                }"
                            }
                        } else {
                            ""
                        }
                        var bedrooms = item.bedrooms
                        bedrooms = if (bedrooms.toInt() > 1) "$bedrooms bedrooms" else "$bedrooms bedroom"

                        var additionalString = "";
                        var visibility = View.GONE
                        if (additionalPrice != null) {
                            if(additionalGuest > 0 && additionalPrice > 0){
                                val additionalPriceInt = additionalPrice.toInt();
                                visibility = View.VISIBLE
                                var guestString = "";
                                if(additionalGuest > 1) guestString = "guests"
                                else guestString = "guest"
                                additionalString = "$additionalGuest additional $guestString @$additionalPriceInt/person"
                            }
                        }

                        add(ViewholderSavedListingBindingModel_().id("recommend - ${item.id}")
                            .title(item.title.trim().replace("\\s+", " ")).roomType(item.roomType)
                            .url(item.listPhotoName).bookType(mostViewed.get(index).bookingType)
                            .reviewsCount(item.reviewsCount)
                            .bedsCount(bedsCount)
                            .reviewsStarRating(item.reviewsStarRating).price(currency)
                            .maxGuestStringMostViewed(maxGuestString)
                            .bedroomsMostViewed(bedrooms).sleepsMostViewed(sleeps)
                            .additionalGuestMostViewed(additionalString).visibilityMostViewed(visibility)
                            .ratingsCount(ratingCount).wishListStatus(item.wishListStatus)
                            .isOwnerList(item.isListOwner).heartClickListener(View.OnClickListener {
                                Utils.clickWithDebounce(it) {
                                    if (viewModel.loginStatus == 0) openAuthActivity()
                                    else {
                                        val bottomSheet = SavedBotomSheet.newInstance(
                                            item.id,
                                            item.listPhotoName!!,
                                            false,
                                            item.wishListGroupCount
                                        )
                                        bottomSheet.show(
                                            childFragmentManager, "bottomSheetFragment"
                                        )
                                    }
                                }
                            }).onClick(View.OnClickListener {
                                Utils.clickWithDebounce(it) {
                                    val photo = ArrayList<String>()
                                    photo.add(item.listPhotoName!!)
                                    ListingDetails.openListDetailsActivity(
                                        it.findViewById(R.id.iv_item_listing_saved_image),
                                        requireActivity(),
                                        ListingInitData(
                                            item.title,
                                            photo,
                                            item.id,
                                            item.roomType!!,
                                            item.reviewsStarRating,
                                            item.reviewsCount,
                                            currency,
                                            0,
                                            selectedCurrency = viewModel.getUserCurrency(),
                                            currencyBase = viewModel.getCurrencyBase(),
                                            currencyRate = viewModel.getCurrencyRates(),
                                            startDate = viewModel.getStartDate(),
                                            endDate = viewModel.getEndDate(),
                                            bookingType = item.bookingType,
                                            minGuestCount = viewModel.getMinGuestCount(),
                                            maxGuestCount = viewModel.getMaxGuestCount(),
                                            isWishList = item.wishListStatus!!
                                        )
                                    )
                                }
                            })
                        )
                    }
                }
            } else {
                viewholderExploreNoResult {
                    id(123)
                }
            }
        }


    }

    private fun navigateToSplash() {
        LoginManager.getInstance().logOut()
        mGoogleSignInClient?.signOut()
        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtra("isHostGuest", 1)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        baseActivity?.finish()
    }

    class CenterLayoutManager : LinearLayoutManager {
        constructor(context: Context) : super(context)
        constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(
            context,
            orientation,
            reverseLayout
        )

        constructor(
            context: Context,
            attrs: AttributeSet,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes)

        override fun smoothScrollToPosition(
            recyclerView: RecyclerView,
            state: RecyclerView.State,
            position: Int
        ) {
            val centerSmoothScroller = CenterSmoothScroller(recyclerView.context)
            centerSmoothScroller.targetPosition = position
            startSmoothScroll(centerSmoothScroller)

        }


        private inner class CenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {
            override fun calculateDtToFit(
                viewStart: Int,
                viewEnd: Int,
                boxStart: Int,
                boxEnd: Int,
                snapPreference: Int
            ): Int = (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
        }
    }


    private fun subscribeToLiveData() {
        viewModel.personCapacity.observe(viewLifecycleOwner, Observer {
            it?.let { personCapacity ->
                if (personCapacity.isNotEmpty() && personCapacity.toInt() > 0) {
                    val text: String = if (personCapacity.toInt() == 1) {
                        "$personCapacity ${resources.getString(R.string.guest_small)}"
                    } else {
                        "$personCapacity ${resources.getString(R.string.guests_small)}"
                    }
                }
            }
        })

        viewModel.isReset.observe(viewLifecycleOwner, Observer {
            if (mBinding.tabFl.adapter != null && viewModel.homeType.isNotEmpty() && viewModel.getRoomTypes.value!!.isNotEmpty()) {
                viewModel.homeType.forEach { it.selected = false }
                mBinding.tabFl.layoutManager!!.scrollToPosition(0)
                mBinding.tabFl.requestModelBuild()
            }
        })

        viewModel.getRoomTypes.observe(viewLifecycleOwner, Observer {
            viewModel.homeType.clear()
            it?.forEach {
                viewModel.homeType.add(HomeType(it?.id!!, it.itemName!!, it.image, false))
            }
            mBinding.tabFl.layoutManager =
                CenterLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            mBinding.tabFl.withModels {
                viewModel.homeType.forEachIndexed { index, item ->
                    viewholderItemTab {
                        id(item.id)
                        title(item.itemName)
                        if (item.image != null && item.image != "") {
                            amenitiesImage(Constants.amenities + item.image)
                        } else {
                            amenitiesImage("")
                        }
                        isIndicatorVisible(item.selected)
                        onClick(View.OnClickListener {
                            viewModel.homeType.forEach { otherItem ->
                                otherItem.selected = otherItem.id == item.id
                                if (otherItem.selected) {
                                    viewModel.roomType = HashSet()
                                    viewModel.roomType.add(item.id)
                                    searchForListing()
                                }
                                viewModel.selectedPosition.value =
                                    viewModel.homeType.indexOfFirst { it.selected }
                                if (viewModel.selectedPosition.value != -1) {
                                    mBinding.tabFl.smoothScrollToPosition(viewModel.selectedPosition.value!!)
                                }
                            }
                            this@withModels.requestModelBuild()
                        })
                    }
                }
            }
        })

        viewModel.startDate.observeForever {
            if (it.isNullOrEmpty().not()) {
                if (it.equals("0")) {
                    viewModel.isoneTotalPriceChecked.value = false
                }
            } else {
                viewModel.isoneTotalPriceChecked.value = false
            }
        }

        viewModel.filterCount.observe(viewLifecycleOwner, Observer {
            it?.let { filterCount ->
                if (filterCount >= 1) {
                    mBinding.searchIconIv.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.ic_left_round
                        )
                    )
                    mBinding.cardSearch.elevation = 4f
                    mBinding.filterCard.elevation = 4f
                    mBinding.filterIconIv.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_filter_white
                        )
                    )
                    mBinding.cardSearch.setBackgroundResource(R.drawable.bg_corner_stroke)
                    Handler(Looper.getMainLooper()).postDelayed({
                        mBinding.filterCard.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.bg_corner_green)
                        mBinding.filterIconIv.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.bg_corner_green)
                    },700)
                } else {
                    mBinding.searchIconIv.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.ic_search_round
                        )
                    )
                    mBinding.filterIconIv.background = resources.getDrawable(R.drawable.bg_corner)
                    mBinding.filterIconIv.setImageDrawable(resources.getDrawable(R.drawable.ic_filter))
                    mBinding.filterCard.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_corner)
                    mBinding.cardSearch.elevation = 4f
                    mBinding.filterCard.elevation = 4f
                    viewModel.startDate.value = "0"
                    viewModel.endDate.value = "0"
                    viewModel.TempstartDateFromResult = ""
                    viewModel.TempendDateFromResult = ""
                }
            }
        })
        viewModel.location.observe(viewLifecycleOwner, Observer {
            it?.let { currentLocation ->
                if (currentLocation.isNotEmpty()) {
                    with(mBinding.searchTv) {
                        text = it
                    }
                    mBinding.searchIconIv.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.ic_left_round
                        )
                    )

                    mBinding.filterCard.enable()
                    mBinding.cardSearch.setBackgroundResource(R.drawable.bg_corner_stroke)
                    if (viewModel.filterCount.value != null && viewModel.filterCount.value!! < 1) mBinding.filterCard.setBackgroundResource(
                        R.drawable.bg_corner_stroke
                    )
                    mBinding.cardSearch.elevation = 4f
                    mBinding.filterCard.elevation = 4f
                    if (isFromLocationSearch.not()) {
                        baseActivity?.onBackPressed()
                    }
                    if (viewModel.personCapacity1.get() != null && viewModel.personCapacity1.get()!!
                            .toInt() > 0
                    ) {
                        viewModel.personCapacity.value = viewModel.personCapacity1.get()
                    }
                    viewModel.startSearching()
                    isFromLocationSearch = false
                    mBinding.headerTitle.visibility = View.VISIBLE
                    mBinding.tabFl.gone()
                    mBinding.exploreLl.elevation = 0f
                    mBinding.exploreLl.setBackgroundColor(
                        getColor(
                            requireContext()!!, R.color.explore_header_bg
                        )
                    )
                } else {
                    with(mBinding.searchTv) {
                        text = resources.getString(R.string.search_box)
                    }
                    mBinding.cardSearch.setBackgroundResource(R.drawable.bg_corner)
                    mBinding.cardSearch.elevation = 4f
                    mBinding.filterCard.elevation = 4f
                }
            }
        })

        viewModel.defaultListingData.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (viewModel.searchResult.value!!.not()) {
                    val recommend1 = it["recommend"]
                    if (recommend1!!.isNotEmpty()) {
                        recommend = recommend1
                    }
                    val mostViewed1 = it["mostViewed"]
                    if (mostViewed1!!.isNotEmpty()) {
                        mostViewed = mostViewed1
                    }
                    mBinding.rvExploreEpoxySearch.gone()
                    mBinding.rvExploreEpoxy.visible()
                    mBinding.flSearchLoading.gone()
                    mBinding.rvShimmerExploreContainer.gone()
                    mBinding.shimmerSearch.gone()
                    mBinding.rvExploreEpoxySearch.adapter = null
                    if (viewModel.getSearchResult().not()) {
                        if (mBinding.rvExploreEpoxy.adapter != null) {
                            mBinding.rvExploreEpoxy.requestModelBuild()

                        } else {
                            setUp()


                        }
                    }
                    enableIcons()
                    mBinding.rvExploreEpoxySearch.clearOnScrollListeners()
                    if (::disposable.isInitialized) {
                        compositeDisposable.remove(disposable)
                    }
                }
            }
        })

        viewModel.searchPageResult12.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isNotEmpty() && viewModel.searchResult.value!!) {
                    mBinding.rvExploreEpoxySearch.visible()
                    mBinding.tabFl.visible()
                    mBinding.exploreLl.elevation = 4f
                    mBinding.headerTitle.gone()
                    if (viewModel.filterCount.value!! <= 0){
                        mBinding.filterCard.setBackgroundResource(
                            R.drawable.bg_corner_stroke
                        )
                    }
                    mBinding.exploreLl.setBackgroundColor(
                        getColor(
                            context!!, R.color.search_location_header_bg
                        )
                    )
                    viewModel.pagingController1.list = it
                    viewModel.pagingController1.requestModelBuild()
                    mBinding.flSearchLoading.gone()
                    mBinding.rvShimmerExploreContainer.gone()
                    mBinding.shimmerSearch.gone()
                    mBinding.rvExploreEpoxy.gone()
                    mBinding.ibExploreLocation.visible()
                }
            }
        })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("selectedPosition", viewModel.selectedPosition.value ?: -1)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            viewModel.selectedPosition.value = it.getInt("selectedPosition", -1)
        }
    }

    override fun searchForListing() {
        pageNumber = 1
        isLoadedAll = false
        lastVisibleItem = 0
        totalItemCount = 0
        mBinding.rvShimmerExploreContainer.gone()
        mBinding.searchIconIv.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_left_round
            )
        )
        mBinding.shimmerSearch.startShimmer()
        mBinding.shimmerSearch.visible()
        viewModel.searchPageResult12.value = ArrayList()
        initController1()
        setUpLoadMoreListener()
        subscribeForData()
    }

    private fun openListingDetail(
        view: View, item: SearchListing, listingInitData: ListingInitData
    ) {
        try {
            listingInitData.photo.clear()
            listingInitData.title = item.title
            listingInitData.photo.add(item.listPhotoName!!)
            listingInitData.id = item.id
            listingInitData.roomType = item.roomType!!
            listingInitData.ratingStarCount = item.reviewsStarRating
            listingInitData.reviewCount = item.reviewsCount
            listingInitData.bookingType = item.bookingType
            listingInitData.isWishList = item.wishListStatus!!
            ListingDetails.openListDetailsActivity(requireContext(), listingInitData)
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            showError()
        }
    }

    override fun enableIcons() {
        with(mBinding) {

            ibExploreLocation.enable()
            searchTv.enable()
            searchIconIv.enable()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 4) {
            if (data != null) {
                val startDateFromResult =
                    data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_START_DATE)
                        .orEmpty()
                val endDateFromResult =
                    data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_END_DATE)
                        .orEmpty()
                if (startDateFromResult.isNotEmpty() && endDateFromResult.isNotEmpty()) {
                    setDateInCalendar(
                        data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_START_DATE)
                            .orEmpty(),
                        data.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_END_DATE)
                            .orEmpty()
                    )
                } else {
                    resetDate()
                    viewModel.getSearchListing()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDateInCalendar(selStartDate: String, selEndDate: String) {
        try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val langType = preferences.getString("Locale.Helper.Selected.Language", "en")
            viewModel.startDate.value = selStartDate
            viewModel.endDate.value = selEndDate
            if (viewModel.personCapacity1.get() != null && viewModel.personCapacity1.get()!!
                    .toInt() > 0
            ) {
                viewModel.personCapacity.value = viewModel.personCapacity1.get()
            }
            viewModel.startSearching()
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    private fun resetDate() {

        viewModel.startDate.value = "0"
        viewModel.endDate.value = "0"
    }


    fun onBackPressed() {
        mBinding.rvExploreEpoxySearch.clear()
        hideSnackbar()
        if (viewModel.getSearchResult()) {
            mBinding.headerTitle.visible()
            mBinding.tabFl.gone()
            mBinding.exploreLl.elevation = 0f
            mBinding.exploreLl.setBackgroundColor(getColor(context!!, R.color.explore_header_bg))
            mBinding.filterCard.visible()
            reset()
            if (toLoadInRecommend) {
                viewModel.getexploreLists()
                toLoadInRecommend = false
            }
        } else {
            baseActivity?.finish()
        }
    }

    fun resetFilterCount() {
        viewModel.filterCount.value = 0
        viewModel.Tempcount = 0
        viewModel.startDate.value = "0"
        viewModel.endDate.value = "0"
        viewModel.dateStart = ""
        viewModel.dateEnd = ""
        viewModel.TempinitialHomeTypeSize = 2
        viewModel.TempinitialAmenitiesSize = 2
        viewModel.TempinitialHouseRulesSize = 2
        viewModel.TempinitialFacilitiesSize = 2
    }

    fun resetTempcount() {
        viewModel.Tempcount = 0
    }

    fun exploreheader(): Boolean {
        return mBinding.headerTitle.isVisible
    }

    fun reset() {
        mBinding.rvExploreEpoxySearch.clearOnScrollListeners()
        mBinding.rvExploreEpoxySearch.clear()
        viewModel.clearSearchRequest()
        resetDate()
        mBinding.headerTitle.visible()
        mBinding.tabFl.gone()
        mBinding.exploreLl.elevation = 0f
        mBinding.exploreLl.setBackgroundColor(getColor(context!!, R.color.explore_header_bg))
        mBinding.filterCard.visible()
        mBinding.ibExploreLocation.gone()
        mBinding.llNoResult.gone()
        mBinding.rvExploreEpoxy.visible()
        viewModel.isoneTotalPriceChecked.value = false
        viewModel.increaseCurrentPage(1)
        isLoadedAll = false
        viewModel.searchPageResult12.value = ArrayList()
        pageNumber = 1
        viewModel.getexploreLists()
    }

    private fun setBgnText(view: TextView, text: String, status: Boolean) {
        view.text = text
        if (status) {
            view.background = ContextCompat.getDrawable(view.context, R.drawable.date_bg)
            view.setTextColor(ContextCompat.getColor(view.context, R.color.black))
        } else {
            view.background = ContextCompat.getDrawable(view.context, R.drawable.curve_button_blue)
            view.setTextColor(ContextCompat.getColor(view.context, R.color.white))
        }
    }

    override fun onDestroyView() {
        mBinding.rvExploreEpoxy.adapter = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRetry() {
        if (viewModel.getSearchResult()) {
            onRefresh()
        } else {
            viewModel.getexploreLists()
        }
    }


    fun onRefresh() {
        if (viewModel.getSearchResult()) {
            mBinding.ibExploreLocation.gone()
            mBinding.rvExploreEpoxy.gone()
            mBinding.searchLoading.visible()
            viewModel.isRefreshing = true
            searchForListing()
        } else {
            viewModel.getexploreLists()
        }
    }

    fun onRefreshOnWishList(value: Int?, flag: Boolean, count: Int) {
        if (viewModel.getSearchResult()) {
            viewModel.refreshOnWishList(true)
            viewModel.changeWishListStatusInSearch(value, flag, count)
        } else {
            viewModel.changeWishListStatus(value, flag, count)
        }
    }


    private val compositeDisposable = CompositeDisposable()
    private val paginator = PublishProcessor.create<Int>()
    private var progressBar: ProgressBar? = null
    private var loading = false
    private var pageNumber = 1
    private val VISIBLE_THRESHOLD = 1
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var isLoadedAll = false
    private lateinit var disposable: Disposable

    private fun initController1() {
        viewModel.pagingController1 = SearchListingController1(ListingInitData(
            selectedCurrency = viewModel.getUserCurrency(),
            currencyBase = viewModel.getCurrencyBase(),
            currencyRate = viewModel.getCurrencyRates(),
            guestCount = viewModel.getPersonCapacity(),
            minGuestCount = viewModel.getMinGuestCount(),
            maxGuestCount = viewModel.getMaxGuestCount()
        ),
            clickListener = { it, item, listingInitData, view ->
                Utils.clickWithDebounce(view) {
                    listingInitData.startDate = viewModel.getStartDate()
                    listingInitData.endDate = viewModel.getEndDate()
                    openListingDetail(view, item, listingInitData)
                }
            },
            retryListener = {
                viewModel.repoRetry()
            },
            onWishListClick = { item, _ ->
                try {
                    if (viewModel.loginStatus == 0) {
                        openAuthActivity()
                    } else {
                        val bottomSheet = SavedBotomSheet.newInstance(
                            item.id, item.listPhotoName!!, false, item.wishListGroupCount
                        )
                        bottomSheet.show(childFragmentManager, "bottomSheetFragment")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError()
                }
            },
            startDate = viewModel.startDate.value!!,
            isoneTotalpriceChecked = viewModel.isoneTotalPriceChecked.value!!,
            oneTotalPriceClicked = {
                viewModel.isoneTotalPriceChecked.value =
                    !viewModel.isoneTotalPriceChecked.value!!
            },
            priceClickListener = { item, view, listinginitdata ->
                if (viewModel.isoneTotalPriceChecked.value!!) {
                    Utils.clickWithDebounce(view) {
                        OneTotalPriceBottomSheet.newInstance(
                            item.oneTotalPrice, listinginitdata, item
                        ).show(childFragmentManager, "onetotalprice")
                    }

                }

            }, viewModel = viewModel
        )
        val layoutManager = LinearLayoutManager(activity)
        mBinding.rvExploreEpoxySearch.layoutManager = layoutManager
        mBinding.rvExploreEpoxySearch.setController(viewModel.pagingController1)

        viewModel.pagingController1.adapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    layoutManager.scrollToPosition(0)
                }
            }
        })
    }

    private fun setUpLoadMoreListener() {
        mBinding.rvExploreEpoxySearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView, dx: Int, dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = mBinding.rvExploreEpoxySearch.layoutManager!!.itemCount
                lastVisibleItem =
                    (mBinding.rvExploreEpoxySearch.layoutManager!! as LinearLayoutManager).findLastVisibleItemPosition()
                if (!isLoadedAll && !loading && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD) {
                    pageNumber++
                    paginator.onNext(pageNumber)
                    loading = true
                }
            }
        })
    }

    private fun subscribeForData() {
        if (::disposable.isInitialized) {
            compositeDisposable.remove(disposable)
        }
        disposable = paginator.onBackpressureDrop().doOnNext { page ->
            loading = true
            if (page == 1) {
                mBinding.flSearchLoading.visible()
                mBinding.llNoResult.gone()
            }
            progressBar?.visibility = View.VISIBLE
            viewModel.increaseCurrentPage(page)
            viewModel.pagingController1.isLoading = true
        }.concatMapSingle { page ->
            viewModel.getSearchListing1()
                .subscribeOn(Schedulers.io()).doOnError { throwable ->
                    throwable.printStackTrace()
                }
        }.observeOn(AndroidSchedulers.mainThread()).subscribe({ items ->
            if (viewModel.searchResult.value!!) {
                if (items!!.data!!.searchListing!!.status == 200) {
                    if (items.data!!.searchListing!!.results!!.isNotEmpty()) {
                        if (items.data!!.searchListing!!.results!!.size < 10) {
                            isLoadedAll = true
                        }
                        viewModel.setSearchData(items.data!!.searchListing!!.results)
                    } else {
                        isLoadedAll = true
                    }
                    mBinding.ibExploreLocation.visible()

                } else {
                    if (pageNumber == 1) {
                        mBinding.llNoResult.visible()
                        mBinding.headerTitle.gone()
                        mBinding.tabFl.visible()
                        mBinding.exploreLl.elevation = 4f
                        if (viewModel.filterCount.value!! <= 0){
                            mBinding.filterCard.setBackgroundResource(
                                R.drawable.bg_corner_stroke
                            )
                        }
                        mBinding.exploreLl.setBackgroundColor(
                            getColor(
                                context!!, R.color.search_location_header_bg
                            )
                        )
                        mBinding.flSearchLoading.gone()
                        mBinding.rvExploreEpoxy.gone()
                        mBinding.ibExploreLocation.gone()
                    }
                    isLoadedAll = true
                }
                loading = false
                viewModel.pagingController1.isLoading = false
            }
        }, { t -> viewModel.handleException(t) })
        compositeDisposable.add(disposable)
        paginator.onNext(pageNumber)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }


    private fun openAuthActivity() {
        AuthActivity.openActivity(requireActivity(), "Home")
    }
}

open class CustomSnapHelper : LinearSnapHelper() {
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        if (layoutManager is LinearLayoutManager) {
            if (!needToDoSnap(layoutManager)) {
                return null
            }
        }
        return super.findSnapView(layoutManager)
    }

    private fun needToDoSnap(linearLayoutManager: LinearLayoutManager): Boolean {
        return linearLayoutManager.findFirstCompletelyVisibleItemPosition() != 0 && linearLayoutManager.findLastCompletelyVisibleItemPosition() != linearLayoutManager.itemCount - 1
    }
}
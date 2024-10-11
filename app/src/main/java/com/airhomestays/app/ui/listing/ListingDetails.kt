package com.airhomestays.app.ui.listing

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.Carousel.setDefaultGlobalSnapHelperFactory
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airbnb.epoxy.VisibilityState
import com.airhomestays.app.*
import com.airhomestays.app.data.remote.paging.Status
import com.airhomestays.app.databinding.ActivityListingDetailsEpoxyBinding
import com.airhomestays.app.ui.auth.AuthActivity
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.booking.Step4Fragment
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.listing.amenities.AmenitiesFragment
import com.airhomestays.app.ui.listing.cancellation.CancellationFragment
import com.airhomestays.app.ui.listing.contact_host.ContactHostFragment
import com.airhomestays.app.ui.listing.desc.DescriptionFragment
import com.airhomestays.app.ui.listing.map.MapFragment
import com.airhomestays.app.ui.listing.photo_story.PhotoStoryFragment
import com.airhomestays.app.ui.listing.pricebreakdown.PriceBreakDownFragment
import com.airhomestays.app.ui.listing.review.ReviewFragment
import com.airhomestays.app.ui.listing.share.ShareActivity
import com.airhomestays.app.ui.payment.PaymentTypeActivity
import com.airhomestays.app.ui.saved.SavedBotomSheet
import com.airhomestays.app.ui.user_profile.UserProfileActivity
import com.airhomestays.app.ui.user_profile.report_user.ReportUserFragment
import com.airhomestays.app.util.*
import com.airhomestays.app.util.Utils.Companion.clickWithDebounce
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.epoxy.CustomSpringAnimation
import com.airhomestays.app.util.epoxy.listingPopularCarousel
import com.airhomestays.app.vo.BillingDetails
import com.airhomestays.app.vo.ListingInitData
import com.yongbeom.aircalendar.AirCalendarDatePickerActivity
import com.yongbeom.aircalendar.core.AirCalendarIntent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import org.json.JSONArray
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.round

class ListingDetails : BaseActivity<ActivityListingDetailsEpoxyBinding, ListingDetailsViewModel>(),
    ListingNavigator {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_listing_details_epoxy
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    private lateinit var mBinding: ActivityListingDetailsEpoxyBinding
    private lateinit var listingDetail: ViewListingDetailsQuery.Results
    private lateinit var initialListData: ListingInitData
    private lateinit var snapHelperFactory: Carousel.SnapHelperFactory
    private lateinit var epoxyVisibilityTracker: EpoxyVisibilityTracker
    private var mCurrentState = State.IDLE
    private var heartDrawableExpaned: Int = 0
    private var heartDrawableIdle: Int = 0
    private var photoPosition = 0
    private var isDescLineCount = 0
    private lateinit var similarListing: ArrayList<GetSimilarListingQuery.Result>
    val array = ArrayList<String>()
    lateinit var openCalendarActivityResultLauncher: ActivityResultLauncher<Intent>

    var residenceType = 0
    var totalRooms = ""
    var propertyType = ""
    var bedTypes = ArrayList<String>()
    var bookRedirection = 0

    enum class State {
        EXPANDED,
        IDLE
    }

    data class PriceBreakDown(var startDate: String, var endDate: String, var guestCount: Int)

    companion object {
        @JvmStatic
        fun openListDetailsActivity(context: Context, listingInitData: ListingInitData) {
            val intent = Intent(context, ListingDetails::class.java)
            intent.putExtra("listingInitData", listingInitData)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }

        @JvmStatic
        fun openListDetailsActivity(
            v: View,
            context: FragmentActivity,
            listingInitData: ListingInitData
        ) {
            val intent = Intent(context, ListingDetails::class.java)
            intent.putExtra("listingInitData", listingInitData)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val options: ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                context, v, ViewCompat.getTransitionName(v).toString()
            )
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        changeStatusBarColor(R.color.black)


        mBinding = viewDataBinding!!
        viewModel.navigator = this
        topView = mBinding.clListingDetails
        window.sharedElementEnterTransition.duration = 300
        CustomSpringAnimation.spring(mBinding.rlListingDetails)

        bookRedirection = intent.getIntExtra("inboxBook", 0)

        if (bookRedirection == 1) {
            viewModel.inboxIntent.value = true
        }

        initView()
        subscribeToLiveData()
        onActivityResults()
    }

    private fun initView() {
        epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(mBinding.rlListingDetails)
        snapHelperFactory = object : Carousel.SnapHelperFactory() {
            override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                return PagerSnapHelper()
            }
        }
        mBinding.ivShareListingDetails.onClick {

            if (supportFragmentManager.backStackEntryCount == 0)
                if (viewModel.isListingDetailsInitialized()) {
                    openShareActivity()
                }
        }

        mBinding.ivItemListingOption.onClick {
            if (supportFragmentManager.backStackEntryCount == 0)
                showAlertDialog()
        }
        mBinding.ivNavigateup.onClick { onBackPressed() }
        if (viewModel.dataManager.currentUserId == null)
            mBinding.rlListingPricedetails.disable()

        mBinding.rlListingPricedetails.onClick {
            if (listingDetail.userId != viewModel.getUserId()) {
                if (viewModel.isListingDetailsInitialized()) {
                    openReview()

                }
            }
        }
        mBinding.btnExplore.onClick {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        mBinding.ivItemListingHeart.onClick {
            if (supportFragmentManager.backStackEntryCount == 0)
                if (viewModel.loginStatus != 0) {
                    listingDetail.id?.let {
                        val bottomSheet =
                            SavedBotomSheet.newInstance(
                                it,
                                listingDetail.listPhotoName!!,
                                false,
                                0
                            )
                        bottomSheet.show(this.supportFragmentManager!!, "bottomSheetFragment")
                    }
                } else {
                    openAuthActivity()
                }
        }

        viewModel.reportUser.get()?.let {
            if (supportFragmentManager.backStackEntryCount == 0)
                Log.e("TAG status", it.toString())
            if (it) {
                Log.e("TAG status2", it.toString())
                openFragment(ReportUserFragment())
            }
        }
    }

    private fun openPrice() {
        if (viewModel.priceBreakDown.get().not()) {
            openAvailabilityActivity()

        } else {
            if (!listingDetail.userId.isNullOrEmpty() && !viewModel.getUserId().isNullOrEmpty()) {
                if (listingDetail.userId != viewModel.getUserId()) {

                    if (openAvailabilityStatus()) {
                        if (viewModel.initialValue.value?.bookingType =="request"){

                            openFragment(PriceBreakDownFragment())
                        }else {
                            openFragment(PriceBreakDownFragment())
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.you_cannot_book_your_own_list),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }
    }

    private fun openReview() {
        if (viewModel.priceBreakDown.get()) {
            if (openAvailabilityStatus()) {
                openAvailabilityActivity()
            }
        }
    }

    private fun openShareActivity() {
        try {
            ShareActivity.openShareIntent(
                this,
                initialListData.id,
                initialListData.title,
                viewModel.carouselUrl.value!!,
                Utils.TransitionAnim(this,"slideup")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadInitialValues(intent).observe(this, Observer {
            it?.let { initValues ->
                initialListData = initValues
                mBinding.rlCheckAvailability.clickWithDebounce {
                    if (NetworkUtils.isNetworkConnected(baseContext)) {
                        if (viewModel.loginStatus != 0) {
                            if (viewModel.isListingDetailsInitialized()) {
                                if (listingDetail.userId != null) {
                                    if (listingDetail.userId != viewModel.getUserId()) {

                                        openPrice()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            resources.getString(R.string.this_listing_is_not_available_to_book),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            openAuthActivity()
                        }
                    } else {
                        viewModel.navigator.showOffline()
                    }
                }
                setUp()

                if (!initialListData.startDate.equals("null") && !initialListData.endDate.equals("null")) {
                    if ((initialListData.startDate == "0").not() &&
                        (initialListData.endDate == "0").not() && !initialListData.startDate.equals(
                            ""
                        )
                    ) {
                        if (initialListData.startDate.contains("-")) {
                            viewModel.startDate.value = initialListData.startDate
                            viewModel.endDate.value = initialListData.endDate
                        } else {
                            viewModel.startDate.value =
                                Utils.getBlockedDateFormat1(initialListData.startDate)
                            viewModel.endDate.value =
                                Utils.getBlockedDateFormat1(initialListData.endDate)
                        }

                        viewModel.getBillingCalculation()
                    }
                }
            }
        })
        viewModel.propertyType.observe(this, Observer {
            if (it != null) {
                propertyType = it
            }
        })
        viewModel.totalRooms.observe(this, Observer {
            if (it != null) {
                totalRooms = it
            }
        })
        viewModel.residenceType.observe(this, Observer {
            residenceType = it
        })

        viewModel.bedTypes.observe(this, Observer {
            bedTypes = it
        })

        viewModel.loadListingDetails().observe(this, Observer { listingDetails ->
            listingDetails?.let {
                try {
                    mBinding.flSearchLoading.gone()
                    initialListData.photo.clear()

                    it.listPhotos?.forEachIndexed { _, listPhoto ->
                        if (it.listPhotoName == listPhoto?.name) {

                            initialListData.photo.add(0, listPhoto?.name!!)

                        } else {
                            initialListData.photo.add(listPhoto?.name!!)

                        }
                    }
                    initialListData.title = it.title!!
                    initialListData.roomType = it.roomType!!

                    initialListData.hostName = it.user?.profile?.firstName!!
                    initialListData.location = it.city + ", " + it.state + ", " + it.country
                    initialListData.mapImage = generateMapLocation(it.lat!!, it.lng!!)
                    initialListData.ownerPhoto = it.user?.profile?.picture
                    initialListData.beds = it.beds!!

                    if (it.isListOwner!!.not()) {
                        viewModel.isWishList.value = it.wishListStatus
                    }

                    with(mBinding) {
                        val currency = viewModel?.getCurrencySymbol() + Utils.formatDecimal(
                            viewModel?.getConvertedRate(
                                it.listingData?.currency!!,
                                it.listingData?.basePrice!!.toDouble()
                            )!!
                        )
                        var pricee = currency + " / " + getString(R.string.night) + " "
                        val imageSpan = ImageSpan(applicationContext, R.drawable.ic_light, 1)
                        val spannableString =
                            SpannableString(pricee + "*")
                        val spannableString1 =
                            SpannableString(pricee)

                        val start = pricee.length

                        val end = pricee.length + 1
                        spannableString.setSpan(imageSpan, start, end, 0)

                        if (!initialListData.bookingType.isNullOrEmpty() && initialListData.bookingType == "instant") {
                            price = spannableString
                        } else {
                            price = spannableString1
                        }
                        reviewsCount = it.reviewsCount

                        var ratingCount = ""
                        if (it.reviewsStarRating != null && it.reviewsStarRating != 0 && it.reviewsCount != null && it.reviewsCount != 0) {
                            val roundOff = round(
                                it.reviewsStarRating?.toDouble()!! / it.reviewsCount
                                    ?.toDouble()!!
                            )
                            Timber.d(
                                "ratingCount ${
                                    round(
                                        it.reviewsStarRating?.toDouble()!! / it.reviewsCount
                                            ?.toDouble()!!
                                    )
                                }"
                            )
                            ratingCount =
                                (it.reviewsStarRating!! / it.reviewsCount!!).toString()
                            ratingsCount = roundOff.toInt().toString()
                        } else {
                            ratingCount = ""
                        }

                        reviewsStarRating = it.reviewsStarRating
                        mBinding.wishListStatus = it.wishListStatus
                        mBinding.isOwnerList = it.isListOwner
                        mBinding.tvListingPrice.visible()
                        mBinding.rlListingBottom.visible()
                    }
                    listingDetail = it
                    if (it.reviewsCount!! > 0) {
                        viewModel.getReview()
                    }
                    listingDetail.houseRules
                        ?.forEachIndexed { _, t: ViewListingDetailsQuery.HouseRule? ->
                            array.add(t?.itemName!!)
                        }

                    mBinding.rlListingDetails.requestModelBuild()

                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    showError()
                }
                viewModel.isSimilarListingLoad.observe(this, Observer {
                    mBinding.rlListingDetails.requestModelBuild()
                })

                viewModel.similarListing.observe(this, Observer { it ->
                    it?.let {
                        similarListing = ArrayList(it)
                        mBinding.rlListingDetails.requestModelBuild()
                    }
                })

                viewModel.posts.observe(this, Observer<PagedList<GetPropertyReviewsQuery.Result>> {
                    viewModel.isReviewsLoad.value = true
                })

                viewModel.isWishList.observe(this, Observer {
                    it?.let { isWishList ->
                        mBinding.wishListStatus = isWishList
                        if (isWishList) {
                            mBinding.ivItemListingHeart.visible()
                            heartDrawableExpaned = R.drawable.ic_filled_heart
                            heartDrawableIdle = R.drawable.ic_filled_heart
                            mBinding.ivItemListingHeart.setImageDrawable(
                                ContextCompat.getDrawable(
                                    this@ListingDetails,
                                    R.drawable.ic_filled_heart
                                )
                            )
                        } else {
                            heartDrawableExpaned = R.drawable.ic_not_filled_heart
                            heartDrawableIdle = R.drawable.ic_not_filled_heart
                            if (mCurrentState == State.EXPANDED) {
                                mBinding.ivItemListingHeart.visible()
                                mBinding.ivItemListingHeart.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@ListingDetails,
                                        heartDrawableExpaned
                                    )
                                )
                            } else {
                                mBinding.ivItemListingHeart.visible()
                                mBinding.ivItemListingHeart.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@ListingDetails,
                                        heartDrawableIdle
                                    )
                                )
                            }
                        }
                    }
                })

                viewModel.reviewCount.observe(this, Observer {
                    mBinding.reviewsCount = it
                })

                viewModel.networkState.observe(this, Observer {
                    it?.let { networkState ->
                        if (networkState.status == Status.FAILED) {
                            viewModel.isReviewsLoad.value = false
                            it.msg?.let { thr ->
                                viewModel.handleException(thr)
                            } ?: viewModel.handleException(Throwable())
                        }
                    }
                })


            }
        })

    }

    private fun movePosition(position: Int?) {
        position?.let {
            if (photoPosition != position) {
                photoPosition = position
                val view =
                    ((mBinding.rlListingDetails as RecyclerView).layoutManager as LinearLayoutManager).findViewByPosition(
                        0
                    )
                if (view is RecyclerView) {
                    view.scrollToPosition(photoPosition)
                }
            }
        }
    }

    private fun generateMapLocation(latitude: Double, longitude: Double): String {
        val location = Location("location")
        location.latitude = latitude
        location.longitude = longitude
        return GoogleStaticMapsAPIServices.getStaticMapURL(location, 155)
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment) {
        hideSnackbar()
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_up,
                R.anim.slide_down,
                R.anim.slide_up,
                R.anim.slide_down
            )
            .add(mBinding.flListing.id, fragment, fragment.tag)
            .addToBackStack(null)
            .commit()
    }

    fun openFragment1(fragment: androidx.fragment.app.Fragment) {
        hideSnackbar()
        val bundle = Bundle()
        bundle.putInt("ProfileID", viewModel.profileID.value!!)
        fragment.arguments = bundle
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_up,
                R.anim.slide_down,
                R.anim.slide_up,
                R.anim.slide_down
            )
            .add(mBinding.flListing.id, fragment)
            .addToBackStack(null)
            .commit()
    }
    fun openFragment2(fragment: androidx.fragment.app.Fragment,type:String) {
        hideSnackbar()
        val bundle = Bundle()
        bundle.putString("screenType", type)
        fragment.arguments = bundle
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_up,
                R.anim.slide_down,
                R.anim.slide_up,
                R.anim.slide_down
            )
            .add(mBinding.flListing.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun toggleAnimation(colorFrom: Int, colorTo: Int) {
        val colorFrom1 = ContextCompat.getColor(this@ListingDetails, colorFrom)
        val colorTo1 = ContextCompat.getColor(this@ListingDetails, colorTo)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom1, colorTo1)
        colorAnimation.duration = 200
        colorAnimation.addUpdateListener { animator ->
            mBinding.toolbarListingDetails.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

    override fun openBillingActivity(isProfilePresent: Boolean) {
        if (isProfilePresent) {
            try {
                viewModel.billingCalculation.value?.let {
                    val spPrice = it.specialPricing
                    var priceArray = ArrayList<HashMap<String, String>>()
                    spPrice?.forEachIndexed { index, specialPricing ->
                        var temp = HashMap<String, String>()
                        temp.put("blockedDates", specialPricing?.blockedDates!!)
                        temp.put("isSpecialPrice", specialPricing.isSpecialPrice.toString())
                        priceArray.add(temp)
                    }
                    val jsonVal = JSONArray(priceArray)

                    var bookingType = viewModel.listingDetails.value!!.bookingType!!
                    if (bookRedirection == 1) {
                        bookingType = "instant"
                    }

                    viewModel.billingDetails.value = BillingDetails(
                        viewModel.billingCalculation.value!!.checkIn!!,
                        viewModel.billingCalculation.value!!.checkOut!!,
                        viewModel.billingCalculation.value!!.basePrice!!,
                        viewModel.billingCalculation.value!!.nights!!,
                        viewModel.billingCalculation.value!!.cleaningPrice!!,
                        viewModel.billingCalculation.value!!.guestServiceFee!!,
                        viewModel.billingCalculation.value!!.discount!!,
                        viewModel.billingCalculation.value?.discountLabel.orEmpty(),

                        viewModel.billingCalculation.value!!.total!!,
                        array,
                        viewModel.listingDetails.value!!.listPhotos!![0]?.name!!,
                        viewModel.listingDetails.value!!.title!!,
                        viewModel.listingDetails.value!!.listingData?.cancellation
                            ?.policyName!!,
                        viewModel.listingDetails.value!!.listingData?.cancellation
                            ?.policyContent!!,
                        viewModel.initialValue.value?.guestCount!!,
                        viewModel.initialValue.value?.additionalGuestCount!!,
                        viewModel.initialValue.value?.visitors!!,
                        viewModel.initialValue.value?.infantCount!!,
                        viewModel.initialValue.value?.petCount!!,
                        viewModel.billingCalculation.value!!.petPrice!!,
                        viewModel.billingCalculation.value!!.infantPrice!!,
                        viewModel.billingCalculation.value!!.visitorsPrice!!,
                        viewModel.billingCalculation.value!!.hostServiceFee!!,
                        viewModel.billingCalculation.value!!.additionalPrice!!,
                        viewModel.initialValue.value!!.id,
                        viewModel.billingCalculation.value!!.currency!!,
                        bookingType,
                        isProfilePresent,
                        viewModel.billingCalculation.value!!.averagePrice!!,
                        viewModel.billingCalculation.value!!.priceForDays!!,
                        jsonVal.toString().orEmpty(),
                       viewModel.billingDetails.value?.razorPayOrderID!!,
                       viewModel.billingDetails.value?.razorPayPaymentID!!,
                        viewModel.billingCalculation.value!!.isSpecialPriceAssigned!!,
                        viewModel.billingDetails.value!!.threadId

                    )
                    val intent = Intent(this, PaymentTypeActivity::class.java)
                    intent.putExtra("listDetails", viewModel.initialValue.value!!)
                    intent.putExtra("billingDetails", viewModel.billingDetails.value)
                    intent.putExtra("msg", viewModel.msg.get()!!.trim())
                    openCalendarActivityResultLauncher.launch(intent)


                }
            } catch (e: Exception) {
                e.printStackTrace()
                showError()
            }
        } else {
            openFragment(Step4Fragment())

        }
    }

    override fun openPriceBreakdown() {
        if (bookRedirection == 1)
            openFragment(PriceBreakDownFragment())
    }

    override fun removeSubScreen() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            onBackPressed()
        }
    }

    @SuppressLint("ResourceType")
    private fun setUp() {
        try {
            mBinding.rlListingDetails.withModels {
                if (::initialListData.isInitialized && !initialListData.title.isEmpty()) {
                    // Listing Stories

                    if (initialListData.photo.isNotEmpty()) listingPhotosCarousel {
                        id("carousel")
                        paddingDp(0)
                        setDefaultGlobalSnapHelperFactory(object : Carousel.SnapHelperFactory() {
                            override fun buildSnapHelper(context: Context?): androidx.recyclerview.widget.SnapHelper {
                                return PagerSnapHelper()
                            }
                        })

                        withModelsFrom(initialListData.photo) {
                            ViewholderSearchCarouselBindingModel_()
                                .id(it)
                                .url(it)
                                .clickListener { _ ->
                                    if (supportFragmentManager.backStackEntryCount == 0)
                                        if (viewModel.isListingDetailsLoad.value!!) {
                                            openFragment(PhotoStoryFragment())
                                        }
                                }
                                .onVisibilityChanged { model, _, _, _, _, _ ->
                                    viewModel.setCarouselCurrentPhoto(model.url())
                                }
                                .onVisibilityStateChanged { _, _, _ ->

                                }
                        }
                        onVisibilityChanged { _, _, percentVisibleHeight, _, _, _ ->
                            mCurrentState = if (percentVisibleHeight < 30) {
                                if (mCurrentState != State.EXPANDED) {
                                    toggleAnimation(R.color.transparent, R.color.white)
                                    if (::listingDetail.isInitialized) {
                                        if (listingDetail.isListOwner!!.not()) {
                                            mBinding.ivItemListingHeart.visible()
                                            mBinding.ivItemListingHeart.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@ListingDetails,
                                                    heartDrawableExpaned
                                                )
                                            )
                                        } else {
                                            mBinding.ivItemListingHeart.visible()
                                        }
                                    }
                                    mBinding.ivNavigateup.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@ListingDetails,
                                            R.drawable.ic_left_arrow
                                        )
                                    )
                                    mBinding.ivShareListingDetails.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@ListingDetails,
                                            R.drawable.ic_share
                                        )
                                    )
                                    ViewCompat.setElevation(mBinding.appBarLayout, 5F)
                                    mBinding.appBarLayout.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@ListingDetails,
                                            R.color.white
                                        )
                                    )
                                }
                                State.EXPANDED
                            } else {
                                if (mCurrentState != State.IDLE) {
                                    toggleAnimation(R.color.white, R.color.transparent)
                                    if (::listingDetail.isInitialized) {
                                        if (listingDetail.isListOwner!!.not()) {
                                            mBinding.ivItemListingHeart.enable()
                                            mBinding.ivItemListingHeart.visible()
                                            mBinding.ivItemListingHeart.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                    this@ListingDetails,
                                                    heartDrawableIdle
                                                )
                                            )
                                        } else {
                                            mBinding.ivItemListingHeart.visible()
                                        }
                                    }
                                    mBinding.ivNavigateup.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@ListingDetails,
                                            R.drawable.ic_left_arrow
                                        )
                                    )
                                    mBinding.ivShareListingDetails.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@ListingDetails,
                                            R.drawable.ic_share
                                        )
                                    )
                                    ViewCompat.setElevation(mBinding.appBarLayout, 0F)
                                    mBinding.appBarLayout.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@ListingDetails,
                                            R.color.transparent
                                        )
                                    )
                                }
                                State.IDLE
                            }
                        }
                        onBind { _, view, _ ->
                            (view as RecyclerView).scrollToPosition(Int.MAX_VALUE / 2)
                        }
                    }

                    // Title / Host
                    if (initialListData.title.isNotEmpty()) viewholderListingDetailsTitle {
                        id("title")
                        type(propertyType)
                        title(initialListData.title.trim().replace("\\s+", " "))
                        location(initialListData.location)
                        owner(getString(R.string.hosted_by) + " " + initialListData.hostName)
                        url(initialListData.ownerPhoto)
                        if (residenceType == 1)
                            noBed(true)

                        beds("Personal home")
                        mBinding.bookingType = initialListData.bookingType
                        onProfileClick(View.OnClickListener {
                            try {
                                Utils.clickWithDebounce(it) {
                                    if (::listingDetail.isInitialized) {
                                        UserProfileActivity.openProfileActivity(
                                            this@ListingDetails,
                                            listingDetail.user?.profile?.profileId!!,
                                            true
                                        )
                                    }
                                }

                            } catch (e: KotlinNullPointerException) {
                                e.printStackTrace()
                            }
                        })
                    }

                    if (viewModel.isListingDetailsLoad.value != null)
                        if (viewModel.isListingDetailsLoad.value!! && ::listingDetail.isInitialized) {
                            // Icons
                            viewholderListingDetailsIcons {
                                id("icon")
                                guestCount(
                                    listingDetail.personCapacity!!
                                        .toString() + " " + resources.getQuantityString(
                                        R.plurals.guest_cap_count,
                                        listingDetail.personCapacity!!
                                    )
                                )
                                val count = listingDetail.bathrooms!!
                                val remain = Utils.formatDecimal(count)
                                if (count > 1.0) {
                                    when (viewModel.bathroomType) {
                                        "Private Room" -> {
                                            bathCount(
                                                "$remain " + resources.getQuantityString(
                                                    R.plurals.private_bath_count,
                                                    2
                                                )
                                            )
                                        }

                                        "Shared Room" -> {
                                            bathCount(
                                                "$remain " + resources.getQuantityString(
                                                    R.plurals.shared_bath_count,
                                                    2
                                                )
                                            )
                                        }

                                        else -> {
                                            bathCount(
                                                "$remain " + resources.getQuantityString(
                                                    R.plurals.bathroom_count,
                                                    2
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    when (viewModel.bathroomType) {
                                        "Private Room" -> {
                                            bathCount(
                                                "$remain " + resources.getQuantityString(
                                                    R.plurals.private_bath_count,
                                                    1
                                                )
                                            )
                                        }

                                        "Shared Room" -> {
                                            bathCount(
                                                "$remain " + resources.getQuantityString(
                                                    R.plurals.shared_bath_count,
                                                    1
                                                )
                                            )
                                        }

                                        else -> {
                                            bathCount(
                                                "$remain " + resources.getQuantityString(
                                                    R.plurals.bathroom_count,
                                                    1
                                                )
                                            )
                                        }
                                    }
                                }

                                bedroomCount(
                                    listingDetail.bedrooms?.toInt()
                                        .toString() + " " + resources.getQuantityString(
                                        R.plurals.bedroom_cap_count,
                                        listingDetail.bedrooms!!.toFloatOrNull()!!.toInt()
                                    )
                                )
                                bedCount(
                                    listingDetail.beds!!
                                        .toString() + " " + resources.getQuantityString(
                                        R.plurals.caps_bed_count,
                                        listingDetail.beds!!
                                    )
                                )
                                totalRooms(totalRooms)
                                roomType(listingDetail.roomType)
                            }

                            viewholderDivider {
                                id(1)
                            }
                            // Check-in / out time
                            viewholderListingDetailsCheckinOut {
                                id("Check-in time")
                                isTime(true)
                                rightSide(resources.getString(R.string.check_in_))
                                clickListener(View.OnClickListener {})
                                if (listingDetail.listingData?.checkInStart!! == "Flexible") {
                                    leftSide(listingDetail.listingData?.checkInStart!!)
                                } else {
                                    if (BindingAdapters.timeConverter(
                                            listingDetail.listingData?.checkInStart!!
                                        ) == "0am"
                                    ) {
                                        leftSide("12am")
                                    } else if (BindingAdapters.timeConverter(
                                            listingDetail.listingData?.checkInStart!!
                                        ) == "0pm"
                                    ) {
                                        leftSide("12pm")
                                    } else {
                                        leftSide(
                                            BindingAdapters.timeConverter(
                                                listingDetail.listingData?.checkInStart!!
                                            )
                                        )
                                    }
                                }
                                if (listingDetail.listingData?.checkInEnd!! == "Flexible") {
                                    leftSide2(listingDetail.listingData?.checkInEnd)
                                } else {
                                    if (BindingAdapters.timeConverter(
                                            listingDetail.listingData?.checkInEnd!!
                                        ) == "0am"
                                    ) {
                                        leftSide2("12am")
                                    } else if (BindingAdapters.timeConverter(
                                            listingDetail.listingData?.checkInEnd!!
                                        ) == "0pm"
                                    ) {
                                        leftSide2("12pm")
                                    } else {
                                        leftSide2(
                                            BindingAdapters.timeConverter(
                                                listingDetail.listingData?.checkInEnd!!
                                            )
                                        )
                                    }
                                }
                                onBind { _, view, _ ->
                                    val textView =
                                        view.dataBinding.root.findViewById<TextView>(R.id.tv_listing_checkin_placeholder)

                                    if (listingDetail.listingData
                                            ?.checkInStart!! == "Flexible" && listingDetail.listingData
                                            ?.checkInEnd!! == "Flexible"
                                    ) {
                                        textView.text =
                                            resources.getString(R.string.check_in_) + ": " + listingDetail.listingData
                                                ?.checkInStart
                                    }
                                }
                            }

                            viewholderDivider {
                                id(766)
                            }


                            viewholderHeaderSmall {
                                id("about")
                                header(resources.getString(R.string.about_list))
                            }
                            // Desc
                            viewholderListingDetailsDesc {
                                id("desc")
                                desc(listingDetail.description)
                                size(13.toFloat())
                                clickListener(View.OnClickListener {
                                    openFragment(
                                        DescriptionFragment()
                                    )
                                })
                                if (isDescLineCount == 1)
                                    paddingBottom(true)
                                else
                                    paddingBottom(false)
                                onBind { _, view, _ ->
                                    val textView =
                                        view.dataBinding.root.findViewById<TextView>(R.id.tv_descTemp)
                                    textView.post {
                                        isDescLineCount = (textView as TextView).lineCount
                                        textView.visibility = View.GONE
                                        this@withModels.requestModelBuild()
                                    }
                                }
                            }
                            if (isDescLineCount > 4) {
                                viewholderListingDetailsListReadmore {
                                    id("readmoreDesc")
                                    paddingTop(true)
                                    text(resources.getString(R.string.read_more))
                                    imgVisibility(true)
                                    clickListener(View.OnClickListener {
                                        openFragment(
                                            DescriptionFragment()
                                        )
                                    })
                                }
                            }

                            viewholderDivider {
                                id(2)
                            }

                            if (bedTypes.size != 0 && bedTypes.isNotEmpty()) {
                                viewholderHeaderSmall {
                                    id("aboutee")
                                    header(resources.getString(R.string.bed_types))
                                }
                                bedTypes.forEachIndexed { index, s ->
                                    if (index < 3)
                                        viewholderListingDetailsDesc2 {
                                            id(index)
                                            desc(s)
                                            size(13.toFloat())
                                            paddingBottom(true)
                                        }

                                }


                                if (bedTypes.size > 3) {
                                    viewholderListingDetailsShowAll {
                                        id("readmore")
                                        text(resources.getString(R.string.show_all_bedTypes))
                                        clickListener(View.OnClickListener {
                                            openFragment(AmenitiesFragment.newInstance(getString(R.string.bed_types)))
                                        })
                                    }
                                    viewholderDivider {
                                        id(555)
                                    }
                                } else {
                                    viewholderDividerPaddingTop {
                                        id(2355)
                                    }
                                }
                            }

                            // Min / Max nights
                            if (listingDetail.listingData
                                    ?.minNight != 0 || listingDetail.listingData
                                    ?.maxNight != 0
                            ) {
                                viewholderHeaderSmall {
                                    id("max/min")
                                    header(resources.getString(R.string.min_max_nights))
                                }

                                if (listingDetail.listingData
                                        ?.minNight != 0
                                ) viewholderListingDetailsDesc2 {
                                    id("min")
                                    if (listingDetail.listingData?.maxNight != 0)
                                        paddingBottom(false)
                                    else
                                        paddingBottom(true)
                                    size(13.toFloat())
                                    if (listingDetail.listingData?.minNight == 0) {
                                        desc(
                                            "1 " + resources.getString(R.string.min) + resources.getQuantityString(
                                                R.plurals.night_count,
                                                1
                                            )
                                        )
                                    } else {
                                        desc(
                                            listingDetail.listingData?.minNight
                                                .toString() + " " + resources.getString(R.string.min) +
                                                    resources.getQuantityString(
                                                        R.plurals.night_count,
                                                        listingDetail.listingData?.minNight!!
                                                    )
                                        )
                                    }
                                }

                                if (listingDetail.listingData?.maxNight != 0) {
                                    viewholderListingDetailsDesc2 {
                                        id("max")
                                        paddingBottom(true)
                                        paddingTop(true)
                                        size(13.toFloat())
                                        if (listingDetail.listingData?.maxNight == 0) {
                                            desc(
                                                "1 " + resources.getString(R.string.max) + resources.getQuantityString(
                                                    R.plurals.night_count,
                                                    1
                                                )
                                            )
                                        } else {
                                            desc(
                                                listingDetail.listingData?.maxNight
                                                    .toString() + " " + resources.getString(R.string.max) +
                                                        resources.getQuantityString(
                                                            R.plurals.night_count,
                                                            listingDetail.listingData
                                                                ?.maxNight!!
                                                        )
                                            )
                                        }
                                    }
                                }

                                viewholderDivider {
                                    id(22)
                                }
                            }

                            // Amenities
                            if (listingDetail.userAmenities?.isNotEmpty()!!) {

                                viewholderHeaderSmall {
                                    id("Amenities")
                                    header(resources.getString(R.string.amenities))
                                }

                                listingDetail.userAmenities
                                    ?.forEachIndexed { index, userAmenity ->
                                        if (index < 3) {
                                            viewholderListingDetailsSublist {
                                                id(userAmenity?.id)
                                                list(userAmenity?.itemName)
                                                needImage(true)
                                                if (userAmenity?.image != null && userAmenity.image != "")
                                                    amenitiesImage(Constants.amenities + userAmenity.image)
                                                else
                                                    amenitiesImage("")
                                                paddingBottom(true)
                                            }
                                        }
                                    }

                                if (listingDetail.userAmenities?.size!! > 3) {
                                    viewholderListingDetailsShowAll {
                                        id("readmore")
                                        text(resources.getString(R.string.show_all_amenities))
                                        clickListener(View.OnClickListener {
                                            openFragment(AmenitiesFragment.newInstance(getString(R.string.amenities)))
                                        })
                                    }
                                    viewholderDivider {
                                        id(5)
                                    }
                                } else {
                                    viewholderDividerPadding {
                                        id(23)
                                    }
                                }
                            }

                            // User Space
                            if (listingDetail.userSpaces?.isNotEmpty()!!) {
                                viewholderHeaderSmall {
                                    id("User Space")
                                    header(resources.getString(R.string.user_space))
                                }

                                listingDetail.userSpaces?.forEachIndexed { index, userSpace ->
                                    if (index < 3) {
                                        viewholderListingDetailsSublist {
                                            id(userSpace?.id)
                                            list(userSpace?.itemName)
                                            paddingBottom(true)
                                            needImage(true)
                                            if (userSpace?.image != null && userSpace.image != "")
                                                amenitiesImage(Constants.amenities + userSpace.image)
                                            else
                                                amenitiesImage("")
                                        }
                                    }
                                }

                                if (listingDetail.userSpaces?.size!! > 3) {
                                    viewholderListingDetailsShowAll {
                                        id("reeadmore")
                                        text(resources.getString(R.string.show_all_shared))
                                        clickListener(View.OnClickListener {
                                            openFragment(AmenitiesFragment.newInstance(getString(R.string.user_space)))
                                        })
                                    }
                                    viewholderDivider {
                                        id(459)
                                    }
                                } else {
                                    viewholderDividerPadding {
                                        id(2098)
                                    }
                                }
                            }

                            // User Safety
                            if (listingDetail.userSafetyAmenities?.isNotEmpty()!!) {

                                viewholderHeaderSmall {
                                    id("User Safety")
                                    header(resources.getString(R.string.safety_amenities))
                                }

                                listingDetail.userSafetyAmenities
                                    ?.forEachIndexed { index, userSafety ->
                                        if (index < 3) {
                                            viewholderListingDetailsSublist {
                                                id(userSafety?.id)
                                                needImage(true)
                                                list(userSafety?.itemName)
                                                if (userSafety?.image != null && userSafety.image != "")
                                                    amenitiesImage(Constants.amenities + userSafety.image)
                                                else
                                                    amenitiesImage("")
                                                paddingBottom(true)
                                            }
                                        }
                                    }

                                if (listingDetail.userSafetyAmenities?.size!! > 3) {
                                    viewholderListingDetailsShowAll {
                                        id("readmore3")
                                        text(resources.getString(R.string.show_all_safety))
                                        clickListener(View.OnClickListener {
                                            openFragment(AmenitiesFragment.newInstance(getString(R.string.safety_amenities)))
                                        })
                                    }
                                    viewholderDivider {
                                        id(459)
                                    }
                                } else {
                                    viewholderDividerPadding {
                                        id(2098)
                                    }
                                }
                            }

                            viewholderHeaderSmall {
                                id("staticMap")
                                header(resources.getString(R.string.you_will_be_here))
                            }

                            // Map Location
                            viewholderListingDetailsMap {
                                id("staticMap3")
                                img(initialListData.mapImage)
                                location(listingDetail.city + ", " + listingDetail.state + ", " + listingDetail.country)
                                clickListener(View.OnClickListener {
                                    clickWithDebounce(it) {
                                        openFragment(MapFragment())
                                    }
                                })
                                onVisibilityStateChanged { _, _, visibilityState ->
                                    if (visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE && viewModel.isSimilarListingLoad.value == null) {
                                        viewModel.getSimilarListing()
                                    }
                                }
                            }


                            viewholderDivider {
                                id(43)
                            }


                            // Review
                            if (viewModel.isReviewsLoad.value != null) {
                                if (viewModel.isReviewsLoad.value!! && viewModel.posts.value?.size!! > 0) {
                                    viewholderReviewHeader {
                                        id("reviewHeader")
                                        paddingStart(true)
                                        if (listingDetail.reviewsCount != null && listingDetail.reviewsStarRating != null) {
                                            val roundOff = ceil(
                                                listingDetail.reviewsStarRating
                                                    ?.toDouble()!! / listingDetail.reviewsCount
                                                    ?.toDouble()!!
                                            )
                                            displayCount(roundOff.toInt())
                                            reviewsCount(listingDetail.reviewsCount)
                                        }
                                        isBlack(true)
                                        large(true)
                                    }
                                    listingPopularCarousel {
                                        id("SimilarCarousel11222")
                                        padding(Carousel.Padding.dp(20, 10, 20, 20, 25))
                                        setDefaultGlobalSnapHelperFactory(snapHelperFactory)
                                        models(mutableListOf<ViewholderListingDetailsReviewListBindingModel_>().apply {
                                            viewModel.posts.value?.forEachIndexed { index, result ->
                                                if (index < 10) {
                                                    val name = if (result.isAdmin == true) {
                                                        getString(R.string.verified_by) + " " + getString(
                                                            R.string.app_name
                                                        )
                                                    } else {
                                                        result.authorData?.profileFields?.firstName ?: ""
                                                    }
                                                    add(ViewholderListingDetailsReviewListBindingModel_()
                                                        .id(index)
                                                        .date(result.createdAt)
                                                        .title("")
                                                        .isListingDetails(true)
                                                        .type("listing")
                                                        .reviewsTotal(1)
                                                        .ratingTotal(result.rating)
                                                        .imgUrl(
                                                            result.authorData?.profileFields?.picture ?: ""
                                                        )
                                                        .profileId(
                                                            result.authorData?.profileFields?.profileId ?: 0
                                                        )
                                                        .name("$name's")
                                                        .onAvatarClick(View.OnClickListener {
                                                            if (result.isAdmin?.not()!!) {
                                                                if (result.authorData != null && result.authorData
                                                                        ?.profileFields
                                                                        ?.profileId != null
                                                                ) {
                                                                    UserProfileActivity.openProfileActivity(
                                                                        this@ListingDetails,
                                                                        result.authorData
                                                                            ?.profileFields
                                                                            ?.profileId!!,
                                                                        false
                                                                    )
                                                                }
                                                            }
                                                        })
                                                        .isAdmin(result.isAdmin)
                                                        .onBind { _, view, _ ->
                                                            try {
                                                                val textView1 =
                                                                    view.dataBinding.root.findViewById<TextView>(
                                                                        R.id.tv_name
                                                                    )

                                                                textView1.onClick {
                                                                    if (!textView1.text.contains(
                                                                            getString(R.string.verified_by)
                                                                        )
                                                                    )
                                                                        UserProfileActivity.openProfileActivity(
                                                                            this@ListingDetails,
                                                                            result.authorData
                                                                                ?.profileFields
                                                                                ?.profileId!!,
                                                                            false
                                                                        )
                                                                }

                                                                val textView =
                                                                    view.dataBinding.root.findViewById<TextView>(
                                                                        R.id.tv_reviewContent
                                                                    )
                                                                var spannableString =
                                                                    SpannableString("")
                                                                val clickableSpan =
                                                                    object : ClickableSpan() {
                                                                        override fun onClick(p0: View) {
                                                                            openFragment(
                                                                                ReviewFragment.newInstance(
                                                                                    listingDetail.reviewsCount!!,
                                                                                    listingDetail.reviewsStarRating!!,
                                                                                    index + 1
                                                                                )
                                                                            )
                                                                        }

                                                                        override fun updateDrawState(
                                                                            ds: TextPaint
                                                                        ) { // override updateDrawState
                                                                            ds.isUnderlineText =
                                                                                false // set to false to remove underline
                                                                            ds.color =
                                                                                ContextCompat.getColor(
                                                                                    view.dataBinding.root.context,
                                                                                    R.color.colorPrimary
                                                                                )
                                                                        }
                                                                    }

                                                                val content = result.reviewContent
                                                                    ?.trim()?.replace(
                                                                    "\\s+".toRegex(),
                                                                    " "
                                                                ) ?: ""
                                                                if (content.length < 70) {
                                                                    spannableString =
                                                                        SpannableString(content)
                                                                } else {
                                                                    val subStringContent =
                                                                        content.substring(0, 70)
                                                                    spannableString =
                                                                        SpannableString(
                                                                            subStringContent + "..." + getString(
                                                                                R.string.read_more_with_out_dot
                                                                            )
                                                                        )
                                                                    spannableString.setSpan(
                                                                        clickableSpan,
                                                                        subStringContent.length + 3,
                                                                        subStringContent.length + 3 + getString(
                                                                            R.string.read_more_with_out_dot
                                                                        ).length,
                                                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                                                    )
                                                                }
                                                                textView.movementMethod =
                                                                    LinkMovementMethod.getInstance()
                                                                textView.text = spannableString
                                                            } catch (e: Exception) {
                                                                e.printStackTrace()
                                                            }
                                                        })
                                                }
                                            }
                                        })
                                    }
                                    if (viewModel.posts.value?.size!! > 1) viewholderListingDetailsReadreview {
                                        id("readReview")
                                        reviewsCount(listingDetail.reviewsCount)
                                        clickListener(View.OnClickListener {
                                            clickWithDebounce(it) {
                                                openFragment(
                                                    ReviewFragment.newInstance(
                                                        listingDetail.reviewsCount!!,
                                                        listingDetail.reviewsStarRating!!,
                                                        0
                                                    )
                                                )
                                            }
                                        })
                                    }

                                }
                                viewholderDivider {
                                    id(645)
                                }
                            }


                            // House rules
                            if (listingDetail.houseRules?.size!! > 0) {
                                viewholderListingDetailsCheckin {
                                    id("House rules")
                                    isTime(false)
                                    rightSide(resources.getString(R.string.house_rules))
                                    leftSide(resources.getString(R.string.read))
                                    clickListener(View.OnClickListener {
                                        clickWithDebounce(it) {
                                            openFragment(AmenitiesFragment.newInstance(getString(R.string.house_rules)))
                                        }
                                    })
                                }

                                viewholderDivider {
                                    id(766677)
                                }
                            }

                            // Cancellation policy
                            viewholderListingDetailsCheckin {
                                id("cancellation policy")
                                isTime(false)
                                rightSide(resources.getString(R.string.cancellation_policy))
                                leftSide(listingDetail.listingData?.cancellation?.policyName)
                                clickListener(View.OnClickListener {
                                        clickWithDebounce(it) {
                                            openFragment(CancellationFragment())
                                        }
                                })
                            }

                            viewholderDivider {
                                id(877)
                            }

                            // Availability
                            viewholderListingDetailsCheckin {
                                id("availability")
                                isTime(false)
                                rightSide(resources.getString(R.string.availability))
                                leftSide(resources.getString(R.string.check))
                                clickListener(View.OnClickListener {

                                    if (viewModel.loginStatus == 0) {
                                        openAvailabilityActivity()
                                    } else {
                                        Utils.clickWithDebounce(it) {
                                            if (listingDetail.userId != viewModel.getUserId()) {
                                                openAvailabilityActivity()
                                            } else {
                                                Toast.makeText(
                                                    this@ListingDetails,
                                                    resources.getString(R.string.this_listing_is_not_available_to_book),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                })
                            }

                            viewholderDivider {
                                id(97677)
                            }

                            // Contact Host
                            if (viewModel.listingDetails.value?.userId != viewModel.dataManager.currentUserId) {
                                viewholderListingDetailsCheckin {
                                    id("Contact Host")
                                    isTime(false)
                                    rightSide(resources.getString(R.string.contact_host))
                                    leftSide(resources.getString(R.string.message))
                                    clickListener(View.OnClickListener {
                                        if (viewModel.loginStatus == 0) {
                                            openAuthActivity()
                                        } else {
                                            clickWithDebounce(it) {
                                                openFragment(ContactHostFragment())
                                            }
                                        }
                                    })
                                }
                                viewholderDivider {
                                    id(10)
                                }
                            }

                            //Similar Homes
                            if (viewModel.similarListing.value?.isNotEmpty()!!) {
                                viewholderHeader {
                                    id("Similar Listing")
                                    header(resources.getString(R.string.similar_listing))
                                }
                                listingPopularCarousel {
                                    id("SimilarCarousel11")
                                    padding(Carousel.Padding.dp(20, 20, 20, 20, 25))
                                    setDefaultGlobalSnapHelperFactory(snapHelperFactory)
                                    models(mutableListOf<ViewholderListingDetailsSimilarCarouselBindingModel_>().apply {
                                        similarListing.forEachIndexed { index, item ->
                                            val currency =
                                                viewModel.getCurrencySymbol() + Utils.formatDecimal(
                                                    viewModel.getConvertedRate(
                                                        item.listingData!!.currency!!,
                                                        item.listingData!!.basePrice!!
                                                            .toDouble()
                                                    )
                                                )
                                            add(
                                                ViewholderListingDetailsSimilarCarouselBindingModel_()
                                                    .id(index)
                                                    .title(item.title)
                                                    .type(item.roomType)
                                                    .bedsCount(item.beds)
                                                    .price(currency)
                                                    .reviewsCount(item.reviewsCount)
                                                    .reviewsStarRating(item.reviewsStarRating)
                                                    .url(item.listPhotoName)
                                                    .bookingType(item.bookingType)
                                                    .wishListStatus(item.wishListStatus)
                                                    .isOwnerList(item.isListOwner)
                                                    .ratingStarCount(
                                                        round(
                                                            item.reviewsStarRating!!
                                                                .toDouble() / item.reviewsCount!!
                                                                .toDouble()
                                                        ).toInt()
                                                    )
                                                    .heartClickListener(View.OnClickListener {
                                                        if (viewModel.loginStatus == 0) {
                                                            openAuthActivity()
                                                        } else {
                                                            val bottomSheet =
                                                                SavedBotomSheet.newInstance(
                                                                    item.id!!,
                                                                    item.listPhotoName!!,
                                                                    true,
                                                                    0
                                                                )
                                                            bottomSheet.show(
                                                                this@ListingDetails.supportFragmentManager,
                                                                "bottomSheetFragment"
                                                            )
                                                        }
                                                    })
                                                    .clickListener(View.OnClickListener {
                                                        Utils.clickWithDebounce(it) {
                                                            val image = ArrayList<String>()
                                                            image.add(item.listPhotoName!!)
                                                            val listingInitData = ListingInitData(
                                                                item.title!!,
                                                                image,
                                                                item.id!!,
                                                                item.roomType!!,
                                                                item.reviewsStarRating,
                                                                item.reviewsCount,
                                                                currency,
                                                                initialListData.guestCount,
                                                                initialListData.additionalGuestCount,
                                                                initialListData.visitors,
                                                                initialListData.petCount,
                                                                initialListData.infantCount,
                                                                initialListData.startDate,
                                                                initialListData.endDate,
                                                                initialListData.beds,
                                                                viewModel.getUserCurrency(),
                                                                viewModel.getCurrencyBase(),
                                                                viewModel.getCurrencyRates(),
                                                                bookingType = item.bookingType!!,
                                                                minGuestCount = initialListData.minGuestCount,
                                                                maxGuestCount = initialListData.maxGuestCount,
                                                                razorPayOrderId = initialListData.razorPayOrderId
                                                            )
                                                            val intent = Intent(
                                                                this@ListingDetails,
                                                                ListingDetails::class.java
                                                            )
                                                            intent.putExtra(
                                                                "listingInitData",
                                                                listingInitData
                                                            )
                                                            openCalendarActivityResultLauncher.launch(intent)
                                                        }
                                                    })
                                            )
                                        }
                                    })
                                }
                            } else {
                                if (viewModel.isSimilarListingLoad.value == null || viewModel.isSimilarListingLoad.value == true) {
                                    viewholderLoader {
                                        id("viewListingLoading")
                                        isLoading(true)
                                    }
                                }
                            }
                        }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount <= 1) {
            setWishListIntent()
            movePosition(viewModel.carouselPosition.value)
            topView = null
            mBinding.flListingRoot.fitsSystemWindows = false
            mBinding.appBarLayout.fitsSystemWindows = true
            mBinding.flListingRoot.requestApplyInsets()
            mBinding.appBarLayout.requestApplyInsets()
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = 0
            onBackPressedDispatcher.onBackPressed()
        } else if (supportFragmentManager.backStackEntryCount == 2) {
            setWishListIntent()
            onBackPressedDispatcher.onBackPressed()
        } else if (supportFragmentManager.backStackEntryCount == 0) {
            setWishListIntent()
            topView = mBinding.clListingDetails
            onBackPressedDispatcher.onBackPressed()

        } else {
            setWishListIntent()
            finish()
        }
    }

    private fun setWishListIntent() {
        if (viewModel.getIsWishListChanged()) {
            val intent = Intent()
            setResult(89, intent)
        }
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    fun openAvailabilityActivity(): Boolean {
        if (::listingDetail.isInitialized) {
            try {
                listingDetail.listingData?.maxDaysNotice?.let {
                    return when (it) {
                        "3months" -> {
                            openCalender(3); true
                        }

                        "6months" -> {
                            openCalender(6); true
                        }

                        "9months" -> {
                            openCalender(9); true
                        }

                        "12months" -> {
                            openCalender(12); true
                        }

                        "available" -> {
                            openCalender(-1); true
                        }

                        "unavailable" -> {
                            showToast(resources.getString(R.string.this_listing_is_not_available_to_book)); false
                        }

                        else -> {
                            false
                        }
                    }
                }
            } catch (e: KotlinNullPointerException) {
                e.printStackTrace()
                return false
            }
        }
        return false
    }

    fun openAvailabilityStatus(): Boolean {
        if (::listingDetail.isInitialized) {
            try {
                listingDetail.listingData?.maxDaysNotice?.let {
                    return when (it) {
                        "3months" -> {
                            true
                        }

                        "6months" -> {
                            true
                        }

                        "9months" -> {
                            true
                        }

                        "12months" -> {
                            true
                        }

                        "available" -> {
                            true
                        }

                        "unavailable" -> {
                            showToast(resources.getString(R.string.this_listing_is_not_available_to_book)); false
                        }

                        else -> {
                            false
                        }
                    }
                }
            } catch (e: KotlinNullPointerException) {
                e.printStackTrace()
                return false
            }
        }
        return false
    }

    fun openCalender(activeMonths: Int) {
        try {
            val isSelect: Boolean =
                !(viewModel.getStartDate() == null || viewModel.getEndDate() == null)
            val intent = AirCalendarIntent(this)
            intent.isBooking(isSelect)
            intent.isSelect(isSelect)
            intent.setBookingDateArray(viewModel.blockedDatesArray)
            intent.setDayStatus(viewModel.dayStatus)
            intent.setCheckInBlockedDate(viewModel.checkInBlockedDatesArray)
            intent.setStartDate(viewModel.startDate.value)
            intent.setEndDate(viewModel.endDate.value)
            intent.isMonthLabels(false)
            intent.setType(true)
            intent.setMaxBookingDate(listingDetail.listingData?.maxNight!!)
            intent.setMinBookingDate(listingDetail.listingData?.minNight!!)
            intent.setActiveMonth(activeMonths)
            openCalendarActivityResultLauncher.launch(intent, Utils.TransitionAnim(this,"slideup"))
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }
    private fun onActivityResults() {
        openCalendarActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            var data=result.data
            if (result.resultCode == RESULT_OK ) {
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
                        mBinding.tvListingCheckAvailability.text =
                            resources.getString(R.string.check_availability)
                    }
                }
            } else if (result.resultCode == 35) {
                val intent = Intent()
                setResult(56, intent)
                finish()
            } else if (result.resultCode == 89) {
                loadSimilarDetails()
            }

        }
    }


    private fun setDateInCalendar(selStartDate: String, selEndDate: String) {
        viewModel.startDate.value = selStartDate
        viewModel.endDate.value = selEndDate
        initialListData.startDate = viewModel.startDate.value!!
        initialListData.endDate = viewModel.endDate.value!!
        viewModel.dateGuestCount.value = PriceBreakDown(
            selStartDate,
            selEndDate,
            viewModel.initialValue.value!!.guestCount
        )
        viewModel.getBillingCalculation()
    }

    override fun onRetry() {
        try {
            if (viewModel.isListingDetailsLoad.value != null &&
                viewModel.isListingDetailsLoad.value!!.not()
            ) {
                viewModel.getListingDetails()
            } else {
                viewModel.loadedApis.value?.let {
                    if (it.contains(1)) {
                        viewModel.getSimilarListing()
                    }
                    if (it.contains(2)) {
                        viewModel.getBillingCalculation()
                    }
                    if (it.contains(3)) {
                        viewModel.checkVerification()
                    }
                    if (it.contains(4)) {
                        viewModel.contactHost()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        epoxyVisibilityTracker.detach(mBinding.rlListingDetails)
        super.onDestroy()
    }

    override fun show404Screen() {
        mBinding.rlRootListing.gone()
        mBinding.flListing.gone()
        mBinding.rlListingDetails.gone()
        mBinding.ll404Page.visible()
    }

    override fun showReportScreen() {
        finish()
    }


    fun loadListingDetails() {
        viewModel.loadListingDetailsWishList()
    }

    fun loadSimilarDetails() {
        viewModel.loadSimilarWishList()
    }
    fun changeStatusBarColor(colorResId: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = this.window
            window.statusBarColor = getColor(android.R.color.white) // or use your preferred color resource
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView: View = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE // Clear previous flags
        }
    }
    private fun showAlertDialog() {
        if (viewModel.profileID.value != null)
            if (viewModel.loginStatus == 0) {
                openAuthActivity()
            } else {
                openFragment1(ReportUserFragment())
            }
    }

    private fun openAuthActivity() {
        AuthActivity.openActivity(this, "Home")
    }


}
package com.airhomestays.app.ui.listing

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.airhomestays.app.ContactHostMutation
import com.airhomestays.app.CreateReportUserMutation
import com.airhomestays.app.CreateRequestToBookMutation
import com.airhomestays.app.GetBillingCalculationQuery
import com.airhomestays.app.GetProfileQuery
import com.airhomestays.app.GetPropertyReviewsQuery
import com.airhomestays.app.GetSimilarListingQuery
import com.airhomestays.app.R
import com.airhomestays.app.ShowUserProfileQuery
import com.airhomestays.app.ViewListingDetailsQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.BillingDetails
import com.airhomestays.app.vo.ListingInitData
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class ListingDetailsViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<ListingNavigator>(dataManager, resourceProvider) {

    val isSimilarListingLoad = MutableLiveData<Boolean?>()
    val isListingDetailsLoad = MutableLiveData<Boolean>()
    val isReviewsLoad = MutableLiveData<Boolean>()
    val isBillingCalculationLoad = MutableLiveData<Boolean>()
    val billingDetails = MutableLiveData<BillingDetails>()

    val similarListing = MutableLiveData<List<GetSimilarListingQuery.Result?>?>()
    val repoResult = MutableLiveData<Listing<GetPropertyReviewsQuery.Result>>()
    val posts: LiveData<PagedList<GetPropertyReviewsQuery.Result>> = repoResult.switchMap {
        it?.pagedList
    }
    val networkState: LiveData<NetworkState> = repoResult.switchMap { it?.networkState }
    val refreshState: LiveData<NetworkState> = repoResult.switchMap { it?.refreshState }
    val reviewCount: LiveData<Int> = repoResult.switchMap { it?.count }
    val startDate = MutableLiveData<String>()
    val endDate = MutableLiveData<String>()
    val priceBreakDown = ObservableBoolean(false)
    val billingCalculation = MutableLiveData<GetBillingCalculationQuery.Result?>()
    val bookingText = ObservableField(resourceProvider.getString(R.string.check_availability))
    val dateText = ObservableField(resourceProvider.getString(R.string.price_breakdown))
    val dateGuestCount = MutableLiveData<ListingDetails.PriceBreakDown>()

    val personCapacity1 = ObservableField<String>()
    val additionalGuest = ObservableField<String>("0")
    val visitors = ObservableField<String>()
    val infants = ObservableField<String>()
    val pets = ObservableField<String>()
    val personCapacity = MutableLiveData<String>()
    val profileID = MutableLiveData<Int?>()
    val selectContent = ObservableField<String>()
    val userProfile = MutableLiveData<ShowUserProfileQuery.Results>()
    val reportUser = ObservableField<Boolean>()
    val avatar = MutableLiveData<String>()

    lateinit var initialValue: MutableLiveData<ListingInitData>
    lateinit var listingDetails: MutableLiveData<ViewListingDetailsQuery.Results?>
    var bathroomType = "Private Room"
    val residenceType = MutableLiveData<Int>()
    val totalRooms = MutableLiveData<String?>()
    val propertyType = MutableLiveData<String?>()
    val bedTypes = MutableLiveData<ArrayList<String>>(arrayListOf())
    val inboxIntent = MutableLiveData<Boolean>(false)


    var blockedDatesArray = ArrayList<String>()
    var checkInBlockedDatesArray = ArrayList<String>()
    var dayStatus = ArrayList<String>()

    var isPreview = false
    var ispublish = ObservableBoolean(false)

    val msg = ObservableField<String>("")

    val loadedApis = MutableLiveData<ArrayList<Int>?>()

    val isWishList = MutableLiveData<Boolean>()

    val carouselPosition = MutableLiveData<Int>()
    val carouselUrl = MutableLiveData<String>()

    val isWishListChanged = MutableLiveData<Boolean>()

    var retryCalled = ""
    var loginStatus = 3

    init {
        loginStatus = dataManager.currentUserLoggedInMode
        similarListing.value = emptyList()
        loadedApis.value = arrayListOf()
        carouselUrl.value = ""
        selectContent.set("")
        reportUser.set(false)
        residenceType.value = 0
        totalRooms.value = ""
        propertyType.value = ""

    }

    fun setPictureInPref(pic: String?) {
        dataManager.currentUserProfilePicUrl = pic
    }


    fun getIsWishListChanged(): Boolean {
        return isWishListChanged.value?.let { it } ?: false
    }

    fun setCarouselCurrentPhoto(url: String) {
        carouselUrl.value?.let {
            if (it != url) {
                carouselUrl.value = url
            } else {
                return
            }
            if (::initialValue.isInitialized) {
                initialValue.value?.photo?.let { photoList ->
                    if (photoList.size > 0) {
                        for (i in 0 until photoList.size) {
                            if (url == photoList[i]) {
                                carouselPosition.value = i
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadInitialValues(intent: Intent): MutableLiveData<ListingInitData> {
        if (!::initialValue.isInitialized) {
            initialValue = MutableLiveData()
            setInitialValuesFromIntent(intent)
        }
        return initialValue
    }

    fun isListingDetailsInitialized(): Boolean {
        return isListingDetailsLoad.value?.let { it } ?: false
    }

    private fun setInitialValuesFromIntent(intent: Intent) {
        try {
            val initData =
                intent.extras!!.getParcelable<ListingInitData>("listingInitData") as ListingInitData
            if (initData.isPreview) {
                isPreview = true
            } else {
                isPreview = false
            }
            var bookingType = intent.getStringExtra("bookingType").orEmpty()
            if (initData.guestCount == 0) {
                initData.guestCount = 1
                initData.additionalGuestCount = 0
            }

            if (inboxIntent.value!!) {
                bookingType = "instant"
            }

            billingDetails.value = BillingDetails(
                checkIn = intent.getStringExtra("checkIn").orEmpty(),
                checkOut = intent.getStringExtra("checkOut").orEmpty(),
                basePrice = intent.getDoubleExtra("basePrice", 0.0),
                nights = intent.getIntExtra("nights", 0),
                guestServiceFee = intent.getDoubleExtra("guestServiceFee", 0.0),
                cleaningPrice = intent.getDoubleExtra("cleaningPrice", 0.0),
                discount = intent.getDoubleExtra("discount", 0.0),
                discountLabel = intent.getStringExtra("discountLabel"),
                total = intent.getDoubleExtra("total", 0.0),
                houseRule = ArrayList(),
                title = intent.getStringExtra("title").orEmpty(),
                image = intent.getStringExtra("image").orEmpty(),
                cancellation = intent.getStringExtra("cancellation").orEmpty(),
                cancellationContent = intent.getStringExtra("cancellationContent").orEmpty(),
                guest = intent.getIntExtra("guest", 0),
                additionalGuest = intent.getIntExtra("additionalGuest", 0),
                infantCount = intent.getIntExtra("infant", 0),
                pets = intent.getIntExtra("pets", 0),
                visitors = intent.getIntExtra("visitors", 0),
                hostServiceFee = intent.getDoubleExtra("hostServiceFee", 0.0),
                additionalGuestPrice = intent.getDoubleExtra("additionalGuestPrice", 0.0),
                currency = intent.getStringExtra("currency").orEmpty(),
                listId = intent.getIntExtra("listId", 0),
                bookingType = bookingType,
                isProfilePresent = intent.getBooleanExtra("isProfilePresent", false),
                averagePrice = intent.getDoubleExtra("averagePrice", 0.0),
                priceForDays = intent.getDoubleExtra("priceForDays", 0.0),
                specialPricing = intent.getStringExtra("specialPricing").orEmpty(),
                razorPayOrderID = "",
                razorPayPaymentID = "",
                infantPrice = intent.getDoubleExtra("infantPrice", 0.0),
                petPrice = intent.getDoubleExtra("petPrice", 0.0),
                visitorPrice=intent.getDoubleExtra("visitorsPrice", 0.0),
                isSpecialPriceAssigned = intent.getBooleanExtra("isSpecialPriceAssigned", false),
                threadId = intent.getIntExtra("threadId",0)

            )
            initialValue.value = initData


        } catch (e: KotlinNullPointerException) {
            navigator.showError()
        }
    }

    fun loadListingDetails(): MutableLiveData<ViewListingDetailsQuery.Results?> {
        if (!::listingDetails.isInitialized) {
            listingDetails = MutableLiveData()
            getListingDetails()
        }
        return listingDetails
    }

    fun loadListingDetailsWishList() {
        isWishListChanged.value = true
        getListingDetails()
    }

    fun loadSimilarWishList() {
        isWishListChanged.value = true
        getSimilarListing()
    }

    fun getReview() {
        try {
            listingDetails.value?.id?.let {
                repoResult.value =
                    dataManager.listOfReview(it, listingDetails.value?.userId!!, 10)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getListingDetails() {
        initialValue.value?.let {
            isListingDetailsLoad.value = false
            var buildQuery: ViewListingDetailsQuery
            if (isPreview) {
                buildQuery = ViewListingDetailsQuery(
                    listId = initialValue.value!!.id,
                    preview = true.toOptional()
                )
            } else {
                buildQuery = ViewListingDetailsQuery(
                    listId = initialValue.value!!.id
                )
            }

            compositeDisposable.add(dataManager.doListingDetailsApiCall(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data?.viewListing
                        if (data!!.status == 200) {
                            val dates = data.results!!.blockedDates
                            profileID.value = data.results!!.user?.profile?.profileId
                            if (dates != null) {
                                if (dates.size > 0) {
                                    dates.forEachIndexed { index, blockedDate ->
                                        if (blockedDate?.calendarStatus.equals("available")
                                                .not()
                                        ) {
                                            val timestamp = blockedDate?.blockedDates!!
                                            blockedDatesArray.add(
                                                Utils.getBlockedDateFormat(
                                                    timestamp
                                                )
                                            )
                                            dayStatus.add(blockedDate!!.dayStatus!!)
                                        }
                                    }
                                }
                            }
                            if (!data.results?.residenceType.isNullOrEmpty()) {
                                residenceType.value = data.results?.residenceType!!.toInt()
                            } else {
                                residenceType.value = 0
                            }
                            val checkInBlockedDates = data.results!!.checkInBlockedDates
                            if (checkInBlockedDates != null) {
                                if (checkInBlockedDates.size > 0) {
                                    checkInBlockedDates.forEachIndexed { index, blockedDate ->
                                        if (blockedDate?.calendarStatus.equals("available")
                                                .not()
                                        ) {
                                            val timestamp = blockedDate?.blockedDates!!
                                            checkInBlockedDatesArray.add(
                                                Utils.getBlockedDateFormat(
                                                    timestamp
                                                )
                                            )
                                        }
                                    }
                                }
                            }



                            data.results!!.settingsData!!.forEachIndexed { _, settingsDatum ->
                                if (settingsDatum?.listsettings != null) {
                                    if (settingsDatum.listsettings!!.settingsType!!
                                            .typeName == "bathroomType"
                                    ) {
                                        bathroomType = settingsDatum.listsettings!!.itemName!!
                                    }
                                }
                            }

                            propertyType.value =
                                data.results!!.settingsData?.get(1)?.listsettings?.itemName
                            totalRooms.value =
                                data.results!!.settingsData?.get(2)?.listsettings?.itemName
                            val temp = ArrayList<String>()
                            data.results!!.userBedsTypes!!
                                .forEachIndexed { index, userBedsType ->
                                    if (userBedsType != null) {
                                        temp.add(
                                            userBedsType.bedName
                                                .toString() + ": " + userBedsType.bedCount
                                                .toString()
                                        )
                                    }

                                }
                            bedTypes.value!!.addAll(temp)

                            if (initialValue.value?.isPreview!!) {
                                ispublish.set(false)
                            } else {
                                ispublish.set(data.results!!.isPublished!!)
                            }
                            listingDetails.value = data.results
                            isListingDetailsLoad.value = true


                        } else {
                            navigator.show404Screen()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, { handleException(it) }
                )
            )
        }
    }

    fun getSimilarListing() {
        isSimilarListingLoad.value = true
        val buildQuery = GetSimilarListingQuery(
            lat = listingDetails.value?.lat.toOptional(),
            lng = listingDetails.value?.lng.toOptional(),
            listId = listingDetails.value?.id.toOptional()
        )

        compositeDisposable.add(dataManager.doSimilarListingApiCall(buildQuery)
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                isSimilarListingLoad.value = false
                removeApiTolist(1)
                try {
                    val data = response.data!!.getSimilarListing
                    if (data!!.status == 200) {
                        if (data.results?.size!! > 0) {
                            similarListing.value = data.results
                        }
                    } else if (data.status == 500) {
                        navigator.openSessionExpire("ListingDetailsVM")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, {
                isSimilarListingLoad.value = false
                addApiTolist(1)
                handleException(it)
            }))
    }

    fun getBillingCalculation() {
        try {
            isBillingCalculationLoad.value = true
            if (initialValue.value!!.guestCount == 0) {
                initialValue.value!!.guestCount = initialValue.value!!.guestCount.plus(1)
            }


            if (startDate.value == null && endDate.value == null) {
                return
            }
            val buildQuery = GetBillingCalculationQuery(
                listId = initialValue.value!!.id,
                startDate = startDate.value!!,
                endDate = endDate.value!!,
                guests = initialValue.value!!.guestCount,
                visitors = initialValue.value!!.visitors,
                infants = initialValue.value!!.infantCount,
                pets = initialValue.value!!.petCount,
                convertCurrency = initialValue.value!!.selectedCurrency
            )

            compositeDisposable.add(dataManager.getBillingCalculation(buildQuery)
                .doOnSubscribe { priceBreakDown.set(false); setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        removeApiTolist(2)
                        val data = response.data?.getBillingCalculation
                        if (data?.status == 200 && !startDate.value.isNullOrEmpty()) {

                            billingCalculation.value = data.result
                            Log.e("guestCheck",  "1" + initialValue.value!!.additionalGuestCount)

                            navigator.openPriceBreakdown()
                            if (initialValue.value!!.bookingType == "request" && !inboxIntent.value!!) {
                                bookingText.set(resourceProvider.getString(R.string.request_to_book))
                                dateText.set(
                                    getCalenderMonth(startDate.value) + "-" + getCalenderMonth(
                                        endDate.value
                                    )
                                )

                            } else {
                                bookingText.set(resourceProvider.getString(R.string.book_txt))
                                dateText.set(
                                    getCalenderMonth(startDate.value) + "-" + getCalenderMonth(
                                        endDate.value
                                    )
                                )
                            }


                            priceBreakDown.set(true)
                            navigator.hideSnackbar()
                        } else if (data?.status == 500) {
                            navigator.openSessionExpire("ListingDetailsVM")
                        } else {
                            billingCalculation.value = null
                            priceBreakDown.set(false)
                            bookingText.set(resourceProvider.getString(R.string.check_availability))
                            dateText.set(" ")
                            startDate.value = "0"
                            endDate.value = "0"
                            data?.errorMessage?.let {
                                navigator.showSnackbar("Info  ", it)
                            }
                        }
                        isBillingCalculationLoad.value = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    isBillingCalculationLoad.value = false
                    billingCalculation.value = null
                    addApiTolist(2)
                    handleException(it)
                }))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCalenderMonth(dateString: String?): String {

        try {
            return if (dateString != null && dateString != "0" && dateString.isNotEmpty()) {
                val startDateArray: List<String> = dateString.split("-")
                val year: Int = startDateArray[0].toInt()
                val month = startDateArray[1].toInt()
                val date = startDateArray[2].toInt()
                var decimalFormat = DecimalFormat("00")
                val monthPattern = SimpleDateFormat("MMM dd", Locale.US)
                val cal = Calendar.getInstance()
                cal.set(year, month - 1, date)
                monthPattern.format(cal.time)
            } else {
                "Add date"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun checkVerification() {
        val buildQuery = GetProfileQuery()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                //removeApiTolist(3)
                try {
                    val data = response.data!!.userAccount
                    if (data?.status == 200) {
                        val result = data.result
                        if (result!!.verification!!.isEmailConfirmed!!.equals(false)) {
                            navigator.showToast(resourceProvider.getString(R.string.email_not_verified))
                        } else {
                            dataManager.currentUserProfilePicUrl = result.picture
                            if (result.picture.isNullOrEmpty()) {
                                navigator.openBillingActivity(false)
                            } else {
                                navigator.openBillingActivity(true)
                            }
                        }
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("ListingDetailsVM")
                    } else {
                        navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                    }
                } catch (e: Exception) {
                    navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                }
            }, {
                addApiTolist(3)
                handleException(it)
            })
        )
    }

    fun getStartDate(): String? {
        return startDate.value.toString()
    }

    fun getEndDate(): String? {
        return endDate.value.toString()
    }

    fun contactHost() {
        try {
            val buildQuery = ContactHostMutation(
                startDate = Utils.getContactHost(startDate.value!!),
                endDate = Utils.getContactHost(endDate.value!!),
                personCapacity = initialValue.value?.guestCount!!.toOptional(),
                content = msg.get()!!,
                hostId = listingDetails.value?.userId!!,
                listId = listingDetails.value?.id!!,
                userId = dataManager.currentUserId!!,
                type = "inquiry".toOptional()
            )

            compositeDisposable.add(dataManager.contactHost(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data!!.createEnquiry
                        if (data?.status == 200) {
                            msg.set("")
                            navigator.showToast(resourceProvider.getString(R.string.your_message_sent_to_host))
                            navigator.removeSubScreen()
                        } else if (data?.status == 500) {
                            navigator.openSessionExpire("ListingDetailsVM")
                        } else {
                            navigator.showToast(data?.errorMessage.toString())
                        }
                    } catch (e: Exception) {
                        navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                    }
                }, {
                    addApiTolist(4)
                    handleException(it)
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    fun createRequestToBook() {
        try {
            val buildQuery = CreateRequestToBookMutation(
                startDate = Utils.getContactHost(startDate.value!!),
                endDate = Utils.getContactHost(endDate.value!!),
                personCapacity = initialValue.value?.guestCount!!.toOptional(),
                content = msg.get()!!,
                hostId = listingDetails.value?.userId!!,
                listId = listingDetails.value?.id!!,
                userId = dataManager.currentUserId!!,
                type = "requestToBook".toOptional(),
                visitors = initialValue.value?.visitors!!.toOptional(),
                infants = initialValue.value?.infantCount!!.toOptional(),
                pets = initialValue.value?.petCount!!.toOptional(),
            )

            compositeDisposable.add(dataManager.createRequestToBook(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data!!.createRequestToBook
                        if (data?.status == 200) {
                            msg.set("")
                            navigator.showToast(resourceProvider.getString(R.string.your_message_sent_to_host))
                            navigator.removeSubScreen()
                        } else if (data?.status == 500) {
                            navigator.openSessionExpire("ListingDetailsVM")
                        } else {
                            navigator.showToast(data?.errorMessage.toString())
                        }
                    } catch (e: Exception) {
                        navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                    }
                }, {
                    addApiTolist(4)
                    handleException(it)
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    fun addApiTolist(id: Int) {
        val api = loadedApis.value
        api?.add(id)
        loadedApis.value = api
    }

    fun removeApiTolist(id: Int) {
        val api = loadedApis.value
        api?.remove(id)
        loadedApis.value = api
    }

    fun currencyConverter(currency: String, total: Double): String {
        return getCurrencySymbol() + getConvertedRate(currency, total).toString()
    }

    override fun onCleared() {
        super.onCleared()
    }


    fun clearStatusBar(activity: Activity) {
        println("prefTheme:: ${dataManager.prefTheme}")
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.white)
                }

                Configuration.UI_MODE_NIGHT_NO -> {
                    var flags = activity.window.decorView.systemUiVisibility
                    flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    activity.window.decorView.systemUiVisibility = flags
                    activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.white)
                }
            }

        }

    }
}
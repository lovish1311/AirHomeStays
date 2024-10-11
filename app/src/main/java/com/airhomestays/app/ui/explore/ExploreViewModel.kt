package com.airhomestays.app.ui.explore

import android.text.TextWatcher
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.apollographql.apollo3.api.ApolloResponse
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ui.IconGenerator
import com.airhomestays.app.GetBillingCalculationQuery
import com.airhomestays.app.GetExploreListingsQuery
import com.airhomestays.app.GetPopularLocationsQuery
import com.airhomestays.app.GetRoomTypeSettingsQuery
import com.airhomestays.app.R
import com.airhomestays.app.SearchListingQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.DefaultListing
import com.airhomestays.app.vo.HomeType
import com.airhomestays.app.vo.OneTotalPrice
import com.airhomestays.app.vo.Outcome
import com.airhomestays.app.vo.Photo
import com.airhomestays.app.vo.SearchListing
import io.reactivex.rxjava3.core.Single
import java.util.Collections
import javax.inject.Inject


class ExploreViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<ExploreNavigator>(dataManager, resourceProvider) {

    var popularLocations: List<GetPopularLocationsQuery.Result?>? = null
    val repoResult = MutableLiveData<Listing<SearchListingQuery.Result>>()

    var selectedPosition = MutableLiveData(-1)
    val searchPageResult12 =
        MutableLiveData<ArrayList<SearchListing>>().apply { value = ArrayList() }

    val posts: LiveData<PagedList<SearchListingQuery.Result>>? = repoResult.switchMap {
        it.pagedList
    }
    val networkState: LiveData<NetworkState>? = repoResult.switchMap { it.networkState }

    fun repoRetry() {
        repoResult.value?.retry?.invoke()
    }



    val homeType = ArrayList<HomeType>()
    val startDate = MutableLiveData<String>()
    val startMonthName = MutableLiveData<String>()
    val endMonthName = MutableLiveData<String>()
    val endDate = MutableLiveData<String>()
    var amenities = HashSet<Int>()
    var roomType = HashSet<Int>()
    val personCapacity = MutableLiveData<String>()
    val bed = MutableLiveData<String>()
    val bathrooms = MutableLiveData<String>()
    val bedrooms = MutableLiveData<String>()
    var spaces = HashSet<Int>()
    var houseRule = HashSet<Int>()
    val priceRange = MutableLiveData<Array<Int>?>()
    val bookingType = MutableLiveData<String>()
    val location = MutableLiveData<String>()
    var resetBoolean = false
    val filterCount = MutableLiveData<Int>()
    val personCapacity1 = ObservableField<String>()
    var isReset = MutableLiveData(false)
    var listingList = ArrayList<com.airhomestays.app.vo.Listing>()

    var personCount = 0;
    val bed1 = ObservableField<String>()
    val bathrooms1 = ObservableField<String>()
    val bedrooms1 = ObservableField<String>()
    val searchLocation = MutableLiveData<Outcome<List<String>>>()
    val searchResult = MutableLiveData<Boolean>().apply { value = false }
    val minRange = MutableLiveData<Int>()
    val maxRange = MutableLiveData<Int>()
    val minRangeSelected = MutableLiveData<Int>()
    val maxRangeSelected = MutableLiveData<Int>()
    val guestMinCount = MutableLiveData<Int?>()
    val guestMaxCount = MutableLiveData<Int?>()
    val getRoomTypes = MutableLiveData<List<GetRoomTypeSettingsQuery.Result?>?>()

    val exploreLists1 = MutableLiveData<GetExploreListingsQuery.Data>()
    lateinit var pagingController1: SearchListingController1

    val map = Collections.synchronizedMap(HashMap<String, ArrayList<DefaultListing>>())

    var defaultListingData =
        MutableLiveData<Map<String, ArrayList<DefaultListing>>>().apply { value = null }

    var isRefreshing = false

    val refreshWishList = MutableLiveData<Boolean>().apply { value = false }

    val currentPage = MutableLiveData<Int>().apply { value = 1 }
    var isoneTotalPriceChecked=MutableLiveData<Boolean>(false)
    var isGoogleLoaded = false
    var loginStatus = 3


    //filterfragment
    var TempinitialHomeTypeSize = 2
    var TempinitialAmenitiesSize = 2
    var TempinitialHouseRulesSize = 2
    var TempinitialFacilitiesSize = 2
    var TempstartDateFromResult = ""
    var TempendDateFromResult = ""
    var Tempamenities = HashSet<Int>()
    var TemproomType = HashSet<Int>()
    var Tempspaces = HashSet<Int>()
    var TemphouseRule = HashSet<Int>()
    var TempbookingType = String()
    var Tempcount = 0
    var TempminRange = 0
    var TempmaxRange = 0
    var TempcurrencySymbol = String()
    var currentFragment: String = ""

    //searchlocation
    lateinit var textWatcher: TextWatcher

    var dateStart = ""
    var dateEnd = ""
    var billingCalculation=MutableLiveData<GetBillingCalculationQuery.Result>()
    init {
        loginStatus = dataManager.currentUserLoggedInMode
        startDate.value = "0"
        endDate.value = "0"
        location.value = ""
        filterCount.value = 0
        bookingType.value = ""
        personCapacity.value = personCount.toString()
        bed.value = "0"
        bathrooms.value = "0"
        bedrooms.value = "0"
        amenities = HashSet()
        spaces = HashSet()
        houseRule = HashSet()
        priceRange.value = emptyArray()
    }

    fun increaseCurrentPage(page: Int) {
        currentPage.value = page
    }

    fun getRefreshWishList(): Boolean {
        return refreshWishList.value?.let { it } ?: false
    }

    fun getSearchResult(): Boolean {
        return searchResult.value?.let { it } ?: false
    }


    fun getPopular() {
        navigator.disableIcons()
        val request = GetPopularLocationsQuery()

        compositeDisposable.add(dataManager.getPopular(request)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                {
                    refreshWishList.value = false
                    try {
                        val response = it.data!!
                        popularLocations = response.getPopularLocations?.results
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                },
                {
                    handleException(it)
                }
            ))
    }


    fun getexploreLists() {
        navigator.disableIcons()
        val request = GetExploreListingsQuery()

        compositeDisposable.add(dataManager.getExploreListing(request)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                {
                    refreshWishList.value = false
                    try {
                        val response = it.data!!
                        if (response.getMostViewedListing?.status == 200 &&
                            response.getRecommend?.status == 200 &&
                            response.getListingSettingsCommon?.status == 200 &&
                            response.getSearchSettings?.status == 200 &&
                            response.currency?.status == 200
                        ) {
                            setInitialData(response)
                        } else if (response.getMostViewedListing?.status == 500) {
                            if (loginStatus != 0) {
                                navigator.openSessionExpire("")
                            }
                        } else {
                            if (response.getMostViewedListing?.errorMessage == null && response.getRecommend?.errorMessage == null
                                && response.getSearchSettings?.errorMessage == null && response.getListingSettingsCommon?.errorMessage == null
                                && response.currency?.errorMessage == null
                            )
                                navigator.showError()
                            else if (response.getMostViewedListing?.errorMessage == null)
                                navigator.showToast(
                                    response.getRecommend?.errorMessage.toString()
                                )
                            else
                                navigator.showToast(
                                    response.getMostViewedListing?.errorMessage.toString()
                                )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                },
                {
                    handleException(it)
                }
            ))
    }



    private fun setInitialData(response: GetExploreListingsQuery.Data) {
        try {
            minRange.value = getConvertedRate(
                response.getSearchSettings!!.results!!.priceRangeCurrency!!,
                response.getSearchSettings!!.results!!.minPrice!!.toDouble()
            ).toInt()
            maxRange.value = getConvertedRate(
                response.getSearchSettings!!.results!!.priceRangeCurrency!!,
                response.getSearchSettings!!.results!!.maxPrice!!.toDouble()
            ).toInt()

            response.getListingSettingsCommon?.results?.let {
                for (i in 0 until it.size) {
                    if (it[i]?.id == 2) {
                        guestMinCount.value = it[i]?.listSettings?.get(0)?.startValue
                        guestMaxCount.value = it[i]?.listSettings?.get(0)?.endValue

                        it[i]?.listSettings?.get(0)?.startValue?.let {
                            personCount = it
                        }


                    }

                }
            }

            val mostRecommend = parseData(response, false)
            val mostViewed = parseData(response, true)
            map.clear()
            map["recommend"] = mostRecommend
            map["mostViewed"] = mostViewed

            exploreLists1.value = response
            defaultListingData.value = map
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    private fun parseData(
        response: GetExploreListingsQuery.Data,
        isMostViewed: Boolean
    ): ArrayList<DefaultListing> {
        val listingData = ArrayList<DefaultListing>()
        if (isMostViewed) {
            response.getMostViewedListing?.results?.forEach {
                listingData.add(
                    DefaultListing(
                        id = it?.id!!,
                        basePrice = it.listingData!!.basePrice!!,
                        beds = it.beds!!,
                        bookingType = it.bookingType!!,
                        coverPhoto = 0,
                        currency = it.listingData!!.currency!!,
                        isListOwner = it.isListOwner!!,
                        listPhotoName = it.listPhotoName!!,
                        personCapacity = it.personCapacity!!,
                        reviewsCount = it.reviewsCount,
                        reviewsStarRating = it.reviewsStarRating,
                        roomType = it.roomType!!,
                        title = it.title!!,
                        wishListStatus = it.wishListStatus,
                        wishListGroupCount = it.wishListGroupCount
                    )
                )
            }
        } else {
            response.getRecommend?.results?.forEach {
                listingData.add(
                    DefaultListing(
                        id = it?.id!!,
                        basePrice = it.listingData!!.basePrice!!,
                        beds = it.beds!!,
                        bookingType = it.bookingType!!,
                        coverPhoto = 0,
                        currency = it.listingData!!.currency!!,
                        isListOwner = it.isListOwner!!,
                        listPhotoName = it.listPhotoName!!,
                        personCapacity = it.personCapacity!!,
                        reviewsCount = it.reviewsCount,
                        reviewsStarRating = it.reviewsStarRating,
                        roomType = it.roomType!!,
                        title = it.title!!,
                        wishListStatus = it.wishListStatus,
                        wishListGroupCount = it.wishListGroupCount
                    )
                )
            }
        }
        return listingData
    }

    fun getPersonCapacity(): Int {
        return personCapacity.value?.toInt() ?: 0
    }

    fun getStartDate(): String {
        return startDate.value.toString()
    }

    fun getEndDate(): String {
        return endDate.value.toString()
    }

    fun getMinGuestCount(): Int? {
        return guestMinCount.value
    }

    fun getMaxGuestCount(): Int? {
        return guestMaxCount.value
    }

    fun startSearching() {
        increaseCurrentPage(1)
        navigator.searchForListing()
    }

    fun getSearchListing1(): Single<ApolloResponse<SearchListingQuery.Data>> {
        if (roomType.isEmpty()) {
            isReset.value = true
        }
        searchResult.value = true
        catchAll("ExploreSetPriceRange") { setPriceRange() }
        val query = SearchListingQuery(
            bookingType = bookingType.value.toOptional(),
            personCapacity = personCapacity.value?.toInt().toOptional(),
            dates = getDate().toOptional(),
            amenities = amenities?.toList().toOptional(),
            beds = bed.value?.toInt().toOptional(),
            bathrooms = bathrooms.value?.toInt().toOptional() ,
            bedrooms = bedrooms.value?.toInt().toOptional(),
            roomType = roomType.toList().toOptional(),
            spaces = spaces?.toList().toOptional(),
            houseRules = houseRule?.toList().toOptional(),
            priceRange = priceRange.value?.toList().toOptional(),
            address = location.value.toOptional(),
            currentPage = currentPage.value.toOptional(),
            currency = getUserCurrency().toOptional(),
            isOneTotal = isoneTotalPriceChecked.value.toOptional()
        )
        return dataManager.getSearchListing(query)
    }

    fun getSearchListing() {
        try {
            if (getUserCurrency().isNotEmpty() || defaultListingData.value != null)  {
                searchResult.value = true
                catchAll("ExploreSetPriceRange") { setPriceRange() }
                val query = SearchListingQuery(
                    bookingType = bookingType.value.toOptional(),
                    personCapacity = personCapacity.value?.toInt().toOptional(),
                    dates = getDate().toOptional(),
                    amenities = amenities?.toList().toOptional(),
                    beds = bed.value?.toInt().toOptional(),
                    bathrooms = bathrooms.value?.toInt().toOptional() ,
                    bedrooms = bedrooms.value?.toInt().toOptional(),
                    roomType = roomType.toList().toOptional(),
                    spaces = spaces?.toList().toOptional(),
                    houseRules = houseRule?.toList().toOptional(),
                    priceRange = priceRange.value?.toList().toOptional(),
                    address = location.value.toOptional(),
                    currentPage = currentPage.value.toOptional(),
                    currency = getUserCurrency().toOptional(),
                    isOneTotal = isoneTotalPriceChecked.value.toOptional()
                )
                if (searchResult.value!!) {
                    repoResult.value = dataManager.listOfSearchListing(query, 5)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }

    }

    private fun getDate(): String? {
        return if (startDate.value == "0" || endDate.value == "0") {
            null
        } else {
            "'${startDate.value}'" + " AND " + "'${endDate.value}'"
        }
    }

    private fun setPriceRange() {
        try {
            if (minRangeSelected.value != minRange.value || maxRangeSelected.value != maxRange.value) {
                priceRange.value = null
                val min =
                    getConvertedRate(getUserCurrency(), minRangeSelected.value!!.toDouble()).toInt()
                val max =
                    getConvertedRate(getUserCurrency(), maxRangeSelected.value!!.toDouble()).toInt()
                priceRange.value = arrayOf(min, max)
            } else {
                priceRange.value = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    fun setLocation(location: String) {
        this.location.value = location
    }

    fun clearSearchRequest() {
        increaseCurrentPage(1)
        searchResult.value = false
        startDate.value = "0"
        endDate.value = "0"
        amenities = HashSet()
        roomType = HashSet()
        personCapacity.value = personCount.toString()
        bed.value = "0"
        bathrooms.value = "0"
        bedrooms.value = "0"
        dateStart = ""
        dateEnd = ""
        spaces = HashSet()
        houseRule = HashSet()
        priceRange.value = emptyArray()
        location.value = ""
        bookingType.value = ""
        filterCount.value = 0
        minRangeSelected.value = minRange.value
        maxRangeSelected.value = maxRange.value
        defaultListingData.value = defaultListingData.value
    }

    fun refreshOnWishList(flag: Boolean) {
        refreshWishList.value = flag
    }

    fun changeWishListStatus(value: Int?, flag: Boolean, count: Int) {
        val list = defaultListingData.value
        val recommend = list?.get("recommend")
        val mostViewed = list?.get("mostViewed")
        recommend?.forEach {
            if (it.id == value) {
                it.wishListGroupCount = count
                it.wishListStatus = flag
            }
        }
        mostViewed?.forEach {
            if (it.id == value) {
                it.wishListGroupCount = count
                it.wishListStatus = flag
            }
        }
        val map = Collections.synchronizedMap(HashMap<String, ArrayList<DefaultListing>>())
        map["recommend"] = recommend
        map["mostViewed"] = mostViewed
        defaultListingData.value = map
    }

    fun changeWishListStatusInSearch(value: Int?, flag: Boolean, count: Int) {
        val list = searchPageResult12.value
        list?.forEach {
            if (it.id == value) {
                it.wishListGroupCount = count
                it.wishListStatus = flag//true//false//flag
            }
        }
        searchPageResult12.value = list
        changeWishListStatus(value, flag, count)
    }

    fun setSearchData(results: List<SearchListingQuery.Result?>?) {
        if (!results.isNullOrEmpty() && !searchPageResult12.value.isNullOrEmpty()){
            if (results.get(0)?.id == searchPageResult12.value!!.get(0).id) {
                searchPageResult12.value = ArrayList()
            }
        }
        val list = searchPageResult12.value
        list?.addAll(parseData(results))
        searchPageResult12.value = list
    }

    private fun parseData(results: List<SearchListingQuery.Result?>?): List<SearchListing> {
        val list = ArrayList<SearchListing>()
        results?.forEach {
            val photoList = ArrayList<Photo>()
            it!!.listPhotos?.forEach {
                photoList.add(Photo(it?.id!!, it?.name!!))
            }
            list.add(
                SearchListing(
                    id = it.id!!,
                    wishListStatus = it.wishListStatus,
                    title = it.title!!,
                    roomType = it.roomType,
                    reviewsStarRating = it.reviewsStarRating,
                    reviewsCount = it.reviewsCount,
                    personCapacity = it.personCapacity!!,
                    listPhotoName = it.listPhotoName,
                    isListOwner = it.isListOwner,
                    currency = it.listingData?.currency!!,
                    coverPhoto = it.coverPhoto?:0,
                    bookingType = it.bookingType!!,
                    beds = it.beds!!,
                    basePrice = it.listingData?.basePrice!!,
                    listPhotos = photoList,
                    lat = it.lat?:0.0!!,
                    lng = it.lng?:0.0!!,
                    city=it.city,
                    state = it.state,
                    country = it.country,
                    oneTotalPrice = OneTotalPrice(
                        averagePrice = it.listingData?.oneTotalPrice?.isAverage?:0.0,
                        nights = it.listingData?.oneTotalPrice?.dayDifference?:0,
                        cleaningPrice = it.listingData?.oneTotalPrice?.cleaningPrice?:0.0,
                        serviceFee = it.listingData?.oneTotalPrice?.serviceFee?:0.0,
                        Total = it.listingData?.oneTotalPrice?.oneTotalPrice?:0.0,
                        Daytotal = it.listingData?.oneTotalPrice?.isDayTotal?:0.0,
                        discount = it.listingData?.oneTotalPrice?.discount?:0.0,
                    ),
                    oneTotalpricechecked = isoneTotalPriceChecked.value
                )
            )
        }
        return list
    }


    fun getRoomTypes() {
        val buildQuery = GetRoomTypeSettingsQuery()
        compositeDisposable.add(dataManager.doGetRoomTypeSettingsApiCall(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.getRoomTypeSettings?.results
                    if (data != null) {
                        getRoomTypes.value = data
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }



}
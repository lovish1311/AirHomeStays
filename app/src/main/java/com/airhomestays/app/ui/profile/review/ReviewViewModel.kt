package com.airhomestays.app.ui.profile.review

import android.widget.TextView
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.airhomestays.app.GetPendingUserReviewQuery
import com.airhomestays.app.GetPendingUserReviewsQuery
import com.airhomestays.app.GetUserReviewsQuery
import com.airhomestays.app.R
import com.airhomestays.app.ViewListingDetailsQuery
import com.airhomestays.app.WriteUserReviewMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.ListingInitData
import javax.inject.Inject

class ReviewViewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<ReviewNavigator>(dataManager, resourceProvider) {


    var aboutYouResult = MutableLiveData<Listing<GetUserReviewsQuery.Result>>()
    var byYouResult = MutableLiveData<Listing<GetUserReviewsQuery.Result>>()
    var pendingResult= MutableLiveData<Listing<GetPendingUserReviewsQuery.Result>>()
    lateinit var aboutYouList: LiveData<PagedList<GetUserReviewsQuery.Result>>
    lateinit var byYouList: LiveData<PagedList<GetUserReviewsQuery.Result>>
    lateinit var pendingList: LiveData<PagedList<GetPendingUserReviewsQuery.Result>>
    val reviewDesc = ObservableField("")
    val networkStateAboutYou: LiveData<NetworkState> = aboutYouResult.switchMap() { it.networkState }
    val netWorkStateByYou : LiveData<NetworkState> = byYouResult.switchMap(){it.networkState}
    val networkStatePending : LiveData<NetworkState> = pendingResult.switchMap(){it.networkState}
    var pendingReviewResult= MutableLiveData<GetPendingUserReviewQuery.Result?>()
    var userRating = ObservableField(0.toFloat())
    val reloadData = MutableLiveData<String>()
    val personCapacity = MutableLiveData<String>()
    val startDate = MutableLiveData<String>()
    val endDate = MutableLiveData<String>()
    val guestMinCount  = MutableLiveData<Int>()
    val guestMaxCount = MutableLiveData<Int>()
    lateinit var retry: () -> Unit
    init {
        reloadData.value = ""
    }
    var reservationId: String = ""

    fun loadAboutYouList(): LiveData<PagedList<GetUserReviewsQuery.Result>> {
        if (!::aboutYouList.isInitialized) {
            aboutYouList = MutableLiveData()
            val buildQuery = GetUserReviewsQuery(
                    ownerType = "other".toOptional()
            )
            aboutYouResult.value = dataManager.getUserReviews(buildQuery, 10)
            aboutYouList = aboutYouResult.switchMap() {
                it.pagedList
            }
        }
        return aboutYouList
    }

    fun loadByYouList(): LiveData<PagedList<GetUserReviewsQuery.Result>> {
       if(!::byYouList.isInitialized){
           byYouList = MutableLiveData()
           val buildQuery = GetUserReviewsQuery(
                   ownerType = "me".toOptional()
                   )
           byYouResult.value = dataManager.getUserReviews(buildQuery,10)
           byYouList = byYouResult.switchMap(){
               it.pagedList
           }
       }
        return byYouList
    }

    fun loadByYouListPending(): LiveData<PagedList<GetPendingUserReviewsQuery.Result>>{
        if(!::pendingList.isInitialized){
            pendingList = MutableLiveData()
            val buildQuery= GetPendingUserReviewsQuery()
            pendingResult.value = dataManager.getPendingUserReviews(buildQuery,10)
            pendingList = pendingResult.switchMap(){
                it.pagedList
            }
        }
        return pendingList
    }


    fun getPendingUserReview(reservationId: Int){
        val buildQuery = GetPendingUserReviewQuery(
                reservationId = reservationId
        )

        compositeDisposable.add(dataManager.getPendingUserReview(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .doOnSubscribe{setIsLoading(true)}
                .doFinally{setIsLoading(false)}
                .subscribe({response->
                    try{
                        if(response.data?.getPendingUserReview?.status==200){
                            val result = response.data?.getPendingUserReview?.result
                            if(result?.listData!=null){
                                pendingReviewResult.value = result
                            }else{
                                navigator.show404Page()
                            }
                        }else{
                            response.data?.getPendingUserReview?.errorMessage.let {
                                navigator.showToast(it ?: resourceProvider.getString(R.string.something_went_wrong))
                            }
                            navigator.show404Page()
                        }
                    }catch (e: Exception){
                         navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                    }
                },
                {
                    handleException(it)
                }))

    }

    fun writeUserReview(listId: Int,receiverId: String,reservationId: Int){
        val buildQuery= WriteUserReviewMutation(
                listId = listId,
                receiverId = receiverId,
                rating = userRating.get()?.toDouble()!!,
                reservationId = reservationId,
                reviewContent = reviewDesc.get()?.trim()?.replace("\\s+".toRegex(), " ")!!
        )
        compositeDisposable.add(dataManager.writeReview(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .doOnSubscribe{setIsLoading(true)}
                .doFinally{setIsLoading(false)}
                .subscribe({response->
                    try{
                        if(response.data?.writeUserReview?.status==200){
                            navigator.moveToScreen(ViewScreen.GO_BACK_AND_REFRESH)
                        }else{
                            response.data?.writeUserReview?.errorMessage.let {
                                navigator.showToast(it ?: resourceProvider.getString(R.string.something_went_wrong))
                            }
                        }
                    }catch (e: Exception){
                        handleException(e)
                    }
                },{
                    handleException(it)
                }))
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

    fun getMinGuestCount() : Int? {
        return guestMinCount.value
    }

    fun getMaxGuestCount() : Int? {
        return guestMaxCount.value
    }

    fun getListingDetails(view : TextView, listId: Int) {
        val buildQuery : ViewListingDetailsQuery = ViewListingDetailsQuery(
                    listId = listId
        )

        compositeDisposable.add(dataManager.doListingDetailsApiCall(buildQuery)
                    .performOnBackOutOnMain(scheduler)
                    .subscribe( { response ->
                        try {
                            val data = response.data?.viewListing
                            if (data!!.status == 200) {
                                val currency = getCurrencySymbol() + Utils.formatDecimal(getConvertedRate(data.results?.listingData?.currency!!
                                        , data.results?.listingData?.basePrice!!))
                                val photo = ArrayList<String>()
                                photo.add(data.results?.listPhotoName!!)
                                ListingDetails.openListDetailsActivity(view.context, ListingInitData(
                                        data.results?.title!!, photo , data.results?.id!!, data.results?.roomType!!,
                                        data.results?.reviewsStarRating, data.results?.reviewsCount, currency,
                                        0,
                                        selectedCurrency = getUserCurrency(),
                                        currencyBase = getCurrencyBase(),
                                        currencyRate = getCurrencyRates(),
                                        startDate = getStartDate(),
                                        endDate = getEndDate(),
                                        bookingType = data.results?.bookingType!!,
                                        minGuestCount = getMinGuestCount(),
                                        maxGuestCount = getMaxGuestCount(),
                                        isWishList = data.results?.wishListStatus!!
                                ))
                                val dates = data.results!!.blockedDates
                                if(dates != null){
                                    if(dates.size > 0){
                                        dates.forEachIndexed { index, blockedDate ->
                                            if(blockedDate?.calendarStatus.equals("available").not()) {
                                                val timestamp = blockedDate?.blockedDates!!

                                            }
                                        }
                                    }
                                }

                                data.results!!.settingsData!!.forEachIndexed { _, settingsDatum ->
                                    if(settingsDatum?.listsettings!=null){
                                        if(settingsDatum.listsettings!!.settingsType!!.typeName=="bathroomType"){

                                        }
                                    }
                                }




                            } else {
                                navigator.show404Page()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, { handleException(it) }
                    )
            )

    }

    enum class ViewScreen {
        GO_BACK_TO,
        GO_BACK_AND_REFRESH
    }

    fun getByYouInitialized(): Boolean{
        return ::byYouList.isInitialized
    }

    fun getPendingInitialized(): Boolean{
        return ::pendingList.isInitialized
    }

    fun onRefreshByYou(){
        byYouResult.value?.refresh?.invoke()
    }

    fun onRefreshPending(){
        pendingResult.value?.refresh?.invoke()
    }

}
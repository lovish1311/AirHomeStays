package com.airhomestays.app.data.remote

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.rx3.Rx3Apollo
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.airhomestays.app.*
import com.airhomestays.app.data.local.db.AppDatabase
import com.airhomestays.app.data.model.db.Message
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.data.remote.paging.inbox.InboxListDataSourceFactory
import com.airhomestays.app.data.remote.paging.inbox_msg.InboxMsgDataSourceFactory
import com.airhomestays.app.data.remote.paging.listing_review.ReviewDataSourceFactory
import com.airhomestays.app.data.remote.paging.reviews.PendingReviewsListDataSourceFactory
import com.airhomestays.app.data.remote.paging.reviews.ReviewsListDataSourceFactory
import com.airhomestays.app.data.remote.paging.search_listing.SearchListingDataSourceFactory
import com.airhomestays.app.data.remote.paging.trips.TripsDataSourceFactory
import com.airhomestays.app.data.remote.paging.user_review.UserReviewDataSourceFactory
import com.airhomestays.app.data.remote.paging.wishlist.WishListDataSourceFactory
import com.airhomestays.app.data.remote.paging.wishlistgroup.WishListGroupDataSourceFactory
import com.airhomestays.app.util.performAsynReturnSingle
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.vo.Outcome
import com.stripe.android.Stripe
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Suppress("UPPER_BOUND_VIOLATED")
@Singleton
class AppApiHelper @Inject constructor(
    @param:Named("Interceptor") val apolloClient: ApolloClient,
    @param:Named("NoInterceptor") val apolloClientNoInterceptor: ApolloClient,
    val scheduler: Scheduler,
    val application: Application,
    val appDatabase: AppDatabase
) : ApiHelper {


    override fun getListSpecialBlockedDates(request: GetListingSpecialPriceQuery): Single<ApolloResponse<GetListingSpecialPriceQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getUpdateSpecialListBlockedDates(request: UpdateSpecialPriceMutation): Single<ApolloResponse<UpdateSpecialPriceMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun getWishList(query: GetWishListGroupQuery): Single<ApolloResponse<GetWishListGroupQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(query)).performAsynReturnSingle(scheduler)
    }

    override fun doGetStep2ListDetailsQuery(request: Step2ListDetailsQuery): Single<ApolloResponse<Step2ListDetailsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }


    override fun listOfInboxMsg1(query: GetThreadsQuery): Single<ApolloResponse<GetThreadsQuery.Data>> {
         return Rx3Apollo.single(apolloClient.query(query)).performAsynReturnSingle(scheduler)
    }


    // Default Settings
    override fun doGetDefaultSettingApiCall(request: GetDefaultSettingQuery): Single<ApolloResponse<GetDefaultSettingQuery.Data>> {
        return Rx3Apollo.single(apolloClientNoInterceptor.query(request)).performAsynReturnSingle(scheduler)
    }

    // Secure Settings
    override fun doGetSecureSettingApiCall(request: GetSecureSiteSettingsQuery): Single<ApolloResponse<GetSecureSiteSettingsQuery.Data>> {
        return Rx3Apollo.single(apolloClientNoInterceptor.query(request)).performAsynReturnSingle(scheduler)
    }


    // Auth
    override fun doServerLoginApiCall(request: LoginQuery): Single<ApolloResponse<LoginQuery.Data>> {
        return Rx3Apollo.single(apolloClientNoInterceptor.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun doLogoutApiCall(request: LogoutMutation): Single<ApolloResponse<LogoutMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doEmailVerificationApiCall(request: CheckEmailExistsQuery): Single<ApolloResponse<CheckEmailExistsQuery.Data>> {
        return Rx3Apollo.single(apolloClientNoInterceptor.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun doSignupApiCall(request: SignupMutation): Single<ApolloResponse<SignupMutation.Data>> {
        return Rx3Apollo.single(apolloClientNoInterceptor.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doForgotPasswordApiCall(request: ForgotPasswordMutation): Single<ApolloResponse<ForgotPasswordMutation.Data>> {
        return Rx3Apollo.single(apolloClientNoInterceptor.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doForgotPasswordVerificationApiCall(request: ForgotPasswordVerificationQuery): Single<ApolloResponse<ForgotPasswordVerificationQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun doResetPasswordApiCall(request: ResetPasswordMutation): Single<ApolloResponse<ResetPasswordMutation.Data>> {
        return Rx3Apollo.single(apolloClientNoInterceptor.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doSocailLoginApiCall(request: SocialLoginQuery): Single<ApolloResponse<SocialLoginQuery.Data>> {
        return Rx3Apollo.single(apolloClientNoInterceptor.query(request)).performAsynReturnSingle(scheduler)
    }



    //Explore
    override fun getExploreListing(request: GetExploreListingsQuery): Single<ApolloResponse<GetExploreListingsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }
    override fun getPopular(request: GetPopularLocationsQuery): Single<ApolloResponse<GetPopularLocationsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getLocationAutoComplete(location: String): Observable<List<AutocompletePrediction>> {
        return Observable.fromCallable {
            getAutocompleteSearchLocation(location)!!
        }
    }

    private fun getAutocompleteSearchLocation(constraint: CharSequence): List<AutocompletePrediction>? {
        Places.initialize(application.applicationContext, Constants.googleMapKey)
        val placesClient = Places.createClient(application.applicationContext)
        val token = AutocompleteSessionToken.newInstance()
        val autocompleteFilter = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ADDRESS)
                .setQuery(constraint.toString())
                .setSessionToken(token)
                .build()

        var autocompletePredictions: List<AutocompletePrediction>? = null
        return try {
            placesClient.findAutocompletePredictions(autocompleteFilter).addOnSuccessListener(OnSuccessListener {
                autocompletePredictions = it.autocompletePredictions
                Timber.tag("AutoComplete Size").w("AutoComplete Size=%s", autocompletePredictions!!.size)
            }).addOnFailureListener(OnFailureListener {
                it.printStackTrace()
            })
            autocompletePredictions
        }catch (e: RuntimeExecutionException) {
            Timber.tag("search").e(e, "Error getting autocomplete prediction API call")
            return null
        }


    }


    // Profile
    override fun doGetProfileDetailsApiCall(request: GetProfileQuery): Single<ApolloResponse<GetProfileQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }



    //CurrencyList
    override fun getCurrencyList(request: GetCurrenciesListQuery): Single<ApolloResponse<GetCurrenciesListQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getVersion(request: GetVersionQuery): Single<ApolloResponse<GetVersionQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }


    //Languages
    override fun doGetLanguagesApiCall(request: UserPreferredLanguagesQuery): Single<ApolloResponse<UserPreferredLanguagesQuery.Data>> {
        return Rx3Apollo.single(apolloClientNoInterceptor.query(request)).performAsynReturnSingle(scheduler)
    }



    //EditProfile
    override fun doEditProfileApiCall(request: EditProfileMutation): Single<ApolloResponse<EditProfileMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun contactSupport(request: ContactSupportQuery): Single<ApolloResponse<ContactSupportQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getUserBanStatus(request: UserBanStatusQuery): Observable<ApolloResponse<UserBanStatusQuery.Data>> {
        return Rx3Apollo.flowable(apolloClient.query(request)).toObservable()
    }



    //Cancellation
    override fun getCancellationDetails(request: CancellationDataQuery): Single<ApolloResponse<CancellationDataQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun cancelReservation(request: CancelReservationMutation): Single<ApolloResponse<CancelReservationMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun createReportUser(request: CreateReportUserMutation): Single<ApolloResponse<CreateReportUserMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }



    // User Profile
    override fun getUserProfile(request: ShowUserProfileQuery): Single<ApolloResponse<ShowUserProfileQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun listOfUserReview(builder: UserReviewsQuery, pageSize: Int): Listing<UserReviewsQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = UserReviewDataSourceFactory(apolloClient, builder, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = sourceFactory.sourceLiveData.switchMap {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }



    //Stripe Token
    override fun createToken(card: Card): MutableLiveData<Outcome<Token>> {
        val ApolloResponse = MutableLiveData<Outcome<Token>>()
        val stripe =  Stripe(application, Constants.stripePublishableKey)
        ApolloResponse.value = Outcome.loading(true)
        return ApolloResponse
    }

    //Payment
    override fun createReservation(request: CreateReservationMutation): Single<ApolloResponse<CreateReservationMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun getUnreadCount(query: GetUnReadCountQuery): Observable<ApolloResponse<GetUnReadCountQuery.Data>> {
        return Rx3Apollo.flowable(apolloClient.query(query)).toObservable()
    }

    //Trips
    override fun getTripsDetails(request: GetAllReservationQuery): Single<ApolloResponse<GetAllReservationQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getReservationDetails(request: GetReservationQuery): Single<ApolloResponse<GetReservationQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }


    // View Listing
    override fun doListingDetailsApiCall(request: ViewListingDetailsQuery): Single<ApolloResponse<ViewListingDetailsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getBillingCalculation(request: GetBillingCalculationQuery): Single<ApolloResponse<GetBillingCalculationQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun doSimilarListingApiCall(request: GetSimilarListingQuery): Single<ApolloResponse<GetSimilarListingQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun contactHost(request: ContactHostMutation): Single<ApolloResponse<ContactHostMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun createRequestToBook(request: CreateRequestToBookMutation): Single<ApolloResponse<CreateRequestToBookMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun getPropertyReviews(query: GetPropertyReviewsQuery): Single<ApolloResponse<GetPropertyReviewsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(query)).performAsynReturnSingle(scheduler)
    }

    override fun listOfReview(listId: Int, hostId: String, pageSize: Int): Listing<GetPropertyReviewsQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = ReviewDataSourceFactory(apolloClient, listId, hostId, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        val count =  sourceFactory.sourceLiveData.switchMap {
            it.count
        }
        return Listing(
                pagedList = livePagedList,
                networkState = sourceFactory.sourceLiveData.switchMap {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = count
        )
    }



    // Search Listing
    override fun listOfSearchListing(query: SearchListingQuery, pageSize: Int): Listing<SearchListingQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = SearchListingDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = sourceFactory.sourceLiveData.switchMap {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    override fun getSearchListing(request: SearchListingQuery): Single<ApolloResponse<SearchListingQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }



    //Trips
    override fun listOfTripsList(query: GetAllReservationQuery, pageSize: Int): Listing<GetAllReservationQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = TripsDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = sourceFactory.sourceLiveData.switchMap {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    override fun getUserReviews(query: GetUserReviewsQuery, pageSize: Int): Listing<GetUserReviewsQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()
        val sourceFactory = ReviewsListDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = sourceFactory.sourceLiveData.switchMap {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    override fun getPendingUserReviews(query: GetPendingUserReviewsQuery, pageSize: Int): Listing<GetPendingUserReviewsQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()
        val sourceFactory = PendingReviewsListDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = sourceFactory.sourceLiveData.switchMap {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }


    override fun getPendingUserReview(request: GetPendingUserReviewQuery): Single<ApolloResponse<GetPendingUserReviewQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    //  WishList
    override fun listOfWishListGroup(query: GetAllWishListGroupQuery, pageSize: Int): Listing<GetAllWishListGroupQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = WishListGroupDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = sourceFactory.sourceLiveData.switchMap {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    override fun listOfWishList(query: GetWishListGroupQuery, pageSize: Int): Listing<GetWishListGroupQuery.WishList> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = WishListDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = sourceFactory.sourceLiveData.switchMap {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }

    override fun listOfWishListWithoutPage(request: GetAllWishListGroupWithoutPageQuery): Single<ApolloResponse<GetAllWishListGroupWithoutPageQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun createWishListGroup(request: CreateWishListGroupMutation): Single<ApolloResponse<CreateWishListGroupMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun CreateWishList(request: CreateWishListMutation): Single<ApolloResponse<CreateWishListMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun writeReview(mutate: WriteUserReviewMutation): Single<ApolloResponse<WriteUserReviewMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(mutate)).performAsynReturnSingle(scheduler)
    }

    override fun deleteWishListGroup(request: DeleteWishListGroupMutation): Single<ApolloResponse<DeleteWishListGroupMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun updateWishListGroup(request: UpdateWishListGroupMutation): Single<ApolloResponse<UpdateWishListGroupMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun getAllWishListGroup(request: GetAllWishListGroupQuery): Single<ApolloResponse<GetAllWishListGroupQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getWishListGroup(request: GetWishListGroupQuery): Observable<ApolloResponse<GetWishListGroupQuery.Data>> {
        return Rx3Apollo.flowable(apolloClient.query(request)).toObservable()
    }



    // Inbox
    override fun sendMessage(mutate: SendMessageMutation): Single<ApolloResponse<SendMessageMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(mutate)).performAsynReturnSingle(scheduler)
    }

    override fun confirmReservation(request: ConfirmReservationMutation): Single<ApolloResponse<ConfirmReservationMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun setReadMessage(request: ReadMessageMutation): Single<ApolloResponse<ReadMessageMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun getNewMessage(request: GetUnReadThreadCountQuery): Observable<ApolloResponse<GetUnReadThreadCountQuery.Data>> {
       return Rx3Apollo.flowable(apolloClient.query(request)).toObservable()
    }

    override fun listOfInboxMsg(query: GetThreadsQuery, pageSize: Int): Listing<GetThreadsQuery.ThreadItem> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = InboxMsgDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = sourceFactory.sourceLiveData.switchMap {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }


    override fun listOfInbox(query: GetAllThreadsQuery, pageSize: Int): Listing<GetAllThreadsQuery.Result> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(10)
                .build()

        val sourceFactory = InboxListDataSourceFactory(apolloClient, query, Executors.newSingleThreadExecutor())
        val livePagedList = LivePagedListBuilder(sourceFactory, myPagingConfig)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
        val refreshState = sourceFactory.sourceLiveData.switchMap {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = sourceFactory.sourceLiveData.switchMap {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState,
                emptyOrNot = null,
                count = null
        )
    }



    //Email Verification
    override fun sendConfirmationEmail(request: SendConfirmEmailQuery): Single<ApolloResponse<SendConfirmEmailQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun ConfirmCodeApiCall(request: CodeVerificationMutation): Single<ApolloResponse<CodeVerificationMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun SocialLoginVerify(request: SocialLoginVerifyMutation): Single<ApolloResponse<SocialLoginVerifyMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)    }



    //SMS verification
    override fun getPhoneNumber(request: GetEnteredPhoneNoQuery): Single<ApolloResponse<GetEnteredPhoneNoQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getCountryCode(request: GetCountrycodeQuery): Single<ApolloResponse<GetCountrycodeQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun addPhoneNumber(request: AddPhoneNumberMutation): Single<ApolloResponse<AddPhoneNumberMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun verifyPhoneNumber(request: VerifyPhoneNumberMutation): Single<ApolloResponse<VerifyPhoneNumberMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }



    //ClearCache
    override fun clearHttpCache(): Observable<Boolean> {
        return Observable.just(true)
    }

    override fun doGetListingSettings(request: GetListingSettingQuery): Single<ApolloResponse<GetListingSettingQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    //Become Host

    override fun doCreateListing(request: CreateListingMutation): Single<ApolloResponse<CreateListingMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun dogetListingSettings(request: GetListingSettingQuery): Single<ApolloResponse<GetListingSettingQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun doUpdateListingStep2(request: UpdateListingStep2Mutation): Single<ApolloResponse<UpdateListingStep2Mutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doUpdateListingStep3(request: UpdateListingStep3Mutation): Single<ApolloResponse<UpdateListingStep3Mutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doGetListingDetailsStep2Query(request: GetListingDetailsStep2Query): Single<ApolloResponse<GetListingDetailsStep2Query.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun ShowListPhotosQuery(request: ShowListPhotosQuery): Single<ApolloResponse<ShowListPhotosQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun doManageListingSteps(request: ManageListingStepsMutation): Single<ApolloResponse<ManageListingStepsMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doShowListingSteps(request: ShowListingStepsQuery): Single<ApolloResponse<ShowListingStepsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }
    override fun doGetStep1ListingDetailsQuery(request: GetStep1ListingDetailsQuery): Single<ApolloResponse<GetStep1ListingDetailsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun doGetStep3Details(request: GetListingDetailsStep3Query): Single<ApolloResponse<GetListingDetailsStep3Query.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun doRemoveListPhotos(request: RemoveListPhotosMutation): Single<ApolloResponse<RemoveListPhotosMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doManagePublishStatus(request: ManagePublishStatusMutation): Single<ApolloResponse<ManagePublishStatusMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doRemoveListingMutation(request: RemoveListingMutation): Single<ApolloResponse<RemoveListingMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doRemoveMultiPhotosMutation(request: RemoveMultiPhotosMutation): Single<ApolloResponse<RemoveMultiPhotosMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    //Payout Preferences
    override fun getPayoutsMethod(request: GetPaymentMethodsQuery): Single<ApolloResponse<GetPaymentMethodsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun setDefaultPayout(request: SetDefaultPayoutMutation): Single<ApolloResponse<SetDefaultPayoutMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun getPayouts(request: GetPayoutsQuery): Single<ApolloResponse<GetPayoutsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun addPayout(request: AddPayoutMutation): Single<ApolloResponse<AddPayoutMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun setPayout(request: ConfirmPayoutMutation): Single<ApolloResponse<ConfirmPayoutMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun confirmPayout(request: VerifyPayoutMutation): Single<ApolloResponse<VerifyPayoutMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    //Manage Listing
    override fun getManageListings(request: ManageListingsQuery): Single<ApolloResponse<ManageListingsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getListBlockedDates(request: ListBlockedDatesQuery): Single<ApolloResponse<ListBlockedDatesQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getUpdateListBlockedDates(request: UpdateListBlockedDatesMutation): Single<ApolloResponse<UpdateListBlockedDatesMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    //Reservation status
    override fun getReseravtionStatus(request: ReservationStatusMutation): Single<ApolloResponse<ReservationStatusMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun submitForVerification(request: SubmitForVerificationMutation): Single<ApolloResponse<SubmitForVerificationMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    //Feedback
    override fun sendfeedBack(request: SendUserFeedbackMutation): Single<ApolloResponse<SendUserFeedbackMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun confirmPayPalPayment(request: ConfirmPayPalExecuteMutation): Single<ApolloResponse<ConfirmPayPalExecuteMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun getStaticPageContent(request: GetStaticPageContentQuery): Single<ApolloResponse<GetStaticPageContentQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getWhyHostData(request: GetWhyHostDataQuery): Single<ApolloResponse<GetWhyHostDataQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun getDeleteUser(request: DeleteUserMutation): Single<ApolloResponse<DeleteUserMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doDeleteUserApiCall(request: DeleteUserMutation): Single<ApolloResponse<DeleteUserMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doChangePasswordApiCall(request: ChangePasswordMutation): Single<ApolloResponse<ChangePasswordMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun doGetRoomTypeSettingsApiCall(request: GetRoomTypeSettingsQuery): Single<ApolloResponse<GetRoomTypeSettingsQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }

    override fun createSecPayment(request: CreateSecPaymentMutation): Single<ApolloResponse<CreateSecPaymentMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }
  override fun updateSecPayment(request: UpdateSecPaymentMutation): Single<ApolloResponse<UpdateSecPaymentMutation.Data>> {
        return Rx3Apollo.single(apolloClient.mutation(request)).performAsynReturnSingle(scheduler)
    }

    override fun getSecPayment(request: GetSecPaymentQuery): Single<ApolloResponse<GetSecPaymentQuery.Data>> {
        return Rx3Apollo.single(apolloClient.query(request)).performAsynReturnSingle(scheduler)
    }
}

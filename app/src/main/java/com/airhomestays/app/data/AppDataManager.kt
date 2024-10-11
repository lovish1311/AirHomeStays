package com.airhomestays.app.data


import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo3.api.ApolloResponse
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.firebase.messaging.FirebaseMessaging
import com.airhomestays.app.*
import com.airhomestays.app.data.local.db.DbHelper
import com.airhomestays.app.data.local.prefs.PreferencesHelper
import com.airhomestays.app.data.model.db.DefaultListing
import com.airhomestays.app.data.model.db.Message
import com.airhomestays.app.data.remote.ApiHelper
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.util.Event
import com.airhomestays.app.vo.Outcome
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.jetbrains.annotations.Nullable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UPPER_BOUND_VIOLATED")
@Singleton
class AppDataManager @Inject constructor(
        private val mPreferencesHelper: PreferencesHelper,
        private val mDbHelper: DbHelper,
        private val mApiHelper: ApiHelper,
        private val firebaseInstanceId: FirebaseMessaging
) : DataManager {


    override fun getListSpecialBlockedDates(request: GetListingSpecialPriceQuery): Single<ApolloResponse<GetListingSpecialPriceQuery.Data>> {
        return mApiHelper.getListSpecialBlockedDates(request)
    }

    override fun getUpdateSpecialListBlockedDates(request: UpdateSpecialPriceMutation): Single<ApolloResponse<UpdateSpecialPriceMutation.Data>> {
        return mApiHelper.getUpdateSpecialListBlockedDates(request)
    }

    override fun getWishList(query: GetWishListGroupQuery): Single<ApolloResponse<GetWishListGroupQuery.Data>> {
        return mApiHelper.getWishList(query)
    }

    override fun getSearchListing(request: SearchListingQuery): Single<ApolloResponse<SearchListingQuery.Data>> {
        return mApiHelper.getSearchListing(request)
    }

    override fun doGetStep2ListDetailsQuery(request: Step2ListDetailsQuery): Single<ApolloResponse<Step2ListDetailsQuery.Data>> {
        return mApiHelper.doGetStep2ListDetailsQuery(request)
    }

    override fun getPayoutsMethod(request: GetPaymentMethodsQuery): Single<ApolloResponse<GetPaymentMethodsQuery.Data>> {
        return mApiHelper.getPayoutsMethod(request)
    }

    override fun setDefaultPayout(request: SetDefaultPayoutMutation): Single<ApolloResponse<SetDefaultPayoutMutation.Data>> {
        return mApiHelper.setDefaultPayout(request)
    }

    override fun confirmReservation(request: ConfirmReservationMutation): Single<ApolloResponse<ConfirmReservationMutation.Data>> {
        return mApiHelper.confirmReservation(request)
    }

    override fun getPayouts(request: GetPayoutsQuery): Single<ApolloResponse<GetPayoutsQuery.Data>> {
        return mApiHelper.getPayouts(request)
    }

    override fun addPayout(request: AddPayoutMutation): Single<ApolloResponse<AddPayoutMutation.Data>> {
        return mApiHelper.addPayout(request)
    }

    override fun setPayout(request: ConfirmPayoutMutation): Single<ApolloResponse<ConfirmPayoutMutation.Data>> {
        return mApiHelper.setPayout(request)
    }

    override fun confirmPayout(request: VerifyPayoutMutation): Single<ApolloResponse<VerifyPayoutMutation.Data>> {
        return mApiHelper.confirmPayout(request)
    }

    override fun insertDefaultListing(defaultListing: DefaultListing): Observable<Boolean> {
        return mDbHelper.insertDefaultListing(defaultListing)
    }

    override fun listOfInboxMsg1(query: GetThreadsQuery): Single<ApolloResponse<GetThreadsQuery.Data>> {
        return mApiHelper.listOfInboxMsg1(query)
    }

    override fun deleteMessage(): Observable<Boolean> {
        return mDbHelper.deleteMessage()
    }

    override fun loadAllMessage(): DataSource.Factory<Int, Message> {
        return mDbHelper.loadAllMessage()
    }

    override fun listOfWishListWithoutPage(request: GetAllWishListGroupWithoutPageQuery): Single<ApolloResponse<GetAllWishListGroupWithoutPageQuery.Data>> {
        return mApiHelper.listOfWishListWithoutPage(request)
    }

    override fun listOfWishListGroup(query: GetAllWishListGroupQuery, pageSize: Int): Listing<GetAllWishListGroupQuery.Result> {
        return mApiHelper.listOfWishListGroup(query, pageSize)
    }

    override fun listOfWishList(query: GetWishListGroupQuery, pageSize: Int): Listing<GetWishListGroupQuery.WishList> {
        return mApiHelper.listOfWishList(query, pageSize)
    }

    override fun createWishListGroup(request: CreateWishListGroupMutation): Single<ApolloResponse<CreateWishListGroupMutation.Data>> {
        return mApiHelper.createWishListGroup(request)
    }

    override fun CreateWishList(request: CreateWishListMutation): Single<ApolloResponse<CreateWishListMutation.Data>> {
        return mApiHelper.CreateWishList(request)
    }

    override fun writeReview(mutate: WriteUserReviewMutation): Single<ApolloResponse<WriteUserReviewMutation.Data>> {
        return mApiHelper.writeReview(mutate)
    }

    override fun deleteWishListGroup(request: DeleteWishListGroupMutation): Single<ApolloResponse<DeleteWishListGroupMutation.Data>> {
        return mApiHelper.deleteWishListGroup(request)
    }

    override fun updateWishListGroup(request: UpdateWishListGroupMutation): Single<ApolloResponse<UpdateWishListGroupMutation.Data>> {
        return mApiHelper.updateWishListGroup(request)
    }

    override fun getAllWishListGroup(request: GetAllWishListGroupQuery): Single<ApolloResponse<GetAllWishListGroupQuery.Data>> {
        return mApiHelper.getAllWishListGroup(request)
    }

    override fun getWishListGroup(request: GetWishListGroupQuery): Observable<ApolloResponse<GetWishListGroupQuery.Data>> {
        return mApiHelper.getWishListGroup(request)
    }


    override var siteName: String?
        get() = mPreferencesHelper.siteName
        set(siteName) {
            mPreferencesHelper.siteName = siteName
        }
    override var listingApproval: Int
        get() = mPreferencesHelper.listingApproval
        set(value) {
            mPreferencesHelper.listingApproval = value
        }

    override fun getNewMessage(request: GetUnReadThreadCountQuery): Observable<ApolloResponse<GetUnReadThreadCountQuery.Data>> {
        return mApiHelper.getNewMessage(request)
    }

    override fun contactSupport(request: ContactSupportQuery): Single<ApolloResponse<ContactSupportQuery.Data>> {
        return mApiHelper.contactSupport(request)
    }

    override fun isUserLoggedIn(): Boolean {
        return currentUserLoggedInMode != DataManager.LoggedInMode.LOGGED_IN_MODE_LOGGED_OUT.type
    }

    override fun getUserBanStatus(request: UserBanStatusQuery): Observable<ApolloResponse<UserBanStatusQuery.Data>> {
        return mApiHelper.getUserBanStatus(request)
    }

    override fun getCancellationDetails(request: CancellationDataQuery): Single<ApolloResponse<CancellationDataQuery.Data>> {
        return mApiHelper.getCancellationDetails(request)
    }

    override fun cancelReservation(request: CancelReservationMutation): Single<ApolloResponse<CancelReservationMutation.Data>> {
        return mApiHelper.cancelReservation(request)
    }

    override fun listOfUserReview(builder: UserReviewsQuery, pageSize: Int): Listing<UserReviewsQuery.Result> {
        return mApiHelper.listOfUserReview(builder, pageSize)
    }

    override fun createReportUser(request: CreateReportUserMutation): Single<ApolloResponse<CreateReportUserMutation.Data>> {
        return mApiHelper.createReportUser(request)
    }

    override fun getUserProfile(request: ShowUserProfileQuery): Single<ApolloResponse<ShowUserProfileQuery.Data>> {
        return mApiHelper.getUserProfile(request)
    }

    override fun contactHost(request: ContactHostMutation): Single<ApolloResponse<ContactHostMutation.Data>> {
        return mApiHelper.contactHost(request)
    }
    override fun createRequestToBook(request: CreateRequestToBookMutation): Single<ApolloResponse<CreateRequestToBookMutation.Data>> {
        return mApiHelper.createRequestToBook(request)
    }

    override fun setReadMessage(request: ReadMessageMutation): Single<ApolloResponse<ReadMessageMutation.Data>> {
        return mApiHelper.setReadMessage(request)
    }

    override fun createToken(card: Card): MutableLiveData<Outcome<Token>> {
        return mApiHelper.createToken(card)
    }

    override fun createReservation(request: CreateReservationMutation): Single<ApolloResponse<CreateReservationMutation.Data>> {
        return mApiHelper.createReservation(request)
    }

    override fun getUnreadCount(query: GetUnReadCountQuery): Observable<ApolloResponse<GetUnReadCountQuery.Data>> {
        return mApiHelper.getUnreadCount(query)
    }

    override fun sendMessage(mutate: SendMessageMutation): Single<ApolloResponse<SendMessageMutation.Data>> {
        return mApiHelper.sendMessage(mutate)
    }

    override fun listOfInboxMsg(query: GetThreadsQuery, pageSize: Int): Listing<GetThreadsQuery.ThreadItem> {
        return mApiHelper.listOfInboxMsg(query, pageSize)
    }

    override fun listOfInbox(query: GetAllThreadsQuery, pageSize: Int): Listing<GetAllThreadsQuery.Result> {
        return mApiHelper.listOfInbox(query, pageSize)
    }

    override fun listOfTripsList(query: GetAllReservationQuery, pageSize: Int): Listing<GetAllReservationQuery.Result> {
        return mApiHelper.listOfTripsList(query, pageSize)
    }

    override fun getUserReviews(query: GetUserReviewsQuery, pageSize: Int): Listing<GetUserReviewsQuery.Result> {
        return mApiHelper.getUserReviews(query, pageSize)
    }

    override fun getPendingUserReviews(query: GetPendingUserReviewsQuery, pageSize: Int): Listing<GetPendingUserReviewsQuery.Result> {
        return mApiHelper.getPendingUserReviews(query, pageSize)
    }

    override fun getPendingUserReview(request: GetPendingUserReviewQuery): Single<ApolloResponse<GetPendingUserReviewQuery.Data>> {
        return mApiHelper.getPendingUserReview(request)
    }

    override fun getTripsDetails(request: GetAllReservationQuery): Single<ApolloResponse<GetAllReservationQuery.Data>> {
        return mApiHelper.getTripsDetails(request)
    }



    override fun clearHttpCache(): Observable<Boolean> {
        return mApiHelper.clearHttpCache()
    }

    override fun getLocationAutoComplete(location: String): Observable<List<AutocompletePrediction>> {
        return mApiHelper.getLocationAutoComplete(location)
    }

    override var currentUserFirstName: String?
        get() = mPreferencesHelper.currentUserFirstName
        set(firstName) {
            mPreferencesHelper.currentUserFirstName = firstName
        }

    override var currentUserLastName: String?
       get() = mPreferencesHelper.currentUserLastName
        set(lastName) {
            mPreferencesHelper.currentUserLastName = lastName
        }

    override var isUserFromDeepLink: Boolean
        get() = mPreferencesHelper.isUserFromDeepLink
        set(isUserFromDeepLink) {
            mPreferencesHelper.isUserFromDeepLink = isUserFromDeepLink
        }

    override var firebaseToken: String?
        get() = mPreferencesHelper.firebaseToken
        set(firebaseToken) {
            mPreferencesHelper.firebaseToken = firebaseToken
        }

    override var accessToken: String?
        get() = mPreferencesHelper.accessToken
        set(accessToken) {
            mPreferencesHelper.accessToken = accessToken
        }

    override var currentUserEmail: String?
        get() = mPreferencesHelper.currentUserEmail
        set(email) {
            mPreferencesHelper.currentUserEmail = email
        }

    override var currentUserId: String?
        get() = mPreferencesHelper.currentUserId
        set(userId) {
            Log.e("TAG", "reportUser: 2 "+userId )
            mPreferencesHelper.currentUserId = userId
        }

    override val currentUserLoggedInMode: Int
        get() = mPreferencesHelper.currentUserLoggedInMode

    override var currentUserName: String?
        get() = mPreferencesHelper.currentUserName
        set(userName) {
            mPreferencesHelper.currentUserName = userName
        }

    override var currentUserProfilePicUrl: String?
        get() = mPreferencesHelper.currentUserProfilePicUrl
        set(profilePicUrl) {
            mPreferencesHelper.currentUserProfilePicUrl = profilePicUrl
        }

    override var currentUserCurrency: String?
        get() = mPreferencesHelper.currentUserCurrency
        set(currency) {
            mPreferencesHelper.currentUserCurrency = currency
        }

    override var currencyBase: String?
        get() = mPreferencesHelper.currencyBase
        set(currencyBase) {
            mPreferencesHelper.currencyBase = currencyBase
        }

    override var currencyRates: String?
        get() = mPreferencesHelper.currencyRates
        set(currencyRates) {
            mPreferencesHelper.currencyRates = currencyRates
        }

    override var isDOB: Boolean?
        get() = mPreferencesHelper.isDOB//To change initializer of created properties use File | Settings | File Templates.
        set(isDOB) {
            mPreferencesHelper.isDOB = isDOB
        }

    override var isPhoneVerified: Boolean?
        get() = mPreferencesHelper.isPhoneVerified
        set(value) {
            mPreferencesHelper.isPhoneVerified = value
        }

    override var isIdVerified: Boolean?
        get() = mPreferencesHelper.isIdVerified
        set(value) {
            mPreferencesHelper.isIdVerified = value
        }

    override var isEmailVerified: Boolean?
        get() = mPreferencesHelper.isEmailVerified
        set(value) {
            mPreferencesHelper.isEmailVerified = value
        }

    override var isFBVerified: Boolean?
        get() = mPreferencesHelper.isFBVerified
        set(value) {
            mPreferencesHelper.isFBVerified = value
        }

    override var isGoogleVerified: Boolean?
        get() = mPreferencesHelper.isGoogleVerified
        set(value) {
            mPreferencesHelper.isGoogleVerified = value
        }

    override var haveNotification: Boolean
        get() = mPreferencesHelper.haveNotification
        set(value) {
            mPreferencesHelper.haveNotification = value
        }

    override var confirmCode: String?
        get() = mPreferencesHelper.confirmCode
        set(value) {
            mPreferencesHelper.confirmCode = value
        }

    override var currentUserPhoneNo: String?
        get() = mPreferencesHelper.currentUserPhoneNo
        set(value) {
            mPreferencesHelper.currentUserPhoneNo = value
        }
    override var currentPhoneNo: String?
        get() = mPreferencesHelper.currentPhoneNo
        set(value) {
            mPreferencesHelper.currentPhoneNo = value
        }
    override var phoneNoType: String?
        get() = mPreferencesHelper.phoneNoType
        set(value) {
            mPreferencesHelper.phoneNoType = value
        }
    override var countryCode: String?
        get() = mPreferencesHelper.countryCode
        set(value) {
            mPreferencesHelper.countryCode = value
        }
    override var currentUserType: String?
        get() = mPreferencesHelper.currentUserType
        set(value) {
            mPreferencesHelper.currentUserType = value
        }

    override var isListAdded: Boolean?
        get() = mPreferencesHelper.isListAdded
        set(value) {
            mPreferencesHelper.isListAdded = value
        }

    override var isHostOrGuest: Boolean
        get() = mPreferencesHelper.isHostOrGuest
        set(value) {
            mPreferencesHelper.isHostOrGuest = value
        }

    override var adminCurrency: String?
        get() = mPreferencesHelper.adminCurrency
        set(value) {
            mPreferencesHelper.adminCurrency = value
        }

    override var prefTheme: String?
        get() = mPreferencesHelper.prefTheme
        set(value) {
            mPreferencesHelper.prefTheme = value
        }

    override fun dogetListingSettings(request: GetListingSettingQuery): Single<ApolloResponse<GetListingSettingQuery.Data>> {
        return mApiHelper.dogetListingSettings(request)
    }

    override fun getPref(): SharedPreferences {
        return mPreferencesHelper.getPref()
    }

    override fun doServerLoginApiCall(request: LoginQuery): Single<ApolloResponse<LoginQuery.Data>> {
        return mApiHelper.doServerLoginApiCall(request)
    }

    override fun doLogoutApiCall(request: LogoutMutation): Single<ApolloResponse<LogoutMutation.Data>> {
        return mApiHelper.doLogoutApiCall(request)
    }

    override fun doEmailVerificationApiCall(request: CheckEmailExistsQuery): Single<ApolloResponse<CheckEmailExistsQuery.Data>> {
        return mApiHelper.doEmailVerificationApiCall(request)
    }

    override fun setCurrentUserLoggedInMode(mode: DataManager.LoggedInMode) {
        mPreferencesHelper.setCurrentUserLoggedInMode(mode)
    }

    override fun doGetProfileDetailsApiCall(request: GetProfileQuery): Single<ApolloResponse<GetProfileQuery.Data>> {
        return mApiHelper.doGetProfileDetailsApiCall(request)
    }

    override fun getPropertyReviews(query: GetPropertyReviewsQuery): Single<ApolloResponse<GetPropertyReviewsQuery.Data>> {
        return mApiHelper.getPropertyReviews(query)
    }

    override fun doEditProfileApiCall(request: EditProfileMutation): Single<ApolloResponse<EditProfileMutation.Data>> {
        return mApiHelper.doEditProfileApiCall(request)
    }

    override fun doSignupApiCall(request: SignupMutation): Single<ApolloResponse<SignupMutation.Data>> {
        return mApiHelper.doSignupApiCall(request)
    }

    override fun doForgotPasswordApiCall(request: ForgotPasswordMutation): Single<ApolloResponse<ForgotPasswordMutation.Data>> {
        return mApiHelper.doForgotPasswordApiCall(request)
    }

    override fun doForgotPasswordVerificationApiCall(request: ForgotPasswordVerificationQuery): Single<ApolloResponse<ForgotPasswordVerificationQuery.Data>> {
        return mApiHelper.doForgotPasswordVerificationApiCall(request)
    }

    override fun doResetPasswordApiCall(request: ResetPasswordMutation): Single<ApolloResponse<ResetPasswordMutation.Data>> {
        return mApiHelper.doResetPasswordApiCall(request)
    }

    override fun doSocailLoginApiCall(request: SocialLoginQuery): Single<ApolloResponse<SocialLoginQuery.Data>> {
        return mApiHelper.doSocailLoginApiCall(request)
    }

    override fun doGetLanguagesApiCall(request: UserPreferredLanguagesQuery): Single<ApolloResponse<UserPreferredLanguagesQuery.Data>> {
        return mApiHelper.doGetLanguagesApiCall(request)
    }

    override fun doGetDefaultSettingApiCall(request: GetDefaultSettingQuery): Single<ApolloResponse<GetDefaultSettingQuery.Data>> {
        return mApiHelper.doGetDefaultSettingApiCall(request)
    }

    override fun doGetSecureSettingApiCall(request: GetSecureSiteSettingsQuery): Single<ApolloResponse<GetSecureSiteSettingsQuery.Data>> {
        return mApiHelper.doGetSecureSettingApiCall(request)
    }

    override fun doSimilarListingApiCall(request: GetSimilarListingQuery): Single<ApolloResponse<GetSimilarListingQuery.Data>> {
        return mApiHelper.doSimilarListingApiCall(request)
    }

    override fun doListingDetailsApiCall(request: ViewListingDetailsQuery): Single<ApolloResponse<ViewListingDetailsQuery.Data>> {
        return mApiHelper.doListingDetailsApiCall(request)
    }

    override fun listOfReview(listId: Int, hostId: String, pageSize: Int): Listing<GetPropertyReviewsQuery.Result> {
        return mApiHelper.listOfReview(listId, hostId, pageSize)
    }

    override fun listOfSearchListing(query: SearchListingQuery, pageSize: Int): Listing<SearchListingQuery.Result> {
        return mApiHelper.listOfSearchListing(query, pageSize)
    }

    override fun getCurrencyList(request: GetCurrenciesListQuery): Single<ApolloResponse<GetCurrenciesListQuery.Data>> {
        return mApiHelper.getCurrencyList(request)
    }

    override fun getVersion(request: GetVersionQuery): Single<ApolloResponse<GetVersionQuery.Data>> {
        return mApiHelper.getVersion(request)
    }

    override fun getBillingCalculation(request: GetBillingCalculationQuery): Single<ApolloResponse<GetBillingCalculationQuery.Data>> {
        return mApiHelper.getBillingCalculation(request)
    }

    override fun getReservationDetails(request: GetReservationQuery): Single<ApolloResponse<GetReservationQuery.Data>> {
        return mApiHelper.getReservationDetails(request)
    }

    override fun sendConfirmationEmail(request: SendConfirmEmailQuery): Single<ApolloResponse<SendConfirmEmailQuery.Data>> {
        return mApiHelper.sendConfirmationEmail(request)
    }

    override fun ConfirmCodeApiCall(request: CodeVerificationMutation): Single<ApolloResponse<CodeVerificationMutation.Data>> {
        return mApiHelper.ConfirmCodeApiCall(request)
    }

    override fun SocialLoginVerify(request: SocialLoginVerifyMutation): Single<ApolloResponse<SocialLoginVerifyMutation.Data>> {
        return mApiHelper.SocialLoginVerify(request)
    }

    override fun generateFirebaseToken(): MutableLiveData<Event<Outcome<String>>> {
        val ApolloResponse = MutableLiveData<Event<Outcome<String>>>()
        ApolloResponse.value = Event(Outcome.loading(true))
        firebaseInstanceId.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.tag("FirebaseToken").d(task.result)
                ApolloResponse.value = Event(Outcome.loading(false))
                ApolloResponse.value = Event(Outcome.success(task.result))
                mPreferencesHelper.firebaseToken = task.result
            }
        }
        firebaseInstanceId.token.addOnFailureListener { exception: Exception ->
            Timber.tag("firebaseToken - Failure").d(exception)
            mPreferencesHelper.firebaseToken = null
            ApolloResponse.value = Event(Outcome.loading(false))
            ApolloResponse.value = Event(Outcome.error(exception))
        }
        firebaseInstanceId.token.addOnCanceledListener {
            Timber.tag("firebasetoken").d("canceled")
        }
        return ApolloResponse
    }

    override fun setUserAsLoggedOut() {
        updateUserInfo(null,
                null,
                DataManager.LoggedInMode.LOGGED_IN_MODE_LOGGED_OUT,
                null,
                null,
                null,
                null,
                null,
                null
        )
        clearPrefs()
    }

    override fun updateUserInfo (
            accezzToken: String?,
            userId: String?,
            loggedInMode: DataManager.LoggedInMode,
            userName: String?,
            email: String?,
            profilePicPath: String?,
            currency: String?,
            language: String?,
            createdAt: String?
    ) {
        accessToken = accezzToken
        currentUserId = userId
        currentUserName = userName
        currentUserEmail = email
        currentUserProfilePicUrl = profilePicPath
        currentUserCurrency = currency
        currentUserCreatedAt = createdAt
        currentUserLanguage =language
        setCurrentUserLoggedInMode(loggedInMode)
    }

    override fun updateAccessToken(accezzToken: String?) {
        accessToken = accezzToken
    }

    override fun updateVerification(
            isPhoneVerification: @Nullable Boolean?,
            isEmailConfirmed: @Nullable Boolean?,
            isIdVerification: @Nullable Boolean?,
            isGoogleConnected: @Nullable Boolean?,
            isFacebookConnected: @Nullable Boolean?) {
        isEmailVerified = isEmailConfirmed
        isFBVerified = isFacebookConnected
        isGoogleVerified = isGoogleConnected
        isIdVerified = isIdVerification
        isPhoneVerified = isPhoneVerification
    }

    override fun clearPrefs() {
        mPreferencesHelper.clearPrefs()
    }

    override var currentUserLanguage: String?
        get() = mPreferencesHelper.currentUserLanguage
        set(language) {
            mPreferencesHelper.currentUserLanguage = language
        }

    override var currentUserCreatedAt: String?
        get() = mPreferencesHelper.currentUserCreatedAt
        set(createdAt) {
            mPreferencesHelper.currentUserCreatedAt = createdAt
        }

    override fun getPhoneNumber(request: GetEnteredPhoneNoQuery): Single<ApolloResponse<GetEnteredPhoneNoQuery.Data>> {
        return mApiHelper.getPhoneNumber(request)
    }

    override fun getCountryCode(request: GetCountrycodeQuery): Single<ApolloResponse<GetCountrycodeQuery.Data>> {
        return mApiHelper.getCountryCode(request)
    }

    override fun addPhoneNumber(request: AddPhoneNumberMutation): Single<ApolloResponse<AddPhoneNumberMutation.Data>> {
        return mApiHelper.addPhoneNumber(request)
    }

    override fun verifyPhoneNumber(request: VerifyPhoneNumberMutation): Single<ApolloResponse<VerifyPhoneNumberMutation.Data>> {
        return mApiHelper.verifyPhoneNumber(request)
    }

    override fun getExploreListing(request: GetExploreListingsQuery): Single<ApolloResponse<GetExploreListingsQuery.Data>> {
        return mApiHelper.getExploreListing(request)
    }
    override fun getPopular(request: GetPopularLocationsQuery): Single<ApolloResponse<GetPopularLocationsQuery.Data>> {
        return mApiHelper.getPopular(request)
    }

    override fun doUpdateListingStep2(request: UpdateListingStep2Mutation): Single<ApolloResponse<UpdateListingStep2Mutation.Data>> {
        return mApiHelper.doUpdateListingStep2(request)
    }

    override fun doUpdateListingStep3(request: UpdateListingStep3Mutation): Single<ApolloResponse<UpdateListingStep3Mutation.Data>> {
        return mApiHelper.doUpdateListingStep3(request)
    }

    override fun ShowListPhotosQuery(request: ShowListPhotosQuery): Single<ApolloResponse<ShowListPhotosQuery.Data>> {
        return mApiHelper.ShowListPhotosQuery(request)
    }

    override fun doGetListingSettings(request: GetListingSettingQuery): Single<ApolloResponse<GetListingSettingQuery.Data>> {
        return mApiHelper.doGetListingSettings(request)
    }

    override fun doCreateListing(request: CreateListingMutation): Single<ApolloResponse<CreateListingMutation.Data>> {
        return mApiHelper.doCreateListing(request)
    }

    override fun doGetListingDetailsStep2Query(request: GetListingDetailsStep2Query): Single<ApolloResponse<GetListingDetailsStep2Query.Data>> {
        return mApiHelper.doGetListingDetailsStep2Query(request)
    }

    override fun doManageListingSteps(request: ManageListingStepsMutation): Single<ApolloResponse<ManageListingStepsMutation.Data>> {
        return mApiHelper.doManageListingSteps(request)
    }

    override fun doShowListingSteps(request: ShowListingStepsQuery): Single<ApolloResponse<ShowListingStepsQuery.Data>> {
        return mApiHelper.doShowListingSteps(request)
    }
    override fun doGetStep1ListingDetailsQuery(request: GetStep1ListingDetailsQuery): Single<ApolloResponse<GetStep1ListingDetailsQuery.Data>> {
        return mApiHelper.doGetStep1ListingDetailsQuery(request)
    }

    override fun doGetStep3Details(request: GetListingDetailsStep3Query): Single<ApolloResponse<GetListingDetailsStep3Query.Data>> {
        return mApiHelper.doGetStep3Details(request)
    }

    override fun doRemoveListPhotos(request: RemoveListPhotosMutation): Single<ApolloResponse<RemoveListPhotosMutation.Data>> {
        return mApiHelper.doRemoveListPhotos(request)
    }

    override fun getReseravtionStatus(request: ReservationStatusMutation): Single<ApolloResponse<ReservationStatusMutation.Data>> {
        return mApiHelper.getReseravtionStatus(request)
    }

    override fun submitForVerification(request: SubmitForVerificationMutation): Single<ApolloResponse<SubmitForVerificationMutation.Data>> {
      return  mApiHelper.submitForVerification(request)
    }

    override fun getListBlockedDates(request: ListBlockedDatesQuery): Single<ApolloResponse<ListBlockedDatesQuery.Data>> {
        return mApiHelper.getListBlockedDates(request)
    }

    override fun getUpdateListBlockedDates(request: UpdateListBlockedDatesMutation): Single<ApolloResponse<UpdateListBlockedDatesMutation.Data>> {
        return mApiHelper.getUpdateListBlockedDates(request)
    }

    override fun getManageListings(request: ManageListingsQuery): Single<ApolloResponse<ManageListingsQuery.Data>> {
        return mApiHelper.getManageListings(request)
    }

    override fun doRemoveListingMutation(request: RemoveListingMutation): Single<ApolloResponse<RemoveListingMutation.Data>> {
        return mApiHelper.doRemoveListingMutation(request)
    }

    override fun doRemoveMultiPhotosMutation(request: RemoveMultiPhotosMutation): Single<ApolloResponse<RemoveMultiPhotosMutation.Data>> {
        return mApiHelper.doRemoveMultiPhotosMutation(request)
    }

    override fun doManagePublishStatus(request: ManagePublishStatusMutation): Single<ApolloResponse<ManagePublishStatusMutation.Data>> {
        return mApiHelper.doManagePublishStatus(request)
    }

    override fun sendfeedBack(request: SendUserFeedbackMutation): Single<ApolloResponse<SendUserFeedbackMutation.Data>> {
        return mApiHelper.sendfeedBack(request)
    }

    override fun confirmPayPalPayment(request: ConfirmPayPalExecuteMutation): Single<ApolloResponse<ConfirmPayPalExecuteMutation.Data>> {
        return mApiHelper.confirmPayPalPayment(request)
    }

    override fun getStaticPageContent(request: GetStaticPageContentQuery): Single<ApolloResponse<GetStaticPageContentQuery.Data>> {
        return mApiHelper.getStaticPageContent(request)
    }

    override fun getWhyHostData(request: GetWhyHostDataQuery): Single<ApolloResponse<GetWhyHostDataQuery.Data>> {
        return mApiHelper.getWhyHostData(request)
    }

    override fun getDeleteUser(request: DeleteUserMutation): Single<ApolloResponse<DeleteUserMutation.Data>> {
        return mApiHelper.getDeleteUser(request)
    }

    override fun doDeleteUserApiCall(request: DeleteUserMutation): Single<ApolloResponse<DeleteUserMutation.Data>> {
        return mApiHelper.getDeleteUser(request)
    }

    override fun doChangePasswordApiCall(request: ChangePasswordMutation): Single<ApolloResponse<ChangePasswordMutation.Data>> {
        return mApiHelper.doChangePasswordApiCall(request)
    }

    override fun doGetRoomTypeSettingsApiCall(request: GetRoomTypeSettingsQuery): Single<ApolloResponse<GetRoomTypeSettingsQuery.Data>> {
        return mApiHelper.doGetRoomTypeSettingsApiCall(request)
    }

    override fun createSecPayment(request: CreateSecPaymentMutation): Single<ApolloResponse<CreateSecPaymentMutation.Data>> {
        return mApiHelper.createSecPayment(request)
    }

    override fun updateSecPayment(request: UpdateSecPaymentMutation): Single<ApolloResponse<UpdateSecPaymentMutation.Data>> {
        return mApiHelper.updateSecPayment(request)
    }

    override fun getSecPayment(request: GetSecPaymentQuery): Single<ApolloResponse<GetSecPaymentQuery.Data>> {
        return mApiHelper.getSecPayment(request)
    }
}

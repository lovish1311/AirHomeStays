package com.airhomestays.app.data.remote

import androidx.lifecycle.MutableLiveData
import com.apollographql.apollo3.api.ApolloResponse
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.airhomestays.app.*
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.vo.Outcome
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@Suppress("UPPER_BOUND_VIOLATED")
interface ApiHelper {

    //ClearHttpCache
    fun clearHttpCache() : Observable<Boolean>

    //Auth
    fun doSocailLoginApiCall(request: SocialLoginQuery): Single<ApolloResponse<SocialLoginQuery.Data>>

    fun doServerLoginApiCall(request: LoginQuery): Single<ApolloResponse<LoginQuery.Data>>

    fun doLogoutApiCall(request: LogoutMutation): Single<ApolloResponse<LogoutMutation.Data>>

    fun doEmailVerificationApiCall(request: CheckEmailExistsQuery): Single<ApolloResponse<CheckEmailExistsQuery.Data>>

    fun doSignupApiCall(request: SignupMutation): Single<ApolloResponse<SignupMutation.Data>>

    fun doForgotPasswordApiCall(request: ForgotPasswordMutation): Single<ApolloResponse<ForgotPasswordMutation.Data>>

    fun doForgotPasswordVerificationApiCall(request: ForgotPasswordVerificationQuery): Single<ApolloResponse<ForgotPasswordVerificationQuery.Data>>

    fun doResetPasswordApiCall(request: ResetPasswordMutation): Single<ApolloResponse<ResetPasswordMutation.Data>>

    //Profile
    fun doGetProfileDetailsApiCall(request: GetProfileQuery): Single<ApolloResponse<GetProfileQuery.Data>>

    fun doEditProfileApiCall(request: EditProfileMutation): Single<ApolloResponse<EditProfileMutation.Data>>

    fun doGetLanguagesApiCall(request: UserPreferredLanguagesQuery): Single<ApolloResponse<UserPreferredLanguagesQuery.Data>>

    //Listing
    fun doGetDefaultSettingApiCall(request: GetDefaultSettingQuery): Single<ApolloResponse<GetDefaultSettingQuery.Data>>

    fun doGetSecureSettingApiCall(request: GetSecureSiteSettingsQuery): Single<ApolloResponse<GetSecureSiteSettingsQuery.Data>>



    //Listing Details
    fun doSimilarListingApiCall(request: GetSimilarListingQuery): Single<ApolloResponse<GetSimilarListingQuery.Data>>

    fun doListingDetailsApiCall(request: ViewListingDetailsQuery): Single<ApolloResponse<ViewListingDetailsQuery.Data>>

    fun listOfReview(listId: Int, hostId: String, pageSize: Int): Listing<GetPropertyReviewsQuery.Result>

    fun contactHost(request: ContactHostMutation): Single<ApolloResponse<ContactHostMutation.Data>>

    fun createRequestToBook(request: CreateRequestToBookMutation): Single<ApolloResponse<CreateRequestToBookMutation.Data>>

    //Search
    fun getSearchListing(request: SearchListingQuery): Single<ApolloResponse<SearchListingQuery.Data>>

    fun listOfSearchListing(query: SearchListingQuery, pageSize: Int): Listing<SearchListingQuery.Result>

    fun getLocationAutoComplete(location: String): Observable<List<AutocompletePrediction>>

    // CurrencyList
    fun getCurrencyList(request: GetCurrenciesListQuery): Single<ApolloResponse<GetCurrenciesListQuery.Data>>

    fun getVersion(request: GetVersionQuery): Single<ApolloResponse<GetVersionQuery.Data>>


    fun getBillingCalculation(request: GetBillingCalculationQuery): Single<ApolloResponse<GetBillingCalculationQuery.Data>>

    fun getReservationDetails(request: GetReservationQuery): Single<ApolloResponse<GetReservationQuery.Data>>

    fun getTripsDetails(request: GetAllReservationQuery): Single<ApolloResponse<GetAllReservationQuery.Data>>

    fun listOfTripsList(query: GetAllReservationQuery, pageSize: Int): Listing<GetAllReservationQuery.Result>


    fun getUserReviews(query: GetUserReviewsQuery,pageSize: Int): Listing<GetUserReviewsQuery.Result>

    fun getPendingUserReviews(query: GetPendingUserReviewsQuery,pageSize: Int):Listing<GetPendingUserReviewsQuery.Result>

    fun getPendingUserReview(request: GetPendingUserReviewQuery) : Single<ApolloResponse<GetPendingUserReviewQuery.Data>>

    fun writeReview(mutate: WriteUserReviewMutation): Single<ApolloResponse<WriteUserReviewMutation.Data>>

    fun getPropertyReviews(query: GetPropertyReviewsQuery): Single<ApolloResponse<GetPropertyReviewsQuery.Data>>

    fun listOfInbox(query: GetAllThreadsQuery, pageSize: Int): Listing<GetAllThreadsQuery.Result>

    fun listOfInboxMsg(query: GetThreadsQuery, pageSize: Int): Listing<GetThreadsQuery.ThreadItem>

    fun listOfInboxMsg1(query: GetThreadsQuery): Single<ApolloResponse<GetThreadsQuery.Data>>

    fun sendMessage(mutate: SendMessageMutation): Single<ApolloResponse<SendMessageMutation.Data>>

    fun getUnreadCount(query: GetUnReadCountQuery): Observable<ApolloResponse<GetUnReadCountQuery.Data>>

    fun createReservation(request: CreateReservationMutation): Single<ApolloResponse<CreateReservationMutation.Data>>

    fun createToken(card: Card): MutableLiveData<Outcome<Token>>

    fun setReadMessage(request: ReadMessageMutation): Single<ApolloResponse<ReadMessageMutation.Data>>

    fun confirmReservation(request: ConfirmReservationMutation): Single<ApolloResponse<ConfirmReservationMutation.Data>>

    fun getNewMessage(request: GetUnReadThreadCountQuery): Observable<ApolloResponse<GetUnReadThreadCountQuery.Data>>

    //User Profile
    fun getUserProfile(request: ShowUserProfileQuery): Single<ApolloResponse<ShowUserProfileQuery.Data>>

    fun createReportUser(request: CreateReportUserMutation): Single<ApolloResponse<CreateReportUserMutation.Data>>

    fun listOfUserReview(builder: UserReviewsQuery, pageSize: Int): Listing<UserReviewsQuery.Result>

    //Cancellation
    fun getCancellationDetails(request: CancellationDataQuery): Single<ApolloResponse<CancellationDataQuery.Data>>

    fun cancelReservation(request: CancelReservationMutation): Single<ApolloResponse<CancelReservationMutation.Data>>

    //User Ban Status
    fun getUserBanStatus(request: UserBanStatusQuery): Observable<ApolloResponse<UserBanStatusQuery.Data>>

    fun contactSupport(request: ContactSupportQuery): Single<ApolloResponse<ContactSupportQuery.Data>>


    fun getCountryCode(request: GetCountrycodeQuery): Single<ApolloResponse<GetCountrycodeQuery.Data>>

    //WishList
    fun createWishListGroup(request: CreateWishListGroupMutation): Single<ApolloResponse<CreateWishListGroupMutation.Data>>

    fun CreateWishList(request: CreateWishListMutation): Single<ApolloResponse<CreateWishListMutation.Data>>

    fun deleteWishListGroup(request: DeleteWishListGroupMutation): Single<ApolloResponse<DeleteWishListGroupMutation.Data>>

    fun updateWishListGroup(request: UpdateWishListGroupMutation): Single<ApolloResponse<UpdateWishListGroupMutation.Data>>

    fun getAllWishListGroup(request: GetAllWishListGroupQuery): Single<ApolloResponse<GetAllWishListGroupQuery.Data>>

    fun getWishListGroup(request: GetWishListGroupQuery): Observable<ApolloResponse<GetWishListGroupQuery.Data>>

    fun listOfWishListGroup(query: GetAllWishListGroupQuery, pageSize: Int): Listing<GetAllWishListGroupQuery.Result>

    fun listOfWishListWithoutPage(request: GetAllWishListGroupWithoutPageQuery): Single<ApolloResponse<GetAllWishListGroupWithoutPageQuery.Data>>

    fun listOfWishList(query: GetWishListGroupQuery, pageSize: Int): Listing<GetWishListGroupQuery.WishList>

    fun getPhoneNumber(request: GetEnteredPhoneNoQuery): Single<ApolloResponse<GetEnteredPhoneNoQuery.Data>>

    fun addPhoneNumber(request: AddPhoneNumberMutation): Single<ApolloResponse<AddPhoneNumberMutation.Data>>

    fun verifyPhoneNumber(request: VerifyPhoneNumberMutation): Single<ApolloResponse<VerifyPhoneNumberMutation.Data>>

    fun getWishList(query: GetWishListGroupQuery): Single<ApolloResponse<GetWishListGroupQuery.Data>>


    //Email Verification
    fun sendConfirmationEmail(request: SendConfirmEmailQuery): Single<ApolloResponse<SendConfirmEmailQuery.Data>>

    fun ConfirmCodeApiCall(request: CodeVerificationMutation): Single<ApolloResponse<CodeVerificationMutation.Data>>

    fun SocialLoginVerify(request : SocialLoginVerifyMutation): Single<ApolloResponse<SocialLoginVerifyMutation.Data>>

    fun getExploreListing(request : GetExploreListingsQuery): Single<ApolloResponse<GetExploreListingsQuery.Data>>

    fun getPopular(request : GetPopularLocationsQuery): Single<ApolloResponse<GetPopularLocationsQuery.Data>>

    //Become a host
    fun doGetListingSettings(request : GetListingSettingQuery): Single<ApolloResponse<GetListingSettingQuery.Data>>

    fun doCreateListing(request : CreateListingMutation): Single<ApolloResponse<CreateListingMutation.Data>>

    //Host Features
    fun dogetListingSettings(request : GetListingSettingQuery): Single<ApolloResponse<GetListingSettingQuery.Data>>

    fun doUpdateListingStep2(request : UpdateListingStep2Mutation): Single<ApolloResponse<UpdateListingStep2Mutation.Data>>

    fun doUpdateListingStep3(request: UpdateListingStep3Mutation): Single<ApolloResponse<UpdateListingStep3Mutation.Data>>

    fun ShowListPhotosQuery(request: ShowListPhotosQuery): Single<ApolloResponse<ShowListPhotosQuery.Data>>

    fun doGetListingDetailsStep2Query(request: GetListingDetailsStep2Query): Single<ApolloResponse<GetListingDetailsStep2Query.Data>>

    fun doManageListingSteps(request : ManageListingStepsMutation): Single<ApolloResponse<ManageListingStepsMutation.Data>>

    fun doShowListingSteps(request: ShowListingStepsQuery): Single<ApolloResponse<ShowListingStepsQuery.Data>>

    fun doGetStep1ListingDetailsQuery(request: GetStep1ListingDetailsQuery): Single<ApolloResponse<GetStep1ListingDetailsQuery.Data>>

    fun doGetStep3Details(request: GetListingDetailsStep3Query): Single<ApolloResponse<GetListingDetailsStep3Query.Data>>

    fun doRemoveListPhotos(request: RemoveListPhotosMutation): Single<ApolloResponse<RemoveListPhotosMutation.Data>>

    fun doManagePublishStatus(request: ManagePublishStatusMutation): Single<ApolloResponse<ManagePublishStatusMutation.Data>>

    fun doRemoveListingMutation(request: RemoveListingMutation): Single<ApolloResponse<RemoveListingMutation.Data>>

    fun doRemoveMultiPhotosMutation(request: RemoveMultiPhotosMutation): Single<ApolloResponse<RemoveMultiPhotosMutation.Data>>

    fun doGetStep2ListDetailsQuery(request: Step2ListDetailsQuery): Single<ApolloResponse<Step2ListDetailsQuery.Data>>

    fun getPayouts(request: GetPayoutsQuery): Single<ApolloResponse<GetPayoutsQuery.Data>>

    fun getPayoutsMethod(request: GetPaymentMethodsQuery): Single<ApolloResponse<GetPaymentMethodsQuery.Data>>

    fun setDefaultPayout(request: SetDefaultPayoutMutation): Single<ApolloResponse<SetDefaultPayoutMutation.Data>>

    fun addPayout(request: AddPayoutMutation): Single<ApolloResponse<AddPayoutMutation.Data>>

    fun setPayout(request: ConfirmPayoutMutation): Single<ApolloResponse<ConfirmPayoutMutation.Data>>

    fun confirmPayout(request: VerifyPayoutMutation): Single<ApolloResponse<VerifyPayoutMutation.Data>>



    //ManageListings
    fun getManageListings(request: ManageListingsQuery): Single<ApolloResponse<ManageListingsQuery.Data>>

    fun getListBlockedDates(request: ListBlockedDatesQuery): Single<ApolloResponse<ListBlockedDatesQuery.Data>>

    fun getUpdateListBlockedDates(request: UpdateListBlockedDatesMutation): Single<ApolloResponse<UpdateListBlockedDatesMutation.Data>>

    fun getListSpecialBlockedDates(request: GetListingSpecialPriceQuery): Single<ApolloResponse<GetListingSpecialPriceQuery.Data>>

    fun getUpdateSpecialListBlockedDates(request: UpdateSpecialPriceMutation): Single<ApolloResponse<UpdateSpecialPriceMutation.Data>>

    //Approve Decline Reservation by Host
    fun getReseravtionStatus(request: ReservationStatusMutation): Single<ApolloResponse<ReservationStatusMutation.Data>>

    fun submitForVerification(request: SubmitForVerificationMutation): Single<ApolloResponse<SubmitForVerificationMutation.Data>>

    //Send Feedback
    fun sendfeedBack(request: SendUserFeedbackMutation):Single<ApolloResponse<SendUserFeedbackMutation.Data>>

    fun confirmPayPalPayment(request: ConfirmPayPalExecuteMutation): Single<ApolloResponse<ConfirmPayPalExecuteMutation.Data>>

    fun getStaticPageContent(request: GetStaticPageContentQuery): Single<ApolloResponse<GetStaticPageContentQuery.Data>>

    fun getWhyHostData(request: GetWhyHostDataQuery): Single<ApolloResponse<GetWhyHostDataQuery.Data>>

    fun getDeleteUser(request: DeleteUserMutation): Single<ApolloResponse<DeleteUserMutation.Data>>

    fun doDeleteUserApiCall(request: DeleteUserMutation): Single<ApolloResponse<DeleteUserMutation.Data>>

    fun doChangePasswordApiCall(request: ChangePasswordMutation): Single<ApolloResponse<ChangePasswordMutation.Data>>

    fun doGetRoomTypeSettingsApiCall(request: GetRoomTypeSettingsQuery): Single<ApolloResponse<GetRoomTypeSettingsQuery.Data>>

    fun createSecPayment(request: CreateSecPaymentMutation): Single<ApolloResponse<CreateSecPaymentMutation.Data>>

    fun getSecPayment(request: GetSecPaymentQuery): Single<ApolloResponse<GetSecPaymentQuery.Data>>
    fun updateSecPayment(request: UpdateSecPaymentMutation): Single<ApolloResponse<UpdateSecPaymentMutation.Data>>
}

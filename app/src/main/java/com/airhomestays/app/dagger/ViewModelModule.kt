package com.airhomestays.app.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.MainViewModelFactory
import com.airhomestays.app.host.calendar.CalendarListingViewModel
import com.airhomestays.app.host.payout.addPayout.AddPayoutViewModel
import com.airhomestays.app.host.payout.editpayout.PayoutViewModel
import com.airhomestays.app.host.photoUpload.Step2ViewModel
import com.airhomestays.app.ui.AuthTokenViewModel
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.auth.birthday.BirthdayViewModel
import com.airhomestays.app.ui.auth.email.EmailVerificationViewModel
import com.airhomestays.app.ui.auth.forgotpassword.ForgotPasswordViewModel
import com.airhomestays.app.ui.auth.login.LoginViewModel
import com.airhomestays.app.ui.auth.name.NameCreationViewModel
import com.airhomestays.app.ui.auth.password.PasswordViewModel
import com.airhomestays.app.ui.auth.resetPassword.ResetPasswordViewModel
import com.airhomestays.app.ui.booking.BookingViewModel
import com.airhomestays.app.ui.cancellation.CancellationViewModel
import com.airhomestays.app.ui.entry.EntryViewModel
import com.airhomestays.app.ui.explore.ExploreViewModel
import com.airhomestays.app.ui.home.HomeViewModel
import com.airhomestays.app.ui.host.HostFinalViewModel
import com.airhomestays.app.ui.host.hostInbox.HostInboxViewModel
import com.airhomestays.app.ui.host.hostInbox.host_msg_detail.HostInboxMsgViewModel
import com.airhomestays.app.ui.host.hostListing.HostListingViewModel
import com.airhomestays.app.ui.host.hostReservation.HostTripsViewModel
import com.airhomestays.app.ui.host.hostReservation.hostContactUs.HostContactUsViewModel
import com.airhomestays.app.ui.host.step_one.StepOneViewModel
import com.airhomestays.app.ui.host.step_three.StepThreeViewModel
import com.airhomestays.app.ui.host.step_two.StepTwoViewModel
import com.airhomestays.app.ui.inbox.InboxBoxViewModel
import com.airhomestays.app.ui.inbox.msg_detail.InboxMsgViewModel
import com.airhomestays.app.ui.listing.ListingDetailsViewModel
import com.airhomestays.app.ui.payment.PaymentViewModel
import com.airhomestays.app.ui.profile.ProfileViewModel
import com.airhomestays.app.ui.profile.about.AboutViewModel
import com.airhomestays.app.ui.profile.confirmPhonenumber.ConfirmPhnoViewModel
import com.airhomestays.app.ui.profile.currency.CurrencyVIewModel
import com.airhomestays.app.ui.profile.edit_profile.EditProfileViewModel
import com.airhomestays.app.ui.profile.feedback.FeedbackViewModel
import com.airhomestays.app.ui.profile.languages.LanguagesViewModel
import com.airhomestays.app.ui.profile.manageAccount.ManageAccountViewModel
import com.airhomestays.app.ui.profile.review.ReviewViewModel
import com.airhomestays.app.ui.profile.setting.SettingViewModel
import com.airhomestays.app.ui.profile.trustAndVerify.TrustAndVerifyViewModel
import com.airhomestays.app.ui.reservation.ReservationViewModel
import com.airhomestays.app.ui.saved.SavedViewModel
import com.airhomestays.app.ui.saved.createlist.CreateListViewModel
import com.airhomestays.app.ui.splash.SplashViewModel
import com.airhomestays.app.ui.trips.TripsViewModel
import com.airhomestays.app.ui.trips.contactus.ContactUsViewModel
import com.airhomestays.app.ui.user_profile.UserProfileViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(EntryViewModel::class)
    abstract fun bindEntryViewModel(authViewModel: EntryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ForgotPasswordViewModel::class)
    abstract fun bindForgotPasswordViewModel(forgotPasswordViewModel: ForgotPasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ResetPasswordViewModel::class)
    abstract fun bindResetPasswordViewModel(resetPasswordViewModel: ResetPasswordViewModel): ViewModel



    @Binds
    @IntoMap
    @ViewModelKey(NameCreationViewModel::class)
    abstract fun bindNameCreationViewModel(nameCreationViewModel: NameCreationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EmailVerificationViewModel::class)
    abstract fun bindEmailVerificationViewModel(emailVerificationViewModel: EmailVerificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PasswordViewModel::class)
    abstract fun bindPasswordViewModel(passwordViewModel: PasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BirthdayViewModel::class)
    abstract fun bindBirthdayViewModel(birthdayViewModel: BirthdayViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel1(profileViewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditProfileViewModel::class)
    abstract fun bindEditProfileViewModel(editProfileViewModel: EditProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LanguagesViewModel::class)
    abstract fun bindLanguagesViewModel(languagesViewModel: LanguagesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    abstract fun bindSplashViewModel(splashViewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ExploreViewModel::class)
    abstract fun bindExploreViewModel(exploreViewModel: ExploreViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ListingDetailsViewModel::class)
    abstract fun bindListingDetailsViewModel(listingDetailsViewModel: ListingDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindhomeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CurrencyVIewModel::class)
    abstract fun bindCurrencyViewModel(currencyVIewModel: CurrencyVIewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BookingViewModel::class)
    abstract fun bindBookingViewModel(bookingViewModel: BookingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReservationViewModel::class)
    abstract fun bindReservationViewModel(reservationViewModel: ReservationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TripsViewModel::class)
    abstract fun bindTripsViewModel(tripsViewModel: TripsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CancellationViewModel::class)
    abstract fun bindCancellationViewModel(cancellationViewModel: CancellationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InboxBoxViewModel::class)
    abstract fun bindInboxBoxViewModel(inboxBoxViewModel: InboxBoxViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InboxMsgViewModel::class)
    abstract fun bindInboxMsgViewModel(inboxMsgViewModel: InboxMsgViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PaymentViewModel::class)
    abstract fun bindPaymentViewModel(paymentViewModel: PaymentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel::class)
    abstract fun bindUserProfileViewModel(userProfileViewModel: UserProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AuthTokenViewModel::class)
    abstract fun bindAuthTokenViewModel(authTokenViewModel: AuthTokenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SavedViewModel::class)
    abstract fun bindSavedViewModel(savedViewModel: SavedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactUsViewModel::class)
    abstract fun bindContactUsViewModel(ContactUsViewModel: ContactUsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConfirmPhnoViewModel::class)
    abstract fun bindConfirmPhnoViewModel(confirmPhnoViewModel: ConfirmPhnoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateListViewModel::class)
    abstract fun bindCreateListViewModel(createListViewModel: CreateListViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: MainViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(TrustAndVerifyViewModel::class)
    abstract fun bindTrustAndVerifyViewModel(trustAndVerifyViewModel : TrustAndVerifyViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StepOneViewModel::class)
    abstract fun bindStepOneViewModel(stepOneViewModel: StepOneViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HostFinalViewModel::class)
    abstract fun bindHostInitialViewModel(hostFinalViewModel: HostFinalViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HostInboxViewModel::class)
    abstract fun bindHostInboxViewModel(hostInboxViewModel: HostInboxViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HostInboxMsgViewModel::class)
    abstract fun bindHostInboxMsgViewModel(hostInboxMsgViewModel: HostInboxMsgViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HostTripsViewModel::class)
    abstract fun bindHostTripsViewModel(hostHostTripsViewModel: HostTripsViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HostContactUsViewModel::class)
    abstract fun bindHostContactUsViewModel(hostContactUsViewModel: HostContactUsViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StepTwoViewModel::class)
    abstract fun bindStepTwoViewModel(stepTwoViewModel : StepTwoViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StepThreeViewModel::class)
    abstract fun bindStepThreeViewModel(stepThreeViewModel : StepThreeViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HostListingViewModel::class)
    abstract fun bindHostListingViewModel(hostListingViewModel: HostListingViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PayoutViewModel::class)
    abstract fun bindPayoutViewModel(payoutViewModel: PayoutViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddPayoutViewModel::class)
    abstract fun bindAddPayoutViewModel(addPayoutViewModel: AddPayoutViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CalendarListingViewModel::class)
    abstract fun bindCalendarListingViewModel(calendarListingViewModel: CalendarListingViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(Step2ViewModel::class)
    abstract fun bindStep2ViewModel(step2ViewModel: Step2ViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingViewModel::class)
    abstract fun bindSettingViewModel(settingViewModel: SettingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AboutViewModel::class)
    abstract fun bindAboutViewModel(aboutViewModel: AboutViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeedbackViewModel::class)
    abstract fun bindFeedbackViewModel(feedbackViewModel: FeedbackViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReviewViewModel::class)
    abstract fun bindReviewViewModel(reviewViewModel: ReviewViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageAccountViewModel::class)
    abstract fun bindManageAccountViewModel(reviewViewModel: ManageAccountViewModel): ViewModel
}

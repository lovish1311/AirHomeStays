package com.airhomestays.app.dagger.builder

import com.airhomestays.app.host.payout.addPayout.AddPayoutActivity
import com.airhomestays.app.host.payout.addPayout.AddPayoutFragmentProvider
import com.airhomestays.app.host.payout.addPayout.StripeWebViewActivity
import com.airhomestays.app.host.payout.editpayout.EditPayoutActivity
import com.airhomestays.app.host.payout.editpayout.EditPayoutFragmentProvider
import com.airhomestays.app.host.photoUpload.Step2FragmentProvider
import com.airhomestays.app.host.photoUpload.UploadPhotoActivity
import com.airhomestays.app.ui.AuthTokenExpireActivity
import com.airhomestays.app.ui.WebViewActivity
import com.airhomestays.app.ui.auth.AuthActivity
import com.airhomestays.app.ui.auth.AuthFragmentProvider
import com.airhomestays.app.ui.booking.BookingActivity
import com.airhomestays.app.ui.booking.BookingFragmentProvider
import com.airhomestays.app.ui.cancellation.CancellationActivity
import com.airhomestays.app.ui.cancellation.CancellationFragmentProvider
import com.airhomestays.app.ui.entry.EntryActivity
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.home.HomeFragmentProvider
import com.airhomestays.app.ui.host.HostFinalActivity
import com.airhomestays.app.ui.host.hostHome.HostHomeActivity
import com.airhomestays.app.ui.host.hostHome.HostHomeFragmentProvider
import com.airhomestays.app.ui.host.hostInbox.host_msg_detail.HostInboxMsgActivity
import com.airhomestays.app.ui.host.hostInbox.host_msg_detail.HostNewInboxMsgActivity
import com.airhomestays.app.ui.host.step_one.StepOneActivity
import com.airhomestays.app.ui.host.step_one.StepOneFragmentProvider
import com.airhomestays.app.ui.host.step_three.StepThreeActivity
import com.airhomestays.app.ui.host.step_three.StepThreeFragmentProvider
import com.airhomestays.app.ui.host.step_two.StepTwoActivity
import com.airhomestays.app.ui.host.step_two.StepTwoFragmentProvider
import com.airhomestays.app.ui.inbox.msg_detail.InboxMsgActivity
import com.airhomestays.app.ui.inbox.msg_detail.NewInboxMsgActivity
import com.airhomestays.app.ui.listing.ListingDetails
import com.airhomestays.app.ui.listing.ListingDetailsFragmentProvider
import com.airhomestays.app.ui.payment.PaymentTypeActivity
import com.airhomestays.app.ui.payment.PaymentTypeFragmentProvider
import com.airhomestays.app.ui.profile.about.AboutActivity
import com.airhomestays.app.ui.profile.about.AboutFragmentProvider
import com.airhomestays.app.ui.profile.about.StaticPageActivity
import com.airhomestays.app.ui.profile.about.why_Host.WhyHostActivity
import com.airhomestays.app.ui.profile.confirmPhonenumber.ConfirmPhnoActivity
import com.airhomestays.app.ui.profile.confirmPhonenumber.ConfirmPhnoFragmentProvider
import com.airhomestays.app.ui.profile.edit_profile.EditProfileActivity
import com.airhomestays.app.ui.profile.edit_profile.EditProfileFragmentProvider
import com.airhomestays.app.ui.profile.feedback.FeedbackActivity
import com.airhomestays.app.ui.profile.manageAccount.ManageAccountActivity
import com.airhomestays.app.ui.profile.manageAccount.ManageAccountFragmentProvider
import com.airhomestays.app.ui.profile.review.ReviewActivity
import com.airhomestays.app.ui.profile.review.ReviewFragmentProvider
import com.airhomestays.app.ui.profile.setting.ChangePasswordActivity
import com.airhomestays.app.ui.profile.setting.SettingActivity
import com.airhomestays.app.ui.profile.setting.SettingFragmentProvider
import com.airhomestays.app.ui.profile.trustAndVerify.TrustAndVerifyActivity
import com.airhomestays.app.ui.reservation.ReservationActivity
import com.airhomestays.app.ui.reservation.ReservationFragmentProvider
import com.airhomestays.app.ui.saved.createlist.CreateListActivity
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.ui.splash.SplashFragmentProvider
import com.airhomestays.app.ui.user_profile.UserProfileActivity
import com.airhomestays.app.ui.user_profile.UserProfileFragmentProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector
    abstract fun bindEntryActivity(): EntryActivity

    @ContributesAndroidInjector(modules = [AuthFragmentProvider::class])
    abstract fun bindAuthActivity(): AuthActivity

    @ContributesAndroidInjector(modules = [SplashFragmentProvider::class])
    abstract fun bindSplashActivity(): SplashActivity

    @ContributesAndroidInjector(modules = [HomeFragmentProvider::class])
    abstract fun bindHomeActivity(): HomeActivity

    @ContributesAndroidInjector(modules = [EditProfileFragmentProvider::class])
    abstract fun bindEditProfileActivity(): EditProfileActivity

    @ContributesAndroidInjector(modules = [ListingDetailsFragmentProvider::class])
    abstract fun bindListingDetailActivity(): ListingDetails

    @ContributesAndroidInjector(modules = [CancellationFragmentProvider::class])
    abstract fun bindCancellationActivity(): CancellationActivity

    @ContributesAndroidInjector(modules = [BookingFragmentProvider::class])
    abstract fun bindBookingActivity(): BookingActivity

    @ContributesAndroidInjector(modules = [ReservationFragmentProvider::class])
    abstract fun bindReservationActivity(): ReservationActivity


    @ContributesAndroidInjector
    abstract fun bindInboxMsgActivity(): InboxMsgActivity

    @ContributesAndroidInjector(modules = [UserProfileFragmentProvider::class])
    abstract fun bindUserProfileActivity(): UserProfileActivity

    @ContributesAndroidInjector
    abstract fun bindAuthTokenExpireActivity(): AuthTokenExpireActivity

    @ContributesAndroidInjector
    abstract fun bindCreateListActivity(): CreateListActivity

    @ContributesAndroidInjector(modules = [ConfirmPhnoFragmentProvider::class])
    abstract fun bindConfirmPhnoActivity(): ConfirmPhnoActivity

    @ContributesAndroidInjector
    abstract fun bindTrustAndVerifyActivity() : TrustAndVerifyActivity

    @ContributesAndroidInjector(modules = [StepOneFragmentProvider::class])
    abstract fun bindStep_one_Activity(): StepOneActivity

    @ContributesAndroidInjector
    abstract fun bindHostFinalActivity(): HostFinalActivity

    @ContributesAndroidInjector(modules = [HostHomeFragmentProvider::class])
    abstract fun bindHostHomeActivity(): HostHomeActivity

    @ContributesAndroidInjector
    abstract fun bindHostInboxMsgActivity(): HostInboxMsgActivity

    @ContributesAndroidInjector
    abstract fun bindNewInboxMsgActivity() : NewInboxMsgActivity

    @ContributesAndroidInjector
    abstract fun bindHostNewInboxMsgActivity() : HostNewInboxMsgActivity

    @ContributesAndroidInjector(modules = [StepTwoFragmentProvider::class])
    abstract fun bindStepTwoActivity() : StepTwoActivity

    @ContributesAndroidInjector(modules = [StepThreeFragmentProvider::class])
    abstract fun bindStepThreeActivity() : StepThreeActivity

    @ContributesAndroidInjector(modules = [EditPayoutFragmentProvider::class])
    abstract fun bindEditPayoutActivity(): EditPayoutActivity

    @ContributesAndroidInjector(modules = [AddPayoutFragmentProvider::class])
    abstract fun bindAddPayoutActivity(): AddPayoutActivity

    @ContributesAndroidInjector(modules = [Step2FragmentProvider::class])
    abstract fun bindUploadPhotoActivity(): UploadPhotoActivity

    @ContributesAndroidInjector(modules = [SettingFragmentProvider::class])
    abstract fun bindSettingActivity(): SettingActivity

    @ContributesAndroidInjector
    abstract fun bindChangePasswordActivity(): ChangePasswordActivity

    @ContributesAndroidInjector
    abstract fun bindFeedbackActivity(): FeedbackActivity

    @ContributesAndroidInjector
    abstract fun bindStripeWebViewActivity(): StripeWebViewActivity

    @ContributesAndroidInjector
    abstract fun bindAboutActivity(): AboutActivity

    @ContributesAndroidInjector
    abstract fun bindWhyHostFragment(): WhyHostActivity

    @ContributesAndroidInjector(modules = [ReviewFragmentProvider::class])
    abstract fun bindReviewActivity(): ReviewActivity


    @ContributesAndroidInjector(modules = [PaymentTypeFragmentProvider::class])
    abstract fun bindPaymentActivity(): PaymentTypeActivity

    @ContributesAndroidInjector
    abstract fun bindWebViewActivity(): WebViewActivity

    @ContributesAndroidInjector
    abstract fun bindStaticContentActivity(): StaticPageActivity

    @ContributesAndroidInjector(modules = [ManageAccountFragmentProvider::class])
    abstract fun bindManageAccountActivity(): ManageAccountActivity
}

package com.airhomestays.app.ui.listing

import com.airhomestays.app.ui.booking.Step4Fragment
import com.airhomestays.app.ui.listing.amenities.AmenitiesBottomFragment
import com.airhomestays.app.ui.listing.amenities.AmenitiesFragment
import com.airhomestays.app.ui.listing.cancellation.CancellationFragment
import com.airhomestays.app.ui.listing.contact_host.ContactHostFragment
import com.airhomestays.app.ui.listing.desc.DescriptionFragment
import com.airhomestays.app.ui.listing.guest.GuestFragment
import com.airhomestays.app.ui.listing.map.MapFragment
import com.airhomestays.app.ui.listing.photo_story.PhotoStoryFragment
import com.airhomestays.app.ui.listing.pricebreakdown.PriceBreakDownFragment
import com.airhomestays.app.ui.listing.report.ReportUserDialog
import com.airhomestays.app.ui.listing.review.ReviewFragment
import com.airhomestays.app.ui.saved.SavedBotomSheet
import com.airhomestays.app.ui.user_profile.report_user.ReportUserFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ListingDetailsFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideAmenitiesFragmentFactory(): AmenitiesFragment

    @ContributesAndroidInjector
    abstract fun provideDescriptionFragmentFactory(): DescriptionFragment

    @ContributesAndroidInjector
    abstract fun provideMapFragmentFactory(): MapFragment

    @ContributesAndroidInjector
    abstract fun provideReviewFragmentFactory(): ReviewFragment

    @ContributesAndroidInjector
    abstract fun provideCancellationFragmentFactory(): CancellationFragment

    @ContributesAndroidInjector
    abstract fun providePhotoStoryFragmentFactory(): PhotoStoryFragment

    @ContributesAndroidInjector
    abstract fun providePriceBreakDownFragmentFactory(): PriceBreakDownFragment

    @ContributesAndroidInjector
    abstract fun provideContactHostFragmentFactory(): ContactHostFragment

    @ContributesAndroidInjector
    abstract fun provideGuestFragmentFactory(): GuestFragment

    @ContributesAndroidInjector
    abstract fun provideInfantFragmentFactory(): InfantFragment

    @ContributesAndroidInjector
    abstract fun providePetsFragmentFactory(): PetsFragment

    @ContributesAndroidInjector
    abstract fun provideAdditionalGuestFragmentFactory(): AdditionalGuestFragment

    @ContributesAndroidInjector
    abstract fun provideSavedBottomFactory(): SavedBotomSheet

    @ContributesAndroidInjector
    abstract fun provideAmenitiesBottomFragmentFactory(): AmenitiesBottomFragment

    @ContributesAndroidInjector
    abstract fun provideReportUserFragment(): ReportUserFragment

    @ContributesAndroidInjector
    abstract fun provideDeleteAccountDialogFactory(): ReportUserDialog

    @ContributesAndroidInjector
    abstract fun provideStep4FragmentFactory(): Step4Fragment
}
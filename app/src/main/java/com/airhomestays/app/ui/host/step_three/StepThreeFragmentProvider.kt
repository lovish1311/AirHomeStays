package com.airhomestays.app.ui.host.step_three

import com.airhomestays.app.ui.host.step_three.bookingWindow.BookWindowFragment
import com.airhomestays.app.ui.host.step_three.discount.DiscountPriceFragment
import com.airhomestays.app.ui.host.step_three.guestBooking.GuestBookingFragment
import com.airhomestays.app.ui.host.step_three.guestNotice.GuestNoticeFragment
import com.airhomestays.app.ui.host.step_three.guestReq.GuestReqFragment
import com.airhomestays.app.ui.host.step_three.houseRules.HouseRuleFragment
import com.airhomestays.app.ui.host.step_three.instantBook.InstantBookFragment
import com.airhomestays.app.ui.host.step_three.listingPrice.ListingPriceFragment
import com.airhomestays.app.ui.host.step_three.localLaws.LocalLawsFragment
import com.airhomestays.app.ui.host.step_three.tripLength.TripLengthFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class StepThreeFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideGuestReqFragment() : GuestReqFragment

    @ContributesAndroidInjector
    abstract fun provideHouseRuleFragment() : HouseRuleFragment

    @ContributesAndroidInjector
    abstract fun provideGuestBookingFragment() : GuestBookingFragment

    @ContributesAndroidInjector
    abstract fun provideGuestNoticeFragment() : GuestNoticeFragment

    @ContributesAndroidInjector
    abstract fun provideOptionsSubFragment() : OptionsSubFragment

    @ContributesAndroidInjector
    abstract fun provideBookWindowFragment() : BookWindowFragment

    @ContributesAndroidInjector
    abstract fun provideTripLengthFragment() : TripLengthFragment

    @ContributesAndroidInjector
    abstract fun provideListingPriceFragment() : ListingPriceFragment

    @ContributesAndroidInjector
    abstract fun provideDiscountPriceFragment() : DiscountPriceFragment

    @ContributesAndroidInjector
    abstract fun provideInstantBookFragment() : InstantBookFragment

    @ContributesAndroidInjector
    abstract fun provideLocalLawsFragment() : LocalLawsFragment
}
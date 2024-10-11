package com.airhomestays.app.ui.home

import com.airhomestays.app.ui.explore.ExploreFragment
import com.airhomestays.app.ui.explore.OneTotalPriceBottomSheet
import com.airhomestays.app.ui.explore.filter.FilterFragment
import com.airhomestays.app.ui.explore.guest.GuestFragment
import com.airhomestays.app.ui.explore.map.ListingMapFragment
import com.airhomestays.app.ui.explore.search.SearchLocationFragment
import com.airhomestays.app.ui.inbox.InboxFragment
import com.airhomestays.app.ui.profile.ProfileFragment
import com.airhomestays.app.ui.saved.SavedBotomSheet
import com.airhomestays.app.ui.saved.SavedDetailFragment
import com.airhomestays.app.ui.saved.SavedFragment
import com.airhomestays.app.ui.trips.TripsFragment
import com.airhomestays.app.ui.trips.TripsListFragment
import com.airhomestays.app.ui.trips.contactus.ContactSupport
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class HomeFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideProfileFragmentFactory(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun provideExploreFragmentFactory(): ExploreFragment

    @ContributesAndroidInjector
    abstract fun provideSearchFragmentFactory(): SearchLocationFragment

    @ContributesAndroidInjector
    abstract fun provideFilterFragmentFactory(): FilterFragment

    @ContributesAndroidInjector
    abstract fun provideGuestFragmentFactory(): GuestFragment

    @ContributesAndroidInjector
    abstract fun provideListingMapFragmentFactory(): ListingMapFragment

    @ContributesAndroidInjector
    abstract fun provideTripsFragmentFactory(): TripsFragment

    @ContributesAndroidInjector
    abstract fun provideTripsListFragmentFactory(): TripsListFragment

    @ContributesAndroidInjector
    abstract fun provideInboxFragmentFactory(): InboxFragment

    @ContributesAndroidInjector
    abstract fun provideSavedFragmentFactory(): SavedFragment

    @ContributesAndroidInjector
    abstract fun provideSavedDetailFragmentFactory(): SavedDetailFragment

    @ContributesAndroidInjector
    abstract fun provideSavedBotomSheetFragmentFactory(): SavedBotomSheet
    @ContributesAndroidInjector
    abstract fun provideOneTotalPriceBottomSheetFragmentFactory(): OneTotalPriceBottomSheet

    @ContributesAndroidInjector
    abstract fun provideContactSupportFragmentFactory(): ContactSupport

}
package com.airhomestays.app.ui.host.hostHome

import com.airhomestays.app.host.calendar.CalendarAvailabilityFragment
import com.airhomestays.app.host.calendar.CalendarListingDialog
import com.airhomestays.app.host.calendar.CalendarListingFragment
import com.airhomestays.app.host.calendar.CalendarListingFragment1
import com.airhomestays.app.ui.host.hostInbox.HostInboxFragment
import com.airhomestays.app.ui.host.hostListing.HostListingFragment
import com.airhomestays.app.ui.host.hostListing.HostListingListFragment
import com.airhomestays.app.ui.host.hostReservation.HostTripsFragment
import com.airhomestays.app.ui.host.hostReservation.HostTripsListFragment
import com.airhomestays.app.ui.host.hostReservation.hostContactUs.HostContactSupport
import com.airhomestays.app.ui.host.step_two.PhotoUploadFragment
import com.airhomestays.app.ui.profile.ProfileFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class HostHomeFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideProfileFragmentFactory(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun provideHostInboxFragmentFactory(): HostInboxFragment

    @ContributesAndroidInjector
    abstract fun provideCalendarListingFragmentFactory(): CalendarListingFragment

    @ContributesAndroidInjector
    abstract fun provideHostListingFragment(): HostListingFragment

    @ContributesAndroidInjector
    abstract fun provideHostTripsFragment(): HostTripsFragment

    @ContributesAndroidInjector
    abstract fun provideHostTripsListFragment(): HostTripsListFragment


    @ContributesAndroidInjector
    abstract fun provideCalendarAvailabilityFragmentFactory(): CalendarAvailabilityFragment

    @ContributesAndroidInjector
    abstract fun provideCalendarAvailabilityFragmentFactory1(): CalendarListingFragment1

    @ContributesAndroidInjector
    abstract fun provideCalendarListingDialogFactory(): CalendarListingDialog

    @ContributesAndroidInjector
    abstract fun providePhotoUploadFragmentFactory(): PhotoUploadFragment

    @ContributesAndroidInjector
    abstract fun provideHostContactSupportFragmentFactory(): HostContactSupport

    @ContributesAndroidInjector
    abstract fun provideHostListingListFragmentFactory(): HostListingListFragment
}
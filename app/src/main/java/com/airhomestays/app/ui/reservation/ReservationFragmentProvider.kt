package com.airhomestays.app.ui.reservation

import com.airhomestays.app.ui.reservation.map.ItineraryMapFragment
import com.airhomestays.app.ui.saved.SavedBotomSheet
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ReservationFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideItineraryFragmentFactory(): ItineraryFragment

    @ContributesAndroidInjector
    abstract fun provideReceiptFragmentFactory(): ReceiptFragment

    @ContributesAndroidInjector
    abstract fun provideSavedBotomSheetFactory(): SavedBotomSheet

    @ContributesAndroidInjector
    abstract fun provideMapFragmentFactory(): ItineraryMapFragment
}
package com.airhomestays.app.ui.booking

import com.airhomestays.app.ui.reservation.ItineraryFragment
import com.airhomestays.app.ui.reservation.ReceiptFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class BookingFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideStep1FragmentFactory(): Step1Fragment

    @ContributesAndroidInjector
    abstract fun provideStep2FragmentFactory(): Step2Fragment

    @ContributesAndroidInjector
    abstract fun provideStep3FragmentFactory(): Step3Fragment

    @ContributesAndroidInjector
    abstract fun provideStep4FragmentFactory(): Step4Fragment

    @ContributesAndroidInjector
    abstract fun provideReviewAndPayFragmentFactory(): ReviewAndPayFragment

    @ContributesAndroidInjector
    abstract fun provideItineraryFragmentFactory(): ItineraryFragment

    @ContributesAndroidInjector
    abstract fun provideReceiptFragmentFactory(): ReceiptFragment

}
package com.airhomestays.app.ui.payment

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class PaymentTypeFragmentProvider {
    @ContributesAndroidInjector
    abstract fun provideStripePaymentFactory(): StripePaymentFragment

    @ContributesAndroidInjector
    abstract fun providePaymentDialogOptionsFragment(): PaymentDialogOptionsFragment
}
package com.airhomestays.app.host.payout.addPayout

import com.airhomestays.app.ui.profile.currency.CurrencyDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class AddPayoutFragmentProvider {

    @ContributesAndroidInjector
    abstract fun providePaymentIntroFragmentFactory(): PaymentIntroFragment

    @ContributesAndroidInjector
    abstract fun providePayoutAccountDetailFragmentFactory(): PayoutAccountDetailFragment

    @ContributesAndroidInjector
    abstract fun providePayoutAccountInfoFragmentFactory(): PayoutAccountInfoFragment

    @ContributesAndroidInjector
    abstract fun provideAddBankAccountDetailFragmentFactory(): AddBankAccountDetailFragment

    @ContributesAndroidInjector
    abstract fun providePaymentTypeFragmentFactory(): PaymentTypeFragment

    @ContributesAndroidInjector
    abstract fun providePayoutPaypalDetailsFragmenttFactory(): PayoutPaypalDetailsFragment

    @ContributesAndroidInjector
    abstract fun provideCurrencyDialogFactory(): CurrencyDialog

    @ContributesAndroidInjector
    abstract fun provideBottomSheetFragment(): BottomSheetFragment

}
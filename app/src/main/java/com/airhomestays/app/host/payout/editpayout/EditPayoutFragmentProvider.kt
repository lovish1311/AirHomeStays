package com.airhomestays.app.host.payout.editpayout

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class EditPayoutFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideStep1FragmentFactory(): CountryFragment

}
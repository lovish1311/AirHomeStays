package com.airhomestays.app.ui.cancellation

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class CancellationFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideCancellationFragmentFactory(): CancellationPolicy

}
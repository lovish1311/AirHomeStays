package com.airhomestays.app.ui.trips

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class TripsListFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideTripsListFragment(): TripsListFragment

}
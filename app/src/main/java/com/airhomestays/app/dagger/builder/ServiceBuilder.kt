package com.airhomestays.app.dagger.builder

import com.airhomestays.app.firebase.MyFirebaseMessagingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ServiceBuilder {

    @ContributesAndroidInjector
    abstract fun bindFirebaseService(): MyFirebaseMessagingService

}
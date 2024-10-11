package com.airhomestays.app.dagger.component

import android.app.Application
import com.airhomestays.app.MainApp
import com.airhomestays.app.dagger.builder.ActivityBuilder
import com.airhomestays.app.dagger.builder.ServiceBuilder
import com.airhomestays.app.dagger.module.AppModule
import com.airhomestays.app.data.local.prefs.PreferencesHelper
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ActivityBuilder::class, ServiceBuilder::class])
interface AppComponent : AndroidInjector<MainApp> {

    override fun inject(app: MainApp)

    fun getAppPreferencesHelper(): PreferencesHelper

//    fun inject(firebase: MyFirebaseMessagingService)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}

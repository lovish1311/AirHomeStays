package com.airhomestays.app.ui.splash

import com.airhomestays.app.ui.profile.manageAccount.DeleteAccountDialog
import com.airhomestays.app.ui.profile.manageAccount.ForceUpdateDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class SplashFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideForceUpdateDialogFactory(): ForceUpdateDialog
}
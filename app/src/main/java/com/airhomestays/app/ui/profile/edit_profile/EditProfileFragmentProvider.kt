package com.airhomestays.app.ui.profile.edit_profile

import com.airhomestays.app.ui.profile.currency.CurrencyDialog
import com.airhomestays.app.ui.profile.languages.LanguagesDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class EditProfileFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideEditProfileFragmentFactory(): EditProfileFragment

    @ContributesAndroidInjector
    abstract fun provideLanguagesDialogFactory(): LanguagesDialog

    @ContributesAndroidInjector
    abstract fun provideCurrencyDialogFactory(): CurrencyDialog

    @ContributesAndroidInjector
    abstract fun provideEditProfileLocationFactory(): EditProfileLocation

}
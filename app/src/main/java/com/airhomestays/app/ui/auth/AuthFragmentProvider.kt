package com.airhomestays.app.ui.auth

import com.airhomestays.app.ui.auth.birthday.BirthdayFragment
import com.airhomestays.app.ui.auth.email.EmailFragment
import com.airhomestays.app.ui.auth.forgotpassword.ForgotPasswordFragment
import com.airhomestays.app.ui.auth.login.LoginFragment
import com.airhomestays.app.ui.auth.name.NameCreationFragment
import com.airhomestays.app.ui.auth.password.PasswordFragment
import com.airhomestays.app.ui.auth.resetPassword.ResetPasswordFragment
import com.airhomestays.app.ui.auth.signup.SignupFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class AuthFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideSignupFragmentFactory(): SignupFragment

    @ContributesAndroidInjector
    abstract fun provideNameCreationFragmentFactory(): NameCreationFragment

    @ContributesAndroidInjector
    abstract fun provideEmailFragmentFactory(): EmailFragment

    @ContributesAndroidInjector
    abstract fun providePasswordFragmentFactory(): PasswordFragment

    @ContributesAndroidInjector
    abstract fun provideBirthdayFragmentFactory(): BirthdayFragment

    @ContributesAndroidInjector
    abstract fun provideLoginFragmentFactory(): LoginFragment

    @ContributesAndroidInjector
    abstract fun provideForgotPasswordFragmentFactory(): ForgotPasswordFragment

    @ContributesAndroidInjector
    abstract fun provideResetPasswordFragmentFactory(): ResetPasswordFragment
}

package com.airhomestays.app.ui.profile.review

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ReviewFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideReviewAboutMeFragmentFactory(): FragmentReviewAboutYou

    @ContributesAndroidInjector
    abstract fun provideReviewByMeFactory(): FragmentReviewByYou

    @ContributesAndroidInjector
    abstract fun provideReviewByYouPast(): FragmentReviewByYouPast

    @ContributesAndroidInjector
    abstract fun provideReviewByYouPending(): FragmentReviewByYouPending

    @ContributesAndroidInjector
    abstract fun provideFragmentWriteReview(): FragmentWriteReview

}
package com.airhomestays.app.dagger.module

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.google.android.gms.tasks.Task
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.airhomestays.app.BuildConfig
import com.airhomestays.app.Constants
import com.airhomestays.app.dagger.ViewModelModule
import com.airhomestays.app.data.AppDataManager
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.local.db.AppDatabase
import com.airhomestays.app.data.local.db.AppDbHelper
import com.airhomestays.app.data.local.db.DbHelper
import com.airhomestays.app.data.local.prefs.AppPreferencesHelper
import com.airhomestays.app.data.local.prefs.PreferencesHelper
import com.airhomestays.app.data.remote.ApiHelper
import com.airhomestays.app.data.remote.AppApiHelper
import com.airhomestays.app.util.CustomInterceptor
import com.airhomestays.app.util.resource.BaseResourceProvider
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.AppScheduler
import com.airhomestays.app.util.rx.Scheduler
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideApiHelper(appApiHelper: AppApiHelper): ApiHelper {
        return appApiHelper
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideDataManager(appDataManager: AppDataManager): DataManager {
        return appDataManager
    }

    @Provides
    fun providePreferenceName(): String {
        return Constants.PREF_NAME
    }

    @Provides
    @Singleton
    fun providePreferencesHelper(appPreferencesHelper: AppPreferencesHelper): PreferencesHelper {
        return appPreferencesHelper
    }

    @Provides
    @Singleton
    @Named("Interceptor")
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(Constants.URL)
            .okHttpClient(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @Named("NoInterceptor")
    fun provideApolloClientNoInterceptor(httpLoggingInterceptor: HttpLoggingInterceptor
    ): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(Constants.URL)
            .okHttpClient(OkHttpClient()
                .newBuilder()
                .addInterceptor(httpLoggingInterceptor)
                .build())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        customInterceptor: CustomInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(customInterceptor)
            .build()
    }


    @Provides
    @Singleton
    fun provideHeaderInterceptor(preferencesHelper: PreferencesHelper): CustomInterceptor {
        return CustomInterceptor(preferencesHelper)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        }
        //interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @Provides
    @Singleton
    fun provideFirebaseInstance(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    fun provideScheduler(): Scheduler {
        return AppScheduler()
    }

    @Provides
    @Singleton
    fun provideResource(resourceProvider: ResourceProvider): BaseResourceProvider {
        return resourceProvider
    }

    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, Constants.DB_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideDbHelper(appDbHelper: AppDbHelper): DbHelper {
        return appDbHelper
    }
}
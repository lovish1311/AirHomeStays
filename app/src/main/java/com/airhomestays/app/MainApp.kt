package com.airhomestays.app

import android.app.*
import android.content.Context
import android.os.Build
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.facebook.FacebookSdk
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import com.airhomestays.app.dagger.component.DaggerAppComponent
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.util.LocaleHelper
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.stripe.android.PaymentConfiguration
import dagger.android.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import net.gotev.uploadservice.UploadServiceConfig
import timber.log.Timber
import javax.inject.Inject


class MainApp : DaggerApplication() {

    companion object {
        const val notificationChannelID = "TestChannel"
    }

    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val applicationInjectors = DaggerAppComponent.builder().application(this).build()

    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var scheduler: Scheduler
    fun activityInjector(): DispatchingAndroidInjector<Activity>? {
        return activityDispatchingAndroidInjector
    }

    fun serviceInjector(): AndroidInjector<Service> {
        return dispatchingServiceInjector
    }

   /* override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }*/
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(notificationChannelID, "TestApp Channel", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
    }
    override fun attachBaseContext(cxt: Context) {
        /*val resources = cxt.resources
        val configuration = Configuration(resources.configuration)
        configuration.uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED
        val baseContext = cxt.createConfigurationContext(configuration)*/
        LocaleHelper.onAttach(cxt)
        super.attachBaseContext(cxt)
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        FacebookSdk.sdkInitialize(applicationContext)
        FirebaseApp.initializeApp(applicationContext)
        UploadServiceConfig.initialize(
            context = this,
            defaultNotificationChannel = notificationChannelID,
            debug = BuildConfig.DEBUG
        )
        createNotificationChannel()
/*        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this)*/

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            startDebugCrashActivity()
        } else {
            startCrashActivity()
        }


        getStripeKey()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return applicationInjectors
    }

    private fun startCrashActivity() {
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(false) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(true) //default: false
            //.minTimeBetweenCrashesMs(2000) //default: 3000
            .restartActivity(SplashActivity::class.java)
            .apply()
    }

    private fun startDebugCrashActivity() {
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(false) //default: true
            .trackActivities(true) //default: false
            //.minTimeBetweenCrashesMs(2000) //default: 3000
            .restartActivity(SplashActivity::class.java)
            .apply()
    }

    private fun getStripeKey() {
        val request = GetSecureSiteSettingsQuery(
            type = "config_settings".toOptional(),
            securityKey = getString(R.string.security_key)
        )

        compositeDisposable.add(dataManager.clearHttpCache()
            .flatMap { dataManager.doGetSecureSettingApiCall(request).toObservable() }
            .performOnBackOutOnMain(scheduler)
            .subscribe( {
                try {
                    for (item in it.data?.getSecureSiteSettings?.results!!) {
                        if (item?.name == "stripePublishableKey") {
                            Constants.stripePublishableKey = item.value!!
                            PaymentConfiguration.init(
                                applicationContext,
                                Constants.stripePublishableKey
                            )
                        }
                    }
                } catch (e: Exception) {
                   e.printStackTrace()
                }
            }, {
                it.printStackTrace()
            }
            ))
    }

}

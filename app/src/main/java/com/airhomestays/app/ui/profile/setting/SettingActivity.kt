package com.airhomestays.app.ui.profile.setting

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivitySettingBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.host.hostHome.HostHomeActivity
import com.airhomestays.app.ui.profile.setting.currency.CurrencyBottomSheet
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.util.LocaleHelper
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.visible
import com.airhomestays.app.viewholderDivider
import com.airhomestays.app.viewholderProfileLists
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class SettingActivity : BaseActivity<ActivitySettingBinding, SettingViewModel>(),
    SettingsNavigator {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_setting
    override val viewModel: SettingViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(SettingViewModel::class.java)
    lateinit var mBinding: ActivitySettingBinding
    var appTheme = " "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        mBinding.actionBar.tvToolbarHeading.text = getString(R.string.setting)
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.ivNavigateup.setOnClickListener {
            finish()
        }
        initView()
        subscribeToLiveData(savedInstanceState)
    }

    private fun initView() {
        mGoogleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        )
    }

    fun subscribeToLiveData(savedInstanceState: Bundle?) {
        viewModel.loadSettingData().observe(this, Observer {
            if(it.isNullOrEmpty().not()){
                Handler(Looper.getMainLooper()).postDelayed({
                    setUp()
                },500)

            }
        })
        viewModel.isEmailUser.observeForever {
            if (mBinding.rvSetting.adapter != null)
                mBinding.rvSetting.requestModelBuild()
        }
        viewModel.appTheme.observe(this, Observer {
            appTheme = it
            if (mBinding.rvSetting.adapter != null)
                mBinding.rvSetting.requestModelBuild()
        })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setUp() {
        viewModel.appTheme.value = viewModel.dataManager.prefTheme.toString()
        mBinding.rvSetting.withModels {
            if (viewModel.isEmailUser.value == true) {
                viewholderProfileLists {
                    id("change_password")
                    name(getString(R.string.change_password))
                    image(R.drawable.ic_profile_change_password)
                    textVisible(true)
                    arrowVisibile(false)
                    onBind { model, view, position ->
                        val textView = view.dataBinding.root.findViewById<TextView>(R.id.ic_currency)
                        textView.visible()
                        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.ic_right_arrow_red),null,null,null)
                        textView.setTextColor(resources.getColor(R.color.colorAccent))
                        textView.text=""
                    }
                    onClick(View.OnClickListener {
                        val intent = Intent(applicationContext, ChangePasswordActivity::class.java)
                        startActivity(intent)
                    })

                }

                viewholderDivider {
                    id("change_password_divider")
                }
            }

            viewholderProfileLists {
                id("language")
                name(getString(R.string.languages))
                image(R.drawable.ic_profile_language)
                textVisible(true)
                arrowVisibile(false)
                currencyText(viewModel.appLanguage.get())
                onClick(View.OnClickListener {
                    viewModel.appLanguage.get().let {
                        CurrencyBottomSheet.newInstance("language")
                            .show(supportFragmentManager, "language")
                    }
                })
            }

            viewholderDivider {
                id("div1")
            }

            viewholderProfileLists {
                id("currency")
                name(getString(R.string.currency))
                image(R.drawable.ic_profile_currency)
                textVisible(true)
                arrowVisibile(false)
                if (BindingAdapters.getCurrencySymbol(viewModel.getCurrencySymbol()) == viewModel.baseCurrency.get()) {
                    currencyText(" ${viewModel.baseCurrency.get()}")
                } else {
                    currencyText("${viewModel.getCurrencySymbol()} ${viewModel.baseCurrency.get()}")
                }
                onClick(View.OnClickListener {
                    viewModel.baseCurrency.get()?.let {
                        CurrencyBottomSheet.newInstance("currency")
                            .show(supportFragmentManager, "currency")
                    }
                })
            }
            viewholderDivider {
                id("div2")
            }
            viewholderProfileLists {
                id("theme")
                name(getString(R.string.theme))
                image(R.drawable.ic_theme)
                textVisible(true)
                arrowVisibile(false)
                if (appTheme == "Auto") {
                    currencyText(getString(R.string.auto))
                } else if (appTheme == "Light") {
                    currencyText(getString(R.string.light))
                } else {
                    currencyText(getString(R.string.dark))
                }
                onClick(View.OnClickListener {
                    CurrencyBottomSheet.newInstance("theme").show(supportFragmentManager, "theme")
                })
            }
            viewholderDivider {
                id("div2")
            }
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.are_you_sure_you_want_to_logout))
            .setPositiveButton(getString(R.string.log_out)) { _, _ -> viewModel.signOut() }
            .setNegativeButton(getString(R.string.CANCEL)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }


    override fun openSplashScreen() {
        if (viewModel.dataManager.isHostOrGuest) {
            val intent = Intent(this, HostHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }

    override fun onRetry() {
        checkNetwork {
            viewModel.loadSettingData()
            viewModel.getCurrency()
            viewModel.getProfileDetails()
        }
    }

    override fun navigateToSplash() {
        LoginManager.getInstance().logOut()
        mGoogleSignInClient?.signOut()
        val intent = Intent(this@SettingActivity, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        this.finish()
    }

    override fun setLocale(key: String) {

        if (key == "en") {
            LocaleHelper.setLocale(applicationContext, "en")
            openSplashScreen()
        } else if (key == "es") {
            LocaleHelper.setLocale(applicationContext, "es")
            openSplashScreen()
        } else if (key == "fr") {
            LocaleHelper.setLocale(applicationContext, "fr")
            openSplashScreen()
        } else if (key == "pt") {
            LocaleHelper.setLocale(applicationContext, "pt")
            openSplashScreen()
        } else if (key == "it") {
            LocaleHelper.setLocale(applicationContext, "it")
            openSplashScreen()
        } else if (key == "ar") {
            LocaleHelper.setLocale(applicationContext, "ar")
            openSplashScreen()
        } else if (key == "iw") {
            LocaleHelper.setLocale(applicationContext, "iw")
            openSplashScreen()
        } else if (key == "he") {
            LocaleHelper.setLocale(applicationContext, "he")
            openSplashScreen()
        }

    }

    override fun finishActivity() {
    }
}
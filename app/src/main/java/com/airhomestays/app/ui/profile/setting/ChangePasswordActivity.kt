package com.airhomestays.app.ui.profile.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityChangePasswordBinding
import com.airhomestays.app.databinding.ActivitySettingBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.host.hostHome.HostHomeActivity
import com.airhomestays.app.ui.profile.setting.currency.CurrencyBottomSheet
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.util.LocaleHelper
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderDivider
import com.airhomestays.app.viewholderProfileLists
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class ChangePasswordActivity: BaseActivity<ActivityChangePasswordBinding, SettingViewModel>(),SettingsNavigator {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_change_password
    override val viewModel: SettingViewModel
        get() = ViewModelProvider(this,mViewModelFactory).get(SettingViewModel::class.java)
    lateinit var mBinding : ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        mBinding.ivClose.setOnClickListener {
            finish()
        }
        setUp()
    }

    fun setUp(){
        mBinding.btnSignup.onClick {
            checkNetwork {
                viewModel.checkPassword()
            }

        }
        mBinding.ivOldPasswordVisibility.onClick {
            showOldPassword()
        }
        mBinding.ivNewPasswordVisibility.onClick {
            showNewPassword()
        }
        mBinding.ivConfirmPasswordVisibility.onClick {
            showConfirmPassword()
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun openSplashScreen() {
        if (viewModel.dataManager.isHostOrGuest){
            val intent = Intent(this, HostHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }else{
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
    override fun onRetry() {
        if(!isNetworkConnected){
            showOffline()
        }
    }

    override fun navigateToSplash() {
    }

    override fun setLocale(key: String) {
    }

    override fun finishActivity() {
       finish()
    }

    fun showOldPassword() {
        if (viewModel.showOldPassword.get() == false) {
            viewModel.showOldPassword.set(true)
            mBinding.ivOldPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_visibility_show
                )
            )
        } else {
            viewModel.showOldPassword.set(false)
            mBinding.ivOldPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_visibility_hide,
                )
            )
        }
    }

    fun showNewPassword() {
        if (viewModel.showNewPassword.get() == false) {
            viewModel.showNewPassword.set(true)
            mBinding.ivNewPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_visibility_show
                )
            )
        } else {
            viewModel.showNewPassword.set(false)
            mBinding.ivNewPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_visibility_hide,
                )
            )
        }
    }

    fun showConfirmPassword() {
        if (viewModel.showConfirmPassword.get() == false) {
            viewModel.showConfirmPassword.set(true)
            mBinding.ivConfirmPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_visibility_show
                )
            )
        } else {
            viewModel.showConfirmPassword.set(false)
            mBinding.ivConfirmPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_visibility_hide,
                )
            )
        }
    }

}
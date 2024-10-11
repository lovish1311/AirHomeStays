package com.airhomestays.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityAuthtokenexpireBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.util.onClick
import javax.inject.Inject

class AuthTokenExpireActivity : BaseActivity<ActivityAuthtokenexpireBinding, AuthTokenViewModel>() {

    companion object {
        @JvmStatic
        fun openActivity(activity: Activity) {
            val intent = Intent(activity, AuthTokenExpireActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityAuthtokenexpireBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_authtokenexpire
    override val viewModel: AuthTokenViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(AuthTokenViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
        mBinding = viewDataBinding!!
        mBinding.btnLogIn.onClick {
            SplashActivity.openActivity(this)
            finish()
        }
    }

    override fun onRetry() {

    }

}
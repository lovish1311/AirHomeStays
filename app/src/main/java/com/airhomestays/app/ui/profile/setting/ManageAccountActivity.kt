package com.airhomestays.app.ui.profile.setting

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityManageAccountBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.splash.SplashActivity
import com.airhomestays.app.util.gone
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class ManageAccountActivity : BaseActivity<ActivityManageAccountBinding, ManageAccountViewModel>(),SettingsNavigator{

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override val bindingVariable: Int
    get() = BR.viewModel
    override val layoutId: Int
    get() = R.layout.activity_manage_account
    override val viewModel: ManageAccountViewModel
    get() = ViewModelProvider(this,mViewModelFactory).get(ManageAccountViewModel::class.java)
    lateinit var mBinding : ActivityManageAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        mBinding.actionBar.tvToolbarHeading.text = getString(R.string.manage_account)
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.ivNavigateup.setOnClickListener {
            onBackPressed()
        }

        mBinding.permenentLl.setOnClickListener {
            showAlertDialog()
        }

    }

    override fun onRetry() {

    }
    private fun showAlertDialog() {
        AlertDialog.Builder(this,R.style.AlertDialogCustom)
            .setTitle(getString(R.string.delete_account))
            .setMessage(getString(R.string.are_you_sure_delete_account_permenently))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> viewModel.delete() }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun openSplashScreen() {

    }

    override fun navigateToSplash() {
        viewModel.navigator.showToast(resources.getString(R.string.account_delete_message))
        LoginManager.getInstance().logOut()
        mGoogleSignInClient?.signOut()
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        this.finish()
    }

    override fun setLocale(key: String) {

    }

    override fun finishActivity() {
        TODO("Not yet implemented")
    }

}
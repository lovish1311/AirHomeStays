package com.airhomestays.app.ui.profile.confirmPhonenumber

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityConfirmPhnoBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.addFragmentToActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class ConfirmPhnoActivity : BaseActivity<ActivityConfirmPhnoBinding, ConfirmPhnoViewModel>(),
        ConfirmPhnoNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    lateinit var mBinding: ActivityConfirmPhnoBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = com.airhomestays.app.R.layout.activity_confirm_phno
    override val viewModel: ConfirmPhnoViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(ConfirmPhnoViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        if (savedInstanceState == null) {
            addFragmentToActivity(mBinding.flConfPhno.id, ConfirmPhnoFragment(), "ConfirmPhno")
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle ) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun navigateScreen(PHScreen: ConfirmPhnoViewModel.PHScreen, vararg params: String?) {
        try {
            hideKeyboard()
            when (PHScreen) {
                ConfirmPhnoViewModel.PHScreen.COUNTRYCODE -> openFragment(CountryCodeFragment())
                ConfirmPhnoViewModel.PHScreen.FOURDIGITCODE -> {
                    viewModel.sendVerification()
                    openFragment(EnterCodeFragment())
                }
                ConfirmPhnoViewModel.PHScreen.CONFIRMPHONE -> {
                    onBackPressed()
                }
                ConfirmPhnoViewModel.PHScreen.FINISHED -> {
                    finish()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    fun openFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flConfPhno.id, fragment)
                .addToBackStack(null)
                .commit()
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onBackPressed() {
        viewModel.listSearch.value = null
        super.onBackPressed()
    }

    override fun onRetry() {
        val fragment = supportFragmentManager.findFragmentById(mBinding.flConfPhno.id)
        if (fragment is BaseFragment<*, *>) {
            fragment.onRetry()
        }
    }
}
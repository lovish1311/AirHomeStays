package com.airhomestays.app.ui.auth.login

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentAuthLoginBinding
import com.airhomestays.app.ui.auth.AuthNavigator
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.RxBus
import com.airhomestays.app.util.UiEvent
import com.airhomestays.app.util.onClick
import com.airhomestays.app.vo.Outcome
import javax.inject.Inject


class LoginFragment : BaseFragment<FragmentAuthLoginBinding, LoginViewModel>(), AuthNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_auth_login
    override val viewModel: LoginViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(LoginViewModel::class.java)
    lateinit var mBinding: FragmentAuthLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        setUp()
        subscribeToLiveData()
    }

    private fun setUp() {
        mBinding.actionBar.tvRightside.text =
            baseActivity!!.resources.getString(R.string.forgotpassword)
        mBinding.actionBar.tvRightside.onClick {
            RxBus.publish(UiEvent.Navigate(AuthViewModel.Screen.FORGOTPASSWORD))
        }
        mBinding.actionBar.rlToolbarNavigateup.onClick {
            baseActivity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.fireBaseResponse?.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { outcome ->
                when (outcome) {
                    is Outcome.Error -> {
                        showError()
                    }

                    is Outcome.Failure -> {
                        showError()
                    }

                    is Outcome.Success -> {
                        viewModel.checkLogin()
                    }

                    is Outcome.Progress -> {
                        viewModel.isLoading.set(outcome.loading)
                    }
                }
            }
        })
    }

    fun showPassword() {
        viewModel.showPassword.set(true)
    }

    override fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?) {
        RxBus.publish(UiEvent.Navigate(screen, *params))
    }

    override fun onRetry() {
        viewModel.checkLogin()
    }

}
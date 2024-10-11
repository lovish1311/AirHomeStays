package com.airhomestays.app.ui.auth.resetPassword

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentAuthChangePasswordBinding
import com.airhomestays.app.ui.auth.AuthNavigator
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.RxBus
import com.airhomestays.app.util.UiEvent
import com.airhomestays.app.util.onClick
import javax.inject.Inject

private const val TOKEN = "param1"
private const val EMAIL = "param2"

class ResetPasswordFragment :
    BaseFragment<FragmentAuthChangePasswordBinding, ResetPasswordViewModel>(), AuthNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_auth_change_password
    override val viewModel: ResetPasswordViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(ResetPasswordViewModel::class.java)
    lateinit var mBinding: FragmentAuthChangePasswordBinding
    private var token: String? = null
    private var email: String? = null

    companion object {
        @JvmStatic
        fun newInstance(token: String, email: String) =
            ResetPasswordFragment().apply {
                arguments = Bundle().apply {
                    putString(TOKEN, token)
                    putString(EMAIL, email)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            token = it.getString(TOKEN)
            email = it.getString(EMAIL)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        viewModel.token.value = token
        viewModel.email.value = email
    }

    private fun initView() {
        mBinding.ivClose.onClick { baseActivity?.onBackPressedDispatcher?.onBackPressed() }
        mBinding.ivPasswordVisibility.setOnClickListener { showPassword() }
        mBinding.ivConfirmPasswordVisibility.setOnClickListener { showConfirmPassword() }
        mBinding.btnLogin.onClick {
            viewModel.validateData()
        }
    }

    override fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?) {
        RxBus.publish(UiEvent.Navigate(screen, *params))
    }

    override fun onRetry() {
        viewModel.validateData()
    }

    fun showPassword() {
        if (viewModel.showPassword.get() == false) {
            viewModel.showPassword.set(true)
            mBinding.ivPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_visibility_show
                )
            )
        } else {
            viewModel.showPassword.set(false)
            mBinding.ivPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_visibility_hide,
                )
            )
        }
    }

    fun showConfirmPassword() {
        if (viewModel.showPassword1.get() == false) {
            viewModel.showPassword1.set(true)
            mBinding.ivConfirmPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_visibility_show
                )
            )
        } else {
            viewModel.showPassword1.set(false)
            mBinding.ivConfirmPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_visibility_hide,
                )
            )
        }
    }
}
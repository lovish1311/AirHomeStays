package com.airhomestays.app.ui.auth.password

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentAuthCreatePasswordBinding
import com.airhomestays.app.ui.auth.AuthNavigator
import com.airhomestays.app.ui.auth.AuthViewModel
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.RxBus
import com.airhomestays.app.util.UiEvent
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"

class PasswordFragment : BaseFragment<FragmentAuthCreatePasswordBinding, PasswordViewModel>(),
    AuthNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_auth_create_password
    override val viewModel: PasswordViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(PasswordViewModel::class.java)
    lateinit var mBinding: FragmentAuthCreatePasswordBinding
    private var param1: String = ""

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            PasswordFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        arguments?.let {
            param1 = it.getString(ARG_PARAM1, "")
        }
        initView()
    }

    private fun initView() {
        mBinding.actionBar.tvRightside.gone()
        mBinding.actionBar.rlToolbarNavigateup.onClick { baseActivity?.onBackPressedDispatcher?.onBackPressed() }
        mBinding.ltLoadingView.setImageResource(R.drawable.ic_right_arrow_blue)
        viewModel.password.set(param1)
    }

    override fun navigateScreen(screen: AuthViewModel.Screen, vararg params: String?) {
        RxBus.publish(UiEvent.Navigate(screen, *params))
    }

    override fun onRetry() {

    }

}
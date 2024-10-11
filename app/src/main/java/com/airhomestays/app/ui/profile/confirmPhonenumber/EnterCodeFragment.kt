package com.airhomestays.app.ui.profile.confirmPhonenumber

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airhomestays.app.BR
import com.airhomestays.app.databinding.FragmentFourdigitCodeBinding
import com.airhomestays.app.ui.base.BaseFragment
import javax.inject.Inject

class EnterCodeFragment : BaseFragment<FragmentFourdigitCodeBinding, ConfirmPhnoViewModel>() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = com.airhomestays.app.R.layout.fragment_fourdigit_code
    override val viewModel: ConfirmPhnoViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ConfirmPhnoViewModel::class.java)
    lateinit var mBinding: FragmentFourdigitCodeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
    }

    private fun initView() {
        mBinding.ivClose.setOnClick { baseActivity?.onBackPressed() }
    }

    override fun onRetry() {
        viewModel.verifyCode()
    }

}
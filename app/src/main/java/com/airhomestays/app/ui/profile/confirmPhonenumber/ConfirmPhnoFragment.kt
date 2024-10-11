package com.airhomestays.app.ui.profile.confirmPhonenumber

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentConfirmPhonenumberBinding
import com.airhomestays.app.ui.base.BaseFragment
import javax.inject.Inject


class ConfirmPhnoFragment :
    BaseFragment<FragmentConfirmPhonenumberBinding, ConfirmPhnoViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_confirm_phonenumber
    override val viewModel: ConfirmPhnoViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(ConfirmPhnoViewModel::class.java)
    lateinit var mBinding: FragmentConfirmPhonenumberBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
    }

    private fun initView() {
        if (viewModel.phoneType.get() == "1") {
            mBinding.tvNext.text = getString(R.string.next)
        } else if (viewModel.phoneType.get() == "2") {
            mBinding.tvNext.text = getString(R.string.update_cap)
        }
        mBinding.ivClose.root.setOnClickListener {
            baseActivity?.finish()
        }
    }

    override fun onRetry() {
        viewModel.getCountryCodes()
    }

}


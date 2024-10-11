package com.airhomestays.app.ui.host.step_one

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentWelcomeBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import javax.inject.Inject


class WelcomeFragment : BaseFragment<HostFragmentWelcomeBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_welcome
    override val viewModel: StepOneViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostFragmentWelcomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.isEdit = false
        initView()
    }

    private fun initView() {
         mBinding.actionBar.tvToolbarHeading.text=""
        mBinding.actionBar.ivNavigateup.onClick { activity?.finish() }
        mBinding.pic=viewModel.dataManager.currentUserProfilePicUrl
        mBinding.tvNext.onClick {
            viewModel.navigator.navigateScreen(StepOneViewModel.NextScreen.TYPE_OF_SPACE)
        }
        val userName = "- "+resources.getString(R.string.hi)+ " "+ viewModel.dataManager.currentUserFirstName!! + "! "
        val myIcon: Drawable = resources.getDrawable(R.drawable.ic_hand)
        myIcon.setBounds(0,0,mBinding.title.lineHeight,mBinding.title.lineHeight)
        val imageSpan = ImageSpan(myIcon)
        val spannableString = SpannableString(userName) //Set text of SpannableString from TextView
        spannableString.setSpan(
            imageSpan,
            0,
            userName.length + 1 - userName.length,
            0
        ) //Add image at start of string
        mBinding.title.setText(spannableString)
    }



    override fun onRetry() {

    }
}
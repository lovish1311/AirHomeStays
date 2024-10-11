package com.airhomestays.app.ui.profile.review

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentReviewByYouBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import javax.inject.Inject

class FragmentReviewByYou : BaseFragment<FragmentReviewByYouBinding,ReviewViewModel>(){
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: FragmentReviewByYouBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_review_by_you
    override val viewModel: ReviewViewModel
        get() = ViewModelProvider(baseActivity!!,mViewModelFactory).get(ReviewViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding=viewDataBinding!!
        openFragment(FragmentReviewByYouPending(),"upComing")

        mBinding.tvPast.onClick {
            val fragment= childFragmentManager.findFragmentByTag("past")
            if(fragment!=null){
                if(fragment.isVisible.not()){
                    mBinding.tvPast.setBackgroundResource(R.drawable.curve_button_blue_map)
                    mBinding.tvUpcoming.setBackgroundResource(R.drawable.curve_button_gray_up)
                    mBinding.tvPast.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                    mBinding.tvUpcoming.setTextColor(ContextCompat.getColor(requireContext(),R.color.black))
                    openFragment(FragmentReviewByYouPast(),"past")
                    if(viewModel.getByYouInitialized()){
                        viewModel.onRefreshByYou()
                    }
                }
            }else{
                mBinding.tvPast.setBackgroundResource(R.drawable.curve_button_blue_map)
                    mBinding.tvUpcoming.setBackgroundResource(R.drawable.curve_button_gray_up)
                    mBinding.tvPast.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                    mBinding.tvUpcoming.setTextColor(ContextCompat.getColor(requireContext(),R.color.black))
                    openFragment(FragmentReviewByYouPast(),"past")
                    if(viewModel.getByYouInitialized()){
                        viewModel.onRefreshByYou()
                    }
            }
        }

        mBinding.tvUpcoming.onClick {
            val fragment= childFragmentManager.findFragmentByTag("upComing")
            if(fragment!=null){
                if(fragment.isVisible.not()){
                    mBinding.tvPast.setBackgroundResource(R.drawable.curve_button_gray_up)
                    mBinding.tvUpcoming.setBackgroundResource(R.drawable.curve_button_blue_map)
                    mBinding.tvPast.setTextColor(ContextCompat.getColor(requireContext(),R.color.black))
                    mBinding.tvUpcoming.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                    openFragment(FragmentReviewByYouPending(),"upComing")
                    if(viewModel.getPendingInitialized()){
                        viewModel.onRefreshPending()
                    }
                }
            }else{
                mBinding.tvPast.setBackgroundResource(R.drawable.curve_button_gray_up)
                mBinding.tvUpcoming.setBackgroundResource(R.drawable.curve_button_blue_map)
                mBinding.tvPast.setTextColor(ContextCompat.getColor(requireContext(),R.color.black))
                mBinding.tvUpcoming.setTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                openFragment(FragmentReviewByYouPending(),"upComing")
                if(viewModel.getPendingInitialized()){
                    viewModel.onRefreshPending()
                }
            }
        }

    }


    fun openFragment(fragment: Fragment,tag: String){
        childFragmentManager
                .beginTransaction()
                .replace(mBinding.frameReviewPast.id, fragment,tag)
                .commit()
    }

    override fun onRetry() {
    }
}
package com.airhomestays.app.ui.listing

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityAdditionalGuestBinding
import com.airhomestays.app.databinding.ActivityGuestBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import javax.inject.Inject

class AdditionalGuestFragment : BaseFragment<ActivityAdditionalGuestBinding, ListingDetailsViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_additional_guest
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    lateinit var mBinding: ActivityAdditionalGuestBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
        if(activity is ListingDetails){
            (activity as ListingDetails).changeStatusBarColor(R.color.black)
        }

    }

    private fun initView() {
        mBinding.additionalGuest = viewModel.visitors
        viewModel.initialValue.value?.let {
            viewModel.visitors.set(it.visitors.toString())
        }
        mBinding.inlToolbar.tvToolbarHeading.text="Visitor"

        mBinding.inlToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }

        mBinding.ibGuestMinus.setOnClickListener {
            viewModel.visitors.get()?.let {
                viewModel.visitors.set(it.toInt().minus(1).toString())
            }

        }

        mBinding.ibGuestPlus.setOnClickListener {
            viewModel.visitors.get()?.let {
                viewModel.visitors.set(it.toInt().plus(1).toString())
            }

        }
        mBinding.btnGuestSeeresult.onClick {
            try {
                baseActivity?.onBackPressed()
                val initialValues = viewModel.initialValue.value!!
                initialValues.visitors = viewModel.visitors.get()!!.toInt()

                viewModel.initialValue.value = initialValues
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun subscribeToLiveData() {
        viewModel.listingDetails.observe(viewLifecycleOwner, Observer {
            it?.let { listDetails ->
                mBinding.minusLimit1 = 0
                mBinding.plusLimit1 = listDetails.listingData?.visitorsLimit
            }
        })
    }

    override fun onRetry() {

    }
    override fun onResume() {
        super.onResume()
        viewModel.clearStatusBar(requireActivity())
    }
}
package com.airhomestays.app.ui.listing

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityGuestBinding
import com.airhomestays.app.databinding.ActivityInfantBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import javax.inject.Inject

class InfantFragment : BaseFragment<ActivityInfantBinding, ListingDetailsViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_infant
    override val viewModel: ListingDetailsViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    lateinit var mBinding: ActivityInfantBinding

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
        mBinding.infant = viewModel.infants
        viewModel.initialValue.value?.let {
            viewModel.infants.set(it.infantCount.toString())
        }
        mBinding.inlToolbar.tvToolbarHeading.text="Edit Infant"

        mBinding.inlToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }

        mBinding.ibGuestMinus.setOnClickListener {
            viewModel.infants.get()?.let {
                viewModel.infants.set(it.toInt().minus(1).toString())
            }

        }

        mBinding.ibGuestPlus.setOnClickListener {
            viewModel.infants.get()?.let {
                viewModel.infants.set(it.toInt().plus(1).toString())
            }

        }
        mBinding.btnGuestSeeresult.onClick {
            try {
                baseActivity?.onBackPressed()
                val initialValues = viewModel.initialValue.value!!
                initialValues.infantCount = viewModel.infants.get()!!.toInt()

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
                mBinding.plusLimit1 = listDetails.listingData?.infantLimit
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
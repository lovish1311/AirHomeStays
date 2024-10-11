package com.airhomestays.app.ui.explore.guest

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityGuestBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.explore.ExploreViewModel
import com.airhomestays.app.util.onClick
import javax.inject.Inject

class GuestFragment : BaseFragment<ActivityGuestBinding, ExploreViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_guest
    override val viewModel: ExploreViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(ExploreViewModel::class.java)
    lateinit var mBinding: ActivityGuestBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.personCapacity1 = viewModel.personCapacity1
        viewModel.personCapacity1.set(viewModel.personCapacity.value)
        mBinding.inlToolbar.ivNavigateup.onClick { baseActivity?.onBackPressed() }
        mBinding.ibGuestMinus.onClick {
            viewModel.personCapacity1.get()?.let {
                viewModel.personCapacity1.set(it.toInt().minus(1).toString())
            }
        }
        mBinding.ibGuestPlus.onClick {
            viewModel.personCapacity1.get()?.let {
                viewModel.personCapacity1.set(it.toInt().plus(1).toString())
            }
        }
        mBinding.btnGuestSeeresult.onClick {
            try {
                baseActivity?.onBackPressed()
                if (viewModel.personCapacity1.get() != null && viewModel.personCapacity1.get()!!
                        .toInt() > 0
                ) {
                    viewModel.personCapacity.value = viewModel.personCapacity1.get()
                    viewModel.startSearching()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun subscribeToLiveData() {
        viewModel.exploreLists1.observe(viewLifecycleOwner, Observer {
            it?.getListingSettingsCommon?.results?.let { list ->
                list.forEachIndexed { _, item ->
                    if (item?.id == 2) {
                        mBinding.minusLimit1 = item?.listSettings?.get(0)?.startValue
                        mBinding.plusLimit1 = item?.listSettings?.get(0)?.endValue
                    }
                    return@forEachIndexed
                }
            }
        })
    }

    override fun onRetry() {

    }
}
package com.airhomestays.app.ui.host.step_one

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.*
import com.airhomestays.app.databinding.HostSelectCountryBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import javax.inject.Inject

class SelectCountry : BaseFragment<HostSelectCountryBinding, StepOneViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_select_country
    override val viewModel: StepOneViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(StepOneViewModel::class.java)
    lateinit var mBinding: HostSelectCountryBinding
    private var list = ArrayList<GetCountrycodeQuery.Result?>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        baseActivity!!.onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    baseActivity?.onBackPressed()
                }
            })
        viewModel.isEdit = false
        subscribeToLiveData()
        mBinding.rlToolbarNavigateup.onClick {
            hideKeyboard()
            baseActivity?.onBackPressed()
        }
        viewModel.list.value?.let {
            viewModel.listSearch.value = java.util.ArrayList(it)
        }
    }

    private fun subscribeToLiveData() {
        viewModel.list.observe(viewLifecycleOwner, Observer {
            it?.let { result ->
                list = ArrayList(result)
                setUp()
            }
        })
        viewModel.listSearch.observe(viewLifecycleOwner, Observer {
            it?.let { result ->
                list = result
                if (mBinding.rlCountryCodes.adapter != null) {
                    mBinding.rlCountryCodes.requestModelBuild()
                }

            }
        })
    }

    private fun setUp() {
        mBinding.rlCountryCodes.withModels {
            if (list.isNotEmpty()) {
                for (index in 0 until list.size) {
                    viewholderCountryCodes {
                        id("countries - $index")
                        header(list[index]?.countryName)
                        large(false)
                        isWhite(false)
                        switcher(true)
                        onClick(View.OnClickListener {
                            list[index]?.countryName?.let {
                                viewModel.country.set(it)
                                viewModel.countryCode.set(list[index]?.countryCode)
                                baseActivity?.onBackPressed()
                            }
                        })
                    }
                    viewholderDividerNoPadding {
                        id(index)
                    }
                }
            } else {
                viewholderCountryCodes {
                    id("noresult")
                    header(getString(R.string.result_not_found))
                    large(false)
                    switcher(false)
                }
            }
        }
    }

    override fun onRetry() {
        viewModel.getCountryCode()
    }
}
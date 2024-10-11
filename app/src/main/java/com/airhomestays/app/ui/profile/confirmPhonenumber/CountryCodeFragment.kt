package com.airhomestays.app.ui.profile.confirmPhonenumber

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.GetCountrycodeQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentCountryCodeBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderCountryCodes
import javax.inject.Inject

class CountryCodeFragment : BaseFragment<FragmentCountryCodeBinding, ConfirmPhnoViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_country_code
    override val viewModel: ConfirmPhnoViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(ConfirmPhnoViewModel::class.java)
    lateinit var mBinding: FragmentCountryCodeBinding
    private var list = ArrayList<GetCountrycodeQuery.Result?>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        subscribeToLiveData()
        mBinding.ivClose.onClick {
            hideKeyboard()
            baseActivity?.onBackPressed()
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
                mBinding.rlCountryCodes.requestModelBuild()
            }
        })
    }

    private fun setUp() {
        mBinding.rlCountryCodes.withModels {
            viewholderCountryCodes {
                id("title")
                header(resources.getString(R.string.select_the_country))
                large(true)
                switcher(true)
            }
            if (list.isNotEmpty()) {
                for (index in 0 until list.size) {
                    viewholderCountryCodes {
                        id("countries - $index")
                        header(list[index]?.countryName)
                        large(false)
                        switcher(true)
                        onClick(View.OnClickListener {
                            list[index]?.dialCode?.let {
                                viewModel.countryCode.set(it)
                                baseActivity?.onBackPressed()
                            }
                        })
                    }
                }
            } else {
                viewholderCountryCodes {
                    id("noresult")
                    header(resources.getString(R.string.result_not_found))
                    large(false)
                    switcher(false)
                }
            }
        }
    }

    override fun onRetry() {
        viewModel.getCountryCodes()
    }
}
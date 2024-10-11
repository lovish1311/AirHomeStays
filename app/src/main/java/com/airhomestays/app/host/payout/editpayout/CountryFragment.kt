package com.airhomestays.app.host.payout.editpayout

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.GetCountrycodeQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentPayoutCountryBinding
import com.airhomestays.app.host.payout.addPayout.AddPayoutActivity
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.enable
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderCountryCodes
import com.airhomestays.app.viewholderDividerNoPadding
import com.airhomestays.app.viewholderLoader
import javax.inject.Inject

class CountryFragment : BaseFragment<FragmentPayoutCountryBinding, PayoutViewModel>(),
    EditPayoutNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_payout_country
    override val viewModel: PayoutViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(PayoutViewModel::class.java)
    lateinit var mBinding: FragmentPayoutCountryBinding
    private var list = ArrayList<GetCountrycodeQuery.Result?>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
        setUp()
    }

    private fun initView() {
        mBinding.ivClose.onClick { baseActivity?.onBackPressed(); hideKeyboard() }
    }

    private fun subscribeToLiveData() {
        viewModel.loadCountryCode().observe(viewLifecycleOwner, Observer {
            it?.let { result ->
                list = ArrayList(result)
                mBinding.rlCountry.requestModelBuild()
            }
        })
        viewModel.listSearch.observe(viewLifecycleOwner, Observer {
            it?.let { result ->
                list = result
                mBinding.rlCountry.requestModelBuild()
            }
        })
    }

    private fun setUp() {
        mBinding.rlCountry.withModels {


            if (viewModel.isLoading.get()) {
                viewholderLoader {
                    id("Loader")
                    isLoading(true)
                }
            } else {
                if (list.isNotEmpty()) {
                    for (index in 0 until list.size) {
                        viewholderCountryCodes {
                            id(baseActivity!!.resources.getString(R.string.countries) + " - $index")
                            header(list[index]?.countryName)
                            large(false)
                            switcher(true)
                            onClick(View.OnClickListener {
                                AddPayoutActivity.openActivity(
                                    baseActivity!!,
                                    list[index]?.countryName!!,
                                    list[index]?.countryCode!!
                                )
                                baseActivity?.supportFragmentManager?.popBackStackImmediate()
                                viewModel.onSearchTextChanged("")
                            })
                        }
                        viewholderDividerNoPadding {
                            id(index)
                        }
                    }
                } else {
                    viewholderCountryCodes {
                        id("noresult")
                        header(baseActivity!!.resources.getString(R.string.result_not_found))
                        large(false)
                        switcher(false)
                    }
                }
            }
        }
    }

    override fun disableCountrySearch(flag: Boolean) {
        if (flag) {
            mBinding.etSearchCountry.enable()
        } else {
            mBinding.etSearchCountry.disable()
        }
    }

    override fun moveToScreen(screen: EditPayoutActivity.Screen) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRetry() {
        viewModel.loadCountryCode()
    }
}
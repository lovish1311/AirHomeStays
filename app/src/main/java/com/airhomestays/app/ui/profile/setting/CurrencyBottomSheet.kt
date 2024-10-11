package com.airhomestays.app.ui.profile.setting.currency

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentOptionsBinding
import com.airhomestays.app.ui.base.BaseBottomSheet
import com.airhomestays.app.ui.profile.setting.SettingViewModel
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.viewholderBgBottomsheetCurrency
import com.airhomestays.app.viewholderDividerNoPadding
import com.airhomestays.app.viewholderOptionText
import javax.inject.Inject

class CurrencyBottomSheet : BaseBottomSheet<HostFragmentOptionsBinding, SettingViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentOptionsBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_options
    override val viewModel: SettingViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(SettingViewModel::class.java)

    var type: String = ""

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
                CurrencyBottomSheet().apply {
                    arguments = Bundle().apply {
                        putString("type", type)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        arguments?.let {
            type = it.getString("type", "ert")
        }
        mBinding.rvOptions.setHasFixedSize(true)
        subscribeToLiveData()
    }

    fun subscribeToLiveData() {
        mBinding.rvOptions.withModels {
            viewholderBgBottomsheetCurrency {
                id("bgbottomsheett")
            }
            if (type.equals("currency")) {
                viewModel.currencies.value?.forEachIndexed { index, s ->
                    viewholderOptionText {
                        id("option" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(true)
                        if (BindingAdapters.getCurrencySymbol(s?.symbol)==s?.symbol){
                            desc( s.symbol)
                        }else{
                            desc( BindingAdapters.getCurrencySymbol(s?.symbol)+" "+s?.symbol)
                        }
                        size(16.toFloat())

                        if (s?.symbol.equals(viewModel.baseCurrency.get())) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                        clickListener(View.OnClickListener {
                            viewModel.updateCurrency(baseActivity?.baseContext!!,s?.symbol!!)
                            dismiss()
                        })
                    }
                    viewholderDividerNoPadding {
                        id(index)
                    }
                }
            }
            else if (type.equals("theme")) {
                viewModel.appTheme.value=viewModel.dataManager.prefTheme.toString()
                viewModel.theme.forEachIndexed { index, s ->
                    viewholderOptionText {
                        id("option" + index)
                        paddingBottom(true)
                        paddingTop(true)
                        iconVisible(false)
                        image(viewModel.themeIcon[index])
                        if (s=="Auto"){
                            desc(getString(R.string.auto))
                        }else if (s=="Light"){
                            desc(getString(R.string.light))
                        }else{
                            desc(getString(R.string.dark))
                        }

                        size(16.toFloat())

                        if (s == viewModel.appTheme.value) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                        clickListener(View.OnClickListener {
                            viewModel.updateTheme(baseActivity?.baseContext!!,s);
                            dismiss()
                        })
                    }
                    viewholderDividerNoPadding {
                        id(index)
                    }
                }
            }
            else {
                viewModel.langName.forEachIndexed { index, s ->
                    viewholderOptionText {
                        id("option" + index)
                        paddingBottom(true)
                        iconVisible(true)
                        paddingTop(true)
                        desc(s)
                        size(16.toFloat())

                        if (s == viewModel.appLanguage.get()) {
                            isSelected(true)
                            txtColor(true)
                        } else {
                            isSelected(false)
                            txtColor(false)
                        }
                        clickListener(View.OnClickListener {
                            val code = viewModel.langCode[index]
                            viewModel.updateLangauge(baseActivity?.baseContext!!,code, s)
                            dismiss()
                        })
                    }
                    viewholderDividerNoPadding {
                        id(index)
                    }
                }
            }
        }
    }

    override fun onRetry() {

    }
}
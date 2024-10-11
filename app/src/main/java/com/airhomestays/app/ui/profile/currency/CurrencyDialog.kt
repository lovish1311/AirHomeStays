package com.airhomestays.app.ui.profile.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.R
import com.airhomestays.app.databinding.DialogProfileCommonBinding
import com.airhomestays.app.ui.base.BaseDialogFragment
import com.airhomestays.app.util.EnableAlpha
import com.airhomestays.app.util.RxBus
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.vo.Outcome
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

private const val PRESELECTED_CURRENCY = "selectedCurrency"

class CurrencyDialog : BaseDialogFragment() {

    private val TAG = CurrencyDialog::class.java.simpleName
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    val viewModel: CurrencyVIewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(CurrencyVIewModel::class.java)
    private var preSelectedCurrency: String? = null
    lateinit var binding: DialogProfileCommonBinding
    companion object {
        fun newInstance(preSelectedLanguages: String) =
                CurrencyDialog().apply {
                    arguments = Bundle().apply {
                        putString(PRESELECTED_CURRENCY, preSelectedLanguages)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            preSelectedCurrency = it.getString(PRESELECTED_CURRENCY)
        }
    }

    fun dismissDialog() {
        dismissDialog(TAG)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<DialogProfileCommonBinding>(inflater, R.layout.dialog_profile_common, container, false)
        val view = binding.root
        AndroidSupportInjection.inject(this)
        viewModel.navigator = this
        binding.title = resources.getString(R.string.preferred_currency)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        setUp()
    }

    private fun setUp() {
        viewModel.preSelectedLanguages.value = preSelectedCurrency
        binding.rvLanguage.setHasFixedSize(true)
        binding.rvLanguage.adapter = CurrencyAdapter( clickListener = {} )
        viewModel.preSelectedLanguages.value?.let {
            (binding.rvLanguage.adapter as CurrencyAdapter).selectedItem = it
        }
        binding.btnApply.onClick {
            RxBus.publish(arrayOf((binding.rvLanguage.adapter as CurrencyAdapter).selectedItem))
        }
        binding.btnCancel.onClick {
            dismissDialog()
        }
    }

    private fun initObserver() {
        viewModel.postsOutcome.observe(viewLifecycleOwner, Observer {res ->
            res?.getContentIfNotHandled()?.let {
                when (it) {
                    is Outcome.Progress -> {
                        if (it.loading) {
                            binding.ltLoading.playAnimation()
                            binding.ltLoading.visible()
                        } else {
                            binding.ltLoading.cancelAnimation()
                            binding.ltLoading.gone()
                        }
                    }
                    is Outcome.Success -> {
                        binding.rvLanguage.visible()
                        it.data?.let { it1 ->
                            (binding.rvLanguage.adapter as CurrencyAdapter).setData(
                                it1
                            )
                        }
                    }
                    is Outcome.Failure -> {

                    }
                    is Outcome.Error -> {
                        binding.btnApply.EnableAlpha(false)
                    }
                }
            }
        })
    }

    fun show(fragmentManager: androidx.fragment.app.FragmentManager) {
        super.show(fragmentManager, TAG)
    }
}
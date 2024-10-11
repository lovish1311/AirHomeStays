package com.airhomestays.app.ui.profile.languages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.R
import com.airhomestays.app.adapter.LanguagesAdapter
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

private const val PRESELECTED_LANGUAGES = "selectedLanguages"

class LanguagesDialog : BaseDialogFragment() {

    private val TAG = LanguagesDialog::class.java.simpleName
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    val viewModel: LanguagesViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(LanguagesViewModel::class.java)
    private var preSelectedLanguages: String? = null
    lateinit var binding: DialogProfileCommonBinding

    companion object {
        fun newInstance(preSelectedLanguages: String) =
            LanguagesDialog().apply {
                arguments = Bundle().apply {
                    putString(PRESELECTED_LANGUAGES, preSelectedLanguages)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            preSelectedLanguages = it.getString(PRESELECTED_LANGUAGES)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<DialogProfileCommonBinding>(
            inflater,
            R.layout.dialog_profile_common,
            container,
            false
        )
        val view = binding.root
        AndroidSupportInjection.inject(this)
        viewModel.navigator = this
        binding.title = resources.getString(R.string.preferred_language)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        viewModel.preSelectedLanguages.value = preSelectedLanguages
        binding.rvLanguage.setHasFixedSize(true)
        binding.rvLanguage.adapter = LanguagesAdapter()
        viewModel.preSelectedLanguages.value?.let {
            (binding.rvLanguage.adapter as LanguagesAdapter).selectedItem = it
        }
        binding.btnApply.onClick {
            RxBus.publish(
                arrayOf(
                    (binding.rvLanguage.adapter as LanguagesAdapter).selectedItem,
                    (binding.rvLanguage.adapter as LanguagesAdapter).languageName
                )
            )
        }
        binding.btnCancel.onClick { activity?.onBackPressed() }
    }

    private fun subscribeToLiveData() {
        viewModel.postsOutcome.observe(viewLifecycleOwner, Observer { res ->
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
                        (binding.rvLanguage.adapter as LanguagesAdapter).setData(it.data)
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
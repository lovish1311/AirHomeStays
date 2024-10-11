package com.airhomestays.app.host.photoUpload

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentListDescBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.invisible
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderListDescEt
import com.airhomestays.app.viewholderUserName2
import javax.inject.Inject


class AddListDescFragment : BaseFragment<HostFragmentListDescBinding, Step2ViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentListDescBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_list_desc
    override val viewModel: Step2ViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(Step2ViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.descToolbar.ivNavigateup.onClick {
            baseActivity?.onBackPressed()
        }
        mBinding.descToolbar.tvRightside.invisible()
        mBinding.tvNext.onClick {
                if (viewModel.checkFilledData()) {
                    viewModel.retryCalled = "update"
                    viewModel.updateStep2()
                }
        }
        subscribeToLiveData()
    }

    fun initView() {
        mBinding.pgBar.progress = 100
        if (viewModel.getListAddedStatus()) {
            mBinding.tvRightsideText.visibility = View.GONE
        } else {
            mBinding.tvRightsideText.visibility = View.VISIBLE

            mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
            mBinding.tvRightsideText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            mBinding.tvRightsideText.onClick {
                if (viewModel.desc.get()?.trim().isNullOrEmpty()) {
                    showSnackbar(
                        getString(R.string.add_desc_alert),
                        getString(R.string.add_description)
                    )
                } else {
                    viewModel.retryCalled = "update"
                    viewModel.updateStep2()
                }
            }
        }
    }

    fun subscribeToLiveData() {
        viewModel.step2Result.observe(viewLifecycleOwner, Observer {
            it?.let {
                initView()
                setup()
            }
        })
    }

    fun setup() {
        mBinding.chips.apply {
            paddingBottom = true
            photos = false
            title = false
            description = true
            photosClick = (View.OnClickListener {
                viewModel.navigator.navigateBack(Step2ViewModel.BackScreen.UPLOAD)
            })
            titleClick = (View.OnClickListener {
                viewModel.navigator.navigateBack(Step2ViewModel.BackScreen.LISTTITLE)
            })
            descriptionClick = (View.OnClickListener {

            })
        }
        mBinding.rvListDesc.withModels {
            viewholderUserName2 {
                id("header")
                name(getString(R.string.edit_desc))
                isBgNeeded(true)
                paddingTop(true)
                paddingBottom(true)
            }


            viewholderListDescEt {
                id("desc")
                text(viewModel.desc)
                title(getString(R.string.summary))
                marginTop(true)
                hint(getString(R.string.desc_hint))
                maxLength(700)
                onBind { _, view, _ ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.requestFocus()
                    editText.setOnTouchListener(View.OnTouchListener { v, event ->
                        if (editText.hasFocus()) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                            when (event.action and MotionEvent.ACTION_MASK) {
                                MotionEvent.ACTION_SCROLL -> {
                                    v.parent.requestDisallowInterceptTouchEvent(true)
                                    return@OnTouchListener true
                                }
                            }
                        }
                        false
                    })
                }
                onUnbind { _, view ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.setOnTouchListener(null)
                }
            }
        }
    }

    override fun onRetry() {
        viewModel.getListDetailsStep2()
    }
}
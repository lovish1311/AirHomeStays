package com.airhomestays.app.host.photoUpload

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostFragmentListTitleBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.disable
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.invisible
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderListDescEt
import com.airhomestays.app.viewholderTips
import com.airhomestays.app.viewholderUserName2
import javax.inject.Inject

class AddListTitleFragment : BaseFragment<HostFragmentListTitleBinding, Step2ViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostFragmentListTitleBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_fragment_list_title
    override val viewModel: Step2ViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(Step2ViewModel::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        mBinding.titleToolbar.tvRightside.invisible()
        mBinding.titleToolbar.ivNavigateup.onClick {
            baseActivity?.onBackPressed()
        }
        mBinding.tvNext.setText(getText(R.string.finish))
        mBinding.tvNext.onClick {
                if (viewModel.title.get()?.trim().isNullOrEmpty()) {
                    showSnackbar(getString(R.string.add_title_alert), getString(R.string.add_title))
                } else {
                    if (viewModel.desc.get()?.trim().isNullOrEmpty()) {
                        showSnackbar(
                            getString(R.string.add_desc_alert),
                            getString(R.string.add_description)
                        )
                    } else {
                        if (viewModel.checkFilledData()) {
                            viewModel.retryCalled = "update"
                            viewModel.updateStep2()
                        }
                    }
            }
        }
        subscribeToLiveData()
        initView()
    }

    private fun initView() {
        mBinding.pgBar.progress = 100

    }

    fun subscribeToLiveData() {
        viewModel.photoList.observe(this, Observer {
            it?.let {
                if (!viewModel.title.get().isNullOrEmpty() && !viewModel.desc.get()
                        .isNullOrEmpty() && !it.isNullOrEmpty()
                ) {
                    mBinding.tvRightsideText.text = getText(R.string.save_and_exit)
                    mBinding.tvRightsideText.setTextColor(
                        ContextCompat.getColor(
                            activity!!.applicationContext,
                            R.color.colorPrimary
                        )
                    )
                    mBinding.tvRightsideText.visibility = View.VISIBLE
                    mBinding.tvRightsideText.setOnClickListener {
                        if (viewModel.checkFilledData()) {
                            it.disable()
                            viewModel.retryCalled = "update"
                            viewModel.updateStep2()
                        }
                    }

                } else {

                    mBinding.tvRightsideText.visibility = View.GONE
                    mBinding.chips.chips2.gone()

                }
            }
        })

        viewModel.step2Result.observe(viewLifecycleOwner, Observer {
            it?.let {
                initView()
            }
        })
        mBinding.chips.apply {
            paddingBottom = true
            photos = false
            title = true
            description = false
            photosClick = (View.OnClickListener {
                viewModel.navigator.navigateBack(Step2ViewModel.BackScreen.UPLOAD)
            })
            titleClick = (View.OnClickListener {

            })
            descriptionClick = (View.OnClickListener {
                viewModel.navigator.navigateToScreen(Step2ViewModel.NextScreen.LISTDESC)
            })
        }

        mBinding.rvAddTitle.withModels {
            viewholderUserName2 {
                id("header")
                name(getString(R.string.name_and_description))
                isBgNeeded(true)
                paddingTop(true)
                paddingBottom(false)
            }


            viewholderListDescEt {
                id("title")
                text(viewModel.title)
                title(getString(R.string.name_space))
                hint(getString(R.string.name_hint))
                maxChar(50)
                maxLength(35)
                onBind { _, view, _ ->
                    val editText = view.dataBinding.root.findViewById<EditText>(R.id.et_msg_booking)
                    editText.maxLines = 1
                    editText.imeOptions = EditorInfo.IME_ACTION_DONE
                    editText.setSingleLine()
                }
            }
            viewholderTips {
                id("tips2")
                tips(getString(R.string.tips_three))
            }
            viewholderListDescEt {
                id("desc")
                text(viewModel.desc)
                title(getString(R.string.description))
                isBgneed(false)
                marginTop(true)
                maxChar(250)
                hint(getString(R.string.desc_hint))
                maxLength(550)
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
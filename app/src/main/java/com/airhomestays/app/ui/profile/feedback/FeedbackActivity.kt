package com.airhomestays.app.ui.profile.feedback

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.*
import com.airhomestays.app.databinding.ActivityFeedbackBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.withModels
import javax.inject.Inject

class FeedbackActivity: BaseActivity<ActivityFeedbackBinding,FeedbackViewModel>(),FeedbackNavigator {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_feedback
    override val viewModel: FeedbackViewModel
        get() = ViewModelProvider(this,mViewModelFactory).get(FeedbackViewModel::class.java)
    private lateinit var mBinding : ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = this.viewDataBinding!!
        viewModel.navigator = this
        mBinding.actionBar.tvToolbarHeading.text = getString(R.string.feedback)
        mBinding.actionBar.ivCameraToolbar.gone()
        mBinding.actionBar.ivNavigateup.setOnClickListener {
            onBackPressed()
        }
        setUp()
    }

    fun setUp(){
        mBinding.rvFeedback.withModels {
            viewholderItineraryTextBold {
                id("header")
                text(getString(R.string.how_we_doing))
                isRed(false)
                large(false)
                paddingTop(true)
                paddingBottom(false)
            }

            viewholderUserNormalText {
                id("content")
                text(getString(R.string.feedback_content))
                paddingTop(true)
                colorss(true)
                paddingBottom(true)

            }

            viewholderItineraryTextBold {
                id("liketodo")
                text(getString(R.string.like_to_do))
                isRed(false)
                large(false)
                paddingBottom(true)
                paddingTop(true)
            }

            viewholderFeedback {
                id("feedback")
                text(getString(R.string.give_product_feedback))
                image(R.drawable.ic_feedback)
                onClick(View.OnClickListener {
                    openFeedBackDialog()
                })
            }

            viewholderFeedback {
                id("bug")
                text(getString(R.string.report_bug))
                image(R.drawable.ic_feedback_bug)
                onClick(View.OnClickListener {
                    openReportBugDialog()
                })
            }

        }
    }

    private fun openFeedBackDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.feedback))
        val dialogLayout = inflater.inflate(R.layout.feedback_alert, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
        builder.setView(dialogLayout)
        viewModel.feedbackType.set("Feed Back")
        editText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                viewModel.msg.set(s.toString())
            }
        })

        builder.setPositiveButton(getString(R.string.send)) { dialogInterface, i ->
            if (viewModel.msg.get()!!.isNotBlank()&&viewModel.msg.get()!!.isNotEmpty()){
                 viewModel.sendFeedback(viewModel.feedbackType.get().toString(), viewModel.msg.get().toString())
                 viewModel.msg.set("")
             }else{
                 showToast(getString(R.string.please_enter_feedback))
             }
              }
         builder.setNegativeButton(getString(R.string.cancel)) {
             dialogInterface, i -> dialogInterface.dismiss()
         }
         builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
             dialog.dismiss()
         }
        builder.show()
    }

    private fun openReportBugDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.bug))
        val dialogLayout = inflater.inflate(R.layout.feedback_alert, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
        editText.setHint(getString(R.string.enter_bug_here))
        viewModel.feedbackType.set("Bug")
        viewModel.msg.set(editText.text.toString())
        builder.setView(dialogLayout)
        editText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                viewModel.msg.set(s.toString())

            }
        })

        builder.setPositiveButton(getString(R.string.send)) { dialogInterface, i ->
            if (viewModel.msg.get()!!.isNotBlank()&&viewModel.msg.get()!!.isNotEmpty()){
                viewModel.sendFeedback(viewModel.feedbackType.get().toString(), viewModel.msg.get().toString())
                viewModel.msg.set("")
            }else{
                showToast(getString(R.string.pls_enter_bug))
            }
        }
        builder.setNegativeButton(getString(R.string.cancel)) {
            dialogInterface, i -> dialogInterface.dismiss()
        }
        builder.show()
    }

    override fun onRetry() {
       viewModel.sendFeedback(viewModel.feedbackType.get().toString(), viewModel.msg.get().toString())
    }
}
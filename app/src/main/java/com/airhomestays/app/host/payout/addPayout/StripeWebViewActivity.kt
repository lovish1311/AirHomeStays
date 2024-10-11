package com.airhomestays.app.host.payout.addPayout


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityStripeViewBinding
import com.airhomestays.app.ui.base.BaseActivity
import javax.inject.Inject


class StripeWebViewActivity : BaseActivity<ActivityStripeViewBinding, AddPayoutViewModel>() {


    companion object {
        @JvmStatic
        fun openWebViewActivity(context: Context, url: String, screen: String) {
            val intent = Intent(context, StripeWebViewActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("screen", screen)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityStripeViewBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_stripe_view
    override val viewModel: AddPayoutViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(AddPayoutViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_stripe_view)
        initView()
    }

    //@SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        mBinding.wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view!!.settings.allowContentAccess = true
                view!!.settings.domStorageEnabled = true
                view!!.settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [FB_IAB/FB4A;FBAV/28.0.0.20.16;]"
                view!!.settings.javaScriptEnabled = true

                if (url!!.contains("/payout/success")) {
                    viewModel.onRetryCalled = "setPayout"
                    viewModel.setPayout()
                    finish()
                }

            }
        }

        intent?.getStringExtra("url")?.let {
            mBinding.wv.settings.allowContentAccess = true
            mBinding.wv.settings.domStorageEnabled = true
            mBinding.wv.settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [FB_IAB/FB4A;FBAV/28.0.0.20.16;]"
            mBinding.wv.settings.javaScriptEnabled = true
            mBinding.wv.loadUrl(intent?.getStringExtra("url").orEmpty())
        }
    }

    override fun onRetry() {

    }

}
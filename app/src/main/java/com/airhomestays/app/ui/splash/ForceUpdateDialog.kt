package com.airhomestays.app.ui.profile.manageAccount

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ForceUpdateBinding
import com.airhomestays.app.ui.base.BaseDialogFragment
import com.airhomestays.app.ui.splash.SplashNavigator
import com.airhomestays.app.ui.splash.SplashViewModel
import com.airhomestays.app.util.onClick
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class ForceUpdateDialog : BaseDialogFragment(), SplashNavigator {

    private val TAG = ForceUpdateDialog::class.java.simpleName
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    val viewModel: SplashViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(SplashViewModel::class.java)
    companion object {
        @JvmStatic
        fun newInstance() = ForceUpdateDialog()
    }
    var url="https://play.google.com/store/apps/details?id=com.airhomestays.app"



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<ForceUpdateBinding>(inflater, R.layout.force_update, container, false)
        val view = binding.root
        AndroidSupportInjection.inject(this)
        viewModel.navigator = this
        isCancelable=false
        viewModel.forceUpdate()
        viewModel.url.observe(this, Observer {
            if (it != null) {
                url=it
            }
        })
        binding.btnApply.onClick {
            val url = url
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        return view
    }

    fun show(fragmentManager: androidx.fragment.app.FragmentManager) {
        super.show(fragmentManager, TAG)
    }


    override fun openLoginActivity() {
        TODO("Not yet implemented")
    }

    override fun openMainActivity() {
        TODO("Not yet implemented")
    }

    override fun openHostActivity() {
        TODO("Not yet implemented")
    }

    override fun openInboxActivity() {
        TODO("Not yet implemented")
    }

}
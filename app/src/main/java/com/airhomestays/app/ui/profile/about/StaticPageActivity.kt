package com.airhomestays.app.ui.profile.about

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentStaticContentBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.util.epoxy.CustomSpringAnimation
import com.airhomestays.app.util.gone
import com.airhomestays.app.viewholderUserNormalText
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject


class StaticPageActivity : BaseActivity<FragmentStaticContentBinding, AboutViewModel>(){

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    lateinit var mBinding: FragmentStaticContentBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_static_content
    override val viewModel: AboutViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(AboutViewModel::class.java)

    var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!

        if (intent.extras!=null){
            id= intent.extras!!.getInt("id")
            viewModel.getStaticContent(id)
        }
        CustomSpringAnimation.spring(mBinding.rvStaticContent)
        mBinding.ivBack.setOnClickListener {
            onBackPressed()
        }
        setUp()
    }

    fun setUp(){
            viewModel.staticContentDetails.observe(this@StaticPageActivity, Observer {
                mBinding.searchLoading.gone()
                mBinding.rvStaticContent.withModels {
                    mBinding.tvHeader.text=it?.getStaticPageContent?.result?.metaTitle

                    viewholderUserNormalText {
                        id("description")
                        onBind {  _, view, _ ->
                            val textView = view.dataBinding.root.findViewById<TextView>(R.id.text1)
                            textView.setText(HtmlCompat.fromHtml(it?.getStaticPageContent?.result?.content!!,HtmlCompat.FROM_HTML_MODE_LEGACY))
                            textView.setMovementMethod(LinkMovementMethod.getInstance());
                            textView.setLinkTextColor(getColor(R.color.com_facebook_blue))
                        }
                    }
                }
            })
    }



    override fun onRetry() {

    }

}
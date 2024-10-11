package com.airhomestays.app.ui.profile.about.why_Host

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentWhyHostBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.host.step_one.StepOneActivity
import com.airhomestays.app.ui.profile.about.AboutViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.onClick
import java.util.Locale
import javax.inject.Inject

class WhyHostActivity : BaseActivity<FragmentWhyHostBinding,AboutViewModel>() {

    private val myImageList2 = intArrayOf(R.drawable.bg_image1, R.drawable.bg_image3, R.drawable.bg_image2, R.drawable.bg_image4)
    private val myImageList = ArrayList<String>()
    private val myTextList2 = intArrayOf(R.string.text_one, R.string.text_two, R.string.text_three, R.string.text_four)
    private val myTextList = ArrayList<String>()
    private val buttonList = ArrayList<String>()


    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentWhyHostBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_why_host
    override val viewModel: AboutViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(AboutViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding=viewDataBinding!!
        init()
    }

    private fun init() {
        viewModel.getWhyHostData()
        viewModel.whyHostData.observe(this) {
            it?.getWhyHostData?.results?.forEachIndexed { index, result ->
                myTextList.add(result?.title.toString())
                buttonList.add(result?.buttonLabel.toString())
                myImageList.add(result?.imageName.toString())
            }
            mBinding.pager.adapter = SlidingImageAdapter(this@WhyHostActivity, populateList())
            if(resources.getBoolean(R.bool.is_left_to_right_layout).not()){
                mBinding.pager.currentItem =mBinding.pager.adapter?.count!!-1
            }else{
                mBinding.pager.currentItem =0
            }

            mBinding.indicator.radius = 5 * resources.displayMetrics.density
            mBinding.indicator.setViewPager(mBinding.pager)
            mBinding.indicator.fillColor=resources.getColor(R.color.white_photo_story)

        }
        mBinding.ivNavigateup.onClick {
            super.onBackPressed()
        }
        mBinding.listYourSpace.onClick {
            val intent = Intent(this@WhyHostActivity, StepOneActivity::class.java)
            startActivity(intent)
        }

    }

    private fun populateList(): ArrayList<ImageModel> {
        val list = ArrayList<ImageModel>()
        for (i in 0 until myImageList.size) {
            val imageModel = ImageModel()
            imageModel.setImage_drawables(myImageList[i])
            imageModel.setText_srings(myTextList[i])
            imageModel.setButton_srings(buttonList[i])
            list.add(imageModel)
        }
        return list
    }

    override fun onRetry() {

    }
}
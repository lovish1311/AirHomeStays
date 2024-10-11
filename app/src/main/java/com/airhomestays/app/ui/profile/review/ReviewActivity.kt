package com.airhomestays.app.ui.profile.review

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityReviewBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.ui.host.hostHome.HostHomeActivity
import com.airhomestays.app.ui.profile.review.pager_adapter.ReviewPagerAdapter
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.airhomestays.app.vo.FromDeeplinks
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class ReviewActivity : BaseActivity<ActivityReviewBinding, ReviewViewModel>(),ReviewNavigator {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityReviewBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_review
    override val viewModel: ReviewViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(ReviewViewModel::class.java)
    var deepLink:Boolean =false
    var is404PageShown = false
    companion object {
        @JvmStatic fun openActivity(activity: Activity, isDeepLink: Boolean, from: FromDeeplinks, reservationId: Int) {
            val intent = Intent(activity, ReviewActivity::class.java)
            intent.putExtra("deepLink", isDeepLink)
            intent.putExtra("reservationId", reservationId)
            intent.putExtra("from", from.ordinal)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        mBinding.ivBack.onClick {
            finish()
        }

        onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })
        mBinding.btnExplore.onClick {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        setAdapter()
    }

    fun setAdapter(){
        val reservationId = intent.getIntExtra("reservationId",0)
        deepLink= intent.getBooleanExtra("deepLink",false)
        if(deepLink){
            viewModel.navigator.openWriteReview(reservationId?.toInt() ?: 0)
        }

        val myAdapter = ReviewPagerAdapter(supportFragmentManager,this)
        with(mBinding.viewPager) {
            adapter = myAdapter
            offscreenPageLimit = 2
            mBinding.tabs.post { mBinding.tabs.setupWithViewPager(this) }
            addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(p0: Int) {
                    Timber.tag("ReviewPage1").d(p0.toString())
                }
                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                    Timber.tag("ReviewPage12").d(p0.toString())
                }
                override fun onPageSelected(p0: Int) {
                    Timber.tag("ReviewPage123").d(p0.toString())
                }
            })
        }
    }


    override fun onRetry() {
        try{
            if(::mViewModelFactory.isInitialized) {
                viewModel.aboutYouResult.value?.retry?.invoke()
                viewModel.byYouResult.value?.retry?.invoke()
                viewModel.pendingResult.value?.retry?.invoke()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }


    override fun moveToScreen(screen: ReviewViewModel.ViewScreen) {
        when (screen) {
            ReviewViewModel.ViewScreen.GO_BACK_TO ->{
                supportFragmentManager.popBackStackImmediate()
            }
            ReviewViewModel.ViewScreen.GO_BACK_AND_REFRESH ->{
                supportFragmentManager.popBackStackImmediate()
                viewModel.reloadData.value="reload"
            }
        }
    }

    override fun show404Page() {
        is404PageShown=true
        mBinding.ll404Page.visible()
    }

    override fun openWriteReview(reservationId: Int) {
        viewModel.reservationId=reservationId.toString()
        openFragment(FragmentWriteReview())
    }

    fun openFragment(fragment: Fragment){
        supportFragmentManager
                .beginTransaction()
                .replace(mBinding.fragFrame.id, fragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onBackPressed() {
        if(is404PageShown){
            mBinding.ll404Page.gone()
        }
        if(supportFragmentManager.backStackEntryCount>0){
            supportFragmentManager.popBackStackImmediate()
        }else{
            if (deepLink){
                if(viewModel.dataManager.isHostOrGuest){
                    val intent = Intent(this, HostHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }else{
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }else{
                if (!mBinding.ll404Page.isVisible) {
                    finish()
                } else {
                    mBinding.ll404Page.gone()
                }
            }
        }
    }
}
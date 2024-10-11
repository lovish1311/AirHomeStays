package com.airhomestays.app.ui.booking

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityBookingBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.reservation.ReservationActivity
import com.airhomestays.app.util.addFragmentToActivity
import com.airhomestays.app.util.replaceFragmentInActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class BookingActivity : BaseActivity<ActivityBookingBinding, BookingViewModel>(), BookingNavigator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityBookingBinding
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_booking
    override val viewModel: BookingViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(BookingViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        viewModel.setInitialData(intent)
        if (savedInstanceState == null) {
            initView()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle ) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun initView() {
        addFragment(Step4Fragment(), "step1")
    }



    private fun addFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        addFragmentToActivity(mBinding.flBooking.id, fragment, tag)
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        replaceFragmentInActivity(mBinding.flBooking.id, fragment, tag)
    }

    fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 32 ) {
            setResult(35, Intent())
            finish()
        }
    }

    override fun onRetry() {

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun navigateToScreen(screen: Int) {
        hideSnackbar()
        when (screen) {
            2 -> { replaceFragment(Step2Fragment(), "step2") }
            3 -> { replaceFragment(Step3Fragment(), "step3") }
            4 -> { replaceFragment(Step4Fragment(), "step4") }
            5 -> { replaceFragment(ReviewAndPayFragment(), "step5") }
            6 -> { startActivity(Intent(this, ReservationActivity::class.java)) }
        }
    }

}
package com.airhomestays.app.ui.host.step_three

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.epoxy.databinding.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostActivityStepThreeBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.host.step_three.bookingWindow.BookWindowFragment
import com.airhomestays.app.ui.host.step_three.discount.DiscountPriceFragment
import com.airhomestays.app.ui.host.step_three.guestBooking.GuestBookingFragment
import com.airhomestays.app.ui.host.step_three.guestNotice.GuestNoticeFragment
import com.airhomestays.app.ui.host.step_three.guestReq.GuestReqFragment
import com.airhomestays.app.ui.host.step_three.houseRules.HouseRuleFragment
import com.airhomestays.app.ui.host.step_three.instantBook.InstantBookFragment
import com.airhomestays.app.ui.host.step_three.listingPrice.ListingPriceFragment
import com.airhomestays.app.ui.host.step_three.localLaws.LocalLawsFragment
import com.airhomestays.app.ui.host.step_three.tripLength.TripLengthFragment
import com.airhomestays.app.util.addFragmentToActivity
import com.airhomestays.app.util.replaceFragmentInActivity
import com.airhomestays.app.util.replaceFragmentToActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class StepThreeActivity : BaseActivity<HostActivityStepThreeBinding, StepThreeViewModel>(),
    StepThreeNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: HostActivityStepThreeBinding

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_activity_step_three
    override val viewModel: StepThreeViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(StepThreeViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        viewModel.listID = intent.getStringExtra("listID").orEmpty()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })
        subscribeToLiveData(savedInstanceState)
    }

    fun subscribeToLiveData(savedInstanceState: Bundle?) {
        viewModel.listSetting().observe(this, Observer {
            it.let {
                if (savedInstanceState == null) {
                    initView()
                }
            }
        })
    }

    fun initView() {
        addFragment(HouseRuleFragment(), "houseRule")
    }


    private fun addFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        addFragmentToActivity(mBinding.flStepThree.id, fragment, tag)
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        replaceFragmentInActivity(mBinding.flStepThree.id, fragment, tag)
    }

    fun popFragment(fragment: Fragment, tag: String) {
        replaceFragmentToActivity(mBinding.flStepThree.id, fragment, tag)
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun navigateToScreen(screen: StepThreeViewModel.NextStep) {
        try {
            when (screen) {
                StepThreeViewModel.NextStep.HOUSERULE -> {
                    replaceFragment(HouseRuleFragment(), "houseRule")
                }

                StepThreeViewModel.NextStep.GUESTBOOK -> {
                    replaceFragment(GuestBookingFragment(), "guestBook")
                }

                StepThreeViewModel.NextStep.GUESTREQUEST -> {
                    replaceFragment(GuestReqFragment(), "GUESTREQUEST")
                }

                StepThreeViewModel.NextStep.GUESTNOTICE -> {
                    replaceFragment(GuestNoticeFragment(), "guestNotice")
                }

                StepThreeViewModel.NextStep.BOOKWINDOW -> {
                    if (viewModel.checkDiscount())
                        replaceFragment(BookWindowFragment(), "bookWindow")
                }

                StepThreeViewModel.NextStep.TRIPLENGTH -> {
                    replaceFragment(TripLengthFragment(), "tripLength")
                }

                StepThreeViewModel.NextStep.LISTPRICE -> {
                    if (viewModel.fromChoosen.equals("From") || viewModel.toChoosen.equals("To")) {
                        showSnackbar(
                            getString(R.string.error),
                            getString(R.string.check_in_error)
                        )
                    } else if (!viewModel.fromChoosen.equals("Flexible") && !viewModel.toChoosen.equals(
                            "Flexible"
                        )
                    ) {
                        if (viewModel.fromChoosen.toInt() >= viewModel.toChoosen.toInt()) {
                            showSnackbar(
                                getString(R.string.time),
                                getString(R.string.checkin_error_text)
                            )
                        } else {
                            replaceFragment(ListingPriceFragment(), "listPrice")
                        }
                    } else {
                        replaceFragment(ListingPriceFragment(), "listPrice")
                    }

                }

                StepThreeViewModel.NextStep.DISCOUNTPRICE -> {
                    replaceFragment(DiscountPriceFragment(), "discountPrice")
                }

                StepThreeViewModel.NextStep.INSTANTBOOK -> {
                    if (viewModel.checkTripLength())
                        replaceFragment(InstantBookFragment(), "instantBook")

                }

                StepThreeViewModel.NextStep.LAWS -> {
                    replaceFragment(LocalLawsFragment(), "localLaws")
                }

                StepThreeViewModel.NextStep.FINISH -> {
                    this.finish()
                }

                StepThreeViewModel.NextStep.NODATA -> {

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun navigateBack(backScreen: StepThreeViewModel.BackScreen) {
        hideKeyboard()
        when (backScreen) {
            StepThreeViewModel.BackScreen.GUESTREQUEST -> popFragment(
                GuestReqFragment(),
                "GUESTREQUEST"
            )

            StepThreeViewModel.BackScreen.HOUSERULE -> {
                popFragment(HouseRuleFragment(), "houseRule")
            }

            StepThreeViewModel.BackScreen.GUESTBOOK -> popFragment(
                GuestBookingFragment(),
                "guestBook"
            )

            StepThreeViewModel.BackScreen.GUESTNOTICE -> popFragment(
                GuestNoticeFragment(),
                "guestNotice"
            )

            StepThreeViewModel.BackScreen.BOOKWINDOW -> popFragment(
                BookWindowFragment(),
                "bookWindow"
            )

            StepThreeViewModel.BackScreen.TRIPLENGTH -> popFragment(
                TripLengthFragment(),
                "tripLength"
            )

            StepThreeViewModel.BackScreen.LISTPRICE -> popFragment(
                ListingPriceFragment(),
                "listPrice"
            )

            StepThreeViewModel.BackScreen.DISCOUNTPRICE -> popFragment(
                DiscountPriceFragment(),
                "discountPrice"
            )

            StepThreeViewModel.BackScreen.INSTANTBOOK -> popFragment(
                InstantBookFragment(),
                "instantBook"
            )

            StepThreeViewModel.BackScreen.LAWS -> popFragment(LocalLawsFragment(), "localLaws")
            StepThreeViewModel.BackScreen.FINISH -> {}
            StepThreeViewModel.BackScreen.NODATA -> {}
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onRetry() {
        if (viewModel.retryCalled.equals("")) {
            viewModel.getListStep3Details()
        } else if (viewModel.retryCalled.equals("update")) {
            viewModel.updateListStep3("edit")
        }
    }

    override fun onBackPressed() {
        hideKeyboard()
        val myFrag = supportFragmentManager.findFragmentByTag("GUESTREQUEST")
        val myFrag2 = supportFragmentManager.findFragmentByTag("houseRule")
        val myFrag3 = supportFragmentManager.findFragmentByTag("guestBook")
        val myFrag4 = supportFragmentManager.findFragmentByTag("guestNotice")
        val myFrag5 = supportFragmentManager.findFragmentByTag("bookWindow")
        val myFrag6 = supportFragmentManager.findFragmentByTag("tripLength")
        val myFrag7 = supportFragmentManager.findFragmentByTag("listPrice")
        val myFrag8 = supportFragmentManager.findFragmentByTag("discountPrice")
        val myFrag9 = supportFragmentManager.findFragmentByTag("instantBook")
        val myFrag10 = supportFragmentManager.findFragmentByTag("localLaws")
        if (myFrag != null && myFrag.isVisible) {
            popFragment(InstantBookFragment(), "instantBook")
        } else if (myFrag2 != null && myFrag2.isVisible) {
            super.finish()
        } else if (myFrag3 != null && myFrag3.isVisible) {
            popFragment(HouseRuleFragment(), "houseRule")
        } else if (myFrag4 != null && myFrag4.isVisible) {
            popFragment(HouseRuleFragment(), "houseRule")
        } else if (myFrag5 != null && myFrag5.isVisible) {
            popFragment(DiscountPriceFragment(), "discountPrice")
        } else if (myFrag6 != null && myFrag6.isVisible) {
            popFragment(BookWindowFragment(), "bookWindow")
        } else if (myFrag7 != null && myFrag7.isVisible) {
            popFragment(GuestNoticeFragment(), "guestNotice")
        } else if (myFrag8 != null && myFrag8.isVisible) {
            popFragment(ListingPriceFragment(), "listPrice")
        } else if (myFrag9 != null && myFrag9.isVisible) {
            popFragment(BookWindowFragment(), "bookWindow")
        } else if (myFrag10 != null && myFrag10.isVisible) {
            popFragment(GuestReqFragment(), "GUESTREQUEST")
        } else {
            finish()
        }
    }

    override fun show404Page() {
        showToast(getString(R.string.list_not_available))
        this.finish()
    }
}
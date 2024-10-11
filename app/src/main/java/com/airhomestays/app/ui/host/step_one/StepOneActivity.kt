package com.airhomestays.app.ui.host.step_one

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.toolbox.Volley
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.HostActivityStepOneBinding
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.host.HostFinalActivity
import com.airhomestays.app.util.addFragmentToActivity
import com.airhomestays.app.util.replaceFragmentInActivity
import com.airhomestays.app.util.replaceFragmentToActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject


class StepOneActivity : BaseActivity<HostActivityStepOneBinding, StepOneViewModel>(),
    StepOneNavigator {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: HostActivityStepOneBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.host_activity_step_one
    override val viewModel: StepOneViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(StepOneViewModel::class.java)
    var strUser: String = ""
    var isReCreated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })
        viewModel.requestQueue = Volley.newRequestQueue(this)
        if (viewModel.uiMode == null)
            viewModel.uiMode = resources.configuration.uiMode
        else
            isReCreated = true

        if (getIntent().hasExtra("from")) {
            strUser = intent.getStringExtra("from").orEmpty()
        }
        if (strUser.isNotEmpty() && strUser.equals("steps")) {
            if (viewModel.listId.get()!!.isEmpty())
                viewModel.listId.set(intent.getStringExtra("listID"))
            viewModel.isListAdded = true
            viewModel.yesNoString.set(intent.getStringExtra("yesNoString"))
            viewModel.bathroomCapacity.set(intent.getStringExtra("bathroomCapacity"))
            viewModel.country.set(intent.getStringExtra("country"))
            viewModel.countryCode.set(intent.getStringExtra("countryCode"))
            if (intent.getStringExtra("street")!!.isNotEmpty())
                viewModel.street.set(intent.getStringExtra("street"))
            viewModel.buildingName.set(intent.getStringExtra("buildingName"))
            viewModel.city.set(intent.getStringExtra("city"))
            viewModel.state.set(intent.getStringExtra("state"))
            viewModel.zipcode.set(intent.getStringExtra("zipcode"))
            viewModel.lat.set(intent.getStringExtra("lat"))
            viewModel.lng.set(intent.getStringExtra("lng"))
            viewModel.isEdit = true
            viewModel.getListingSetting("edit")
        } else {
            if (!isReCreated) {
                viewModel.isEdit = false
                addFragmentToActivity(mBinding.flSteps.id, WelcomeFragment(), "WELCOME")
            }
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    fun openFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(mBinding.flSteps.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun popFragment(fragment: Fragment, tag: String) {
        replaceFragmentToActivity(mBinding.flSteps.id, fragment, tag)
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        replaceFragmentInActivity(mBinding.flSteps.id, fragment, tag)
    }

    override fun onRetry() {
        if (viewModel.retryCalled.equals("")) {
            viewModel.step1Retry(strUser)
        } else {
            viewModel.updateHostStepOne(true)
            viewModel.getCountryCode()
        }
    }

    override fun navigateScreen(NextScreen: StepOneViewModel.NextScreen) {
        hideKeyboard()
        when (NextScreen) {
            StepOneViewModel.NextScreen.NO_OF_GUEST -> replaceFragment(
                NoOfGuestFragment(),
                "NO_OF_GUEST"
            )

            StepOneViewModel.NextScreen.KIND_OF_PLACE -> {
                replaceFragment(KindOfPlaceFragment(), "KIND_OF_PLACE")
            }

            StepOneViewModel.NextScreen.TYPE_OF_BEDS -> replaceFragment(
                TypeOfBedsFragment(),
                "BedRooms"
            )

            StepOneViewModel.NextScreen.TYPE_OF_SPACE -> replaceFragment(
                TypeOfSpaceFragment(),
                "TYPE_OF_SPACE"
            )

            StepOneViewModel.NextScreen.NO_OF_BATHROOM -> {
                viewModel.editBedCount = 0
                viewModel.updateCount.value?.forEachIndexed { index, s ->
                    viewModel.editBedCount = viewModel.editBedCount + s.toInt()
                }
                if (viewModel.bedCapacity.get()!!.toInt() < viewModel.editBedCount) {
                    showSnackbar(
                        getString(R.string.bed_count),
                        getString(R.string.choosen_bed_count_is_exceeded_than_bed_for_guest_count)
                    )
                } else {
                    replaceFragment(NoOfBathroomFragment(), "NO_OF_BATHROOM")
                }
            }

            StepOneViewModel.NextScreen.ADDRESS -> replaceFragment(AddressFragment(), "ADDRESS")
            StepOneViewModel.NextScreen.MAP_LOCATION -> replaceFragment(
                MaplocationFragment(),
                "MAP_LOCATION"
            )

            StepOneViewModel.NextScreen.AMENITIES -> replaceFragment(
                AmenitiesFragment(),
                "AMENITIES"
            )

            StepOneViewModel.NextScreen.SAFETY_PRIVACY -> replaceFragment(
                SafetynPrivacyFragment(),
                "SAFETY_PRIVACY"
            )

            StepOneViewModel.NextScreen.GUEST_SPACE -> replaceFragment(
                GuestSpacesFragment(),
                "GUEST_SPACE"
            )

            StepOneViewModel.NextScreen.SELECT_COUNTRY -> replaceFragment(
                SelectCountry(),
                "SELECT_COUNTRY"
            )

            StepOneViewModel.NextScreen.SAVE_N_EXIT -> {}
            StepOneViewModel.NextScreen.FINISHED -> {
                val intent = Intent(this@StepOneActivity, HostFinalActivity::class.java)
                intent.putExtra("listId", viewModel.listId.get())
                intent.putExtra("yesNoString", "Yes")
                intent.putExtra("bathroomCapacity", "0")
                intent.putExtra("country", "")
                intent.putExtra("countryCode", "")
                intent.putExtra("street", "")
                intent.putExtra("buildingName", "")
                intent.putExtra("city", "")
                intent.putExtra("state", "")
                intent.putExtra("zipcode", "")
                intent.putExtra("lat", "")
                intent.putExtra("lng", "")
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
    }

    override fun navigateBack(BackScreen: StepOneViewModel.BackScreen) {
        hideKeyboard()
        when (BackScreen) {
            StepOneViewModel.BackScreen.WELCOME -> popFragment(WelcomeFragment(), "WELCOME")
            StepOneViewModel.BackScreen.TYPE_OF_SPACE -> popFragment(
                TypeOfSpaceFragment(),
                "TYPE_OF_SPACE"
            )

            StepOneViewModel.BackScreen.KIND_OF_PLACE -> {
                popFragment(KindOfPlaceFragment(), "KIND_OF_PLACE")
            }

            StepOneViewModel.BackScreen.NO_OF_GUEST -> popFragment(
                NoOfGuestFragment(),
                "NO_OF_GUEST"
            )

            StepOneViewModel.BackScreen.TYPE_OF_BEDS -> popFragment(
                TypeOfBedsFragment(),
                "BedRooms"
            )

            StepOneViewModel.BackScreen.NO_OF_BATHROOM -> popFragment(
                NoOfBathroomFragment(),
                "NO_OF_BATHROOM"
            )

            StepOneViewModel.BackScreen.ADDRESS -> popFragment(AddressFragment(), "ADDRESS")
            StepOneViewModel.BackScreen.MAP_LOCATION -> popFragment(
                MaplocationFragment(),
                "MAP_LOCATION"
            )

            StepOneViewModel.BackScreen.AMENITIES -> popFragment(AmenitiesFragment(), "AMENITIES")
            StepOneViewModel.BackScreen.SAFETY_PRIVACY -> popFragment(
                SafetynPrivacyFragment(),
                "SAFETY_PRIVACY"
            )

            StepOneViewModel.BackScreen.GUEST_SPACE -> popFragment(
                GuestSpacesFragment(),
                "GUEST_SPACE"
            )
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (isReCreated) {
            if (viewModel.isEdit)
                if (viewModel.currentFragment != null)
                    replaceFragment(viewModel.currentFragment!!, viewModel.fragmentTag!!)
        }

    }

    override fun onBackPressed() {
        hideKeyboard()
        val myFrag = supportFragmentManager.findFragmentByTag("KIND_OF_PLACE")
        val myFrag2 = supportFragmentManager.findFragmentByTag("NO_OF_GUEST")
        val myFrag5 = supportFragmentManager.findFragmentByTag("ADDRESS")
        val myFrag6 = supportFragmentManager.findFragmentByTag("MAP_LOCATION")
        val myFrag7 = supportFragmentManager.findFragmentByTag("AMENITIES")
        val myFrag8 = supportFragmentManager.findFragmentByTag("SAFETY_PRIVACY")
        val myFrag9 = supportFragmentManager.findFragmentByTag("GUEST_SPACE")
        val myFrag10 = supportFragmentManager.findFragmentByTag("SELECT_COUNTRY")
        val myFrag11 = supportFragmentManager.findFragmentByTag("TYPE_OF_SPACE")
        val myFrag12 = supportFragmentManager.findFragmentByTag("WELCOME")
        if (myFrag != null && myFrag.isVisible && myFrag.isAdded) {
            if (strUser.isNotEmpty() && strUser.equals("steps")) {
                if (viewModel.isEdit) {
                    val intent = Intent(this, HostFinalActivity::class.java)
                    intent.putExtra("listId", viewModel.listId.get())
                    intent.putExtra("yesNoString", "Yes")
                    intent.putExtra("bathroomCapacity", "0")
                    intent.putExtra("country", "")
                    intent.putExtra("countryCode", "")
                    intent.putExtra("street", "")
                    intent.putExtra("buildingName", "")
                    intent.putExtra("city", "")
                    intent.putExtra("state", "")
                    intent.putExtra("zipcode", "")
                    intent.putExtra("lat", "")
                    intent.putExtra("lng", "")
                    startActivity(intent)
                    finish()
                } else {
                    finish()
                }
            } else {
                popFragment(TypeOfSpaceFragment(), "TYPE_OF_SPACE")
            }
        } else if (myFrag2 != null && myFrag2.isVisible) {
            popFragment(KindOfPlaceFragment(), "KIND_OF_PLACE")
        } else if (myFrag5 != null && myFrag5.isVisible) {
            popFragment(NoOfGuestFragment(), "NO_OF_GUEST")
        } else if (myFrag6 != null && myFrag6.isVisible) {
            popFragment(AddressFragment(), "ADDRESS")
        } else if (myFrag7 != null && myFrag7.isVisible) {
            popFragment(MaplocationFragment(), "MAP_LOCATION")
        } else if (myFrag8 != null && myFrag8.isVisible) {
            popFragment(AmenitiesFragment(), "AMENITIES")
        } else if (myFrag9 != null && myFrag9.isVisible) {
            popFragment(SafetynPrivacyFragment(), "SAFETY_PRIVACY")
        } else if (myFrag10 != null && myFrag10.isVisible) {
            popFragment(AddressFragment(), "ADDRESS")
        } else if (myFrag11 != null && myFrag11.isVisible) {
            popFragment(WelcomeFragment(), "WELCOME")
        } else if (myFrag12 != null && myFrag12.isVisible) {
            if (viewModel.isEdit) {
                val intent = Intent(this, HostFinalActivity::class.java)
                intent.putExtra("listId", viewModel.listId.get())
                intent.putExtra("yesNoString", "Yes")
                intent.putExtra("bathroomCapacity", "0")
                intent.putExtra("country", "")
                intent.putExtra("countryCode", "")
                intent.putExtra("street", "")
                intent.putExtra("buildingName", "")
                intent.putExtra("city", "")
                intent.putExtra("state", "")
                intent.putExtra("zipcode", "")
                intent.putExtra("lat", "")
                intent.putExtra("lng", "")
                startActivity(intent)
                finish()
            } else {
                finish()
            }
        }
    }


    override fun onPause() {
        super.onPause()
        viewModel.currentFragment = supportFragmentManager.findFragmentById(mBinding.flSteps.id)
        viewModel.fragmentTag = viewModel.currentFragment!!.tag
    }

    override fun show404Page() {
        showToast(getString(R.string.list_not_available))
        val intent = Intent(this@StepOneActivity, HostFinalActivity::class.java)
        intent.putExtra("listId", viewModel.listId.get())
        intent.putExtra("yesNoString", "Yes")
        intent.putExtra("bathroomCapacity", "0")
        intent.putExtra("country", "")
        intent.putExtra("countryCode", "")
        intent.putExtra("street", "")
        intent.putExtra("buildingName", "")
        intent.putExtra("city", "")
        intent.putExtra("state", "")
        intent.putExtra("zipcode", "")
        intent.putExtra("lat", "")
        intent.putExtra("lng", "")
        startActivity(intent)
        this.finish()
    }

}
package com.airhomestays.app.host.payout.editpayout

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.GetSecPaymentQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityPayoutBinding
import com.airhomestays.app.host.payout.addPayout.AddPayoutActivity
import com.airhomestays.app.ui.WebViewActivity
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.profile.edit_profile.RC_LOCATION_PERM
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderLoader
import com.airhomestays.app.viewholderReviewDetail
import com.airhomestays.app.vo.Payout
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject


class EditPayoutActivity : BaseActivity<ActivityPayoutBinding, PayoutViewModel>(),
    EditPayoutNavigator,EasyPermissions.PermissionCallbacks {

    enum class Screen {
        INTRO,
        INFO,
        PAYOUTTYPE,
        PAYOUTDETAILS,
        PAYPALDETAILS,
        WEBVIEW,
        FINISH
    }

    companion object {
        @JvmStatic
        fun openActivity(activity: Activity) {
            val intent = Intent(activity, EditPayoutActivity::class.java)
            activity.startActivity(intent)
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityPayoutBinding

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_payout
    override val viewModel: PayoutViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(PayoutViewModel::class.java)
    var payoutList = ArrayList<Payout>()
    var detail : GetSecPaymentQuery.Data? = null
    var detailList : ArrayList<CommonModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
        setUp()
    }

    private fun initView() {
        mBinding.rlToolbarNavigateup.onClick {
            finish()
        }
        viewModel.getSecPayment()
        if (payoutList.isNotEmpty()) {

        }
        mBinding.containerUpdateButton.visibility = View.GONE
        mBinding.containerAddPayoutMethod.visibility = View.VISIBLE

        mBinding.containerAddPayoutMethod.onClick {

            openFragment(CountryFragment(), "Country")
        }
        mBinding.btnUpdate.setOnClickListener {
         AddPayoutActivity.openActivityUpdate(this,detail)
        /*    val intent = Intent(this, AddPayoutActivity::class.java)
           startActivity(intent)*/
           supportFragmentManager?.popBackStackImmediate()
        }
        mBinding.btnOk.setOnClickListener {
        finish()
           supportFragmentManager?.popBackStackImmediate()
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.payoutsList.value != null) {
            viewModel.getPayouts()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.loadPayouts().observe(this, Observer {
            it?.let { result ->
                payoutList = result
                mBinding.rlEditPayout.requestModelBuild()
            }
        })

        viewModel.loadPayment().observe(this,Observer{
            it?.let { result ->
                detail = result
                if (result!=null && result.getSecPayment?.result?.accountNumber!=null){
                    mBinding.containerUpdateButton.visibility = View.VISIBLE
                    mBinding.containerAddPayoutMethod.visibility = View.INVISIBLE

                }else{
                    mBinding.containerUpdateButton.visibility = View.GONE
                    mBinding.containerAddPayoutMethod.visibility = View.VISIBLE

                }
                detailList.clear()
                detailList.add(CommonModel("Country",result.getSecPayment?.result?.country!!))
                detailList.add(CommonModel("Address line 1",result.getSecPayment.result.address1!!))
                detailList.add(CommonModel("Address line 2",result.getSecPayment?.result?.address2!!))
                detailList.add(CommonModel("City",result.getSecPayment?.result?.city!!))
                detailList.add(CommonModel("State",result.getSecPayment?.result?.state!!))
                detailList.add(CommonModel("ZipCode",result.getSecPayment?.result?.zipcode!!))
                detailList.add(CommonModel("Account type",result.getSecPayment?.result?.accountType!!))
                detailList.add(CommonModel("Account Holder name",result.getSecPayment?.result?.accountHolderName!!))
                detailList.add(CommonModel("Mobile number",result.getSecPayment?.result?.mobileNumber!!))
                detailList.add(CommonModel("Account number",result.getSecPayment?.result?.accountNumber!!))
                detailList.add(CommonModel("cnf Account number",result.getSecPayment?.result?.confirmAccountNumber!!))
                detailList.add(CommonModel("IFSC code",result.getSecPayment?.result?.ifscCode!!))
                detailList.add(CommonModel("GST number",result.getSecPayment?.result?.gstNumber!!))
                detailList.add(CommonModel("PAN number",result.getSecPayment?.result?.panNumber!!))
                mBinding.rlEditPayout.requestModelBuild()
            }
        })
    }

    private fun setUp() {
        mBinding.rlEditPayout.withModels {
            if (viewModel.isLoading.get()) {
                viewholderLoader {
                    id("Loader")
                    isLoading(true)
                }
            } else {
                if (detail != null) {
                    if (detail?.getSecPayment?.result?.accountNumber?.isNotEmpty() == true) {
                        detailList.forEachIndexed { index, commonModel ->
                            viewholderReviewDetail {
                                // Use a unique id for each item to avoid recycling issues
                                id("ReviewDetail_${detail?.getSecPayment?.result?.id}_$index")

                                onBind { _, view, _ ->
                                    val tvHeader = view.dataBinding.root.findViewById<TextView>(R.id.tvHeader)
                                    val tvSubHeader = view.dataBinding.root.findViewById<TextView>(R.id.tvSubHeader)

                                    // Bind your header and sub-header text
                                    tvHeader.text = commonModel.header
                                    tvSubHeader.text = commonModel.subHeader
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun openFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_up,
                R.anim.slide_down,
                R.anim.slide_up,
                R.anim.slide_down
            )
            .add(mBinding.flEditPayout.id, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    override fun disableCountrySearch(flag: Boolean) {
        val fragment = supportFragmentManager.findFragmentById(mBinding.flEditPayout.id)
        if (fragment is CountryFragment) {
            fragment.disableCountrySearch(flag)
        }
    }

    override fun moveToScreen(screen: Screen) {
        when (screen.name) {
            Screen.WEBVIEW.name -> {
                askCameraPermission()
            }

            Screen.FINISH.name -> finish()
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onBackPressed() {
        viewModel.listSearch.value = null
        super.onBackPressed()
    }
    @AfterPermissionGranted(RC_LOCATION_PERM)
    private fun askCameraPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            if (EasyPermissions.hasPermissions(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA

                )
            ) {
                WebViewActivity.openWebViewActivity(
                    this,
                    viewModel.connectingURL,
                    "EditStripe Onboarding-${viewModel.accountID}"
                )
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Permission to access your gallery and photos",
                    RC_LOCATION_PERM,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA
                )
            }
        } else {
            if (EasyPermissions.hasPermissions(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA

                )
            ) {
                WebViewActivity.openWebViewActivity(
                    this,
                    viewModel.connectingURL,
                    "EditStripe Onboarding-${viewModel.accountID}"
                )
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Permission to access your gallery and photos",
                    RC_LOCATION_PERM, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            }
        }

    }
    override fun onRetry() {
        val fragment = supportFragmentManager.findFragmentById(mBinding.flEditPayout.id)
        if (fragment is CountryFragment) {
            viewModel.getCountryCode()
        } else {
            viewModel.getPayouts()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Timber.tag("AddPayoutActivity").d("Permission Denied!!")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Timber.tag("AddPayoutActivity").d("Permission Denied!!")
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

}
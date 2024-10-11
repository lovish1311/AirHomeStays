package com.airhomestays.app.host.payout.addPayout

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.GetSecPaymentQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ActivityAddPayoutBinding
import com.airhomestays.app.ui.WebViewActivity
import com.airhomestays.app.ui.base.BaseActivity
import com.airhomestays.app.ui.profile.edit_profile.RC_LOCATION_PERM
import com.airhomestays.app.util.addFragmentToActivity
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.replaceFragmentInActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject

class AddPayoutActivity : BaseActivity<ActivityAddPayoutBinding, AddPayoutViewModel>(), AddPayoutNavigator, EasyPermissions.PermissionCallbacks {
    var connectUrl = ""
    enum class Screen {
        INFO,
        PAYOUTTYPE,
        PAYOUTDETAILS,
        BANKDETAILS,
        PAYPALDETAILS,
        WEBVIEW,
        FINISH,
    }

    companion object {
        @JvmStatic fun openActivity(activity: Activity, countryName: String, countryCode: String) {
            val intent = Intent(activity, AddPayoutActivity::class.java)
            intent.putExtra("country", countryName)
            intent.putExtra("countryCode", countryCode)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
        }

        @JvmStatic fun openActivityUpdate(activity: Activity, detail: GetSecPaymentQuery.Data?) {
            val intent = Intent(activity, AddPayoutActivity::class.java)


            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
        }

        @JvmStatic fun openActivityFromWebView(activity: Activity, status: String, accountId: String) {
            val intent = Intent(activity, AddPayoutActivity::class.java)
            intent.putExtra("from", "webview")
            intent.putExtra("status", status)
            intent.putExtra("accountId", accountId)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
        }
    }

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: ActivityAddPayoutBinding

    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_add_payout
    override val viewModel: AddPayoutViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(AddPayoutViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
        mBinding = viewDataBinding!!
        if (intent?.hasExtra("accountId")!!) {
            viewModel.accountID = intent.getStringExtra("accountId").orEmpty()
        }
        initView()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun initView() {
        if (intent?.hasExtra("from")!!) {
            val status = intent?.getStringExtra("status").orEmpty()
            if (status.equals("success")) {
                viewModel.setPayout()
            }
        } else {

            viewModel.country.set(intent?.getStringExtra("country"))
            viewModel.countryCode.set(intent?.getStringExtra("countryCode"))
            viewModel.getSecPayment()
        }
        with(mBinding.ivBack) {
            setImageResource(R.drawable.ic_left_arrow)
            onClick {
                onBackPressed()
            }
        }
        mBinding.tvHeader.text = getText(R.string.address)
        addFragment(PayoutAccountInfoFragment(), "PaymentInfo")


    }

    private fun addFragment(fragment: Fragment, tag: String) {
        addFragmentToActivity(mBinding.flAddPayout.id, fragment, tag)
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        if (tag.equals("PaymentType")) {
            mBinding.tvHeader.text = getText(R.string.choose_how_we_pay_you)
        } else if (tag.equals("PaymentInfo")) {
            mBinding.tvHeader.text = getText(R.string.address)
        } else if (tag.equals("BankDetail")) {
            mBinding.tvHeader.text = getText(R.string.bank_detail)
        } else if (tag.equals("PaypalDetails")) {
            mBinding.tvHeader.text = getText(R.string.paypal)
        } else if (tag.equals("PaymentDetails")) {
            mBinding.tvHeader.text = getText(R.string.account_details_of_payout)
        }
        replaceFragmentInActivity(mBinding.flAddPayout.id, fragment, tag)
    }

    override fun moveToScreen(screen: Screen) {
        when (screen.name) {
            Screen.INFO.name -> {
                replaceFragment(PayoutAccountInfoFragment(), "PaymentInfo")
                mBinding.tvHeader.text = getString(R.string.address)
            }
            Screen.PAYOUTTYPE.name -> replaceFragment(PaymentTypeFragment(), "PaymentType")
            Screen.BANKDETAILS.name -> replaceFragment(AddBankAccountDetailFragment(), "BankDetails")
            Screen.PAYOUTDETAILS.name -> replaceFragment(PayoutAccountDetailFragment(), "PaymentDetails")
            Screen.PAYPALDETAILS.name -> replaceFragment(PayoutPaypalDetailsFragment(), "PaypalDetails")
            Screen.WEBVIEW.name -> {
                askCameraPermission()
            }
            Screen.FINISH.name -> { finish() }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (supportFragmentManager.findFragmentByTag("PaymentType")?.isVisible == true) {
            mBinding.tvHeader.text = getText(R.string.choose_how_we_pay_you)
        } else if (supportFragmentManager.findFragmentByTag("PaymentInfo")?.isVisible == true) {
            mBinding.tvHeader.text = getText(R.string.address)
        } else if (supportFragmentManager.findFragmentByTag("BankDetails")?.isVisible == true) {
            mBinding.tvHeader.text = getText(R.string.bank_detail)
        } else if (supportFragmentManager.findFragmentByTag("PaypalDetails")?.isVisible == true) {
            mBinding.tvHeader.text = getText(R.string.paypal)
        }
    }

    fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onRetry() {
        val fragment = supportFragmentManager.findFragmentById(mBinding.flAddPayout.id)
        if (fragment is PaymentTypeFragment) {
            viewModel.getPayoutMethods()
        } else if (fragment is PayoutPaypalDetailsFragment) {
            fragment.checkDetails()
        }
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    private fun askCameraPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            if (EasyPermissions.hasPermissions(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA,
                )
            ) {
                WebViewActivity.openWebViewActivity(
                    this,
                    viewModel.connectingURL,
                    "AddStripe Onboarding-${viewModel.accountID}",
                )
                finish()
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Permission to access your gallery and photos",
                    RC_LOCATION_PERM,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA,
                )
            }
        } else {
            if (EasyPermissions.hasPermissions(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                )
            ) {
                WebViewActivity.openWebViewActivity(
                    this,
                    viewModel.connectingURL,
                    "AddStripe Onboarding-${viewModel.accountID}",
                )
                finish()
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Permission to access your gallery and photos",
                    RC_LOCATION_PERM,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                )
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Timber.tag("AddPayoutActivity").d("Permission Denied!!")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Timber.tag("AddPayoutActivity").d("Permission Denied!!")
            AppSettingsDialog.Builder(this).build().show()
        }
    }
}

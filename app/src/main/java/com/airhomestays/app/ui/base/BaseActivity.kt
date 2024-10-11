package com.airhomestays.app.ui.base

import android.annotation.TargetApi
import android.app.UiModeManager
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.ui.AuthTokenExpireActivity
import com.airhomestays.app.util.LocaleHelper
import com.airhomestays.app.util.LocaleHelper.onAttachAndGetConfig
import com.airhomestays.app.util.NetworkUtils
import com.airhomestays.app.util.Utils
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber
import java.util.Locale


abstract class BaseActivity<T : ViewDataBinding, V : BaseViewModel<*>> : DaggerAppCompatActivity(),
    BaseFragment.Callback, BaseNavigator, DialogInterface {


    var viewDataBinding: T? = null
        private set
    private var mViewModel: V? = null
    var snackbar: Snackbar? = null
    var topView: View? = null

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    abstract val bindingVariable: Int

    /**
     * @return layout resource id
     */
    @get:LayoutRes
    abstract val layoutId: Int

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract val viewModel: V

    val isNetworkConnected: Boolean
        get() = NetworkUtils.isNetworkConnected(applicationContext)

    abstract fun onRetry()

    override fun onFragmentAttached() {

    }

    override fun onFragmentDetached(tag: String) {

    }

    private fun appTheme() {
        val pref: SharedPreferences = getSharedPreferences("THEME", Context.MODE_PRIVATE)
        val uiModeManager = this.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        when (pref.getString("appTheme", "Auto")) {
            "Auto" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }

            "Dark" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }

            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        appTheme()
        performDependencyInjection()
        super.onCreate(savedInstanceState)
        performDataBinding()
    }


    override fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun hideLoading() {

    }

    override fun openSessionExpire(logInStatus: String) {
        if (logInStatus != Constants.WITHOUT_LOGIN)
            AuthTokenExpireActivity.openActivity(this)
    }

    fun performDependencyInjection() {
        AndroidInjection.inject(this)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissionsSafely(permissions: Array<String>, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
    }

    fun showLoading() {
//        hideLoading()
//        mProgressDialog = CommonUtils.showLoadingDialog(this)
    }

    private fun performDataBinding() {
        viewDataBinding = DataBindingUtil.setContentView(this, layoutId)
        this.mViewModel = if (mViewModel == null) viewModel else mViewModel
        viewDataBinding!!.setVariable(bindingVariable, mViewModel)
        viewDataBinding!!.executePendingBindings()
    }

    override fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showSnackbar(title: String, msg: String, action: String?) {
        hideSnackbar()
        snackbar = if (topView == null) {
            Utils.showSnackBar(this, viewDataBinding!!.root, title, msg)
        } else {
            Utils.showSnackBar(this, topView!!, title, msg)
        }
    }

    override fun showSnackbarWithRetry() {
        hideSnackbar()
    }

    override fun dismiss() {
        dismiss()
    }

    override fun cancel() {
        dismiss()
    }

    override fun showError(e: Exception?) {
        hideSnackbar()
        Timber.e(e, "ERROR_APP")
        snackbar = if (topView == null) {
            Utils.showSnackBar(
                this, viewDataBinding!!.root, " ",
                resources.getString(R.string.something_went_wrong)
            ) // + e?.message
        } else {
            Utils.showSnackBar(
                this, topView!!, "",
                resources.getString(R.string.something_went_wrong)
            ) // + e?.message
        }
    }

    override fun showOffline() {
        hideSnackbar()
        snackbar = if (topView == null) {
            Utils.showSnackbarWithAction2(
                this,
                viewDataBinding!!.root,
                Utils.getHtmlText(this, "", resources.getString(R.string.currently_offline)),
                resources.getString(R.string.retry)
            ) { onRetry() }
        } else {
            Utils.showSnackbarWithAction2(
                this,
                topView!!,
                Utils.getHtmlText(this, "", resources.getString(R.string.currently_offline)),
                resources.getString(R.string.retry)
            ) { onRetry() }
        }
    }

    override fun hideSnackbar() {
        snackbar?.let {
            if (it.isShown) {
                it.dismiss()
            }
        }
    }

    fun checkNetwork(action: () -> Unit) {
        if (isNetworkConnected) {
            action()
        } else {
            showOffline()
        }
    }
    override fun onResume() {
        appTheme()
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val langType = preferences.getString("Locale.Helper.Selected.Language", "en")
        langType?.let { Locale(it) }?.let { Locale.setDefault(it) }
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        super.onResume()
    }

    override fun onDestroy() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val langType = preferences.getString("Locale.Helper.Selected.Language", "en")
        langType?.let { Locale(it) }?.let { Locale.setDefault(it) }
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        super.onDestroy()
    }

    override fun attachBaseContext(base: Context) {
        val context = LocaleHelper.onAttach(base)
        applyOverrideConfiguration(onAttachAndGetConfig(base))
        Timber.d("LangCheck attachBaseContext baseact " + context.resources.configuration.locale.displayLanguage)
        super.attachBaseContext(base)
    }

}


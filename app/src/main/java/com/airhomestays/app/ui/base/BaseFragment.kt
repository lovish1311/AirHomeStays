package com.airhomestays.app.ui.base

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment


abstract class BaseFragment<T : ViewDataBinding, V : BaseViewModel<*>> : DaggerFragment(),
    BaseNavigator {

    var baseActivity: BaseActivity<*, *>? = null
        private set
    private var mRootView: View? = null
    var viewDataBinding: T? = null
        private set
    private var mViewModel: V? = null
    private var snackbar: Snackbar? = null

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
        get() = baseActivity != null && baseActivity!!.isNetworkConnected

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*, *>) {
            this.baseActivity = context
            context.onFragmentAttached()
        }
    }

    private fun appTheme() {
        val pref: SharedPreferences =
            baseActivity!!.getSharedPreferences("THEME", Context.MODE_PRIVATE)
        println("pref:: ${pref.getString("appTheme", "Auto")}")
        val uiModeManager = baseActivity!!.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager

        when (pref.getString("appTheme", "Auto")) {
            "Auto" -> {
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }

            "Dark" -> {
                //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    uiModeManager?.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }

            else -> {
               // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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
        mViewModel = viewModel
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        mRootView = viewDataBinding!!.root
        return mRootView
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding!!.setVariable(bindingVariable, mViewModel)
        viewDataBinding!!.executePendingBindings()
    }

    override fun hideKeyboard() {
        if (baseActivity != null) {
            baseActivity!!.hideKeyboard()
        }
    }

    override fun openSessionExpire(logInStatus: String) {
        if (baseActivity != null) {
            baseActivity!!.openSessionExpire("BaseFragment")
        }
    }

    private fun performDependencyInjection() {
        AndroidSupportInjection.inject(this)
    }

    override fun showToast(msg: String) {
        baseActivity?.showToast(msg)
    }

    override fun showSnackbar(title: String, msg: String, action: String?) {
        baseActivity?.showSnackbar(title, msg, action)
    }

    override fun showError(e: Exception?) {
        baseActivity?.showError(e)
    }

    override fun showOffline() {
        baseActivity?.showOffline()
    }

    override fun hideSnackbar() {
        baseActivity?.hideSnackbar()
    }

    override fun showSnackbarWithRetry() {
        baseActivity?.showSnackbarWithRetry()
    }

    abstract fun onRetry()

    open fun clearDisposal() {
        mViewModel?.compositeDisposable?.clear()
    }
    fun checkNetwork(action: () -> Unit) {
        if (isNetworkConnected) {
            action()
        } else {
            showOffline( )
        }
    }
    interface Callback {

        fun onFragmentAttached()

        fun onFragmentDetached(tag: String)
    }

}

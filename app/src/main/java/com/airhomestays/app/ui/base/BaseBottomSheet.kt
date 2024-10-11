package com.airhomestays.app.ui.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection

abstract class BaseBottomSheet<T : ViewDataBinding, V : BaseViewModel<*>> : BottomSheetDialogFragment(), BaseNavigator {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        performDependencyInjection()
        super.onCreate(savedInstanceState)
        mViewModel = viewModel
        setHasOptionsMenu(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        mRootView = viewDataBinding!!.root
        return mRootView
    }

    override fun onDetach() {
        baseActivity = null
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

    override fun openSessionExpire(logInStatus : String) {
        if (baseActivity != null) {
//            baseActivity!!.openSessionExpire("")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.setPeekHeight(resources.displayMetrics.heightPixels / 2)
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

    fun onDismiss(dialog: FragmentManager) {
        dismiss()
    }

    abstract fun onRetry()

    open fun clearDisposal() {
        mViewModel?.compositeDisposable?.clear()
    }

    interface Callback {

        fun onFragmentAttached()

        fun onFragmentDetached(tag: String)
    }

}

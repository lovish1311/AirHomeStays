package com.airhomestays.app.ui.listing.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.R
import com.airhomestays.app.databinding.ReportUserAccountBinding
import com.airhomestays.app.ui.base.BaseDialogFragment
import com.airhomestays.app.ui.listing.ListingDetailsViewModel
import com.airhomestays.app.ui.listing.ListingNavigator
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class ReportUserDialog : BaseDialogFragment(), ListingNavigator {

    private val TAG = ReportUserDialog::class.java.simpleName
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    val viewModel: ListingDetailsViewModel
        get() = ViewModelProvider(this, mViewModelFactory).get(ListingDetailsViewModel::class.java)
    companion object {
        @JvmStatic
        fun newInstance() = ReportUserDialog()
    }

    fun dismissDialog() {
        dismissDialog(TAG)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<ReportUserAccountBinding>(inflater, R.layout.report_user_account, container, false)
        val view = binding.root
        AndroidSupportInjection.inject(this)
        viewModel.navigator = this
        binding.btnCancel.onClick {
            binding.ltLoading.gone()
            dismissDialog()
        }
        binding.btnApply.onClick{
            viewModel.reportUser.set(true)
        }
        return view
    }

    fun show(fragmentManager: androidx.fragment.app.FragmentManager) {
        super.show(fragmentManager, TAG)
    }

    override fun openBillingActivity(isProfilePresent: Boolean) {

    }

    override fun openPriceBreakdown() {
        TODO("Not yet implemented")
    }

    override fun removeSubScreen() {

    }

    override fun show404Screen() {

    }

    override fun showReportScreen() {
        activity?.finish()
        dismissDialog()
    }


}
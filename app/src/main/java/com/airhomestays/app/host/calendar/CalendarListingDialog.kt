package com.airhomestays.app.host.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.Constants
import com.airhomestays.app.R
import com.airhomestays.app.databinding.DialogCalendarListingBinding
import com.airhomestays.app.ui.base.BaseDialogFragment
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderCalendarListing
import com.airhomestays.app.viewholderDividerPadding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class CalendarListingDialog : BaseDialogFragment(), CalendarAvailabilityNavigator {
    override fun closeAvailability(flag: Boolean) {
       // baseActivity?.onBackPressed()
    }

    override fun hideWholeView(flag: Boolean) {

    }

    override fun hideCalendar(flag: Boolean) {
        (parentFragment as CalendarListingFragment).hideCalendar(flag)
    }

    override fun moveBackToScreen() {

    }

    private val TAG = CalendarListingDialog::class.java.simpleName
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    val viewModel: CalendarListingViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(CalendarListingViewModel::class.java)
    private lateinit var mBinding: DialogCalendarListingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_calendar_listing, container, false)
        val view = mBinding.root
        AndroidSupportInjection.inject(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.ivClose.onClick {
            dismiss()
        }
        subscribeToLiveData()
    }

    private fun setUp(it: List<CalendarListingViewModel.list>) {
        mBinding.rvLanguage.withModels {
            it.forEachIndexed { _, result ->
                viewholderCalendarListing {
                    id(result.id)
                    title(result.title)
                    room(result.room)
                    img(Constants.imgListingSmall + result.img)
                    if (result.id == viewModel.selectedListing.value!!.id) {
                        isSelected(true)
                    } else {
                        isSelected(false)
                    }
                    onClick { _ ->
                        viewModel.selectedList(
                                CalendarListingViewModel.list(
                                        result.id,
                                        result.title,
                                        result.room,
                                        result.img
                                )
                        )
                        dismiss()
                    }
                }
                viewholderDividerPadding {
                    id("divider - ${result.id}")
                }
            }
        }
    }

    private fun subscribeToLiveData() {
        viewModel.manageListing1.observe(this, androidx.lifecycle.Observer {
            setUp(it)
        })
    }

    fun show(fragmentManager: androidx.fragment.app.FragmentManager) {
        super.show(fragmentManager, TAG)
    }
}
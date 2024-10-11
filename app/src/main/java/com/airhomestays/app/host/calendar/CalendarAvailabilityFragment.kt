package com.airhomestays.app.host.calendar

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.BR
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentCalendarAvailabilityBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.hostHome.HostHomeActivity
import com.airhomestays.app.util.Utils.Companion.getMonth1
import com.airhomestays.app.util.binding.BindingAdapters
import com.airhomestays.app.util.onClick
import com.airhomestays.app.viewholderCalendarAvailableHeader
import com.airhomestays.app.viewholderCalendarDateHeader
import com.airhomestays.app.viewholderDivider
import com.airhomestays.app.viewholderReportUserRadio
import com.airhomestays.app.viewholderSpecialpriceEt
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject

class CalendarAvailabilityFragment :
    BaseFragment<FragmentCalendarAvailabilityBinding, CalendarListingViewModel>(),
    CalendarAvailabilityNavigator {


    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentCalendarAvailabilityBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_calendar_availability
    override val viewModel: CalendarListingViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(CalendarListingViewModel::class.java)
    private var selectArray = arrayOf(true, false)
    private var isSelected = false

    var currency = "$"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.incClose.ivNavigateup.onClick {
            hideKeyboard()
            if (viewModel.navigateBack.get() == true) {
                openHostCalendar()
            } else {
                baseActivity!!.onBackPressed()
            }
        }
        mBinding.tvNext.onClick {
            if (selectArray[0]) {
                checkPrice()
            } else if (selectArray[1]) {
                viewModel.calendarStatus.set("blocked")
                viewModel.updateBlockedDates()
            }
        }

        mBinding.rlCalendarEdit.withModels {
            viewholderCalendarAvailableHeader {
                id(54)
                header(baseActivity!!.resources.getString(R.string.availability))
            }
            viewholderCalendarDateHeader {
                id(324)
                header(
                    setDateInCalendar(
                        viewModel.startDate.value.toString(),
                        viewModel.endDate.value.toString()
                    )
                )

            }
            viewholderReportUserRadio {
                id("2")
                text(baseActivity!!.resources.getString(R.string.make_available))
                radioVisibility(selectArray[0])
                onClick(View.OnClickListener { selector(0) })
            }
            viewholderDivider {
                id("Divider - 2")
            }
            viewholderReportUserRadio {
                id("3")
                if (viewModel.endDate.value != null) {
                    text(baseActivity!!.resources.getString(R.string.block_selected_dates))
                } else {
                    text(baseActivity!!.resources.getString(R.string.block_selected_date))
                }


                radioVisibility(selectArray[1])
                onClick(View.OnClickListener { hideKeyboard(); selector(1) })
            }
            if (selectArray[0]) {
                viewholderSpecialpriceEt {
                    id("23")
                    title(baseActivity!!.resources.getString(R.string.add_special_price))
                    currency(BindingAdapters.getCurrencySymbol(currency))
                    hint(getString(R.string.price_per_night))
                    text(viewModel.specialPrice)
                    onBind { model, view, position ->
                        val editText =
                            view.dataBinding.root.findViewById<EditText>(R.id.et_host_edit)
                        editText.filters = arrayOf(InputFilter.LengthFilter(13))

                        setEditTextMaxLength(editText, 16)
                        editText.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable) {}

                            override fun beforeTextChanged(
                                s: CharSequence, start: Int,
                                count: Int, after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence, start: Int,
                                before: Int, count: Int
                            ) {
                                isInputValid(s, editText)
                            }
                        })

                    }

                    onUnbind { model, view ->
                        val editText =
                            view.dataBinding.root.findViewById<EditText>(R.id.et_host_edit)
                        editText.setText("")
                    }
                }
            }
        }
    }

    fun checkPrice() {
        if (isvalidspecialprice(viewModel.specialPrice.get().toString())) {
            val f = viewModel.specialPrice.get()?.toDouble()
            if (f != null) {
                val i = f.toInt()
                if (i != 0) {
                    viewModel.calendarStatus.set("available")
                    viewModel.updateBlockedDates()
                } else {
                    showToast(getString(R.string.spl_price_valid))
                }
            } else {
                viewModel.calendarStatus.set("available")
                viewModel.updateBlockedDates()
            }
        } else if (viewModel.specialPrice.get()!!.isEmpty()) {
            viewModel.calendarStatus.set("available")
            viewModel.updateBlockedDates()
        } else {
            showToast(getString(R.string.spl_price_valid))
        }

    }

    fun isvalidspecialprice(target: CharSequence):Boolean{
        return !TextUtils.isEmpty(target)&& Pattern.compile("^\\d+(\\.\\d)?\\d*\$").matcher(target).matches()
    }

    private fun openHostCalendar() {
        var intent: Intent? = null

        intent = Intent(baseActivity, HostHomeActivity::class.java)
        intent?.putExtra("from", "calendar")
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        baseActivity!!.startActivity(intent)
    }


    private fun selector(index: Int) {
        selectArray.forEachIndexed { i: Int, _: Boolean ->
            selectArray[i] = index == i
            isSelected = true
        }
        mBinding.rlCalendarEdit.requestModelBuild()
    }

    fun setDateInCalendar(selStartDate: String, selEndDate: String?): String {
        return try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val langType = preferences.getString("Locale.Helper.Selected.Language", "en")
            if (selEndDate == "null") {
                val startMonthName = getMonth1(selStartDate)
                "$startMonthName"
            } else {
                val startMonthName = getMonth1(selStartDate)
                val endMonthName = getMonth1(selEndDate!!)
                "$startMonthName  - $endMonthName"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    override fun onDestroyView() {
        viewModel.specialPrice.set("")
        super.onDestroyView()
    }

    override fun moveBackToScreen() {
        baseActivity?.onBackPressed()
    }

    private fun subscribeToLiveData() {
        viewModel.manageListing1.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            currency = it.get(0).currency!!
            mBinding.rlCalendarEdit.requestModelBuild()
        })
    }

    fun onRefresh() {
    }

    override fun onRetry() {

    }

    override fun closeAvailability(flag: Boolean) {
    }

    override fun hideCalendar(flag: Boolean) {

    }

    override fun hideWholeView(flag: Boolean) {
    }

    fun isInputValid(s: CharSequence, editText: EditText) {
        if (!s.contains('.')){
            if (s.length > 13){
                editText.text?.delete(13, editText.text.length)
            }
        }
        s.indexOfFirst { it == '.' }.takeIf { it != -1 }?.let { dotIndex ->
            if (s.length - dotIndex > 3) {
                editText.text?.delete(dotIndex+3, editText.text.length)
            }
        }
    }


    fun setEditTextMaxLength(editText: EditText, length: Int) {
        editText.filters = arrayOf(InputFilter.LengthFilter(length ?: 13))
    }
}
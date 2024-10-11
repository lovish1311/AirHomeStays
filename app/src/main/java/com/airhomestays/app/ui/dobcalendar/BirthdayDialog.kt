package com.airhomestays.app.ui.dobcalendar

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.TypedValue
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.airhomestays.app.R
import com.airhomestays.app.ui.base.BaseDialogFragment
import com.airhomestays.app.util.Utils
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

class BirthdayDialog : BaseDialogFragment() {

    private val TAG = BirthdayDialog::class.java.simpleName
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    var onDateSet: DatePickerDialog.OnDateSetListener? = null
    lateinit var datePickerDialog: DatePickerDialog

    companion object {
        fun newInstance(selYear: Int, selMonth: Int, selDay: Int) =
            BirthdayDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, selYear)
                    putInt(ARG_PARAM2, selMonth)
                    putInt(ARG_PARAM3, selDay)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            year = it.getInt(ARG_PARAM1)
            month = it.getInt(ARG_PARAM2)
            day = it.getInt(ARG_PARAM3)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        datePickerDialog = if ((year != 0 && day != 0) || month != 0) {
            DatePickerDialog(
                this.requireContext(),
                R.style.CustomDatePickerDialogTheme, onDateSet, year, month, day
            )
        } else {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(
                this.requireContext(),
                R.style.CustomDatePickerDialogTheme, onDateSet, year, month, day
            )
        }
        val tv = TextView(context)
        val lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(50, 50, 50, 50)
        tv.layoutParams = lp
        tv.setPadding(10, 10, 10, 10)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        val typeface = ResourcesCompat.getFont(this.requireContext(), R.font.be_vietnampro_semibold)
        tv.typeface = typeface
        tv.text = resources.getString(R.string.set_your_birthday)
        datePickerDialog.setCustomTitle(tv)
        datePickerDialog.setButton(
            DatePickerDialog.BUTTON_POSITIVE,
            resources.getString(R.string.okay),
            datePickerDialog
        )


        var ss = Utils.get18YearLimit().get(0).toString() + "/" + Utils.get18YearLimit().get(1)
            .toString() + "/" + Utils.get18YearLimit().get(2).toString()

        val date1: Date = SimpleDateFormat("yyyy/MM/dd").parse(ss)
        var time = Timestamp(date1.time)
        datePickerDialog.datePicker.maxDate = time.time
        return datePickerDialog

    }

    fun dismissDialog() {
        dismissDialog(TAG)
    }

    fun setCallBack(ondate: DatePickerDialog.OnDateSetListener) {
        onDateSet = ondate
    }

    fun show(fragmentManager: androidx.fragment.app.FragmentManager) {
        super.show(fragmentManager, TAG)
    }
}
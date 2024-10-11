package com.airhomestays.app.host.calendar

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.airhomestays.app.BR
import com.airhomestays.app.Constants
import com.airhomestays.app.ListBlockedDatesQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentCalendarListingBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.util.daysOfWeekFromLocale
import com.airhomestays.app.util.gone
import com.airhomestays.app.util.onClick
import com.airhomestays.app.util.visible
import com.squareup.timessquare.CalendarPickerView
import com.squareup.timessquare.DefaultDayViewAdapter
import org.threeten.bp.YearMonth
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class CalendarListingFragment : BaseFragment<FragmentCalendarListingBinding, CalendarListingViewModel>(), CalendarAvailabilityNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentCalendarListingBinding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_calendar_listing
    override val viewModel: CalendarListingViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(CalendarListingViewModel::class.java)
    var targetFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    var originalFormat = SimpleDateFormat("EEE LLL dd HH:mm:ss Z yyyy", Locale.ENGLISH)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        subscribeToLiveData()
    }

    private fun initView() {
        mBinding.tvEdit.onClick {
            if (viewModel.selectedListing.value != null) {
                val selectedList = ArrayList<String>()
                mBinding.calendarPickerView.selectedDates.forEach {
                    val originalDate = originalFormat.parse(it.toString())
                    val targetDate = targetFormatter.format(originalDate)
                    selectedList.add(targetDate)
                }
            }
        }
        mBinding.rlListingDetails.onClick {
            if (viewModel.selectedListing.value != null) {
                CalendarListingDialog().show(childFragmentManager)
            }
        }

        val daysOfWeek = daysOfWeekFromLocale()

        val currentMonth = YearMonth.now()
        mBinding.calendarView.setup(currentMonth, currentMonth.plusMonths(12), daysOfWeek.first())
        mBinding.calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
          //  val textView = view.calendarDayText
        }

        mBinding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                //container.textView.text = day.date.dayOfMonth.toString()
            }
        }

    }

    fun openFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        childFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(mBinding.flRoot.id, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    private fun subscribeToLiveData() {
        viewModel.selectedListing.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                mBinding.tvListingName.text = it.title
                mBinding.tvListingType.text = it.room
                mBinding.img = Constants.imgListingSmall + it.img

            }
        })

        viewModel.manageListing1.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()) {
                mBinding.rlRoot.visible()
                mBinding.llNoResult.gone()
            } else {
                mBinding.llNoResult.visible()
                mBinding.rlRoot.gone()
            }
        })


    }

    private fun initCalendar(it: List<ListBlockedDatesQuery.BlockedDate>) {
        mBinding.calendarPickerView.invalidateViews()
        mBinding.calendarPickerView.invalidate()
        val selectedDays = ArrayList<Date>()
        val bookedDays = ArrayList<Date>()

        val nextYear = Calendar.getInstance()
        nextYear.add(Calendar.YEAR, 1)
        val today = Date()

        it.forEachIndexed { _, result ->
            if (result.reservationId == null) {
                val epoch = java.lang.Long.parseLong(result.blockedDates!!)
                val time = getStartOfDayEpochSecond()
                if (epoch >= time) {
                    selectedDays.add(Date(epoch))
                }
            } else {
                val epoch = java.lang.Long.parseLong(result.blockedDates!!)
                val ttime = getStartOfDayEpochSecond()
                if (epoch >= ttime) {
                    bookedDays.add(Date(epoch))
                }
            }
        }
        mBinding.calendarPickerView.setCustomDayView(DefaultDayViewAdapter())
        mBinding.calendarPickerView.init(today, nextYear.time)
                .inMode(CalendarPickerView.SelectionMode.RANGE)
                .withBookedDates(bookedDays)
                .withHighlightedDates(selectedDays)
               // .withSelectedDates(selectedDays)
        hideCalendar(false)
    }

    fun getStartOfDayEpochSecond(): Long {
        val secondInaDay = (60 * 60 * 24).toLong()
        val currentMilliSecond = System.currentTimeMillis() / 1000
        return currentMilliSecond - currentMilliSecond % secondInaDay
    }

    fun onRefresh() {
        viewModel.getManageListings()
    }

    override fun onRetry() {
        if (viewModel.isCalendarLoading.get()) {
            viewModel.getListBlockedDates()
        } else {
            viewModel.getManageListings()
        }
    }

    override fun hideWholeView(flag: Boolean) {
        mBinding.rlRoot.gone()
        mBinding.llNoResult.gone()
    }

    override fun hideCalendar(flag: Boolean) {

    }

    override fun moveBackToScreen() {

    }

    override fun closeAvailability(flag: Boolean) {
        baseActivity?.onBackPressed()
    }

}
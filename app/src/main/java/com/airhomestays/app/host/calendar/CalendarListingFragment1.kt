package com.airhomestays.app.host.calendar

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.airhomestays.app.BR
import com.airhomestays.app.Constants
import com.airhomestays.app.GetListingSpecialPriceQuery
import com.airhomestays.app.R
import com.airhomestays.app.databinding.FragmentCalendarListing1Binding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.host.hostHome.HostHomeActivity
import com.airhomestays.app.ui.saved.currencyConverter
import com.airhomestays.app.util.*
import com.airhomestays.app.vo.SpecialDate
import org.threeten.bp.*
import timber.log.Timber
import javax.inject.Inject



class CalendarListingFragment1 : BaseFragment<FragmentCalendarListing1Binding, CalendarListingViewModel>(), CalendarAvailabilityNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentCalendarListing1Binding
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_calendar_listing_1
    override val viewModel: CalendarListingViewModel
        get() = ViewModelProvider(baseActivity!!, mViewModelFactory).get(CalendarListingViewModel::class.java)
    private val today = LocalDate.now()
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private var radiusUpdated = false

    val selectedDays1 = ArrayList<LocalDate?>()
    val bookedDays1 = ArrayList<LocalDate>()
    val specialPricingDates1 = ArrayList<SpecialDate>()
    var listingCurrency = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding!!
        viewModel.navigator = this
        initView()
        initCalendar()
        subscribeToLiveData()
        viewModel.getManageListings()
    }

    private fun initCalendar() {
        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        mBinding.calendarView.setup(currentMonth, currentMonth.plusMonths(12), daysOfWeek.first())
        mBinding.calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {

            lateinit var day: CalendarDay
            val textView = view.findViewById<TextView>(R.id.exFourDayText)
            val roundBgView = view.findViewById<View>(R.id.exFourRoundBgView)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH && (day.date == today || day.date.isAfter(today))) {
                        val date = day.date
                        if (startDate != null) {
                            if (date < startDate || endDate != null) {
                                startDate = date
                                mBinding.tvEdit.visible()
                                endDate = null
                            } else if (date != startDate && getBlockedDates(startDate!!, date)) {
                                endDate = date
                            }
                        } else {
                            startDate = date
                            mBinding.tvEdit.visible()
                        }
                        mBinding.calendarView.notifyCalendarChanged()
                    }
                }

                view.setOnLongClickListener(View.OnLongClickListener {
                    try {
                        it.tag?.let {
                            val tag = (view.tag as String).substringBefore("-")
                            if (tag == "specialPrice") {
                                val price = (view.tag as String).substringAfterLast("-")
                                showPriceAlert(price)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return@OnLongClickListener true
                })
            }
        }

        mBinding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val view = container.view
                val textView = container.textView
                val roundBgView = container.roundBgView

                textView.text = null
                textView.background = null
                roundBgView.makeInVisible()
                view.isClickable = true
                view.enable()
                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.text = day.day.toString()
                    if (day.date.isBefore(today)) {
                        textView.setTextColorRes(R.color.example_4_grey_past)
                    } else if (bookedDays1.contains(day.date)) {
                        textView.setTextColorRes(R.color.example_4_grey)
                        roundBgView.makeVisible()
                        roundBgView.setBackgroundResource(R.drawable.blocked_date)
                        view.tag = null
                        view.isClickable = false
                        view.disable()
                    } else {
                        when {
                            startDate == day.date && endDate == null -> {
                                textView.setTextColorRes(R.color.white_photo_story)
                                roundBgView.makeVisible()
                                roundBgView.setBackgroundResource(R.drawable.example_4_single_selected_bg)
                            }
                            day.date == startDate -> {
                                textView.setTextColorRes(R.color.white_photo_story)
                                updateDrawableRadius(textView)
                                textView.background = startBackground
                            }
                            startDate != null && endDate != null && (day.date > startDate && day.date < endDate) -> {
                                textView.setTextColorRes(R.color.white_photo_story)
                                textView.setBackgroundResource(R.drawable.example_4_continuous_selected_bg_middle)
                            }
                            day.date == endDate -> {
                                textView.setTextColorRes(R.color.white_photo_story)
                                updateDrawableRadius(textView)
                                textView.background = endBackground
                            }
                            selectedDays1.contains(day.date) -> {
                                textView.setTextColorRes(R.color.white_photo_story)
                                roundBgView.makeVisible()
                                roundBgView.setBackgroundResource(R.drawable.selected_date)
                                view.tag = null
                            }

                            getSpecialPrice(day.date, textView, roundBgView, view) -> { }
                            else -> textView.setTextColorRes(R.color.example_4_grey)
                        }
                    }
                }
            }
        }


        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = view.rootView.findViewById<TextView>(R.id.exFourHeaderText)
            val containerDays = view.rootView.findViewById<LinearLayout>(R.id.container_days)
        }

        mBinding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                val monthTitle = "${baseActivity!!.resources.getStringArray(R.array.label_calender_month)[month.yearMonth.month.value].toLowerCase().capitalize()} ${month.year}"
                container.textView.text = monthTitle
            }
        }
    }

    private fun showPriceAlert(price: String) {
        val dialog = AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.specialPrice)+ " "+currencyConverter(viewModel.getCurrencyBase(), viewModel.getCurrencyRates(), viewModel.getUserCurrency(), listingCurrency, price.toDouble()))
                .setCancelable(true)
                .setOnCancelListener { dialog -> dialog.dismiss() }
                .show()

        val layoutParams = WindowManager.LayoutParams()

        // Copy the alert dialog window attributes to new layout parameter instance
        layoutParams.copyFrom(dialog.window?.attributes)

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        // Apply the newly created layout parameters to the alert dialog window
        dialog.window?.attributes = layoutParams

        val messageView = dialog.findViewById<TextView>(android.R.id.message) as TextView
        messageView.gravity = Gravity.CENTER
        messageView.typeface = Typeface.DEFAULT_BOLD
    }

    private fun initView() {
        mBinding.tvEdit.onClick {
            viewModel.startDate.value = startDate
            viewModel.endDate.value = endDate
            (baseActivity as HostHomeActivity).hideBottomNavigation()
            openFragment(CalendarAvailabilityFragment(), "availability")
        }
        mBinding.rlListingDetails.onClick {
            if (viewModel.selectedListing.value != null) {
                CalendarListingDialog().show(childFragmentManager)
            }
        }
    }

    private fun getSpecialPrice(date: LocalDate, textView: TextView, roundBgView: View, view: View?): Boolean {
        view?.tag = ""
        for (i in 0 until specialPricingDates1.size) {
            if (specialPricingDates1[i].date == date) {
                if(specialPricingDates1[i].isSpecialPrice!=0.0) {
                    view?.tag = "specialPrice-${specialPricingDates1[i].isSpecialPrice}"
                    textView.setTextColorRes(R.color.white_photo_story)
                    roundBgView.makeVisible()
                    roundBgView.setBackgroundResource(R.drawable.specialpricing_date)
                    return true
                }
            } else {
                view?.tag = null
            }
        }
        return false
    }

    private fun getBlockedDates(startDate1: LocalDate, date: LocalDate): Boolean {
        for (i in 0 until bookedDays1.size) {
            if (bookedDays1[i].toEpochDay() in startDate1.toEpochDay()..date.toEpochDay()) {
                startDate = date
                mBinding.tvEdit.visible()
                return false
            }
        }
        return true
    }

    private val startBackground: GradientDrawable by lazy {
        Timber.d("language-->>>1    ${Utils.getCurrentLocale(this.requireContext())!!.language}")
        if(context?.resources?.getBoolean(R.bool.is_left_to_right_layout)!!){
            return@lazy requireContext().getDrawableCompat(R.drawable.example_4_continuous_selected_bg_start)!! as GradientDrawable
        }else{
            return@lazy requireContext().getDrawableCompat(R.drawable.example_4_continuous_selected_bg_end)!! as GradientDrawable
        }

    }

    private val endBackground: GradientDrawable by lazy {
        Timber.d("language-->>>2   ${Utils.getCurrentLocale(this.requireContext())!!.language}")
        if(context?.resources?.getBoolean(R.bool.is_left_to_right_layout)!!){
            return@lazy requireContext().getDrawableCompat(R.drawable.example_4_continuous_selected_bg_end)!! as GradientDrawable
        }else{
            return@lazy requireContext().getDrawableCompat(R.drawable.example_4_continuous_selected_bg_start)!! as GradientDrawable
        }
    }

    private fun updateDrawableRadius(textView: TextView) {
        if (radiusUpdated) return
        radiusUpdated = true

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
                mBinding.img = it.img
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

        viewModel.blockedDates1.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            initCalendar1(it)

        })
    }

    private fun initCalendar1(it: List<GetListingSpecialPriceQuery.Result?>?) {
        specialPricingDates1.clear()
        bookedDays1.clear()
        selectedDays1.clear()
        try{
            it?.forEachIndexed { _, result ->
                listingCurrency = result!!.listCurrency.toString()
                if (result.calendarStatus == "available") {
                    val epoch = java.lang.Long.parseLong(result.blockedDates!!)
                    val time = getStartOfDayEpochSecond()
                    if (epoch >= time) {
                        val aa = Instant.ofEpochMilli(epoch).atZone(ZoneOffset.UTC).toLocalDate()
                        if(result.isSpecialPrice!=null){
                            specialPricingDates1.add(SpecialDate(aa, isSpecialPrice = result.isSpecialPrice!!))
                        }
                    }
                } else if(result.calendarStatus == "blocked") {
                    if (result.reservationId != null) {
                        val epoch = java.lang.Long.parseLong(result.blockedDates!!)
                        val ttime = getStartOfDayEpochSecond()
                        if (epoch >= ttime) {
                            val aa = Instant.ofEpochMilli(epoch).atZone(ZoneOffset.UTC).toLocalDate()
                            bookedDays1.add(aa)
                        }
                    } else {
                        val epoch = java.lang.Long.parseLong(result.blockedDates!!)
                        val time = getStartOfDayEpochSecond()
                        if (epoch >= time) {
                            val aa = Instant.ofEpochMilli(epoch).atZone(ZoneOffset.UTC).toLocalDate()
                            selectedDays1.add(aa)
                        }
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        hideCalendar(false)
        startDate = null
        endDate = null
        mBinding.tvEdit.gone()
        mBinding.calendarView.notifyCalendarChanged()
        val currentMonth = YearMonth.now()
        mBinding.calendarView.scrollToMonth(currentMonth)
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
        if (flag) {
            mBinding.flLottieView.visible()
            mBinding.calendarView.gone()
            mBinding.llInfo.gone()
            mBinding.tvEdit.gone()
        } else {
            mBinding.flLottieView.gone()
            mBinding.calendarView.visible()
            mBinding.llInfo.visible()
        }
    }

    override fun moveBackToScreen() {

    }


    override fun closeAvailability(flag: Boolean) {
        if (flag) {
            viewModel.getListBlockedDates()
        }else{
            initCalendar1( viewModel.blockedDates1.value)
        }
        baseActivity?.onBackPressed()
    }

}
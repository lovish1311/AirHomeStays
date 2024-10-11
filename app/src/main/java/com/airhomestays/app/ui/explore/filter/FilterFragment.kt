package com.airhomestays.app.ui.explore.filter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.airhomestays.app.*
import com.airhomestays.app.databinding.FragmentListingAmenitiesBinding
import com.airhomestays.app.ui.base.BaseFragment
import com.airhomestays.app.ui.explore.ExploreFragment
import com.airhomestays.app.ui.explore.ExploreViewModel
import com.airhomestays.app.ui.home.HomeActivity
import com.airhomestays.app.util.*
import com.airhomestays.app.util.binding.BindingAdapters
import com.yongbeom.aircalendar.AirCalendarDatePickerActivity
import com.yongbeom.aircalendar.core.AirCalendarIntent
import javax.inject.Inject

class FilterFragment : BaseFragment<FragmentListingAmenitiesBinding, ExploreViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.fragment_listing_amenities
    override val viewModel: ExploreViewModel
        get() = ViewModelProvider(
            baseActivity!!,
            mViewModelFactory
        ).get(ExploreViewModel::class.java)
    lateinit var mBinding: FragmentListingAmenitiesBinding

    var startMonth: String = ""
    var endMonth: String = ""
    var tvDates = ""
    lateinit var openCalendarActivityResultLauncher: ActivityResultLauncher<Intent>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mBinding = viewDataBinding!!


        tvDates = getString(R.string.select_date)
        if (viewModel.currentFragment.isEmpty() && (viewModel.filterCount.value == null || viewModel.filterCount.value == 0)) {
            updateLocalSelectedFilters()
        }

        initView()
        subscribeToLiveData()
        onActivityResults()
    }


    private fun initView() {
        if (viewModel.currentFragment.isNotEmpty() && (viewModel.filterCount.value == null || viewModel.filterCount.value == 0)) {


            viewModel.personCapacity1.set(viewModel.personCount.toString())
            viewModel.personCapacity.value = viewModel.personCount.toString()


            viewModel.minRangeSelected.value = viewModel.minRange.value
            viewModel.maxRangeSelected.value = viewModel.maxRange.value


            viewModel.TemproomType = viewModel.roomType!!.clone() as HashSet<Int>




        } else if (viewModel.currentFragment.isNotEmpty() && viewModel.filterCount.value != null && viewModel.filterCount.value != 0) {
            viewModel.personCapacity1.set(viewModel.personCapacity.value)
            viewModel.TemproomType = viewModel.roomType!!.clone() as HashSet<Int>

        }
        (baseActivity as HomeActivity).hideBottomNavigation()
        mBinding.ivClose.visible()
        mBinding.rlShowresult.disable()
        mBinding.ivClose.onClick {
            baseActivity?.onBackPressed()
        }
        mBinding.tvRightsideText.onClick {
            clearSelectedFilters()
            mBinding.rlListingAmenities.requestModelBuild()
        }


        if (viewModel.dateStart.isNotEmpty()) {
            startMonth = Utils.getMonth1(viewModel.dateStart)
            endMonth = Utils.getMonth1(viewModel.dateEnd)
            tvDates = "$startMonth - $endMonth"
            if (mBinding.rlListingAmenities.adapter!=null) {
                mBinding.rlListingAmenities.requestModelBuild()
            }

        }
        if(viewModel.startDate.value !="0" && viewModel.startDate.value!!.isNotEmpty()){
            startMonth = Utils.getMonth1(viewModel.startDate.value!!)
            endMonth = Utils.getMonth1(viewModel.endDate.value!!)
            tvDates = "$startMonth - $endMonth"
            if (mBinding.rlListingAmenities.adapter!=null) {
                mBinding.rlListingAmenities.requestModelBuild()
            }

        }


        if (!viewModel.startDate.value.isNullOrEmpty() && viewModel.startDate.value.toString() != "0"
            && !viewModel.endDate.value.isNullOrEmpty() && viewModel.endDate.value.toString() != "0"
        ) {
            viewModel.TempstartDateFromResult = viewModel.startDate.value!!
            viewModel.TempendDateFromResult = viewModel.endDate.value!!
        } else {
            viewModel.startDate.value = "0"
            viewModel.endDate.value = "0"
        }
        mBinding.btnGuestSeeresult.onClick {
            try {


                viewModel.startDate.value = viewModel.TempstartDateFromResult
                viewModel.endDate.value = viewModel.TempendDateFromResult
                viewModel.dateStart = viewModel.startDate.value!!
                viewModel.dateEnd = viewModel.endDate.value!!
                viewModel.startMonthName.value = startMonth
                viewModel.endMonthName.value = endMonth


                setViewModelValue()
                var count =
                    (((baseActivity as HomeActivity).pageAdapter.getCurrentFragment()) as ExploreFragment).childFragmentManager.backStackEntryCount
                while (count >= 0) {
                    (((baseActivity as HomeActivity).pageAdapter.getCurrentFragment()) as ExploreFragment).childFragmentManager.popBackStack()
                    count--
                }
                (baseActivity as HomeActivity).showBottomNavigation()
                if (viewModel.personCapacity1.get() != null && viewModel.personCapacity1.get()!!
                        .toInt() > 0
                ) {
                    viewModel.personCapacity.value = viewModel.personCapacity1.get()
                }

                if (viewModel.filterCount.value!! >= 1) {
                    viewModel.startSearching()
                } else if (!viewModel.roomType.isNullOrEmpty()) {
                    viewModel.startSearching()
                } else {
                    if (!viewModel.location.value.isNullOrEmpty()) {
                        (((baseActivity as HomeActivity).pageAdapter.getCurrentFragment()) as ExploreFragment).childFragmentManager.popBackStack()
                        viewModel.startSearching()
                    } else {
                        (((baseActivity as HomeActivity).pageAdapter.getCurrentFragment()) as ExploreFragment).reset()
                    }
                }




            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (viewModel.currentFragment.isNotEmpty())
            getViewModelValue()

        
        mBinding.personCapacity1 = viewModel.personCapacity1


        mBinding.rlListingAmenities.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
    private fun onActivityResults() {
        openCalendarActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK ) {
                if (result.data != null) {
                    viewModel.TempstartDateFromResult =
                        result.data?.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_START_DATE)
                            .orEmpty()
                    viewModel.TempendDateFromResult =
                        result.data?.getStringExtra(AirCalendarDatePickerActivity.RESULT_SELECT_END_DATE)
                            .orEmpty()
                    if (viewModel.TempstartDateFromResult.isNotEmpty() && viewModel.TempendDateFromResult.isNotEmpty()) {
                        setDateInCalendar()


                    } else {
                        resetDate()
                    }


                }


            }

        }


    }




    private fun openCalender() {
        val isSelect: Boolean = true
        val intent = AirCalendarIntent(activity)
        intent.isBooking(isSelect)
        intent.isSelect(isSelect)

        if(viewModel.TempstartDateFromResult.isNotEmpty()){
            intent.setStartDate(viewModel.TempstartDateFromResult)
            intent.setEndDate(viewModel.TempendDateFromResult)
        }

        if (viewModel.dateStart.isNotEmpty()) {
            intent.setStartDate(viewModel.dateStart)
            intent.setEndDate(viewModel.dateEnd)
        }


        intent.isMonthLabels(false)
        intent.setType(false)
        openCalendarActivityResultLauncher.launch(intent, Utils.TransitionAnim(requireContext(),"slideup"))
    }


    @SuppressLint("SetTextI18n")
    private fun setDateInCalendar() {
        try {
            startMonth = Utils.getMonth1(viewModel.TempstartDateFromResult)
            endMonth = Utils.getMonth1(viewModel.TempendDateFromResult)
             viewModel.dateStart=viewModel.TempstartDateFromResult
            viewModel.dateEnd=viewModel.TempendDateFromResult
            tvDates = "$startMonth - $endMonth"
            if (mBinding.rlListingAmenities.adapter!=null) {
                mBinding.rlListingAmenities.requestModelBuild()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }


    private fun clearSelectedFilters() {
        try {
            viewModel.TempstartDateFromResult = ""
            viewModel.TempendDateFromResult = ""
            tvDates = getString(R.string.select_date)
            viewModel.personCapacity1.set(viewModel.personCount.toString())
            viewModel.Tempamenities = HashSet()
            viewModel.Tempspaces = HashSet()
            viewModel.TemphouseRule = HashSet()
            viewModel.TempbookingType = String()
            viewModel.Tempcount = 0
            viewModel.TempminRange = viewModel.minRange.value?.toInt()!!
            viewModel.TempmaxRange = viewModel.maxRange.value?.toInt()!!
            viewModel.bed1.set("0")
            viewModel.bedrooms1.set("0")
            viewModel.bathrooms1.set("0")
            viewModel.dateStart=""
            viewModel.dateEnd=""
            if (mBinding.rlListingAmenities.adapter!=null) {
                mBinding.rlListingAmenities.requestModelBuild()
            }

            AirCalendarDatePickerActivity.activity.clearSelectedDates()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    private fun getViewModelValue() {
        try {
            viewModel.TempbookingType = viewModel.bookingType.value!!
            viewModel.Tempamenities = viewModel.amenities.clone() as HashSet<Int>
            viewModel.Tempspaces = viewModel.spaces.clone() as HashSet<Int>
            viewModel.TemphouseRule = viewModel.houseRule.clone() as HashSet<Int>
            viewModel.bed1.set(viewModel.bed.value)
            viewModel.bedrooms1.set(viewModel.bedrooms.value)
            viewModel.bathrooms1.set(viewModel.bathrooms.value)
            viewModel.TempminRange = viewModel.minRangeSelected.value?.toInt()!!
            viewModel.TempmaxRange = viewModel.maxRangeSelected.value?.toInt()!!
            viewModel.TempcurrencySymbol =
                BindingAdapters.getCurrencySymbol(viewModel.getUserCurrency())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun setViewModelValue() {
        try {
            viewModel.Tempcount = 0
            if (viewModel.TempbookingType == "instant") {
                viewModel.Tempcount++
            } else {
                if (viewModel.Tempcount != 0) {
                    viewModel.Tempcount--
                }
            }


            viewModel.Tempcount += viewModel.Tempamenities.size
            viewModel.Tempcount += viewModel.Tempspaces.size
            viewModel.Tempcount += viewModel.TemphouseRule.size


            if (viewModel.bed1.get()!!.toInt() > 0) {
                viewModel.Tempcount++
            }

            if (viewModel.startDate.value != "0" && viewModel.endDate.value != "0" && !viewModel.startDate.value.isNullOrEmpty() && !viewModel.endDate.value.isNullOrEmpty()) {
                viewModel.Tempcount++
            }
            if (viewModel.bedrooms1.get()!!.toInt() > 0) {
                viewModel.Tempcount++
            }
            if (viewModel.bathrooms1.get()!!.toInt() > 0) {
                viewModel.Tempcount++
            }
            if (viewModel.personCapacity1.get()!!.toInt() > viewModel.personCount) {
                viewModel.Tempcount++
            }
            if (viewModel.TempminRange != viewModel.minRange.value || viewModel.TempmaxRange != viewModel.maxRange.value) {
                viewModel.Tempcount++
            }
            updateLocalSelectedFilters()
            viewModel.filterCount.value = viewModel.Tempcount
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun updateLocalSelectedFilters() {


        viewModel.startDate.value = viewModel.TempstartDateFromResult
        viewModel.endDate.value = viewModel.TempendDateFromResult
        viewModel.bookingType.value = viewModel.TempbookingType
        viewModel.amenities = viewModel.Tempamenities.clone() as HashSet<Int>
        viewModel.spaces = viewModel.Tempspaces.clone() as HashSet<Int>
        viewModel.houseRule = viewModel.TemphouseRule.clone() as HashSet<Int>
        viewModel.bed.value = (viewModel.bed1.get())
        viewModel.bedrooms.value = (viewModel.bedrooms1.get())
        viewModel.bathrooms.value = (viewModel.bathrooms1.get())
        viewModel.minRangeSelected.value = viewModel.TempminRange
        viewModel.maxRangeSelected.value = viewModel.TempmaxRange
        viewModel.roomType = viewModel.TemproomType!!.clone() as HashSet<Int>


    }


    private fun subscribeToLiveData() {


        viewModel.exploreLists1.observe(viewLifecycleOwner, Observer {
            it?.let { list ->
                try {
                    initEpoxy(list.getListingSettingsCommon?.results!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }


    private fun resetDate() {

        viewModel.TempstartDateFromResult="0"
        viewModel.TempendDateFromResult="0"
        tvDates = resources.getString(R.string.select_date)
        if (mBinding.rlListingAmenities.adapter!=null) {
            mBinding.rlListingAmenities.requestModelBuild()
        }
    }


    private fun initEpoxy(it: List<GetExploreListingsQuery.Result3?>) {
        try {


            if (viewModel.currentFragment.isEmpty() &&  viewModel.TempstartDateFromResult.isNotEmpty() && viewModel.TempendDateFromResult.isNotEmpty()) {
                setDateInCalendar()
            } else if (!viewModel.TempstartDateFromResult.isNullOrEmpty() && viewModel.startDate.value != viewModel.TempstartDateFromResult && viewModel.startDate.value.isNullOrEmpty()) {
                resetDate()
            } else if (!viewModel.TempstartDateFromResult.isNullOrEmpty() && viewModel.startDate.value != viewModel.TempstartDateFromResult && !viewModel.startDate.value.isNullOrEmpty() && viewModel.startDate.value != "0") {
                viewModel.TempstartDateFromResult = viewModel.startDate.value!!
                viewModel.TempendDateFromResult = viewModel.endDate.value!!
                setDateInCalendar()
            }


            mBinding.rlListingAmenities.withModels {
                viewholderSelectDates {
                    id("select_dates")
                    dates(tvDates)
                    onclick(View.OnClickListener {
                        Utils.clickWithDebounce(it){
                            openCalender()
                        }
                    })
                }

                for (i in 0 until it.size) {
                    if (it[i]?.id == 2 && it[i]?.listSettings!!.size > 0) {
                        viewholderFilterPlusMinusGuest {
                            id(it[i]?.id)
                            text(it[i]?.typeLabel)
                            plusLimit1(it[i]?.listSettings!![0]?.endValue)
                            minusLimit1(it[i]?.listSettings!![0]?.startValue)
                            viewModel(viewModel)
                            onPlusClick(View.OnClickListener {
                                viewModel.personCapacity1.set(
                                    viewModel.personCapacity1.get()!!.toInt().plus(1).toString()
                                )
                            })
                            onMinusClick(View.OnClickListener {
                                viewModel.personCapacity1.set(
                                    viewModel.personCapacity1.get()!!.toInt().minus(1).toString()
                                )
                            })
                        }
                        break
                    }
                }


                viewholderDivider {
                    id("3")
                }


                viewholderFilterInstantbook {
                    id("instantBook")
                    isChecked(viewModel.TempbookingType == "instant")
                    onClick(View.OnClickListener {
                        viewModel.TempbookingType = if (viewModel.TempbookingType == "instant") {
                            ""
                        } else {
                            "instant"
                        }
                        this@withModels.requestModelBuild()
                    })
                }


                viewholderDivider {
                    id("1")
                }


                viewholderListingDetailsHeader {
                    id("priceheader")
                    header(resources.getString(R.string.price_range))
                    large(false)
                    isBlack(true)
                    typeface(Typeface.DEFAULT_BOLD)
                }


                if (viewModel.maxRange.value!! > viewModel.minRange.value!!) {
                    viewholderFilterPricerange {
                        id("pricerange")
                        price(viewModel.TempcurrencySymbol + viewModel.TempminRange.toString() + " - " + viewModel.TempcurrencySymbol + viewModel.TempmaxRange.toString())
                        onBind { _, view, _ ->
                            with(((view.dataBinding.root).findViewById<RangeSeekBar>(R.id.rangebar_filter_price))) {
                                setRange(
                                    viewModel.minRange.value!!.toFloat(),
                                    viewModel.maxRange.value!!.toFloat()
                                )
                                setValue(
                                    viewModel.TempminRange.toFloat(),
                                    viewModel.TempmaxRange.toFloat()
                                )
                                setOnRangeChangedListener(object : OnRangeChangedListener {
                                    override fun onStartTrackingTouch(
                                        view: RangeSeekBar?,
                                        isLeft: Boolean
                                    ) {
                                    }


                                    override fun onRangeChanged(
                                        view: RangeSeekBar?,
                                        leftValue: Float,
                                        rightValue: Float,
                                        isFromUser: Boolean
                                    ) {
                                        viewModel.TempminRange = leftValue.toInt()
                                        viewModel.TempmaxRange = rightValue.toInt()

                                        mBinding.rlListingAmenities.requestModelBuild()
                                    }


                                    override fun onStopTrackingTouch(
                                        view: RangeSeekBar?,
                                        isLeft: Boolean
                                    ) {
                                    }
                                })
                            }
                        }
                        onUnbind { _, view ->
                            with(((view.dataBinding.root).findViewById<RangeSeekBar>(R.id.rangebar_filter_price))) {
                                setOnRangeChangedListener(null)
                            }
                        }
                    }
                }


                viewholderDivider {
                    id("2")
                }


                viewholderListingDetailsHeader {
                    id("Roomsandbeds")
                    header(resources.getString(R.string.Rooms_and_beds))
                    large(false)
                    isBlack(true)
                    typeface(Typeface.DEFAULT_BOLD)
                }

                for (i in 0 until it.size) {
                    if (it[i]?.id == 5 && it[i]?.listSettings!!.size > 0) {
                        viewholderFilterPlusMinusBedroom {
                            id(it[i]?.id)
                            text(it[i]?.typeLabel)
                            plusLimit1(it[i]?.listSettings!![0]?.endValue)
                            minusLimit1(0)
                            viewModel(viewModel)
                            onPlusClick(View.OnClickListener {
                                viewModel.bedrooms1.set(
                                    viewModel.bedrooms1.get()!!.toInt().plus(1).toString()
                                )
                            })
                            onMinusClick(View.OnClickListener {
                                viewModel.bedrooms1.set(
                                    viewModel.bedrooms1.get()!!.toInt().minus(1).toString()
                                )
                            })
                        }
                        break
                    }
                }
                for (i in 0 until it.size) {
                    if (it[i]?.id == 6 && it[i]?.listSettings!!.size > 0) {
                        viewholderFilterPlusMinusBathroom {
                            id(it[i]?.id)
                            text(it[i]?.typeLabel)
                            plusLimit1(it[i]?.listSettings!![0]?.endValue)
                            minusLimit1(0)
                            viewModel(viewModel)
                            onPlusClick(View.OnClickListener {
                                viewModel.bathrooms1.set(
                                    viewModel.bathrooms1.get()!!.toInt().plus(1).toString()
                                )
                            })
                            onMinusClick(View.OnClickListener {
                                viewModel.bathrooms1.set(
                                    viewModel.bathrooms1.get()!!.toInt().minus(1).toString()
                                )
                            })
                        }
                        break
                    }
                }
                for (i in 0 until it.size) {
                    if (it[i]?.id == 8 && it[i]?.listSettings!!.size > 0) {
                        viewholderFilterPlusMinus {
                            id(it[i]?.id)
                            text(it[i]?.typeLabel)
                            plusLimit1(it[i]?.listSettings!![0]?.endValue)
                            minusLimit1(0)
                            viewModel(viewModel)
                            onPlusClick(View.OnClickListener {
                                viewModel.bed1.set(
                                    viewModel.bed1.get()!!.toInt().plus(1).toString()
                                )
                            })
                            onMinusClick(View.OnClickListener {
                                viewModel.bed1.set(
                                    viewModel.bed1.get()!!.toInt().minus(1).toString()
                                )
                            })
                        }
                        break
                    }
                }
                viewholderDivider {
                    id("4")
                }
                it.forEachIndexed { _, item ->
                    if (item?.id == 10 && item.listSettings!!.size > 0) {
                        viewholderListingDetailsHeader {
                            id("initialAmenitiesSize")
                            header(resources.getString(R.string.amenities))
                            large(false)
                            isBlack(true)
                            typeface(Typeface.DEFAULT_BOLD)
                        }
                        item?.listSettings!!.subList(0, viewModel.TempinitialAmenitiesSize)
                            .forEachIndexed { index, list ->
                                viewholderFilterCheckbox {
                                    id("initialAmenitiesSize ${list?.id}")
                                    text(list!!.itemName)
                                    isIconNeeded(true)
                                    if (list.image != null && list.image != "") {
                                        amenitiesImage(Constants.amenities + list.image)
                                    } else {
                                        amenitiesImage("")
                                    }
                                    isChecked(viewModel.Tempamenities.contains(list.id))
                                    onClick(View.OnClickListener {
                                        if (viewModel.Tempamenities.contains(list.id!!)) {
                                            viewModel.Tempamenities.remove(list.id!!)
                                        } else {
                                            viewModel.Tempamenities.add(list.id!!)
                                        }
                                        this@withModels.requestModelBuild()
                                    })
                                }
                            }
                        if (item.listSettings!!.size > 3) {
                            viewholderListingDetailsListShowmore {
                                id("readmore amenities")
                                if (viewModel.TempinitialAmenitiesSize == 2) {
                                    text(resources.getString(R.string.show_more))
                                    imgVisibility(1)
                                } else {
                                    text(resources.getString(R.string.close_all))
                                    imgVisibility(0)
                                }
                                clickListener(View.OnClickListener {
                                    viewModel.TempinitialAmenitiesSize =
                                        if (viewModel.TempinitialAmenitiesSize == 2) {
                                            item.listSettings!!.size
                                        } else {
                                            2
                                        }
                                    this@withModels.requestModelBuild()
                                })
                            }
                        }
                        viewholderDivider {
                            id("5")
                        }
                    }
                    if (item?.id == 12 && item?.listSettings!!.size > 0) {
                        viewholderListingDetailsHeader {
                            id("Facilities")
                            header(resources.getString(R.string.user_space))
                            large(false)
                            isBlack(true)
                            typeface(Typeface.DEFAULT_BOLD)
                        }
                        item.listSettings!!.subList(0, viewModel.TempinitialHouseRulesSize)
                            .forEachIndexed { index, list ->
                                viewholderFilterCheckbox {
                                    id("Facilities ${list?.id}")
                                    text(list?.itemName)
                                    isIconNeeded(true)
                                    if (list?.image != null && list.image != "") {
                                        amenitiesImage(Constants.amenities + list.image)
                                    } else {
                                        amenitiesImage("")
                                    }
                                    isChecked(viewModel.Tempspaces.contains(list?.id))
                                    onClick(View.OnClickListener {
                                        if (viewModel.Tempspaces.contains(list?.id!!)) {
                                            viewModel.Tempspaces.remove(list.id!!)
                                        } else {
                                            viewModel.Tempspaces.add(list.id!!)
                                        }
                                        this@withModels.requestModelBuild()
                                    })
                                }
                            }
                        if (item.listSettings!!.size > 3) {
                            viewholderListingDetailsListShowmore {
                                id("readmore facilities")
                                if (viewModel.TempinitialHouseRulesSize == 2) {
                                    text(resources.getString(R.string.show_more))
                                    imgVisibility(1)
                                } else {
                                    text(resources.getString(R.string.close_all))
                                    imgVisibility(0)
                                }
                                clickListener(View.OnClickListener {
                                    viewModel.TempinitialHouseRulesSize =
                                        if (viewModel.TempinitialHouseRulesSize == 2) {
                                            item.listSettings!!.size
                                        } else {
                                            2
                                        }
                                    this@withModels.requestModelBuild()
                                })
                            }
                        }
                        viewholderDivider {
                            id("6")
                        }
                    }
                    if (item?.id == 14 && item.listSettings!!.size > 0) {
                        viewholderListingDetailsHeader {
                            id("House rules")
                            header(resources.getString(R.string.house_rules))
                            large(false)
                            isBlack(true)
                            typeface(Typeface.DEFAULT_BOLD)
                        }
                        item.listSettings!!.subList(0, viewModel.TempinitialFacilitiesSize)
                            .forEachIndexed { index, list ->
                                viewholderFilterCheckbox {
                                    id("House rules ${list?.id}")
                                    text(list?.itemName)
                                    isChecked(viewModel.TemphouseRule.contains(list?.id))
                                    onClick(View.OnClickListener {
                                        if (viewModel.TemphouseRule.contains(list?.id!!)) {
                                            viewModel.TemphouseRule.remove(list.id!!)
                                        } else {
                                            viewModel.TemphouseRule.add(list.id!!)
                                        }
                                        this@withModels.requestModelBuild()
                                    })
                                }
                            }
                        if (item.listSettings!!.size > 3) {
                            viewholderListingDetailsListShowmore {
                                id("readmore house rules")
                                if (viewModel.TempinitialFacilitiesSize == 2) {
                                    text(resources.getString(R.string.show_more))
                                    imgVisibility(1)
                                } else {
                                    text(resources.getString(R.string.close_all))
                                    imgVisibility(0)
                                }
                                clickListener(View.OnClickListener {
                                    viewModel.TempinitialFacilitiesSize =
                                        if (viewModel.TempinitialFacilitiesSize == 2) {
                                            item.listSettings!!.size
                                        } else {
                                            2
                                        }
                                    this@withModels.requestModelBuild()
                                })
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDetach() {
        viewModel.TempstartDateFromResult=""
        viewModel.TempendDateFromResult=""
        viewModel.dateStart=""
        viewModel.dateEnd=""
        super.onDetach()
    }


    override fun onRetry() {


    }
}


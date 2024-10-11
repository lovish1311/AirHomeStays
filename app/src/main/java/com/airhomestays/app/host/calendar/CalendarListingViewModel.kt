package com.airhomestays.app.host.calendar

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.GetListingSpecialPriceQuery
import com.airhomestays.app.ManageListingsQuery
import com.airhomestays.app.R
import com.airhomestays.app.UpdateSpecialPriceMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import org.threeten.bp.LocalDate
import javax.inject.Inject


class CalendarListingViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<CalendarAvailabilityNavigator>(dataManager,resourceProvider) {

    data class list(val id: Int, val title: String = "", val room: String? ="", val img: String? ="",val currency: String? ="" )

    val startDate = MutableLiveData<LocalDate>()
    val endDate = MutableLiveData<LocalDate>()
    val selectedListing = MutableLiveData<list>()
    val manageListing1 = MutableLiveData<ArrayList<list>>()
    val blockedDates1 = MutableLiveData<List<GetListingSpecialPriceQuery.Result?>>()
    val calendarStatus = ObservableField<String>("available")
    val specialPrice = ObservableField<String>("")

    val isCalendarLoading = ObservableBoolean(false)
    val navigateBack = ObservableBoolean(false)

    init {
//        getManageListings()
    }

    fun getManageListings() {
        navigator.hideWholeView(true)

        val buildQuery = ManageListingsQuery()

        compositeDisposable.add(dataManager.getManageListings(buildQuery)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data?.manageListings
                        if (data!!.status == 200) {
                            setData(data.results!!)
                        } else if(data.status == 500) {
                            navigator.openSessionExpire("calendar listing VM")
                        } else {
                            if (data.errorMessage==null){
                                navigator.showError()
                            }else{
                                navigator.showToast(data.errorMessage.toString())
                            }
                            }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                    navigator.hideWholeView(true)
                    it.printStackTrace()
                } )
        )
    }

    private fun setData(data: List<ManageListingsQuery.Result?>) {
        try {
            val list = ArrayList<list>()
            data.forEachIndexed { _, result ->
                var title = ""
                if(result?.title.isNullOrEmpty()){
                    title = ""
                }else{
                    title = result?.title!!
                }
                if (result?.isReady!! ) {
                    list.add(list(result.id!!,
                            title,
                            result.settingsData!![0]?.listsettings?.itemName,
                            result.listPhotoName,result.listingData!!.currency))
                    if (list.size == 1) {
                        selectedList(list[0])
                    }
                }
            }
            manageListing1.value = list
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun selectedList(list: list) {
        selectedListing.value = list
        getListBlockedDates()
    }

    fun getListBlockedDates() {
        val buildQuery = GetListingSpecialPriceQuery(
                listId = selectedListing.value!!.id
                )
        navigator.hideCalendar(true)
        isCalendarLoading.set(true)
        compositeDisposable.add(dataManager.getListSpecialBlockedDates(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data?.getListingSpecialPrice
                        if (data!!.status == 200) {
                            isCalendarLoading.set(false)
                            blockedDates1.value = data.results!!
                        } else if(data.status == 500) {
                            navigator.openSessionExpire("calendar listing VM")
                        } else {
                            isCalendarLoading.set(true)
                            navigator.hideCalendar(true)
                            data.errorMessage?.let {
                                navigator.showToast(it)
                            } ?: navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        isCalendarLoading.set(true)
                        navigator.hideCalendar(true)
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    isCalendarLoading.set(true)
                    it.printStackTrace()
                    navigator.hideCalendar(true)
                    handleException(it)
                } )
        )
    }

    var isSpecialPrice : Double? = 0.0
    fun updateBlockedDates() {
        navigator.hideKeyboard()
        isLoading.set(true)

        if (calendarStatus.get() == "available") {
            isSpecialPrice = if (specialPrice.get()!!.isNotEmpty()) {
                specialPrice.get()?.toDouble()!!
            } else {
                null
            }
        }

        val buildQueryBuilder = UpdateSpecialPriceMutation(
            listId = selectedListing.value!!.id,
            blockedDates = getSelectedDates().toOptional(),
            isSpecialPrice = isSpecialPrice.toOptional(),
            calendarStatus = calendarStatus.get().toOptional()
        )


        compositeDisposable.add(dataManager.getUpdateSpecialListBlockedDates(buildQueryBuilder)
                .performOnBackOutOnMain(scheduler)
                .doFinally { setIsLoading(false) }
                .subscribe( { response ->
                    try {
                        val data = response.data?.updateSpecialPrice
                        if (data!!.status == 200) {
                            navigateBack.set(false)
                            navigator.showToast(resourceProvider.getString(R.string.your_dates_updated))
                            navigator.closeAvailability(true)
                        } else if(data.status == 500) {
                            navigateBack.set(false)
                            navigator.openSessionExpire("calendar listing VM")
                        } else {
                            navigateBack.set(true)
                            if (data.errorMessage==null){
                                navigator.showToast(resourceProvider.getString(R.string.list_not_available))
                            }else{
                                navigator.showToast(data.errorMessage.toString())
                            }

                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    it.printStackTrace()
                    handleException(it, true)
                } )
        )
    }

    private fun getSelectedDates(): MutableList<String> {
        val selectedList = ArrayList<String>()
        if (startDate.value != null && endDate.value != null) {
            var date = startDate.value!!.minusDays(1)
            while (date!!.isBefore(endDate.value)) {
                date = date.plusDays(1)
                selectedList.add(date.toString())
            }
        } else {
            selectedList.add(startDate.value.toString())
        }
        return selectedList
    }
}
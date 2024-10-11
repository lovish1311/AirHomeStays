package com.airhomestays.app.ui.host.step_three

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.GetCurrenciesListQuery
import com.airhomestays.app.GetListingDetailsStep3Query
import com.airhomestays.app.GetListingSettingQuery
import com.airhomestays.app.ManageListingStepsMutation
import com.airhomestays.app.R
import com.airhomestays.app.UpdateListingStep3Mutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.ListDetailsStep3
import javax.inject.Inject

class StepThreeViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<StepThreeNavigator>(dataManager, resourceProvider) {

    var listID: String = ""

    var retryCalled = ""

    var baseCurrency = ""

    val selectedRules = ArrayList<Int?>()

    var isSnackbarShown: Boolean = false

    var fromChoosen: String = "From"

    var toChoosen: String = "To"

    val availOptions =
        arrayOf("unavailable", "3months", "6months", "9months", "12months", "available")

    val fromTime = arrayOf(
        "Flexible",
        "8",
        "9",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
        "24",
        "25"
    )

    val toTime = arrayOf(
        "Flexible",
        "9",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
        "24",
        "25",
        "26"
    )

    val fromOptions = arrayOf(
        "Flexible",
        "8 AM",
        "9 AM",
        "10 AM",
        "11 AM",
        "12 PM(noon)",
        "1 PM",
        "2 PM",
        "3 PM",
        "4 PM",
        "5 PM",
        "6 PM",
        "7 PM",
        "8 PM",
        "9 PM",
        "10 PM",
        "11 PM",
        "12 AM (mid night)",
        "1 AM (next day)"
    )

    val toOptions = arrayOf(
        "Flexible",
        "9 AM",
        "10 AM",
        "11 AM",
        "12 PM(noon)",
        "1 PM",
        "2 PM",
        "3 PM",
        "4 PM",
        "5 PM",
        "6 PM",
        "7 PM",
        "8 PM",
        "9 PM",
        "10 PM",
        "11 PM",
        "12 AM (mid night)",
        "1 AM (next day)",
        "2 PM (next day)"
    )

    val datesAvailable = arrayOf(
        resourceProvider.getString(R.string.unavailable),
        resourceProvider.getString(R.string.three_month),
        resourceProvider.getString(R.string.six_month),
        resourceProvider.getString(R.string.nine_month),
        resourceProvider.getString(R.string.twelve_month),
        resourceProvider.getString(R.string.available_by_default)
    )

    val cancellationPolicy = arrayOf(
        resourceProvider.getString(R.string.flexible),
        resourceProvider.getString(R.string.moderate),
        resourceProvider.getString(R.string.strict)
    )

    val infantOptions = arrayOf(
        "0",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
    )
    val visitorsOptions = arrayOf(
        "0",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
    )
    val petsOptions = arrayOf(
        "0",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
    )
    val guestsOptions = Array(60) { (it + 1).toString() }.toMutableList().apply {
        add(0, "select")
    }

    var isListAdded: Boolean = false

    val currency = MutableLiveData<GetCurrenciesListQuery.GetCurrencies?>()

    var noticeTime: String? = ""
    var noticeFrom: String? = ""
    var noticeTo: String? = ""
    var cancelPolicy: Int? = 1
    var bookWind: String? = ""
    var bookingType: String = "instant"


    val minNight = ObservableField<String>()
    val maxNight = ObservableField<String>()

    val basePrice = ObservableField("")
    val additionalBasePrice = ObservableField("0.0")
    val noOfGuest =  ObservableField("")
    val cleaningPrice = ObservableField("")
    val petPrice = ObservableField("0.0")
    val infantPrice = ObservableField("0.0")
    val visitorPrice = ObservableField("0.0")
    val weekDiscount = ObservableField("")
    val monthDiscount = ObservableField("")
    val TaxPrice=ObservableField("")
    val selectedCurrency = MutableLiveData<String>()
    var selectArray = arrayOf(true, false)


    lateinit var listSettingArray: MutableLiveData<GetListingSettingQuery.Results>
    var listSettingArrayTemp: MutableLiveData<GetListingSettingQuery.Results?> = MutableLiveData()

    lateinit var listDetailsArray: GetListingDetailsStep3Query.Results

    lateinit var listDetailsStep3: MutableLiveData<ListDetailsStep3?>

    fun listSetting(): MutableLiveData<GetListingSettingQuery.Results> {
        if (!::listSettingArray.isInitialized) {
            listSettingArray = MutableLiveData()
            listDetailsStep3 = MutableLiveData()
            getListStep3Details()
        }
        return listSettingArray
    }


    enum class NextStep {
        GUESTREQUEST,
        HOUSERULE,
        GUESTBOOK,
        GUESTNOTICE,
        BOOKWINDOW,
        TRIPLENGTH,
        LISTPRICE,
        DISCOUNTPRICE,
        INSTANTBOOK,
        LAWS,
        FINISH,
        NODATA
    }

    enum class BackScreen {
        GUESTREQUEST,
        HOUSERULE,
        GUESTBOOK,
        GUESTNOTICE,
        BOOKWINDOW,
        TRIPLENGTH,
        LISTPRICE,
        DISCOUNTPRICE,
        INSTANTBOOK,
        LAWS,
        FINISH,
        NODATA
    }

    fun getListingSetting() {

        val buildQuery = GetListingSettingQuery()
        compositeDisposable.add(dataManager.dogetListingSettings(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                val data = response.data
                try {
                    if (data?.getListingSettings!!.status == 200) {
                        val result = data.getListingSettings!!.results
                        listSettingArrayTemp.value = result
                        getCurrency()
                    } else if (data?.getListingSettings!!.status == 500) {
                        navigator.openSessionExpire("")
                    } else {
                        if (data.getListingSettings!!.errorMessage == null)
                            navigator.showError()
                        else navigator.showToast(
                            data.getListingSettings!!.errorMessage.toString()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, { handleException(it) })
        )

    }
    fun getListingSetting(from: String) {
        val request = GetListingSettingQuery()

        compositeDisposable.add(dataManager.doGetListingSettings(request)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                { response ->
                    val data = response.data
                    try {
                        if (data?.getListingSettings?.status == 200) {
                            val result = data!!.getListingSettings!!.results

                        } else if (data?.getListingSettings!!.status == 500) {
                            navigator.openSessionExpire("")
                        } else {
                            if (data.getListingSettings!!.errorMessage == null)
                                navigator.showError()
                            else navigator.showToast(
                                data.getListingSettings!!.errorMessage.toString()
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError(e)
                    }
                },
                {
                    handleException(it)
                }
            ))
    }

    fun getCurrency() {
        val query = GetCurrenciesListQuery()
        compositeDisposable.add(dataManager.getCurrencyList(query)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                val data = response.data!!.getCurrencies
                if (data?.status == 200) {
                    currency.value = data
                    listSettingArray.value = listSettingArrayTemp.value
                    val currencyResult = currency.value!!.results
                    currencyResult!!.forEachIndexed { index, cresult ->
                        if (cresult?.isBaseCurrency!!) {
                            baseCurrency = cresult.symbol!!
                        }
                    }
                    if (isListAdded) {
                        setDatafromAPI()
                    } else {
                        setData()
                    }
                } else if (data?.status == 500) {
                    navigator.openSessionExpire("")
                } else {
                    //   postsOutcome.value = Event(Outcome.failure(recommendListing.status().toString()))
                }
            }, {
                handleException(it)
            })
        )
    }

    fun getListStep3Details() {
        val buildQuery = GetListingDetailsStep3Query(listId = listID, preview = true.toOptional())
        compositeDisposable.add(dataManager.doGetStep3Details(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                val data = response.data!!.getListingDetails
                try {
                    if (data?.status == 200) {
                        if (data.results!!.listingData != null) {
                            if(data.results!!.listingData?.basePrice!=null){
                                isListAdded = true
                                retryCalled = ""
                                listDetailsArray = data!!.results!!
                                    noOfGuest.set(data!!.results?.personCapacity!!.toString())
                                getListingSetting()
                            }else{
                                isListAdded = false
                                getListingSetting()
                            }

                        } else {
                            isListAdded = false
                            noOfGuest.set(data!!.results?.personCapacity!!.toString())
                            getListingSetting()
                        }
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("")
                    } else {
                        isListAdded = false
                        navigator.show404Page()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, { handleException(it) })
        )
    }

    fun updateListStep3(from: String) {
        var cleanPri: Double? = null
        var weekDis: Int? = null
        var monthDis: Int? = null
        var taxprice:Double?=null
        if (!cleaningPrice.get().isNullOrEmpty()) {
            cleanPri = cleaningPrice.get()!!.toDouble()
        }
        if (!TaxPrice.get().isNullOrEmpty()) {
            taxprice = TaxPrice.get()!!.toDouble()
        }
        if (!weekDiscount.get().isNullOrEmpty()) {
            weekDis = weekDiscount.get()!!.toInt()
        }
        if (!monthDiscount.get().isNullOrEmpty()) {
            monthDis = monthDiscount.get()!!.toInt()
        }

        if (checkChoosenTime()) {
            val query = UpdateListingStep3Mutation(
                id = listID.toInt().toOptional(),
                houseRules = selectedRules.toOptional(),
                bookingNoticeTime = noticeTime.toOptional(),
                checkInStart= fromChoosen.toOptional(),
                checkInEnd = toChoosen.toOptional(),
                maxDaysNotice = bookWind.toOptional(),
                minNight= minNight.get()!!.toInt().toOptional(),
                maxNight= maxNight.get()!!.toInt().toOptional(),
                basePrice = basePrice.get()!!.toDouble().toOptional(),
                guestBasePrice = listDetailsStep3.value!!.guestCount!!.toInt().toOptional(),
                additionalPrice = (additionalBasePrice.get()?:"0.0").toDouble().toOptional(),
                cleaningPrice = cleanPri.toOptional(),
                tax = taxprice.toOptional(),
                currency = listDetailsStep3.value!!.currency.toOptional(),
                weeklyDiscount = weekDis.toOptional(),
                monthlyDiscount = monthDis.toOptional(),
                bookingType = bookingType,
                cancellationPolicy = cancelPolicy.toOptional(),
                petPrice = petPrice.get()!!.toDouble().toOptional(),
                infantPrice = infantPrice.get()!!.toDouble().toOptional(),
                visitorsPrice = visitorPrice.get()!!.toDouble().toOptional(),
                petLimit = listDetailsStep3.value!!.petsCount.toOptional(),
                visitorsLimit = listDetailsStep3.value!!.visitorCount.toOptional(),
                infantLimit = listDetailsStep3.value!!.infantsCount.toOptional()

            )
            compositeDisposable.add(dataManager.doUpdateListingStep3(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    val data = response.data?.updateListingStep3
                    try {
                        if (data?.status == 200) {
                            retryCalled = ""
                            updateStepDetails()

                        } else if (data?.status == 500) {
                            navigator.openSessionExpire("")
                        } else {
                            if (data?.errorMessage == null)
                                navigator.showError()
                            else navigator.showSnackbar(
                                resourceProvider.getString(R.string.error),
                                data?.errorMessage!!
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, { handleException(it) })
            )
        }
    }

    fun updateStepDetails() {
        val buildQuery = ManageListingStepsMutation(
            listId = listID,
            currentStep = 3
            )

        compositeDisposable.add(dataManager.doManageListingSteps(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                val data = response.data?.manageListingSteps
                try {
                    if (data?.status == 200) {
                        navigator.navigateToScreen(NextStep.FINISH)
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("")
                    } else if (data?.status == 400) {
                        navigator.showSnackbar(
                            resourceProvider.getString(R.string.update_error),
                            data.errorMessage.toString()
                        )
                    } else {
                        if (data?.errorMessage == null)
                            navigator.showError()
                        else navigator.showToast(data.errorMessage.toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, { handleException(it) })
        )
    }

    fun setData() {
        listDetailsStep3.value = ListDetailsStep3(
            noticePeriod = listSettingArray.value!!.bookingNoticeTime!!
                .listSettings!![0]!!.itemName,
            noticeFrom = resourceProvider.getString(R.string.from),
            noticeTo = resourceProvider.getString(R.string.to),
            availableDate = datesAvailable.get(datesAvailable.size - 1),
            cancellationPolicy = cancellationPolicy.get(0),
            minStay = listSettingArray.value!!.minNight!!.listSettings!![0]!!.startValue
                .toString(),
            maxStay = listSettingArray.value!!.maxNight!!.listSettings!![0]!!.startValue
                .toString(),
            basePrice = 0.0,
            cleaningPrice = 0.0,
            currency = baseCurrency,
            weekDiscount = 0,
            monthDiscount = 0,
            infantsCount  = 0,
            petsCount = 0,
            visitorCount  =0,
            guestCount = 0.0,
            totalGuestCount = noOfGuest.get()?.toInt(),
            taxpercentage = 0.0
        )

        minNight.set(
            listSettingArray.value!!.minNight!!.listSettings!![0]!!.startValue.toString()
        )
        maxNight.set(
            listSettingArray.value!!.maxNight!!.listSettings!![0]!!.startValue.toString()
        )
        bookWind = availOptions.get(availOptions.size - 1)
        noticeTime =
            listSettingArray.value!!.bookingNoticeTime!!.listSettings!![0]!!.id.toString()
    }

    fun setDatafromAPI() {
        try {
            var bookingTime: String = ""
            var bookID: String = ""
            var bookCheck24: String = ""
            var bookout24: String = ""
            var bookcheckIn: String = ""
            var bookCheckOut: String = ""
            var noticeDur: String = ""
            var cancella: String = ""
            var noticeDur1: String = ""
            if (listDetailsArray.listingData!!.bookingNoticeTime!!.equals("")) {
                bookingTime =
                    listSettingArray.value!!.bookingNoticeTime!!.listSettings!![0]
                        ?.itemName!!
                bookID = listSettingArray.value!!.bookingNoticeTime!!.listSettings!![0]?.id!!
                    .toString()
            } else {
                listSettingArray.value!!.bookingNoticeTime!!.listSettings!!
                    .forEachIndexed { index, listSetting15 ->
                        if (listSetting15?.id!! == listDetailsArray.listingData!!
                                .bookingNoticeTime!!.toInt()
                        ) {
                            bookingTime = listSetting15.itemName.toString()
                            bookID = listSetting15.id.toString()
                        }
                    }
            }

            noticeTime = bookID

            if (listDetailsArray.houseRules!!.isNotEmpty()) {
                val list = listDetailsArray.houseRules!!
                list.forEachIndexed { index, houseRule ->
                    selectedRules.add(houseRule?.id)
                }
            }

            fromTime.forEachIndexed { index, s ->
                if (s.equals(listDetailsArray.listingData!!.checkInStart)) {
                    bookcheckIn = fromOptions.get(index)
                    bookCheck24 = fromTime.get(index)
                }
            }

            noticeFrom = bookcheckIn

            fromChoosen = bookCheck24

            toTime.forEachIndexed { index, s ->
                if (s.equals(listDetailsArray.listingData!!.checkInEnd)) {
                    bookCheckOut = toOptions.get(index)
                    bookout24 = toTime.get(index)
                }
            }
            noticeTo = bookCheckOut
            toChoosen = bookout24

            availOptions.forEachIndexed { index, s ->
                if (s.equals(listDetailsArray.listingData!!.maxDaysNotice)) {
                    noticeDur = datesAvailable.get(index)
                    noticeDur1 = availOptions.get(index)
                }
            }
            bookWind = noticeDur1
            cancelPolicy = listDetailsArray.listingData!!.cancellationPolicy
            minNight.set(listDetailsArray.listingData!!.minNight.toString())
            maxNight.set(listDetailsArray.listingData!!.maxNight.toString())
            var base: Double = 0.0
            var clean: Double = 0.0
            var infantPriceSet: Double = 0.0
            var petPriceSet: Double = 0.0
            var noOfPet: Int = 0
            var noOfInfant: Int = 0
            var week: Int = 0
            var month: Int = 0
            var tax:Double=0.0
            var additionalBase:Double=0.0
            if (listDetailsArray.listingData!!.basePrice == null) {
                basePrice.set("")
                base = 0.0
            } else {
                basePrice.set(Utils.formatDecimal(listDetailsArray.listingData!!.basePrice!!))
            }
            if (listDetailsArray.listingData!!.tax == null) {
                TaxPrice.set("")
                tax = 0.0
            } else {
                TaxPrice.set(Utils.formatDecimal(listDetailsArray.listingData!!.tax!!))
            }
            if (listDetailsArray.listingData!!.cleaningPrice == null) {
                cleaningPrice.set("")
                clean = 0.0
            } else {
                cleaningPrice.set(
                    Utils.formatDecimal(
                        listDetailsArray.listingData!!.cleaningPrice!!
                    )
                )
            }
            if (listDetailsArray.listingData!!.infantPrice == null) {
                infantPrice.set("0")
                infantPriceSet = 0.0
            } else {
                infantPrice.set(
                    Utils.formatDecimal(
                        listDetailsArray.listingData!!.infantPrice!!
                    )
                )
            }
            if (listDetailsArray.listingData!!.petPrice == null) {

                petPrice.set("0")
                petPriceSet = 0.0
            } else {
                petPrice.set(
                    Utils.formatDecimal(
                        listDetailsArray.listingData!!.petPrice!!
                    )
                )
            }
            if (listDetailsArray.listingData!!.visitorsPrice == null) {
                visitorPrice.set("0")
                petPriceSet = 0.0
            } else {
                visitorPrice.set(
                    Utils.formatDecimal(
                        listDetailsArray.listingData!!.visitorsPrice!!
                    )
                )
            }
            if (listDetailsArray.listingData!!.additionalPrice == null) {
                additionalBasePrice.set("0.0")
                additionalBase = 0.0
            //    guestsOptions.set(0)
                petPriceSet = 0.0
            } else {
                additionalBasePrice.set(
                    Utils.formatDecimal(
                        listDetailsArray.listingData!!.additionalPrice!!
                    )
                )
            }
            if (listDetailsArray.listingData!!.infantLimit == null) {
                listDetailsStep3.value?.infantsCount = 0
                petPriceSet = 0.0
            } else {
                listDetailsStep3.value?.infantsCount =listDetailsArray.listingData!!.infantLimit

            }
            if (listDetailsArray.listingData!!.petLimit == null) {
                listDetailsStep3.value?.petsCount = 0

            } else {
                listDetailsStep3.value?.petsCount = listDetailsArray.listingData!!.petLimit

            }
            if (listDetailsArray.listingData!!.visitorsLimit == null) {
                listDetailsStep3.value?.visitorCount = 0

            } else {
                listDetailsStep3.value?.visitorCount = listDetailsArray.listingData!!.visitorsLimit

            }
            if (listDetailsArray.listingData!!.guestBasePrice == null) {
                listDetailsStep3.value?.guestCount = 0.0

            } else {
                listDetailsStep3.value?.guestCount = listDetailsArray.listingData!!.guestBasePrice

            }

            if (listDetailsArray!!.listingData!!.weeklyDiscount == null) {
                weekDiscount.set("")
                week = 0
            } else {

                weekDiscount.set(listDetailsArray!!.listingData!!.weeklyDiscount.toString())
                try {
                    if (weekDiscount.get()!!.toInt() == 0) {
                        weekDiscount.set("0")
                    }
                } catch (e: Exception) {

                }
                week = weekDiscount.get()!!.toInt()
            }

            if (listDetailsArray!!.listingData!!.monthlyDiscount == null) {
                monthDiscount.set("")
                month = 0
            } else {
                monthDiscount.set(listDetailsArray!!.listingData!!.monthlyDiscount.toString())
                try {
                    if (monthDiscount.get()!!.toInt() == 0) {
                        monthDiscount.set("0")
                    }
                } catch (e: Exception) {

                }
                month = monthDiscount.get()!!.toInt()
            }
            val cancelIndex = listDetailsArray!!.listingData!!.cancellationPolicy!!

            bookingType = listDetailsArray!!.bookingType!!

            if (bookingType.equals("instant")) {
                selectArray.set(0, true)
                selectArray.set(1, false)
            } else {
                selectArray.set(0, false)
                selectArray.set(1, true)
            }

            listDetailsStep3.value = ListDetailsStep3(
                noticePeriod = bookingTime,
                noticeFrom = noticeFrom,
                noticeTo = noticeTo,
                availableDate = noticeDur,
                cancellationPolicy = cancellationPolicy.get(cancelIndex - 1),
                minStay = minNight.get(),
                maxStay = maxNight.get(),
                basePrice = base,
                cleaningPrice = clean,
                currency = listDetailsArray!!.listingData!!.currency,
                weekDiscount = week,
                monthDiscount = month,
                infantsCount = listDetailsArray!!.listingData?.infantLimit?:0,
                petsCount = listDetailsArray!!.listingData?.petLimit?:0,
                visitorCount = listDetailsArray!!.listingData?.visitorsLimit?:0,
                guestCount = listDetailsArray!!.listingData?.guestBasePrice?:0.0,
                totalGuestCount = noOfGuest.get()?.toInt(),
                taxpercentage = tax
            )
        } catch (E: Exception) {
            navigator.showError()
        }
    }

    fun onClick(screen: StepThreeViewModel.NextStep) {
        if (screen == NextStep.DISCOUNTPRICE) {
            if (checkPrice()) {
                if(checkTax()){
                    navigator.navigateToScreen(screen)
                }
            }
        } else if (screen == NextStep.INSTANTBOOK) {
            if (checkDiscount()) {
                navigator.navigateToScreen(screen)
            }
        } else {
            navigator.navigateToScreen(screen)
        }
    }

    fun checkPrice(): Boolean {

        if (basePrice.get().isNullOrEmpty()) {
            isSnackbarShown = true
            navigator.showSnackbar(
                resourceProvider.getString(R.string.add_price),
                resourceProvider.getString(R.string.base_price_error)
            )
        } else if (basePrice.get().equals("0") || basePrice.get().equals("0.0")) {
            isSnackbarShown = true
            navigator.showSnackbar(
                resourceProvider.getString(R.string.add_price),
                resourceProvider.getString(R.string.base_price_error)
            )
        } else {
            try {
                val price = basePrice.get()!!.toDouble()
                if (price >= 1) {
                    var price = basePrice.get()
                    var occrence = 0
                    for (i in 0 until price!!.length) {
                        val pr = price[i]
                        val ch = '.'
                        if (ch == pr) {
                            occrence++
                        }
                    }
                    if (occrence == 0 || occrence == 1) {
                        if (!cleaningPrice.get().isNullOrEmpty()) {
                            try {
                                var price = cleaningPrice.get()
                                var occ = 0
                                for (i in 0 until price!!.length) {
                                    val pr = price[i]
                                    val ch = '.'
                                    if (ch == pr) {
                                        occ++
                                    }
                                }

                                if (occ == 0 || occ == 1) {
                                    navigator.hideKeyboard()
                                    navigator.hideSnackbar()
                                    val data = listDetailsStep3.value
                                    data?.basePrice = basePrice.get()!!.toDouble()
                                    if (cleaningPrice.get().isNullOrEmpty()) {
                                        data?.cleaningPrice = null
                                    } else {
                                        data?.cleaningPrice = cleaningPrice.get()!!.toDouble()
                                    }
                                    listDetailsStep3.value = data
                                    return true
                                } else {
                                    navigator.showSnackbar(
                                        resourceProvider.getString(R.string.add_price),
                                        resourceProvider.getString(R.string.cleaning_price_error)
                                    )
                                }
                            } catch (e: Exception) {
                                navigator.showSnackbar(
                                    resourceProvider.getString(R.string.add_price),
                                    resourceProvider.getString(R.string.cleaning_price_error)
                                )
                            }
                        } else {
                            navigator.hideKeyboard()
                            navigator.hideSnackbar()
                            val data = listDetailsStep3.value
                            data?.basePrice = basePrice.get()!!.toDouble()
                            if (cleaningPrice.get().isNullOrEmpty()) {
                                data?.cleaningPrice = null
                            } else {
                                data?.cleaningPrice = cleaningPrice.get()!!.toDouble()
                            }
                            listDetailsStep3.value = data
                            return true
                        }

                    } else {
                        isSnackbarShown = true
                        navigator.showSnackbar(
                            resourceProvider.getString(R.string.add_price),
                            resourceProvider.getString(R.string.price_error_text)
                        )
                    }
                } else {
                    navigator.showSnackbar(
                        resourceProvider.getString(R.string.add_price),
                        resourceProvider.getString(R.string.price_error_text)
                    )
                }
            } catch (e: NumberFormatException) {
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.add_price),
                    resourceProvider.getString(R.string.price_error_text)
                )
            }
        }
        return false
    }

    fun checkDiscount(): Boolean {
        val data = listDetailsStep3.value
        var weekCheck: Boolean = false
        var monthCheck: Boolean = false
        if (!weekDiscount.get().equals("")) {
            if (weekDiscount.get()!!.toLong() >= 100) {
                isSnackbarShown = true
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.discounts),
                    resourceProvider.getString(R.string.discount_error_text)
                )
                return false
            } else {
                weekCheck = true
                data?.weekDiscount = weekDiscount.get()!!.toInt()
            }
        } else {
            weekCheck = true
            data?.weekDiscount = null
        }
        if (!monthDiscount.get().equals("")) {
            if (monthDiscount.get()!!.toLong() >= 100) {
                isSnackbarShown = true
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.discounts),
                    resourceProvider.getString(R.string.discount_error_text)
                )
                return false
            } else {
                monthCheck = true
                data?.monthDiscount = monthDiscount.get()!!.toInt()
            }
        } else {
            monthCheck = true
            data?.monthDiscount = null
        }
        if (weekCheck && monthCheck) {
            return true
        }
        return false
    }
    fun checkTax(): Boolean {
        val data = listDetailsStep3.value
        var TaxCheck: Boolean = false
        if (!TaxPrice.get().equals("")) {
            if (TaxPrice.get()!!.toLong() >= 100) {
                isSnackbarShown = true
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.taxes),
                    resourceProvider.getString(R.string.taxprice_error_text)
                )
                return false
            } else {
                TaxCheck = true
                data?.taxpercentage = TaxPrice.get()!!.toDouble()
            }
        } else {
            TaxCheck = true
            data?.taxpercentage = null
        }

        if (TaxCheck ) {
            return true
        }
        return false
    }
    fun checkTripLength(): Boolean {
        if (minNight.get()!!.toInt() == 0 && maxNight.get()!!.toInt() == 0) {
            navigator.hideSnackbar()
            return true
        } else {
            if (maxNight.get()!!.toInt() != 0) {
                if ((minNight.get()!!.toInt() > maxNight.get()!!.toInt())) {
                    isSnackbarShown = true
                    navigator.showSnackbar(
                        resourceProvider.getString(R.string.trip_length),
                        resourceProvider.getString(R.string.stay_error_text)
                    )
                    isSnackbarShown = true
                } else {
                    navigator.hideSnackbar()
                    return true
                }
            } else {
                navigator.hideSnackbar()
                return true
            }
        }
        return false
    }

    fun checkChoosenTime(): Boolean {
        if (fromChoosen == "Flexible" || toChoosen == "Flexible") {
            retryCalled = "update"
            return true
        } else {
            if (fromChoosen.toInt() >= toChoosen.toInt()) {
                isSnackbarShown = true
                navigator.showSnackbar(
                    resourceProvider.getString(R.string.time),
                    resourceProvider.getString(R.string.checkin_error_text)
                )
            } else {
                retryCalled = "update"
                return true
            }
        }
        return false
    }


}
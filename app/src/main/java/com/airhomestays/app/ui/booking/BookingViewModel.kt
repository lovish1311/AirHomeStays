package com.airhomestays.app.ui.booking

import android.content.Intent
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.GetProfileQuery
import com.airhomestays.app.GetReservationQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.vo.BillingDetails
import com.airhomestays.app.vo.ListingInitData
import javax.inject.Inject

class BookingViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
): BaseViewModel<BookingNavigator>(dataManager, resourceProvider) {

    val billingDetails = MutableLiveData<BillingDetails>()

    var listDetails = ListingInitData()

    val msg = ObservableField("")

    val avatar = MutableLiveData<String>()

    val reservation = MutableLiveData<GetReservationQuery.Results>()







    fun setInitialData(intent: Intent) {
        try {
            listDetails = intent.getParcelableExtra("lisitingDetails")!!
            billingDetails.value = BillingDetails(
                checkIn = intent.getStringExtra("checkIn").orEmpty(),
                checkOut = intent.getStringExtra("checkOut").orEmpty(),
                basePrice = intent.getDoubleExtra("basePrice", 0.0),
                nights = intent.getIntExtra("nights", 0),
                guestServiceFee = intent.getDoubleExtra("guestServiceFee", 0.0),
                cleaningPrice = intent.getDoubleExtra("cleaningPrice", 0.0),
                discount = intent.getDoubleExtra("discount", 0.0),
                discountLabel = intent.getStringExtra("discountLabel"),
                total = intent.getDoubleExtra("total", 0.0),
                houseRule = intent.getStringArrayListExtra("houseRules")!!,
                title = intent.getStringExtra("title").orEmpty(),
                image = intent.getStringExtra("image").orEmpty(),
                cancellation = intent.getStringExtra("cancellation").orEmpty(),
                cancellationContent = intent.getStringExtra("cancellationContent").orEmpty(),
                guest = intent.getIntExtra("guest", 0),
                additionalGuest = intent.getIntExtra("additionalGuest", 0),
                pets = intent.getIntExtra("petCount", 0),
                infantCount = intent.getIntExtra("infantCount", 0),
                visitors = intent.getIntExtra("visitors", 0),
                hostServiceFee = intent.getDoubleExtra("hostServiceFee", 0.0),
                additionalGuestPrice = intent.getDoubleExtra("additionalGuestPrice", 0.0),
                currency = intent.getStringExtra("currency").orEmpty(),
                listId = intent.getIntExtra("listId", 0),
                bookingType = intent.getStringExtra("bookingType").orEmpty(),
                isProfilePresent = intent.getBooleanExtra("isProfilePresent", false),
                averagePrice = intent.getDoubleExtra("averagePrice",0.0),
                priceForDays = intent.getDoubleExtra("priceForDays",0.0),
                specialPricing = intent.getStringExtra("specialPricing").orEmpty(),
                isSpecialPriceAssigned = intent.getBooleanExtra("isSpecialPriceAssigned",false),
                razorPayOrderID = intent.getStringExtra("razorPayOrderId").toString(),
                razorPayPaymentID = intent.getStringExtra("razorPayPaymentId").toString(),
                infantPrice = intent.getDoubleExtra("infantPrice", 0.0) ,
                petPrice = intent.getDoubleExtra("petPrice", 0.0) ,
                visitorPrice = intent.getDoubleExtra("visitorsPrice", 0.0) ,
                threadId = intent.getIntExtra("threadId",0)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkVerification() {
        val buildQuery = GetProfileQuery()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe( { response ->
                try {
                    val data = response.data!!.userAccount
                    if (data?.status == 200) {
                        val result = data.result
                        if(result?.picture.isNullOrEmpty()) {
                            navigator.navigateToScreen(4)
                            billingDetails.value?.isProfilePresent = false
                        } else {
                            dataManager.currentUserProfilePicUrl = result?.picture
                            navigator.navigateToScreen(5)
                            billingDetails.value?.isProfilePresent = true
                        }
                    } else if(data?.status == 500) {
                        navigator.openSessionExpire("BookingVM")
                    } else {
                        if (data?.errorMessage==null)
                            navigator.showError()
                        else navigator.showToast(data.errorMessage.toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, { handleException(it) } )
        )
    }
}
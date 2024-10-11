package com.airhomestays.app.host.payout.addPayout

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.*
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.apollographql.apollo3.exception.ApolloNetworkException
import timber.log.Timber
import javax.inject.Inject
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.AccountParams
import com.stripe.android.model.Address
import com.stripe.android.model.PersonTokenParams
import com.stripe.android.model.Token
import org.json.JSONObject

class AddPayoutViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<AddPayoutNavigator>(dataManager, resourceProvider) {

    val firstName = ObservableField("")
    val accountType = ObservableField(resourceProvider.getString(R.string.individual))
    val lastNameVisible = MutableLiveData<Boolean>()
    val updateSecPayment = MutableLiveData<Boolean>()
    val lastName = ObservableField("")
    val account = ObservableField("")
    val accountBank = ObservableField("")
    val accountHolder = ObservableField("")
    val cnfAccount = ObservableField("")
    val cnfAccountBank = ObservableField("")
    val ssn = ObservableField("")
    val routingNo = ObservableField("")
    val country = ObservableField("")
    val countryCode = ObservableField("")
    val address1 = ObservableField("")
    val address2 = ObservableField("")
    val ifscCode = ObservableField("")
    val city = ObservableField("")
    val state = ObservableField("")
    val zip = ObservableField("")
    val listPref = ObservableField("")
    val email = ObservableField("")
    val currency = ObservableField("")
    val gstNumber = ObservableField("")
    val mobileNumber = ObservableField("")
    val panNumber = ObservableField("")
    var isDOB = false
    var connectingURL: String = ""
    var isUpdateSec: Boolean =false
    var accountID: String = ""
    var onRetryCalled: String = ""
    private var personToken: String? = null
    private var accountToken: String?
    lateinit var stripe: Stripe
    val europeCountries = listOf("AT", "BE", "BG", "CY", "CZ", "DK", "EE",
        "FI", "FR", "DE", "GR", "HU", "IE", "IT",
        "LV", "LT", "LU", "MT", "NL", "NO", "PL",
        "PT", "RO", "SK", "SI", "ES", "SE", "CH")
    lateinit var paymentMethods : MutableLiveData<List<GetPaymentMethodsQuery.Result?>?>
    lateinit var secPayment: MutableLiveData<GetSecPaymentQuery.Data?>

    init {
        isDOB = dataManager.isDOB!!
        listPref.set(resourceProvider.getString(R.string.individual))
        accountToken = null
    }

    fun loadPayoutMethods(): MutableLiveData<List<GetPaymentMethodsQuery.Result?>?> {
        if (!::paymentMethods.isInitialized) {
            paymentMethods = MutableLiveData()
            getPayoutMethods()
        }
        return paymentMethods
    }

    fun checkPaypalInfo(): Boolean {
        if (email.get()!!.trim().isNotBlank()) {
            if (Utils.isValidEmail(email.get()!!)) {
                if (currency.get()!!.isNotBlank()) {
                    return true
                } else navigator.showToast(resourceProvider.getString(R.string.please_select_currency))
            } else navigator.showToast(resourceProvider.getString(R.string.please_enter_valid_email_address))
        } else navigator.showToast(resourceProvider.getString(R.string.please_enter_email_address))
        return false
    }

    fun checkAccountInfo(): Boolean {
        if (address1.get()!!.trim().isNotBlank()) {
            if (city.get()!!.trim().isNotBlank()) {
                if (state.get()!!.trim().isNotBlank()) {
                    if (zip.get()!!.isNotBlank()) {
                        return true
                    } else navigator.showToast(resourceProvider.getString(R.string.please_enter_zip_code))
                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_the_state))
            } else navigator.showToast(resourceProvider.getString(R.string.please_enter_city))
        } else navigator.showToast(resourceProvider.getString(R.string.please_enter_address_1))
        return false
    }

    fun checkSecPaymentApi(){
        getSecPayment()
    }

    fun checkAccountDetails(): Boolean {
        if(accountType.get()=="Individual"){
            if(!accountType.get()!!.trim().equals(resourceProvider.getString(R.string.account_type))) {
                if (firstName.get()!!.trim().isNotBlank()) {
                    if (lastName.get()!!.trim().isNotBlank()) {
                        if (routingNo.get()!!.trim().isNotBlank() || (europeCountries.contains(countryCode.get()) || countryCode.get() == "MX" || countryCode.get() == "NZ" ) ) {
                            if (account.get()!!.trim().isNotBlank()) {
                                if (cnfAccount.get()!!.trim().isNotBlank()) {
                                    if (account.get()!! == cnfAccount.get()!!) {
                                        return true
                                    } else navigator.showToast(resourceProvider.getString(R.string.please_enter_correct_confirm_account_number, checkCountryAndReturn()))
                                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_confirm_account_number, checkCountryAndReturn()))
                            } else navigator.showToast(resourceProvider.getString(R.string.please_enter_account_number, checkCountryAndReturn()))
                        } else navigator.showToast((if (countryCode.get() == "UK" || countryCode.get() == "GB") resourceProvider.getString(R.string.please_enter_sort_number) else resourceProvider.getString(R.string.please_enter_routing_number)))
                    } else navigator.showToast(resourceProvider.getString(R.string.please_enter_last_name))
                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_first_name))
            } else navigator.showToast(resourceProvider.getString(R.string.account_type_error))
            return false
        }else{
            if(!accountType.get()!!.trim().equals(resourceProvider.getString(R.string.account_type))) {
                if (firstName.get()!!.trim().isNotBlank()) {
                    if (routingNo.get()!!.trim().isNotBlank() || (europeCountries.contains(countryCode.get()) || countryCode.get() == "MX" || countryCode.get() == "NZ" ) ) {
                        if (account.get()!!.trim().isNotBlank()) {
                            if (cnfAccount.get()!!.trim().isNotBlank()) {
                                if (account.get()!! == cnfAccount.get()!!) {
                                    return true
                                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_correct_confirm_account_number, checkCountryAndReturn()))
                            } else navigator.showToast(resourceProvider.getString(R.string.please_enter_confirm_account_number, checkCountryAndReturn()))
                        } else navigator.showToast(resourceProvider.getString(R.string.please_enter_account_number, checkCountryAndReturn()))
                    } else navigator.showToast(if (countryCode.get() == "UK" || countryCode.get() == "GB") resourceProvider.getString(R.string.please_enter_sort_number) else resourceProvider.getString(R.string.please_enter_routing_number) )
                } else navigator.showToast(resourceProvider.getString(R.string.please_enter_first_name))
            } else navigator.showToast(resourceProvider.getString(R.string.account_type_error))
            return false
        }
    }
    fun getResultAccountToken(type: Int, accountHashMap: HashMap<String, String>) {
        setIsLoading(true)
        val payoutEmail: String = if (email.get()!!.isEmpty()) {
            dataManager.currentUserEmail.toString()
        } else {
            email.get()!!
        }
        val accountParams : AccountParams = if (accountHashMap["accountHolderType"] == resourceProvider.getString(R.string.individual)) {
            AccountParams.create(true,
                AccountParams.BusinessTypeParams.Individual(
                    firstName = accountHashMap["firstName"],
                    lastName = accountHashMap["lastName"],
                    email = payoutEmail,
                    address = Address.fromJson(JSONObject("""{"city": "${accountHashMap["city"]}","country": "${accountHashMap["country"]}","line1": "${accountHashMap["line1"]}","line2": "${accountHashMap["line2"]}","postal_code": "${accountHashMap["postal_code"]}","state": "${accountHashMap["state"]}"}"""))
                ))
        }else{
            AccountParams.create(true,
                AccountParams.BusinessTypeParams.Company(
                    name = accountHashMap["firstName"],
                    address = Address.fromJson(JSONObject("""{"city": "${accountHashMap["city"]}","country": "${accountHashMap["country"]}","line1": "${accountHashMap["line1"]}","line2": "${accountHashMap["line2"]}","postal_code": "${accountHashMap["postal_code"]}","state": "${accountHashMap["state"]}"}"""))
                ))
        }

        stripe.createAccountToken(accountParams, callback = object : ApiResultCallback<Token> {
            override fun onError(e: Exception) {
                Timber.e(e, e.message)
                navigator.showToast(e.message ?: "Token Error")

                setIsLoading(false)
            }

            override fun onSuccess(result: Token) {
                accountToken = result.id

                val accType: String = if(accountType.get().equals(resourceProvider.getString(R.string.individual))){
                    "individual"
                }else{
                    "company"
                }
                if (accType == "company") {



                    val personAcc = PersonTokenParams.Builder()
                        .setAddress(Address.fromJson(JSONObject("""{"city": "${accountHashMap["city"]}","country": "${accountHashMap["country"]}","line1": "${accountHashMap["line1"]}","line2": "${accountHashMap["line2"]}","postal_code": "${accountHashMap["postal_code"]}","state": "${accountHashMap["state"]}"}""")))
                        .setEmail(payoutEmail)
                        .setRelationship(PersonTokenParams.Relationship.Builder().setRepresentative(true).build())
                        .setFirstName(accountHashMap["firstName"])
                        .setLastName(accountHashMap["lastName"])
                        .build()

                    stripe.createPersonToken(personAcc, callback = object : ApiResultCallback<Token> {
                        override fun onError(e: Exception) {
                            Timber.e(e, e.message)
                            navigator.showToast(e.message ?: "Token Error")
                            setIsLoading(false)
                        }

                        override fun onSuccess(result: Token) {
                            personToken = result.id
                            addPayout(type)
                            setIsLoading(false)
                        }
                    })
                }
                else  {
                    personToken = null
                    addPayout(type)
                    setIsLoading(false)
                }
            }
        })

    }

    private fun checkCountryAndReturn(): String {
        return if (europeCountries.contains(countryCode.get())) resourceProvider.getString(R.string.iban_number)
        else resourceProvider.getString(R.string.account_number)
    }

    fun addPayout(type: Int) {

        var accType: String
        if (accountType.get()
                .equals(resourceProvider.getString(R.string.individual), ignoreCase = true)
        ) {
            accType = "individual"
        } else {
            accType = "company"
        }

        var payoutEmail: String
        if (email.get()!!.isEmpty()) {
            payoutEmail = dataManager.currentUserEmail.toString()
        } else {
            payoutEmail = email.get()!!
        }

        val mutate: AddPayoutMutation
        if (europeCountries.contains(countryCode.get())
                .not() && (countryCode.get() != "MX" || countryCode.get() != "NZ")
        ) {
            mutate = AddPayoutMutation(
                methodId=type,
                payEmail=payoutEmail,
                address1=address1.get()!!,
                address2=address2.get()!!,
                city= city.get()!!,
                country=countryCode.get()!!,
                state = state.get()!!,
                zipcode = zip.get()!!,
                currency = currency.get() ?: "USD",
                firstname = firstName.get().toOptional(),
                lastname = lastName.get().toOptional(),
                accountNumber= account.get().toOptional(),
                routingNumber= routingNo.get().toOptional(),
                businessType = accType.toOptional(),
                accountToken = accountToken.toOptional(),
                personToken= personToken.toOptional(),
            )
        } else {
            mutate = AddPayoutMutation(
                methodId =type,
                payEmail = payoutEmail,
                address1 = address1.get()!!,
                address2 = address2.get()!!,
                city = city.get()!!,
                country = countryCode.get()!!,
                state = state.get()!!,
                zipcode = zip.get()!!,
                currency = currency.get() ?: "USD",
                firstname = firstName.get().toOptional(),
                lastname = lastName.get().toOptional(),
                accountNumber = account.get().toOptional(),
                businessType = accType.toOptional(),
                accountToken = accountToken.toOptional(),
                personToken = personToken.toOptional(),
            )

        }
        compositeDisposable.add(dataManager.addPayout(mutate)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data?.addPayout
                    if (data?.status == 200) {
                        if (type == 1) {
                            navigator.moveToScreen(AddPayoutActivity.Screen.FINISH)
                        } else {
                            connectingURL = data.connectUrl.toString()
                            accountID = data.stripeAccountId.toString()
                            navigator.moveToScreen(AddPayoutActivity.Screen.WEBVIEW)
                            navigator.moveToScreen(AddPayoutActivity.Screen.FINISH)

                        }
                    } else if (data?.status == 400) {
                        navigator.showToast(data.errorMessage!!)
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("AddPayoutVM")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }

    fun buildTokenHashMap(): HashMap<String, String> {
        val hashMap = hashMapOf<String, String>()
        hashMap["firstName"] = firstName.get().toString()
        hashMap["lastName"] = lastName.get().toString()
        hashMap["country"] = countryCode.get().toString()
        hashMap["currency"] = currency.get().toString()
        hashMap["accountNumber"] = account.get().toString()
        hashMap["accountHolderType"] = accountType.get().toString()
        hashMap["routingNumber"] = routingNo.get().toString()
        hashMap["line1"] = address1.get().toString()
        hashMap["line2"] = address2.get().toString()
        hashMap["city"] = city.get().toString()
        hashMap["state"] = state.get().toString()
        hashMap["postal_code"] = zip.get().toString()

        return hashMap

    }

    fun setPayout() {
        val query = ConfirmPayoutMutation(
            currentAccountId= accountID.toOptional()
        )
        compositeDisposable.add(dataManager.setPayout(query)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.confirmPayout
                    if (data!!.status == 200) {
                        navigator.moveToScreen(AddPayoutActivity.Screen.FINISH)
                    } else {
                        if (data.errorMessage==null){
                            navigator.showError()
                        }else{
                            navigator.showToast(data.errorMessage.toString())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }

    fun getPayoutMethods() {
        val query = GetPaymentMethodsQuery()

        compositeDisposable.add(dataManager.getPayoutsMethod(query)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.getPaymentMethods
                    if (data!!.status == 200) {
                        paymentMethods.value = data.results

                    } else {
                        if (data.errorMessage == null) {
                            navigator.showError()
                        } else {
                            navigator.showToast(data.errorMessage.toString())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }

    fun createSecPayment() {
        try {
            val query = CreateSecPaymentMutation(
                userId = dataManager.currentUserId!!,
                country = country.get().toString(),
            address1 = address1.get().toOptional(),
            address2 =  address2.get().toOptional(),
            city = city.get().toString(),
            state = state.get().toString(),
            zipcode = zip.get().toString(),
            accountNumber =  accountBank.get().toString(),
            confirmAccountNumber =  cnfAccountBank.get().toString(),
           ifscCode =  ifscCode.get().toString(),
           accountHolderName =  accountHolder.get().toString(),
          gstNumber =  gstNumber.get().toString(),
           panNumber =  panNumber.get().toString(),
           mobileNumber =  mobileNumber.get().toString(),
           accountType =  accountType.get().toString())

            compositeDisposable.add(dataManager.createSecPayment(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({
                    try {
                      /*  token.value = null
                        if (it.data?.createReservation?.status == 200) {
                            if (selectedPaymentType == 1) {
                                if (it.data?.createReservation?.redirectUrl != null) {
                                    setIsLoading(true)
                                    reservationId.value =
                                        it.data?.createReservation?.reservationId ?: 0
                                    navigator.moveToPayPalWebView(
                                        it.data?.createReservation?.redirectUrl ?: ""
                                    )
                                } else {
                                    navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                                }
                            } else {
                                navigator.moveToReservation(
                                    it!!.data!!.createReservation!!.results!!.id!!
                                )
                            }
                        } else if (it.data?.createReservation?.status == 500) {
                            navigator.openSessionExpire("PaymentVM")
                        } else {
                            if (it.data?.createReservation?.errorMessage == null) {
                                if (it.data?.createReservation
                                        ?.requireAdditionalAction == true
                                ) {
                                    paymentIntentSecret.value =
                                        it.data?.createReservation?.paymentIntentSecret!!
                                    reservationId.value =
                                        it.data?.createReservation?.reservationId!!

                                    stripeReqAdditionAction.value = "1"
                                } else {
                                    stripeReqAdditionAction.value = "0"
                                }
                            } else {
                                navigator.showSnackbar(
                                    "",
                                    it.data?.createReservation?.errorMessage!!
                                )
                            }
                        }*/
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, {
                    if (it is ApolloNetworkException) {
                        navigator.showOffline()
                    } else {
                        navigator.showError()
                        //navigator.finishScreen()
                    }
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateSecPayment() {
        try {
            val query = UpdateSecPaymentMutation(
                userId = dataManager.currentUserId!!,
                country =  country.get().toOptional(),
            address1 = address1.get().toOptional(),
            address2 =  address2.get().toOptional(),
            city = city.get().toOptional(),
            state = state.get().toOptional(),
            zipcode = zip.get().toOptional(),
            accountNumber =  accountBank.get().toOptional(),
            confirmAccountNumber = cnfAccountBank.get().toOptional(),
           ifscCode =  ifscCode.get().toOptional(),
           accountHolderName =  accountHolder.get().toOptional(),
          gstNumber =  gstNumber.get().toOptional(),
           panNumber =  panNumber.get().toOptional(),
           mobileNumber = mobileNumber.get().toOptional() ,
           accountType =  accountType.get().toOptional())

            compositeDisposable.add(dataManager.updateSecPayment(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({
                    try {
                        if (it.data?.updateSecPayment?.status == "success"){
                             updateSecPayment.value = true
                           // navigator.moveToScreen(AddPayoutActivity.Screen.FINISH)
                        }
                      /*  token.value = null
                        if (it.data?.createReservation?.status == 200) {
                            if (selectedPaymentType == 1) {
                                if (it.data?.createReservation?.redirectUrl != null) {
                                    setIsLoading(true)
                                    reservationId.value =
                                        it.data?.createReservation?.reservationId ?: 0
                                    navigator.moveToPayPalWebView(
                                        it.data?.createReservation?.redirectUrl ?: ""
                                    )
                                } else {
                                    navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                                }
                            } else {
                                navigator.moveToReservation(
                                    it!!.data!!.createReservation!!.results!!.id!!
                                )
                            }
                        } else if (it.data?.createReservation?.status == 500) {
                            navigator.openSessionExpire("PaymentVM")
                        } else {
                            if (it.data?.createReservation?.errorMessage == null) {
                                if (it.data?.createReservation
                                        ?.requireAdditionalAction == true
                                ) {
                                    paymentIntentSecret.value =
                                        it.data?.createReservation?.paymentIntentSecret!!
                                    reservationId.value =
                                        it.data?.createReservation?.reservationId!!

                                    stripeReqAdditionAction.value = "1"
                                } else {
                                    stripeReqAdditionAction.value = "0"
                                }
                            } else {
                                navigator.showSnackbar(
                                    "",
                                    it.data?.createReservation?.errorMessage!!
                                )
                            }
                        }*/
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, {
                    if (it is ApolloNetworkException) {
                        navigator.showOffline()
                    } else {
                        navigator.showError()
                        //navigator.finishScreen()
                    }
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getSecPayment() {
        try {
            val query = GetSecPaymentQuery(userId = dataManager.currentUserId!!)

            compositeDisposable.add(dataManager.getSecPayment(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({
                    try {
                        if (it.data?.getSecPayment?.status == "success"){
                            secPayment.postValue(it.data)
                            country.set(it.data?.getSecPayment?.result?.country)
                            city.set(it.data?.getSecPayment?.result?.city)
                            address1.set(it.data?.getSecPayment?.result?.address1)
                            address2.set(it.data?.getSecPayment?.result?.address2)
                            state.set(it.data?.getSecPayment?.result?.state)
                            zip.set(it.data?.getSecPayment?.result?.zipcode)
                            accountHolder.set(it.data?.getSecPayment?.result?.accountHolderName)
                            accountBank.set(it.data?.getSecPayment?.result?.accountNumber)
                            mobileNumber.set(it.data?.getSecPayment?.result?.mobileNumber)
                            cnfAccountBank.set(it.data?.getSecPayment?.result?.confirmAccountNumber)
                            ifscCode.set(it.data?.getSecPayment?.result?.ifscCode)
                            gstNumber.set(it.data?.getSecPayment?.result?.gstNumber)
                            panNumber.set(it.data?.getSecPayment?.result?.panNumber)
                        }

                      /*  token.value = null
                        if (it.data?.createReservation?.status == 200) {
                            if (selectedPaymentType == 1) {
                                if (it.data?.createReservation?.redirectUrl != null) {
                                    setIsLoading(true)
                                    reservationId.value =
                                        it.data?.createReservation?.reservationId ?: 0
                                    navigator.moveToPayPalWebView(
                                        it.data?.createReservation?.redirectUrl ?: ""
                                    )
                                } else {
                                    navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                                }
                            } else {
                                navigator.moveToReservation(
                                    it!!.data!!.createReservation!!.results!!.id!!
                                )
                            }
                        } else if (it.data?.createReservation?.status == 500) {
                            navigator.openSessionExpire("PaymentVM")
                        } else {
                            if (it.data?.createReservation?.errorMessage == null) {
                                if (it.data?.createReservation
                                        ?.requireAdditionalAction == true
                                ) {
                                    paymentIntentSecret.value =
                                        it.data?.createReservation?.paymentIntentSecret!!
                                    reservationId.value =
                                        it.data?.createReservation?.reservationId!!

                                    stripeReqAdditionAction.value = "1"
                                } else {
                                    stripeReqAdditionAction.value = "0"
                                }
                            } else {
                                navigator.showSnackbar(
                                    "",
                                    it.data?.createReservation?.errorMessage!!
                                )
                            }
                        }*/
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, {
                    if (it is ApolloNetworkException) {
                        navigator.showOffline()
                    } else {
                        navigator.showError()
                        //navigator.finishScreen()
                    }
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun loadPayment(): MutableLiveData<GetSecPaymentQuery.Data?>{
        if (!::secPayment.isInitialized) {
            secPayment = MutableLiveData()
            getSecPayment()
        }
        return secPayment
    }
}
package com.airhomestays.app.ui.profile.confirmPhonenumber

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.AddPhoneNumberMutation
import com.airhomestays.app.GetCountrycodeQuery
import com.airhomestays.app.GetEnteredPhoneNoQuery
import com.airhomestays.app.R
import com.airhomestays.app.VerifyPhoneNumberMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import javax.inject.Inject

class ConfirmPhnoViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<ConfirmPhnoNavigator>(dataManager, resourceProvider) {

    val phoneno = ObservableField("")
    val phoneType = ObservableField("")

    val countryCode = ObservableField("+1")

    val list = MutableLiveData<List<GetCountrycodeQuery.Result?>?>()
    val listSearch = MutableLiveData<ArrayList<GetCountrycodeQuery.Result?>>()
    val isCountryCodeLoad = ObservableField(false)

    val code = ObservableField("")
    val lottieProgress =
        ObservableField<ConfirmPhnoViewModel.LottieProgress>(ConfirmPhnoViewModel.LottieProgress.NORMAL)
    val isNext = ObservableField<Boolean>(false)

    init {
        getCountryCodes()
        if (dataManager.countryCode.isNullOrEmpty())
            countryCode.set("+1")
        else
            countryCode.set(dataManager.countryCode)


        phoneType.set(dataManager.phoneNoType)
        if (dataManager.currentPhoneNo.isNullOrEmpty())
            phoneno.set("")
        else
            phoneno.set(dataManager.currentPhoneNo)
    }

    enum class PHScreen {
        COUNTRYCODE,
        FOURDIGITCODE,
        CONFIRMPHONE,
        FINISHED
    }

    enum class LottieProgress {
        NORMAL,
        LOADING,
        CORRECT
    }

    fun onSearchTextChanged(text: CharSequence) {
        if (text.isNotEmpty()) {
            val searchText = text.toString().capitalize()
            val containsItem = ArrayList<GetCountrycodeQuery.Result?>()
            list.value?.forEachIndexed { _, result ->
                result?.countryName?.let {
                    if (it.contains(searchText)) {
                        containsItem.add(result)
                    }
                }
            }
            listSearch.value = containsItem
        } else {
            list.value?.let {
                listSearch.value = ArrayList(it)
            }
        }
    }

    fun onCodeTextChanged() {
        navigator.hideSnackbar()
    }

    fun onClick(PHScreen: PHScreen) {
        navigator.navigateScreen(PHScreen)
    }

    fun getCountryCodes() {
        val query = GetCountrycodeQuery()
        compositeDisposable.add(dataManager.getCountryCode(query)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.getCountries
                    if (data?.status == 200) {
                        isCountryCodeLoad.set(true)
                        list.value = response.data!!.getCountries!!.results
                    } else if (data!!.status == 500) {
                        (navigator as BaseNavigator).openSessionExpire("ConfirmPhnVM")
                    } else {
                        isCountryCodeLoad.set(false)
                        if (data.errorMessage == null) {
                            navigator.showError()
                        } else {
                            navigator.showToast(data.errorMessage.toString())
                        }

                    }
                } catch (e: Exception) {
                    isCountryCodeLoad.set(false)
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                isCountryCodeLoad.set(false)
                handleException(it)
            })
        )
    }

    fun addPhnumber() {
        val query = AddPhoneNumberMutation(
            countryCode = countryCode.get().toString(),
            phoneNumber = phoneno.get().toString()
        )
        compositeDisposable.add(dataManager.addPhoneNumber(query)
            .doOnSubscribe {
                isNext.set(true)
                lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.LOADING)
            }
            .doFinally {
                isNext.set(false)
                lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.NORMAL)
            }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.addPhoneNumber
                    if (data?.status == 200) {
                        dataManager.phoneNoType =
                            data.phoneNumberStatus //Default value set in SplashViewModel also
                        if (data.phoneNumberStatus == "1") {
                            navigator.navigateScreen(PHScreen.FOURDIGITCODE)
                        } else if (data.phoneNumberStatus == "2") {
                            navigator.navigateScreen(PHScreen.FINISHED)
                        }
                    } else if (data?.status == 400) {
                        dataManager.phoneNoType = "1"
                        data.errorMessage?.let {
                            navigator.showSnackbar(
                                resourceProvider.getString(R.string.phone_number),
                                it
                            )
                        } ?: navigator.showSnackbar(
                            resourceProvider.getString(R.string.phone_number),
                            resourceProvider.getString(R.string.invalid_phone_number)
                        )
                    } else {
                        navigator.hideKeyboard()
                        if (data?.errorMessage == null)
                            navigator.showError()
                        else
                            navigator.showToast(data.errorMessage.toString())
                        (navigator as BaseNavigator).openSessionExpire("ConfirmPhnVM")
                    }
                } catch (e: Exception) {
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }


    fun sendCodeAgain() {
        val query = AddPhoneNumberMutation(
            countryCode = countryCode.get().toString(),
            phoneNumber = phoneno.get().toString()
        )
        compositeDisposable.add(dataManager.addPhoneNumber(query)
            .doOnSubscribe {
                isNext.set(true)
                lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.LOADING)
            }
            .doFinally {
                isNext.set(false)
                lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.NORMAL)
            }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.addPhoneNumber
                    if (data?.status == 200) {
                        navigator.showToast(resourceProvider.getString(R.string.we_sent_the_code_to_your_phone_number))
                    } else if (data?.status == 400) {
                        data.errorMessage?.let {
                            navigator.showSnackbar(
                                resourceProvider.getString(R.string.phone_number),
                                it
                            )
                        } ?: navigator.showSnackbar(
                            resourceProvider.getString(R.string.phone_number),
                            resourceProvider.getString(R.string.invalid_phone_number)
                        )
                    } else {
                        navigator.hideKeyboard()
                        navigator.showError()
                        (navigator as BaseNavigator).openSessionExpire("ConfirmPhnVM")
                    }
                } catch (e: Exception) {
                    navigator.showError()
                }
            }, {
                handleException(it)
            })
        )
    }


    fun sendVerification() {
        val query = GetEnteredPhoneNoQuery()
        compositeDisposable.add(dataManager.getPhoneNumber(query)
            .doOnSubscribe {
                isNext.set(true)
                lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.LOADING)
            }
            .doFinally {
                isNext.set(false)
                lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.NORMAL)
            }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.getPhoneData
                    if (data?.status == 200) {
                        dataManager.currentUserPhoneNo = data.countryCode + data.phoneNumber
                        dataManager.currentPhoneNo = data.phoneNumber
                        dataManager.countryCode = data.countryCode
                        countryCode.set(data.countryCode)
                        dataManager.isPhoneVerified = data.verification?.isPhoneVerified
                        navigator.showToast(resourceProvider.getString(R.string.we_sent_the_code_to_your_phone_number))
                    } else if (data!!.status == 500) {
                        (navigator as BaseNavigator).openSessionExpire("ConfirmPhnVM")
                    } else {
                        navigator.showError()
                    }
                } catch (e: Exception) {
                    navigator.showError()
                }
            }, {
                handleException(it, true)
            })
        )
    }

    fun verifyCode() {
        try {
            val query = VerifyPhoneNumberMutation(
                verificationCode = code.get()!!.toInt()
            )
            compositeDisposable.add(dataManager.verifyPhoneNumber(query)
                .doOnSubscribe {
                    isNext.set(true)
                    lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.LOADING)
                }
                .doFinally {
                    isNext.set(false)
                    lottieProgress.set(ConfirmPhnoViewModel.LottieProgress.NORMAL)
                }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data!!.verifyPhoneNumber
                        if (data?.status == 200) {
                            dataManager.isPhoneVerified = true
                            navigator.navigateScreen(ConfirmPhnoViewModel.PHScreen.FINISHED)
                        } else if (data!!.status == 500) {
                            (navigator as BaseNavigator).openSessionExpire("ConfirmPhnVM")
                        } else {
                            navigator.hideKeyboard()
                            if (data.errorMessage == null)
                                navigator.showSnackbar(
                                    resourceProvider.getString(R.string.error),
                                    resourceProvider.getString(R.string.the_entered_4_digit_code_is_incorrect)
                                )
                            else navigator.showToast(data.errorMessage.toString())
                        }
                    } catch (e: Exception) {
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }
}
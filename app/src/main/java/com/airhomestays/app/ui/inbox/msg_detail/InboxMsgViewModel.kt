package com.airhomestays.app.ui.inbox.msg_detail

import android.content.Intent
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.apollographql.apollo3.api.ApolloResponse
import com.airhomestays.app.GetBillingCalculationQuery
import com.airhomestays.app.GetProfileQuery
import com.airhomestays.app.GetThreadsQuery
import com.airhomestays.app.GetUnReadThreadCountQuery
import com.airhomestays.app.R
import com.airhomestays.app.ReadMessageMutation
import com.airhomestays.app.SendMessageMutation
import com.airhomestays.app.ViewListingDetailsQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.ui.inbox.InboxNavigator
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.ChatMaskingUtil
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.BillingDetails
import com.airhomestays.app.vo.InboxMsgInitData
import com.airhomestays.app.vo.PreApproved
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InboxMsgViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<InboxNavigator>(dataManager, resourceProvider) {

    private val repoResult = MutableLiveData<Listing<GetThreadsQuery.ThreadItem>>()
    val inboxInitData = MutableLiveData<InboxMsgInitData>()
    val posts: LiveData<PagedList<GetThreadsQuery.ThreadItem>> = repoResult.switchMap() {
        it.pagedList
    }
    val networkState: LiveData<NetworkState> = repoResult.switchMap() { it.networkState }
    val refreshState: LiveData<NetworkState> = repoResult.switchMap() { it.refreshState }
    val msg = ObservableField("")
    val isNewMessage = MutableLiveData<Boolean>()
    val isRetry = ObservableField(-1)

    var retryCalled = ""

    var timerValue = ObservableField("")

    var preApprovalVisible = ObservableField(false)

    var preApproved = MutableLiveData<PreApproved>()

    var isProfilePic = false
    var approved = false

    private lateinit var disposable: Disposable

    var listingDetails: ViewListingDetailsQuery.Results? = null

    var billingCalculation: GetBillingCalculationQuery.Result? = null
    val billingDetails = MutableLiveData<BillingDetails>()
    val lottieProgress =
        ObservableField<InboxMsgViewModel.LottieProgress>(InboxMsgViewModel.LottieProgress.LOADING)
    val isBook = ObservableField<Boolean>(false)
    var loginStatus = 3

    init {
        loginStatus = dataManager.currentUserLoggedInMode
        preApproved.value = PreApproved(
            0,
            "",
            false,
            0,
            "",
            "",
            0,
            visitors = 0,
            pets = 0,
            infants = 0
        )
    }

    enum class LottieProgress {
        NORMAL,
        LOADING,
        CORRECT
    }

    fun notificationRefresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun notificationRetry() {
        repoResult.value?.retry?.invoke()
    }

    fun setInitialData(intent: Intent) {
        try {
            val initData =
                intent.extras?.getParcelable<InboxMsgInitData>("inboxInitData") as InboxMsgInitData
            inboxInitData.value = initData
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun getInboxMsg() {
        val buildQuery = GetThreadsQuery(
            threadId = getThreadId().toOptional(),
            threadType = "Guest".toOptional()
        )
        repoResult.value = dataManager.listOfInboxMsg(buildQuery, 10)
    }

    fun getInboxMsg1(page: Int): Single<ApolloResponse<GetThreadsQuery.Data>> {
        isRetry.set(-1)
        val buildQuery = GetThreadsQuery(
            threadId = getThreadId().toOptional(),
            threadType = "Guest".toOptional(),
            currentPage = page.toOptional()
        )
        return dataManager.listOfInboxMsg1(buildQuery)
    }

     fun getThreadId(): Int {
        return inboxInitData.value?.threadId ?: 0
    }

    fun readMessage() {
        val mutate = ReadMessageMutation(
            threadId=  getThreadId()
        )

        compositeDisposable.add(dataManager.setReadMessage(mutate)
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                {
                    try {
                        it.data?.let { response ->
                            if (response.readMessage?.status == 200) {
                            } else if (response.readMessage?.status == 500) {
                                navigator.openSessionExpire("InboxMsgVM")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                {
                    // navigator.showError()
                }
            ))
    }

    fun sendMsg() {
        isRetry.set(1)
        val chatMaskingUtil = ChatMaskingUtil()

        val message = msg.get()!!.trim()
        if (message.isNotEmpty()) {
            val maskedMessage = chatMaskingUtil.applyChatMasking(message)
            val mutate = SendMessageMutation(
                threadId = inboxInitData.value!!.threadId,
                content = maskedMessage.toOptional(),
                type = "message".toOptional()
            )
            compositeDisposable.add(dataManager.sendMessage(mutate)
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                    {
                        try {
                            it.data?.let { response ->
                                if (response.sendMessage?.status == 200) {
                                    msg.set("")
                                    navigator.addMessage(response.sendMessage!!.results!!)
                                } else if (response.sendMessage?.status == 500) {
                                    navigator.openSessionExpire("InboxMsgVM")
                                }
                            } ?: navigator.showError()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navigator.showError()
                        }
                    },
                    {
                        isRetry.set(1)
                        handleException(it)
                    }
                ))
        }
    }


    fun newMsg() {
        if (::disposable.isInitialized) {
            compositeDisposable.remove(disposable)
        }

        val buildQuery = GetUnReadThreadCountQuery(
            threadId = getThreadId().toOptional()
        )
        disposable = Observable.interval(5, TimeUnit.SECONDS, Schedulers.io())
            .switchMap {
                dataManager.getNewMessage(buildQuery)
                    .onErrorResumeNext { _: Throwable -> Observable.empty() }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                try {
                    val result = it.data?.getUnReadThreadCount
                    if (result!!.status == 200) {
                        if (isNewMessage.value != result.results?.isUnReadMessage) {
                            isNewMessage.value = result.results?.isUnReadMessage!!
                        }
                    } else if (result.status == 500) {
                        navigator.openSessionExpire("InboxMsgVM")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { it.printStackTrace() })

        compositeDisposable.add(disposable)
    }

    fun checkVerification() {
        isRetry.set(-1)
        isBook.set(true)
        lottieProgress.set(InboxMsgViewModel.LottieProgress.LOADING)
        val buildQuery = GetProfileQuery()
        compositeDisposable.add(dataManager.doGetProfileDetailsApiCall(buildQuery)
            .performOnBackOutOnMain(scheduler)
            .doFinally {
                isBook.set(false)
                lottieProgress.set(InboxMsgViewModel.LottieProgress.NORMAL)
            }
            .subscribe({ response ->
                try {
                    val data = response.data!!.userAccount
                    if (data?.status == 200) {
                        val result = data.result
                        retryCalled = ""
                        if (result!!.verification!!.isEmailConfirmed!!.not()) {
                            navigator.showSnackbar(
                                resourceProvider.getString(R.string.verification),
                                resourceProvider.getString(R.string.email_not_verified),
                                resourceProvider.getString(R.string.dismiss)
                            )
                        } else {
                            dataManager.currentUserProfilePicUrl = result.picture
                            isProfilePic = !result.picture.isNullOrEmpty()
                            getBillingCalculation()
                        }
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("InboxMsgVM")
                    } else {
                        navigator.showSnackbar("Info  ", data!!.errorMessage!!)
                    }
                } catch (e: Exception) {
                    navigator.showToast(resourceProvider.getString(R.string.something_went_wrong_action))
                }
            }, {
                isRetry.set(4)

                handleException(it)
            })
        )
    }

    fun getBillingCalculation() {
        try {
            val buildQuery = GetBillingCalculationQuery(
                listId = inboxInitData.value!!.listID!!,
                startDate = Utils.getBlockedDateFormat1(preApproved.value!!.startDate),
                endDate = Utils.getBlockedDateFormat1(preApproved.value!!.endDate),
                guests = preApproved.value!!.personCapacity,
                visitors = preApproved.value!!.visitors!!,
                infants = preApproved.value!!.infants!!,
                pets = preApproved.value!!.pets!!,
                convertCurrency = getUserCurrency()
            )

            compositeDisposable.add(dataManager.getBillingCalculation(buildQuery)
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data?.getBillingCalculation
                        when {
                            data?.status == 200 -> {
                                billingCalculation = data.result
                                getListingDetails("billing")
                            }

                            data?.status == 500 -> {
                                navigator.openSessionExpire("InboxMsgVM")
                            }

                            else -> {
                                isBook.set(false)
                                lottieProgress.set(InboxMsgViewModel.LottieProgress.NORMAL)
                                navigator.showSnackbar("Info  ", data!!.errorMessage!!)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, { handleException(it) }))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getListingDetails(str: String) {
        val buildQuery = ViewListingDetailsQuery(
            listId = inboxInitData.value!!.listID!!
        )

        compositeDisposable.add(dataManager.doListingDetailsApiCall(buildQuery)
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data?.viewListing
                    if (data!!.status == 200) {
                        listingDetails = data.results
                        if (str.equals("billing")) {
                            isBook.set(false)
                            lottieProgress.set(InboxMsgViewModel.LottieProgress.NORMAL)
                            navigator.openBillingActivity()
                        } else {
                            navigator.openListingDetails()
                        }
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("InboxMsgVM")
                    } else {
                        isBook.set(false)
                        lottieProgress.set(InboxMsgViewModel.LottieProgress.NORMAL)
                        navigator.showToast(resourceProvider.getString(R.string.list_not_available))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { handleException(it) }
            )
        )
    }


    fun clearHttp() {
        dataManager.clearHttpCache()
    }
}

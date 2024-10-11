package com.airhomestays.app.ui.host.hostInbox.host_msg_detail

import android.content.Intent
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.apollographql.apollo3.api.ApolloResponse
import com.airhomestays.app.GetThreadsQuery
import com.airhomestays.app.GetUnReadThreadCountQuery
import com.airhomestays.app.R
import com.airhomestays.app.ReadMessageMutation
import com.airhomestays.app.ReservationStatusMutation
import com.airhomestays.app.SendMessageMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.ui.inbox.InboxNavigator
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.airhomestays.app.vo.InboxMsgInitData
import com.airhomestays.app.vo.PreApproved
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HostInboxMsgViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<InboxNavigator>(dataManager, resourceProvider) {

    private val repoResult = MutableLiveData<Listing<GetThreadsQuery.ThreadItem>>()
    val inboxInitData = MutableLiveData<InboxMsgInitData>()
    val posts: LiveData<PagedList<GetThreadsQuery.ThreadItem>> = repoResult.switchMap() {
        it.pagedList
    }
    val networkState = repoResult.switchMap() { it.networkState }!!
    val refreshState = repoResult.switchMap() { it.refreshState }!!
    val msg = ObservableField("")
    val isNewMessage = MutableLiveData<Boolean>()
    val isRetry = ObservableField(-1)
    var retryCalled = ""

    var timerValue = ObservableField("")

    var preApprovalVisible = ObservableField(false)

    var preApproved = MutableLiveData<PreApproved>()

    private lateinit var disposable: Disposable

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
            0
        )
    }

    var isPreApproved = false

    var preApprovedTime: Long = 0

    var approved = false

    var apprvedTime: Long = 0


    fun notificationRefresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun notificationRetry() {
        repoResult.value?.retry?.invoke()
    }

    fun setInitialData(intent: Intent) {
        try {
            val initData =
                intent.extras?.getParcelable<InboxMsgInitData>("inboxInitData")!! as InboxMsgInitData
            inboxInitData.value = initData
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    fun getInboxMsg() {
        val buildQuery = GetThreadsQuery(
            threadId = getThreadId().toOptional(),
            threadType = "host".toOptional()
        )
        repoResult.value = dataManager.listOfInboxMsg(buildQuery, 10)
    }

    fun getInboxMsg1(page: Int): Single<ApolloResponse<GetThreadsQuery.Data>> {
        isRetry.set(-1)
        val buildQuery = GetThreadsQuery(
            threadId = getThreadId().toOptional(),
            threadType = "host".toOptional(),
            currentPage = page.toOptional()
        )
        return dataManager.listOfInboxMsg1(buildQuery)
    }

    private fun getThreadId(): Int {
        return inboxInitData.value?.threadId ?: 0
    }

    fun readMessage() {
        val mutate = ReadMessageMutation(
            threadId = getThreadId()
        )

        compositeDisposable.add(dataManager.setReadMessage(mutate)
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                {
                    try {
                        it.data?.let { response ->
                            if (response.readMessage?.status == 200) {
                            } else if (response.readMessage?.status == 500) {
                                navigator.openSessionExpire("HostInboxMsgVM")
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

    fun sendMsg(from: String="") {
        //isRetry.set(1)

        var mutate: SendMessageMutation

        if (from.equals("")) {
            if (msg.get()!!.trim().isNotEmpty()) {
                mutate = SendMessageMutation(
                    threadId = inboxInitData.value!!.threadId,
                    content = msg.get()!!.trim().toOptional(),
                    type = "message".toOptional()
                    )

            } else {
                return
            }
        } else {
            mutate = SendMessageMutation(
                threadId = inboxInitData.value!!.threadId,
                startDate = preApproved.value!!.startDate.toOptional(),
                endDate = preApproved.value!!.endDate.toOptional(),
                personCapacity = preApproved.value!!.personCapacity.toOptional(),
                type = "preApproved".toOptional()
            )
        }
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
                                navigator.openSessionExpire("HostInboxMsgVM")
                            }
                            else if(response.sendMessage?.status==400){
                                navigator.showToast(response.sendMessage?.errorMessage.toString())
                            }

                        } ?: navigator.showError()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                },
                {
                    if (from == "") {
                        isRetry.set(1)
                    } else {
                        isRetry.set(6)
                    }
                    handleException(it)
                }
            ))
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
                        navigator.openSessionExpire("HostInboxMsgVM")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { it.printStackTrace() })

        compositeDisposable.add(disposable)
    }

    fun approveReservation(
        threadId: Int,
        content: String,
        type: String,
        startDate: String,
        endDate: String,
        personCapacity: Int,
        visitors: Int,
        pets: Int,
        infants: Int,
        reservationId: Int,
        actionType: String,
        view: View? = null
    ) {
        val mutate = ReservationStatusMutation(
            threadId = threadId,
            type = type.toOptional(),
            startDate = startDate.toOptional(),
            endDate = endDate.toOptional(),
            personCapacity = personCapacity.toOptional(),
            visitors =  visitors.toOptional(),
            pets = pets.toOptional(),
            infants = infants.toOptional(),
            reservationId = reservationId.toOptional(),
            actionType = actionType.toOptional()
        )
        compositeDisposable.add(dataManager.getReseravtionStatus(mutate)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                {
                    try {
                        it.data?.let { response ->
                            if (view != null) {
                                android.os.Handler(Looper.getMainLooper()).postDelayed({
                                    view.isClickable = true
                                }, 3000)
                            }
                            if (response.reservationStatus?.status == 200) {
                                retryCalled = ""
                                if (actionType == "approved") {
                                    isRetry.set(4)
                                    navigator.hideTopView(resourceProvider.getString(R.string.reservation_approved))
                                } else if (actionType == "declined") {
                                    isRetry.set(5)
                                    navigator.hideTopView(resourceProvider.getString(R.string.reservation_declined))
                                }
                            } else if (response.reservationStatus?.status == 500) {
                                navigator.openSessionExpire("HostInboxMsgVM")
                            } else {
                                if(response.reservationStatus?.errorMessage.isNullOrEmpty()){
                                    navigator.showToast(resourceProvider.getString(R.string.list_not_available))
                                }else{
                                    navigator.showToast(response.reservationStatus?.errorMessage.toString())
                                }

                            }
                        } ?: navigator.showError()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                {
                    if (view != null) {
                        android.os.Handler(Looper.getMainLooper()).postDelayed({
                            view.isClickable = true
                        }, 3000)
                    }
                    if (actionType == "approved") {
                        isRetry.set(4)
                    } else if (actionType == "declined") {
                        isRetry.set(5)
                    }
                    handleException(it)
                }
            ))
    }

    fun clearHttp() {
        dataManager.clearHttpCache()
    }
}
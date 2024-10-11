package com.airhomestays.app.ui.inbox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.airhomestays.app.GetAllThreadsQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import javax.inject.Inject

class InboxBoxViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<BaseNavigator>(dataManager, resourceProvider) {

    val inboxResult = MutableLiveData<Listing<GetAllThreadsQuery.Result>>()
    lateinit var inboxList: LiveData<PagedList<GetAllThreadsQuery.Result>>
    val networkState: LiveData<NetworkState> = inboxResult.switchMap() { it.networkState }
    val refreshState: LiveData<NetworkState> = inboxResult.switchMap() { it.refreshState }

    var isRefreshing = false

    fun loadInbox(): LiveData<PagedList<GetAllThreadsQuery.Result>> {
        if (!::inboxList.isInitialized) {
            inboxList = MutableLiveData()
            val buildQuery = GetAllThreadsQuery(
                threadType = "Guest".toOptional()
            )
            inboxResult.value = dataManager.listOfInbox(buildQuery, 10)
            inboxList = inboxResult.switchMap() {
                it.pagedList
            }
        }
        return inboxList
    }

    fun inboxRefresh() {
        inboxResult.value?.refresh?.invoke()
    }

    fun inboxRetry() {
        inboxResult.value?.retry?.invoke()
    }

    fun getInboxList() {
        val buildQuery = GetAllThreadsQuery(
            threadType = "Guest".toOptional()
        )
        inboxResult.value = dataManager.listOfInbox(buildQuery, 10)
    }
}
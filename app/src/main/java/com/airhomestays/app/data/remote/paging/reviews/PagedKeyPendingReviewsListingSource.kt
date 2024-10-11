package com.airhomestays.app.data.remote.paging.reviews

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.airhomestays.app.GetPendingUserReviewsQuery
import com.airhomestays.app.GetUserReviewsQuery
import com.airhomestays.app.data.remote.paging.NetworkState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

class PagedKeyPendingReviewsListingSource(
    private val apolloClient: ApolloClient,
    private val query: GetPendingUserReviewsQuery,
    private val retryExecutor: Executor) : PageKeyedDataSource<String, GetPendingUserReviewsQuery.Result>() {

    private var retry: (() -> Any)? = null

    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, GetPendingUserReviewsQuery.Result>) {}

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, GetPendingUserReviewsQuery.Result>) {

        if (params.key.isNotEmpty()) {
            networkState.postValue(NetworkState.LOADING)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        apolloClient.query(GetPendingUserReviewsQuery(

                            currentPage = Optional.presentIfNotNull(params.key.toInt() + 1)
                        )).execute()
                    }
                    try {
                        if (response.data?.getPendingUserReviews?.status == 200) {
                            val items =
                                ArrayList(response.data?.getPendingUserReviews?.results!!)
                            items.reverse()
                            retry = null
                            if (items.size < 10) {
                                callback.onResult(items, "")
                            } else {
                                callback.onResult(items, (params.key.toInt() + 1).toString())
                            }
                            networkState.postValue(NetworkState.LOADED)
                        } else {
                            retry = null
                            networkState.postValue(NetworkState.LOADED)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }catch(e: Exception){
                    retry = { loadBefore(params, callback) }
                    networkState.postValue(NetworkState.error(e))
                }
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, GetPendingUserReviewsQuery.Result>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apolloClient.query(GetPendingUserReviewsQuery(currentPage = Optional.presentIfNotNull(1) )).execute()
                }
                try {
                    if (response.data?.getPendingUserReviews?.status == 200) {
                        val items =
                            response.data?.getPendingUserReviews?.results?.let { ArrayList(it) }
                        items?.reverse()
                        retry = null
                        networkState.postValue(NetworkState.LOADED)
                        initialLoad.postValue(NetworkState.LOADED)
                        if (items?.size!! < 10) {
                            callback.onResult(items, "", "2")
                        } else {
                            callback.onResult(items, "1", "2")
                        }
                    } else {
                        retry = null
                        networkState.postValue(NetworkState.SUCCESSNODATA)
                        initialLoad.postValue(NetworkState.LOADED)
                    }
                } catch (e: Exception) {
                    val error = NetworkState.error(e)
                    networkState.postValue(error)
                    initialLoad.postValue(error)
                    e.printStackTrace()
                }

            }catch(e: Exception){
                retry = { loadInitial(params, callback) }
                val error = NetworkState.error(e)
                networkState.postValue(error)
                initialLoad.postValue(error)
            }
        }
    }
}
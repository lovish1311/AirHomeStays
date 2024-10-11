package com.airhomestays.app.data.remote.paging.wishlistgroup

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.airhomestays.app.GetAllWishListGroupQuery
import com.airhomestays.app.data.remote.paging.NetworkState
import java.util.concurrent.Executor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.apollographql.apollo3.api.Optional

class PagedKeyWishListGroupSource(
    private val apolloClient: ApolloClient,
    private val builder: GetAllWishListGroupQuery,
    private val retryExecutor: Executor
) : PageKeyedDataSource<String, GetAllWishListGroupQuery.Result>() {

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

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, GetAllWishListGroupQuery.Result>
    ) {
    }

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, GetAllWishListGroupQuery.Result>
    ) {
        if (params.key.isNotEmpty()) {
            networkState.postValue(NetworkState.LOADING)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        apolloClient.query(GetAllWishListGroupQuery( currentPage =Optional.presentIfNotNull(params.key.toInt()) )).execute()
                    }
                    try {
                        if (response.data?.getAllWishListGroup!!.status == 200) {
                            val items = response.data?.getAllWishListGroup!!.results
                            retry = null
                            if (items!!.size < 10) {
                                callback.onResult(items, "")
                            } else {
                                callback.onResult(items, (params.key.toInt() + 1).toString())
                            }
                            networkState.postValue(NetworkState.LOADED)
                        } else if (response.data?.getAllWishListGroup!!.status == 500) {
                            retry = null
                            networkState.postValue(NetworkState.EXPIRED)
                        } else {
                            retry = null
                            networkState.postValue(NetworkState.LOADED)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val error = NetworkState.error(e)
                        networkState.postValue(error)
                    }




                }catch(e: Exception){
                    retry = { loadAfter(params, callback) }
                    val error = NetworkState.error(e)
                    networkState.postValue(error)
                }
            }

        }
    }

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, GetAllWishListGroupQuery.Result>
    ) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apolloClient.query(GetAllWishListGroupQuery(
                        currentPage =Optional.presentIfNotNull(1)
                    )).execute()
                }
                try {
                    if (response.data?.getAllWishListGroup!!.status == 200) {
                        val items = response.data?.getAllWishListGroup!!.results
                        retry = null
                        networkState.postValue(NetworkState.LOADED)
                        initialLoad.postValue(NetworkState.LOADED)
                        if (items!!.size < 10) {
                            callback.onResult(items, "1", "")
                        } else {
                            callback.onResult(items, "1", "2")
                        }
                    } else if (response.data?.getAllWishListGroup!!.status == 500) {
                        retry = null
                        networkState.postValue(NetworkState.EXPIRED)
                    } else {
                        retry = null
                        networkState.postValue(NetworkState.SUCCESSNODATA)
                        initialLoad.postValue(NetworkState.LOADED)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val error = NetworkState.error(e)
                    networkState.postValue(error)
                    initialLoad.postValue(error)
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

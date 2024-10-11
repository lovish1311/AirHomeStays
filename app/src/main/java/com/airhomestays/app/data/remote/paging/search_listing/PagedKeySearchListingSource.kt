package com.airhomestays.app.data.remote.paging.search_listing

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.airhomestays.app.SearchListingQuery
import com.airhomestays.app.data.remote.paging.NetworkState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import com.apollographql.apollo3.api.Optional
class PageKeyedSearchListingSource(
    private val apolloClient: ApolloClient,
    private val query: SearchListingQuery,
    private val retryExecutor: Executor
) : PageKeyedDataSource<String, SearchListingQuery.Result>() {

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
        callback: LoadCallback<String, SearchListingQuery.Result>
    ) {
    }

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, SearchListingQuery.Result>
    ) {
        if (params.key.isNotEmpty()) {
            networkState.postValue(NetworkState.LOADING)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        apolloClient.query(SearchListingQuery(
                            personCapacity = query.personCapacity,
                            dates = query.dates,
                            lat = query.lat,
                            lng = query.lng,
                            priceRange = query.priceRange,
                            address = query.address,
                            currency = query.currency,
                            bookingType = query.bookingType,
                            amenities = query.amenities,
                            beds = query.beds,
                            bedrooms = query.bedrooms,
                            bathrooms = query.bathrooms,
                            roomType = query.roomType,
                            spaces = query.spaces,
                            houseRules = query.houseRules,
                            geoType = query.geoType,
                            geography = query.geography,
                            isOneTotal = query.isOneTotal,
                            currentPage = Optional.presentIfNotNull(params.key.toInt())
                        )).execute()
                    }
                    try {
                            if (response.data?.searchListing?.status == 200) {
                                val items = response.data?.searchListing?.results
                                retry = null
                                if (items!!.size < 10) {
                                    callback.onResult(items, "")
                                } else {
                                    callback.onResult(items, (params.key.toInt() + 1).toString())
                                }
                                networkState.postValue(NetworkState.LOADED)
                            } else if (response.data?.searchListing?.status == 500) {
                                retry = null
                                networkState.postValue(NetworkState.EXPIRED)
                            } else {
                                retry = null
                                networkState.postValue(NetworkState.LOADED)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            retry = { loadAfter(params, callback) }
                            val error = NetworkState.error(e)
                            networkState.postValue(error)
                        }


                } catch (e: Exception) {
                    retry = { loadAfter(params, callback) }
                            val error = NetworkState.error(e)
                            networkState.postValue(error)
                }
            }

        }
    }

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, SearchListingQuery.Result>
    ) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apolloClient.query(SearchListingQuery(
                        personCapacity = query.personCapacity,
                        dates = query.dates,
                        lat = query.lat,
                        lng = query.lng,
                        priceRange = query.priceRange,
                        address = query.address,
                        currency = query.currency,
                        bookingType = query.bookingType,
                        amenities = query.amenities,
                        beds = query.beds,
                        bedrooms = query.bedrooms,
                        bathrooms = query.bathrooms,
                        roomType = query.roomType,
                        spaces = query.spaces,
                        houseRules = query.houseRules,
                        geoType = query.geoType,
                        geography = query.geography,
                        isOneTotal = query.isOneTotal,
                        currentPage = Optional.presentIfNotNull(1))).execute()
                }
                try {
                        if (response.data?.searchListing?.status == 200) {
                            val items = response.data?.searchListing?.results
                            retry = null
                            networkState.postValue(NetworkState.LOADED)
                            initialLoad.postValue(NetworkState.LOADED)
                            if (items!!.size < 10) {
                                callback.onResult(items, "1", "")
                            } else {
                                callback.onResult(items, "1", "2")
                            }
                        } else if (response.data?.searchListing?.status == 500) {
                            retry = null
                            networkState.postValue(NetworkState.EXPIRED)
                        } else {
                            retry = null
                            networkState.postValue(NetworkState.SUCCESSNODATA)
                            initialLoad.postValue(NetworkState.LOADED)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        retry = { loadInitial(params, callback) }
                        val error = NetworkState.error(e)
                        networkState.postValue(error)
                        initialLoad.postValue(error)
                    }



            } catch (e: Exception) {
                retry = { loadInitial(params, callback) }
                val error = NetworkState.error(e)
                networkState.postValue(error)
            }
        }
        }

    }

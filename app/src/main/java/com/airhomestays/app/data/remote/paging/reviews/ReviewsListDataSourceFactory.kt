package com.airhomestays.app.data.remote.paging.reviews

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo3.ApolloClient
import com.airhomestays.app.GetAllReservationQuery
import com.airhomestays.app.GetUserReviewsQuery
import com.airhomestays.app.data.remote.paging.trips.PageKeyedTripsListingSource
import java.util.concurrent.Executor

class ReviewsListDataSourceFactory(
    private val apolloClient: ApolloClient,
    private val query: GetUserReviewsQuery,
    private val executor: Executor) : DataSource.Factory<String, GetUserReviewsQuery.Result>() {
    val sourceLiveData = MutableLiveData<PagedKeyReviewsListingSource>()
    override fun create(): DataSource<String, GetUserReviewsQuery.Result> {
        val source = PagedKeyReviewsListingSource(apolloClient, query, executor)
        sourceLiveData.postValue(source)
        return source
    }
}
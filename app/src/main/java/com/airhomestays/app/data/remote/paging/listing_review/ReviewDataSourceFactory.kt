package com.airhomestays.app.data.remote.paging.listing_review

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo3.ApolloClient
import com.airhomestays.app.GetPropertyReviewsQuery
import com.airhomestays.app.GetReviewsListQuery
import java.util.concurrent.Executor

class ReviewDataSourceFactory(
    private val apolloClient: ApolloClient,
    private val listId: Int,
    private val hostId: String,
    private val executor: Executor
) : DataSource.Factory<String, GetPropertyReviewsQuery.Result>() {
    val sourceLiveData = MutableLiveData<PageKeyedReviewDataSource>()
    override fun create(): DataSource<String, GetPropertyReviewsQuery.Result> {
        val source = PageKeyedReviewDataSource(apolloClient, listId, hostId, executor)
        sourceLiveData.postValue(source)
        return source
    }
}
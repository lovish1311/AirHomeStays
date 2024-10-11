package com.airhomestays.app.data.remote.paging.inbox_msg

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.apollographql.apollo3.ApolloClient
import com.airhomestays.app.GetThreadsQuery
import java.util.concurrent.Executor

class InboxMsgDataSourceFactory(
    private val apolloClient: ApolloClient,
    private val query: GetThreadsQuery,
    private val executor: Executor
) : DataSource.Factory<String, GetThreadsQuery.ThreadItem>() {
    val sourceLiveData = MutableLiveData<PageKeyedInboxMsgSource>()
    override fun create(): DataSource<String, GetThreadsQuery.ThreadItem> {
        val source = PageKeyedInboxMsgSource(apolloClient, query, executor)
        sourceLiveData.postValue(source)
        return source
    }
}
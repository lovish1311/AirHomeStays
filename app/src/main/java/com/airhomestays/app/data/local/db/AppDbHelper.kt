package com.airhomestays.app.data.local.db

import androidx.paging.DataSource
import com.airhomestays.app.data.model.db.DefaultListing
import com.airhomestays.app.data.model.db.Message
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppDbHelper @Inject constructor(
        private val mAppDatabase: AppDatabase
): DbHelper {


    override fun insertDefaultListing(defaultListing: DefaultListing): Observable<Boolean> {
        mAppDatabase.defaultListingDao().insert(defaultListing)
        return Observable.fromCallable { true }
    }

    override fun deleteMessage(): Observable<Boolean> {
        mAppDatabase.InboxMessage().deleteAllMessage()

        return Observable.fromCallable {
            true
        }
    }

    override fun loadAllMessage(): DataSource.Factory<Int, Message> {
        return mAppDatabase.InboxMessage().loadAll()
    }

}
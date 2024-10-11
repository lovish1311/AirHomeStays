package com.airhomestays.app.data.local.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.airhomestays.app.data.model.db.Message

@Dao
interface InboxMsgDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(question: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(questions: List<Message>)

    @Query("SELECT * FROM message")
    fun loadAll(): DataSource.Factory<Int, Message>

    @Query("DELETE FROM Message")
    fun deleteAllMessage()

}
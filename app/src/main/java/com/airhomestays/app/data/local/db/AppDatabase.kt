package com.airhomestays.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.airhomestays.app.data.local.db.dao.DefaultListingDao
import com.airhomestays.app.data.local.db.dao.InboxMsgDao
import com.airhomestays.app.data.model.db.DefaultListing
import com.airhomestays.app.data.model.db.Message


@Database(entities = [(Message::class), (DefaultListing::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun defaultListingDao(): DefaultListingDao

    abstract fun InboxMessage(): InboxMsgDao


}

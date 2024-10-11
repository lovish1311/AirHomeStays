package com.airhomestays.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.airhomestays.app.data.model.db.DefaultListing

@Dao
interface DefaultListingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(defaultListing: DefaultListing)
}
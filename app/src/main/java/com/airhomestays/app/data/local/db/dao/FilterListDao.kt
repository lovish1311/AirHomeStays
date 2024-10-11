package com.airhomestays.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.airhomestays.app.data.model.db.FilterSubList


@Dao
interface FilterListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(filterSubList: FilterSubList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(filterSubList: List<FilterSubList>)

    @Query("SELECT * FROM filterSubList")
    fun loadAll(): List<FilterSubList>

    @Query("SELECT * FROM filterSubList WHERE typeId = :filterId")
    fun loadAllByFilterId(filterId: Int): List<FilterSubList>
}
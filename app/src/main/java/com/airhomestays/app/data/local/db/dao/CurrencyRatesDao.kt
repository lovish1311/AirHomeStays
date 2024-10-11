package com.airhomestays.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.airhomestays.app.data.model.db.CurrencyRates


@Dao
interface CurrencyRatesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currencyRates: CurrencyRates)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(currencyRatesList: List<CurrencyRates>)

    @Query("SELECT * FROM currencyRates")
    fun loadAll(): List<CurrencyRates>

    @Query("SELECT * FROM currencyRates WHERE currencyCode = :currencyCode LIMIT 1")
    fun loadCurrencyRatesByCode(currencyCode: String): CurrencyRates
}
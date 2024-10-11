package com.airhomestays.app.data.model.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "filterSubList",
        foreignKeys = [ForeignKey(
                entity = (Filter::class),
                parentColumns = ["id"],
                childColumns = ["typeId"]
        )]
)
data class FilterSubList(
        @PrimaryKey
        val id: Int,
        val itemName: String,
        val typeId: Int,
        val otherItemName: String?,
        val startValue : Int?,
        val endValue : Int?,
        val maximum : Int?,
        val minimum: Int?
)
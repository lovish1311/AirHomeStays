package com.airhomestays.app.data.model.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "WishList",
        foreignKeys = [ForeignKey(
                entity = (WishListGroup::class),
                parentColumns = ["id"],
                childColumns = ["typeId"]
        )]
)
data class WishList(
        @PrimaryKey
        val id: Int

)
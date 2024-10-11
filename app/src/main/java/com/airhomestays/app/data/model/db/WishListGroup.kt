package com.airhomestays.app.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "WishListGroup")
data class WishListGroup(
        @PrimaryKey
        val id: Int,
        val title: String,
        val img: String?,
        val wishListCount: Int? = 0,
        var isWishList: Boolean
)
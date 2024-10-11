package com.airhomestays.app.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "defaultListing")
data class DefaultListing(
        @PrimaryKey
        val id: Int,
        val type: String,
        val beds: Int,
        val bookingType: String,
        val coverPhoto: Int,
        val listPhotoName: String,
        val personCapacity: Int,
        val reviewsCount: Int,
        val reviewsStarRating: Int,
        val roomType: String,
        val title: String,
        val basePrice: Int,
        val currency: String
)
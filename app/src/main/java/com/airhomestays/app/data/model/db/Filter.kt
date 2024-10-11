package com.airhomestays.app.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter")
data class Filter(
        @PrimaryKey
        val id: Int,
        val typeLabel: String,
        val typeName: String
)

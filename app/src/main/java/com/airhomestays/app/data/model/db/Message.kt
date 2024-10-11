package com.airhomestays.app.data.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class Message(
        @PrimaryKey
        val id: Int,
        val typeLabel: String,
        val typeName: String,
        val threadId: Int?,
        val reservationId: Int?,
        val content: String?,
        val sentBy: String?,
        val type: String?,
        val startDate: String?,
        val endDate: String?,
        val createdAt: String?,
        var page: Int
)

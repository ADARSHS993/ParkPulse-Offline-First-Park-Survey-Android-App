package com.example.parkplus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    indices = [Index(value = ["surveyId"])]
)
data class PhotoEntity(

    @PrimaryKey
    val id: String,

    val surveyId: String,

    val localPath: String,

    val latitude: Double,

    val longitude: Double,

    val accuracyMeters: Float,

    val capturedAt: Long,

    val isPendingSync: Boolean = true
)

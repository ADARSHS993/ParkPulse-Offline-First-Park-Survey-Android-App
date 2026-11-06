package com.example.parkplus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "surveys",
    indices = [Index(value = ["parkId"])]
)
data class SurveyEntity(
    @PrimaryKey
    val id: String,

    val parkId: String,

    val checkedInAt: Long,

    val latitude: Double,

    val longitude: Double,

    val gpsAccuracyMeters: Float,

    val treesCounted: Int,

    val lightsWorking: Boolean,

    val cleanliness: String,

    val notes: String,

    val isPendingSync: Boolean = true,

    val updatedAt: Long = System.currentTimeMillis()
)

package com.example.parkplus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parks")
data class ParkEntity(

    @PrimaryKey
    val id: String,
    val name: String,
    val type: String,
    val areaSquareMeters: Double,
    val status: String,
    val lastSurveyDate: Long?,
    // Coordinates stored as encoded text.
    val boundary: String,
    val isPendingSync: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

package com.example.parkplus.domain.model

import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.ParkType

data class Park(
    val id: String,
    val name: String,
    val type: ParkType,
    val areaSquareMeters: Double,
    val status: ParkStatus,
    val lastSurveyDate: Long? = null,
    val boundary: List<GeoPoint> = emptyList(),
    val isPendingSync: Boolean = false
)

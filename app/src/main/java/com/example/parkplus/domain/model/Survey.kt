package com.example.parkplus.domain.model

import com.example.parkplus.domain.Cleanliness

data class Survey(
    val id: String,
    val parkId: String,
    val checkedInAt: Long,
    val checkInLocation: GeoPoint,
    val gpsAccuracyMeters: Float,
    val treesCounted: Int,
    val lightsWorking: Boolean,
    val cleanliness: Cleanliness,
    val notes: String,
    val photoIds: List<String> = emptyList(),
    val isPendingSync: Boolean = true
)

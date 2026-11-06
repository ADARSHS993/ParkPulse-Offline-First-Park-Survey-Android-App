package com.example.parkplus.domain.model

data class PhotoRecord(
    val id: String,
    val surveyId: String,
    val localPath: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val capturedAt: Long,
    val isPendingSync: Boolean = true
)

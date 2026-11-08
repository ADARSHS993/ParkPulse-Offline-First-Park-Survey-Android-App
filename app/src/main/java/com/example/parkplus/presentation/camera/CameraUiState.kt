package com.example.parkplus.presentation.camera

import com.example.parkplus.domain.model.PhotoRecord

data class CameraUiState(
    val parkName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val photos: List<PhotoRecord> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)
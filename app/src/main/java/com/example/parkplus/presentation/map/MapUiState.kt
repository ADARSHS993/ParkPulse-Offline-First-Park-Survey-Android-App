package com.example.parkplus.presentation.map

import com.example.parkplus.domain.model.Park

data class MapUiState(
    val parks: List<Park> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

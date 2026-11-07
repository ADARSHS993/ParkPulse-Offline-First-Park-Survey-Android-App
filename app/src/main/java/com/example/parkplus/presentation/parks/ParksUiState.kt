package com.example.parkplus.presentation.parks

import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.model.Park

data class ParksUiState(
    val parks: List<Park> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: ParkStatus? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

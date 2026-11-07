
package com.example.parkplus.presentation.addpark

import com.example.parkplus.domain.ParkType
import com.example.parkplus.domain.model.GeoPoint

data class AddParkUiState(
    val name: String = "",
    val selectedType: ParkType = ParkType.GARDEN,
    val boundaryPoints: List<GeoPoint> = emptyList(),
    val areaSquareMeters: Double = 0.0,
    val areaAcres: Double = 0.0,
    val perimeterMeters: Double = 0.0,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean
        get() = name.isNotBlank() &&
                boundaryPoints.size >= 3 &&
                !isSaving
}

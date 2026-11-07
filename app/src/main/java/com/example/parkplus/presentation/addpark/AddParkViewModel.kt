
package com.example.parkplus.presentation.addpark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.model.GeoPoint
import com.example.parkplus.domain.model.Park
import com.example.parkplus.domain.usecase.park.AddParkUseCase
import com.example.parkplus.domain.usecase.park.CalculateParkAreaUseCase
import com.example.parkplus.domain.usecase.park.CalculatePerimeterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddParkViewModel @Inject constructor(
    private val calculateParkAreaUseCase: CalculateParkAreaUseCase,
    private val calculatePerimeterUseCase: CalculatePerimeterUseCase,
    private val addParkUseCase: AddParkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddParkUiState())
    val uiState: StateFlow<AddParkUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(name = name, error = null)
        }
    }

    fun onTypeChange(type: com.example.parkplus.domain.ParkType) {
        _uiState.update {
            it.copy(selectedType = type, error = null)
        }
    }

    fun addBoundaryPoint(point: GeoPoint) {
        updateBoundary(
            _uiState.value.boundaryPoints + point
        )
    }

    fun updateBoundaryPoint(
        index: Int,
        point: GeoPoint
    ) {
        val points = _uiState.value.boundaryPoints.toMutableList()

        if (index !in points.indices) return

        points[index] = point
        updateBoundary(points)
    }

    fun removeLastPoint() {
        val points = _uiState.value.boundaryPoints.dropLast(1)
        updateBoundary(points)
    }

    fun removeBoundaryPoint(index: Int) {
        val points = _uiState.value.boundaryPoints.toMutableList()

        if (index !in points.indices) return

        points.removeAt(index)
        updateBoundary(points)
    }

    fun clearBoundary() {
        updateBoundary(emptyList())
    }

    private fun updateBoundary(points: List<GeoPoint>) {

        val area = calculateParkAreaUseCase(points)
        val perimeter = calculatePerimeterUseCase(points)

        _uiState.update {
            it.copy(
                boundaryPoints = points,
                areaSquareMeters = area,
                areaAcres = area / 4046.8564224,
                perimeterMeters = perimeter,
                error = null
            )
        }
    }

    fun savePark() {

        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update {
                it.copy(error = "Please enter park name")
            }
            return
        }

        if (currentState.boundaryPoints.size < 3) {
            _uiState.update {
                it.copy(error = "Add at least 3 boundary points")
            }
            return
        }

        if (currentState.isSaving) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(isSaving = true, error = null)
            }

            try {
                val park = Park(
                    id = UUID.randomUUID().toString(),
                    name = currentState.name.trim(),
                    type = currentState.selectedType,
                    areaSquareMeters = currentState.areaSquareMeters,
                    status = ParkStatus.OPEN,
                    lastSurveyDate = null,
                    boundary = currentState.boundaryPoints,
                    isPendingSync = true
                )

                addParkUseCase(park)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }

            } catch (exception: Exception) {

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = exception.message
                            ?: "Unable to save park"
                    )
                }
            }
        }
    }

    fun resetSavedState() {
        _uiState.update {
            AddParkUiState()
        }
    }
}

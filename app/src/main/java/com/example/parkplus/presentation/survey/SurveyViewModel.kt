
package com.example.parkplus.presentation.survey

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkplus.domain.model.GeoPoint
import com.example.parkplus.domain.usecase.park.GetParkByIdUseCase
import com.example.parkplus.domain.usecase.survey.StartSurveyUseCase
import com.example.parkplus.domain.usecase.survey.ValidateCheckInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SurveyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getParkByIdUseCase: GetParkByIdUseCase,
    private val validateCheckInUseCase: ValidateCheckInUseCase,
    private val startSurveyUseCase: StartSurveyUseCase
) : ViewModel() {

    private val parkId: String =
        checkNotNull(savedStateHandle["parkId"]) {
            "Park ID is missing from navigation"
        }

    private val _uiState = MutableStateFlow(SurveyUiState())
    val uiState: StateFlow<SurveyUiState> = _uiState.asStateFlow()

    init {
        loadPark()
    }

    private fun loadPark() {
        viewModelScope.launch {
            try {
                val park = getParkByIdUseCase(parkId)

                if (park == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Park not found"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            park = park,
                            isLoading = false,
                            checkInMessage = "Waiting for GPS location..."
                        )
                    }

                    // Revalidate if a location arrived before the park loaded.
                    itLocationRevalidation()
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unable to load park"
                    )
                }
            }
        }
    }

    private fun itLocationRevalidation() {
        val location = _uiState.value.currentLocation ?: return
        updateLocation(location)
    }

    fun updateLocation(location: Location) {
        val park = _uiState.value.park

        _uiState.update { current ->
            current.copy(
                currentLocation = location,
                gpsAccuracyMeters = location.accuracy,
                checkInMessage = if (park == null) {
                    "Loading park information..."
                } else {
                    current.checkInMessage
                }
            )
        }

        if (park == null) return

        val validation = validateCheckInUseCase(
            park = park,
            userLocation = GeoPoint(
                latitude = location.latitude,
                longitude = location.longitude
            ),
            accuracyMeters = location.accuracy
        )

        _uiState.update {
            it.copy(
                isInsidePark = validation.isInsidePark,
                hasAccurateGps = validation.hasAccurateGps,
                canStartSurvey = validation.canStart,
                checkInMessage = validation.message
            )
        }
    }

    fun startSurvey() {
        val current = _uiState.value

        val park = current.park ?: return
        val location = current.currentLocation ?: return

        if (!current.canStartSurvey || current.isStartingSurvey) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isStartingSurvey = true,
                    error = null
                )
            }

            try {
                val result = startSurveyUseCase(
                    park = park,
                    userLocation = GeoPoint(
                        latitude = location.latitude,
                        longitude = location.longitude
                    ),
                    accuracyMeters = location.accuracy
                )

                _uiState.update {
                    it.copy(
                        isStartingSurvey = false,
                        canStartSurvey = result.validation.canStart,
                        checkInMessage = result.validation.message,
                        surveyId = result.survey?.id
                    )
                }

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isStartingSurvey = false,
                        error = exception.message
                            ?: "Unable to start survey"
                    )
                }
            }
        }
    }
}

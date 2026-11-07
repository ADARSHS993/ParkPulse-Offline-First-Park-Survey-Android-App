
package com.example.parkplus.presentation.survey

import android.location.Location
import com.example.parkplus.domain.model.Park

data class SurveyUiState(
    val park: Park? = null,
    val currentLocation: Location? = null,
    val gpsAccuracyMeters: Float? = null,
    val checkInMessage: String = "Waiting for GPS location...",
    val isInsidePark: Boolean = false,
    val hasAccurateGps: Boolean = false,
    val canStartSurvey: Boolean = false,
    val isLoading: Boolean = true,
    val isStartingSurvey: Boolean = false,
    val surveyId: String? = null,
    val error: String? = null
)

package com.example.parkplus.presentation.survey

import com.example.parkplus.domain.Cleanliness

data class SurveyDetailsUiState(
    val treesCounted: String = "0",
    val lightsWorking: Boolean = true,
    val cleanliness: Cleanliness = Cleanliness.GOOD,
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
package com.example.parkplus.presentation.survey

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkplus.domain.Cleanliness
import com.example.parkplus.domain.repository.SurveyRepository
import com.example.parkplus.domain.usecase.survey.UpdateSurveyDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SurveyRepository,
    private val updateSurveyDetailsUseCase: UpdateSurveyDetailsUseCase
) : ViewModel() {

    private val surveyId: String =
        checkNotNull(savedStateHandle["surveyId"])

    private val _uiState = MutableStateFlow(SurveyDetailsUiState())
    val uiState: StateFlow<SurveyDetailsUiState> = _uiState.asStateFlow()

    init {
        loadSurvey()
    }

    private fun loadSurvey() {
        viewModelScope.launch {
            try {
                val survey = repository.getSurveyById(surveyId)

                if (survey == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Survey record not found."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            treesCounted = survey.treesCounted.toString(),
                            lightsWorking = survey.lightsWorking,
                            cleanliness = survey.cleanliness,
                            notes = survey.notes,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unable to load survey."
                    )
                }
            }
        }
    }

    fun updateTreesCount(value: String) {
        if (value.isEmpty() || value.toIntOrNull() != null) {
            _uiState.update { it.copy(treesCounted = value) }
        }
    }

    fun updateLightsWorking(value: Boolean) {
        _uiState.update { it.copy(lightsWorking = value) }
    }

    fun updateCleanliness(value: Cleanliness) {
        _uiState.update { it.copy(cleanliness = value) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun saveSurvey() {
        val state = _uiState.value
        val trees = state.treesCounted.toIntOrNull()

        if (trees == null || trees < 0) {
            _uiState.update {
                it.copy(error = "Enter a valid number of trees.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                updateSurveyDetailsUseCase(
                    surveyId = surveyId,
                    treesCounted = trees,
                    lightsWorking = state.lightsWorking,
                    cleanliness = state.cleanliness,
                    notes = state.notes.trim()
                )

                _uiState.update {
                    it.copy(isSaving = false, isSaved = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Unable to save survey."
                    )
                }
            }
        }
    }
}
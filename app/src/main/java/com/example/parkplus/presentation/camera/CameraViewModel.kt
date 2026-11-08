package com.example.parkplus.presentation.camera

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkplus.domain.repository.PhotoRepository
import com.example.parkplus.domain.usecase.park.GetParkByIdUseCase
import com.example.parkplus.domain.usecase.survey.GetSurveyByIdUseCase
import com.example.parkplus.domain.usecase.survey.SavePhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSurveyByIdUseCase: GetSurveyByIdUseCase,
    private val getParkByIdUseCase: GetParkByIdUseCase,
    private val savePhotoUseCase: SavePhotoUseCase,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    val surveyId: String = checkNotNull(savedStateHandle["surveyId"])

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        loadSurvey()
        observePhotos()
    }

    private fun loadSurvey() {
        viewModelScope.launch {
            try {
                val survey = getSurveyByIdUseCase(surveyId)
                    ?: throw IllegalStateException("Survey not found")

                val park = getParkByIdUseCase(survey.parkId)
                    ?: throw IllegalStateException("Park not found")

                _uiState.update {
                    it.copy(
                        parkName = park.name,
                        latitude = survey.checkInLocation.latitude,
                        longitude = survey.checkInLocation.longitude,
                        accuracy = survey.gpsAccuracyMeters,
                        isLoading = false
                    )
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

    private fun observePhotos() {
        viewModelScope.launch {
            photoRepository.getPhotosBySurvey(surveyId).collect { photos ->
                _uiState.update { it.copy(photos = photos) }
            }
        }
    }

    fun savePhoto(path: String) {
        val state = _uiState.value

        if (state.isLoading || state.parkName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                savePhotoUseCase(
                    surveyId = surveyId,
                    localPath = path,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    accuracyMeters = state.accuracy
                )

                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Unable to save photo."
                    )
                }
            }
        }
    }
}
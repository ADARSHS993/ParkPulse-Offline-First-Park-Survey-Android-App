package com.example.parkplus.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkplus.domain.usecase.park.GetAllParksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class MapViewModel @Inject constructor(
    getAllParksUseCase: GetAllParksUseCase
) : ViewModel() {

    val uiState: StateFlow<MapUiState> =
        getAllParksUseCase()
            .map { parks ->
                MapUiState(
                    parks = parks,
                    isLoading = false
                )
            }
            .catch { exception ->
                emit(
                    MapUiState(
                        isLoading = false,
                        error = exception.message ?: "Unable to load map"
                    )
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                MapUiState()
            )
}
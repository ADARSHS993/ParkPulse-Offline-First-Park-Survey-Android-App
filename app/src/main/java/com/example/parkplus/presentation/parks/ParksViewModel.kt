package com.example.parkplus.presentation.parks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.usecase.park.GetAllParksUseCase
import com.example.parkplus.domain.usecase.park.SearchParksUseCase
import com.example.parkplus.domain.usecase.park.GetParksByStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ParksViewModel @Inject constructor(
    private val getAllParksUseCase: GetAllParksUseCase,
    private val searchParksUseCase: SearchParksUseCase,
    private val getParksByStatusUseCase: GetParksByStatusUseCase
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedStatus = MutableStateFlow<ParkStatus?>(null)

    val uiState: StateFlow<ParksUiState> =
        combine(searchQuery, selectedStatus) { query, status ->
            query to status
        }.flatMapLatest { (query, status) ->
            when {
                query.isNotBlank() -> searchParksUseCase(query)
                else -> getParksByStatusUseCase(status)
            }
        }.map { parks ->
            ParksUiState(
                parks = parks,
                searchQuery = searchQuery.value,
                selectedStatus = selectedStatus.value,
                isLoading = false
            )
        }.catch { exception ->
            emit(
                ParksUiState(
                    isLoading = false,
                    error = exception.message ?: "Unable to load parks"
                )
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ParksUiState()
        )

    fun onSearchChange(value: String) {
        searchQuery.value = value
    }

    fun onStatusChange(value: ParkStatus?) {
        selectedStatus.value = value
    }
}
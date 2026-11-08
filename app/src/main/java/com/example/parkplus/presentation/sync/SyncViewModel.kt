
package com.example.parkplus.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkplus.domain.usecase.sync.SyncNowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SyncUiState(
    val isSyncing: Boolean = false,
    val message: String? = null,
    val parksSynced: Int = 0,
    val surveysSynced: Int = 0,
    val photosSynced: Int = 0,
    val failed: Int = 0
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncNowUseCase: SyncNowUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState = _uiState.asStateFlow()

    fun syncNow() {
        if (_uiState.value.isSyncing) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSyncing = true,
                    message = "Synchronizing your data..."
                )
            }

            try {
                val result = syncNowUseCase()

                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        message = result.message,
                        parksSynced = result.parksSynced,
                        surveysSynced = result.surveysSynced,
                        photosSynced = result.photosSynced,
                        failed = result.failed
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        message = exception.message
                            ?: "Synchronization failed."
                    )
                }
            }
        }
    }
}

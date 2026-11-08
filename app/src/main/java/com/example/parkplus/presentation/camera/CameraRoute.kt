package com.example.parkplus.presentation.camera

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.parkplus.presentation.survey.CameraCaptureScreen

@Composable
fun CameraRoute(
    onBack: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> CircularProgressIndicator()

            state.error != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error ?: "Unable to load camera.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    CameraCaptureScreen(
                        surveyId = viewModel.surveyId,
                        parkName = state.parkName,
                        latitude = state.latitude,
                        longitude = state.longitude,
                        accuracy = state.accuracy,
                        onPhotoSaved = viewModel::savePhoto,
                        onBack = onBack
                    )

                    state.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Text(
                        text = "Photos saved: ${state.photos.size}",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
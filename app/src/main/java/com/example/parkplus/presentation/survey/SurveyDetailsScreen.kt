package com.example.parkplus.presentation.survey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.parkplus.domain.Cleanliness

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SurveyDetailsScreen(
    onSurveySaved: () -> Unit,
    viewModel: SurveyDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSurveySaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Park Survey Details") }
            )
        }
    ) { padding ->

        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "Inspection Information",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "Record the current condition of the park.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = state.treesCounted,
                        onValueChange = viewModel::updateTreesCount,
                        label = { Text("Number of trees counted") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Divider()

                    Text(
                        text = "Are the park lights working?",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.lightsWorking,
                            onClick = {
                                viewModel.updateLightsWorking(true)
                            }
                        )
                        Text("Yes")

                        Spacer(modifier = Modifier.padding(horizontal = 12.dp))

                        RadioButton(
                            selected = !state.lightsWorking,
                            onClick = {
                                viewModel.updateLightsWorking(false)
                            }
                        )
                        Text("No")
                    }

                    Divider()

                    Text(
                        text = "Park cleanliness",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Cleanliness.values().forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.cleanliness == option,
                                onClick = {
                                    viewModel.updateCleanliness(option)
                                }
                            )
                            Text(option.name.lowercase().replaceFirstChar {
                                it.uppercase()
                            })
                        }
                    }

                    Divider()

                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = viewModel::updateNotes,
                        label = { Text("Additional notes") },
                        placeholder = {
                            Text("Describe any issues found...")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )

                    state.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = viewModel::saveSurvey,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Survey Details")
                        }
                    }
                }
            }
        }
    }
}
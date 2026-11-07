package com.example.parkplus.presentation.parks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.model.Park

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParksScreen(
    onOpenMap: (String) -> Unit,
    viewModel: ParksViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("ParkPulse") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search parks") },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.selectedStatus == null,
                    onClick = { viewModel.onStatusChange(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = state.selectedStatus == ParkStatus.OPEN,
                    onClick = {
                        viewModel.onStatusChange(ParkStatus.OPEN)
                    },
                    label = { Text("Open") }
                )
                FilterChip(
                    selected = state.selectedStatus == ParkStatus.NEEDS_REPAIR,
                    onClick = {
                        viewModel.onStatusChange(ParkStatus.NEEDS_REPAIR)
                    },
                    label = { Text("Repair") }
                )
                FilterChip(
                    selected = state.selectedStatus == ParkStatus.CLOSED,
                    onClick = {
                        viewModel.onStatusChange(ParkStatus.CLOSED)
                    },
                    label = { Text("Closed") }
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                state.error != null -> {
                    Text(state.error ?: "Something went wrong")
                }

                state.parks.isEmpty() -> {
                    Text("No parks found")
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.parks,
                            key = { it.id }
                        ) { park ->
                            ParkCard(
                                park = park,
                                onClick = { onOpenMap(park.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParkCard(
    park: Park,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = park.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text("Type: ${park.type.name.replace('_', ' ')}")

            Text(
                "Area: ${
                    "%.2f".format(
                        park.areaSquareMeters / 4046.856
                    )
                } acres"
            )

            Text("Status: ${park.status.name.replace('_', ' ')}")

            park.lastSurveyDate?.let {
                Text("Last survey: ${java.text.DateFormat.getDateInstance().format(it)}")
            } ?: Text("Last survey: Not surveyed")

            if (park.isPendingSync) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Pending sync",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "View on map →",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
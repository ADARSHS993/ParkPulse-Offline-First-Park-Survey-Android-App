
package com.example.parkplus.presentation.parks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.model.Park
import com.example.parkplus.presentation.sync.SyncViewModel
import java.text.DateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParksScreen(
    onOpenMap: (String) -> Unit,
    onAddPark: () -> Unit,
    viewModel: ParksViewModel = hiltViewModel(),
    syncViewModel: SyncViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val syncState by syncViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPark) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Park"
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("ParkPulse") }
            )
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Offline Sync",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = syncState.message ?: "Ready to sync",
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (
                            syncState.parksSynced > 0 ||
                            syncState.surveysSynced > 0 ||
                            syncState.photosSynced > 0
                        ) {
                            Text(
                                text = "Parks: ${syncState.parksSynced} | " +
                                        "Surveys: ${syncState.surveysSynced} | " +
                                        "Photos: ${syncState.photosSynced}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (syncState.failed > 0) {
                            Text(
                                text = "Failed: ${syncState.failed}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Button(
                        onClick = syncViewModel::syncNow,
                        enabled = !syncState.isSyncing
                    ) {
                        if (syncState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sync Now")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Text(
                        text = state.error ?: "Something went wrong",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.parks.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No parks found")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = park.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Type: ${park.type.name.replace('_', ' ')}"
            )

            Text(
                text = "Area: ${
                    "%.2f".format(
                        park.areaSquareMeters / 4046.856
                    )
                } acres"
            )

            Text(
                text = "Status: ${park.status.name.replace('_', ' ')}"
            )

            park.lastSurveyDate?.let {
                Text(
                    text = "Last survey: ${
                        DateFormat.getDateInstance().format(it)
                    }"
                )
            } ?: Text("Last survey: Not surveyed")

            if (park.isPendingSync) {
                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Pending sync",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
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

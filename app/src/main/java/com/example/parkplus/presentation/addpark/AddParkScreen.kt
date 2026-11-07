
package com.example.parkplus.presentation.addpark

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.parkplus.domain.model.GeoPoint
import com.example.parkplus.domain.ParkType
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParkScreen(
    onParkSaved: () -> Unit,
    viewModel: AddParkViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var typeMenuExpanded by remember {
        mutableStateOf(false)
    }

    val mapView = remember {
        Configuration.getInstance().userAgentValue =
            context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(16.0)

            // Initial map location: Surat, Gujarat.
            controller.setCenter(
                OsmGeoPoint(21.1702, 72.8311)
            )
        }
    }

    // Handle map taps and redraw draggable boundary markers.
    LaunchedEffect(mapView, state.boundaryPoints) {

        mapView.overlays.clear()

        // Add polygon when at least three points exist.
        if (state.boundaryPoints.size >= 3) {
            val polygon = org.osmdroid.views.overlay.Polygon(mapView).apply {
                points = state.boundaryPoints.map {
                    OsmGeoPoint(it.latitude, it.longitude)
                }

                fillPaint.color =
                    android.graphics.Color.argb(65, 33, 150, 243)

                outlinePaint.color =
                    android.graphics.Color.BLUE

                outlinePaint.strokeWidth = 4f
            }

            mapView.overlays.add(polygon)
        }

        // Add draggable markers for every corner.
        state.boundaryPoints.forEachIndexed { index, point ->

            val marker = Marker(mapView).apply {
                position = OsmGeoPoint(
                    point.latitude,
                    point.longitude
                )

                title = "Corner ${index + 1}"
                setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
                )

                isDraggable = true

                setOnMarkerDragListener(
                    object : Marker.OnMarkerDragListener {

                        override fun onMarkerDrag(marker: Marker) {
                            // Update the corner after the drag ends.
                        }

                        override fun onMarkerDragEnd(marker: Marker) {
                            viewModel.updateBoundaryPoint(
                                index = index,
                                point = GeoPoint(
                                    latitude = marker.position.latitude,
                                    longitude = marker.position.longitude
                                )
                            )
                        }

                        override fun onMarkerDragStart(marker: Marker) {
                            // Nothing needed here.
                        }
                    }
                )
            }

            mapView.overlays.add(marker)
        }

        // Listen for taps to add new corners.
        val tapOverlay = MapEventsOverlay(
            object : MapEventsReceiver {

                override fun singleTapConfirmedHelper(
                    point: OsmGeoPoint
                ): Boolean {

                    viewModel.addBoundaryPoint(
                        GeoPoint(
                            latitude = point.latitude,
                            longitude = point.longitude
                        )
                    )

                    return true
                }

                override fun longPressHelper(
                    point: OsmGeoPoint
                ): Boolean = false
            }
        )

        // Add tap overlay last so it receives map taps.
        mapView.overlays.add(tapOverlay)

        mapView.invalidate()
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // Navigate after successful saving.
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.resetSavedState()
            onParkSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add New Park")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = {
                    Text("Park Name")
                },
                placeholder = {
                    Text("Enter park name")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Park Type",
                style = MaterialTheme.typography.titleSmall
            )

            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = {
                    typeMenuExpanded = !typeMenuExpanded
                }
            ) {

                OutlinedTextField(
                    value = state.selectedType.name
                        .replace('_', ' '),
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text("Select park type")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = typeMenuExpanded
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            MenuAnchorType.PrimaryNotEditable
                        )
                )

                ExposedDropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = {
                        typeMenuExpanded = false
                    }
                ) {

                    ParkType.entries.forEach { type ->

                        DropdownMenuItem(
                            text = {
                                Text(
                                    type.name.replace('_', ' ')
                                )
                            },
                            onClick = {
                                viewModel.onTypeChange(type)
                                typeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Draw Park Boundary",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Tap at least 3 corners on the map. " +
                        "Drag the markers to adjust the boundary.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(8.dp))

            AndroidView(
                factory = { mapView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Boundary Points: ${state.boundaryPoints.size}"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = viewModel::removeLastPoint,
                    enabled = state.boundaryPoints.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Undo")
                }

                OutlinedButton(
                    onClick = viewModel::clearBoundary,
                    enabled = state.boundaryPoints.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Park Measurements",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Area: %.2f m²".format(
                            state.areaSquareMeters
                        )
                    )

                    Text(
                        "Area: %.4f acres".format(
                            state.areaAcres
                        )
                    )

                    Text(
                        "Perimeter: %.2f m".format(
                            state.perimeterMeters
                        )
                    )
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = viewModel::savePark,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth()
            ) {

                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Park")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

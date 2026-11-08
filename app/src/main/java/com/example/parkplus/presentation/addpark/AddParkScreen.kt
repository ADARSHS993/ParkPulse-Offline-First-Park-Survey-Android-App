
package com.example.parkplus.presentation.addpark

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.parkplus.domain.ParkType
import com.example.parkplus.domain.model.GeoPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

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

    var showLocationExplanation by remember {
        mutableStateOf(false)
    }

    var showSettingsMessage by remember {
        mutableStateOf(false)
    }

    var shouldCenterAfterPermission by remember {
        mutableStateOf(false)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val mapView = remember {
        Configuration.getInstance().userAgentValue =
            context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)

            controller.setZoom(16.0)

            // Initial map location.
            controller.setCenter(
                OsmGeoPoint(21.1702, 72.8311)
            )
        }
    }

    // Request location permission.
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            hasLocationPermission =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (hasLocationPermission) {
                shouldCenterAfterPermission = true
            } else {
                shouldCenterAfterPermission = false
                showSettingsMessage = true
            }
        }

    // Fetch the current GPS location and move the map.
    LaunchedEffect(
        hasLocationPermission,
        shouldCenterAfterPermission
    ) {

        if (hasLocationPermission && shouldCenterAfterPermission) {

            shouldCenterAfterPermission = false

            try {
                @SuppressLint("MissingPermission")
                val locationTask = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                )

                locationTask.addOnSuccessListener { location ->

                    if (location != null) {

                        mapView.controller.animateTo(
                            OsmGeoPoint(
                                location.latitude,
                                location.longitude
                            )
                        )

                        mapView.controller.setZoom(18.0)
                        mapView.invalidate()

                    }
                }

            } catch (_: SecurityException) {
                hasLocationPermission = false
            }
        }
    }

    // Draw the polygon and draggable corner markers.
    LaunchedEffect(mapView, state.boundaryPoints) {

        mapView.overlays.clear()

        // Draw polygon.
        if (state.boundaryPoints.size >= 3) {

            val polygon = Polygon(mapView).apply {

                points = state.boundaryPoints.map {
                    OsmGeoPoint(
                        it.latitude,
                        it.longitude
                    )
                }

                fillPaint.color =
                    android.graphics.Color.argb(
                        65,
                        33,
                        150,
                        243
                    )

                outlinePaint.color =
                    android.graphics.Color.BLUE

                outlinePaint.strokeWidth = 4f
            }

            mapView.overlays.add(polygon)
        }

        // Add draggable markers.
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
                        }
                    }
                )
            }

            mapView.overlays.add(marker)
        }

        // Listen for map taps.
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

        mapView.overlays.add(tapOverlay)

        mapView.invalidate()
    }

    // Release map resources.
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

    // Explain location permission.
    if (showLocationExplanation) {

        AlertDialog(
            onDismissRequest = {
                showLocationExplanation = false
            },
            title = {
                Text("Location Permission")
            },
            text = {
                Text(
                    "ParkPulse needs your location to center the map " +
                            "on your current position so you can accurately " +
                            "draw the park boundary."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {

                        showLocationExplanation = false

                        if (!hasLocationPermission) {
                            shouldCenterAfterPermission = true

                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            shouldCenterAfterPermission = true
                        }
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLocationExplanation = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show settings option after permission denial.
    if (showSettingsMessage) {

        AlertDialog(
            onDismissRequest = {
                showSettingsMessage = false
            },
            title = {
                Text("Location Permission Required")
            },
            text = {
                Text(
                    "Location permission was denied. You can enable " +
                            "it in App Settings to center the park map " +
                            "on your current position."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {

                        showSettingsMessage = false

                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                context.packageName,
                                null
                            )
                        )

                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSettingsMessage = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Park name.
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

            // Park type.
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

            HorizontalDivider()

            // Boundary drawing instructions.
            Text(
                text = "Draw Park Boundary",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "First center the map on your current location. " +
                        "Then tap at least 3 corners around the park. " +
                        "Drag markers to adjust the boundary.",
                style = MaterialTheme.typography.bodySmall
            )

            // Use My Location button.
            Button(
                onClick = {
                    if (hasLocationPermission) {
                        shouldCenterAfterPermission = true
                    } else {
                        showLocationExplanation = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use My Location")
            }

            // Map.
            AndroidView(
                factory = {
                    mapView
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            )

            // Boundary count.
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

            HorizontalDivider()

            // Area and perimeter.
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

            // Error message.
            if (state.error != null) {

                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Save park.
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

                    Spacer(Modifier.width(8.dp))

                    Text("Saving...")

                } else {
                    Text("Save Park")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}


package com.example.parkplus.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.model.GeoPoint
import com.example.parkplus.domain.model.Park
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
fun MapScreen(
    parkId: String,
    viewModel: MapViewModel = hiltViewModel(),
    onAddPark: () -> Unit,
    onStartSurvey: (String) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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

    var currentLocation by remember {
        mutableStateOf<Location?>(null)
    }

    var selectedPark by remember {
        mutableStateOf<Park?>(null)
    }

    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Initialize map.
    val mapView = remember {
        Configuration.getInstance().userAgentValue =
            context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
            controller.setCenter(
                OsmGeoPoint(21.1702, 72.8311)
            )
        }
    }

    // Focus on the selected park when its data is loaded.

    LaunchedEffect(parkId, state.parks) {

        val selected = state.parks.firstOrNull {
            it.id == parkId
        }

        if (selected != null && selected.boundary.size >= 3) {

            val center = getParkCenter(selected)

            mapView.controller.animateTo(
                OsmGeoPoint(
                    center.latitude,
                    center.longitude
                )
            )

            mapView.controller.setZoom(17.0)
        }
    }


    // Request location updates only after permission is granted.
    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L
            )
                .setMinUpdateIntervalMillis(3000L)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(
                    result: LocationResult
                ) {
                    currentLocation = result.lastLocation
                }
            }

            try {
                @SuppressLint("MissingPermission")
                fun startUpdates() {
                    locationClient.requestLocationUpdates(
                        request,
                        callback,
                        context.mainLooper
                    )
                }

                startUpdates()
            } catch (_: SecurityException) {
                // Permission may have been revoked.
            }

            onDispose {
                locationClient.removeLocationUpdates(callback)
            }
        }
    }

    // Draw park polygons, location marker and map tap listener.

    LaunchedEffect(state.parks, currentLocation) {

        mapView.overlays.clear()

        state.parks.forEach { park ->

            if (park.boundary.size >= 3) {

                // Draw colored park boundary.
                val polygon = Polygon(mapView).apply {

                    points = park.boundary.map {
                        OsmGeoPoint(
                            it.latitude,
                            it.longitude
                        )
                    }

                    // Stronger transparent fill.
                    fillPaint.color = when (park.status) {
                        ParkStatus.OPEN ->
                            android.graphics.Color.argb(110, 46, 160, 67)

                        ParkStatus.NEEDS_REPAIR ->
                            android.graphics.Color.argb(110, 255, 165, 0)

                        ParkStatus.CLOSED ->
                            android.graphics.Color.argb(110, 220, 40, 40)
                    }

                    // Thick colored outline.
                    outlinePaint.color = when (park.status) {
                        ParkStatus.OPEN ->
                            android.graphics.Color.rgb(20, 120, 40)

                        ParkStatus.NEEDS_REPAIR ->
                            android.graphics.Color.rgb(220, 125, 0)

                        ParkStatus.CLOSED ->
                            android.graphics.Color.rgb(190, 20, 20)
                    }

                    outlinePaint.strokeWidth = 7f
                }

                mapView.overlays.add(polygon)

                // Add park name marker at the approximate center.
                val center = getParkCenter(park)

                val parkMarker = Marker(mapView).apply {

                    position = OsmGeoPoint(
                        center.latitude,
                        center.longitude
                    )

                    title = park.name

                    snippet =
                        "${park.status.name.replace('_', ' ')} • " +
                                "${"%.2f".format(park.areaSquareMeters)} m²"

                    setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )
                }

                mapView.overlays.add(parkMarker)
            }
        }

        // Current user location.
        currentLocation?.let { location ->

            val userMarker = Marker(mapView).apply {

                position = OsmGeoPoint(
                    location.latitude,
                    location.longitude
                )

                title = "My Location"

                setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
                )
            }

            mapView.overlays.add(userMarker)
        }

        // Detect park polygon taps.
        val tapOverlay = MapEventsOverlay(
            object : MapEventsReceiver {

                override fun singleTapConfirmedHelper(
                    point: OsmGeoPoint
                ): Boolean {

                    selectedPark = state.parks.firstOrNull { park ->
                        isPointInsidePolygon(
                            point.latitude,
                            point.longitude,
                            park
                        )
                    }

                    return selectedPark != null
                }

                override fun longPressHelper(
                    point: OsmGeoPoint
                ): Boolean = false
            }
        )

        mapView.overlays.add(tapOverlay)

        mapView.invalidate()
    }


    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    Scaffold { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = {
                            if (hasLocationPermission) {
                                currentLocation?.let { location ->

                                    mapView.controller.animateTo(
                                        OsmGeoPoint(
                                            location.latitude,
                                            location.longitude
                                        )
                                    )

                                    mapView.controller.setZoom(16.0)
                                }
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("My Location")
                    }

                    OutlinedButton(
                        onClick = onAddPark,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Park +")
                    }
                }

                if (!hasLocationPermission) {
                    Text(
                        text = "Location permission is needed to show your position.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                state.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Park details bottom sheet.
    selectedPark?.let { park ->

        ModalBottomSheet(
            onDismissRequest = {
                selectedPark = null
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = park.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    "Type: ${park.type.name.replace('_', ' ')}"
                )

                Text(
                    "Area: %.2f m²".format(
                        park.areaSquareMeters
                    )
                )

                Text(
                    "Status: ${park.status.name.replace('_', ' ')}"
                )

                currentLocation?.let { user ->

                    val center = getParkCenter(park)

                    val destination = Location("park").apply {
                        latitude = center.latitude
                        longitude = center.longitude
                    }

                    val distance = user.distanceTo(destination)
                    val bearing = user.bearingTo(destination)

                    Text(
                        "Distance: ${distance.toInt()} m, " +
                                compassDirection(bearing)
                    )

                } ?: Text(
                    "Enable location to see distance and direction."
                )

                Button(
                    onClick = {
                        onStartSurvey(park.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Survey")
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// Check whether a coordinate is inside a park boundary.
fun isPointInsidePolygon(
    latitude: Double,
    longitude: Double,
    park: Park
): Boolean {

    val points = park.boundary

    if (points.size < 3) return false

    var inside = false
    var j = points.lastIndex

    for (i in points.indices) {

        val xi = points[i].longitude
        val yi = points[i].latitude

        val xj = points[j].longitude
        val yj = points[j].latitude

        val intersects =
            ((yi > latitude) != (yj > latitude)) &&
                    (longitude < (xj - xi) *
                            (latitude - yi) / (yj - yi) + xi)

        if (intersects) {
            inside = !inside
        }

        j = i
    }

    return inside
}

// Calculate approximate center of park boundary.
fun getParkCenter(park: Park): GeoPoint {

    val points = park.boundary

    require(points.isNotEmpty()) {
        "Park boundary cannot be empty"
    }

    return GeoPoint(
        latitude = points.map { it.latitude }.average(),
        longitude = points.map { it.longitude }.average()
    )
}

// Convert bearing into compass direction.
fun compassDirection(bearing: Float): String {

    val directions = listOf(
        "north",
        "north-east",
        "east",
        "south-east",
        "south",
        "south-west",
        "west",
        "north-west"
    )

    val normalized = (bearing + 360f) % 360f

    val index = (
            (normalized + 22.5f) / 45f
            ).toInt() % 8

    return directions[index]
}

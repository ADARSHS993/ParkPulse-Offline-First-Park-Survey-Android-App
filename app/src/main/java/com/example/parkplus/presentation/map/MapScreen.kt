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
import kotlinx.coroutines.launch
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
    viewModel: MapViewModel = hiltViewModel()
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

    val mapView = remember {
        Configuration.getInstance().userAgentValue =
            context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
            controller.setCenter(OsmGeoPoint(21.1702, 72.8311))
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L
            ).setMinUpdateIntervalMillis(3000L).build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
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
                // Permission may have been revoked while the screen was open.
            }

            onDispose {
                locationClient.removeLocationUpdates(callback)
            }
        }
    }

    // Draw the latest park polygons on the map.
    LaunchedEffect(state.parks, currentLocation) {
        mapView.overlays.clear()

        state.parks.forEach { park ->
            if (park.boundary.size >= 3) {
                val polygon = Polygon(mapView).apply {
                    points = park.boundary.map {
                        OsmGeoPoint(it.latitude, it.longitude)
                    }

                    fillPaint.color = when (park.status) {
                        ParkStatus.OPEN ->
                            android.graphics.Color.argb(65, 46, 160, 67)

                        ParkStatus.NEEDS_REPAIR ->
                            android.graphics.Color.argb(75, 255, 165, 0)

                        ParkStatus.CLOSED ->
                            android.graphics.Color.argb(75, 220, 40, 40)
                    }

                    outlinePaint.color = when (park.status) {
                        ParkStatus.OPEN ->
                            android.graphics.Color.rgb(46, 160, 67)

                        ParkStatus.NEEDS_REPAIR ->
                            android.graphics.Color.rgb(255, 165, 0)

                        ParkStatus.CLOSED ->
                            android.graphics.Color.rgb(220, 40, 40)
                    }

                    outlinePaint.strokeWidth = 4f
                }

                mapView.overlays.add(polygon)
            }
        }

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

        // Detect taps on a park polygon.
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
                ) = false
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
                    .padding(16.dp)
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("My Location")
                }

                if (!hasLocationPermission) {
                    Text(
                        text = "Location permission is needed to show your position.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (state.error != null) {
                    Text(state.error ?: "")
                }
            }
        }
    }

    selectedPark?.let { park ->
        ModalBottomSheet(
            onDismissRequest = { selectedPark = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = park.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Text("Area: ${park.areaSquareMeters} m²")
                Text("Status: ${park.status.name.replace('_', ' ')}")

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
                } ?: Text("Enable location to see distance and direction.")

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

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
                    (longitude < (xj - xi) * (latitude - yi) /
                            (yj - yi) + xi)

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
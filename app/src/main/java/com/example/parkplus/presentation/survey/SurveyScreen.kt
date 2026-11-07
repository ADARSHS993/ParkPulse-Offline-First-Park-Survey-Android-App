
package com.example.parkplus.presentation.survey

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    onSurveyStarted: (String) -> Unit,
    viewModel: SurveyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    var permissionDenied by remember {
        mutableStateOf(false)
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

        permissionDenied = !hasLocationPermission
    }

    // Request location updates only while this screen is visible.
    DisposableEffect(
        hasLocationPermission,
        lifecycleOwner
    ) {

        if (!hasLocationPermission) {
            onDispose { }
        } else {

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000L
            )
                .setMinUpdateIntervalMillis(2000L)
                .build()

            val locationCallback = object : LocationCallback() {

                override fun onLocationResult(
                    result: LocationResult
                ) {
                    result.lastLocation?.let { location ->
                        viewModel.updateLocation(location)
                    }
                }
            }

            @SuppressLint("MissingPermission")
            fun startLocationUpdates() {
                try {
                    locationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        context.mainLooper
                    )
                } catch (_: SecurityException) {
                    hasLocationPermission = false
                }
            }

            fun stopLocationUpdates() {
                locationClient.removeLocationUpdates(
                    locationCallback
                )
            }

            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        startLocationUpdates()
                    }

                    Lifecycle.Event.ON_STOP -> {
                        stopLocationUpdates()
                    }

                    else -> Unit
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            if (
                lifecycleOwner.lifecycle.currentState
                    .isAtLeast(Lifecycle.State.STARTED)
            ) {
                startLocationUpdates()
            }

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                stopLocationUpdates()
            }
        }
    }

    // Navigate when the survey is successfully created.
    LaunchedEffect(state.surveyId) {
        state.surveyId?.let { id ->
            onSurveyStarted(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Park Survey Check-in")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            if (state.isLoading) {

                CircularProgressIndicator()

            } else if (state.error != null && state.park == null) {

                Text(
                    text = state.error ?: "Unable to load park",
                    color = MaterialTheme.colorScheme.error
                )

            } else {

                Text(
                    text = state.park?.name ?: "Selected Park",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Survey check-in requires you to be inside " +
                            "the park boundary with GPS accuracy of 25 metres or better.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(20.dp))

                // Location permission section.
                if (!hasLocationPermission) {

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = "Location permission required",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "ParkPulse needs your location to verify " +
                                        "that you are physically inside the park."
                            )

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            ) {
                                Text("Grant Location Permission")
                            }

                            if (permissionDenied) {
                                Text(
                                    text = "Location permission was denied. " +
                                            "Enable it in app settings to continue.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                } else {

                    // Current location information.
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = "Current GPS Location",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(Modifier.height(12.dp))

                            val location = state.currentLocation

                            if (location != null) {

                                Text(
                                    "Latitude: ${location.latitude}"
                                )

                                Text(
                                    "Longitude: ${location.longitude}"
                                )

                                Text(
                                    "GPS Accuracy: " +
                                            "${"%.1f".format(location.accuracy)} metres"
                                )

                            } else {

                                CircularProgressIndicator()

                                Spacer(Modifier.height(8.dp))

                                Text("Waiting for GPS signal...")
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Geofence validation result.
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.canStartSurvey) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = if (state.canStartSurvey) {
                                    "Ready to Check In"
                                } else {
                                    "Check-in Not Available"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(state.checkInMessage)

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "Inside park: " +
                                        if (state.isInsidePark) "Yes" else "No"
                            )

                            Text(
                                "GPS accuracy: " +
                                        if (state.hasAccurateGps) {
                                            "Acceptable"
                                        } else {
                                            "Too weak"
                                        }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.startSurvey()
                        },
                        enabled = state.canStartSurvey &&
                                !state.isStartingSurvey,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        if (state.isStartingSurvey) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )

                        } else {

                            Text("Start Survey")
                        }
                    }
                }

                if (state.error != null) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

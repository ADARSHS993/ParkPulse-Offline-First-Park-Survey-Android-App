package com.example.parkplus.presentation.survey

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

@Composable
fun CameraCaptureScreen(
    surveyId: String,
    parkName: String,
    latitude: Double,
    longitude: Double,
    accuracy: Float,
    onPhotoSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) error = "Camera permission is required to capture survey photos."
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(hasPermission, lifecycleOwner) {
        var cameraProvider: ProcessCameraProvider? = null

        if (hasPermission) {
            val future = ProcessCameraProvider.getInstance(context)

            future.addListener({
                try {
                    cameraProvider = future.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        capture
                    )

                    imageCapture = capture
                } catch (e: Exception) {
                    error = e.message ?: "Unable to start camera."
                }
            }, mainExecutor)
        }

        onDispose {
            imageCapture = null
            cameraProvider?.unbindAll()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    fun capturePhoto() {
        val capture = imageCapture ?: return

        isCapturing = true
        error = null

        val directory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "ParkPulse"
        ).apply { mkdirs() }

        val rawFile = File(directory, "raw_${System.currentTimeMillis()}.jpg")

        val options = ImageCapture.OutputFileOptions.Builder(rawFile).build()

        capture.takePicture(
            options,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {

                @RequiresApi(Build.VERSION_CODES.Q)
                override fun onImageSaved(
                    outputFileResults: ImageCapture.OutputFileResults
                ) {
                    try {
                        val exif = ExifInterface(rawFile)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )

                        val original = BitmapFactory.decodeFile(rawFile.absolutePath)
                            ?: throw IllegalStateException("Unable to read captured photo.")

                        val matrix = Matrix().apply {
                            when (orientation) {
                                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                                ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->
                                    postScale(-1f, 1f)
                                ExifInterface.ORIENTATION_FLIP_VERTICAL ->
                                    postScale(1f, -1f)
                            }
                        }

                        val oriented = BitmapFactory.decodeFile(rawFile.absolutePath)
                            ?.let {
                                val rotated = android.graphics.Bitmap.createBitmap(
                                    it,
                                    0,
                                    0,
                                    it.width,
                                    it.height,
                                    matrix,
                                    true
                                )
                                it.recycle()
                                rotated
                            } ?: original

                        val stamped = PhotoStamp.addStamp(
                            original = oriented,
                            parkName = parkName,
                            latitude = latitude,
                            longitude = longitude,
                            accuracy = accuracy,
                            capturedAt = System.currentTimeMillis()
                        )

                        val finalFile = File(
                            directory,
                            "survey_${surveyId}_${System.currentTimeMillis()}.jpg"
                        )

                        FileOutputStream(finalFile).use { output ->
                            stamped.compress(
                                android.graphics.Bitmap.CompressFormat.JPEG,
                                92,
                                output
                            )
                        }

                        rawFile.delete()
                        original.recycle()
                        if (oriented !== original) oriented.recycle()
                        stamped.recycle()

                        mainExecutor.execute {
                            isCapturing = false
                            onPhotoSaved(finalFile.absolutePath)
                        }

                    } catch (e: Exception) {
                        mainExecutor.execute {
                            isCapturing = false
                            error = e.message ?: "Photo processing failed."
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    mainExecutor.execute {
                        isCapturing = false
                        error = exception.message ?: "Photo capture failed."
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Capture Park Photo",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(12.dp))

        if (hasPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            Spacer(Modifier.height(24.dp))
            Text("Allow camera access to photograph the park.")
            Button(onClick = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }) {
                Text("Grant Camera Permission")
            }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }

            Button(
                onClick = ::capturePhoto,
                enabled = hasPermission && imageCapture != null && !isCapturing,
                modifier = Modifier.weight(1f)
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Capture Photo")
                }
            }
        }
    }
}
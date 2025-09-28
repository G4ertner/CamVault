package dev.nik.vaultcam.ui.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import dev.nik.vaultcam.crypto.TinkModule
import dev.nik.vaultcam.data.VaultRepository
import dev.nik.vaultcam.util.Orientation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "CameraScreen"

@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as? Activity

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val aead = remember(appContext) { TinkModule.getAead(appContext) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraController = remember(context) {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            isTapToFocusEnabled = true
            isPinchToZoomEnabled = true
        }
    }

    val executor = remember(context) {
        ContextCompat.getMainExecutor(context)
    }

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var flashMode by rememberSaveable { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }

    if (hasCameraPermission) {
        LaunchedEffect(lifecycleOwner, cameraController) {
            cameraController.bindToLifecycle(lifecycleOwner)
        }

        LaunchedEffect(cameraSelector) {
            cameraController.cameraSelector = cameraSelector
        }

        LaunchedEffect(flashMode) {
            cameraController.imageCaptureFlashMode = flashMode
        }
    }

    val gridColor = Color.White.copy(alpha = 0.3f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!hasCameraPermission) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Camera permission required", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text(text = "Grant Permission")
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("screen_camera_preview"),
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                controller = cameraController
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                        }
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f)
                    ) {
                        drawRuleOfThirds(gridColor)
                    }

                    CameraControls(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        flashMode = flashMode,
                        onFlashToggle = {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                                else -> ImageCapture.FLASH_MODE_OFF
                            }
                        },
                        onFlipCamera = {
                            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                        },
                        onShutterClick = {
                            val surfaceRotation = resolveDisplayRotation(activity)
                            cameraController.updateImageCaptureTargetRotation(surfaceRotation)
                            val rotationDegrees = Orientation.displayRotationToDegrees(surfaceRotation)
                            Log.d(TAG, "Shutter pressed; displayRotation=$surfaceRotation (${rotationDegrees} deg)")

                            cameraController.takePicture(
                                executor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val bytes = try {
                                            image.toByteArray()
                                        } finally {
                                            image.close()
                                        }

                                        coroutineScope.launch {
                                            val result = runCatching {
                                                withContext(Dispatchers.IO) {
                                                    VaultRepository.saveEncrypted(appContext, bytes, aead)
                                                }
                                            }
                                            result.onSuccess { id ->
                                                Log.d(TAG, "Encrypted image saved with id=$id")
                                                snackbarHostState.showSnackbar("Saved to Vault")
                                            }.onFailure { error ->
                                                Log.e(TAG, "Failed to save encrypted image", error)
                                                snackbarHostState.showSnackbar("Failed to save photo")
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e(TAG, "Capture failed", exception)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Capture failed")
                                        }
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawRuleOfThirds(color: Color) {
    val thirdWidth = size.width / 3f
    val thirdHeight = size.height / 3f

    drawLine(color, start = androidx.compose.ui.geometry.Offset(thirdWidth, 0f), end = androidx.compose.ui.geometry.Offset(thirdWidth, size.height))
    drawLine(color, start = androidx.compose.ui.geometry.Offset(thirdWidth * 2, 0f), end = androidx.compose.ui.geometry.Offset(thirdWidth * 2, size.height))
    drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, thirdHeight), end = androidx.compose.ui.geometry.Offset(size.width, thirdHeight))
    drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, thirdHeight * 2), end = androidx.compose.ui.geometry.Offset(size.width, thirdHeight * 2))
}

@Composable
private fun CameraControls(
    modifier: Modifier = Modifier,
    flashMode: Int,
    onFlashToggle: () -> Unit,
    onFlipCamera: () -> Unit,
    onShutterClick: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.testTag("btn_flip_camera"),
            onClick = onFlipCamera,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(imageVector = Icons.Filled.Cameraswitch, contentDescription = "Switch camera")
        }

        Spacer(modifier = Modifier.width(24.dp))

        IconButton(
            modifier = Modifier.testTag("btn_shutter"),
            onClick = onShutterClick,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = "Capture photo")
        }

        Spacer(modifier = Modifier.width(24.dp))

        val flashIcon = when (flashMode) {
            ImageCapture.FLASH_MODE_ON -> Icons.Filled.FlashOn
            ImageCapture.FLASH_MODE_AUTO -> Icons.Filled.FlashAuto
            else -> Icons.Filled.FlashOff
        }
        val flashDescription = when (flashMode) {
            ImageCapture.FLASH_MODE_ON -> "Flash on"
            ImageCapture.FLASH_MODE_AUTO -> "Flash auto"
            else -> "Flash off"
        }

        IconButton(
            modifier = Modifier.testTag("btn_flash_toggle"),
            onClick = onFlashToggle,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(imageVector = flashIcon, contentDescription = flashDescription)
        }
    }
}

private fun resolveDisplayRotation(activity: Activity?): Int {
    if (activity == null) return Surface.ROTATION_0
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.display?.rotation ?: Surface.ROTATION_0
    } else {
        @Suppress("DEPRECATION")
        activity.windowManager.defaultDisplay.rotation
    }
}

private fun LifecycleCameraController.updateImageCaptureTargetRotation(rotation: Int) {
    runCatching {
        val controllerClass = CameraController::class.java
        val field = controllerClass.getDeclaredField("mImageCapture")
        field.isAccessible = true
        val imageCapture = field.get(this) as? ImageCapture
        imageCapture?.targetRotation = rotation
    }.onFailure { error ->
        Log.w(TAG, "Unable to update image capture rotation", error)
    }
}

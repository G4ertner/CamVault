package dev.nik.vaultcam.ui.viewer

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.nik.vaultcam.crypto.TinkModule
import dev.nik.vaultcam.data.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    id: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val aead = remember(appContext) { TinkModule.getAead(appContext) }

    var imageBitmap by remember(id) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(id) { mutableStateOf(true) }
    var loadError by remember(id) { mutableStateOf<String?>(null) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(id) {
        isLoading = true
        loadError = null
        scale = 1f
        offset = Offset.Zero
        val result = runCatching {
            withContext(Dispatchers.IO) {
                val bytes = VaultRepository.decrypt(appContext, id, aead)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }
        }
        imageBitmap = result.getOrNull()
        loadError = result.exceptionOrNull()?.localizedMessage
        isLoading = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("btn_delete"),
                        onClick = {
                            coroutineScope.launch {
                                val deleted = withContext(Dispatchers.IO) {
                                    VaultRepository.delete(appContext, id)
                                }
                                if (deleted) {
                                    onDeleted()
                                } else {
                                    snackbarHostState.showSnackbar("Failed to delete")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .testTag("viewer_container"),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                imageBitmap != null -> {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Vault photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("viewer_image")
                            .pointerInput(imageBitmap) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    offset += pan
                                }
                            }
                            .graphicsLayer {
                                translationX = offset.x
                                translationY = offset.y
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }
                else -> {
                    Text(
                        modifier = Modifier
                            .testTag("viewer_error")
                            .padding(24.dp),
                        text = loadError ?: "Unable to load image",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

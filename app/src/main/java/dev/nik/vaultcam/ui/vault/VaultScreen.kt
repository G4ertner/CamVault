package dev.nik.vaultcam.ui.vault

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.nik.vaultcam.crypto.TinkModule
import dev.nik.vaultcam.data.VaultItem
import dev.nik.vaultcam.data.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VaultScreen(
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val aead = remember(appContext) { TinkModule.getAead(appContext) }
    var items by remember { mutableStateOf<List<VaultItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val thumbnailCache = remember { mutableStateMapOf<String, ImageBitmap?>() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val loadItems: suspend () -> Unit = {
        val list = withContext(Dispatchers.IO) { VaultRepository.listItems(appContext) }
        val ids = list.map { it.id }.toSet()
        val toRemove = thumbnailCache.keys.filterNot { it in ids }
        toRemove.forEach { thumbnailCache.remove(it) }
        items = list
        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadItems()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isLoading = true
                coroutineScope.launch { loadItems() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when {
        isLoading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("vault_loading"),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        items.isEmpty() -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("vault_empty"),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No items yet", style = MaterialTheme.typography.bodyLarge)
            }
        }
        else -> {
            LazyVerticalGrid(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("vault_grid"),
                columns = GridCells.Adaptive(minSize = 128.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    VaultGridItem(
                        item = item,
                        aeadProvider = { aead },
                        cachedThumbnails = thumbnailCache,
                        onClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultGridItem(
    item: VaultItem,
    aeadProvider: () -> com.google.crypto.tink.Aead,
    cachedThumbnails: MutableMap<String, ImageBitmap?>,
    onClick: (String) -> Unit
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    var thumbnail by remember(item.id) { mutableStateOf<ImageBitmap?>(cachedThumbnails[item.id]) }
    var isLoading by remember(item.id) { mutableStateOf(thumbnail == null) }

    LaunchedEffect(item.id) {
        if (thumbnail == null) {
            val bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = VaultRepository.decrypt(appContext, item.id, aeadProvider())
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                }.getOrNull()
            }
            if (bitmap != null) {
                cachedThumbnails[item.id] = bitmap
                thumbnail = bitmap
            }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(Color(0xFFE0E0E0), shape = MaterialTheme.shapes.small)
            .clickable(enabled = thumbnail != null) { onClick(item.id) }
            .testTag("vault_item_${item.id}"),
        contentAlignment = Alignment.Center
    ) {
        when {
            thumbnail != null -> {
                Image(
                    bitmap = thumbnail!!,
                    contentDescription = "Vault item",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
            else -> {
                Text(text = "Error", color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

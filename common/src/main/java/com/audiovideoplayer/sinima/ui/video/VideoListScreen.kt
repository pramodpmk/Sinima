package com.audiovideoplayer.sinima.ui.video

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.audiovideoplayer.sinima.data.MediaItem
import com.audiovideoplayer.sinima.ui.audio.formatDuration
import com.audiovideoplayer.sinima.viewmodel.VideoViewModel

@Composable
fun VideoListScreen(viewModel: VideoViewModel, onVideoClick: (String, String) -> Unit) {
    val context = LocalContext.current
    val videoItems by viewModel.videoItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.loadVideo()
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.loadVideo()
        else permissionLauncher.launch(permission)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !hasPermission -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Storage permission required to list video files")
                    Button(onClick = { permissionLauncher.launch(permission) }) {
                        Text("Grant Permission")
                    }
                }
            }
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            videoItems.isEmpty() -> Text(
                text = "No video files found on device",
                modifier = Modifier.align(Alignment.Center)
            )
            else -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(videoItems) { item ->
                    VideoListItem(item = item, onClick = { onVideoClick(item.uri, item.title) })
                    HorizontalDivider(modifier = Modifier.padding(start = 76.dp))
                }
            }
        }
    }
}

@Composable
private fun VideoListItem(item: MediaItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VideoThumbnail(uri = item.uri)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.mimeType,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Text(
            text = formatDuration(item.duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VideoThumbnail(uri: String) {
    val context = LocalContext.current
    var hasError by remember(uri) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(80.dp, 52.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!hasError) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(uri))
                    .videoFrameMillis(1_000)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { hasError = true }
            )
        } else {
            Icon(
                Icons.Default.PlayCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

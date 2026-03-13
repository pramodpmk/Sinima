package com.audiovideoplayer.sinima.ui.livevideo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class LiveStream(
    val title: String,
    val description: String,
    val url: String
)

private val liveStreams = listOf(
    LiveStream(
        title = "Big Buck Bunny (HLS)",
        description = "Open-source animated film — Blender Foundation",
        url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    ),
    LiveStream(
        title = "Elephant Dream (MP4)",
        description = "Open-source animated film — Blender Foundation",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    ),
    LiveStream(
        title = "Subaru Outback Adventure",
        description = "Google sample test stream",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
    )
)

@Composable
fun LiveVideoScreen(onStreamClick: (String, String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(liveStreams) { stream ->
                LiveStreamItem(
                    stream = stream,
                    onClick = {
                        onStreamClick(stream.url, stream.title)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 76.dp))
            }
        }
    }
}

@Composable
private fun LiveStreamItem(stream: LiveStream, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Tv,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stream.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stream.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Default.PlayCircle,
            contentDescription = "Play",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

package com.audiovideoplayer.sinima.ui.liveaudio

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.audiovideoplayer.sinima.viewmodel.AudioViewModel

private const val LIVE_AUDIO_URL = "https://stream.epic-lounge.com/nature-sounds"
private const val LIVE_AUDIO_TITLE = "Nature Sounds"

@Composable
fun LiveAudioScreen(viewModel: AudioViewModel) {
    val state by viewModel.playbackState.collectAsState()
    val isThisStreamPlaying = state.isPlaying && state.currentTitle == LIVE_AUDIO_TITLE

    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Station icon with pulse animation
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(if (isThisStreamPlaying) pulseScale else 1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Radio,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = LIVE_AUDIO_TITLE,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Epic Lounge Radio",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isThisStreamPlaying) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                )
                Text(
                    text = "LIVE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Play / Stop button
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    if (isThisStreamPlaying) {
                        viewModel.stop()
                    } else {
                        viewModel.playLiveAudio(LIVE_AUDIO_URL, LIVE_AUDIO_TITLE)
                    }
                }
            ) {
                Icon(
                    imageVector = if (isThisStreamPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isThisStreamPlaying) "Stop" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isThisStreamPlaying) "Tap to stop" else "Tap to listen",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

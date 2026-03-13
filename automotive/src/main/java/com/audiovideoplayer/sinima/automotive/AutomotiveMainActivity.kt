package com.audiovideoplayer.sinima.automotive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.audiovideoplayer.sinima.automotive.ui.video.AutomotiveVideoPlayerActivity
import com.audiovideoplayer.sinima.ui.audio.AudioListScreen
import com.audiovideoplayer.sinima.ui.audio.AudioPlayerScreen
import com.audiovideoplayer.sinima.ui.liveaudio.LiveAudioScreen
import com.audiovideoplayer.sinima.ui.livevideo.LiveVideoScreen
import com.audiovideoplayer.sinima.ui.navigation.Screen
import com.audiovideoplayer.sinima.ui.theme.SinimaTheme
import com.audiovideoplayer.sinima.ui.video.VideoListScreen
import com.audiovideoplayer.sinima.viewmodel.AudioPlaybackState
import com.audiovideoplayer.sinima.viewmodel.AudioViewModel
import com.audiovideoplayer.sinima.viewmodel.VideoViewModel

class AutomotiveMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SinimaTheme {
                AutomotiveApp()
            }
        }
    }
}

@Composable
fun AutomotiveApp() {
    val navController = rememberNavController()
    val audioViewModel: AudioViewModel = viewModel()
    val videoViewModel: VideoViewModel = viewModel()

    val playbackState by audioViewModel.playbackState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Triple(Screen.AudioList, "Audio", Icons.Default.MusicNote),
        Triple(Screen.VideoList, "Video", Icons.Default.VideoLibrary),
        Triple(Screen.LiveAudio, "Live Audio", Icons.Default.Radio),
        Triple(Screen.LiveVideo, "Live Video", Icons.Default.Tv)
    )

    Scaffold(
        bottomBar = {
            Column {
                val showMiniPlayer = playbackState.currentTitle.isNotEmpty()
                        && currentRoute != Screen.AudioPlayer.route
                if (showMiniPlayer) {
                    AutomotiveMiniPlayer(
                        state = playbackState,
                        onTogglePlayPause = audioViewModel::togglePlayPause,
                        onClick = { navController.navigate(Screen.AudioPlayer.route) }
                    )
                }
                NavigationBar(tonalElevation = 4.dp) {
                    bottomNavItems.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            icon = {
                                Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp))
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.AudioList.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.AudioList.route) {
                AudioListScreen(
                    viewModel = audioViewModel,
                    onNavigateToPlayer = { navController.navigate(Screen.AudioPlayer.route) }
                )
            }
            composable(Screen.AudioPlayer.route) {
                AudioPlayerScreen(
                    viewModel = audioViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.VideoList.route) {
                val context = LocalContext.current
                VideoListScreen(
                    viewModel = videoViewModel,
                    onVideoClick = { uri, title ->
                        context.startActivity(
                            AutomotiveVideoPlayerActivity.createIntent(context, uri, title)
                        )
                    }
                )
            }
            composable(Screen.LiveAudio.route) {
                LiveAudioScreen(viewModel = audioViewModel)
            }
            composable(Screen.LiveVideo.route) {
                val context = LocalContext.current
                LiveVideoScreen(
                    onStreamClick = { url, title ->
                        context.startActivity(
                            AutomotiveVideoPlayerActivity.createIntent(context, url, title)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AutomotiveMiniPlayer(
    state: AudioPlaybackState,
    onTogglePlayPause: () -> Unit,
    onClick: () -> Unit
) {
    Surface(tonalElevation = 8.dp, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.currentTitle.ifEmpty { "Playing" },
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = state.currentArtist.ifEmpty { "Unknown Artist" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.OpenInFull,
                    contentDescription = "Open Player",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

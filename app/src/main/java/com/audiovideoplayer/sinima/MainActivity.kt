package com.audiovideoplayer.sinima

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.audiovideoplayer.sinima.ui.audio.AudioListScreen
import com.audiovideoplayer.sinima.ui.audio.AudioPlayerScreen
import com.audiovideoplayer.sinima.ui.liveaudio.LiveAudioScreen
import com.audiovideoplayer.sinima.ui.livevideo.LiveVideoScreen
import com.audiovideoplayer.sinima.ui.navigation.Screen
import com.audiovideoplayer.sinima.ui.theme.SinimaTheme
import com.audiovideoplayer.sinima.ui.video.VideoListScreen
import com.audiovideoplayer.sinima.ui.video.VideoPlayerActivity
import com.audiovideoplayer.sinima.viewmodel.AudioPlaybackState
import com.audiovideoplayer.sinima.viewmodel.AudioViewModel
import com.audiovideoplayer.sinima.viewmodel.VideoViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            SinimaTheme {
                SinimaApp()
            }
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun SinimaApp() {
    val navController = rememberNavController()
    val audioViewModel: AudioViewModel = viewModel()
    val videoViewModel: VideoViewModel = viewModel()

    val playbackState by audioViewModel.playbackState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.AudioList, "Audio", Icons.Default.MusicNote),
        BottomNavItem(Screen.VideoList, "Video", Icons.Default.VideoLibrary),
        BottomNavItem(Screen.LiveAudio, "Live Audio", Icons.Default.Radio),
        BottomNavItem(Screen.LiveVideo, "Live Video", Icons.Default.Tv)
    )

    Scaffold(
        bottomBar = {
            Column {
                // Mini player: show when audio is playing and not on full player screen
                val showMiniPlayer = playbackState.currentTitle.isNotEmpty()
                        && currentRoute != Screen.AudioPlayer.route
                if (showMiniPlayer) {
                    MiniPlayer(
                        state = playbackState,
                        onTogglePlayPause = audioViewModel::togglePlayPause,
                        onClick = { navController.navigate(Screen.AudioPlayer.route) }
                    )
                }
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
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
                        context.startActivity(VideoPlayerActivity.createIntent(context, uri, title))
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
                        context.startActivity(VideoPlayerActivity.createIntent(context, url, title))
                    }
                )
            }
        }
    }
}

@Composable
fun MiniPlayer(
    state: AudioPlaybackState,
    onTogglePlayPause: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.currentTitle.ifEmpty { "Playing" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = state.currentArtist.ifEmpty { "Unknown Artist" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play"
                )
            }
        }
    }
}

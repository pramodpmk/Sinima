package com.audiovideoplayer.sinima.ui.navigation

sealed class Screen(val route: String) {
    data object AudioList : Screen("audio_list")
    data object AudioPlayer : Screen("audio_player")
    data object VideoList : Screen("video_list")
    data object LiveAudio : Screen("live_audio")
    data object LiveVideo : Screen("live_video")
}

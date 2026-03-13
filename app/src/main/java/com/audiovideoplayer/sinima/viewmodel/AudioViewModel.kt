package com.audiovideoplayer.sinima.viewmodel

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem as Media3MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.audiovideoplayer.sinima.data.MediaItem
import com.audiovideoplayer.sinima.data.MediaRepository
import com.audiovideoplayer.sinima.service.AudioPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val currentTitle: String = "",
    val currentArtist: String = "",
    val currentAlbumArtUri: String? = null,
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false
)

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)

    private val _audioItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val audioItems: StateFlow<List<MediaItem>> = _audioItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    private val _controllerReady = MutableStateFlow(false)
    val controllerReady: StateFlow<Boolean> = _controllerReady.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onMediaItemTransition(mediaItem: Media3MediaItem?, reason: Int) {
            val meta = mediaItem?.mediaMetadata
            _playbackState.update {
                it.copy(
                    currentTitle = meta?.title?.toString() ?: "",
                    currentArtist = meta?.artist?.toString() ?: "",
                    currentAlbumArtUri = meta?.artworkUri?.toString(),
                    duration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L,
                    hasNext = mediaController?.hasNextMediaItem() ?: false,
                    hasPrevious = mediaController?.hasPreviousMediaItem() ?: false
                )
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_READY) {
                _playbackState.update {
                    it.copy(
                        duration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L,
                        hasNext = mediaController?.hasNextMediaItem() ?: false,
                        hasPrevious = mediaController?.hasPreviousMediaItem() ?: false
                    )
                }
            }
        }
    }

    init {
        initController()
        startPositionUpdater()
    }

    private fun initController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), AudioPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture!!.addListener({
            mediaController = controllerFuture!!.get()
            mediaController?.addListener(playerListener)
            _controllerReady.value = true
            syncStateFromController()
        }, MoreExecutors.directExecutor())
    }

    private fun syncStateFromController() {
        mediaController?.let { c ->
            val meta = c.currentMediaItem?.mediaMetadata
            _playbackState.update {
                it.copy(
                    isPlaying = c.isPlaying,
                    currentTitle = meta?.title?.toString() ?: "",
                    currentArtist = meta?.artist?.toString() ?: "",
                    currentAlbumArtUri = meta?.artworkUri?.toString(),
                    duration = c.duration.coerceAtLeast(0L),
                    hasNext = c.hasNextMediaItem(),
                    hasPrevious = c.hasPreviousMediaItem()
                )
            }
        }
    }

    private fun startPositionUpdater() {
        viewModelScope.launch {
            while (true) {
                delay(500)
                mediaController?.let { c ->
                    if (c.isPlaying) {
                        _playbackState.update {
                            it.copy(
                                currentPosition = c.currentPosition.coerceAtLeast(0L),
                                duration = c.duration.coerceAtLeast(0L)
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadAudio() {
        viewModelScope.launch {
            _isLoading.value = true
            _audioItems.value = repository.getAudioItems()
            _isLoading.value = false
        }
    }

    fun playAudio(items: List<MediaItem>, startIndex: Int) {
        val controller = mediaController ?: return
        val media3Items = items.map { item ->
            Media3MediaItem.Builder()
                .setUri(item.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.title)
                        .setArtist(item.artist)
                        .setAlbumTitle(item.album)
                        .setArtworkUri(item.albumArtUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
        }
        controller.setMediaItems(media3Items, startIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun playLiveAudio(url: String, title: String) {
        val controller = mediaController ?: return
        val item = Media3MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist("Live Stream")
                    .build()
            )
            .build()
        controller.setMediaItem(item)
        controller.prepare()
        controller.play()
    }

    fun togglePlayPause() {
        val c = mediaController ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun seekToNext() { mediaController?.seekToNextMediaItem() }
    fun seekToPrevious() { mediaController?.seekToPreviousMediaItem() }
    fun seekTo(positionMs: Long) { mediaController?.seekTo(positionMs) }
    fun stop() { mediaController?.stop() }

    override fun onCleared() {
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}

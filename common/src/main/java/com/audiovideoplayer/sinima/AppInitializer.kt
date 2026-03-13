package com.audiovideoplayer.sinima

import android.content.Context
import coil.ImageLoader
import coil.decode.VideoFrameDecoder

fun createImageLoader(context: Context): ImageLoader =
    ImageLoader.Builder(context)
        .components { add(VideoFrameDecoder.Factory()) }
        .crossfade(true)
        .build()

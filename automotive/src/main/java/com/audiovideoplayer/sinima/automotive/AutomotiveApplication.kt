package com.audiovideoplayer.sinima.automotive

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.audiovideoplayer.sinima.createImageLoader

class AutomotiveApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader = createImageLoader(this)
}

package com.audiovideoplayer.sinima

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

class SinimaApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader = createImageLoader(this)
}

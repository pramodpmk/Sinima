plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.audiovideoplayer.sinima"
    compileSdk {
        version = release(36)
    }
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
    lint { disable += "UnsafeOptInUsageError" }
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.navigation.compose)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.activity.compose)
    api(libs.androidx.media3.exoplayer)
    api(libs.androidx.media3.exoplayer.hls)
    api(libs.androidx.media3.session)
    api(libs.androidx.media3.ui)
    api(libs.coil.compose)
    api(libs.coil.video)
}

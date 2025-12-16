package com.audil

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AudilApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize logging or other global configurations here
    }
}

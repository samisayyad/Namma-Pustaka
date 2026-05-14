package com.nammapustaka

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NammaPustakaApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

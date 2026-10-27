package com.example.adminessentialhub

import android.app.Application
import com.cloudinary.android.MediaManager


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MediaManager.init(
            this,
            mapOf(
                "cloud_name" to BuildConfig.CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
            )
        )
    }
}

// notification jb dikhe tb click krne pr app open ho
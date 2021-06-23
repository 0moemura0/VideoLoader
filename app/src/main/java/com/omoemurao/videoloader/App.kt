package com.omoemurao.videoloader

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import com.omoemurao.videoloader.Constants.CHANNEL_ID

import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler


class App:  Application() {
    override fun onCreate() {
        super.onCreate()
        VK.addTokenExpiredHandler(tokenTracker)

    }

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            MainActivity.startFrom(this@App)
        }
    }
}
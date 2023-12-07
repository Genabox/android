package com.example.callboy2

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.content.SharedPreferences
import android.util.Log

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("AlarmSettings", MODE_PRIVATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mp3Url = sharedPreferences.getString("mp3_url", null)
        if (mp3Url.isNullOrEmpty()) {
            Log.e("AlarmService", "MP3 URL is missing")
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(mp3Url)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Error playing MP3: ${e.message}")
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

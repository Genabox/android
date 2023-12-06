package com.example.callboy2

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.concurrent.schedule

import android.app.Service
import android.content.Intent

import android.os.IBinder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager

import android.os.Binder

import android.os.PowerManager
import androidx.core.app.NotificationCompat


class MyAlarmService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private val binder = LocalBinder()
    private lateinit var wakeLock: PowerManager.WakeLock

    inner class LocalBinder : Binder() {
        fun getService(): MyAlarmService = this@MyAlarmService
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "AlarmService::MyWakelockTag")

        wakeLock.acquire()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
        wakeLock.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private fun createNotification(): Notification {
        val channelId = "AlarmServiceChannel"
        val channelName = "Alarm Service Channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Alarm Service")
            .setContentText("Alarm Service is running in the background")
            .setSmallIcon(R.drawable.ic_launcher_foreground) 
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        return notificationBuilder.build()
    }
}


class AlarmService : Service() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        // Инициализация MediaPlayer и воспроизведение музыки
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Обработка срабатывания будильника, включение черного экрана и воспроизведение музыки
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        // Остановка MediaPlayer и освобождение ресурсов
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
class MainActivity : AppCompatActivity() {
    private lateinit var mp3UrlEditText: EditText
    private lateinit var alarmTimeEditText: EditText
    private lateinit var alarmIntervalEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var startAlarmButton: Button
    private lateinit var stopAlarmButton: Button
    private lateinit var testAlarmButton: Button
    private val handler = Handler()
    private var mediaPlayer: MediaPlayer? = null
    private var alarmTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mp3UrlEditText = findViewById(R.id.mp3UrlEditText)
        alarmTimeEditText = findViewById(R.id.alarmTimeEditText)
        alarmIntervalEditText = findViewById(R.id.alarmIntervalEditText)
        saveButton = findViewById(R.id.saveButton)
        startAlarmButton = findViewById(R.id.startAlarmButton)
        stopAlarmButton = findViewById(R.id.stopAlarmButton)
        testAlarmButton = findViewById(R.id.testAlarmButton)

        loadSettings()

        saveButton.setOnClickListener {
            saveSettings()
        }

        startAlarmButton.setOnClickListener {
            startAlarm()
        }

        stopAlarmButton.setOnClickListener {
            stopAlarm()
        }

        testAlarmButton.setOnClickListener {
            testAlarm()
        }

        // Установим начальное состояние кнопки "Стоп" в неактивное
        stopAlarmButton.isEnabled = false
    }

    private fun loadSettings() {
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val mp3Url = sharedPref.getString("mp3_url", "https://file-examples.com/storage/febf69dcf3656dfd992b0fa/2017/11/file_example_MP3_700KB.mp3")
        val alarmTime = sharedPref.getString("alarm_time", "21:41")
        val alarmInterval = sharedPref.getString("alarm_interval", "00:00:15")

        mp3UrlEditText.setText(mp3Url)
        alarmTimeEditText.setText(alarmTime)
        alarmIntervalEditText.setText(alarmInterval)
    }

    private fun saveSettings() {
        val mp3Url = mp3UrlEditText.text.toString()
        val alarmTime = alarmTimeEditText.text.toString()
        val alarmInterval = alarmIntervalEditText.text.toString()

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("mp3_url", mp3Url)
        editor.putString("alarm_time", alarmTime)
        editor.putString("alarm_interval", alarmInterval)
        editor.apply()

        Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
    }


    private var backgroundThread: Thread? = null


    private fun startAlarm() {
        val alarmTimeString = alarmTimeEditText.text.toString()
        val alarmTimeParts = alarmTimeString.split(":")

        if (alarmTimeParts.size == 2) {
            val hours = alarmTimeParts[0].toIntOrNull()
            val minutes = alarmTimeParts[1].toIntOrNull()

            if (hours != null && minutes != null && hours in 0..23 && minutes in 0..59) {
                val currentTime = Calendar.getInstance()

                val alarmTime = Calendar.getInstance()
                alarmTime.set(Calendar.HOUR_OF_DAY, hours)
                alarmTime.set(Calendar.MINUTE, minutes)
                alarmTime.set(Calendar.SECOND, 0)

                // Если указанное время уже прошло, добавляем 1 день
                if (alarmTime.before(currentTime)) {
                    alarmTime.add(Calendar.DAY_OF_YEAR, 1)
                }

                val delayMillis = alarmTime.timeInMillis - currentTime.timeInMillis

                alarmTimer = Timer()
                alarmTimer?.schedule(delayMillis) {
                    handler.post {
                        playAlarm()
                    }
                }

                startAlarmButton.isEnabled = false
                testAlarmButton.isEnabled = false
                stopAlarmButton.isEnabled = true
            } else {
                // Если время введено некорректно, можно добавить обработку ошибки или уведомление.
                // Например, Toast.makeText(this, "Введите корректное время", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playAlarm() {
        val mp3Url = mp3UrlEditText.text.toString()

        if (mp3Url.isNotEmpty()) {
            backgroundThread = Thread {
                try {
                    mediaPlayer = MediaPlayer()
                    mediaPlayer?.setDataSource(mp3Url)
                    mediaPlayer?.prepare()

                    mediaPlayer?.setOnCompletionListener {
                        handler.post {
                            testAlarmButton.isEnabled = false
                            startAlarmButton.isEnabled = false
                            stopAlarmButton.isEnabled = true
                        }

                        val intervalString = alarmIntervalEditText.text.toString()
                        val intervalParts = intervalString.split(":")
                        if (intervalParts.size == 3) {
                            val intervalHours = intervalParts[0].toLongOrNull()
                            val intervalMinutes = intervalParts[1].toLongOrNull()
                            val intervalSeconds = intervalParts[2].toLongOrNull()

                            if (intervalHours != null && intervalMinutes != null && intervalSeconds != null) {
                                val intervalMillis =
                                    intervalHours * 3600000 + intervalMinutes * 60000 + intervalSeconds * 1000

                                handler.postDelayed({
                                    playAlarm()
                                }, intervalMillis)
                            }
                        }
                    }

                    mediaPlayer?.start()
                    backgroundThread = null
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.post {
                        Toast.makeText(this, "Ошибка при проигрывании будильника", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            backgroundThread?.start()
        } else {
            Toast.makeText(this, "Введите URL для запуска будильника", Toast.LENGTH_SHORT).show()
        }
    }


    private fun stopAlarm() {
        if (backgroundThread != null && backgroundThread?.isAlive == true) {
            backgroundThread?.interrupt()
        }

        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        alarmTimer?.cancel()
        alarmTimer = null

        testAlarmButton.isEnabled = true
        startAlarmButton.isEnabled = true
        stopAlarmButton.isEnabled = false
    }

    private fun testAlarm() {
        val mp3Url = mp3UrlEditText.text.toString()
        testAlarmButton.isEnabled = false
        stopAlarmButton.isEnabled = true
        startAlarmButton.isEnabled = false
        if (mp3Url.isNotEmpty()) {
            backgroundThread = Thread {
                try {
                    mediaPlayer = MediaPlayer()
                    mediaPlayer?.setDataSource(mp3Url)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()

                    // Включаем кнопку "Стоп"
                    stopAlarmButton.isEnabled = true

                    mediaPlayer?.setOnCompletionListener {
                        handler.post {
                            // Воспроизведение завершено, включаем кнопку "Тестировать будильник" и
                            // отключаем кнопку "Стоп"
                            testAlarmButton.isEnabled = true
                            stopAlarmButton.isEnabled = false
                            startAlarmButton.isEnabled = false
                        }

                        val intervalString = alarmIntervalEditText.text.toString()
                        val intervalParts = intervalString.split(":")
                        if (intervalParts.size == 3) {
                            val intervalHours = intervalParts[0].toLongOrNull()
                            val intervalMinutes = intervalParts[1].toLongOrNull()
                            val intervalSeconds = intervalParts[2].toLongOrNull()

                            if (intervalHours != null && intervalMinutes != null && intervalSeconds != null) {
                                val intervalMillis =
                                    intervalHours * 3600000 + intervalMinutes * 60000 + intervalSeconds * 1000

                                handler.postDelayed({
                                    playAlarm()
                                }, intervalMillis)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.post {
                        Toast.makeText(this, "Ошибка при тестировании будильника", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            backgroundThread?.start()

            // Отключаем кнопку "Тестировать будильник" на время воспроизведения
            testAlarmButton.isEnabled = false
        } else {
            Toast.makeText(this, "Введите URL для тестирования", Toast.LENGTH_SHORT).show()
        }
    }

}

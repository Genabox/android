package com.example.callboy2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import java.util.Timer
import java.util.TimerTask
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.PowerManager

class MainActivity : AppCompatActivity() {

    private lateinit var mp3UrlEditText: EditText
    private lateinit var alarmTimeEditText: EditText
    private lateinit var alarmIntervalEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var startAlarmButton: Button
    private lateinit var stopAlarmButton: Button
    private lateinit var testAlarmButton: Button
    private var alarmTimer: Timer? = null
    private var backgroundThread: Thread? = null
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация компонентов UI
        mp3UrlEditText = findViewById(R.id.mp3UrlEditText)
        alarmTimeEditText = findViewById(R.id.alarmTimeEditText)
        alarmIntervalEditText = findViewById(R.id.alarmIntervalEditText)
        saveButton = findViewById(R.id.saveButton)
        startAlarmButton = findViewById(R.id.startAlarmButton)
        stopAlarmButton = findViewById(R.id.stopAlarmButton)
        testAlarmButton = findViewById(R.id.testAlarmButton)
        startAlarmButton = findViewById(R.id.startAlarmButton)
        startAlarmButton.setOnClickListener {
            startAlarm()
        }
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

    private fun startAlarm() {
        val alarmTimeString = alarmTimeEditText.text.toString()
        val alarmTimeParts = alarmTimeString.split(":")

        if (alarmTimeParts.size == 2) {
            val hours = alarmTimeParts[0].toIntOrNull()
            val minutes = alarmTimeParts[1].toIntOrNull()

            if (hours != null && minutes != null && hours in 0..23 && minutes in 0..59) {
                val currentTime = Calendar.getInstance()

                val alarmTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hours)
                    set(Calendar.MINUTE, minutes)
                    set(Calendar.SECOND, 0)

                    // Если указанное время уже прошло, добавляем 1 день
                    if (before(currentTime)) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                val delayMillis = alarmTime.timeInMillis - currentTime.timeInMillis

                alarmTimer?.cancel() // Отменяем предыдущий таймер, если он существует
                alarmTimer = Timer().apply {
                    schedule(object : TimerTask() {
                        override fun run() {
                            handler.post {
                                playAlarm()
                            }
                        }
                    }, delayMillis)
                }

                startAlarmButton.isEnabled = false
                testAlarmButton.isEnabled = false
                stopAlarmButton.isEnabled = true
            } else {
                Toast.makeText(this, "Введите корректное время", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Введите время в формате HH:mm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playAlarm() {
        val mp3Url = mp3UrlEditText.text.toString()

        if (mp3Url.isNotEmpty()) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakeLockTag").apply {
                acquire(10*60*1000L /*10 minutes*/) // Захватываем WakeLock на 10 минут
            }

            backgroundThread = Thread {
                try {
                    mediaPlayer = MediaPlayer()
                    mediaPlayer?.setDataSource(mp3Url)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()

                    mediaPlayer?.setOnCompletionListener {
                        handler.post {
                            testAlarmButton.isEnabled = true
                            startAlarmButton.isEnabled = true
                            stopAlarmButton.isEnabled = false

                            val intervalString = alarmIntervalEditText.text.toString()
                            val intervalParts = intervalString.split(":")
                            if (intervalParts.size == 3) {
                                val intervalHours = intervalParts[0].toLongOrNull()
                                val intervalMinutes = intervalParts[1].toLongOrNull()
                                val intervalSeconds = intervalParts[2].toLongOrNull()

                                if (intervalHours != null && intervalMinutes != null && intervalSeconds != null) {
                                    val intervalMillis = intervalHours * 3600000 + intervalMinutes * 60000 + intervalSeconds * 1000

                                    handler.postDelayed({
                                        playAlarm()
                                    }, intervalMillis)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.post {
                        Toast.makeText(this, "Ошибка при проигрывании будильника", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    wakeLock.release() // Освобождаем WakeLock
                }
            }
            backgroundThread?.start()
        } else {
            Toast.makeText(this, "Введите URL для запуска будильника", Toast.LENGTH_SHORT).show()
        }
    }



    private fun stopAlarm() {
        stopService(Intent(this, AlarmService::class.java))
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

        backgroundThread = Thread(Runnable {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(mp3Url)
                prepare()
                start()
            }
            handler.post {
                testAlarmButton.isEnabled = false
                stopAlarmButton.isEnabled = true
            }
        }).apply {
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundThread?.interrupt()
        mediaPlayer?.release()
    }
}

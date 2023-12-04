package com.example.callboy2

import android.content.res.AssetManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File
import java.util.*
import android.content.SharedPreferences
import android.content.Context


class MainActivity : AppCompatActivity() {
    private lateinit var mp3UrlEditText: EditText
    private lateinit var alarmTimeEditText: EditText
    private lateinit var alarmIntervalEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var startAlarmButton: Button
    private lateinit var stopAlarmButton: Button
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var alarmTime: String
    private lateinit var testAlarmButton: Button
    private lateinit var exitButton: Button
    private val handler = Handler()
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

        // Выводим сообщение об успешном сохранении
        Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
    }


    private fun startAlarm() {
        val alarmTimeString = alarmTimeEditText.text.toString()
        val alarmTimeParts = alarmTimeString.split(":")
        if (alarmTimeParts.size == 2) {
            val hours = alarmTimeParts[0].toIntOrNull()
            val minutes = alarmTimeParts[1].toIntOrNull()

            if (hours != null && minutes != null && hours in 0..23 && minutes in 0..59) {
                val currentTime = Calendar.getInstance().apply {
                    set(Calendar.SECOND, 0)
                }
                val alarmTime = Calendar.getInstance()
                alarmTime.set(Calendar.HOUR_OF_DAY, hours)
                alarmTime.set(Calendar.MINUTE, minutes)
                alarmTime.set(Calendar.SECOND, 0)

                val delayMillis = alarmTime.timeInMillis - currentTime.timeInMillis

                if (delayMillis > 0) {
                    alarmTimer = Timer()
                    alarmTimer?.schedule(object : TimerTask() {
                        override fun run() {
                            handler.post {
                                playAlarm()
                            }
                        }
                    }, delayMillis)

                    // Отключаем кнопку "Старт" и "Тест звука", активируем кнопку "Стоп"
                    startAlarmButton.isEnabled = false
                    testAlarmButton.isEnabled = false
                    stopAlarmButton.isEnabled = true
                } else {
                    Toast.makeText(this, "Выберите будущее время", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Некорректное время", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Некорректный формат времени (HH:mm)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playAlarm() {
        val mp3Url = mp3UrlEditText.text.toString()

        if (mp3Url.isNotEmpty()) {
            try {
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(mp3Url)
                mediaPlayer?.prepare()
                mediaPlayer?.start()

                mediaPlayer?.setOnCompletionListener {
                    // Воспроизведение завершено, оставляем кнопки "Тестировать будильник" и "Старт" активными
                    // и не трогаем кнопку "Стоп"
                    //testAlarmButton.isEnabled = true
                    //startAlarmButton.isEnabled = true
                    //stopAlarmButton.isEnabled = false

                    // Получаем интервал из текстового поля и разбираем его
                    val intervalString = alarmIntervalEditText.text.toString()
                    val intervalParts = intervalString.split(":")
                    if (intervalParts.size == 3) {
                        val intervalHours = intervalParts[0].toLongOrNull()
                        val intervalMinutes = intervalParts[1].toLongOrNull()
                        val intervalSeconds = intervalParts[2].toLongOrNull()

                        if (intervalHours != null && intervalMinutes != null && intervalSeconds != null) {
                            // Вычисляем общее время интервала в миллисекундах
                            val intervalMillis =
                                intervalHours * 3600000 + intervalMinutes * 60000 + intervalSeconds * 1000

                            // Поставляем задачу на паузу перед следующим воспроизведением (с интервалом)
                            handler.postDelayed({
                                playAlarm()
                            }, intervalMillis)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PlayAlarm", "Error playing alarm: ${e.message}")
                Toast.makeText(this, "Ошибка при проигрывании будильника", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Введите URL для запуска будильника", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAlarm() {
        // Останавливаем воспроизведение, если оно активно
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            // Активируем кнопку "Тестировать будильник" и "Старт", отключаем кнопку "Стоп"
            testAlarmButton.isEnabled = true
            startAlarmButton.isEnabled = true
            stopAlarmButton.isEnabled = false
        }

        // Отменяем таймер будильника
        alarmTimer?.cancel()
    }

    private fun testAlarm() {
        val mp3Url = mp3UrlEditText.text.toString()

        if (mp3Url.isNotEmpty()) {
            try {
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(mp3Url)
                mediaPlayer?.prepare()
                mediaPlayer?.start()

                // Отключаем кнопку "Тестировать будильник" на время воспроизведения
                testAlarmButton.isEnabled = false
                // Активируем кнопку "Стоп"
                stopAlarmButton.isEnabled = true

                mediaPlayer?.setOnCompletionListener {
                    // Воспроизведение завершено, включаем кнопку "Тестировать будильник" и
                    // отключаем кнопку "Стоп"
                    testAlarmButton.isEnabled = true
                    stopAlarmButton.isEnabled = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TestAlarm", "Error testing alarm: ${e.message}")
                Toast.makeText(this, "Ошибка при тестировании будильника", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Введите URL для тестирования", Toast.LENGTH_SHORT).show()
        }
    }
}

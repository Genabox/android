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
    private lateinit var hideButton: Button
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
        hideButton = findViewById(R.id.hideButton)
        exitButton = findViewById(R.id.exitButton)

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

        hideButton.setOnClickListener {
            moveTaskToBack(true) // Спрятать приложение
        }

        exitButton.setOnClickListener {

            finish()
            System.out.close()
        }
    }

    private fun loadSettings() {
        val assetManager: AssetManager = resources.assets

        try {
            // Открываем и читаем файл settings.json из папки assets
            val inputStream = assetManager.open("settings.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            // Преобразуем байты в строку JSON
            val json = String(buffer, Charsets.UTF_8)

            // Разбираем JSON
            val jsonObject = JSONObject(json)

            val mp3Url = jsonObject.getString("mp3_url")
            val alarmTime = jsonObject.getString("alarm_time")
            val alarmInterval = jsonObject.getString("alarm_interval")

            // Теперь у вас есть значения mp3Url и alarmTime, которые вы можете использовать
            // для настроек вашего приложения.
            mp3UrlEditText.setText(mp3Url)
            alarmTimeEditText.setText(alarmTime)
            alarmIntervalEditText.setText(alarmInterval)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("LoadSettings", "Error loading settings: ${e.message}")
        }
    }

    private fun saveSettings() {
        val mp3Url = mp3UrlEditText.text.toString()
        val alarmTime = alarmTimeEditText.text.toString()

        val settings = JSONObject()
        settings.put("mp3_url", mp3Url)
        settings.put("alarm_time", alarmTime)
        val alarmInterval = alarmIntervalEditText.text.toString()
        settings.put("alarm_interval", alarmInterval)


        try {
            // Создаем или перезаписываем файл settings.json во внутреннем хранилище приложения
            val settingsFile = File(filesDir, "settings.json")
            settingsFile.writeText(settings.toString())

            // Выводим сообщение об успешном сохранении в logcat
            Log.d("SaveSettings", "Настройки успешно сохранены")
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SaveSettings", "Error saving settings: ${e.message}")
            Toast.makeText(this, "Ошибка при сохранении настроек", Toast.LENGTH_SHORT).show()
        }
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

# Android Alarm Service App

This repository contains an Android application that allows users to set alarms and play music at specified times. It provides two services for handling alarms, a user interface for configuring alarms, and the ability to test alarms.

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Getting Started](#getting-started)
- [Code Analysis](#code-analysis)
- [License](#license)

## Introduction

The Android Alarm Service App is designed to help users set and manage alarms easily. It offers a simple user interface for configuring alarm settings, including specifying the MP3 URL for the alarm sound, the alarm time, and the interval between alarms. Users can start, stop, and test alarms based on their settings.

## Features

- **Set Alarms**: Users can configure alarms by providing an MP3 URL, alarm time, and interval.
- **Start Alarm**: The app allows users to start an alarm based on the configured settings.
- **Stop Alarm**: Users can stop a currently running alarm.
- **Test Alarm**: The app provides a feature to test alarms by playing the alarm sound immediately.
- **Background Execution**: The app uses background services to ensure alarms work even when the app is not in the foreground.

## Getting Started

To use this Android Alarm Service App, follow these steps:

1. Clone or download this repository to your local machine.
2. Open the project in Android Studio.
3. Build and run the app on an Android emulator or device.

## Code Analysis

### `MyAlarmService` Class

- `onCreate()`: Initializes the `MyAlarmService` with a `MediaPlayer` and acquires a wake lock.
- `onStartCommand()`: Starts the service as a foreground service and is responsible for handling alarm triggers and music playback.
- `onDestroy()`: Stops and releases the `MediaPlayer` and releases the wake lock.
- `onBind()`: Returns the binder for the service.
- `createNotification()`: Creates a notification for the foreground service.

### `AlarmService` Class

- `onCreate()`: Initializes the `AlarmService` with a `MediaPlayer` (Unused in the provided code).
- `onStartCommand()`: Handles alarm triggers (Unused in the provided code).
- `onDestroy()`: Stops and releases the `MediaPlayer` (Unused in the provided code).

### `MainActivity` Class

- `onCreate()`: Initializes the main activity, sets up UI elements, and loads settings.
- `loadSettings()`: Loads user settings from shared preferences.
- `saveSettings()`: Saves user settings to shared preferences.
- `startAlarm()`: Starts an alarm based on user-configured settings.
- `playAlarm()`: Plays the alarm sound and handles interval-based alarm repetitions.
- `stopAlarm()`: Stops a running alarm.
- `hideAlarm()`: Minimizes the app to the background.
- `testAlarm()`: Tests the alarm by playing the sound immediately.

## License

This project is licensed under the MIT License. See the [LICENSE] https://github.com/Genabox/Android/blob/main/LICENSE file for details.

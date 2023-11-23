[app]

# Название вашего приложения
title = Callboy

# Пакетное имя (package name) вашего приложения
package.name = callboy

# Список зависимостей вашего приложения
requirements = python3,kivy

# Ориентация экрана (portrait - вертикальная, landscape - горизонтальная)
orientation = portrait

# Разрешения
android.permissions = INTERNET,ACCESS_NETWORK_STATE

# Архитектура Android (armeabi-v7a, arm64-v8a, x86, x86_64)
android.arch = armeabi-v7a, arm64-v8a

# Имя файла иконки для вашего приложения
icon.filename = icons/ic_launcher-web.png

# Версия приложения
version = 1.0

[buildozer]

# Версия Android NDK
ndk = 22.0.7026061

# Архитектуры для сборки (armeabi-v7a, arm64-v8a, x86, x86_64)
arch = armeabi-v7a, arm64-v8a

# Режим сборки (release - окончательная сборка)
mode = release

# Путь к ключу подписи
sign.keystore = keystore/my-release-key.keystore

# Пароль к ключу подписи
sign.keystore.password = password

# Алиас ключа
sign.key_alias = myapp

# Пароль к алиасу ключа
sign.key_alias.password = password

# Пароль к коду алиаса ключа
sign.key_alias.password = password

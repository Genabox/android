import os
import requests
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.button import Button
from kivy.uix.image import Image
from kivy.core.window import Window
import json
from datetime import datetime
from kivy.clock import Clock
from kivy.core.audio import SoundLoader

#from kivy.config import Config
#Config.set('kivy', 'clipboard', 'sdl2')

# Путь к файлу с настройками
SETTINGS_FILE = os.path.join(os.path.dirname(os.path.realpath(__file__)), "settings.json")

class AlarmApp(App):
    def build(self):
        self.layout = BoxLayout(orientation='vertical', spacing=10, padding=10)
        self.layout.background_color = [0, 0, 0, 1]  # Черный фон
        self.layout.border = [30, 30, 30, 30]  # Закругление углов

        # Устанавливаем иконку приложения с помощью Window
        Window.set_icon('icons/ic_launcher-web.png')

        self.label = Label(text='Адрес mp3-файла:')
        self.layout.add_widget(self.label)

        self.mp3_input = TextInput(hint_text='Введите адрес mp3-файла')
        self.layout.add_widget(self.mp3_input)

        self.label = Label(text='Время срабатывания (HH:MM):')
        self.layout.add_widget(self.label)

        self.time_input = TextInput(
            hint_text='Введите время (например, 07:30)',
            font_size="24sp",
            halign="center",  # Центрирование текста по горизонтали
            foreground_color=[0.2, 0.2, 0.2, 1]  # Темно-серый цвет шрифта
        )
        self.layout.add_widget(self.time_input)

        self.save_button = Button(text='Save Settings', on_press=self.save_settings)
        self.layout.add_widget(self.save_button)

        self.hide_button = Button(text='Hide', on_press=self.hide_app)
        self.layout.add_widget(self.hide_button)

        self.stop_button = Button(text='Stop', on_press=self.stop_alarm)
        self.layout.add_widget(self.stop_button)
        self.stop_button.disabled = True

        self.test_alarm_button = Button(text='Test alarm', on_press=self.test_alarm)
        self.layout.add_widget(self.test_alarm_button)

        self.audio = None
        self.scheduled_event = None

        # Загружаем настройки при запуске приложения
        self.load_settings()

        # Запускаем функцию для проверки времени
        Clock.schedule_interval(self.check_alarm_time, 60)  # Проверяем каждую минуту

        return self.layout

    def load_settings(self):
        try:
            # Прочитаем настройки из файла
            with open(SETTINGS_FILE, 'r') as file:
                settings = json.load(file)
                self.mp3_input.text = settings.get('mp3_url', '')
                self.time_input.text = settings.get('alarm_time', '')
        except FileNotFoundError:
            pass

    def save_settings(self, instance):
        mp3_url = self.mp3_input.text
        alarm_time = self.time_input.text

        settings = {
            'mp3_url': mp3_url,
            'alarm_time': alarm_time
        }

        with open(SETTINGS_FILE, 'w') as file:
            json.dump(settings, file)

    def hide_app(self, instance):
        self.root_window.minimize()

    def on_pause(self):
        return True

    def on_resume(self):
        pass

    def check_alarm_time(self, dt):
        # Получаем текущее время
        now = datetime.now().time()
        current_time = f"{now.hour:02}:{now.minute:02}"

        # Получаем время срабатывания из настроек
        alarm_time = self.time_input.text

        # Сравниваем текущее время с временем срабатывания
        if current_time == alarm_time:
            mp3_url = self.mp3_input.text
            self.play_alarm(mp3_url)

    def play_alarm(self, mp3_url):
        try:
            print(f"Alarm triggered. Playing alarm sound from {mp3_url}")

            # Получаем настройки из файла
            settings = json.load(open(SETTINGS_FILE))
            mp3_filename = settings.get('mp3_filename', 'default_sound.mp3')

            # Скачиваем mp3-файл
            response = requests.get(mp3_url)
            mp3_file = os.path.join(os.path.dirname(os.path.realpath(__file__)), mp3_filename)
            with open(mp3_file, 'wb') as file:
                file.write(response.content)

            # Проигрываем скачанный mp3-файл
            self.audio = SoundLoader.load(mp3_file)
            if self.audio:
                self.audio.bind(on_stop=self.play_alarm_repeat)  # Слушаем событие завершения воспроизведения
                self.audio.play()
                self.stop_button.disabled = False
        except Exception as e:
            print(f"Error playing alarm sound: {str(e)}")

    def play_alarm_repeat(self, instance):
        if self.audio:
            self.audio.play()

    def stop_alarm(self, instance):
        if self.audio is not None:
            self.audio.stop()
            self.audio.unload()
            self.stop_button.disabled = True

    def test_alarm(self, instance):
        mp3_url = self.mp3_input.text
        self.play_alarm(mp3_url)

if __name__ == '__main__':
    AlarmApp().run()

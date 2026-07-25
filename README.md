# 🧭 Wear Compass & GPS Waypoint Navigator

[![Wear OS](https://img.shields.io/badge/Platform-Wear%20OS%204-blue.svg)](https://developer.android.com/wear)
[![Release](https://img.shields.io/badge/Release-v1.0.0-green.svg)](https://github.com/ajimsjames/WearCompass/releases/tag/v1.0.0)
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)

An offline **360° circular hardware compass** and **GPS waypoint navigation app** designed specifically for Wear OS smartwatches (Samsung Galaxy Watch 4/5/6, Pixel Watch, TicWatch). Built with Kotlin and Jetpack Compose for Wear OS by **Aju George**.

---

## ✨ Features

* **🧭 Live 360° Hardware Compass Dial**: Low-pass filtered smooth rotation vector sensor with Cardinal directions (N, E, S, W) and degree tick marks.
* **📍 Offline GPS Waypoint Saver**: Save your current location instantly without internet connection (e.g. Parked Car 🚗, Hotel 🏨, Campsite 🏕️).
* **🟢 Target Guidance Arrow**: Dynamic target arrow pointing directly toward your selected saved waypoint with live distance readout (in meters / kilometers).
* **⚡ Battery-Efficient OLED Theme**: Minimal black background designed for AMOLED smartwatch displays to conserve battery.
* **⌚ Native Circular Design**: Tailored layout with zero bezel clipping on 480x480 circular smartwatch displays.

---

## 📸 Overview

* **Compass Screen**: Real-time sensor rotation with live bearing degree reading.
* **Save Spot Modal**: Custom waypoint name input & instant location lock.
* **Waypoints Picker**: Select target destination or delete saved waypoints.

---

## 🚀 Installation

### Option 1: Direct ADB Wireless Install
Download the pre-compiled [`app-release.apk`](https://github.com/ajimsjames/WearCompass/releases/download/v1.0.0/app-release.apk) from the [v1.0.0 Release Page](https://github.com/ajimsjames/WearCompass/releases/tag/v1.0.0) and install over ADB:

```bash
adb connect <your-watch-ip>:5555
adb install -r app-release.apk
```

### Option 2: Build from Source
```bash
git clone https://github.com/ajimsjames/WearCompass.git
cd WearCompass
./gradlew assembleRelease
```
The APK will be generated at `app/build/outputs/apk/release/app-release.apk`.

---

## 🛠️ Tech Stack & Requirements

* **Platform**: Wear OS 3.0+ (API 30+)
* **Language**: Kotlin 1.9
* **UI Framework**: Jetpack Compose for Wear OS
* **Sensory APIs**: `Sensor.TYPE_ROTATION_VECTOR`, `Sensor.TYPE_MAGNETIC_FIELD`, `LocationManager` (GPS)

---

## 👨‍💻 Developer & Author

* **Author**: Aju George ([@ajimsjames](https://github.com/ajimsjames))
* **Email**: `ajimsjames@gmail.com`

---

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

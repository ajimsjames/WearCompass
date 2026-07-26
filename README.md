# 🧭 Wear Compass (v1.1.0)

**360° Circular Digital Compass & Target Waypoint Navigation for Wear OS (Samsung Galaxy Watch 6)**

Developed by **Aju George**.

---

## ✨ Features

- 🧭 **360° Real-Time Azimuth Needle**: Displays live cardinal headings (`N, NE, E, SE, S, SW, W, NW`) powered by smartwatch hardware magnetometer & accelerometer sensors.
- 🎯 **Target Waypoint Direction Pointer**: Shows a glowing green arrow pointing directly toward saved waypoints (e.g. Parked Car 🚗, Hotel 🏨, Camp Site 🏕️).
- 📳 **Haptic Alignment Feedback**: Vibrates watch motor when your wrist aligns within ±5° of target destination.
- 🌐 **Magnetic Declination Correction**: Adjusts magnetic north to true geographical north using device GPS location.
- ⭕ **Bezel-Aligned Navigation & About Dialog**: Curved top navigation bar (`CurvedLayout`) with About App screen and One UI squircle launcher icon.

---

## 🛠️ Architecture & Tech Stack

- **Framework**: Android Wear OS (Min SDK 30 / Target SDK 33)
- **UI Engine**: Wear Compose + Jetpack Compose + CurvedLayout
- **Hardware Integration**: Android SensorManager + LocationManager + Vibrator.

---

## 📦 Installation

```bash
# Connect to Galaxy Watch 6 via Wireless ADB
adb connect <WATCH_IP>:<PORT>

# Build and Install Release APK
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 📄 License & Credits

Created and maintained by **Aju George**. Distributed for Wear OS devices.

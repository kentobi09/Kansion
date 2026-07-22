# Kansion - Ilokano Offline Music Player

**Kansion** is a premium, distraction-free offline music player for Android, fully localized in the **Ilokano dialect**. It features a modern slate-black and gold aesthetic with an immersive full-screen lyrics visualizer, dynamic OPM-optimized fallback search, and an interactive rotating vinyl interface. Designed with simplicity in mind, it provides a tailored, familiar listening experience for the Ilokano-speaking community.

[![Download APK](https://img.shields.io/badge/📥_Download_APK-latest-F59E0B?style=for-the-badge&logo=android&logoColor=white)](https://github.com/kentobi09/Kansion/raw/main/kansion.apk)

[**👉 Click here to Download the latest Kansion APK**](https://github.com/kentobi09/Kansion/raw/main/kansion.apk)

---

## 📱 Features

* **Dialect Localization**: Native Ilokano phrasing across the interface (e.g., *Kankansion* for Songs, *Listaan* for Playlists, *Ur-uray* for Queue, *Pinagpaut* for Duration, *Denggeg* for Volume).
* **Full-Screen Spotify-Style Lyrics**: 
  * Access lyrics via the playback kebab menu (**`Liriko`**).
  * Opens a beautiful full-screen scrollable lyrics sheet with large bold typography and a vertical gradient them.
  * Finds the lyrics for you online or paste the lyrics.
* **Settings Dashboard**:
  * **Rupan Kansion (App Theme)**: Light/Dark mode toggle (saved to preferences to persist across app restarts).
  * **Oras ti Turog (Sleep Timer)**: Configurable background sleep timer (15m, 30m, 45m, 60m) with a real-time countdown.
* **Simultaneous Playback**: Disabled automatic audio focus pause constraints. You can play your local music continuously in the background—even during active voice/video calls or even with other applications playing sound.

---

## 📥 Quick Download & Installation

1. **Download APK**: Click the [**Download APK Badge**](https://github.com/kentobi09/Kansion/raw/main/kansion.apk) or [**Direct Link**](https://github.com/kentobi09/Kansion/raw/main/kansion.apk) above on your Android phone.
2. **Install**: Tap the downloaded `kansion.apk` file to install it. (You may need to tap "Install Anyway" if prompted by Play Protect).
3. **Enjoy**: Open **Kansion** and enjoy your local offline music with lyrics and native translation!

---

## 🛠️ Tech Stack

* **Language**: Kotlin
* **UI Toolkit**: Jetpack Compose (Material 3)
* **Media Framework**: Jetpack Media3 (ExoPlayer + MediaSession)
* **Database**: SQLite (custom DatabaseHelper helper class)
* **Dependency Engine**: Gradle (Groovy DSL)
* **Image Rendering**: Localized custom assets and embedded media retriever bitmaps

---

## 🚀 Building from Source

### Prerequisites
* [Android Studio](https://developer.android.com/studio) (Hedgehog or newer)
* Android Device or Emulator running Android 8.0+

### Steps
1. **Clone the repository**:
   ```bash
   git clone https://github.com/kentobi09/Kansion.git
   ```
2. **Open in Android Studio**:
   - Open Android Studio -> **Open** -> Select the `kansion` project folder.
3. **Build and Run**:
   - Connect your Android device via USB.
   - Press the green **Run (▶)** button to compile and install on your phone.

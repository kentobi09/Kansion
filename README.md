# Kansion - Ilokano Offline Music Player

**Kansion** is an elegant, premium offline music player built for Android. The entire application is fully localized in the **Ilokano dialect**, providing a tailored, familiar experience for Ilokano-speaking users. It features a slate-black and golden aesthetic, highlighted by a tactile rotating vinyl record interface.

---

## 📱 Features

* **Dialect Localization**: Native Ilokano phrasing across the interface (e.g., *Kankansion* for Songs, *Listaan* for Playlists, *Ur-uray* for Queue, *Pinagpaut* for Duration, *Denggeg* for Volume).
* **Oras ti Turog (Sleep Timer)**: Configurable background sleep timer (15m, 30m, 45m, 60m) with a real-time countdown.
* **Simultaneous Playback**: Disabled automatic audio focus pause constraints. You can play your local music continuously in the background—even during active voice/video calls.

---

## 🛠️ Tech Stack

* **Language**: Kotlin
* **UI Toolkit**: Jetpack Compose (Material 3)
* **Media Framework**: Jetpack Media3 (ExoPlayer + MediaSession)
* **Database**: SQLite (custom DatabaseHelper helper class)
* **Dependency Engine**: Gradle (Groovy DSL)
* **Image Rendering**: Localized custom assets and embedded media retriever bitmaps

---

## 📦 How to Build & Run

### Running the precompiled APK
A signed release package is compiled and available at the root directory for easy installation:
👉 **[kansion.apk](kansion.apk)**

### Opening in Android Studio
1. Clone the repository and open Android Studio.
2. Select **File ➔ Open...** and target the project folder.
3. Wait for the automatic Gradle Sync to complete.
4. Press the green **Run (▶)** button to deploy to an emulator or connected physical device.

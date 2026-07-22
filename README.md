# Kansion - Ilokano Offline Music Player

**Kansion** is an elegant, premium offline music player built for Android. The entire application is fully localized in the **Ilokano dialect**, providing a tailored, familiar experience for Ilokano-speaking users. It features a slate-black and golden aesthetic, highlighted by a tactile rotating vinyl record interface.

---

## 📱 Features

* **Dialect Localization**: Native Ilokano phrasing across the interface (e.g., *Kankansion* for Songs, *Listaan* for Playlists, *Ur-uray* for Queue, *Pinagpaut* for Duration, *Denggeg* for Volume).
* **Tactile Vinyl Disk Representation**: Smoothly rotating record visual representation that animates in sync with playback.
* **Full-Screen Spotify-Style Lyrics**: 
  * Access lyrics via the playback kebab menu (**`Liriko`**).
  * Opens a beautiful full-screen scrollable lyrics sheet with large bold typography and a vertical gradient theme.
  * Pinned bottom playback controls (Previous, Play/Pause, Next) let you control music without exiting the lyrics view.
* **Lyrics Search, Caching & Editing**:
  * **Online Search**: Queries the free, public *LRCLIB* database and caches matches locally in SQLite (v2 database upgrade).
  * **Manual Editing & Pasting**: Allows you to paste new lyrics or correct existing cached lyrics in-app using a pre-filled text editor.
  * **Smart Query Sanitization**: Cleans underscores, strips extensions (`.mp3`), and removes clutter tags (like `[Lyrics]` or `(Official Video)`). Auto-splits combined filenames (like `Artist - Title` or `Title by Artist`) when tags are missing, maximizing lyrics match accuracy.
* **Kebab Options Menu**: A vertical three-dot option menu inside the playback screen header grouping volume controls (*Denggeg*), playlist additions (*Inayon iti listaan*), and lyrics display (*Liriko*), keeping the interface clean and symmetrical.
* **10-Song Playback Queue Cap**: Automatically loads the clicked song + the next 9 sequential songs when playing from the main list. This keeps your queue compact and prevents interface lag on devices with large libraries.
* **Settings Dashboard**:
  * **Rupan Kansion (App Theme)**: Light/Dark mode toggle (saved to preferences to persist across app restarts).
  * **Oras ti Turog (Sleep Timer)**: Configurable background sleep timer (15m, 30m, 45m, 60m) with a real-time countdown.
  * **Sukiren manen dagiti kansion (Rescan)**: Instantly rescans local files to update the audio library.
  * **Pukawen ti Data (Reset Data)**: Localized warning dialog to securely wipe database playlists and queue states.
* **Simultaneous Playback**: Disabled automatic audio focus pause constraints. You can play your local music continuously in the background—even during active voice/video calls.
* **Headphone Unplug Pausing**: Automatically pauses playback when headphones are unplugged to prevent accidental speaker output.

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

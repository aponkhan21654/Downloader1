# 🚀 Apon Downloader — Watermark-Free Media Downloader for Android

A sleek, highly responsive, and high-performance Android application built with modern **Jetpack Compose**, **Kotlin**, **Room Database**, and **Retrofit**. It enables users to download watermark-free video and audio directly from platforms like **YouTube, Instagram, TikTok, and Facebook** by either pasting links or using the native Android system share sheet!

---

## 🎨 Visual Preview & Design Concept

Designed with an **Obsidian Deep Space** dark aesthetic, featuring vibrant glowing teal, electric indigo accents, and hot pink highlights. The layout includes spacious padding, beautiful platform cards, and responsive progress tracking.

### Core Features

-   **🔄 Direct System Share Integration:** Share any video from YouTube, TikTok, Instagram, or Facebook directly to *Apon Downloader* and it will automatically extract and begin the download!
-   **📥 Manual Link Entry:** A dedicated clipboard paste field inside the app allows copy-pasting links manually.
-   **⚙️ Settings Panel:**
    -   Configure **Video Quality** resolutions (1080p, 720p, 480p, 360p).
    -   Toggle **Audio Only (MP3)** mode to strip video and keep high-quality audio tracks.
    -   Customize the backend **Cobalt API Server Instance** (easily swap between public instances or point to your own self-hosted deployment).
-   **📱 Android System Integration:** Integrates natively with Android’s system `DownloadManager` for high-speed background transfers, auto-resuming, and statusbar notifications.
-   **🗄️ Local Persistence (Room):** Saves your download history in a fast, lightweight local SQLite database (via Room) so you can access, copy, or share previous download paths.
-   **📣 Community Popup:** Includes a beautiful Telegram channel popup on startup linking to `https://t.me/TeamWithApon`.

---

## 🛠️ Architecture & Tech Stack

Apon Downloader is engineered using industry-standard **Android MVVM Clean Architecture** pattern:

*   **UI Framework:** Jetpack Compose (Material Design 3)
*   **Asynchronous Logic:** Kotlin Coroutines & Flow
*   **Database:** Room Database (SQLite)
*   **Networking:** Retrofit 2 + Moshi (JSON Converter)
*   **Media Downloader Backend:** Powered by the open-source **Cobalt API** engine.

---

## 📦 Project Directory Structure

```text
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml          # Declares system network, storage & share intent filters
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt          # Manages incoming Share intents and edge-to-edge screens
│   │   │   │   ├── data/
│   │   │   │   │   ├── database/            # Room Database: AppDatabase, DownloadDao, DownloadRecord
│   │   │   │   │   ├── network/             # Retrofit: CobaltApiService, CobaltRequest, CobaltResponse
│   │   │   │   │   └── repository/          # DownloadRepository handles API queries & DownloadManager binding
│   │   │   │   └── ui/
│   │   │   │       ├── screens/
│   │   │   │       │   ├── DownloadViewModel.kt  # Exposes flows for UI state, link extraction & tracker poller
│   │   │   │       │   └── MainScreen.kt         # Beautiful, fully-accessible Jetpack Compose screens
│   │   │   │       └── theme/               # Dark Space Obsidian Theme typography, custom colors & shapes
│   │   │   └── res/                         # Vector assets, XML settings, dynamic high-contrast app icon
│   │   └── test/                            # Roborazzi screenshot and local Robolectric JVM tests
│   └── build.gradle.kts                     # App module build configuration
├── build.gradle.kts                         # Root build configuration
├── settings.gradle.kts                      # Module setup and repo routing
└── gradle/libs.versions.toml                # Dependency version catalog
```

---

## ⚙️ Building and Running Locally

To run this project on your machine, follow these simple steps:

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/yourusername/apon-downloader.git
    cd apon-downloader
    ```

2.  **Open in Android Studio:**
    -   Launch **Android Studio (Ladybug or newer)**.
    -   Select **Open** and choose the root directory of the cloned project.
    -   Let Gradle sync finish downloading the necessary dependencies.

3.  **Run the App:**
    -   Connect your physical device or start an emulator.
    -   Click the **Run** button (green play icon) in the Android Studio toolbar.

---

## 🤝 Contribution Guidelines

Contributions are welcome! If you'd like to improve the UI, add support for more APIs, or fix a bug:
1.  Fork the repository.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

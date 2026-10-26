# 🎲 LAN Bingo

**LAN Bingo** is an offline, peer-to-peer (P2P) local multiplayer Bingo game built for Android using modern Jetpack Compose and Material Design 3. Play seamlessly with friends and family across Wi-Fi or portable mobile hotspot without needing an active internet connection!

---

## 🌟 Key Features

- **🌐 Zero-Internet Local Multiplayer**:
  - Automatically discovers rooms on the same local network using UDP broadcast / multicast sockets.
  - Direct peer-to-peer TCP communication for low-latency synchronized gameplay.
- **📷 Instant QR Code Pairing**:
  - Hosts can show an in-app QR code encoding their IP address and port.
  - Clients can scan the QR code using the built-in CameraX scanner or manually enter IP addresses.
- **🎯 Multiple Game Modes**:
  - **Classic 5×5 Bingo**: 25 cells, lines to win B-I-N-G-O.
  - **ExtraBingo 10×10**: 100 cells for longer, strategic sessions.
- **🤖 Offline Solo & Bot Matches**:
  - Play solo practice games or against smart AI opponents with varying difficulty levels (Easy, Medium, Hard).
- **⏱️ Real-time Turn Mechanics & Timers**:
  - Synchronized turns, turn indicators, turn time limits, and optional auto-draw countdown timer.
- **📊 Stats & Career History**:
  - Tracks games played, wins, win streaks, match history, and detailed post-match victory dialogs.
- **🎨 Modern Material 3 UI & Themes**:
  - Clean edge-to-edge layout, responsive across phones, foldables, and tablets.
  - Dark Mode and Vibrant Lime themes with smooth animations and tactile audio/haptic feedback.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel) + StateFlow & Coroutines
- **Networking**: Kotlin Coroutines with Java Sockets (`ServerSocket`, `Socket`, `DatagramSocket`) for custom JSON-serialized P2P protocol
- **Vision & Scanning**: CameraX + Google ML Kit Barcode Scanning
- **QR Generation**: ZXing Core
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`gradle/libs.versions.toml`)

---

## 📂 Project Structure

```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/            # Local data sources, preferences & career stats
│   │   │   │   ├── model/           # Game models, board state, network packets
│   │   │   │   ├── network/         # P2P Server, Client & UDP discovery engine
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/  # Bingo boards, QR dialogs, audio effects
│   │   │   │   │   ├── screens/     # Home, Lobby, Game, Settings, Stats
│   │   │   │   │   └── theme/       # Material 3 colors, shapes, typography
│   │   │   │   └── viewmodel/       # BingoViewModel & state management
│   │   │   ├── res/                 # Vector drawables, mipmaps, strings
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # Unit tests & Robolectric test suite
├── gradle/
│   ├── libs.versions.toml           # Version catalog for dependencies & plugins
│   └── wrapper/                     # Gradle wrapper binary and configuration
├── gradlew                          # Linux/macOS Gradle wrapper executable
├── gradlew.bat                      # Windows Gradle wrapper executable
├── build.gradle.kts                 # Root project build configuration
├── settings.gradle.kts              # Project settings & dependency repositories
└── README.md
```

---

## 🚀 Getting Started & Building

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java 17 or higher
- **Android SDK**: Min SDK 24 (Android 7.0), Target/Compile SDK 34–36

### Quick Build via Command Line

1. Clone or extract this repository:
   ```bash
   git clone https://github.com/<your-username>/lan-bingo.git
   cd lan-bingo
   ```

2. Build the Debug APK:
   - **Linux / macOS**:
     ```bash
     ./gradlew assembleDebug
     ```
   - **Windows**:
     ```bat
     gradlew.bat assembleDebug
     ```

3. The generated APK will be located at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

4. Or simply open the project folder directly in **Android Studio** and click **Run ▶**.

---

## 🎮 How to Play Local Multiplayer

1. **Connect Devices**:
   - Make sure all players are connected to the **same Wi-Fi network**, OR
   - One player turns on their **Mobile Hotspot**, and the other players connect to that Hotspot (no mobile data required!).
2. **Host a Room**:
   - Player 1 taps **"Host Game"**, selects the mode (5×5 or 10×10), and enters the Lobby.
3. **Join the Room**:
   - Player 2 opens the app and either:
     - Sees the host's room appear automatically via **Auto-Discovery**, OR
     - Taps **"Scan QR"** and points the camera at Player 1's QR code, OR
     - Taps **"Direct IP"** and enters Player 1's local IP address.
4. **Play**:
   - Players mark ready, the Host starts the match, and turns sync in real time!

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

# 🏎️ ApexWheel: Mobile Racing Controller for PC Games

ApexWheel turns your Android smartphone into a high-precision, low-latency wireless racing wheel and gamepad controller for PC racing games (*Forza Horizon*, *Assetto Corsa*, *F1 24*, *BeamNG.drive*, *Need for Speed*, *Project CARS*, etc.) or any driving game with support for controllers.

By combining Android's fused motion sensors (rotation vector / 6-DOF gyroscope) with an authentic Formula 1 cockpit interface, ApexWheel converts phone tilt, analog pedal pressures, and rapid tactile button presses into standard **virtual Xbox 360 controller (XInput)** inputs recognized automatically by Windows games.

---

## 📑 Table of Contents (Index)

Jump directly to any section:

1. [⚡ Quick Start Guide (3-Minute Setup)](#quick-start-guide)
2. [📋 System Requirements & Prerequisites](#system-requirements)
3. [🖥️ Step 1: Setting Up the PC Companion](#step-1-pc-companion)
   - [Windows Installation (Gaming Mode with ViGEmBus)](#windows-installation)
   - [Linux / macOS Installation (Developer / Mock Mode)](#linux-macos-installation)
   - [Running the PC Companion](#running-the-pc-companion)
   - [Firewall & Network Configuration](#firewall-network-configuration)
4. [📱 Step 2: Setting Up the Android App](#step-2-android-app)
   - [Option A: Install Pre-Built APK (Recommended)](#option-a-install-apk)
   - [Option B: Build from Source with Gradle](#option-b-build-source)
5. [🔗 Step 3: Connecting Phone to PC](#step-3-connecting)
   - [Method 1: Instant QR Code Scan (Fastest Wireless)](#method-1-qr-scan)
   - [Method 2: Manual IP Entry (Wi-Fi)](#method-2-manual-ip)
   - [Method 3: Wired USB Cable (<1ms Latency, Zero Jitter)](#method-3-wired-usb)
6. [🎮 Step 4: Configuring Your PC Games](#step-4-game-config)
7. [🕹️ Cockpit Interface & Input Mappings](#cockpit-interface)
   - [Default Xbox 360 Controller Mapping](#default-mapping)
   - [Cockpit Features & Layout](#cockpit-layout)
   - [F1 Team Liveries & Steering Wheels](#f1-liveries)
8. [🎯 Sensor Calibration & Tuning](#calibration-tuning)
9. [🛡️ Disconnect Safety & Failsafe Watchdog](#disconnect-safety)
10. [🔧 Troubleshooting & Common Issues](#troubleshooting)
11. [🧪 Running Unit Tests & Verification](#unit-tests)
12. [📁 Project Architecture & Directory Layout](#project-architecture)

---

<a id="quick-start-guide"></a>
## ⚡ Quick Start Guide (3-Minute Setup)

For players who want to jump into a race immediately:
1. **Make sure to download run_pc_companion.exe from release section in this github repo**
2. **Run the exe"
3. **Phone**: Install `Apex-Wheel.apk` onto your Android phone from release section from this github page and open **ApexWheel**.
4. **Connect**: Tap the connection badge on your phone,to connect with wired enable usb debugging from developer mode in your android phone and connect it to pc via usb cable, or if wirelessly tap **Scan PC QR Code**, and point your camera at the QR code in your PC terminal or on `http://localhost:8080`.
5. **Race**: Hold your phone horizontally, tap **Calibrate**, launch your favorite racing game, and drive!

---

<a id="system-requirements"></a>
## 📋 System Requirements & Prerequisites

### PC Side
- **OS**: Windows 10 / 11 (64-bit) for physical gaming. *(Linux & macOS supported for development / mock mode).*
- **Python**: Python 3.10 or newer ([python.org](https://www.python.org/downloads/)).
- **Driver (Windows only)**: [ViGEmBus v1.22+](https://github.com/nefarius/ViGEmBus/releases) to emulate physical Xbox 360 controller hardware.
- **Network**: Wi-Fi network shared with your Android phone.

### Android Device Side
- **OS**: Android 10.0+ (API Level 29+).
- **Hardware**: Device with an internal Gyroscope and Accelerometer (virtually all modern smartphones).
- **Permissions**: Camera permission (required for scanning the connection QR code).

### For Developers Building from Source
- **JDK**: Java Development Kit 21 (JDK 21 is strongly recommended; avoid JDK 25+ due to Gradle 8 compatibility).
- **Android SDK**: Android SDK Build-Tools 34+ / Platform SDK 36.

---

<a id="step-1-pc-companion"></a>
## 🖥️ Step 1: Setting Up the PC Companion

The PC Companion listens for UDP/TCP packets from your phone over your local network and forwards them into Windows as real Xbox 360 controller hardware events.

<a id="windows-installation"></a>
### Windows Installation (Gaming Mode with ViGEmBus)

1. **Install ViGEmBus**:
   - Download `ViGEmBus_Setup_x64.exe` from the [Official ViGEmBus Releases](https://github.com/nefarius/ViGEmBus/releases).
   - Run the installer and finish setup (requires administrator rights).
2. **Set up Python Virtual Environment (Recommended)**:
   Open Command Prompt or PowerShell in the project root:
   ```cmd
   cd pc_companion
   python -m venv venv
   .\venv\Scripts\activate
   ```
3. **Install Requirements**:
   ```cmd
   pip install -r requirements.txt
   ```
   *This installs `vgamepad`, `qrcode`, and other dependencies.*

<a id="linux-macos-installation"></a>
### Linux / macOS Installation (Developer / Mock Mode)

On Linux or macOS, physical Windows gamepad emulation is unavailable, but you can run in **Mock Mode** to test networking, telemetry, web dashboards, and JSON protocols:

```bash
cd pc_companion
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python run_pc_companion.py --mock
```

<a id="running-the-pc-companion"></a>
### Running the PC Companion

Start the companion application:

```bash
# Standard startup (Windows gaming mode)
python run_pc_companion.py

# Optional: Override listening port or web UI port
python run_pc_companion.py --port 9876 --web-port 8080
```

Once running, your terminal will display:
- The active Gamepad Driver (`ViGEmBus (Virtual Xbox 360 Gamepad)` or `Mock Output`).
- Your PC's Local Network IP Addresses (e.g. `192.168.1.150`).
- A clean **ASCII QR Code** directly in the terminal for instant mobile scanning.
- An embedded Web Dashboard URL: `http://localhost:8080`.

> [!TIP]
> Open `http://localhost:8080` in any web browser on your PC to view the live cockpit dashboard with real-time analog steering gauges, pedal meters, packet rates, and network ping diagnostics.

<a id="firewall-network-configuration"></a>
### Firewall & Network Configuration

When running for the first time, Windows Defender Firewall will prompt you:
- Check **"Private networks, such as my home or work network"**.
- Click **"Allow access"**.
- Ensure both PC and phone are connected to the same Wi-Fi router / SSID.
- If using public or university Wi-Fi with client isolation, enable a mobile hotspot on your phone or PC and connect both devices to it.

---

<a id="step-2-android-app"></a>
## 📱 Step 2: Setting Up the Android App

<a id="option-a-install-apk"></a>
### Option A: Install Pre-Built APK (Recommended)

1. Locate the compiled APK at:
   - `android_app/app/build/outputs/apk/debug/app-debug.apk`
   - or directly from your downloads folder if generated (`~/Downloads/app-debug.apk`).
2. **Install via ADB (Fastest)**:
   ```bash
   adb install -r android_app/app/build/outputs/apk/debug/app-debug.apk
   ```
3. **Or Transfer Directly**:
   Copy `app-debug.apk` to your phone via USB cable, Google Drive, or local file sharing, tap it in your phone's file manager, and choose **Install**.

<a id="option-b-build-source"></a>
### Option B: Build from Source with Gradle

If you are customizing code or adding features:

1. Open a terminal in `android_app/`.
2. Ensure `JAVA_HOME` points to JDK 21:
   ```bash
   export JAVA_HOME=/path/to/jdk-21
   ```
   *(Or set `org.gradle.java.home` in `android_app/gradle.properties`)*.
3. Build the debug package:
   ```bash
   ./gradlew assembleDebug
   ```
4. The output APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

<a id="step-3-connecting"></a>
## 🔗 Step 3: Connecting Phone to PC

<a id="method-1-qr-scan"></a>
### Method 1: Instant QR Code Scan (Fastest)

1. Launch **ApexWheel** on your phone.
2. Tap the **Connection Status Pill** (or gear icon) on the top-right HUD to open the **Connection Screen**.
3. Tap **"Scan PC QR Code"**.
4. Grant camera permission if requested.
5. Point your camera at either:
   - The QR code printed in your **PC Terminal**, or
   - The QR code shown on the Web Dashboard (`http://localhost:8080`).
6. The app instantly parses the IP address and port, connects within milliseconds, and returns you to the active cockpit screen!

<a id="method-2-manual-ip"></a>
### Method 2: Manual IP Entry

1. Look at the IP address printed in your PC companion console (e.g. `192.168.1.150`).
2. In the app's Connection Screen, type the IP into the **PC Server IP** field.
3. Keep default port `9876` (or enter your custom port).
4. Tap **"Connect to PC"**.
5. The status pill will switch from yellow `CONNECTING` to glowing green `CONNECTED (X ms)`.

<a id="method-3-wired-usb"></a>
### Method 3: Wired USB Cable (<1ms Latency, Zero Jitter)

For competitive esport racing, fast reflex reaction times, or environments with crowded/laggy Wi-Fi, you can connect your phone directly to your PC using a standard USB-C / USB cable.

#### Why Choose Wired USB?
- **Ultra-low latency**: Sub-millisecond round-trip response time (`< 0.5 ms` vs `10–30 ms` over Wi-Fi).
- **100% Reliability**: Zero packet jitter, zero frame drops, and immune to 2.4 GHz/5 GHz microwave or wall interference.
- **Works completely offline**: No local Wi-Fi router or internet access required.

#### Option A: Plug & Play via ADB Reverse (Recommended)
1. Connect your Android phone to the PC with a USB cable.
2. Ensure **USB Debugging** is turned on in your phone's Developer Options (*Settings > About Phone > tap 'Build Number' 7 times > System > Developer Options > USB Debugging*).
3. Start the PC Companion. The companion automatically detects your phone and executes:
   ```bash
   adb reverse tcp:9876 tcp:9876
   ```
4. Open the **ApexWheel** app on your phone, go to the Connection Screen, and simply tap:
   **`🔌 Connect via USB Cable (<1ms)`**
5. The app connects instantly to `127.0.0.1:9876` over the physical USB bus. The top HUD badge will display **`🔌 USB <1ms`**.

#### Option B: USB Tethering (No Developer Options Needed)
1. Connect your phone to your PC via USB cable.
2. On your phone, go to **Settings > Network & Internet > Hotspot & Tethering** and enable **USB Tethering**.
3. Windows and Linux will recognize a direct high-speed virtual network adapter (typically on subnet `192.168.42.x`).
4. The PC Companion automatically discovers this adapter IP and displays it in the terminal and on `http://localhost:8080`.
5. In the ApexWheel app, either tap **Scan PC QR Code** or enter the tethered IP address to connect directly over the USB wire.

---

<a id="step-4-game-config"></a>
## 🎮 Step 4: Configuring Your PC Games

Once connected, Windows registers a physical **Xbox 360 Controller for Windows**. No third-party key mapping software (JoyToKey, x360ce) is necessary.

1. **Verify Controller in Windows**:
   - Press `Win + R`, type `joy.cpl`, and press Enter.
   - You should see **"Controller (XBOX 360 For Windows)"** with status **OK**.
   - Double-click it to see live movement when you tilt your phone or press pedals.
2. **In Racing Games**:
   - Launch your game (*Forza Horizon 5*, *Assetto Corsa*, *F1 24*, *iRacing*, *BeamNG*, etc.).
   - Go to **Settings > Controls / Controller Settings**.
   - Select **Gamepad / Controller (Xbox 360)**.
   - The steering axis is automatically bound to **Left Stick X**, Acceleration to **Right Trigger (RT)**, and Brakes to **Left Trigger (LT)**.

---

<a id="cockpit-interface"></a>
## 🕹️ Cockpit Interface & Input Mappings

<a id="default-mapping"></a>
### Default Xbox 360 Controller Mapping

| Mobile Cockpit Control | Xbox 360 Gamepad Output | Behavior & Range |
|---|---|---|
| **Motion Steering Tilt** | **Left Thumbstick X** | Analog `-1.0` (Full Left) to `+1.0` (Full Right) |
| **Accelerator Button (Accl)** | **Right Trigger (RT)** | Analog `0.0` to `1.0` (mapped to `0..255`) |
| **Brake Button (Brake)** | **Left Trigger (LT)** | Analog `0.0` to `1.0` (mapped to `0..255`) |
| **Clutch Button** | **Button X** (or Left Bumper) | Digital clutch bite toggle |
| **E-Brake Button `(!)`** | **Button A** | Handbrake lock |
| **Gear Up Chevron `(▲)`** | **Right Bumper (`RB`)** | Sequential shift up |
| **Gear Down Chevron `(▼)`** | **Left Bumper (`LB`)** | Sequential shift down |
| **Select Pill** | **Back / View** | Telemetry / camera switch / replay |
| **Start Pill** | **Start / Menu** | Pause menu |

> [!NOTE]
> All gamepad button assignments can be freely remapped in `pc_companion/config.json`.

<a id="cockpit-layout"></a>
### Cockpit Features & Layout

```
┌─────────────────────────────────────────────────────────────────────────┐
│ [🏎️ F1 Logo] [Calibrate] [Sens: 1.0x]   [RPM REV LIGHTS]   [🟢 Connected]│
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ( BRAKE )                   ┌──────────────┐                 ( ACCL ) │
│   [ Crimson ]                 │ STEER  +0.0° │                 [Emerald]│
│                               └──────────────┘                          │
│                                                                         │
│   ( CLUTCH )                    ╭──────────╮                   ▲ GEAR UP│
│   [ U-Pedal ]                  │    F1    │                             │
│                                │  WHEEL   │                   ▼ GEAR DN │
│                                 ╰──────────╯                            │
│                               ( E-BRAKE [!] )                           │
│                                                                         │
│                              [ Select ] [ Start ]                       │
└─────────────────────────────────────────────────────────────────────────┘
```

- **RPM Shift Light Strip**: Dynamic 15-LED progressive rev bar at the top (Green $\rightarrow$ Red $\rightarrow$ Flashing Blue).
- **Steering Telemetry Badge (`STEER +0.0°`)**: Motec-style digital telemetry HUD positioned cleanly above the wheel with an active LED dot (emerald green when straight, team accent when turning).
- **Circular Racing Wheel**: Authentic circular rim with perforated leather grips, 12 o'clock racing stripe, 3-spoke carbon hub, and rotating official team emblem.
- **Expressive Tactile Buttons**: Spring-compressed micro-animations (`0.88f` scale), neon radiance bloom, and high-contrast illuminated glyphs when pressed.

<a id="f1-liveries"></a>
### F1 Team Liveries & Steering Wheels

Tap the team badge in the top-left HUD to open the **F1 Theme Selector**. Choose between 10 official team liveries with custom team colors and authentic vector emblems:

- **Red Bull Racing** (Matte Navy, Racing Yellow, Crimson Red)
- **Scuderia Ferrari** (Rosso Corsa, Italian Modena Yellow, White)
- **Mercedes-AMG Petronas** (Petronas Electric Cyan, Obsidian Black, Silver)
- **McLaren F1 Team** (Papaya Orange, Anthracite Grey, Sky Blue)
- **Aston Martin Aramco** (British Racing Green, Lime Essence, Silver)
- **BWT Alpine** (Alpine Racing Blue, BWT Vibrant Pink, Carbon)
- **Williams Racing** (Gulf Deep Navy, Heritage Light Blue)
- **Kick Sauber** (Neon Fluo Green, Stealth Carbon Black)
- **MoneyGram Haas** (Championship Red, Cool White, Charcoal)
- **Visa Cash App RB** (Toro Metallic Blue, Bright White, Bulls Red)

---

<a id="calibration-tuning"></a>
## 🎯 Sensor Calibration & Tuning

### Neutral Position Calibration
1. Sit in your driving posture and hold your phone horizontally at your most comfortable angle.
2. Tap the **Calibrate** button on the top HUD bar.
3. This sets your current tilt angle as `0.0°` (straight ahead), allowing you to hold your phone naturally without wrist fatigue.

### Customizing Steering Dynamics (Settings)
Tap the gear icon on the top bar to adjust:
- **Sensitivity (`0.5x` to `2.5x`)**: Controls how fast steering reacts. Use `0.8x - 1.0x` for smooth GT3 racing and `1.6x - 2.2x` for drifting / rallying.
- **Max Steering Angle (`±20°` to `±90°`)**: Degrees of physical phone tilt needed to reach full 100% steering lock (default `±45°`).
- **Dead Zone (`0%` to `15%`)**: Eliminates unintentional trembling when your hands are steady (default `3%`).
- **Smoothing Filter**: Low-pass exponential smoothing to filter out sensor noise without adding latency.
- **Touch Steering Toggle**: Switch between motion steering and dragging the wheel directly with your thumb.

---

<a id="disconnect-safety"></a>
## 🛡️ Disconnect Safety & Failsafe Watchdog

Safety under packet loss or Wi-Fi interference is paramount in sim racing:

- **500ms Watchdog Timer**: If the PC Companion misses packets for more than 500 ms (e.g. phone receives a call or drops Wi-Fi), the watchdog immediately triggers an emergency neutral state:
  - Steering instantly returns to center (`0.0`).
  - Throttle and Brake are reset to `0.0` (preventing stuck accelerator crashes).
  - All pressed buttons are immediately released.
- **Graceful Shutdown**: When you close the phone app or stop the PC companion (`Ctrl + C`), the virtual controller is reset cleanly and unmounted from Windows without leaving phantom inputs.

---

<a id="troubleshooting"></a>
## 🔧 Troubleshooting & Common Issues

### 1. Phone Cannot Connect to PC / Connection Timed Out
- **Check Wi-Fi Network**: Ensure both your phone and PC are connected to the exact same Wi-Fi router (not one on cellular data and one on Wi-Fi).
- **Windows Firewall**: Make sure Python has inbound permissions on private networks. You can run this in administrator PowerShell to unblock port `9876`:
  ```powershell
  New-NetFirewallRule -DisplayName "ApexWheel Companion" -Direction Inbound -LocalPort 9876,8080 -Protocol TCP -Action Allow
  ```
- **Router Client Isolation**: Some guest/hotel networks prevent devices from talking to each other. Use a Mobile Hotspot from your phone or PC if this occurs.

### 2. ViGEmBus Error on Windows (`vgamepad.win.vigem.ViGEmBusNotFound`)
- Ensure you ran `ViGEmBus_Setup_x64.exe` as Administrator and restarted your PC if prompted.
- Verify in **Device Manager** under **System Devices** that **Virtual Gamepad Emulation Bus** is present and running without error flags.

### 3. Gradle Build Failure (`Unsupported class file major version` or Java Errors)
- Gradle 8.x is compatible with **JDK 17 or JDK 21**. If your system default is Java 25+, Gradle will fail.
- Point Gradle to JDK 21 by adding the following line to `android_app/gradle.properties`:
  ```properties
  org.gradle.java.home=/path/to/jdk-21
  ```

### 4. Steering Tilts in the Opposite Direction
- Go to the **Settings Screen** in the mobile app and toggle **Invert Steering Axis**.

---

<a id="unit-tests"></a>
## 🧪 Running Unit Tests & Verification

### PC Companion Tests
Verify the protocol parser, clamping, watchdog failsafe, and mock controller:
```bash
cd pc_companion
python tests/run_tests.py
```
*Expected: All 5 test suites pass (100% test success).*

### Android Unit Tests
Verify mathematical sensor filtering, deadzone scaling, and JSON serialization:
```bash
cd android_app
./gradlew testDebugUnitTest
```
*Expected: `BUILD SUCCESSFUL` with all unit tests passing.*

---

<a id="project-architecture"></a>
## 📁 Project Architecture & Directory Layout

```
Steering Project/
├── android_app/                         # Native Android Application (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── build.gradle.kts             # Compile SDK 36, Min SDK 29, Java 21 target
│   │   └── src/
│   │       ├── main/
│   │       │   ├── AndroidManifest.xml  # Permissions, orientations, custom scanner activity
│   │       │   ├── res/                 # Vector scanner frame, team assets, layouts
│   │       │   └── java/com/racing/controller/
│   │       │       ├── MainActivity.kt
│   │       │       ├── model/           # ControllerPacket, RacingTeam (F1 liveries)
│   │       │       ├── sensors/         # SteeringSensorManager (fused rotation + filter)
│   │       │       ├── network/         # NetworkClient (TCP socket, heartbeat, ping)
│   │       │       ├── settings/        # SettingsRepository (DataStore persistence)
│   │       │       ├── viewmodel/       # ControllerViewModel (input loop & haptics)
│   │       │       └── ui/
│   │       │           ├── components/  # Circular SteeringWheelView, TactileButtons, TelemetryBadge
│   │       │           └── screens/     # ControllerScreen, ConnectionScreen, F1ThemeDialog
│   │       └── test/                    # SteeringMathTest, PacketSerializationTest
│   ├── build.gradle.kts
│   └── gradle.properties                # JDK 21 configuration
│
├── pc_companion/                        # PC Server & Virtual Gamepad Engine (Python 3)
│   ├── requirements.txt                 # Dependencies: vgamepad, qrcode
│   ├── run_pc_companion.py              # Main CLI entry launcher
│   ├── config.json                      # Configurable ports & button mappings
│   ├── src/
│   │   ├── app.py                       # Application coordinator & CLI argument parser
│   │   ├── config.py                    # Config loader & IP auto-detection
│   │   ├── protocol.py                  # Packet parsing, bounds clamping, pong response
│   │   ├── server.py                    # Async TCP socket server & 500ms safety watchdog
│   │   ├── controller/
│   │   │   ├── base.py                  # AbstractGamepad interface
│   │   │   ├── vgamepad_output.py       # Windows ViGEmBus Xbox 360 output driver
│   │   │   └── mock_output.py           # Cross-platform / development driver
│   │   └── ui/
│   │       ├── web_ui.py                # Live HTML5/CSS3 Web Control Panel (Port 8080)
│   │       └── cli_ui.py                # ANSI Terminal HUD with real-time ASCII meters
│   └── tests/                           # Python unit test suite
│
└── README.md                            # Complete Project Documentation & Instructions
```

---

## 📄 License & Attribution

ApexWheel is developed as an open-source racing project.
All team trademarks, vehicle names, and brand identifiers (Red Bull, Ferrari, Mercedes, McLaren, Aston Martin, Alpine, Williams, Sauber, Haas, VCARB) belong to their respective copyright and trademark owners and are referenced solely for personalization aesthetics.

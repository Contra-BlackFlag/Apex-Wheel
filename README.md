# ApexWheel: Mobile Racing Controller for PC Games

ApexWheel turns your Android smartphone into a high-precision, low-latency wireless racing controller for PC racing games (Forza Horizon, Assetto Corsa, F1, BeamNG.drive, Need for Speed, Project CARS, etc.).

By combining Android's fused motion sensors (rotation vector / gyroscope) with a custom Nothing-inspired tactile cockpit interface, ApexWheel converts physical phone steering tilt, analog pedal pressures, and quick-shift touches into standard Xbox 360 gamepad inputs recognized by Windows games.

---

## Visual Design Reference & Interface

The Android interface is inspired by Nothing's minimalist dark design language:
- **Near-black matte background** (`#111215`) with charcoal controller touch surfaces (`#21242A`).
- **Tactile feedback** with subtle highlight bevels and embossed depth.
- **Steering Wheel Canvas**: Large circular wheel with full crosshairs and center indicator that rotates smoothly in real-time as you tilt your phone.
- **Brake & Accelerator**: Circular touch pads positioned comfortably at the upper corners.
- **Clutch**: U-shaped rounded-bottom pedal button below the brake.
- **E-Brake**: Dedicated automotive handbrake button with standard `(!)` symbol.
- **Gear Up / Down**: Wide chevron arrows for rapid shifting.
- **Select & Start**: Pill buttons centered beneath the steering wheel.

---

## System Architecture

```
┌──────────────────────────────────────────────┐
│          Android Client (Kotlin)             │
│  - SensorManager (Fused Game Rotation Vector)│
│  - Low-pass Exponential Smoothing Filter     │
│  - Deadzone & Sensitivity Remapping          │
│  - Jetpack Compose Cockpit UI                │
│  - Non-blocking TCP Socket (TCP_NODELAY)     │
└──────────────────────┬───────────────────────┘
                       │ Wi-Fi LAN
                       │ Newline-delimited JSON (60 Hz)
                       ▼
┌──────────────────────────────────────────────┐
│           PC Companion (Python 3)            │
│  - Asynchronous TCP Socket Server            │
│  - 500ms Watchdog Disconnect Safety          │
│  - Input Protocol Parser & Validation        │
│  - Embedded Web Control Panel (Port 8080)    │
│  - ANSI Real-time Terminal HUD               │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│       Windows ViGEmBus Driver Layer          │
│  - vgamepad (Virtual Xbox 360 Controller)    │
│  - Analog Steering (Left Thumbstick X)       │
│  - Analog Throttle (Right Trigger 0..255)    │
│  - Analog Brake (Left Trigger 0..255)        │
│  - Clutch, Handbrake, Shifters, Menus        │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│      PC Games (Forza, Assetto Corsa, etc.)   │
│   Native XInput Controller Support           │
└──────────────────────────────────────────────┘
```

---

## Directory Structure

```
Steering Project/
├── android_app/                     # Android Application (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── build.gradle.kts         # Compile SDK 36, Min SDK 29, Java 21
│   │   └── src/
│   │       ├── main/
│   │       │   ├── AndroidManifest.xml
│   │       │   └── java/com/racing/controller/
│   │       │       ├── MainActivity.kt
│   │       │       ├── model/       # ControllerPacket, ConnectionState, Settings
│   │       │       ├── sensors/     # SteeringSensorManager (fused rotation + filter)
│   │       │       ├── network/     # NetworkClient (TCP socket, heartbeat, ping)
│   │       │       ├── settings/    # SettingsRepository (DataStore persistence)
│   │       │       ├── viewmodel/   # ControllerViewModel (input loop & haptics)
│   │       │       └── ui/
│   │       │           ├── theme/       # Dark cockpit palette & typography
│   │       │           ├── components/  # SteeringWheelView, TactileButtons, Chevrons
│   │       │           └── screens/     # ControllerScreen, ConnectionScreen, Settings
│   │       └── test/                # SteeringMathTest, PacketSerializationTest
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradlew
│
├── pc_companion/                    # PC Companion Application (Python 3)
│   ├── requirements.txt             # Dependencies (vgamepad)
│   ├── run_pc_companion.py          # Application entry launcher
│   ├── config.json                  # Configurable ports and button mappings
│   ├── src/
│   │   ├── app.py                   # Desktop application coordinator
│   │   ├── config.py                # Configuration loader & local IP discovery
│   │   ├── protocol.py              # Protocol parser, clamping, pong generator
│   │   ├── server.py                # Async TCP server & 500ms watchdog timer
│   │   ├── controller/
│   │   │   ├── base.py              # AbstractGamepad interface
│   │   │   ├── vgamepad_output.py   # Windows ViGEmBus Xbox 360 controller output
│   │   │   └── mock_output.py       # Cross-platform / test fallback driver
│   │   └── ui/
│   │       ├── web_ui.py            # Live HTML5/CSS3 Web Control Panel
│   │       └── cli_ui.py            # ANSI Terminal HUD with live meters
│   └── tests/
│       ├── run_tests.py             # Standalone test runner
│       ├── test_protocol.py         # Packet parsing & clamping tests
│       ├── test_safety.py           # Watchdog timeout & neutral safety tests
│       ├── test_controller.py       # Controller output tests
│       └── test_server.py           # TCP socket server workflow tests
│
└── README.md
```

---

## Controller Input Mapping

| UI Control | Virtual Gamepad Target (Xbox 360) | Range / Action |
|---|---|---|
| **Steering** | Left Thumbstick X-Axis | Analog `-1.0` (Full Left) to `+1.0` (Full Right) |
| **Accelerator** | Right Trigger (RT) | Analog `0.0` to `1.0` (mapped to `0..255`) |
| **Brake** | Left Trigger (LT) | Analog `0.0` to `1.0` (mapped to `0..255`) |
| **Clutch** | Button `X` (or Left Shoulder) | Analog / digital clutch disengage |
| **E-Brake** | Button `A` | Handbrake toggle / hold |
| **Gear Up** | Right Shoulder (`RB`) | Shift up transmission |
| **Gear Down** | Left Shoulder (`LB`) | Shift down transmission |
| **Select** | Back / View Button | In-game telemetry / camera switch |
| **Start** | Start / Menu Button | Game pause / main menu |

*Note: All button assignments can be customized in `pc_companion/config.json`.*

---

## Disconnect Safety & Failsafe Watchdog

For racing games, safety under packet loss or unexpected disconnections is critical:
- **500ms Watchdog**: If the PC Companion stops receiving packets from the phone for more than 500 milliseconds (due to Wi-Fi dropouts, incoming calls, or app backgrounding), the watchdog immediately triggers an **emergency failsafe**:
  - Steering is reset to `0.0` (straight ahead).
  - Throttle and Brake are reset to `0.0` (prevents runaway acceleration into a wall).
  - All held buttons are immediately released.
- On client socket disconnect or application shutdown, `gamepad.reset_all()` is executed unconditionally.

---

## Setup & Installation

### 1. PC Companion (Windows 10 / 11)

#### Step 1: Install ViGEmBus Driver
The Virtual Gamepad Emulation Bus (`ViGEmBus`) is required on Windows to emulate a physical Xbox 360 controller:
1. Download the latest installer from the official release page:
   [ViGEmBus Releases on GitHub](https://github.com/nefarius/ViGEmBus/releases)
2. Run `ViGEmBus_Setup_x64.exe` and follow the prompt (requires administrator privileges).

#### Step 2: Install Python Dependencies
```bash
cd pc_companion
pip install -r requirements.txt
```

#### Step 3: Run the PC Companion
```bash
python run_pc_companion.py
```

The companion will start and display:
1. Your PC's local network IP address (e.g. `192.168.1.15`).
2. The listening TCP port (default `9876`).
3. The Web Control Panel URL: `http://localhost:8080`.

Open `http://localhost:8080` in your browser to view the live dashboard with real-time steering gauges, pedal meters, and connection diagnostics.

#### Firewall Permission
When running the PC companion for the first time, Windows Defender Firewall will ask to allow Python on Private Networks. Ensure you check **"Private networks"** and click **Allow access**.

---

### 2. Android Application

#### Step 1: Build the APK
From the `android_app` directory:
```bash
./gradlew assembleDebug
```
The compiled APK will be created at:
`android_app/app/build/outputs/apk/debug/app-debug.apk`

#### Step 2: Install onto Android Device
Connect your Android phone via USB with USB Debugging enabled, and run:
```bash
adb install android_app/app/build/outputs/apk/debug/app-debug.apk
```

#### Step 3: Connect to PC
1. Connect your phone to the **same Wi-Fi network** as your PC.
2. Open **ApexWheel** on your phone.
3. Tap the connection pill at the top left to open the **Connection Screen**.
4. Enter the PC's IP address displayed in the PC companion window and tap **Connect to PC**.
5. Return to the controller cockpit.

---

## Calibration & Tuning

1. **Neutral Calibration**:
   - Hold your phone horizontally at your preferred natural racing angle.
   - Tap the **Calibrate** button on the top HUD.
   - The current phone orientation is locked as the neutral center (`0.0`).
2. **Steering Dynamics (Settings Screen)**:
   - **Sensitivity**: Adjust from `0.5x` (steady GT3 style) to `2.5x` (quick response rally / drift).
   - **Range**: Customize maximum physical tilt required for full lock (from `±20°` to `±90°`, default `±45°`).
   - **Dead Zone**: Filter out resting hand micro-shakes (`0%` to `15%`, default `3%`).
   - **Smoothing Filter**: Low-pass filter to eliminate sensor jitter while maintaining sub-5ms response time.
   - **Touch Steering**: Enable to drag the on-screen steering wheel directly instead of using motion tilt.

---

## Running Verification Tests

### Android Unit Tests
```bash
cd android_app
./gradlew test
```
Runs unit tests for:
- Dead zone calculations, sensitivity curve scaling, inversion, and clamping.
- Packet JSON serialization and schema compatibility.

### PC Companion Unit Tests
```bash
cd pc_companion
python tests/run_tests.py
```
Runs unit tests for:
- Input packet parsing and bounds clamping (`[-1.0, 1.0]`, `[0.0, 1.0]`).
- Heartbeat ping and pong packet validation.
- Watchdog timeout neutral failsafe.
- Mock gamepad state tracking.
- Client socket connect/disconnect workflow.

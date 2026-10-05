# Helix WiFi Mic

🎤 Aplikasi open-source ringan untuk mengubah HP Android menjadi mikrofon PC via Wi-Fi

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://android.com)
[![Python](https://img.shields.io/badge/Python-3.8%2B-blue.svg)](https://python.org)

## 🚀 Quick Start

**5 menit setup:**

1. Install Virtual Audio Cable di PC
2. Start Python receiver: `python -m receiver.main`
3. Install Android APK dan masukkan IP PC
4. Tap "Start Streaming" ✅

📖 [Detailed Setup Guide](QUICKSTART.md)

## ✨ Features

- 🎙️ **Real-time streaming** - Low latency UDP (~50-200ms)
- 📱 **Modern Android UI** - Material 3 + Jetpack Compose
- 🔊 **High quality audio** - 48kHz 16-bit PCM
- 🌐 **WiFi streaming** - No cables needed
- 🔋 **Background support** - Works with screen off
- 📊 **Live monitoring** - Packet count & status updates
- 🎯 **Easy setup** - Auto-detect Virtual Audio Cable

## 🏗️ Architecture

```
┌─────────────────┐         UDP         ┌──────────────────┐
│  Android App    │ ───────────────────► │   PC Receiver    │
│                 │    48kHz PCM Audio   │                  │
│ • AudioRecord   │                      │ • UDP Server     │
│ • UDP Streamer  │                      │ • PyAudio Output │
│ • Compose UI    │                      │ • Virtual Cable  │
└─────────────────┘                      └──────────────────┘
                                                 │
                                                 ▼
                                         ┌──────────────────┐
                                         │  Discord / OBS   │
                                         │  Zoom / Teams    │
                                         │  Any App         │
                                         └──────────────────┘
```

## 📁 Project Structure

```
helix/
├── android/                    # Android app (Kotlin + Compose)
│   ├── app/src/main/java/com/helix/wifimic/
│   │   ├── MainActivity.kt            # Main UI
│   │   ├── audio/
│   │   │   ├── AudioCapture.kt        # Mic recording (AudioRecord)
│   │   │   └── AudioConfig.kt         # Audio configuration
│   │   ├── network/
│   │   │   └── UdpStreamer.kt         # UDP transmission
│   │   └── service/
│   │       └── AudioStreamService.kt  # Foreground service
│   ├── ARCHITECTURE.md         # Technical docs
│   └── BUILD.md               # Build instructions
│
├── pc/                        # PC receiver (Python)
│   ├── src/receiver/
│   │   ├── main.py           # Entry point
│   │   ├── udp_receiver.py   # UDP server
│   │   ├── audio_output.py   # PyAudio output
│   │   └── config.py         # Configuration
│   └── README.md             # Setup guide
│
└── docs/
    ├── QUICKSTART.md         # 5-min setup guide
    ├── USER_GUIDE.md         # Complete user manual
    └── CONTRIBUTING.md       # Contribution guidelines
```

## 🛠️ Tech Stack

### Android
- **Language:** Kotlin 1.9.20
- **UI:** Jetpack Compose + Material 3
- **Audio:** Android AudioRecord API
- **Network:** Java DatagramSocket (UDP)
- **Architecture:** Service-based with Coroutines
- **Min SDK:** 24 (Android 7.0)

### PC
- **Language:** Python 3.8+
- **Audio:** PyAudio
- **Network:** UDP socket server
- **Dependencies:** NumPy

## 📦 Installation

### Prerequisites

**PC:**
- Python 3.8+
- Virtual Audio Cable ([VB-CABLE](https://vb-audio.com/Cable/))
- Windows / Linux / macOS

**Android:**
- Android 7.0+ (API 24+)
- Android Studio (for building) or pre-built APK

### Build from Source

**Android:**
```bash
cd android
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

**PC:**
```bash
cd pc
python -m venv venv
source venv/bin/activate  # Linux/Mac
venv\Scripts\activate     # Windows
pip install -r requirements.txt
```

## 🎮 Usage

### 1. Start PC Receiver

```bash
cd pc
python -m receiver.main

# Custom port (optional)
python -m receiver.main --port 5556
```

### 2. Configure Android App

1. Open **Helix WiFi Mic**
2. Enter PC IP address (e.g., `192.168.1.100`)
3. Tap **"Start Streaming"**

### 3. Select in Your Apps

In Discord, OBS, Zoom, etc:
- Audio Input → **CABLE Output (VB-Audio Virtual Cable)**

## 📖 Documentation

- 📘 [Quick Start Guide](QUICKSTART.md) - Get running in 5 minutes
- 📗 [User Guide](USER_GUIDE.md) - Complete usage & troubleshooting
- 📙 [Android Architecture](android/ARCHITECTURE.md) - Technical deep dive
- 📕 [Build Guide](android/BUILD.md) - Compilation & development
- 📓 [PC Receiver Setup](pc/README.md) - Python setup & configuration

## 🤝 Contributing

Contributions welcome! See [CONTRIBUTING.md](CONTRIBUTING.md)

Areas for contribution:
- USB connection support
- Audio compression (Opus)
- Better error handling
- Localization (i18n)
- Unit tests

## 🐛 Troubleshooting

**Connection failed?**
- Check IP address is correct
- Ensure phone & PC on same WiFi
- Check firewall allows UDP port 5555

**No audio in PC apps?**
- Virtual Audio Cable installed?
- Selected "CABLE Output" as input device?
- Receiver shows "Connected"?

**Audio choppy?**
- Use 5GHz WiFi if available
- Move closer to router
- Close other network-heavy apps

📚 [Full Troubleshooting Guide](USER_GUIDE.md#troubleshooting)

## 🔒 Security & Privacy

- Audio transmitted unencrypted over local network
- No internet connection required
- No data collection or storage
- Use only on trusted WiFi networks

## 📊 Performance

- **Latency:** 50-200ms (typical)
- **Bandwidth:** ~768 kbps
- **CPU Usage:** 5-15% (Android)
- **Battery:** ~10-15%/hour

## 📜 License

MIT License - see [LICENSE](LICENSE) file

Free and open source forever 🎉

## 🙏 Acknowledgments

Inspired by [AudioRelay](https://audiorelay.net/) - check them out for a feature-rich commercial alternative

## 📧 Support

- 🐛 [Issues](../../issues)
- 💬 [Discussions](../../discussions)
- 📖 [Documentation](USER_GUIDE.md)

---

Made with ❤️ for the open source community

**Star ⭐ this repo if you find it useful!**

# Helix WiFi Mic - Quick Start

🎤 Transform your Android phone into a PC microphone via WiFi

## Quick Setup (5 minutes)

### 1️⃣ PC Setup

```bash
# Install Virtual Audio Cable
Download: https://vb-audio.com/Cable/
# Restart PC after install

# Install Python receiver
cd pc
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt

# Find your PC IP
ipconfig
# Note the IPv4 Address (e.g., 192.168.1.100)

# Start receiver
python -m receiver.main
```

### 2️⃣ Android Setup

```bash
# Build and install
cd android
gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or open in Android Studio and click Run ▶️

### 3️⃣ Connect & Stream

1. Open **Helix WiFi Mic** app on Android
2. Grant **RECORD_AUDIO** permission
3. Enter your PC IP: `192.168.1.100`
4. Tap **"Start Streaming"** ✅

### 4️⃣ Use in Apps

In Discord, OBS, Zoom, etc:
- **Input Device** → **CABLE Output (VB-Audio Virtual Cable)**

## Requirements

- Android 7.0+ (API 24+)
- Python 3.8+
- Same WiFi network for phone & PC
- Virtual Audio Cable installed on PC

## Documentation

- 📖 [Full User Guide](USER_GUIDE.md)
- 🏗️ [Android Architecture](android/ARCHITECTURE.md)
- 🔧 [Build Instructions](android/BUILD.md)
- 🐍 [PC Receiver Setup](pc/README.md)
- 🤝 [Contributing](CONTRIBUTING.md)

## Features

✅ Low latency UDP streaming (~50-200ms)  
✅ 48kHz 16-bit PCM audio quality  
✅ Foreground service (works with screen off)  
✅ Material 3 UI with Jetpack Compose  
✅ Auto-detect Virtual Audio Cable  
✅ Real-time packet monitoring  

## Tech Stack

**Android:** Kotlin + Jetpack Compose + AudioRecord + UDP Sockets  
**PC:** Python + PyAudio + UDP Server

## License

MIT License - Free and open source

## Support

Having issues? Check [USER_GUIDE.md](USER_GUIDE.md) troubleshooting section.

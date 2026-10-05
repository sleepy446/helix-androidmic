# Helix WiFi Mic - Android App

Aplikasi Android untuk merekam audio dari mikrofon dan streaming via UDP.

## Teknologi

- **Kotlin** - Bahasa pemrograman
- **Jetpack Compose** - UI modern
- **AudioRecord API** - Recording audio mentah (PCM)
- **UDP Socket** - Streaming audio real-time

## Build Requirements

- Android Studio Hedgehog | 2023.1.1 atau lebih baru
- JDK 17
- Android SDK 34
- Gradle 8.2

## Struktur Proyek

```
app/
├── src/main/
│   ├── java/com/helix/wifimic/
│   │   ├── MainActivity.kt              # Activity utama
│   │   ├── audio/
│   │   │   ├── AudioCapture.kt         # Capture audio dari mic
│   │   │   └── AudioConfig.kt          # Konfigurasi audio
│   │   ├── network/
│   │   │   ├── UdpStreamer.kt          # UDP streaming
│   │   │   └── NetworkConfig.kt        # Konfigurasi network
│   │   ├── service/
│   │   │   └── AudioStreamService.kt   # Foreground service
│   │   └── ui/
│   │       ├── theme/                  # Compose theme
│   │       └── screens/                # UI screens
│   ├── res/                            # Resources
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Permissions

App memerlukan:
- `RECORD_AUDIO` - Akses mikrofon
- `INTERNET` - Streaming via network
- `FOREGROUND_SERVICE` - Service background
- `ACCESS_WIFI_STATE` - Info WiFi

## Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Install ke device
./gradlew installDebug

# Run
./gradlew run
```

## Audio Format

- Sample Rate: 48000 Hz
- Channel: Mono
- Encoding: PCM 16-bit
- Buffer Size: Optimal berdasarkan device

## Network Protocol

- Protocol: UDP
- Default Port: 5555
- Packet Size: 1024 bytes
- Format: Raw PCM data

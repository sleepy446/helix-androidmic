# Helix WiFi Mic - Build & Development Guide

## Prerequisites

### Required
- **Android Studio Hedgehog** (2023.1.1) atau lebih baru
- **JDK 17**
- **Android SDK 34** (Android 14)
- **Gradle 8.2** (sudah included via wrapper)

### Optional
- Physical Android device (Android 7.0+ / API 24+) atau Emulator
- ADB (Android Debug Bridge) untuk testing

## Project Setup

### 1. Clone atau buka project di Android Studio

```bash
cd android
```

### 2. Sync Gradle

Android Studio akan otomatis sync Gradle saat pertama kali buka project. Atau manual:

```bash
./gradlew --refresh-dependencies
```

### 3. Check konfigurasi

Pastikan file `local.properties` ada dan berisi path ke Android SDK:

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

## Build Commands

### Via Android Studio

1. **Build > Make Project** (Ctrl+F9)
2. **Build > Build Bundle(s) / APK(s) > Build APK(s)**

### Via Gradle CLI

#### Build Debug APK

```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

#### Build Release APK (unsigned)

```bash
gradlew assembleRelease
```

#### Install ke device

```bash
gradlew installDebug
```

#### Build + Install + Run

```bash
gradlew quickRun
```

## Run & Test

### 1. Connect device via USB

```bash
# Check device connected
adb devices
```

### 2. Enable USB Debugging di HP

Settings > About Phone > tap "Build number" 7x > Developer Options > USB Debugging

### 3. Run dari Android Studio

**Run > Run 'app'** (Shift+F10)

### 4. Run via CLI

```bash
gradlew installDebug
adb shell am start -n com.helix.wifimic/.MainActivity
```

## Debug

### View Logcat

```bash
# Filter by app
adb logcat | findstr "HelixWiFiMic\|AudioCapture\|UdpStreamer\|AudioStreamService"

# atau di Android Studio
View > Tool Windows > Logcat
```

### Check app permissions

```bash
adb shell dumpsys package com.helix.wifimic | findstr permission
```

### Grant permissions manually

```bash
adb shell pm grant com.helix.wifimic android.permission.RECORD_AUDIO
```

## Clean Build

Jika ada masalah build:

```bash
gradlew clean
gradlew build --refresh-dependencies
```

Atau di Android Studio:
**Build > Clean Project** kemudian **Build > Rebuild Project**

## Code Quality

### Format code

Android Studio: **Code > Reformat Code** (Ctrl+Alt+L)

### Lint check

```bash
gradlew lint
```

Report: `app/build/reports/lint-results.html`

## Signing Release APK

### 1. Generate keystore

```bash
keytool -genkey -v -keystore helix-release.keystore -alias helix -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Edit `app/build.gradle.kts`

Tambahkan signing config:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../helix-release.keystore")
            storePassword = "your-password"
            keyAlias = "helix"
            keyPassword = "your-password"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ...
        }
    }
}
```

### 3. Build signed APK

```bash
gradlew assembleRelease
```

## Troubleshooting

### Gradle sync failed

- Check internet connection
- **File > Invalidate Caches / Restart**
- Delete `.gradle` folder dan sync ulang

### Kotlin version mismatch

Pastikan versi Kotlin di `build.gradle.kts` (root) sama dengan plugin Compose:

```kotlin
id("org.jetbrains.kotlin.android") version "1.9.20"
id("org.jetbrains.kotlin.plugin.compose") version "1.9.20"
```

### AudioRecord initialization failed

- Check permission RECORD_AUDIO granted
- Test di real device (emulator mungkin tidak punya mic)
- Restart app

### UDP connection failed

- Check IP address format valid
- Pastikan PC dan HP di network yang sama
- Check firewall di PC tidak block port 5555

## Performance Tips

### Reduce APK size

Di `app/build.gradle.kts`:

```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}
```

### Enable R8 full mode

Sudah enabled by default di `gradle.properties`:

```properties
android.enableR8.fullMode=true
```

## CI/CD

### GitHub Actions example

```yaml
- name: Build Debug APK
  run: |
    cd android
    chmod +x gradlew
    ./gradlew assembleDebug
```

## Additional Resources

- [Android Developer Docs](https://developer.android.com)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [AudioRecord API](https://developer.android.com/reference/android/media/AudioRecord)

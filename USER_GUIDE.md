# Helix WiFi Mic - User Guide

## Cara Menggunakan

### Setup PC (Windows)

#### 1. Install Virtual Audio Cable

Download dan install salah satu:

**VB-CABLE (Recommended - Gratis)**
- Download: https://vb-audio.com/Cable/
- Jalankan installer
- Restart PC setelah instalasi

**VoiceMeeter (Alternatif - Lebih advanced)**
- Download: https://vb-audio.com/Voicemeeter/
- Include virtual audio cable + mixer

#### 2. Install Python Dependencies

```bash
cd pc
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
```

**Troubleshooting PyAudio di Windows:**

Jika `pip install pyaudio` gagal, gunakan:

```bash
pip install pipwin
pipwin install pyaudio
```

Atau download wheel manual dari:
https://www.lfd.uci.edu/~gohlke/pythonlibs/#pyaudio

#### 3. Cari IP Address PC

```bash
ipconfig
```

Cari bagian **IPv4 Address** di adapter yang terkoneksi ke WiFi, contoh: `192.168.1.100`

#### 4. Start Receiver

```bash
cd pc
python -m receiver.main
```

Output:
```
==================================================
Helix WiFi Mic - PC Receiver
==================================================

Audio Devices yang tersedia:
0: Microphone (Default)
1: CABLE Input (VB-Audio Virtual Cable) (Output: 2)
  -> Virtual Audio Cable terdeteksi

✓ Audio output started: CABLE Input (VB-Audio Virtual Cable)
✓ UDP server listening on port 5555

Waiting for audio stream from Android...
```

### Setup Android

#### 1. Install APK

**Via Android Studio:**
- Buka project di Android Studio
- Connect HP via USB
- Run > Run 'app' (Shift+F10)

**Via APK File:**
```bash
cd android
gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### 2. Konfigurasi App

1. Buka **Helix WiFi Mic** app
2. Grant permission **RECORD_AUDIO** saat diminta
3. Masukkan **IP Address PC** (contoh: `192.168.1.100`)
4. Port default: `5555` (biarkan default)

#### 3. Start Streaming

1. Tap tombol **"Start Streaming"** (hijau)
2. Notification muncul: "Helix WiFi Mic - Streaming to 192.168.1.100:5555"
3. Di PC receiver akan muncul:
   ```
   ✓ Connected to 192.168.1.100:xxxxx
   Received 1000 packets...
   ```

#### 4. Test Audio

**Di PC, buka aplikasi apapun yang support audio input:**

1. **Discord**
   - User Settings > Voice & Video
   - Input Device: **CABLE Output (VB-Audio Virtual Cable)**
   
2. **OBS Studio**
   - Sources > Add > Audio Input Capture
   - Device: **CABLE Output (VB-Audio Virtual Cable)**

3. **Zoom / Microsoft Teams**
   - Audio Settings
   - Microphone: **CABLE Output (VB-Audio Virtual Cable)**

4. **Windows Sound Recorder** (untuk test)
   - Buka Sound Recorder
   - Settings > Select input: **CABLE Output**
   - Mulai recording dan bicara ke HP

#### 5. Stop Streaming

1. Tap tombol **"Stop Streaming"** (merah) di app
2. Atau swipe notification dan tap "Stop"
3. PC receiver akan menampilkan total packets sent

## Network Requirements

### Same WiFi Network

HP dan PC **HARUS** di network WiFi yang sama:

✅ HP: WiFi "HomeNetwork" (192.168.1.x)  
✅ PC: WiFi "HomeNetwork" (192.168.1.x)

❌ HP: WiFi "HomeNetwork"  
❌ PC: Ethernet / WiFi berbeda

### Firewall

**Windows Firewall** mungkin block UDP port 5555. Jika koneksi gagal:

```bash
# Allow port 5555 (Run as Administrator)
netsh advfirewall firewall add rule name="Helix WiFi Mic" dir=in action=allow protocol=UDP localport=5555
```

Atau manual:
1. Windows Security > Firewall & network protection
2. Advanced settings > Inbound Rules > New Rule
3. Port > UDP > 5555 > Allow connection

### Router Configuration

Jika masih gagal, check router settings:
- **AP Isolation** harus dimatikan (setting di router admin panel)
- Beberapa router memblok komunikasi antar client di WiFi guest network

## Tips & Tricks

### Minimize Latency

1. **Gunakan WiFi 5GHz** jika tersedia (lebih stabil, less interference)
2. **Dekatkan HP ke router** untuk signal strength optimal
3. **Tutup aplikasi background** yang consume network bandwidth
4. **Matikan WiFi power saving** di HP (Settings > WiFi > Advanced)

### Audio Quality

- Sample rate 48kHz sudah optimal untuk voice
- Jika ingin quality lebih tinggi, bisa modify di `AudioConfig.kt` dan `config.py`

### Battery Saving

- App otomatis minta **battery optimization exemption**
- Streaming audio consume battery, charge HP jika untuk durasi lama

### Background Streaming

App akan tetap streaming meskipun:
- ✅ Screen off (layar mati)
- ✅ App di background / minimize
- ✅ Switch ke app lain
- ❌ App di force close
- ❌ HP restart

## Troubleshooting

### "Failed to initialize audio capture"

**Solusi:**
- Grant permission RECORD_AUDIO
- Close aplikasi lain yang pakai mic (voice recorder, voice call, dll)
- Restart app
- Test di real device (bukan emulator)

### "Failed to connect to X.X.X.X:5555"

**Solusi:**
1. Check IP address benar (typo?)
2. PC receiver sudah running?
3. HP dan PC di WiFi yang sama?
4. Firewall di PC block port 5555?
5. Test dengan `ping` dari HP ke PC:
   - Install "Network Tools" app
   - Ping IP PC
   - Jika failed = network issue

### "Audio terputus-putus / choppy"

**Solusi:**
- WiFi signal lemah? Dekatkan HP ke router
- Too many devices di WiFi? Disconnect yang tidak perlu
- Gunakan 5GHz WiFi jika available
- Close download/streaming di device lain

### "No audio di PC aplikasi"

**Solusi:**
1. Virtual Audio Cable terdeteksi di receiver? (check output log)
2. App di PC sudah pilih **CABLE Output** sebagai input device?
3. Test dengan Windows Sound Recorder dulu
4. Volume tidak mute di Windows Sound mixer?

### "Permission denied"

Jika app crash saat start recording:
- Android 6.0+ butuh runtime permission
- Tap "Allow" saat permission dialog muncul
- Atau manual: Settings > Apps > Helix WiFi Mic > Permissions > Microphone > Allow

### "Latensi tinggi (delay)"

- UDP sudah low-latency by design
- Latensi biasanya ~50-200ms tergantung network
- Jika >500ms = network issue atau interference
- Switch ke WiFi 5GHz atau ethernet PC jika available

## Advanced Usage

### Custom Port

Jika port 5555 conflict dengan app lain:

**PC:**
```bash
python -m receiver.main --port 5556
```

**Android:**
Ubah nilai Port di input field ke `5556`

### USB Connection (Future Feature)

Saat ini hanya support WiFi. USB via ADB port forwarding planned untuk v2.0.

### Multiple Instances

Bisa run multiple HP ke satu PC dengan port berbeda:

**PC Terminal 1:**
```bash
python -m receiver.main --port 5555
```

**PC Terminal 2:**
```bash
python -m receiver.main --port 5556
```

Tapi Virtual Audio Cable hanya 1 channel, jadi audio akan tercampur.

## Performance Metrics

### Typical Performance

- **Latency**: 50-200ms
- **Bandwidth**: ~768 kbps (48kHz * 16-bit)
- **CPU Usage (Android)**: 5-15%
- **Battery Drain**: ~10-15%/hour
- **Packet Loss**: <0.1% pada WiFi stabil

### Monitoring

**Android:**
- Packets sent counter di UI (updates setiap 1000 packets)
- Logcat: `adb logcat | findstr AudioCapture`

**PC:**
- Console output menampilkan packet count
- Packets received counter

## Best Practices

1. **Always start PC receiver first**, baru Android app
2. **Gunakan IP static** di PC untuk avoid IP berubah
3. **Keep HP charged** saat long streaming sessions
4. **Close unused apps** untuk free up resources
5. **Test audio** sebelum meeting/recording penting

## FAQ

**Q: Apa bedanya dengan AudioRelay?**  
A: Helix WiFi Mic adalah open-source, lightweight, dan free. AudioRelay adalah commercial app dengan more features.

**Q: Apakah data audio disimpan?**  
A: Tidak. Audio di-stream real-time via UDP, tidak ada recording atau storage.

**Q: Support Bluetooth?**  
A: Belum. Saat ini hanya WiFi UDP. Bluetooth support planned untuk future.

**Q: Bisa untuk iOS?**  
A: Tidak. Ini Android-only app. iOS butuh development terpisah.

**Q: Apakah aman?**  
A: Data audio dikirim plain (unencrypted) via local network. Jangan gunakan di public WiFi untuk privasi.

**Q: Kenapa pakai UDP bukan TCP?**  
A: UDP lebih low-latency karena no handshake, no retransmission. Perfect untuk real-time audio.

## Support

- GitHub Issues: (link ke repo jika sudah public)
- Documentation: README.md
- Architecture: android/ARCHITECTURE.md
- Build Guide: android/BUILD.md

## License

MIT License - Free and open source

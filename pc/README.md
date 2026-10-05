# Helix WiFi Mic - PC Client

Client Python untuk menerima audio stream dari Android dan output ke Virtual Audio Cable.

## Requirements

- Python 3.8+
- PyAudio
- Virtual Audio Cable (VB-CABLE atau sejenisnya)

## Instalasi

### 1. Install Virtual Audio Cable

Download dan install salah satu:
- **VB-CABLE** (Gratis): https://vb-audio.com/Cable/
- **VoiceMeeter** (Gratis, lebih advanced): https://vb-audio.com/Voicemeeter/

### 2. Setup Python Environment

```bash
# Buat virtual environment
python -m venv venv

# Activate venv
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

**Catatan untuk Windows:**
PyAudio mungkin perlu diinstall via wheel file:
```bash
pip install pipwin
pipwin install pyaudio
```

## Usage

### Basic Usage

```bash
python -m receiver.main
```

### Custom Port

```bash
python -m receiver.main --port 5556
```

### Help

```bash
python -m receiver.main --help
```

## Cara Kerja

1. Script akan mencari Virtual Audio Cable device secara otomatis
2. UDP server listen di port 5555 (default)
3. Menerima audio stream dari Android app
4. Output langsung ke Virtual Audio Cable
5. Aplikasi lain (Discord, OBS, Zoom, dll) bisa pilih Virtual Cable sebagai input mic

## Konfigurasi di Aplikasi

Setelah receiver berjalan, di aplikasi lain (Discord, OBS, dll):
1. Buka audio settings
2. Pilih input device: **CABLE Output (VB-Audio Virtual Cable)**
3. Selesai!

## Audio Format

- Sample Rate: 48000 Hz
- Channels: Mono
- Bit Depth: 16-bit PCM
- Protocol: UDP
- Default Port: 5555

## Troubleshooting

### PyAudio not found
- Windows: Install via `pipwin install pyaudio`
- Linux: `sudo apt-get install portaudio19-dev python3-pyaudio`
- Mac: `brew install portaudio && pip install pyaudio`

### Virtual Audio Cable tidak terdeteksi
- Pastikan VB-CABLE sudah terinstall
- Restart PC setelah install VB-CABLE
- Check daftar audio devices di Windows Sound Settings

### Latensi tinggi
- Gunakan koneksi WiFi 5GHz jika memungkinkan
- Pastikan PC dan HP di network yang sama
- Kurangi jarak antara HP dan router

### Audio terputus-putus
- Check network connection
- Tutup aplikasi yang menggunakan bandwidth besar
- Kurangi buffer size di konfigurasi (advanced)

## Development

```bash
# Install development dependencies
pip install -r requirements.txt

# Format code
black src/

# Lint
pylint src/
```

## Architecture

```
receiver/
├── __init__.py
├── main.py              # Entry point & orchestrator
├── config.py            # Configuration constants
├── udp_receiver.py      # UDP socket handler
└── audio_output.py      # PyAudio output handler
```

# Contributing to Helix WiFi Mic

Terima kasih atas minat Anda untuk berkontribusi! 🎉

## How to Contribute

### Reporting Bugs

Jika menemukan bug, buat issue dengan informasi:
- Deskripsi bug yang jelas
- Langkah-langkah untuk reproduce
- Expected behavior vs actual behavior
- Device info (Android version, PC OS)
- Logs (Logcat untuk Android, console output untuk PC)

### Suggesting Features

Feature requests sangat diapresiasi! Jelaskan:
- Use case untuk feature tersebut
- Bagaimana feature akan digunakan
- Alternative solutions yang sudah dicoba

### Pull Requests

1. Fork repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

### Code Style

**Android/Kotlin:**
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use Android Studio default formatter (Ctrl+Alt+L)
- Meaningful variable/function names
- Add comments untuk logic yang kompleks

**Python:**
- Follow PEP 8
- Use `black` untuk formatting: `black src/`
- Type hints appreciated
- Docstrings untuk functions/classes

### Testing

Sebelum submit PR, pastikan:
- ✅ App builds tanpa errors
- ✅ Test di real device (minimal 1 Android version)
- ✅ Existing functionality tidak break
- ✅ Code formatted properly

## Development Setup

Lihat dokumentasi:
- [Android BUILD.md](android/BUILD.md)
- [PC README.md](pc/README.md)

## Areas for Contribution

### High Priority
- [ ] USB connection via ADB
- [ ] Audio compression (Opus codec)
- [ ] Reconnection logic on network drop
- [ ] Better error messages
- [ ] Unit tests

### Medium Priority
- [ ] Audio quality settings UI
- [ ] Latency monitoring
- [ ] Connection history
- [ ] Dark mode optimization
- [ ] Localization (i18n)

### Nice to Have
- [ ] Bluetooth support
- [ ] Multiple PC connections
- [ ] Audio effects (noise suppression, echo cancellation)
- [ ] Desktop app (Electron) untuk PC receiver GUI
- [ ] CI/CD pipeline

## Questions?

Jangan ragu untuk bertanya via GitHub Issues atau Discussions!

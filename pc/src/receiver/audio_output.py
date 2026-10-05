"""
Audio output handler untuk streaming ke Virtual Audio Cable
"""
import pyaudio
from typing import Optional
from .config import SAMPLE_RATE, CHANNELS, SAMPLE_WIDTH, VAC_KEYWORDS


class AudioOutput:
    """Handler untuk output audio ke Virtual Audio Cable"""

    def __init__(self, device_index: Optional[int] = None):
        self.audio = pyaudio.PyAudio()
        self.stream = None
        self.device_index = device_index or self._find_vac_device()

    def _find_vac_device(self) -> Optional[int]:
        """Cari Virtual Audio Cable device"""
        device_count = self.audio.get_device_count()

        print("\nAudio Devices yang tersedia:")
        for i in range(device_count):
            info = self.audio.get_device_info_by_index(i)
            print(f"{i}: {info['name']} (Output: {info['maxOutputChannels']})")

            # Cek apakah ini Virtual Audio Cable
            if any(keyword.lower() in info['name'].lower() for keyword in VAC_KEYWORDS):
                if info['maxOutputChannels'] > 0:
                    print(f"  -> Virtual Audio Cable terdeteksi")
                    return i

        print("\nPeringatan: Virtual Audio Cable tidak ditemukan!")
        print("Menggunakan default output device")
        return None

    def start(self) -> bool:
        """Mulai output stream"""
        try:
            self.stream = self.audio.open(
                format=self.audio.get_format_from_width(SAMPLE_WIDTH),
                channels=CHANNELS,
                rate=SAMPLE_RATE,
                output=True,
                output_device_index=self.device_index,
                frames_per_buffer=1024
            )

            device_info = self.audio.get_device_info_by_index(
                self.device_index if self.device_index is not None
                else self.audio.get_default_output_device_info()['index']
            )
            print(f"\n✓ Audio output started: {device_info['name']}")
            return True

        except Exception as e:
            print(f"✗ Error starting audio output: {e}")
            return False

    def write(self, data: bytes):
        """Write audio data ke output stream"""
        if self.stream:
            self.stream.write(data)

    def stop(self):
        """Stop dan cleanup"""
        if self.stream:
            self.stream.stop_stream()
            self.stream.close()
        self.audio.terminate()
        print("Audio output stopped")

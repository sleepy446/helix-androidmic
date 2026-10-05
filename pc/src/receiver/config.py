"""
Konfigurasi untuk audio receiver
"""

# Audio Configuration
SAMPLE_RATE = 48000  # Hz
CHANNELS = 1  # Mono
SAMPLE_WIDTH = 2  # 2 bytes = 16-bit
CHUNK_SIZE = 1024  # bytes per packet

# Network Configuration
DEFAULT_UDP_PORT = 5555
BUFFER_SIZE = 4096  # UDP receive buffer

# Virtual Audio Cable
# Akan mencari device yang mengandung kata-kata ini
VAC_KEYWORDS = ["CABLE", "Virtual", "VB-Audio"]

"""
UDP receiver untuk menerima audio stream dari Android
"""
import socket
from typing import Callable, Optional
from .config import DEFAULT_UDP_PORT, BUFFER_SIZE


class UdpReceiver:
    """UDP socket receiver untuk audio streaming"""

    def __init__(self, port: int = DEFAULT_UDP_PORT, callback: Optional[Callable] = None):
        self.port = port
        self.callback = callback
        self.socket = None
        self.running = False

    def start(self) -> bool:
        """Start UDP server"""
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            self.socket.bind(('0.0.0.0', self.port))
            self.socket.settimeout(1.0)  # Timeout untuk checking running flag

            print(f"✓ UDP server listening on port {self.port}")
            self.running = True
            return True

        except Exception as e:
            print(f"✗ Error starting UDP server: {e}")
            return False

    def receive_loop(self):
        """Main receive loop"""
        print("Waiting for audio stream from Android...\n")

        packet_count = 0

        while self.running:
            try:
                data, addr = self.socket.recvfrom(BUFFER_SIZE)

                if packet_count == 0:
                    print(f"✓ Connected to {addr[0]}:{addr[1]}")

                packet_count += 1

                if packet_count % 1000 == 0:
                    print(f"Received {packet_count} packets...")

                # Call callback dengan data
                if self.callback:
                    self.callback(data)

            except socket.timeout:
                # Timeout normal, cek running flag
                continue
            except Exception as e:
                if self.running:
                    print(f"Error receiving data: {e}")

    def stop(self):
        """Stop UDP server"""
        self.running = False
        if self.socket:
            self.socket.close()
        print("UDP receiver stopped")

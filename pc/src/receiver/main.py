"""
Main entry point untuk Helix WiFi Mic PC Receiver
"""
import sys
import signal
from .udp_receiver import UdpReceiver
from .audio_output import AudioOutput
from .config import DEFAULT_UDP_PORT


class HelixReceiver:
    """Main application class"""

    def __init__(self, port: int = DEFAULT_UDP_PORT):
        self.port = port
        self.audio_output = None
        self.udp_receiver = None
        self.running = False

    def handle_audio_data(self, data: bytes):
        """Callback untuk handle incoming audio data"""
        if self.audio_output:
            self.audio_output.write(data)

    def start(self):
        """Start receiver"""
        print("=" * 50)
        print("Helix WiFi Mic - PC Receiver")
        print("=" * 50)

        # Setup audio output
        self.audio_output = AudioOutput()
        if not self.audio_output.start():
            print("Failed to start audio output")
            return False

        # Setup UDP receiver
        self.udp_receiver = UdpReceiver(
            port=self.port,
            callback=self.handle_audio_data
        )

        if not self.udp_receiver.start():
            print("Failed to start UDP receiver")
            self.audio_output.stop()
            return False

        self.running = True

        # Setup signal handler untuk graceful shutdown
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)

        print("\n✓ Receiver started successfully")
        print("Press Ctrl+C to stop\n")

        # Main receive loop
        try:
            self.udp_receiver.receive_loop()
        except KeyboardInterrupt:
            pass

        return True

    def stop(self):
        """Stop receiver"""
        print("\n\nStopping receiver...")
        self.running = False

        if self.udp_receiver:
            self.udp_receiver.stop()

        if self.audio_output:
            self.audio_output.stop()

        print("Goodbye!")

    def _signal_handler(self, signum, frame):
        """Handle shutdown signals"""
        self.stop()
        sys.exit(0)


def main():
    """Main entry point"""
    import argparse

    parser = argparse.ArgumentParser(
        description="Helix WiFi Mic PC Receiver - Receive audio stream from Android"
    )
    parser.add_argument(
        "-p", "--port",
        type=int,
        default=DEFAULT_UDP_PORT,
        help=f"UDP port to listen on (default: {DEFAULT_UDP_PORT})"
    )

    args = parser.parse_args()

    receiver = HelixReceiver(port=args.port)
    receiver.start()
    receiver.stop()


if __name__ == "__main__":
    main()

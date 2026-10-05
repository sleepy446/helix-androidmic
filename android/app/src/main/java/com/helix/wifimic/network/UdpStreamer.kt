package com.helix.wifimic.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * UDP streamer untuk mengirim audio data ke PC
 */
class UdpStreamer(
    private val ipAddress: String,
    private val port: Int
) {
    private var socket: DatagramSocket? = null
    private var targetAddress: InetAddress? = null
    private var isConnected = false
    private var packetsSent = 0L

    companion object {
        private const val TAG = "UdpStreamer"
    }

    /**
     * Initialize UDP socket
     */
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            targetAddress = InetAddress.getByName(ipAddress)
            socket = DatagramSocket()

            // Set socket options untuk performance
            socket?.apply {
                sendBufferSize = 65536 // 64KB buffer
                trafficClass = 0x10 // IPTOS_LOWDELAY untuk low latency
            }

            isConnected = true
            packetsSent = 0
            Log.i(TAG, "UDP socket connected to $ipAddress:$port")
            Log.i(TAG, "Send buffer size: ${socket?.sendBufferSize} bytes")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting UDP socket: ${e.message}")
            isConnected = false
            false
        }
    }

    /**
     * Send audio data via UDP
     */
    suspend fun sendAudioData(data: ByteArray, size: Int): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected || socket == null || targetAddress == null) {
            Log.e(TAG, "Socket not connected")
            return@withContext false
        }

        try {
            val packet = DatagramPacket(
                data,
                size,
                targetAddress,
                port
            )

            socket?.send(packet)
            packetsSent++

            // Log setiap 1000 packets
            if (packetsSent % 1000 == 0L) {
                Log.d(TAG, "Sent $packetsSent packets")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending UDP packet: ${e.message}")
            false
        }
    }

    /**
     * Disconnect and release resources
     */
    fun disconnect() {
        isConnected = false
        try {
            socket?.close()
            socket = null
            Log.i(TAG, "UDP socket disconnected. Total packets sent: $packetsSent")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting UDP socket: ${e.message}")
        }
    }

    /**
     * Check if connected
     */
    fun isConnected(): Boolean = isConnected

    /**
     * Get packets sent count
     */
    fun getPacketsSent(): Long = packetsSent

    /**
     * Validate IP address format
     */
    companion object {
        fun isValidIpAddress(ip: String): Boolean {
            val ipPattern = Regex(
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
            )
            return ipPattern.matches(ip)
        }
    }
}

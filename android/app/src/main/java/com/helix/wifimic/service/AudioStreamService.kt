package com.helix.wifimic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.helix.wifimic.MainActivity
import com.helix.wifimic.R
import com.helix.wifimic.audio.AudioCapture
import com.helix.wifimic.audio.AudioConfig
import com.helix.wifimic.network.UdpStreamer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service untuk streaming audio
 */
class AudioStreamService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var audioCapture: AudioCapture? = null
    private var udpStreamer: UdpStreamer? = null
    private var isStreaming = false

    private var streamingCallback: StreamingCallback? = null

    companion object {
        private const val TAG = "AudioStreamService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "helix_audio_stream_channel"

        const val ACTION_START_STREAMING = "com.helix.wifimic.START_STREAMING"
        const val ACTION_STOP_STREAMING = "com.helix.wifimic.STOP_STREAMING"
        const val EXTRA_IP_ADDRESS = "ip_address"
        const val EXTRA_PORT = "port"
    }

    /**
     * Callback interface untuk status streaming
     */
    interface StreamingCallback {
        fun onStreamingStarted()
        fun onStreamingStopped()
        fun onStreamingError(error: String)
        fun onPacketsSent(count: Long)
    }

    /**
     * Binder untuk komunikasi dengan Activity
     */
    inner class LocalBinder : Binder() {
        fun getService(): AudioStreamService = this@AudioStreamService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_STREAMING -> {
                val ipAddress = intent.getStringExtra(EXTRA_IP_ADDRESS) ?: ""
                val port = intent.getIntExtra(EXTRA_PORT, AudioConfig.DEFAULT_PORT)
                startStreaming(ipAddress, port)
            }
            ACTION_STOP_STREAMING -> {
                stopStreaming()
            }
        }
        return START_STICKY
    }

    /**
     * Set callback untuk status updates
     */
    fun setStreamingCallback(callback: StreamingCallback?) {
        this.streamingCallback = callback
    }

    /**
     * Start audio streaming
     */
    fun startStreaming(ipAddress: String, port: Int = AudioConfig.DEFAULT_PORT) {
        if (isStreaming) {
            Log.w(TAG, "Already streaming")
            return
        }

        Log.i(TAG, "Starting streaming to $ipAddress:$port")

        // Start foreground service dengan notification
        startForeground(NOTIFICATION_ID, createNotification("Connecting..."))

        serviceScope.launch {
            try {
                // Initialize audio capture
                audioCapture = AudioCapture()
                if (!audioCapture!!.initialize()) {
                    handleError("Failed to initialize audio capture")
                    return@launch
                }

                // Initialize UDP streamer
                udpStreamer = UdpStreamer(ipAddress, port)
                if (!udpStreamer!!.connect()) {
                    handleError("Failed to connect to $ipAddress:$port")
                    return@launch
                }

                // Update notification
                updateNotification("Streaming to $ipAddress:$port")

                isStreaming = true
                streamingCallback?.onStreamingStarted()

                // Start recording dan streaming
                audioCapture!!.startRecording(object : AudioCapture.AudioDataCallback {
                    override fun onAudioData(data: ByteArray, size: Int) {
                        serviceScope.launch {
                            udpStreamer?.sendAudioData(data, size)

                            // Update packets sent setiap 1000 packets
                            val packetsSent = udpStreamer?.getPacketsSent() ?: 0
                            if (packetsSent % 1000 == 0L) {
                                streamingCallback?.onPacketsSent(packetsSent)
                            }
                        }
                    }

                    override fun onError(error: String) {
                        handleError(error)
                    }
                })

            } catch (e: Exception) {
                handleError(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Stop audio streaming
     */
    fun stopStreaming() {
        if (!isStreaming) {
            Log.w(TAG, "Not streaming")
            return
        }

        Log.i(TAG, "Stopping streaming")

        isStreaming = false

        // Stop audio capture
        audioCapture?.release()
        audioCapture = null

        // Disconnect UDP
        udpStreamer?.disconnect()
        udpStreamer = null

        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        streamingCallback?.onStreamingStopped()
        Log.i(TAG, "Streaming stopped")
    }

    /**
     * Handle error
     */
    private fun handleError(error: String) {
        Log.e(TAG, "Error: $error")
        isStreaming = false

        audioCapture?.release()
        audioCapture = null

        udpStreamer?.disconnect()
        udpStreamer = null

        updateNotification("Error: $error")
        streamingCallback?.onStreamingError(error)

        // Stop service setelah delay
        serviceScope.launch {
            kotlinx.coroutines.delay(3000)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    /**
     * Check if currently streaming
     */
    fun isStreaming(): Boolean = isStreaming

    override fun onDestroy() {
        super.onDestroy()
        stopStreaming()
        serviceScope.cancel()
        Log.i(TAG, "Service destroyed")
    }

    /**
     * Create notification channel (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setSound(null, null)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create notification
     */
    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    /**
     * Update notification text
     */
    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

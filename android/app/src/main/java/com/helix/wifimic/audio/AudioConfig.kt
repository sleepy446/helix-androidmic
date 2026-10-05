package com.helix.wifimic.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

/**
 * Audio configuration constants
 */
object AudioConfig {
    // Audio format configuration
    const val SAMPLE_RATE = 48000 // Hz
    const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC

    // Buffer size
    val BUFFER_SIZE: Int = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        CHANNEL_CONFIG,
        AUDIO_FORMAT
    ) * 2 // 2x untuk stability

    // Network configuration
    const val DEFAULT_PORT = 5555
    const val UDP_PACKET_SIZE = 1024 // bytes per packet
}

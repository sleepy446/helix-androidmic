package com.helix.wifimic.audio

import android.media.AudioRecord
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioCapture {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    companion object {
        private const val TAG = "AudioCapture"
    }

    interface AudioDataCallback {
        fun onAudioData(data: ByteArray, size: Int)
        fun onError(error: String)
    }

    fun initialize(): Boolean {
        return try {
            audioRecord = AudioRecord(
                AudioConfig.AUDIO_SOURCE,
                AudioConfig.SAMPLE_RATE,
                AudioConfig.CHANNEL_CONFIG,
                AudioConfig.AUDIO_FORMAT,
                AudioConfig.BUFFER_SIZE
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                return false
            }

            Log.i(TAG, "AudioRecord initialized successfully")
            Log.i(TAG, "Sample Rate: ${AudioConfig.SAMPLE_RATE} Hz")
            Log.i(TAG, "Buffer Size: ${AudioConfig.BUFFER_SIZE} bytes")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AudioRecord: ${e.message}")
            false
        }
    }

    suspend fun startRecording(callback: AudioDataCallback) = withContext(Dispatchers.IO) {
        if (audioRecord == null) {
            callback.onError("AudioRecord not initialized")
            return@withContext
        }

        try {
            audioRecord?.startRecording()
            isRecording = true
            Log.i(TAG, "Recording started")

            val buffer = ByteArray(AudioConfig.UDP_PACKET_SIZE)

            while (isRecording) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                if (readSize > 0) {
                    callback.onAudioData(buffer, readSize)
                } else if (readSize < 0) {
                    Log.e(TAG, "Error reading audio data: $readSize")
                    when (readSize) {
                        AudioRecord.ERROR_INVALID_OPERATION -> {
                            callback.onError("Invalid operation")
                        }
                        AudioRecord.ERROR_BAD_VALUE -> {
                            callback.onError("Bad value")
                        }
                        AudioRecord.ERROR_DEAD_OBJECT -> {
                            callback.onError("Dead object")
                        }
                        else -> {
                            callback.onError("Unknown error: $readSize")
                        }
                    }
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during recording: ${e.message}")
            callback.onError(e.message ?: "Unknown error")
        }
    }

    fun stopRecording() {
        isRecording = false
        try {
            audioRecord?.stop()
            Log.i(TAG, "Recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording: ${e.message}")
        }
    }

    fun release() {
        stopRecording()
        try {
            audioRecord?.release()
            audioRecord = null
            Log.i(TAG, "AudioRecord released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioRecord: ${e.message}")
        }
    }

    fun isRecording(): Boolean = isRecording

    fun getRecordingState(): Int = audioRecord?.recordingState ?: AudioRecord.RECORDSTATE_STOPPED
}

package com.helix.wifimic

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.helix.wifimic.audio.AudioConfig
import com.helix.wifimic.network.UdpStreamer
import com.helix.wifimic.service.AudioStreamService
import com.helix.wifimic.ui.theme.HelixWiFiMicTheme
import com.helix.wifimic.ui.theme.StreamingGreen
import com.helix.wifimic.ui.theme.StreamingRed

class MainActivity : ComponentActivity() {

    private var audioStreamService: AudioStreamService? = null
    private var isServiceBound = false

    private var isStreaming by mutableStateOf(false)
    private var ipAddress by mutableStateOf("")
    private var port by mutableStateOf(AudioConfig.DEFAULT_PORT.toString())
    private var statusMessage by mutableStateOf("Ready")
    private var packetsSent by mutableStateOf(0L)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions required to record audio", Toast.LENGTH_LONG).show()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioStreamService.LocalBinder
            audioStreamService = binder.getService()
            isServiceBound = true

            audioStreamService?.setStreamingCallback(object : AudioStreamService.StreamingCallback {
                override fun onStreamingStarted() {
                    runOnUiThread {
                        isStreaming = true
                        statusMessage = "Streaming to $ipAddress:$port"
                    }
                }

                override fun onStreamingStopped() {
                    runOnUiThread {
                        isStreaming = false
                        statusMessage = "Stopped"
                        packetsSent = 0
                    }
                }

                override fun onStreamingError(error: String) {
                    runOnUiThread {
                        isStreaming = false
                        statusMessage = "Error: $error"
                        Toast.makeText(
                            this@MainActivity,
                            "Error: $error",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onPacketsSent(count: Long) {
                    runOnUiThread {
                        packetsSent = count
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioStreamService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        setContent {
            HelixWiFiMicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        ipAddress = ipAddress,
                        onIpAddressChange = { ipAddress = it },
                        port = port,
                        onPortChange = { port = it },
                        isStreaming = isStreaming,
                        statusMessage = statusMessage,
                        packetsSent = packetsSent,
                        onStartStopClick = { toggleStreaming() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, AudioStreamService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun toggleStreaming() {
        if (!checkPermissions()) {
            Toast.makeText(this, "Please grant audio recording permission", Toast.LENGTH_LONG).show()
            checkAndRequestPermissions()
            return
        }

        if (isStreaming) {
            stopStreaming()
        } else {
            startStreaming()
        }
    }

    private fun startStreaming() {
        if (!UdpStreamer.isValidIpAddress(ipAddress)) {
            Toast.makeText(this, "Invalid IP address", Toast.LENGTH_SHORT).show()
            return
        }

        val portNumber = port.toIntOrNull()
        if (portNumber == null || portNumber !in 1..65535) {
            Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, AudioStreamService::class.java).apply {
            action = AudioStreamService.ACTION_START_STREAMING
            putExtra(AudioStreamService.EXTRA_IP_ADDRESS, ipAddress)
            putExtra(AudioStreamService.EXTRA_PORT, portNumber)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopStreaming() {
        audioStreamService?.stopStreaming()
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun MainScreen(
    ipAddress: String,
    onIpAddressChange: (String) -> Unit,
    port: String,
    onPortChange: (String) -> Unit,
    isStreaming: Boolean,
    statusMessage: String,
    packetsSent: Long,
    onStartStopClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Helix WiFi Mic",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isStreaming) StreamingGreen.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isStreaming) StreamingGreen else MaterialTheme.colorScheme.onSurface
                )
                if (isStreaming && packetsSent > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Packets sent: ${packetsSent}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = ipAddress,
            onValueChange = onIpAddressChange,
            label = { Text("PC IP Address") },
            placeholder = { Text("192.168.1.100") },
            enabled = !isStreaming,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = port,
            onValueChange = onPortChange,
            label = { Text("Port") },
            placeholder = { Text(AudioConfig.DEFAULT_PORT.toString()) },
            enabled = !isStreaming,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartStopClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isStreaming) StreamingRed else StreamingGreen
            )
        ) {
            Icon(
                imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isStreaming) "Stop Streaming" else "Start Streaming",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Start the receiver on your PC\n" +
                            "2. Enter your PC's IP address\n" +
                            "3. Tap 'Start Streaming'\n" +
                            "4. Select Virtual Audio Cable as input in your apps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

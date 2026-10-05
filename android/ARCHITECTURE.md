# Helix WiFi Mic - Android Architecture

## Overview

Aplikasi Android untuk streaming audio real-time dari mikrofon HP ke PC via UDP.

## Architecture Pattern

**MVVM-like dengan Compose** - Separation of concerns dengan layer yang jelas:

```
UI Layer (Compose) → Service Layer → Business Logic (Audio/Network)
```

## Components

### 1. UI Layer

#### MainActivity.kt
- **Role**: Main entry point, UI host
- **Responsibilities**:
  - Permission handling (RECORD_AUDIO, INTERNET, POST_NOTIFICATIONS)
  - Service binding dan lifecycle management
  - Compose UI hosting
  - User input validation
- **State Management**: Compose `mutableStateOf`
- **Communication**: ServiceConnection untuk bind ke AudioStreamService

#### UI Composables
- `MainScreen`: Main UI dengan input fields dan streaming control
- Material 3 Design dengan dynamic color support
- Reactive UI yang update berdasarkan service callbacks

### 2. Service Layer

#### AudioStreamService.kt
- **Type**: Foreground Service
- **Role**: Orchestrator untuk audio streaming
- **Responsibilities**:
  - Manage AudioCapture dan UdpStreamer lifecycle
  - Coordinate antara audio recording dan network streaming
  - Maintain foreground notification
  - Callback ke UI untuk status updates
- **Threading**: Coroutines (Main + IO dispatchers)
- **Lifecycle**: START_STICKY untuk survive process death

**Service Communication Flow:**
```
Activity → ServiceConnection → AudioStreamService
                                      ↓
                            AudioCapture + UdpStreamer
                                      ↓
                            StreamingCallback → Activity UI
```

### 3. Audio Layer

#### AudioCapture.kt
- **Role**: Audio recording handler
- **Technology**: Android AudioRecord API
- **Configuration**:
  - Sample Rate: 48000 Hz
  - Channel: MONO
  - Encoding: PCM_16BIT
  - Source: MIC
- **Buffer**: 2x minBufferSize untuk stability
- **Threading**: Coroutines IO dispatcher
- **Callback**: AudioDataCallback interface untuk deliver audio data

**Recording Flow:**
```
AudioRecord.startRecording()
    ↓
Read loop (1024 bytes/read)
    ↓
AudioDataCallback.onAudioData()
    ↓
To UdpStreamer
```

#### AudioConfig.kt
- **Role**: Centralized audio configuration
- **Constants**: Sample rate, format, buffer sizes, network config

### 4. Network Layer

#### UdpStreamer.kt
- **Role**: UDP packet transmission
- **Technology**: Java DatagramSocket
- **Configuration**:
  - Protocol: UDP (connectionless)
  - Default Port: 5555
  - Packet Size: 1024 bytes
  - Buffer: 64KB send buffer
- **Threading**: Coroutines IO dispatcher
- **Features**:
  - IP address validation
  - Packet counter untuk monitoring
  - Traffic class optimization (IPTOS_LOWDELAY)

**Streaming Flow:**
```
Audio bytes from AudioCapture
    ↓
DatagramPacket creation
    ↓
DatagramSocket.send() to PC
    ↓
Packet counter increment
```

### 5. UI Theme

#### Theme System
- **Color.kt**: Color palette definition
- **Type.kt**: Typography scale
- **Theme.kt**: Material 3 theme composition
- **Features**:
  - Dynamic color support (Android 12+)
  - Light/Dark mode
  - Custom colors untuk streaming states

## Data Flow

### Streaming Start Flow

```
User taps "Start Streaming"
    ↓
MainActivity validates input
    ↓
Start AudioStreamService (foreground)
    ↓
Service creates AudioCapture + UdpStreamer
    ↓
AudioCapture initializes AudioRecord
    ↓
UdpStreamer creates DatagramSocket
    ↓
AudioCapture starts recording loop
    ↓
Audio data → Callback → UdpStreamer → UDP packets → PC
    ↓
Service callbacks update UI state
```

### Streaming Stop Flow

```
User taps "Stop Streaming" / Service stopped
    ↓
Service.stopStreaming()
    ↓
AudioCapture.stopRecording() + release()
    ↓
UdpStreamer.disconnect() + close socket
    ↓
Stop foreground service + remove notification
    ↓
Service callbacks update UI state
```

## Threading Model

### Main Thread
- UI rendering (Compose)
- Service binding
- State updates

### IO Dispatcher (Coroutines)
- Audio recording loop
- UDP packet transmission
- Network operations

### Supervision
- SupervisorJob untuk coroutine scope
- Isolated failure handling

## Error Handling

### Permission Errors
- Runtime permission checks
- Request launcher untuk missing permissions
- User-friendly error messages

### Audio Errors
- AudioRecord initialization failures
- Recording state errors
- Callback error reporting

### Network Errors
- Invalid IP address validation
- Socket connection failures
- Packet send failures
- Graceful degradation

## State Management

### Service State
- `isStreaming: Boolean` - Current streaming status
- `packetsSent: Long` - Packet counter
- `statusMessage: String` - User-facing status

### UI State (Compose)
- `mutableStateOf` untuk reactive updates
- Service callbacks trigger UI recomposition
- Input fields disabled saat streaming

## Permissions

### Required Permissions
1. **RECORD_AUDIO** - Dangerous, runtime request
2. **INTERNET** - Normal, auto-granted
3. **ACCESS_WIFI_STATE** - Normal, auto-granted
4. **FOREGROUND_SERVICE** - Normal, auto-granted
5. **FOREGROUND_SERVICE_MICROPHONE** - Foreground service type
6. **POST_NOTIFICATIONS** - Dangerous (Android 13+), runtime request

### Permission Flow
```
App launch → Check permissions
    ↓
Missing? → Request via ActivityResultContracts
    ↓
User grants → Enable streaming
    ↓
User denies → Show toast, disable streaming
```

## Performance Optimizations

### Audio
- 2x buffer size untuk menghindari buffer underrun
- Optimal packet size (1024 bytes)
- Minimal processing di audio thread

### Network
- UDP untuk low latency (vs TCP overhead)
- Large send buffer (64KB)
- Traffic class optimization
- Coroutines untuk non-blocking I/O

### UI
- Compose recomposition optimization
- Throttled packet counter updates (setiap 1000 packets)
- Minimal state updates

## Lifecycle Management

### Activity Lifecycle
- `onCreate`: Setup, request permissions
- `onStart`: Bind to service
- `onStop`: Unbind from service
- Service tetap running saat Activity destroyed

### Service Lifecycle
- `onCreate`: Initialize
- `onStartCommand`: Handle actions
- `onBind`: Return binder untuk Activity
- `onDestroy`: Cleanup resources

### Resource Cleanup
- AudioRecord release
- DatagramSocket close
- Coroutine scope cancel
- Notification remove

## Testing Considerations

### Unit Tests
- AudioConfig constants
- UdpStreamer IP validation
- Service state transitions

### Integration Tests
- Service binding
- Permission handling
- Streaming flow end-to-end

### Manual Testing
- Different devices (various Android versions)
- Different network conditions
- Background/foreground transitions
- Screen off scenarios

## Future Enhancements

### Potential Features
- USB connection via ADB port forwarding
- Audio quality settings (sample rate options)
- Latency monitoring
- Reconnection logic
- Audio compression (Opus codec)
- Bluetooth support
- Multiple PC connections

### Code Improvements
- Dependency injection (Hilt/Koin)
- Repository pattern untuk network
- ViewModel untuk better state management
- WorkManager untuk scheduled tasks
- Room database untuk connection history

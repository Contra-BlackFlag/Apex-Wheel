package com.racing.controller.network

import com.racing.controller.model.ConnectionState
import com.racing.controller.model.ControllerPacket
import com.racing.controller.model.HeartbeatPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong

class NetworkClient(private val scope: CoroutineScope) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _packetsSentCount = MutableStateFlow(0L)
    val packetsSentCount: StateFlow<Long> = _packetsSentCount.asStateFlow()

    private var socket: Socket? = null
    private var rawOutputStream: java.io.OutputStream? = null
    private var reader: BufferedReader? = null

    private var connectJob: Job? = null
    private var receiveJob: Job? = null
    private var heartbeatJob: Job? = null

    private val sequenceCounter = AtomicLong(0)
    private val sendMutex = Mutex()
    private var currentHost: String = ""
    private var currentPort: Int = 0

    fun connect(host: String, port: Int) {
        if (_connectionState.value is ConnectionState.Connecting ||
            _connectionState.value is ConnectionState.Connected
        ) {
            disconnect()
        }

        currentHost = host
        currentPort = port
        _connectionState.value = ConnectionState.Connecting

        connectJob = scope.launch(Dispatchers.IO) {
            try {
                val s = Socket()
                s.tcpNoDelay = true // Disable Nagle algorithm: transmit immediately without waiting for full frame
                try {
                    s.trafficClass = 0x10 // IPTOS_LOWDELAY (RFC 1349 low-delay flag)
                } catch (_: Exception) {}
                s.sendBufferSize = 1024 // Tiny buffer to eliminate kernel buffering
                s.receiveBufferSize = 1024
                s.soTimeout = 4000 // 4s read timeout
                s.connect(InetSocketAddress(host, port), 3500)

                socket = s
                rawOutputStream = s.getOutputStream()
                reader = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.UTF_8))

                _connectionState.value = ConnectionState.Connected(host, port, pingMs = 0)

                // Start heartbeat loop
                startHeartbeat()

                // Start receive listener loop
                startReceiveLoop()
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Connection failed: ${e.localizedMessage ?: e.message}")
                cleanup()
            }
        }
    }

    fun disconnect() {
        scope.launch(Dispatchers.IO) {
            cleanup()
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    suspend fun sendControllerPacket(packet: ControllerPacket) = withContext(Dispatchers.IO) {
        val out = rawOutputStream ?: return@withContext
        val s = socket ?: return@withContext
        if (!s.isConnected || s.isClosed) return@withContext

        try {
            val jsonBytes = (json.encodeToString(packet) + "\n").toByteArray(Charsets.UTF_8)
            sendMutex.withLock {
                out.write(jsonBytes)
                out.flush()
            }
            _packetsSentCount.value++
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Send failed: ${e.localizedMessage}")
            cleanup()
        }
    }

    fun getNextSequence(): Long = sequenceCounter.incrementAndGet()

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(1000)
                val out = rawOutputStream ?: break
                val s = socket ?: break
                if (!s.isConnected || s.isClosed) break

                try {
                    val ping = HeartbeatPacket(timestamp = System.currentTimeMillis())
                    val pingBytes = (json.encodeToString(ping) + "\n").toByteArray(Charsets.UTF_8)
                    sendMutex.withLock {
                        out.write(pingBytes)
                        out.flush()
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun startReceiveLoop() {
        receiveJob?.cancel()
        receiveJob = scope.launch(Dispatchers.IO) {
            val r = reader ?: return@launch
            try {
                while (isActive) {
                    val line = r.readLine() ?: break
                    if (line.isBlank()) continue

                    try {
                        val parsed = json.parseToJsonElement(line).jsonObject
                        val type = parsed["type"]?.jsonPrimitive?.content
                        if (type == "pong") {
                            val originalTime = parsed["t"]?.jsonPrimitive?.long ?: 0L
                            if (originalTime > 0) {
                                val rtt = System.currentTimeMillis() - originalTime
                                val state = _connectionState.value
                                if (state is ConnectionState.Connected) {
                                    _connectionState.value = state.copy(pingMs = rtt)
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // ignore malformed responses
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    _connectionState.value = ConnectionState.Error("Disconnected from server: ${e.localizedMessage}")
                }
            } finally {
                cleanup()
            }
        }
    }

    private fun cleanup() {
        heartbeatJob?.cancel()
        receiveJob?.cancel()
        connectJob?.cancel()

        try {
            rawOutputStream?.close()
        } catch (_: Exception) {}
        try {
            reader?.close()
        } catch (_: Exception) {}
        try {
            socket?.close()
        } catch (_: Exception) {}

        rawOutputStream = null
        reader = null
        socket = null
    }
}

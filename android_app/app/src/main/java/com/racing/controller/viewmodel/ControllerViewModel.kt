package com.racing.controller.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.racing.controller.model.ConnectionState
import com.racing.controller.model.ControllerInputs
import com.racing.controller.model.ControllerPacket
import com.racing.controller.model.ControllerSettings
import com.racing.controller.network.NetworkClient
import com.racing.controller.sensors.SteeringSensorManager
import com.racing.controller.settings.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ControllerViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    val settingsState: StateFlow<ControllerSettings> = settingsRepository.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ControllerSettings()
    )

    val sensorManager = SteeringSensorManager(application)
    val networkClient = NetworkClient(viewModelScope)

    val connectionState: StateFlow<ConnectionState> = networkClient.connectionState
    val steeringAngleDegrees: StateFlow<Float> = sensorManager.currentSteeringAngleDegrees
    val steeringNormalized: StateFlow<Float> = sensorManager.steeringOutput

    // Touch inputs state
    private val _inputs = MutableStateFlow(ControllerInputs())
    val inputs: StateFlow<ControllerInputs> = _inputs.asStateFlow()

    private var transmitJob: Job? = null

    // Vibrator
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        // Observe settings changes and update sensor manager
        viewModelScope.launch {
            settingsState.collect { newSettings ->
                sensorManager.settings = newSettings
                restartTransmitLoop(newSettings.updateRateHz)
            }
        }

        // Start transmitting
        restartTransmitLoop(settingsState.value.updateRateHz)
    }

    fun onResumeScreen() {
        if (!settingsState.value.useTouchSteering) {
            sensorManager.startListening()
        }
    }

    fun onPauseScreen() {
        sensorManager.stopListening()
    }

    fun calibrateSteering() {
        vibrate(30)
        sensorManager.calibrateNeutral()
    }

    fun connect(host: String, port: Int) {
        viewModelScope.launch {
            settingsRepository.saveSettings(settingsState.value.copy(hostIp = host, port = port))
            networkClient.connect(host, port)
        }
    }

    fun disconnect() {
        networkClient.disconnect()
    }

    // Input press/release handlers
    fun setThrottle(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        if (_inputs.value.throttle != clamped) {
            _inputs.value = _inputs.value.copy(throttle = clamped)
            if (clamped > 0.05f && _inputs.value.throttle <= 0.05f) vibrate(12)
        }
    }

    fun setBrake(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        if (_inputs.value.brake != clamped) {
            _inputs.value = _inputs.value.copy(brake = clamped)
            if (clamped > 0.05f && _inputs.value.brake <= 0.05f) vibrate(15)
        }
    }

    fun setClutch(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        if (_inputs.value.clutch != clamped) {
            _inputs.value = _inputs.value.copy(clutch = clamped)
            if (clamped > 0.05f && _inputs.value.clutch <= 0.05f) vibrate(12)
        }
    }

    fun setHandbrake(pressed: Boolean) {
        if (_inputs.value.handbrake != pressed) {
            _inputs.value = _inputs.value.copy(handbrake = pressed)
            if (pressed) vibrate(25)
        }
    }

    fun setGearUp(pressed: Boolean) {
        if (_inputs.value.gearUp != pressed) {
            _inputs.value = _inputs.value.copy(gearUp = pressed)
            if (pressed) vibrate(35)
        }
    }

    fun setGearDown(pressed: Boolean) {
        if (_inputs.value.gearDown != pressed) {
            _inputs.value = _inputs.value.copy(gearDown = pressed)
            if (pressed) vibrate(35)
        }
    }

    fun setSelect(pressed: Boolean) {
        if (_inputs.value.select != pressed) {
            _inputs.value = _inputs.value.copy(select = pressed)
            if (pressed) vibrate(15)
        }
    }

    fun setStart(pressed: Boolean) {
        if (_inputs.value.start != pressed) {
            _inputs.value = _inputs.value.copy(start = pressed)
            if (pressed) vibrate(15)
        }
    }

    fun setTouchSteering(value: Float) {
        if (settingsState.value.useTouchSteering) {
            sensorManager.setManualSteering(value)
        }
    }

    fun saveSettings(newSettings: ControllerSettings) {
        viewModelScope.launch {
            settingsRepository.saveSettings(newSettings)
        }
    }

    fun resetSettingsToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
        }
    }

    private fun restartTransmitLoop(hz: Int) {
        transmitJob?.cancel()
        val intervalMs = (1000L / hz.coerceIn(20, 120)).coerceAtLeast(8L)

        transmitJob = viewModelScope.launch {
            while (isActive) {
                delay(intervalMs)
                if (networkClient.connectionState.value is ConnectionState.Connected) {
                    val currentInputs = _inputs.value.copy(
                        steering = sensorManager.steeringOutput.value
                    )
                    val packet = ControllerPacket(
                        sequenceNumber = networkClient.getNextSequence(),
                        timestamp = System.currentTimeMillis(),
                        inputs = currentInputs
                    )
                    networkClient.sendControllerPacket(packet)
                }
            }
        }
    }

    private fun vibrate(durationMs: Long) {
        if (!settingsState.value.hapticFeedbackEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopListening()
        networkClient.disconnect()
        transmitJob?.cancel()
    }
}

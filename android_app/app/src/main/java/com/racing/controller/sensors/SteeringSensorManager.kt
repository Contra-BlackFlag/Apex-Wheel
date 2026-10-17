package com.racing.controller.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.racing.controller.model.ControllerSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sign

class SteeringSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _steeringOutput = MutableStateFlow(0f)
    val steeringOutput: StateFlow<Float> = _steeringOutput.asStateFlow()

    private val _currentSteeringAngleDegrees = MutableStateFlow(0f)
    val currentSteeringAngleDegrees: StateFlow<Float> = _currentSteeringAngleDegrees.asStateFlow()

    private val _isCalibrated = MutableStateFlow(false)
    val isCalibrated: StateFlow<Boolean> = _isCalibrated.asStateFlow()

    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    // Fallback sensor reading caches
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var hasAccelerometer = false
    private var hasMagnetometer = false

    // Calibration neutral quaternion [x, y, z, w]
    private var neutralQuat: FloatArray? = null

    // Filter state
    private var smoothedSteering: Float = 0f

    // Active settings
    var settings: ControllerSettings = ControllerSettings()

    init {
        // Prefer game rotation vector (no magnetic interference), then standard rotation vector
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationVectorSensor == null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }
    }

    fun startListening() {
        val delay = SensorManager.SENSOR_DELAY_FASTEST
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, delay)
        } ?: run {
            accelerometerSensor?.let { sensorManager.registerListener(this, it, delay) }
            magnetometerSensor?.let { sensorManager.registerListener(this, it, delay) }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    fun calibrateNeutral() {
        neutralQuat = currentRawQuat.clone()
        smoothedSteering = 0f
        _steeringOutput.value = 0f
        _currentSteeringAngleDegrees.value = 0f
        _isCalibrated.value = true
    }

    private var currentRawQuat = FloatArray(4) { 0f }.apply { this[3] = 1f }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR ||
            event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR
        ) {
            SensorManager.getQuaternionFromVector(currentRawQuat, event.values)
            processOrientationQuaternion(currentRawQuat)
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, 3)
            hasAccelerometer = true
            if (hasMagnetometer) {
                processFallbackOrientation()
            }
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, 3)
            hasMagnetometer = true
            if (hasAccelerometer) {
                processFallbackOrientation()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private fun processFallbackOrientation() {
        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            inclinationMatrix,
            lastAccelerometer,
            lastMagnetometer
        )
        if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            // Roll in landscape orientation: azimuth, pitch, roll
            // For landscape, phone tilt left/right corresponds to pitch or roll depending on orientation.
            val rawAngleDeg = (orientation[2] * 180f / PI.toFloat())
            updateSteeringFromAngle(rawAngleDeg)
        }
    }

    private fun processOrientationQuaternion(quat: FloatArray) {
        if (neutralQuat == null) {
            // Auto calibrate on first sensor tick if not calibrated yet
            neutralQuat = quat.clone()
            _isCalibrated.value = true
        }

        val nQ = neutralQuat ?: return

        // Compute relative rotation Q_rel = Q_neutral^(-1) * Q_current
        // Inverse of unit quaternion [x, y, z, w] is [-x, -y, -z, w]
        val nInvX = -nQ[0]
        val nInvY = -nQ[1]
        val nInvZ = -nQ[2]
        val nInvW = nQ[3]

        val curX = quat[0]
        val curY = quat[1]
        val curZ = quat[2]
        val curW = quat[3]

        // Quaternion multiplication:
        // qRel = nInv * cur
        val relW = nInvW * curW - nInvX * curX - nInvY * curY - nInvZ * curZ
        val relX = nInvW * curX + nInvX * curW + nInvY * curZ - nInvZ * curY
        val relY = nInvW * curY - nInvX * curZ + nInvY * curW + nInvZ * curX
        val relZ = nInvW * curZ + nInvX * curY - nInvY * curX + nInvZ * curW

        // Convert relative quaternion to relative rotation matrix
        val rotMatrix = FloatArray(9)
        val relQuat = floatArrayOf(relX, relY, relZ, relW)
        quaternionToRotationMatrix(relQuat, rotMatrix)

        // For landscape holding (steering wheel), left/right roll is rotation around phone screen normal (Z body axis)
        // or around the Y axis depending on landscape.
        // We calculate the angle using atan2:
        val angleRad = atan2(rotMatrix[1], rotMatrix[0])
        val angleDeg = (angleRad * 180f / PI.toFloat())

        updateSteeringFromAngle(angleDeg)
    }

    fun updateSteeringFromAngle(angleDeg: Float) {
        val maxAngle = settings.maxSteeringAngleDegrees.coerceAtLeast(5.0f)
        val deadZoneDegrees = (settings.deadZonePercent / 100f) * maxAngle

        _currentSteeringAngleDegrees.value = angleDeg

        val normalized = computeNormalizedSteering(
            angleDeg = angleDeg,
            deadZoneDegrees = deadZoneDegrees,
            maxAngleDegrees = maxAngle,
            sensitivity = settings.steeringSensitivity,
            invert = settings.invertSteering
        )

        // Apply low-pass exponential smoothing filter
        val alpha = (1.0f - settings.smoothingFactor).coerceIn(0.05f, 1.0f)
        smoothedSteering = smoothedSteering + alpha * (normalized - smoothedSteering)

        _steeringOutput.value = smoothedSteering.coerceIn(-1.0f, 1.0f)
    }

    fun setManualSteering(value: Float) {
        val clamped = value.coerceIn(-1.0f, 1.0f)
        smoothedSteering = clamped
        _steeringOutput.value = clamped
        _currentSteeringAngleDegrees.value = clamped * settings.maxSteeringAngleDegrees
    }

    companion object {
        fun computeNormalizedSteering(
            angleDeg: Float,
            deadZoneDegrees: Float,
            maxAngleDegrees: Float,
            sensitivity: Float,
            invert: Boolean
        ): Float {
            val absAngle = abs(angleDeg)
            if (absAngle <= deadZoneDegrees) {
                return 0f
            }

            // Remap [deadZone, maxAngle] -> [0.0, 1.0]
            val activeRange = maxAngleDegrees - deadZoneDegrees
            if (activeRange <= 0.001f) return 0f

            val unclampedRatio = (absAngle - deadZoneDegrees) / activeRange
            val scaled = (unclampedRatio * sensitivity).coerceIn(0f, 1f)
            val signed = sign(angleDeg) * scaled

            return if (invert) -signed else signed
        }

        private fun quaternionToRotationMatrix(q: FloatArray, m: FloatArray) {
            val x = q[0]
            val y = q[1]
            val z = q[2]
            val w = q[3]

            val xx = x * x
            val xy = x * y
            val xz = x * z
            val xw = x * w

            val yy = y * y
            val yz = y * z
            val yw = y * w

            val zz = z * z
            val zw = z * w

            m[0] = 1f - 2f * (yy + zz)
            m[1] = 2f * (xy - zw)
            m[2] = 2f * (xz + yw)

            m[3] = 2f * (xy + zw)
            m[4] = 1f - 2f * (xx + zz)
            m[5] = 2f * (yz - xw)

            m[6] = 2f * (xz - yw)
            m[7] = 2f * (yz + xw)
            m[8] = 1f - 2f * (xx + yy)
        }
    }
}

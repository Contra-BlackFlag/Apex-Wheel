package com.racing.controller.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.racing.controller.model.ControllerSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "controller_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val HOST_IP = stringPreferencesKey("host_ip")
        val PORT = intPreferencesKey("port")
        val STEERING_SENSITIVITY = floatPreferencesKey("steering_sensitivity")
        val MAX_STEERING_ANGLE = floatPreferencesKey("max_steering_angle")
        val DEAD_ZONE_PERCENT = floatPreferencesKey("dead_zone_percent")
        val INVERT_STEERING = booleanPreferencesKey("invert_steering")
        val SMOOTHING_FACTOR = floatPreferencesKey("smoothing_factor")
        val USE_TOUCH_STEERING = booleanPreferencesKey("use_touch_steering")
        val UPDATE_RATE_HZ = intPreferencesKey("update_rate_hz")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val THROTTLE_RAMP = booleanPreferencesKey("throttle_ramp")
        val SELECTED_TEAM = stringPreferencesKey("selected_team")
        val BUTTON_SHAPE = stringPreferencesKey("button_shape")
        val BUTTON_SCALE = floatPreferencesKey("button_scale")
        val SWAP_THROTTLE_BRAKE = booleanPreferencesKey("swap_throttle_brake")
        val SHOW_REV_LIGHTS = booleanPreferencesKey("show_rev_lights")
        val SHOW_STEERING_DEGREE = booleanPreferencesKey("show_steering_degree")
        val SHOW_CLUTCH = booleanPreferencesKey("show_clutch")
        val SHOW_HANDBRAKE = booleanPreferencesKey("show_handbrake")
        val HUD_LAYOUT_JSON = stringPreferencesKey("hud_layout_json")
    }

    val settingsFlow: Flow<ControllerSettings> = context.dataStore.data.map { preferences ->
        ControllerSettings(
            hostIp = preferences[PreferencesKeys.HOST_IP] ?: "192.168.1.100",
            port = preferences[PreferencesKeys.PORT] ?: 9876,
            steeringSensitivity = preferences[PreferencesKeys.STEERING_SENSITIVITY] ?: 1.0f,
            maxSteeringAngleDegrees = preferences[PreferencesKeys.MAX_STEERING_ANGLE] ?: 45.0f,
            deadZonePercent = preferences[PreferencesKeys.DEAD_ZONE_PERCENT] ?: 3.0f,
            invertSteering = preferences[PreferencesKeys.INVERT_STEERING] ?: false,
            smoothingFactor = preferences[PreferencesKeys.SMOOTHING_FACTOR] ?: 0.20f,
            useTouchSteering = preferences[PreferencesKeys.USE_TOUCH_STEERING] ?: false,
            updateRateHz = preferences[PreferencesKeys.UPDATE_RATE_HZ] ?: 60,
            hapticFeedbackEnabled = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true,
            throttleRampEnabled = preferences[PreferencesKeys.THROTTLE_RAMP] ?: false,
            selectedTeam = preferences[PreferencesKeys.SELECTED_TEAM] ?: "red_bull",
            buttonShape = preferences[PreferencesKeys.BUTTON_SHAPE] ?: "circle",
            buttonScale = preferences[PreferencesKeys.BUTTON_SCALE] ?: 1.0f,
            swapThrottleBrake = preferences[PreferencesKeys.SWAP_THROTTLE_BRAKE] ?: false,
            showRevLights = preferences[PreferencesKeys.SHOW_REV_LIGHTS] ?: true,
            showSteeringDegree = preferences[PreferencesKeys.SHOW_STEERING_DEGREE] ?: true,
            showClutch = preferences[PreferencesKeys.SHOW_CLUTCH] ?: true,
            showHandbrake = preferences[PreferencesKeys.SHOW_HANDBRAKE] ?: true,
            hudLayoutJson = preferences[PreferencesKeys.HUD_LAYOUT_JSON] ?: ""
        )
    }

    suspend fun saveSettings(settings: ControllerSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HOST_IP] = settings.hostIp
            preferences[PreferencesKeys.PORT] = settings.port
            preferences[PreferencesKeys.STEERING_SENSITIVITY] = settings.steeringSensitivity
            preferences[PreferencesKeys.MAX_STEERING_ANGLE] = settings.maxSteeringAngleDegrees
            preferences[PreferencesKeys.DEAD_ZONE_PERCENT] = settings.deadZonePercent
            preferences[PreferencesKeys.INVERT_STEERING] = settings.invertSteering
            preferences[PreferencesKeys.SMOOTHING_FACTOR] = settings.smoothingFactor
            preferences[PreferencesKeys.USE_TOUCH_STEERING] = settings.useTouchSteering
            preferences[PreferencesKeys.UPDATE_RATE_HZ] = settings.updateRateHz
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = settings.hapticFeedbackEnabled
            preferences[PreferencesKeys.THROTTLE_RAMP] = settings.throttleRampEnabled
            preferences[PreferencesKeys.SELECTED_TEAM] = settings.selectedTeam
            preferences[PreferencesKeys.BUTTON_SHAPE] = settings.buttonShape
            preferences[PreferencesKeys.BUTTON_SCALE] = settings.buttonScale
            preferences[PreferencesKeys.SWAP_THROTTLE_BRAKE] = settings.swapThrottleBrake
            preferences[PreferencesKeys.SHOW_REV_LIGHTS] = settings.showRevLights
            preferences[PreferencesKeys.SHOW_STEERING_DEGREE] = settings.showSteeringDegree
            preferences[PreferencesKeys.SHOW_CLUTCH] = settings.showClutch
            preferences[PreferencesKeys.SHOW_HANDBRAKE] = settings.showHandbrake
            preferences[PreferencesKeys.HUD_LAYOUT_JSON] = settings.hudLayoutJson
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

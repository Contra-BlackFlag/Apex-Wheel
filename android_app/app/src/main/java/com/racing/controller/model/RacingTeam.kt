package com.racing.controller.model

import androidx.compose.ui.graphics.Color

/**
 * Formula 1 & Motorsports Team Themes with signature colorways,
 * wheel rim styling, and badges.
 */
enum class RacingTeam(
    val id: String,
    val teamName: String,
    val shortName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val gripColor: Color,
    val surfaceColor: Color,
    val rimStripeColor: Color,
    val telemetryColor: Color
) {
    RED_BULL(
        id = "red_bull",
        teamName = "Oracle Red Bull Racing",
        shortName = "RED BULL",
        primaryColor = Color(0xFF001A30),
        secondaryColor = Color(0xFFE10600),
        accentColor = Color(0xFFFFD700),
        gripColor = Color(0xFF0A1220),
        surfaceColor = Color(0xFF060B14),
        rimStripeColor = Color(0xFFFFD700),
        telemetryColor = Color(0xFFE10600)
    ),
    FERRARI(
        id = "ferrari",
        teamName = "Scuderia Ferrari",
        shortName = "FERRARI",
        primaryColor = Color(0xFFE8002D),
        secondaryColor = Color(0xFFFFF200),
        accentColor = Color(0xFFFFFFFF),
        gripColor = Color(0xFF220508),
        surfaceColor = Color(0xFF140204),
        rimStripeColor = Color(0xFFFFF200),
        telemetryColor = Color(0xFFE8002D)
    ),
    MERCEDES(
        id = "mercedes",
        teamName = "Mercedes-AMG Petronas",
        shortName = "MERCEDES",
        primaryColor = Color(0xFF00A19B),
        secondaryColor = Color(0xFF00D2BE),
        accentColor = Color(0xFFC0C0C0),
        gripColor = Color(0xFF101618),
        surfaceColor = Color(0xFF080D0E),
        rimStripeColor = Color(0xFF00D2BE),
        telemetryColor = Color(0xFF00D2BE)
    ),
    MCLAREN(
        id = "mclaren",
        teamName = "McLaren Formula 1",
        shortName = "MCLAREN",
        primaryColor = Color(0xFFFF8000),
        secondaryColor = Color(0xFF47C7FC),
        accentColor = Color(0xFFFFFFFF),
        gripColor = Color(0xFF201306),
        surfaceColor = Color(0xFF120A03),
        rimStripeColor = Color(0xFFFF8000),
        telemetryColor = Color(0xFFFF8000)
    ),
    ASTON_MARTIN(
        id = "aston_martin",
        teamName = "Aston Martin Aramco",
        shortName = "ASTON MARTIN",
        primaryColor = Color(0xFF00594F),
        secondaryColor = Color(0xFFCEDC00),
        accentColor = Color(0xFFFFFFFF),
        gripColor = Color(0xFF061A16),
        surfaceColor = Color(0xFF030D0B),
        rimStripeColor = Color(0xFFCEDC00),
        telemetryColor = Color(0xFFCEDC00)
    ),
    CLASSIC(
        id = "classic",
        teamName = "Carbon Sim Pro",
        shortName = "SIM PRO",
        primaryColor = Color(0xFFE53935),
        secondaryColor = Color(0xFF454954),
        accentColor = Color(0xFFB0B4C0),
        gripColor = Color(0xFF181A1E),
        surfaceColor = Color(0xFF111215),
        rimStripeColor = Color(0xFFE53935),
        telemetryColor = Color(0xFFE53935)
    );

    companion object {
        fun fromId(id: String): RacingTeam {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: RED_BULL
        }
    }
}

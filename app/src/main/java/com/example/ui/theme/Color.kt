package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class OrbitThemePreset(
    val displayName: String,
    val isDark: Boolean,
    val previewPrimary: Color,
    val previewSurface: Color,
) {
    RADIX_INDIGO(
        displayName = "Radix Indigo",
        isDark = false,
        previewPrimary = Color(0xFF3E63DD),
        previewSurface = Color(0xFFFCFCFD),
    ),
    RADIX_OBSIDIAN(
        displayName = "Obsidian",
        isDark = true,
        previewPrimary = Color(0xFF5B7FFF),
        previewSurface = Color(0xFF111113),
    ),
    DAISY_NORD(
        displayName = "Nord",
        isDark = false,
        previewPrimary = Color(0xFF5E81AC),
        previewSurface = Color(0xFFECEFF4),
    ),
    DAISY_CUPCAKE(
        displayName = "Cupcake",
        isDark = false,
        previewPrimary = Color(0xFF0D9488),
        previewSurface = Color(0xFFFAF7F5),
    ),
    DAISY_DRACULA(
        displayName = "Dracula",
        isDark = true,
        previewPrimary = Color(0xFFBD93F9),
        previewSurface = Color(0xFF282A36),
    ),
}

enum class OrbitRadiusPreset(
    val displayName: String,
    val cardRadiusDp: Int,
    val controlRadiusDp: Int,
) {
    COMPACT("Sharp", 10, 8),
    BALANCED("Medium", 18, 14),
    PILL("Rounded", 26, 24),
}

// Keep semantic accent constants accessible across components
val SecureEmerald = Color(0xFF29A383)
val WarningAmber = Color(0xFFE59500)
val PrivateViolet = Color(0xFF8E4EC6)
val PrivatePlumDeep = Color(0xFF23113A)
val AuroraCyan = Color(0xFF00A2C7)

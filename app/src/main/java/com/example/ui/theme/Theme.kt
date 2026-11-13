package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class OrbitDesignTokens(
    val preset: OrbitThemePreset,
    val isDark: Boolean,
    // daisyUI base-100 / base-200 / base-300 + Radix 12-step neutral scale
    val base100: Color,
    val base200: Color,
    val base300: Color,
    val baseContent: Color,
    val mutedContent: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    // Radix accent steps (3=soft, 7=border, 9=solid, 11=softText)
    val accentSolid: Color,
    val accentSoft: Color,
    val accentSoftText: Color,
    val accentBorder: Color,
    val secondarySolid: Color,
    val secondarySoft: Color,
    val cardRadius: Dp,
    val controlRadius: Dp,
)

fun tokensFor(
    preset: OrbitThemePreset,
    radiusPreset: OrbitRadiusPreset,
): OrbitDesignTokens {
    val cardR = radiusPreset.cardRadiusDp.dp
    val ctrlR = radiusPreset.controlRadiusDp.dp
    return when (preset) {
        OrbitThemePreset.RADIX_INDIGO -> OrbitDesignTokens(
            preset = preset,
            isDark = false,
            base100 = Color(0xFFFFFFFF),
            base200 = Color(0xFFF9F9FB),
            base300 = Color(0xFFF0F0F3),
            baseContent = Color(0xFF1C2024),
            mutedContent = Color(0xFF60646C),
            borderSubtle = Color(0xFFE0E1E6),
            borderStrong = Color(0xFFCDCED6),
            accentSolid = Color(0xFF3E63DD),
            accentSoft = Color(0xFFEDF2FE),
            accentSoftText = Color(0xFF3451B2),
            accentBorder = Color(0xFFAEC0F5),
            secondarySolid = Color(0xFF00A2C7),
            secondarySoft = Color(0xFFE1F6FA),
            cardRadius = cardR,
            controlRadius = ctrlR,
        )

        OrbitThemePreset.RADIX_OBSIDIAN -> OrbitDesignTokens(
            preset = preset,
            isDark = true,
            base100 = Color(0xFF18191B),
            base200 = Color(0xFF111113),
            base300 = Color(0xFF212225),
            baseContent = Color(0xFFEEEEF0),
            mutedContent = Color(0xFFB0B4BA),
            borderSubtle = Color(0xFF2B2C30),
            borderStrong = Color(0xFF3A3B40),
            accentSolid = Color(0xFF5B7FFF),
            accentSoft = Color(0xFF1D284A),
            accentSoftText = Color(0xFF9EB1FF),
            accentBorder = Color(0xFF364B8B),
            secondarySolid = Color(0xFF22D3EE),
            secondarySoft = Color(0xFF123442),
            cardRadius = cardR,
            controlRadius = ctrlR,
        )

        OrbitThemePreset.DAISY_NORD -> OrbitDesignTokens(
            preset = preset,
            isDark = false,
            base100 = Color(0xFFF7F9FC),
            base200 = Color(0xFFECEFF4),
            base300 = Color(0xFFE5E9F0),
            baseContent = Color(0xFF2E3440),
            mutedContent = Color(0xFF4C566A),
            borderSubtle = Color(0xFFD8DEE9),
            borderStrong = Color(0xFFC2CCDC),
            accentSolid = Color(0xFF5E81AC),
            accentSoft = Color(0xFFE4ECF5),
            accentSoftText = Color(0xFF3B5A82),
            accentBorder = Color(0xFFA3BCD9),
            secondarySolid = Color(0xFF81A1C1),
            secondarySoft = Color(0xFFE8F1F8),
            cardRadius = cardR,
            controlRadius = ctrlR,
        )

        OrbitThemePreset.DAISY_CUPCAKE -> OrbitDesignTokens(
            preset = preset,
            isDark = false,
            base100 = Color(0xFFFFFDFB),
            base200 = Color(0xFFFAF7F5),
            base300 = Color(0xFFEFEAE6),
            baseContent = Color(0xFF291334),
            mutedContent = Color(0xFF6B5A74),
            borderSubtle = Color(0xFFE5DDD7),
            borderStrong = Color(0xFFD3C7BF),
            accentSolid = Color(0xFF0D9488),
            accentSoft = Color(0xFFE0F5F2),
            accentSoftText = Color(0xFF0B6E65),
            accentBorder = Color(0xFF99DCD4),
            secondarySolid = Color(0xFFEC4899),
            secondarySoft = Color(0xFFFCE7F3),
            cardRadius = cardR,
            controlRadius = ctrlR,
        )

        OrbitThemePreset.DAISY_DRACULA -> OrbitDesignTokens(
            preset = preset,
            isDark = true,
            base100 = Color(0xFF282A36),
            base200 = Color(0xFF21222C),
            base300 = Color(0xFF343746),
            baseContent = Color(0xFFF8F8F2),
            mutedContent = Color(0xFFB4BCD6),
            borderSubtle = Color(0xFF44475A),
            borderStrong = Color(0xFF565B73),
            accentSolid = Color(0xFFBD93F9),
            accentSoft = Color(0xFF382C56),
            accentSoftText = Color(0xFFD6BCFA),
            accentBorder = Color(0xFF6F53A8),
            secondarySolid = Color(0xFFFF79C6),
            secondarySoft = Color(0xFF4A2542),
            cardRadius = cardR,
            controlRadius = ctrlR,
        )
    }
}

val LocalOrbitTokens = staticCompositionLocalOf {
    tokensFor(OrbitThemePreset.RADIX_INDIGO, OrbitRadiusPreset.BALANCED)
}

@Composable
fun OrbitTheme(
    preset: OrbitThemePreset = OrbitThemePreset.RADIX_INDIGO,
    radiusPreset: OrbitRadiusPreset = OrbitRadiusPreset.BALANCED,
    content: @Composable () -> Unit,
) {
    val tokens = tokensFor(preset, radiusPreset)

    val colorScheme = if (tokens.isDark) {
        darkColorScheme(
            primary = tokens.accentSolid,
            onPrimary = Color(0xFF0B0E17),
            primaryContainer = tokens.accentSoft,
            onPrimaryContainer = tokens.accentSoftText,
            secondary = tokens.secondarySolid,
            onSecondary = Color(0xFF08141A),
            secondaryContainer = tokens.secondarySoft,
            tertiary = PrivateViolet,
            background = tokens.base200,
            onBackground = tokens.baseContent,
            surface = tokens.base100,
            onSurface = tokens.baseContent,
            surfaceVariant = tokens.base300,
            onSurfaceVariant = tokens.mutedContent,
            outline = tokens.borderSubtle,
        )
    } else {
        lightColorScheme(
            primary = tokens.accentSolid,
            onPrimary = Color.White,
            primaryContainer = tokens.accentSoft,
            onPrimaryContainer = tokens.accentSoftText,
            secondary = tokens.secondarySolid,
            onSecondary = Color.White,
            secondaryContainer = tokens.secondarySoft,
            tertiary = PrivateViolet,
            background = tokens.base200,
            onBackground = tokens.baseContent,
            surface = tokens.base100,
            onSurface = tokens.baseContent,
            surfaceVariant = tokens.base300,
            onSurfaceVariant = tokens.mutedContent,
            outline = tokens.borderSubtle,
        )
    }

    val shapes = Shapes(
        small = RoundedCornerShape(tokens.controlRadius * 0.7f),
        medium = RoundedCornerShape(tokens.controlRadius),
        large = RoundedCornerShape(tokens.cardRadius),
        extraLarge = RoundedCornerShape(tokens.cardRadius + 8.dp),
    )

    CompositionLocalProvider(LocalOrbitTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OrbitTypography,
            shapes = shapes,
            content = content,
        )
    }
}

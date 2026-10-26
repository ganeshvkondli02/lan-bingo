package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val primary: Color,
    val primaryDark: Color,
    val accentLime: Color,
    val accentBorder: Color,
    val accentDarkText: Color,
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardLight: Color,
    val border: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textOnPrimary: Color,
    val isDark: Boolean = false
)

enum class AppTheme(
    val id: String,
    val title: String,
    val colors: AppColors
) {
    DARK_MODE(
        id = "dark",
        title = "Black & White (Dark)",
        colors = AppColors(
            primary = Color(0xFFFFFFFF),        // Pure White primary
            primaryDark = Color(0xFFE5E5EA),    // High-contrast light grey
            accentLime = Color(0xFFFFFFFF),     // Pure White accent
            accentBorder = Color(0xFFFFFFFF),   // Crisp White border
            accentDarkText = Color(0xFF000000), // Pure Black text on white accents
            background = Color(0xFF000000),     // Pure Deep Black background
            surface = Color(0xFF121212),        // Sleek dark surface
            card = Color(0xFF1C1C1E),           // Dark card container
            cardLight = Color(0xFF2C2C2E),      // Inner card / cell container
            border = Color(0xFF38383A),         // High-contrast subtle border
            borderStrong = Color(0xFF636366),   // Strong border
            textPrimary = Color(0xFFFFFFFF),    // Pure White text
            textSecondary = Color(0xFFEBEBF5),  // Soft White text
            textMuted = Color(0xFF8E8E93),      // Muted silver/grey text
            textOnPrimary = Color(0xFF000000),  // Black text on white primary elements
            isDark = true
        )
    ),
    CYBER_NEON(
        id = "cyber",
        title = "Cyber Neon",
        colors = AppColors(
            primary = Color(0xFF00ADB5),
            primaryDark = Color(0xFF007A80),
            accentLime = Color(0xFF00FFF5),
            accentBorder = Color(0xFF00E5DC),
            accentDarkText = Color(0xFF0A192F),
            background = Color(0xFF090D16),
            surface = Color(0xFF111A2E),
            card = Color(0xFF1A263E),
            cardLight = Color(0xFF253554),
            border = Color(0xFF2A3E62),
            borderStrong = Color(0xFF3D5888),
            textPrimary = Color(0xFFF0FDF4),
            textSecondary = Color(0xFF94A3B8),
            textMuted = Color(0xFF64748B),
            textOnPrimary = Color(0xFF0A192F),
            isDark = true
        )
    ),
    VIBRANT_LIME(
        id = "lime",
        title = "Olive & Lime",
        colors = AppColors(
            primary = Color(0xFF4F6600),
            primaryDark = Color(0xFF3D4F00),
            accentLime = Color(0xFFD1ED7A),
            accentBorder = Color(0xFFB9D363),
            accentDarkText = Color(0xFF151F00),
            background = Color(0xFFFDFCFA),
            surface = Color(0xFFF0EFEA),
            card = Color(0xFFE4E3DB),
            cardLight = Color(0xFFFFFFFF),
            border = Color(0xFFC8C7BF),
            borderStrong = Color(0xFFA8A79E),
            textPrimary = Color(0xFF1B1C18),
            textSecondary = Color(0xFF46483B),
            textMuted = Color(0xFF5C614B),
            textOnPrimary = Color(0xFFFDFCFA),
            isDark = false
        )
    ),
    SUNSET_CORAL(
        id = "sunset",
        title = "Sunset Coral",
        colors = AppColors(
            primary = Color(0xFFE65100),
            primaryDark = Color(0xFFBF360C),
            accentLime = Color(0xFFFFCC80),
            accentBorder = Color(0xFFFFB74D),
            accentDarkText = Color(0xFF3E1404),
            background = Color(0xFFFFFBF7),
            surface = Color(0xFFFCEFE3),
            card = Color(0xFFF6DEC9),
            cardLight = Color(0xFFFFFFFF),
            border = Color(0xFFE0C4AC),
            borderStrong = Color(0xFFC2A083),
            textPrimary = Color(0xFF2A1608),
            textSecondary = Color(0xFF664936),
            textMuted = Color(0xFF8C6B55),
            textOnPrimary = Color(0xFFFFFBF7),
            isDark = false
        )
    ),
    EMERALD_MINT(
        id = "emerald",
        title = "Emerald Mint",
        colors = AppColors(
            primary = Color(0xFF00695C),
            primaryDark = Color(0xFF004D40),
            accentLime = Color(0xFFA7FFEB),
            accentBorder = Color(0xFF64FFDA),
            accentDarkText = Color(0xFF002922),
            background = Color(0xFFF5FAF8),
            surface = Color(0xFFE2F1ED),
            card = Color(0xFFCBE5DE),
            cardLight = Color(0xFFFFFFFF),
            border = Color(0xFFAECBC3),
            borderStrong = Color(0xFF89ACA4),
            textPrimary = Color(0xFF0F241F),
            textSecondary = Color(0xFF3B5750),
            textMuted = Color(0xFF5A7770),
            textOnPrimary = Color(0xFFF5FAF8),
            isDark = false
        )
    ),
    ROYAL_AMETHYST(
        id = "amethyst",
        title = "Royal Amethyst",
        colors = AppColors(
            primary = Color(0xFF5E35B1),
            primaryDark = Color(0xFF4527A0),
            accentLime = Color(0xFFD1C4E9),
            accentBorder = Color(0xFFB39DDB),
            accentDarkText = Color(0xFF220A45),
            background = Color(0xFFFAF7FD),
            surface = Color(0xFFF0E8F8),
            card = Color(0xFFE3D6F3),
            cardLight = Color(0xFFFFFFFF),
            border = Color(0xFFCEBEE5),
            borderStrong = Color(0xFFAB96C7),
            textPrimary = Color(0xFF211338),
            textSecondary = Color(0xFF514168),
            textMuted = Color(0xFF73628A),
            textOnPrimary = Color(0xFFFAF7FD),
            isDark = false
        )
    );

    val primaryColor: Color get() = colors.primary
    val primaryDarkColor: Color get() = colors.primaryDark
    val accentLime: Color get() = colors.accentLime
    val accentBorder: Color get() = colors.accentBorder
    val accentDarkText: Color get() = colors.accentDarkText
    val backgroundColor: Color get() = colors.background
    val surfaceColor: Color get() = colors.surface
    val cardColor: Color get() = colors.card
}

val LocalAppColors = staticCompositionLocalOf { AppTheme.VIBRANT_LIME.colors }

object LocalTheme {
    val current: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

// Dynamic theme color getters for entire app
val VibrantPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.primary

val VibrantPrimaryDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.primaryDark

val VibrantLime: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.accentLime

val VibrantLimeBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.accentBorder

val VibrantLimeDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.accentDarkText

val VibrantBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.background

val VibrantSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.surface

val VibrantCard: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.card

val VibrantCardLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.cardLight

val VibrantBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.border

val VibrantBorderStrong: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.borderStrong

val VibrantTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textPrimary

val VibrantTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textSecondary

val VibrantTextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textMuted

val VibrantTextOnPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textOnPrimary

// Constant helper accents
val VibrantAmber = Color(0xFFE5A000)
val VibrantCoral = Color(0xFFD94848)
val VibrantTeal = Color(0xFF007F73)

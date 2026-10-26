package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.VIBRANT_LIME,
    content: @Composable () -> Unit
) {
    val themeColors = appTheme.colors
    val colorScheme = if (themeColors.isDark) {
        darkColorScheme(
            primary = themeColors.primary,
            onPrimary = themeColors.textOnPrimary,
            primaryContainer = themeColors.accentLime,
            onPrimaryContainer = themeColors.accentDarkText,
            secondary = themeColors.primaryDark,
            onSecondary = themeColors.textOnPrimary,
            secondaryContainer = themeColors.card,
            onSecondaryContainer = themeColors.textSecondary,
            tertiary = themeColors.accentLime,
            onTertiary = themeColors.accentDarkText,
            background = themeColors.background,
            onBackground = themeColors.textPrimary,
            surface = themeColors.surface,
            onSurface = themeColors.textPrimary,
            surfaceVariant = themeColors.card,
            onSurfaceVariant = themeColors.textMuted,
            outline = themeColors.border
        )
    } else {
        lightColorScheme(
            primary = themeColors.primary,
            onPrimary = themeColors.textOnPrimary,
            primaryContainer = themeColors.accentLime,
            onPrimaryContainer = themeColors.accentDarkText,
            secondary = themeColors.primaryDark,
            onSecondary = themeColors.textOnPrimary,
            secondaryContainer = themeColors.card,
            onSecondaryContainer = themeColors.textSecondary,
            tertiary = themeColors.accentLime,
            onTertiary = themeColors.accentDarkText,
            background = themeColors.background,
            onBackground = themeColors.textPrimary,
            surface = themeColors.surface,
            onSurface = themeColors.textPrimary,
            surfaceVariant = themeColors.card,
            onSurfaceVariant = themeColors.textMuted,
            outline = themeColors.border
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = themeColors.background.toArgb()
                window.navigationBarColor = themeColors.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !themeColors.isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !themeColors.isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides themeColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

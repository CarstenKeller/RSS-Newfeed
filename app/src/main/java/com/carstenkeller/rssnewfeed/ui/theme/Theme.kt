package com.carstenkeller.rssnewfeed.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// A deliberately chosen blue/indigo palette instead of the device's dynamic
// (wallpaper-derived) Material You colors, so the app has a consistent, modern
// look regardless of the phone's wallpaper. Red is reserved for the swipe-to-
// dismiss/error role only, never used as a background color at rest.
private val LightColors = lightColorScheme(
    primary = Color(0xFF2653C9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE1FF),
    onPrimaryContainer = Color(0xFF001655),
    secondary = Color(0xFF585E71),
    background = Color(0xFFFAFAFD),
    surface = Color(0xFFFAFAFD),
    onSurface = Color(0xFF1A1B21),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44464F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB6C4FF),
    onPrimary = Color(0xFF00287A),
    primaryContainer = Color(0xFF073AA3),
    onPrimaryContainer = Color(0xFFDBE1FF),
    secondary = Color(0xFFC0C6DC),
    background = Color(0xFF12131A),
    surface = Color(0xFF12131A),
    onSurface = Color(0xFFE3E2E9),
    surfaceVariant = Color(0xFF44464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val ReadHighlightLight = Color(0xFFD7F2DA)
private val ReadHighlightDark = Color(0xFF20402A)

/** Whether dark colors are actually active right now (after resolving ThemeMode.SYSTEM). */
private val LocalDarkThemeActive = compositionLocalOf { false }

@Composable
fun readHighlightColor(): Color = if (LocalDarkThemeActive.current) ReadHighlightDark else ReadHighlightLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun brandedTopAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
)

@Composable
fun RssNewfeedTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.HELL -> false
        ThemeMode.DUNKEL -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(LocalDarkThemeActive provides darkTheme) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}

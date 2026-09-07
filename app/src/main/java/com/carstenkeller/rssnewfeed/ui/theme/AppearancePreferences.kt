package com.carstenkeller.rssnewfeed.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { HELL, DUNKEL, SYSTEM }

/**
 * App-wide theme mode, held in a singleton StateFlow so any composable that changes it
 * (the hamburger menu's "Darstellung" dialog) is immediately reflected by MainActivity's
 * top-level RssNewfeedTheme without recreating the Activity.
 */
object AppearancePreferences {
    private const val PREFS_NAME = "appearance_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun init(context: Context) {
        _themeMode.value = readThemeMode(context)
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
        _themeMode.value = mode
    }

    private fun readThemeMode(context: Context): ThemeMode {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(stored ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
}

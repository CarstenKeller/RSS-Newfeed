package com.carstenkeller.rssnewfeed.data.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(val tag: String) { DEUTSCH("de"), ENGLISH("en") }

/**
 * Wraps AndroidX's per-app language API. AppCompatDelegate persists the choice itself
 * (via its auto-storage mechanism, enabled by the androidx.appcompat dependency), so this
 * object holds no separate SharedPreferences state.
 *
 * Note: this only switches the app's Locale (affects date/number formatting, the
 * keyboard, and any text pulled from string resources). The app's own UI text is still
 * hard-coded German throughout — switching to English will not yet translate screens.
 * Full translation requires extracting those strings into res/values(-en)/strings.xml,
 * which is a separate, larger follow-up.
 */
object LanguagePreferences {
    fun getLanguage(): AppLanguage {
        val current = AppCompatDelegate.getApplicationLocales()
        if (current.isEmpty) return AppLanguage.DEUTSCH
        val tag = current[0]?.language
        return AppLanguage.entries.find { it.tag == tag } ?: AppLanguage.DEUTSCH
    }

    fun setLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }
}

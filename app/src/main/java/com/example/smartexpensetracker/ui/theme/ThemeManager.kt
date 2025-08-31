package com.example.smartexpensetracker.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class ThemeManager(private val context: Context) {
    private val KEY_DARK = booleanPreferencesKey("pref_dark")
    val isDark = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK] ?: (isSystemInDarkThemeDefault())
    }

    private fun isSystemInDarkThemeDefault(): Boolean {
        val uiMode = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    suspend fun setDark(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_DARK] = value }
    }
}
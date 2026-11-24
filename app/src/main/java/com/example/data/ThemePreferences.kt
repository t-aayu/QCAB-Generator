package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.qcabPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "qcab_preferences"
)

class ThemePreferences(private val context: Context) {

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val themeModeFlow: Flow<ThemeMode> = context.qcabPreferencesDataStore.data.map { prefs ->
        val stored = prefs[KEY_THEME_MODE]
        runCatching {
            if (stored != null) ThemeMode.valueOf(stored) else ThemeMode.SYSTEM
        }.getOrDefault(ThemeMode.SYSTEM)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.qcabPreferencesDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }
}

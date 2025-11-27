package org.example.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
PUBLIC_INTERFACE
SettingsRepository provides persistent app settings using DataStore.
Currently supports a high-contrast accessibility flag that is applied at runtime.
 */
class SettingsRepository(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "user_settings")

    private object Keys {
        val HIGH_CONTRAST_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("high_contrast_enabled")
    }

    // PUBLIC_INTERFACE
    fun highContrastEnabled(): Flow<Boolean> {
        /**
         Returns a Flow of the High Contrast enabled flag, defaulting to false.
         */
        return context.dataStore.data.map { prefs -> prefs[Keys.HIGH_CONTRAST_ENABLED] ?: false }
    }

    // PUBLIC_INTERFACE
    suspend fun setHighContrastEnabled(enabled: Boolean) {
        /**
         Persists the High Contrast enabled flag.
         */
        context.dataStore.edit { prefs ->
            prefs[Keys.HIGH_CONTRAST_ENABLED] = enabled
        }
    }
}

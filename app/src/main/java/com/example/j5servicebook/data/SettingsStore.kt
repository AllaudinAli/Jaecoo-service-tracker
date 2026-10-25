package com.example.j5servicebook.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class Settings(
    val leadDays: Int = DEFAULT_LEAD_DAYS,
    val wallpaperColors: Boolean = false,
) {
    companion object {
        const val DEFAULT_LEAD_DAYS = 7
        val LEAD_OPTIONS = listOf(3, 7, 14)
    }
}

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map {
        Settings(
            leadDays = it[LEAD_DAYS] ?: Settings.DEFAULT_LEAD_DAYS,
            wallpaperColors = it[WALLPAPER_COLORS] ?: false,
        )
    }

    suspend fun setLeadDays(days: Int) {
        store.edit { it[LEAD_DAYS] = days }
    }

    suspend fun setWallpaperColors(on: Boolean) {
        store.edit { it[WALLPAPER_COLORS] = on }
    }

    suspend fun sentKeys(): Set<String> = store.data.first()[SENT_KEYS].orEmpty()

    /** Remembers which reminder stages were posted, capped so the set cannot grow forever. */
    suspend fun addSentKeys(keys: Collection<String>) {
        store.edit { prefs ->
            prefs[SENT_KEYS] = (prefs[SENT_KEYS].orEmpty() + keys).toList().takeLast(MAX_SENT_KEYS).toSet()
        }
    }

    private companion object {
        val LEAD_DAYS = intPreferencesKey("lead_days")
        val WALLPAPER_COLORS = booleanPreferencesKey("wallpaper_colors")
        val SENT_KEYS = stringSetPreferencesKey("sent_notice_keys")
        const val MAX_SENT_KEYS = 300
    }
}

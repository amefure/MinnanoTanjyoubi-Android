package com.amefure.minnanotanjyoubi.Model.DataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreManager(private val context: Context) {

    companion object {
        val NOTIFY_TIME = stringPreferencesKey("notify_time")
    }

    suspend fun saveNotifyTime(sortItem: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[NOTIFY_TIME] = sortItem
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    public fun observeNotifyTime(): Flow<String?> {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[NOTIFY_TIME]
        }
    }
}
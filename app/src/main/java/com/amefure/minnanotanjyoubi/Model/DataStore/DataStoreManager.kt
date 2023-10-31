package com.amefure.minnanotanjyoubi.Model.DataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreManager(private val context: Context) {

    companion object {
        // 「7:00」形式で保存
        val NOTIFY_TIME = stringPreferencesKey("notify_time")
        // 「当日」or「前日」
        val NOTIFY_DAY = stringPreferencesKey("notify_day")
        // 「通知に発行したいメッセージ」
        val NOTIFY_MESSAGE = stringPreferencesKey("notify_message")

        // 保存容量
        val LIMIT_CAPACITY = intPreferencesKey("limit_capacity")
        // 最終広告視聴日
        val LAST_ACQUISITION_DATE = stringPreferencesKey("last_acquisition_date")
    }

    suspend fun saveNotifyTime(notifyTime: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[NOTIFY_TIME] = notifyTime
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

    suspend fun saveNotifyDay(notifyDay: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[NOTIFY_DAY] = notifyDay
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    public fun observeNotifyDay(): Flow<String?> {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[NOTIFY_DAY]
        }
    }

    suspend fun saveNotifyMsg(notifyMsg: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[NOTIFY_MESSAGE] = notifyMsg
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    public fun observeNotifyMsg(): Flow<String?> {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[NOTIFY_MESSAGE]
        }
    }

    suspend fun saveLimitCapacity(capacity: Int) {
        try {
            context.dataStore.edit { preferences ->
                preferences[LIMIT_CAPACITY] = capacity
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    public fun observeLimitCapacity(): Flow<Int?> {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[LIMIT_CAPACITY]
        }
    }

    suspend fun saveLastAcquisitionDate(date: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[LAST_ACQUISITION_DATE] = date
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    public fun observeLastAcquisitionDate(): Flow<String?> {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[LAST_ACQUISITION_DATE]
        }
    }
}
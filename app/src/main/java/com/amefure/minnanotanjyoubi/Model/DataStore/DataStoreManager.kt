package com.amefure.minnanotanjyoubi.Model.DataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

/** アプリ内ローカル保存データ管理クラス */
class DataStoreManager(
    private val context: Context,
) {
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

        /** アプリ内課金購入フラグ：広告削除 */
        val IN_APP_REMOVE_ADS = booleanPreferencesKey("IN_APP_REMOVE_ADS")

        /** アプリ内課金購入フラグ：容量解放 */
        val IN_APP_UNLOCK_STORAGE = booleanPreferencesKey("IN_APP_UNLOCK_STORAGE")
    }

    /** 保存；通知時間 */
    suspend fun saveNotifyTime(notifyTime: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[NOTIFY_TIME] = notifyTime
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    /** 観測：通知時間 */
    public fun observeNotifyTime(): Flow<String?> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[NOTIFY_TIME]
            }

    /** 保存；通知日 */
    suspend fun saveNotifyDay(notifyDay: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[NOTIFY_DAY] = notifyDay
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    /** 観測；通知日 */
    public fun observeNotifyDay(): Flow<String?> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[NOTIFY_DAY]
            }

    /** 保存；通知メッセージ */
    suspend fun saveNotifyMsg(notifyMsg: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[NOTIFY_MESSAGE] = notifyMsg
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    /** 観測；通知メッセージ */
    public fun observeNotifyMsg(): Flow<String?> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[NOTIFY_MESSAGE]
            }

    /** 保存；容量 */
    suspend fun saveLimitCapacity(capacity: Int) {
        try {
            context.dataStore.edit { preferences ->
                preferences[LIMIT_CAPACITY] = capacity
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    /** 観測；容量 */
    public fun observeLimitCapacity(): Flow<Int?> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[LIMIT_CAPACITY]
            }

    /** 保存；最終視聴日 */
    suspend fun saveLastAcquisitionDate(date: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[LAST_ACQUISITION_DATE] = date
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    /** 観測；最終視聴日 */
    public fun observeLastAcquisitionDate(): Flow<String?> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[LAST_ACQUISITION_DATE]
            }

    /** 保存；広告削除購入フラグ */
    suspend fun saveInAppRemoveAdsFlag(purchased: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[IN_APP_REMOVE_ADS] = purchased
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    /** 取得；広告削除購入フラグ */
    suspend fun getInAppRemoveAds(): Boolean =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.firstOrNull() // 最初の値を1回だけ取得
            ?.get(IN_APP_REMOVE_ADS) ?: false

    /** 観測；容量解放購入フラグ */
    public fun observeInAppRemoveAds(): Flow<Boolean> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[IN_APP_REMOVE_ADS] ?: false
            }

    /** 保存；容量解放購入フラグ */
    suspend fun saveInAppUnlockStorage(purchased: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[IN_APP_UNLOCK_STORAGE] = purchased
            }
        } catch (e: IOException) {
            // Handle the exception here if needed
        }
    }

    /** 取得；容量解放購入フラグ */
    suspend fun getInAppUnlockStorage(): Boolean =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.firstOrNull() // 最初の値を1回だけ取得
            ?.get(IN_APP_UNLOCK_STORAGE) ?: false

    /** 観測；容量解放購入フラグ */
    public fun observeInAppUnlockStorage(): Flow<Boolean> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[IN_APP_UNLOCK_STORAGE] ?: false
            }
}

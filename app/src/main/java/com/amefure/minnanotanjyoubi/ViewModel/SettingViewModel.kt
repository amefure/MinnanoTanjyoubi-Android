package com.amefure.minnanotanjyoubi.ViewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amefure.minnanotanjyoubi.Manager.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Model.domain.Capacity
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.Model.domain.NotifyDay
import com.amefure.minnanotanjyoubi.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val dataStoreManager = DataStoreManager(context)
    private val calcDateInfoManager = CalcDateInfoManager()

    /** デフォルト通知時間 */
    private val defaultNotifyTime = context.getString(R.string.notify_default_time)
    /** デフォルト通知日 */
    private val defaultNotifyDay = context.getString(R.string.notify_default_day)
    /** リワード広告読み込み中 */
    private val adsLoadingText = context.getString(R.string.ads_setting_loading)
    /** 初期容量 */
    private val initialCapacity = Capacity.initialCapacity

    /** 通知時間 */
    var notifyTime by mutableStateOf(defaultNotifyTime)
        private set
    /** 通知日 */
    var notifyDay by mutableStateOf(defaultNotifyDay)
        private set
    /** 現在の容量 */
    var currentCapacity by mutableStateOf(initialCapacity)
        private set
    /** 容量解放フラグ */
    var unlockStorage by mutableStateOf(false)
        private set
    /** リワード広告視聴開始ボタン */
    var adsButtonText by mutableStateOf(adsLoadingText)
        private set

    /** 最終視聴日 */
    private var lastAcquisitionDate = ""

    private var isProcessing = false

    /** UI通知イベント */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        object HideKeyboard : UiEvent()
        object Saved : UiEvent()
    }

    fun updateAdsButtonState(isEnabled: Boolean) {
        if (isEnabled) {
            adsButtonText = context.getString(R.string.ads_setting_add)
        } else {
            adsButtonText = adsLoadingText
        }
    }

    fun fetchSetUpLocalData() {
        viewModelScope.launch {
            val time = withContext(Dispatchers.IO) { dataStoreManager.getNotifyTime() }
            val day = withContext(Dispatchers.IO) { dataStoreManager.getNotifyDay() }
            val capacity = withContext(Dispatchers.IO) { dataStoreManager.getLimitCapacity() }
            val acquisitionDate = withContext(Dispatchers.IO) { dataStoreManager.getLastAcquisitionDate() }
            val unlockStorage = withContext(Dispatchers.IO) { dataStoreManager.getInAppUnlockStorage() }

            withContext(Dispatchers.Main) {
                // メインスレッドで更新しないとUIに反映されない
                notifyTime = time ?: defaultNotifyTime
                Log.d("99999", "取得" + notifyTime)
                notifyDay = day ?: defaultNotifyDay
                currentCapacity = capacity ?: initialCapacity
                lastAcquisitionDate = acquisitionDate ?: ""
                this@SettingViewModel.unlockStorage = unlockStorage
            }
        }
    }

    fun switchNotifyDay() {
        if (notifyDay == NotifyDay.NOW.value) {
            saveNotifyDay(NotifyDay.PRE)
        } else {
            saveNotifyDay(NotifyDay.NOW)
        }
    }

    private fun saveNotifyDay(notifyDay: NotifyDay) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.saveNotifyDay(notifyDay)
            withContext(Dispatchers.Main) {
                this@SettingViewModel.notifyDay = notifyDay.value
            }
        }
    }

    fun saveNotifyTime(hour: Int, minutes: Int) {
        var minutesStr = minutes.toString()
        if (minutesStr.length == 1) {
            minutesStr = "0$minutesStr"
        }
        val time = "$hour:$minutesStr"
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.saveNotifyTime(time)
            withContext(Dispatchers.Main) {
                notifyTime = time
            }
        }
    }

    fun fetchNotifyTime(): Pair<Int, Int> {
        // 日付を選択済みの場合は初期値を変更
        val parts = notifyTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        return Pair(hour, minute)
    }

    fun isShowAd() = !calcDateInfoManager.isToday(lastAcquisitionDate)

    fun updateLimitCapacity() {
        viewModelScope.launch {
            // 広告を視聴し終えたら容量の追加と視聴日を保存
            val newCapacity = currentCapacity + Capacity.addCapacity
            dataStoreManager.saveLimitCapacity(newCapacity)
            dataStoreManager.saveLastAcquisitionDate(calcDateInfoManager.getTodayString())
        }
    }

    companion object {
        private const val EDITING_MSG = "EDITING_MSG"
    }
}
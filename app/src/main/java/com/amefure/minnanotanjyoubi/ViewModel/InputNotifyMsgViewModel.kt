package com.amefure.minnanotanjyoubi.ViewModel

import com.amefure.minnanotanjyoubi.R
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class InputNotifyMsgViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val dataStoreManager = DataStoreManager(context)

    private val defaultMsg = context.getString(R.string.notify_default_message)

    var editingMsg by mutableStateOf(
        savedStateHandle[EDITING_MSG] ?: defaultMsg
    )
        private set

    private var isProcessing = false

    /** UI通知イベント */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        object HideKeyboard : UiEvent()
        object Saved : UiEvent()
    }

    init {
        viewModelScope.launch {
            val msg = withContext(Dispatchers.IO) { dataStoreManager.getNotifyMsg() }

            // SavedStateHandle に値があればそちらを優先
            val current = savedStateHandle[EDITING_MSG] ?: msg.ifEmpty { defaultMsg }

            withContext(Dispatchers.Main) {
                // メインスレッドで更新しないと反映されない
                editingMsg = current
                savedStateHandle[EDITING_MSG] = current
            }
        }
    }

    /** 編集中メッセージを更新 */
    fun updateEditingMsg(newValue: String) {
        editingMsg = newValue
        savedStateHandle[EDITING_MSG] = newValue
    }

    /** ローカルに通知メッセージを保存 */
    fun saveNotifyMsg() {
        // 処置中なら実行しない
        if (isProcessing)  return
        // 画面を強制的に戻すためfalseに戻さない
        isProcessing = true
        viewModelScope.launch(Dispatchers.IO) {
            // キーボードを閉じる
            _uiEvent.emit(UiEvent.HideKeyboard)
            dataStoreManager.saveNotifyMsg(editingMsg)
            // 完了通知
            _uiEvent.emit(UiEvent.Saved)
        }
    }

    companion object {
        private val EDITING_MSG = "EDITING_MSG"
    }
}
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputNotifyMsgViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {

    private val dataStoreManager = DataStoreManager(context)

    var editingMsg by mutableStateOf("")
        private set

    /** UI通知イベント */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        object HideKeyboard : UiEvent()
        object Saved : UiEvent()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val msg = dataStoreManager.getNotifyMsg()
            editingMsg = if (msg.isNotEmpty()) msg else context.getString(R.string.notify_default_message)
        }
    }

    fun updateEditingMsg(newValue: String) {
        editingMsg = newValue
    }

    fun saveNotifyMsg() {
        viewModelScope.launch(Dispatchers.IO) {
            // キーボードを閉じる
            _uiEvent.emit(UiEvent.HideKeyboard)
            dataStoreManager.saveNotifyMsg(editingMsg)
            // 完了通知
            _uiEvent.emit(UiEvent.Saved)
        }
    }
}
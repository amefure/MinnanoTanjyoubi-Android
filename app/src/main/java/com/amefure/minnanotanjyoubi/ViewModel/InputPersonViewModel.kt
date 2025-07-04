package com.amefure.minnanotanjyoubi.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amefure.minnanotanjyoubi.Manager.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Repository.RootRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputPersonViewModel @Inject constructor(
    private val rootRepository: RootRepository
) : ViewModel() {
    private val calcDateInfoManager = CalcDateInfoManager()

    // 日付ピッカーで選択された日付が格納される
    private val _selectDate: MutableLiveData<String> = MutableLiveData<String>(calcDateInfoManager.getTodayString())
    val selectDate: LiveData<String> = _selectDate

    public fun setSelectDate(selectDate: String) {
        _selectDate.value = selectDate
    }

    public fun insertPerson(
        name: String,
        ruby: String,
        date: String,
        relation: String,
        memo: String,
        alert: Boolean,
        callback: (Long) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = rootRepository.insertPerson(name, ruby, date, relation, memo, alert)
            callback(id)
        }
    }

    public fun updatePerson(
        id: Int,
        name: String,
        ruby: String,
        date: String,
        relation: String,
        memo: String,
        alert: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            rootRepository.updatePerson(id, name, ruby, date, relation, memo, alert)
        }
    }
}

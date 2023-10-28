package com.amefure.minnanotanjyoubi.ViewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amefure.minnanotanjyoubi.Domain.CalcDateInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class InputPersonViewModel(app: Application): RootViewModel(app) {

    private val calcDateInfoManager = CalcDateInfoManager()

    // 日付ピッカーで選択された日付が格納される
    private val _selectDate: MutableLiveData<String> = MutableLiveData<String>(calcDateInfoManager.getTodayString())
    val selectDate: LiveData<String> = _selectDate

    public fun setSelectDate(selectDate: String){
        _selectDate.value = selectDate
    }

    public fun insertPerson(name: String, ruby: String, date: String, relation: String, memo: String, alert:Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            rootRepository.insertPerson(name, ruby, date, relation, memo ,alert)
        }
    }

    public fun updatePerson(id: Int, name: String, ruby: String, date: String, relation: String, memo: String, alert:Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            rootRepository.updatePerson(id,name, ruby, date, relation, memo ,alert)
        }
    }
}
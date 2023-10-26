package com.amefure.minnanotanjyoubi.ViewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class InputPersonViewModel(app: Application): RootViewModel(app) {

    // 日付ピッカーで選択された日付が格納される
    private val _selectDate: MutableLiveData<String> = MutableLiveData<String>(getTodayString())
    val selectDate: LiveData<String> = _selectDate

    public fun setSelectDate(selectDate: String){
        _selectDate.value = selectDate
    }

    // 本日の日付を文字列で取得する
    public fun getTodayString() :String {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))
        val df = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        val fdate = df.format(now)
        return fdate
    }

    public fun insertPerson(name: String, ruby: String, date: String, relation: String, memo: String, alert:Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            rootRepository.insertPerson(name, ruby, date, relation, memo ,alert)
        }
    }
}
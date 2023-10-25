package com.amefure.minnanotanjyoubi.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class InputPersonViewModel: ViewModel() {
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
}
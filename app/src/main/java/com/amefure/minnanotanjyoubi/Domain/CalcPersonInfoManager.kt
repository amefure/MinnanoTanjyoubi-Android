package com.amefure.minnanotanjyoubi.Domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CalcPersonInfoManager {

    private val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
    private val today = LocalDate.now()

    // 現在の年齢を計算する
    public fun currentAge(date: String): Int {
        val birthday = LocalDate.parse(date, formatter)
        val age = ChronoUnit.YEARS.between(birthday, today)
        return age.toInt()
    }

    // 誕生日まであと何日か
    public fun daysLater(date: String): Int {
        val birthday = LocalDate.parse(date, formatter)
        val nextBirthday = birthday.withYear(today.year)
        val daysUntilNextBirthday: Long = if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            ChronoUnit.DAYS.between(today, nextBirthday.plusYears(1))
        } else {
            ChronoUnit.DAYS.between(today, nextBirthday)
        }
        var daysLaterInt = daysUntilNextBirthday.toInt()
        if (daysLaterInt == 366) {
            daysLaterInt = 0
        }
        return daysLaterInt
    }
}
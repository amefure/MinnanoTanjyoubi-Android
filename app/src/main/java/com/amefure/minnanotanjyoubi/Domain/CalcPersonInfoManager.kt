package com.amefure.minnanotanjyoubi.Domain

import java.time.LocalDate
import java.time.chrono.JapaneseDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

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

    // 元号(和暦)を取得 ：令和5年形式
    public fun japaneseEraName(date: String): String {
        val birthday = LocalDate.parse(date, formatter)
        val japanDay = JapaneseDate.from(birthday)
        val df = DateTimeFormatter.ofPattern("Gy年", Locale.JAPAN)
        val jpEra = df.format(japanDay)
        return jpEra
    }

    // 12星座を取得
    public fun signOfZodiac(date: String): String {
        val thisYear = date.substring(0, 4).toInt()
        val nowYear = "$thisYear/"
        val lateYear = "${thisYear + 1}/"
        val birthday = LocalDate.parse(date, formatter)

        if (isDateInRange(birthday, getDate(nowYear,"3/21"), getDate(nowYear,"4/20"))) {
            return "おひつじ座"
        } else if (isDateInRange(birthday, getDate(nowYear,"4/20"), getDate(nowYear,"5/21"))) {
            return "おうし座"
        } else if (isDateInRange(birthday, getDate(nowYear,"5/21"), getDate(nowYear,"6/22"))) {
            return "ふたご座"
        } else if (isDateInRange(birthday, getDate(nowYear,"6/22"), getDate(nowYear,"7/23"))) {
            return "かに座"
        } else if (isDateInRange(birthday, getDate(nowYear,"7/23"), getDate(nowYear,"8/23"))) {
            return "しし座"
        } else if (isDateInRange(birthday, getDate(nowYear,"8/23"), getDate(nowYear,"9/23"))) {
            return "おとめ座"
        } else if (isDateInRange(birthday, getDate(nowYear,"9/23"), getDate(nowYear,"10/24"))) {
            return "てんびん座"
        } else if (isDateInRange(birthday, getDate(nowYear,"10/24"), getDate(nowYear,"11/23"))) {
            return "さそり座"
        } else if (isDateInRange(birthday, getDate(nowYear,"11/23"), getDate(nowYear,"12/22"))) {
            return "いて座"
        } else if (isDateInRange(birthday, getDate(nowYear,"12/22"), getDate(lateYear,"1/1"))) {
            return "やぎ座"
        } else if (isDateInRange(birthday, getDate(nowYear,"1/1"), getDate(nowYear,"1/20"))) {
            return "やぎ座"
        }else if (isDateInRange(birthday, getDate(nowYear,"1/20"), getDate(nowYear,"2/19"))) {
            return "みずがめ座"
        }else if (isDateInRange(birthday, getDate(nowYear,"2/19"), getDate(nowYear,"3/21"))) {
            return "うお座"
        } else {
            return ""
        }
    }

    // 日付を受け取ってLocalDate型に変換する
    private fun getDate(year: String, day: String): LocalDate {
        val df = DateTimeFormatter.ofPattern("yyyy/M/d")
        return LocalDate.parse(year + day, df)
    }

    // 日付が期間の範囲内か識別
    private fun isDateInRange(dateToCheck: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        return (dateToCheck.isEqual(startDate) || dateToCheck.isAfter(startDate)) && dateToCheck.isBefore(endDate)
    }

    // 干支を取得
    public fun zodiac(date: String): String {
        val thisYear = date.substring(0, 4).toInt()
        val num = thisYear % 12
        return when (num) {
            4 -> "ねずみ年"
            5 -> "うし年"
            6 -> "とら年"
            7 -> "うさぎ年"
            8 -> "たつ年"
            9 -> "へび年"
            10 -> "うま年"
            11 -> "ひつじ年"
            0 -> "さる年"
            1 -> "とり年"
            2 -> "いぬ年"
            3 -> "いのしし年"
            else -> "..."
        }
    }

}
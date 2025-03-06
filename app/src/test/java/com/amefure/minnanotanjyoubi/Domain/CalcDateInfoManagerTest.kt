package com.amefure.minnanotanjyoubi.Domain

import org.junit.Assert.*

import org.junit.Test
import java.time.LocalDate

class CalcDateInfoManagerTest {

    private val manager = CalcDateInfoManager()

    @Test
    fun testGetTodayString() {
        val today = manager.getTodayString()
        val expectedToday = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
        assertEquals(expectedToday, today, "本日の日付が正しく取得されているか確認")
    }

    @Test
    fun testIsToday() {
        val todayString = manager.getTodayString()
        assertTrue(manager.isToday(todayString)) // "本日の日付が正しく判定されているか確認"
        assertFalse(manager.isToday("2000年01月01日")) // "過去の日付が本日ではないと判定されるか確認"
    }

    @Test
    fun testIsBirthDay() {
        val todayString = manager.getTodayString()
        assertTrue(manager.isBirthDay(todayString)) // "本日が誕生日の場合、正しく判定されるか確認"
        assertFalse(manager.isBirthDay("2000年01月01日")) // "誕生日ではない場合に誤判定されないか確認"
    }

    @Test
    fun testCurrentAge() {
        val birthday = LocalDate.now().minusYears(25).format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
        assertEquals(25, manager.currentAge(birthday)) // "年齢が正しく計算されているか確認"
    }

    @Test
    fun testDaysLater() {
        val today = LocalDate.now()
        val birthday = today.plusDays(10).format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
        assertEquals(10, manager.daysLater(birthday)) // "誕生日までの日数が正しく計算されているか確認"
    }

    @Test
    fun testGetBeforeOneDayString() {
        val date = "2024年01月01日"
        val expected = "2023年12月31日"
        assertEquals(expected, manager.getBeforeOneDayString(date), "前日の日付が正しく取得されているか確認")
    }

    @Test
    fun testJapaneseEraName() {
        val date = "2024年01月01日"
        assertEquals("令和6年", manager.japaneseEraName(date), "和暦の形式が正しいか確認")
    }

    @Test
    fun testSignOfZodiac() {
        val date = "2024年03月21日"
        assertEquals("おひつじ座", manager.signOfZodiac(date), "正しい星座が取得されるか確認")
    }

    @Test
    fun testZodiac() {
        val date = "2024年01月01日"
        assertEquals("たつ年", manager.zodiac(date), "正しい干支が取得されるか確認")
    }
}

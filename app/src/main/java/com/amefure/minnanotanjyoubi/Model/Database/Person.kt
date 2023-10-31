package com.amefure.minnanotanjyoubi.Model.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person_table")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,       // 名前
    var ruby: String,       // ルビ
    var date: String ,      // 誕生日
    var relation: String,   // 関係
    var memo: String,       // メモ
    var alert: Boolean = false // 通知フラグ
){
    companion object {
        fun getDemoData(): List<Person> {
            return listOf(
                Person(1, "吉田　真紘", "よしだ　まひろ", "1994年12月21日","友達","",false),
                Person(2, "長谷　慎二", "はせ　しんじ", "1990年03月03日","会社","",false),
                Person(3, "森山　美沙子", "もりやま　みさこ", "1994年05月12日","友達","",false),
                Person(4, "中川　健太", "なかがわ　けんた", "2008年06月29日","友達","",false),
                Person(5, "お父さん", "おとうさん", "1969年07月07日","家族","",false),
                Person(6, "お母さん", "おかあさん", "1977年01月09日","家族","",false),
                Person(7, "吉田　葵", "よしだ　あおい", "2018年08月15日","友達","",false),
                Person(8, "川本　依", "かわもと　より", "2009年09月03日","","友達",false),
                Person(9, "三谷　なぎさ", "みたに　なぎさ", "1995年08月11日","友達","",false),
                Person(10, "笹島先輩", "ささじませんぱい", "1989年02月02日","会社","",false)
            )
        }
    }
}
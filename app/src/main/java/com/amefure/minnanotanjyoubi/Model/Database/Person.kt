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
                Person(1, "吉田　真紘", "よしだ　まひろ", "1994-12-21","友達","",false),
                Person(2, "長谷　慎二", "はせ　しんじ", "1990-3-3","会社","",false),
                Person(3, "森山　美沙子", "もりやま　みさこ", "1994-5-12","友達","",false),
                Person(4, "中川　健太", "なかがわ　けんた", "2008-6-29","友達","",false),
                Person(5, "お父さん", "おとうさん", "1969-7-7","家族","",false),
                Person(6, "お母さん", "おかあさん", "1977-1-9","家族","",false),
                Person(7, "吉田　葵", "よしだ　あおい", "2018-8-15","友達","",false),
                Person(8, "川本　依", "かわもと　より", "2009-9-3","","友達",false),
                Person(9, "三谷　なぎさ", "みたに　なぎさ", "1995-8-11","友達","",false),
                Person(10, "笹島先輩", "ささじませんぱい", "1989-2-2","会社","",false)
            )
        }
    }
}
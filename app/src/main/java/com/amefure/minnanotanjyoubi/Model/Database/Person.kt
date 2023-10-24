package com.amefure.minnanotanjyoubi.Model.Database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "person_table")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,     // 名前
    var ruby: String,     // ルビ
    var date: Date ,      // 誕生日
    var relation: String, // 関係
    var memo: String,     // メモ
    var alert: Boolean = false // 通知フラグ
)
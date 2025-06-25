package com.amefure.minnanotanjyoubi.Repository

import com.amefure.minnanotanjyoubi.Model.Database.Person

/** アプリ内で扱うPersonデータのRepositoryインターフェース */
interface RootRepository {

    fun loadAllPerson(callback: (List<Person>) -> Unit)

    fun insertPerson(
        name: String,
        ruby: String,
        date: String,
        relation: String,
        memo: String,
        alert: Boolean,
    ): Long

    fun updatePerson(
        id: Int,
        name: String,
        ruby: String,
        date: String,
        relation: String,
        memo: String,
        alert: Boolean,
    )

    fun deletePerson(id: Int)
}
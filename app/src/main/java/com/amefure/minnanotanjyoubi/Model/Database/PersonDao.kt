package com.amefure.minnanotanjyoubi.Model.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Flowable

@Dao
interface PersonDao {
    @Insert
    fun insertPerson(person: Person): Long

    @Update
    fun updatePerson(person: Person)

    @Query("DELETE FROM person_table WHERE id = :id")
    fun deletePerson(id: Int)

    @Query("SELECT * FROM person_table")
    fun getAllPerson(): Flowable<List<Person>>

}
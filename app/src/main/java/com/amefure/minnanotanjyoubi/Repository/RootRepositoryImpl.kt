package com.amefure.minnanotanjyoubi.Repository

import android.content.Context
import com.amefure.minnanotanjyoubi.Model.Database.Person
import com.amefure.minnanotanjyoubi.Model.Database.PersonDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/** アプリ内で扱うPersonデータのRepository実態 */
class RootRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
): RootRepository {
    // Dao
    private val personDao = PersonDatabase.getDatabase(context).userDao()

    // ゴミ箱
    private val compositeDisposable = CompositeDisposable()

    override fun loadAllPerson(callback: (List<Person>) -> Unit) {
        compositeDisposable.add(
            personDao
                .getAllPerson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { people ->
                        // データ取得完了時の処理
                        callback(people)
                    },
                ),
        )
    }

    override fun insertPerson(
        name: String,
        ruby: String,
        date: String,
        relation: String,
        memo: String,
        alert: Boolean,
    ): Long {
        val person =
            Person(
                id = 0,
                name = name,
                ruby = ruby,
                date = date,
                relation = relation,
                memo = memo,
                alert = alert,
            )
        return personDao.insertPerson(person)
    }

    override fun updatePerson(
        id: Int,
        name: String,
        ruby: String,
        date: String,
        relation: String,
        memo: String,
        alert: Boolean,
    ) {
        val person =
            Person(
                id = id,
                name = name,
                ruby = ruby,
                date = date,
                relation = relation,
                memo = memo,
                alert = alert,
            )
        personDao.updatePerson(person)
    }

    override fun deletePerson(id: Int) {
        personDao.deletePerson(id)
    }
}

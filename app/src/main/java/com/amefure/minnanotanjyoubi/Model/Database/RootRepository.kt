package com.amefure.minnanotanjyoubi.Model.Database


import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class RootRepository (context: Context) {

    // Dao
    private val personDao = PersonDatabase.getDatabase(context).userDao()

    // ゴミ箱
    private val compositeDisposable = CompositeDisposable()


    public fun loadAllPerson(callback: (List<Person>) -> Unit) {
        compositeDisposable.add(
            personDao.getAllPerson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { people ->
                        // データ取得完了時の処理
                        callback(people)
                    }
                )
        )
    }

    public fun insertPerson(name: String, ruby: String, date: String, relation: String, memo: String, alert:Boolean): Long{
        val person = Person(
            id = 0,
            name = name,
            ruby = ruby,
            date = date,
            relation = relation,
            memo = memo,
            alert = alert
        )
        return personDao.insertPerson(person)
    }

    public fun updatePerson(id: Int, name: String, ruby: String, date: String, relation: String, memo: String, alert:Boolean) {
        val person = Person(
            id = id,
            name = name,
            ruby = ruby,
            date = date,
            relation = relation,
            memo = memo,
            alert = alert
        )
        personDao.updatePerson(person)
    }

    public fun deletePerson(id: Int) {
        personDao.deletePerson(id)
    }

}
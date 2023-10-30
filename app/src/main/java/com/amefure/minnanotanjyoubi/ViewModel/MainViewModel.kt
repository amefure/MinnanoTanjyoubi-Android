package com.amefure.minnanotanjyoubi.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amefure.minnanotanjyoubi.Model.Database.Person
import com.amefure.minnanotanjyoubi.Model.Relation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Filter

class MainViewModel(app: Application): RootViewModel(app)  {

    private var _personList = MutableLiveData<List<Person>>()
    var personList: LiveData<List<Person>> = _personList

    public fun fetchAllPerson() {
        viewModelScope.launch(Dispatchers.IO) {
            rootRepository.loadAllPerson {
                _personList.postValue(it)
            }
        }
    }

    public fun fetchFilterPerson(relation: Relation) {
        viewModelScope.launch(Dispatchers.IO) {
            rootRepository.loadAllPerson {
                when (relation) {
                    Relation.FRIEND -> {
                        _personList.postValue(it.filter { it.relation == Relation.FRIEND.value() })
                        Log.e("================",it.filter { it.relation == Relation.FRIEND.value() }.toString())
                    }
                    Relation.FAMILY -> {
                        _personList.postValue(it.filter { it.relation == Relation.FAMILY.value() })
                        Log.e("================",it.filter { it.relation == Relation.FAMILY.value() }.toString())

                    }
                    Relation.SCHOOL -> {
                        _personList.postValue(it.filter { it.relation == Relation.SCHOOL.value() })
                        Log.e("================",it.filter { it.relation == Relation.SCHOOL.value() }.toString())

                    }
                    Relation.WORK -> {
                        _personList.postValue(it.filter { it.relation == Relation.WORK.value() })
                    }
                    Relation.OTHER -> {
                        _personList.postValue(it.filter { it.relation == Relation.OTHER.value() })
                    }
                }
            }
        }
    }
}
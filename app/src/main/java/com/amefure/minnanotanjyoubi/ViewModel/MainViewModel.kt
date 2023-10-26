package com.amefure.minnanotanjyoubi.ViewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amefure.minnanotanjyoubi.Model.Database.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
}
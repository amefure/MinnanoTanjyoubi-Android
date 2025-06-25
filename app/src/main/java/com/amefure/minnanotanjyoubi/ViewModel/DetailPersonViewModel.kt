package com.amefure.minnanotanjyoubi.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amefure.minnanotanjyoubi.Repository.RootRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailPersonViewModel @Inject constructor(
    private val rootRepository: RootRepository
) : ViewModel() {
    public fun updatePerson(
        id: Int,
        name: String,
        ruby: String,
        date: String,
        relation: String,
        memo: String,
        alert: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            rootRepository.updatePerson(id, name, ruby, date, relation, memo, alert)
        }
    }
}

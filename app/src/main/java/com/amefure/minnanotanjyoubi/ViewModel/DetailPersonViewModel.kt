package com.amefure.minnanotanjyoubi.ViewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailPersonViewModel(
    app: Application,
) : RootViewModel(app) {
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

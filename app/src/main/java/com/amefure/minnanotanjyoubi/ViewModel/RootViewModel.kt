package com.amefure.minnanotanjyoubi.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.amefure.minnanotanjyoubi.View.Activity.App

abstract class RootViewModel(application: Application) : AndroidViewModel(application) {
    protected val rootRepository = (application as App).rootRepository
}
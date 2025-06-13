package com.amefure.minnanotanjyoubi.View.Activity

import android.app.Application
import com.amefure.minnanotanjyoubi.Model.Database.RootRepository

class App : Application() {
    val rootRepository: RootRepository by lazy { RootRepository(this) }
}

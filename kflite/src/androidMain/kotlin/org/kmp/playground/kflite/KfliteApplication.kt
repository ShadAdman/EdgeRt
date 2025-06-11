package org.kmp.playground.kflite

import android.app.Application

class KfliteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.setUp(applicationContext)
    }
}
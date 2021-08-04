package com.droidtools.hiddentestapp

import android.app.Application
import com.droidtools.hiddentestapp.pref.PrefsHelper

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // initialize preference
        PrefsHelper.init(this)
    }
}
package com.appadore.flagchallenge

import android.app.Application
import com.appadore.flagchallenge.util.Utils

class FlagChallenge:Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
}
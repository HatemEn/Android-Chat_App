package com.me.hatem.a02_kt_fa.Controller

import android.app.Application
import com.me.hatem.a02_kt_fa.Utilities.SharedPrefs

class App : Application() {
    /*
    * Its global activity which start first before any activity
    * Should extend to Application()
    * Should also set up the manifests Application tag by add name attribute
    * */

    // Its singleton inside the class
    companion object {
        lateinit var prefs: SharedPrefs
    }
    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}
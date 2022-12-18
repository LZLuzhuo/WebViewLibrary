package me.luzhuo.webviewdemo

import androidx.multidex.MultiDex
import me.luzhuo.lib_core.app.base.CoreBaseApplication

class MyApplication : CoreBaseApplication() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }
}
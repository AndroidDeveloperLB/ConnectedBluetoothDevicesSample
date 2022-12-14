package com.example.connected_bluetooth_devices_sample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.*
import androidx.lifecycle.AndroidViewModel

/**usage: class MyViewModel(application: Application) : BaseViewModel(application)
 * getting instance:    private lateinit var viewModel: MyViewModel
 * viewModel=ViewModelProvider(this).get(MyViewModel::class.java)*/
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    @Suppress("MemberVisibilityCanBePrivate")
    var isCleared = false
    @Suppress("MemberVisibilityCanBePrivate")
    val onClearedListeners = ArrayList<Runnable>()

    @Suppress("unused")
    @SuppressLint("StaticFieldLeak")
    val context: Context = application.applicationContext
    @Suppress("unused")
    val handler = Handler(Looper.getMainLooper())

    override fun onCleared() {
        super.onCleared()
        isCleared = true
        onClearedListeners.forEach { it.run() }
    }
}

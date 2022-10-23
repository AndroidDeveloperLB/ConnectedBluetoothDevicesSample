package com.example.connected_bluetooth_devices_sample

import android.content.*
import android.os.Build
import androidx.core.content.ContextCompat

inline fun <reified T : Any> Context.getSystemService(): T =
    ContextCompat.getSystemService(applicationContext, T::class.java)!!

object Utils {
}

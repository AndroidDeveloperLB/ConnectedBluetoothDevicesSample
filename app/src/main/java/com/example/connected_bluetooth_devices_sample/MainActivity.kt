package com.example.connected_bluetooth_devices_sample

import android.Manifest
import android.os.*
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        viewModel.initWithLifecycle(lifecycle)
        viewModel.bluetoothTurnedOnLiveData.observe(this) {
            Log.d("AppLog", "MainActivity bluetoothTurnedOnLiveData BT turned on? $it")
        }
        viewModel.isConnectedToBtHeadsetLiveData.observe(this) {
            Log.d("AppLog", "MainActivity isConnectedToBtHeadsetLiveData BT headset connected? $it")
        }
        viewModel.connectedDevicesLiveData.observe(this) {
            Log.d("AppLog", "MainActivity connectedDevicesLiveData devices: $it")
        }
        findViewById<View>(R.id.grantBtPermission).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
            }
        }
    }
}

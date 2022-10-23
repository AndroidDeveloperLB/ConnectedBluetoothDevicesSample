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
        //        viewModel.connectedDevicesLiveData.observe(this) { state ->
        //            when (state) {
        //                MainActivityViewModel.ConnectedDevicesState.BluetoothIsTurnedOff -> {
        //                    Log.d("AppLog", "disconnected")
        //                }
        //
        //                is MainActivityViewModel.ConnectedDevicesState.GotResult -> {
        //                    val connectedDevices = state.connectedDevices
        //                    if(connectedDevices.isEmpty())
        //                        Log.d("AppLog", "no connected devices")
        //                    else {
        //                        //noinspection MissingPermission
        //                        val connectedDevicesNames = connectedDevices.joinToString { it.name }
        //                        Log.d("AppLog", "connected devices:$connectedDevicesNames")
        //                    }
        //                }
        //
        //                MainActivityViewModel.ConnectedDevicesState.Idle ->{
        //                    Log.d("AppLog", "idle")
        //                }
        //                MainActivityViewModel.ConnectedDevicesState.Loading -> {
        //                    Log.d("AppLog", "checking connected devices")
        //                }
        //                MainActivityViewModel.ConnectedDevicesState.NeedBlueToothConnectPermission -> {
        //                    Log.d("AppLog", "NeedBlueToothConnectPermission")
        //                }
        //            }
        //        }
        //        val bluetoothAdapter: BluetoothAdapter =
        //            getSystemService<BluetoothManager>().adapter
        //
        //        val btFoundReceiver = object : BroadcastReceiver() {
        //            override fun onReceive(context: Context?, intent: Intent) {
        //                val action = intent.action
        //                Log.d("AppLog", "action:$action")
        //                if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
        //                    if (action == BluetoothDevice.ACTION_FOUND) {
        //                        val device: BluetoothDevice =
        //                            intent.getParcelableExtraCompat(BluetoothDevice.EXTRA_DEVICE)!!
        //                        val clazz: BluetoothClass =
        //                            intent.getParcelableExtraCompat(BluetoothDevice.EXTRA_CLASS)!!
        //                        Log.d("AppLog", "found BT of device:${device.name} ${device.address} ${clazz.majorDeviceClass} ${clazz.deviceClass}")
        //                    }
        //                }
        //                else{
        //                    Log.d("AppLog", "missing permission to get found BT device")
        //                }
        //
        //            }
        //
        //        }
        //        registerReceiver(btFoundReceiver, IntentFilter().also {
        //            it.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        //            it.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        //            it.addAction(BluetoothDevice.ACTION_FOUND)
        //        })
        //        val successCancelingDiscovery = bluetoothAdapter.cancelDiscovery()
        //        Log.d("AppLog", "successCancelingDiscovery?$successCancelingDiscovery")
        //        val successStartingDiscovery = bluetoothAdapter.startDiscovery()
        //        Log.d("AppLog", "successStartingDiscovery?$successStartingDiscovery")
        //        lifecycle.addObserver(object : DefaultLifecycleObserver {
        //            override fun onDestroy(owner: LifecycleOwner) {
        //                super.onDestroy(owner)
        //                unregisterReceiver(btFoundReceiver)
        //            }
        //        })
    }
}

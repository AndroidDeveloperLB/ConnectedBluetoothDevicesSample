package com.example.connected_bluetooth_devices_sample

import android.Manifest
import android.app.Application
import android.bluetooth.*
import android.content.*
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.*

@UiThread
class MainActivityViewModel(application: Application) : BaseViewModel(application) {
    private val bluetoothAdapter: BluetoothAdapter =
        context.getSystemService<BluetoothManager>()!!.adapter
    private var bluetoothHeadsetProfile: BluetoothProfile? = null
    val connectedDevicesLiveData =
        DistinctLiveDataWrapper(MutableLiveData<ConnectedDevicesState>(ConnectedDevicesState.Idle))
    val bluetoothTurnedOnLiveData = DistinctLiveDataWrapper(MutableLiveData<Boolean?>(null))
    val isConnectedToBtHeadsetLiveData = DistinctLiveDataWrapper(MutableLiveData<Boolean?>(null))
    private val pollingBtStateRunnable: Runnable

    init {
        updateBtStates()
        pollingBtStateRunnable = object : Runnable {
            override fun run() {
                updateBtStates()
                handler.postDelayed(this, POLLING_TIME_IN_MS)
            }
        }
        // Establish connection to the proxy.
        val serviceListener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
                this@MainActivityViewModel.bluetoothHeadsetProfile = bluetoothProfile
                handler.removeCallbacks(pollingBtStateRunnable)
                pollingBtStateRunnable.run()
            }

            override fun onServiceDisconnected(profile: Int) {
                handler.removeCallbacks(pollingBtStateRunnable)
                updateBtStates()
            }
        }
        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HEADSET)
        onClearedListeners.add {
            this.bluetoothHeadsetProfile?.let { bluetoothProfile ->
                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
            }
            handler.removeCallbacks(pollingBtStateRunnable)
        }
    }

    fun initWithLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                pollingBtStateRunnable.run()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                handler.removeCallbacks(pollingBtStateRunnable)
            }
        })
    }

    @UiThread
    private fun updateBtStates() {
        //        Log.d("AppLog", "updateBtStates")
        val isBlueToothTurnedOn = bluetoothAdapter.state == BluetoothAdapter.STATE_ON
        bluetoothTurnedOnLiveData.value = isBlueToothTurnedOn
        if (!isBlueToothTurnedOn) {
            connectedDevicesLiveData.value = ConnectedDevicesState.BluetoothIsTurnedOff
            isConnectedToBtHeadsetLiveData.value = false
            return
        }
        val isConnectedToBtHeadset = try {
            bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothAdapter.STATE_CONNECTED
        } catch (e: SecurityException) {
            null
        }
        isConnectedToBtHeadsetLiveData.value = isConnectedToBtHeadset
        val bluetoothProfile = bluetoothHeadsetProfile
        if (bluetoothProfile != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                val connectedDevicesSet = bluetoothProfile.connectedDevices.toHashSet()
                val previousConnectedDevices =
                    (connectedDevicesLiveData.value as? ConnectedDevicesState.GotResult)?.connectedDevices
                if (previousConnectedDevices == null || previousConnectedDevices != connectedDevicesSet)
                    connectedDevicesLiveData.value =
                        ConnectedDevicesState.GotResult(connectedDevicesSet)
            } else {
                connectedDevicesLiveData.value =
                    ConnectedDevicesState.NeedBlueToothConnectPermission
            }
        } else {
            connectedDevicesLiveData.value = ConnectedDevicesState.Idle
        }
    }

    companion object {
        private const val POLLING_TIME_IN_MS = 500L
    }
}

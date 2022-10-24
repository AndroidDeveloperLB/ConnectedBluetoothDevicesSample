package com.example.connected_bluetooth_devices_sample

import android.Manifest
import android.app.Application
import android.bluetooth.*
import android.content.*
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*

@UiThread
class MainActivityViewModel(application: Application) : BaseViewModel(application) {
    private val bluetoothAdapter: BluetoothAdapter =
        context.getSystemService<BluetoothManager>().adapter
    private var bluetoothHeadsetProfile: BluetoothProfile? = null
    val connectedDevicesLiveData =
        DistinctLiveDataWrapper(MutableLiveData<ConnectedDevicesState>(ConnectedDevicesState.Idle))
    val bluetoothTurnedOnLiveData = DistinctLiveDataWrapper(MutableLiveData<Boolean?>(null))
    val isConnectedToBtHeadsetLiveData = DistinctLiveDataWrapper(MutableLiveData<Boolean?>(null))

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

    init {
        //        Log.d("AppLog", "MainActivityViewModel CTOR")
        Log.d("AppLog", "bluetoothAdapter.state:${getBluetoothAdapterStateStr(bluetoothAdapter.state)} ${checkIfConnectedToHeadset(bluetoothAdapter)}")
        updateBtStates()
        val pollingBtStateRunnable = object : Runnable {
            override fun run() {
                updateBtStates()
                handler.postDelayed(this, POLLING_TIME_IN_MS)
            }
        }
        // Establish connection to the proxy.
        val serviceListener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
                Log.d("AppLog", "onServiceConnected  bluetoothAdapter.state:${getBluetoothAdapterStateStr(bluetoothAdapter.state)} ${checkIfConnectedToHeadset(bluetoothAdapter)}")
                this@MainActivityViewModel.bluetoothHeadsetProfile = bluetoothProfile
                handler.removeCallbacks(pollingBtStateRunnable)
                pollingBtStateRunnable.run()
                //                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                //                    val connectedDevices = bluetoothProfile.connectedDevices
                //                    Log.d("AppLog", "connectedDevices:${connectedDevices.size} ${connectedDevices.joinToString { it.name }}")
                //                    connectedDevicesLiveData.value =
                //                        ConnectedDevicesState.GotResult(connectedDevices.toHashSet())
                //                } else {
                //                    //                    Log.d("AppLog", "missing permission to get information of connected devices")
                //                    connectedDevicesLiveData.value =
                //                        ConnectedDevicesState.NeedBlueToothConnectPermission
                //                }
                //                onClearedListeners.add {
                //                    this@MainActivityViewModel.bluetoothProfile = null
                //                    //                    Log.d("AppLog", "closing bluetoothAdapter")
                //                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
                //                }
            }

            override fun onServiceDisconnected(profile: Int) {
                Log.d("AppLog", "onServiceDisconnected bluetoothAdapter.state:${getBluetoothAdapterStateStr(bluetoothAdapter.state)}  ${checkIfConnectedToHeadset(bluetoothAdapter)}")
                handler.removeCallbacks(pollingBtStateRunnable)
                updateBtStates()
                //                connectedDevicesLiveData.value = ConnectedDevicesState.BluetoothIsTurnedOff
            }
        }
        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HEADSET)
        onClearedListeners.add {
            this.bluetoothHeadsetProfile?.let { bluetoothProfile ->
                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
            }
            handler.removeCallbacks(pollingBtStateRunnable)
        }

        //
        //        val btConnectedReceiver = object : BroadcastReceiver() {
        //            override fun onReceive(context: Context, intent: Intent) {
        //                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        //                    Log.d("AppLog", "missing permission to handle BT connected devices intent")
        //                    connectedDevicesLiveData.value =
        //                        ConnectedDevicesState.NeedBlueToothConnectPermission
        //                    return
        //                }
        //                val action = intent.action
        //                val bluetoothDevice: BluetoothDevice =
        //                    intent.getParcelableExtraCompat(BluetoothDevice.EXTRA_DEVICE)!!
        //                val bondState = bluetoothDevice.bondState
        //                val connectionState = bluetoothProfile?.getConnectionState(bluetoothDevice)
        //                when (action) {
        //                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
        //                        Log.d("AppLog", "connected device:${bluetoothDevice.name} bondState:${getBondStateStr(bondState)} connectionState:${getBluetoothDeviceConnectionStateStr(connectionState)}")
        //                        if (bondState == BluetoothDevice.BOND_NONE || connectionState == BluetoothProfile.STATE_DISCONNECTING || connectionState == BluetoothProfile.STATE_DISCONNECTED)
        //                            return
        //                        (connectedDevicesLiveData.value as? ConnectedDevicesState.GotResult)?.let { result: ConnectedDevicesState.GotResult ->
        //                            val newConnectedDevicesCollection =
        //                                HashSet<BluetoothDevice>(result.connectedDevices.size + 1)
        //                            newConnectedDevicesCollection.addAll(result.connectedDevices)
        //                            newConnectedDevicesCollection.add(bluetoothDevice)
        //                            connectedDevicesLiveData.value =
        //                                ConnectedDevicesState.GotResult(newConnectedDevicesCollection)
        //                        }
        //                    }
        //
        //                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
        //                        Log.d("AppLog", "disconnected device:${bluetoothDevice.name} bondState:${getBondStateStr(bondState)} connectionState:${getBluetoothDeviceConnectionStateStr(connectionState)}")
        //                        (connectedDevicesLiveData.value as? ConnectedDevicesState.GotResult)?.let { result: ConnectedDevicesState.GotResult ->
        //                            val newConnectedDevicesCollection =
        //                                HashSet<BluetoothDevice>(result.connectedDevices.size + 1)
        //                            newConnectedDevicesCollection.addAll(result.connectedDevices)
        //                            newConnectedDevicesCollection.remove(bluetoothDevice)
        //                            connectedDevicesLiveData.value =
        //                                ConnectedDevicesState.GotResult(newConnectedDevicesCollection)
        //                        }
        //                    }
        //
        //                    BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
        //                        Log.d("AppLog", "BT connection changed: ${getBluetoothAdapterStateStr(bluetoothAdapter.state)}")
        //                    }
        //
        //                    else -> {
        //                        //                        Log.d("AppLog", "btConnectedReceiver action:$action")
        //                    }
        //                }
        //            }
        //
        //        }
        //        context.registerReceiver(btConnectedReceiver, IntentFilter().also { filter ->
        //            //                            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        //            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        //            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        //            filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        //        })
        //        onClearedListeners.add {
        //            //            Log.d("AppLog", "closing btConnectedReceiver")
        //            context.unregisterReceiver(btConnectedReceiver)
        //        }
    }


    //    fun refresh() {
    //        connectedDevicesLiveData.value = ConnectedDevicesState.Loading
    //        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
    //            //            Log.d("AppLog", "missing permission to get connected devices list")
    //            connectedDevicesLiveData.value = ConnectedDevicesState.NeedBlueToothConnectPermission
    //        } else {
    //            bluetoothProfile.let { bluetoothProfile ->
    //                if (bluetoothProfile != null) {
    //                    connectedDevicesLiveData.value =
    //                        ConnectedDevicesState.GotResult(bluetoothProfile.connectedDevices.toHashSet())
    //                } else {
    //                    connectedDevicesLiveData.value = ConnectedDevicesState.BluetoothIsTurnedOff
    //                }
    //            }
    //        }
    //    }

    companion object {
        private const val POLLING_TIME_IN_MS = 500L
        private fun getBluetoothAdapterStateStr(bluetoothAdapterState: Int): String {
            //            https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#getState()
            return when (bluetoothAdapterState) {
                BluetoothAdapter.STATE_OFF -> "STATE_OFF ($bluetoothAdapterState)"
                BluetoothAdapter.STATE_TURNING_ON -> "STATE_TURNING_ON ($bluetoothAdapterState)"
                BluetoothAdapter.STATE_ON -> "STATE_ON ($bluetoothAdapterState)"
                BluetoothAdapter.STATE_TURNING_OFF -> "STATE_TURNING_OFF ($bluetoothAdapterState)"
                else -> "unknownBluetoothAdapterState:$bluetoothAdapterState"
            }
        }

        private fun getBondStateStr(bondState: Int): String {
            //            https://developer.android.com/reference/android/bluetooth/BluetoothDevice#getBondState()
            return when (bondState) {
                BluetoothDevice.BOND_BONDED -> "BOND_BONDED ($bondState)"
                BluetoothDevice.BOND_BONDING -> "BOND_BONDING ($bondState)"
                BluetoothDevice.BOND_NONE -> "BOND_NONE ($bondState)"
                else -> "unknownBondState:$bondState"
            }
        }

        @Suppress("KotlinConstantConditions")
        private fun getBluetoothDeviceConnectionStateStr(connectionState: Int?): String {
            //            https://developer.android.com/reference/android/bluetooth/BluetoothProfile#getConnectionState(android.bluetooth.BluetoothDevice)
            return when (connectionState) {
                BluetoothProfile.STATE_CONNECTED -> "STATE_CONNECTED ($connectionState)"
                BluetoothProfile.STATE_CONNECTING -> "STATE_CONNECTING ($connectionState)"
                BluetoothProfile.STATE_DISCONNECTING -> "STATE_DISCONNECTING ($connectionState)"
                BluetoothProfile.STATE_DISCONNECTED -> "STATE_DISCONNECTED ($connectionState)"
                null -> "unknown"
                else -> "unknownConnectionState:$connectionState"
            }
        }

        private fun checkIfConnectedToHeadset(bluetoothAdapter: BluetoothAdapter): String {
            try {
                val str = when (val headsetConnectionState =
                    bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                    BluetoothAdapter.STATE_DISCONNECTED -> "STATE_DISCONNECTED ($headsetConnectionState)"
                    BluetoothAdapter.STATE_CONNECTING -> "STATE_CONNECTING ($headsetConnectionState)"
                    BluetoothAdapter.STATE_CONNECTED -> "STATE_CONNECTED ($headsetConnectionState)"
                    BluetoothAdapter.STATE_DISCONNECTING -> "STATE_DISCONNECTING ($headsetConnectionState)"
                    else -> "unknownHeadsetState:headsetConnectionState"
                }
                return "checkIfConnectedToHeadset:$str"
            } catch (e: SecurityException) {
                return "failed to get headsetConnectionState:$e"
            }
        }
    }
}

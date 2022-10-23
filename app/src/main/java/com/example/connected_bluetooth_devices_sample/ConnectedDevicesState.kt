package com.example.connected_bluetooth_devices_sample

import android.bluetooth.BluetoothDevice

sealed class ConnectedDevicesState {
    object Idle : ConnectedDevicesState() {
        override fun toString(): String {
            if (BuildConfig.DEBUG)
                return "Idle"
            return super.toString()
        }
    }

    class GotResult(@Suppress("MemberVisibilityCanBePrivate") val connectedDevices: Set<BluetoothDevice>) : ConnectedDevicesState() {
        override fun toString(): String {
            if (BuildConfig.DEBUG) {
                return "GotResult: connectedDevices:${
                    connectedDevices.map {
                        try {
                            it.name
                        } catch (e: SecurityException) {
                            it.address
                        }
                    }
                }"
            }
            return super.toString()
        }
    }

    object BluetoothIsTurnedOff : ConnectedDevicesState() {
        override fun toString(): String {
            if (BuildConfig.DEBUG)
                return "BluetoothIsTurnedOff"
            return super.toString()
        }
    }

    object NeedBlueToothConnectPermission : ConnectedDevicesState() {
        override fun toString(): String {
            if (BuildConfig.DEBUG)
                return "NeedBlueToothConnectPermission"
            return super.toString()
        }
    }
}

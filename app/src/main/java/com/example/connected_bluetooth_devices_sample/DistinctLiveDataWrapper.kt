package com.example.connected_bluetooth_devices_sample

import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.lifecycle.*

class DistinctLiveDataWrapper<T>(@Suppress("MemberVisibilityCanBePrivate") val mutableLiveData: MutableLiveData<T>) {
    @Suppress("MemberVisibilityCanBePrivate")
    val distinctLiveData = Transformations.distinctUntilChanged(mutableLiveData)
    var value: T?
        @UiThread
        set(value) {
            mutableLiveData.value = value
        }
        get() {
            return mutableLiveData.value
        }

    @AnyThread
    fun postValue(value: T) {
        mutableLiveData.postValue(value)
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<in T>) {
        distinctLiveData.observe(lifecycleOwner, observer)
    }
}

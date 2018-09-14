package com.hyc.m3u8downloader.utils

import android.util.Log
import kotlin.reflect.KProperty

class ItemDelegate<T> {
    var value: T? = null
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
        Log.e("hyc--000", thisRef.toString() + "-----" + property.toString() + "-----" + value)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }

}
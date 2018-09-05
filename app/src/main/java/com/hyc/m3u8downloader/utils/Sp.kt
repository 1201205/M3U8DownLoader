package com.hyc.m3u8downloader.utils

import android.content.Context
import android.content.SharedPreferences
import com.hyc.m3u8downloader.MainApplication
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class Sp<T>(val name: String, val default: T) {
    val pref: SharedPreferences by lazy { MainApplication.instance.getSharedPreferences(name, Context.MODE_PRIVATE) }
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getSharedPreferences(name, default)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putSharedPreferences(name, value)
    }

    private fun putSharedPreferences(name: String, value: T) = with(pref.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("sp can not save $name-$value")
        }.apply()
    }

    private fun getSharedPreferences(name: String, default: T): T = with(pref) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("sp can not get $name")
        }
        return res as T
    }

}
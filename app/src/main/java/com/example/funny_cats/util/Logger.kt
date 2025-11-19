package com.example.funny_cats.util

import android.util.Log
import com.example.funny_cats.BuildConfig

object Logger {

    private const val TAG = "FunnyCats"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    fun i(message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message, throwable)
        }
    }

    fun w(message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, message)
        }
    }

    fun v(message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, message)
        }
    }
}
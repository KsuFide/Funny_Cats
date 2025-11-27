package com.example.funny_cats.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.funny_cats.BuildConfig
import com.example.funny_cats.util.Logger

open class BaseFragment : Fragment() {

    private val tag = "MemoryMonitor"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logMemory("onCreate")
    }

    override fun onStart() {
        super.onStart()
        logMemory("onStart")
    }

    override fun onResume() {
        super.onResume()
        logMemory("onResume")
    }

    override fun onPause() {
        super.onPause()
        logMemory("onPause")
    }

    override fun onStop() {
        super.onStop()
        logMemory("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        logMemory("onDestroy")
    }

    private fun logMemory(method: String) {
        if (BuildConfig.DEBUG) {
            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
            val maxMemory = runtime.maxMemory() / (1024 * 1024)

            Logger.d("${this::class.java.simpleName}.$method - Memory: ${usedMemory}MB / ${maxMemory}MB")
        }
    }
}
package com.aeromarine.logbook.ror.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.aeromarine.logbook.ror.presentation.app.AeroMarineApp

class AeroMarinePushHandler() {
    fun chickenHandlePush(extras: Bundle?) {
        Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map = todoSphereBundleToMap(extras)
            Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Map from Push = $map")
            map?.let {
                if (map.containsKey("url")) {
                    AeroMarineApp.Companion.CHICKEN_FB_LI = map["url"]
                    Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Push data no!")
        }
    }

    private fun todoSphereBundleToMap(extras: Bundle): Map<String, String?>? {
        val map: MutableMap<String, String?> = HashMap()
        val ks = extras.keySet()
        val iterator: Iterator<String> = ks.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = extras.getString(key)
        }
        return map
    }

}
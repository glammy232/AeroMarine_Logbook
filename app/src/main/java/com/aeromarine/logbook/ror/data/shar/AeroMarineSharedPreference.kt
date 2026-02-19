package com.aeromarine.logbook.ror.data.shar

import android.content.Context
import androidx.core.content.edit

class AeroMarineSharedPreference(context: Context) {
    private val chickenPrefs = context.getSharedPreferences("aeromarineSharedPrefsAb", Context.MODE_PRIVATE)

    var chickenSavedUrl: String
        get() = chickenPrefs.getString(CHICKEN_SAVED_URL, "") ?: ""
        set(value) = chickenPrefs.edit { putString(CHICKEN_SAVED_URL, value) }

    var chickenExpired : Long
        get() = chickenPrefs.getLong(CHICKEN_EXPIRED, 0L)
        set(value) = chickenPrefs.edit { putLong(CHICKEN_EXPIRED, value) }

    var chickenAppState: Int
        get() = chickenPrefs.getInt(CHICKEN_APPLICATION_STATE, 0)
        set(value) = chickenPrefs.edit { putInt(CHICKEN_APPLICATION_STATE, value) }

    var chickenNotificationRequest: Long
        get() = chickenPrefs.getLong(CHICKEN_NOTIFICAITON_REQUEST, 0L)
        set(value) = chickenPrefs.edit { putLong(CHICKEN_NOTIFICAITON_REQUEST, value) }

    var chickenNotificationRequestedBefore: Boolean
        get() = chickenPrefs.getBoolean(CHICKEN_NOTIFICATION_REQUEST_BEFORE, false)
        set(value) = chickenPrefs.edit { putBoolean(
            CHICKEN_NOTIFICATION_REQUEST_BEFORE, value) }

    companion object {
        private const val CHICKEN_SAVED_URL = "fishlocationlogSavedUrl"
        private const val CHICKEN_EXPIRED = "fishlocationlogExpired"
        private const val CHICKEN_APPLICATION_STATE = "fishlocationlogApplicationState"
        private const val CHICKEN_NOTIFICAITON_REQUEST = "fishlocationlogNotificationRequest"
        private const val CHICKEN_NOTIFICATION_REQUEST_BEFORE = "fishlocationlogNotificationRequestedBefore"
    }
}
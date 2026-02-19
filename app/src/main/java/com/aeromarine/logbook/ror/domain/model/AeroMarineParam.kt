package com.aeromarine.logbook.ror.domain.model

import com.google.gson.annotations.SerializedName


private const val AEROMARINE_A = "com.aeromarine.logbook"
data class AeroMarineParam (
    @SerializedName("af_id")
    val chickenAfId: String,
    @SerializedName("bundle_id")
    val chickenBundleId: String = AEROMARINE_A,
    @SerializedName("os")
    val chickenOs: String = "Android",
    @SerializedName("store_id")
    val chickenStoreId: String = AEROMARINE_A,
    @SerializedName("locale")
    val chickenLocale: String,
    @SerializedName("push_token")
    val chickenPushToken: String,
    @SerializedName("firebase_project_id")
    val chickenFirebaseProjectId: String = "aeromarine-logbook"

    )
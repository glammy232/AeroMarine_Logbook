package com.aeromarine.logbook.ror.domain.model

import com.google.gson.annotations.SerializedName


data class AeroMarineEntity (
    @SerializedName("ok")
    val chickenOk: String,
    @SerializedName("url")
    val chickenUrl: String,
    @SerializedName("expires")
    val chickenExpires: Long,
)
package com.comp4200.project.papertrader.models

import com.google.gson.annotations.SerializedName

data class TokenModel (
    @SerializedName("token") val access: String,
    @SerializedName("refresh") val refresh: String
)


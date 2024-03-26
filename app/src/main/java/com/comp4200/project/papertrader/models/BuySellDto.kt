package com.comp4200.project.papertrader.models

import com.google.gson.annotations.SerializedName

data class BuySellDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("quantity") val quantity: Int
)

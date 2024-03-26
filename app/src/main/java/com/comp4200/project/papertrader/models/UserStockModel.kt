package com.comp4200.project.papertrader.models

import com.google.gson.annotations.SerializedName

data class UserStockModel(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("price") val price: Float,
    @SerializedName("quantity") val quantity: Int
)

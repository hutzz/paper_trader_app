package com.comp4200.project.papertrader.models

import com.google.gson.annotations.SerializedName

class StockModel (
    @SerializedName("Datetime") val dateTimes: List<String>,
    @SerializedName("Open") val open: List<Float>,
    @SerializedName("Close") val close: List<Float>,
    @SerializedName("High") val high: List<Float>,
    @SerializedName("Low") val low: List<Float>,
    @SerializedName("Volume") val volume: List<Float>,
    @SerializedName("Stock Splits") val splits: List<Float>,
    @SerializedName("Dividends") val dividends: List<Float>
)
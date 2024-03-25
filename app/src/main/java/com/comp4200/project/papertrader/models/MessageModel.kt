package com.comp4200.project.papertrader.models

import com.google.gson.annotations.SerializedName

data class MessageModel(
    @SerializedName("message") val msg: String
)

package com.comp4200.project.papertrader.models

import com.google.gson.annotations.SerializedName

data class UserModel(
    @SerializedName("public_id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("balance") val balance: Float
)

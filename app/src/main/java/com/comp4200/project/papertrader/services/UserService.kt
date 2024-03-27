package com.comp4200.project.papertrader.services

import android.content.Context
import com.comp4200.project.papertrader.models.UserModel
import okhttp3.OkHttpClient

class UserService(client: OkHttpClient, private val context: Context) : ServiceBase(client, context) {
    suspend fun getUserData(token: String): UserModel {
        val url = createUrl("/me")
        return getJson(url, UserModel::class.java, token)
    }
}
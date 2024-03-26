package com.comp4200.project.papertrader.services

import com.comp4200.project.papertrader.models.UserModel
import okhttp3.OkHttpClient

class UserService(client: OkHttpClient) : ServiceBase(client) {
    suspend fun getUserData(token: String): UserModel {
        val url = createUrl("/me")
        return getJson(url, UserModel::class.java, token)
    }
}
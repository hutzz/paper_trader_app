package com.comp4200.project.papertrader.services

import android.content.Context
import com.comp4200.project.papertrader.models.LoginModel
import com.comp4200.project.papertrader.models.TokenModel

import okhttp3.OkHttpClient

class LoginService(client: OkHttpClient, private val context: Context) : ServiceBase(client, context) {
    suspend fun login(user: LoginModel): TokenModel {
        val url = createUrl("/login")
        return getJson(url, TokenModel::class.java, user.username, user.password)
    }
}
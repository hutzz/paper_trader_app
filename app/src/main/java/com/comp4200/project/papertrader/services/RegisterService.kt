package com.comp4200.project.papertrader.services

import com.comp4200.project.papertrader.models.MessageModel
import com.comp4200.project.papertrader.models.RegisterModel
import okhttp3.OkHttpClient

class RegisterService(client: OkHttpClient) : ServiceBase(client) {
    suspend fun register(user: RegisterModel): MessageModel {
        val url = createUrl("/register")
        return postJson(url, user, MessageModel::class.java)
    }
}
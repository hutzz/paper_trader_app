package com.comp4200.project.papertrader.services

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import com.google.gson.Gson
import com.comp4200.project.papertrader.models.MessageModel


open class ServiceBase (private val client: OkHttpClient) {
    private val gson = Gson()

    fun createUrl(route: String): String {
        val baseUrl = "http://10.147.17.101:5000"
        return baseUrl + route
    }
    @Throws(IOException::class)
    fun <T> getJson(url: String, clazz: Class<T>, token: String? = null,): T {
        val request = requestBuilder(url, token).build()
        val response = client.newCall(request).execute()
        return handleResponse(response, clazz)
    }
    @Throws(IOException::class)
    suspend fun <T> getJson(url: String, clazz: Class<T>, username: String, password: String): T {
        val credentials = Credentials.basic(username, password)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", credentials)
            .build()
        val response = client.newCall(request).execute()
        return handleResponse(response, clazz)
    }
    @Throws(IOException::class)
    fun <T> postJson(url: String, body: Any, clazz: Class<T>, token: String? = null,): T {
        val json = gson.toJson(body)
        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request = requestBuilder(url, token).post(requestBody).build()
        val response = client.newCall(request).execute()
        return handleResponse(response, clazz)
    }
    protected fun <T> getRefreshJson(url: String, token: String): T {
        val request = Request.Builder()
            .url(url)
            .header("x-refresh-token", token)
            .build()
        val response = client.newCall(request).execute()
        return handleResponse(response, MessageModel::class.java) as T
    }
    private fun requestBuilder(url: String, token: String? = null): Request.Builder {
        val builder = Request.Builder().url(url)
        token?.let { builder.addHeader("x-access-token", it) }
        return builder
    }
    @Throws(IOException::class)
    private fun <T> handleResponse(response: Response, clazz: Class<T>): T {
        val responseBody = response.body ?: throw IOException("Response body is null")
        val responseBodyString = responseBody.string()

        if (checkResponse(response)) {
            val message = getMessage(responseBodyString)
            Log.i("info", message)

            return gson.fromJson(responseBodyString, clazz)
        } else {
            throw IOException(getMessage(responseBodyString))
        }
    }

    private fun checkResponse(response: Response): Boolean {
        if (response.isSuccessful) return true
        Log.i("info","Request: ${response.request.url}")
        Log.i("info", "Response status: ${response.code} ${response.message}")
        return false
    }
    private fun getMessage(responseBodyString: String): String {
        val message = gson.fromJson(responseBodyString, MessageModel::class.java)
        return message?.msg ?: "No message available"
    }
}
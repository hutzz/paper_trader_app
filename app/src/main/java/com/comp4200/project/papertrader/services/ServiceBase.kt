package com.comp4200.project.papertrader.services

import android.content.Context
import android.content.Intent
import android.util.Log
import com.comp4200.project.papertrader.LoginActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import com.google.gson.Gson
import com.comp4200.project.papertrader.models.MessageModel
import com.comp4200.project.papertrader.models.TokenModel
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.ZoneOffset


open class ServiceBase (private val client: OkHttpClient, private val context: Context) {
    private val gson = Gson()

    fun createUrl(route: String): String {
        val baseUrl = "http://10.147.17.101:5000"
        return baseUrl + route
    }
    @Throws(IOException::class)
    suspend fun <T> getJson(url: String, clazz: Class<T>, token: String? = null,): T {
        checkTokenExpiration(token)
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val newToken = sharedPreferences.getString("token", null);
        val request = requestBuilder(url, newToken).build()
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
    suspend fun <T> getJson(url: String, type: Type, token: String? = null): T {
        checkTokenExpiration(token)
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val newToken = sharedPreferences.getString("token", null);
        val request = requestBuilder(url, newToken).build()
        val response = client.newCall(request).execute()
        return handleResponse(response, type)
    }
    @Throws(IOException::class)
    suspend fun <T> postJson(url: String, body: Any, clazz: Class<T>, token: String? = null,): T {
        checkTokenExpiration(token)
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val newToken = sharedPreferences.getString("token", null);
        val json = gson.toJson(body)
        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request = requestBuilder(url, newToken).post(requestBody).build()
        val response = client.newCall(request).execute()
        return handleResponse(response, clazz)
    }
    protected fun <T> getRefreshJson(url: String, token: String): T {
        val request = Request.Builder()
            .url(url)
            .header("x-refresh-token", token)
            .build()
        val response = client.newCall(request).execute()
        return handleResponse(response, TokenModel::class.java) as T
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

        if (responseBodyString.isBlank()) {
            throw IOException("Response body is empty")
        }

        if (checkResponse(response)) {
            val message = getMessage(responseBodyString)
            Log.i("info", message ?: "No message available")
            return gson.fromJson(responseBodyString, clazz)
        } else {
            throw IOException(getMessage(responseBodyString) ?: "Error message unavailable")        }
    }
    @Throws(IOException::class)
    private fun <T> handleResponse(response: Response, type: Type): T {
        val responseBody = response.body ?: throw IOException("Response body is null")
        val responseBodyString = responseBody.string()

        if (responseBodyString.isBlank()) {
            throw IOException("Response body is empty")
        }

        if (checkResponse(response)) {
            val message = getMessage(responseBodyString)
            Log.i("info", message ?: "No message available")
            return gson.fromJson(responseBodyString, type)
        } else {
            throw IOException(getMessage(responseBodyString) ?: "Error message unavailable")        }
    }

    private fun checkResponse(response: Response): Boolean {
        if (response.isSuccessful) return true
        Log.i("info","Request: ${response.request.url}")
        Log.i("info", "Response status: ${response.code} ${response.message}")
        return false
    }
    private fun getMessage(responseBodyString: String): String {
        return try {
            val jsonElement = JsonParser.parseString(responseBodyString)
            if (jsonElement.isJsonObject) {
                val messageModel = gson.fromJson(jsonElement, MessageModel::class.java)
                messageModel.msg
            } else {
                "Unexpected JSON structure. Expected an object."
            }
        } catch (e: JsonSyntaxException) {
            Log.e("API Error", "Failed to parse message: ${e.message}")
            "Invalid message format"
        }
    }
    private suspend fun isExpired(token: String): Boolean {
        val url = createUrl("/expiry")
        val requestBody = mapOf("token" to token)
        val responseBody = postJson(url, requestBody, Map::class.java)
        val jsonObject = JSONObject(responseBody) // Assuming you have imported org.json.JSONObject
        val timestamp = jsonObject.optDouble("expiry_time")
        if (timestamp.isNaN()) {
            throw IOException("Invalid response format: expiry_time not found")
        }
        val expiryTime = LocalDateTime.ofEpochSecond(timestamp.toLong(), 0, ZoneOffset.UTC)
        val currentUtcTime = LocalDateTime.now(ZoneOffset.UTC)
        return expiryTime.isBefore(currentUtcTime)
    }
    suspend fun checkTokenExpiration(token: String?) {
        if (token != null && isExpired(token)) {
            try {
                val url = createUrl("/refresh")
                val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                val refresh = sharedPreferences.getString("refresh", null)
                Log.i("basetoken", "access: ${sharedPreferences.getString("token", null)}")
                Log.i("basetoken", "refresh: $refresh")
                val newTokens = refresh?.let { getRefreshJson<TokenModel>(url, it) }
                val editor = sharedPreferences.edit()
                if (newTokens != null) {
                    editor.putString("token", newTokens.access)
                    editor.putString("refresh", newTokens.refresh)
                }
                editor.apply()
                Log.i("basetoken", "new access: ${sharedPreferences.getString("token", null)}")
                Log.i("basetoken", "new refresh: ${sharedPreferences.getString("refresh", null)}")
            } catch (e: Exception) {
                Log.e("error", "refresh error: $e")
                redirectToLogin()
            }
        }
    }

    fun redirectToLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
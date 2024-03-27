package com.comp4200.project.papertrader.services

import android.content.Context
import android.util.Log
import com.comp4200.project.papertrader.models.TokenModel
import com.google.gson.Gson
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TokenService(client: OkHttpClient, private val context: Context) : ServiceBase(client, context) {
    private val accessKey = "token"
    private val refreshKey = "refresh"
    private val gson = Gson()

    suspend fun storeTokens(token: TokenModel) {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        val editor = sharedPreferences.edit()
        editor.putString(accessKey, token.access)
        editor.putString(refreshKey, token.refresh)
        editor.apply()
    }
    suspend fun getAccessToken(): String? {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPreferences.getString(accessKey, null);
    }
    suspend fun getRefreshToken(): String? {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPreferences.getString(refreshKey, null);
    }
    suspend fun deleteTokens(token: TokenModel) {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        val editor = sharedPreferences.edit()
        editor.remove(accessKey)
        editor.remove(refreshKey)
        editor.apply()
    }
    suspend fun refreshTokens() {
        try {
            val url = createUrl("/refresh")
            val refresh = getRefreshToken()
            Log.i("token", "access: " + getAccessToken())
            Log.i("token", "refresh: " + getRefreshToken())
            val newTokens = refresh?.let { getRefreshJson<TokenModel>(url, it) }
            if (newTokens != null) {
                storeTokens(newTokens)
            }
            Log.i("token", "new access: " + getAccessToken())
            Log.i("token", "new refresh: " + getRefreshToken())
        }
        catch (e: Exception) {
            Log.i("TokenService", "issue with refreshing: $e")
            redirectToLogin()
        }
    }
    private suspend fun getExpiryTime(token: String): LocalDateTime {
        val url = createUrl("/expiry")
        val requestBody = mapOf("token" to token)
        val responseBody = postJson(url, requestBody, Map::class.java)
        val jsonObject = JSONObject(responseBody) // Assuming you have imported org.json.JSONObject
        val timestamp = jsonObject.optDouble("expiry_time")
        if (timestamp.isNaN()) {
            throw IOException("Invalid response format: expiry_time not found")
        }
        return LocalDateTime.ofEpochSecond(timestamp.toLong(), 0, ZoneOffset.UTC)
    }

    suspend fun isTokenExpired(token: String): Boolean {
        val expiryTime = getExpiryTime(token)
        val currentUtcTime = LocalDateTime.now(ZoneOffset.UTC)
        return expiryTime.isBefore(currentUtcTime)
    }


}
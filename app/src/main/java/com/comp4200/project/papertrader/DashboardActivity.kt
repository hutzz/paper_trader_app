package com.comp4200.project.papertrader

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.comp4200.project.papertrader.models.LoginModel
import com.comp4200.project.papertrader.models.UserModel
import com.comp4200.project.papertrader.services.TokenService
import com.comp4200.project.papertrader.services.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val usernameText = findViewById<TextView>(R.id.user)


        lifecycleScope.launch {
            try {
                val client = OkHttpClient()
                val userService = UserService(client)
                val tokenService = TokenService(client, this@DashboardActivity)
                val user = getUserData(userService, tokenService)
                usernameText.text = user.username
            } catch (e: Exception) {
                Log.e("DashboardError", "Failed to get user details: ", e)
            }
        }
    }
    private suspend fun getUserData(userService: UserService, tokenService: TokenService): UserModel {
        return withContext(Dispatchers.IO) {
            val token = tokenService.getAccessToken() ?: throw IllegalStateException("Access token is null")
            userService.getUserData(token)
        }
    }
}
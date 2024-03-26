package com.comp4200.project.papertrader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.comp4200.project.papertrader.models.LoginModel
import com.comp4200.project.papertrader.models.TokenModel
import com.comp4200.project.papertrader.services.LoginService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MyAppTag", "This is a debug message.")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<EditText>(R.id.username_editText)
        val passwordEditText = findViewById<EditText>(R.id.password_editText)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            lifecycleScope.launch {
                try {
                    val user = LoginModel(username, password)
                    val client = OkHttpClient()
                    val tokens = login(user, client)
                    handleLoginSuccess(tokens)
                } catch (e: Exception) {
                    Log.e("LoginError", "Login failed: ", e)
                }
            }
        }
    }

    private suspend fun login(loginModel: LoginModel, client: OkHttpClient): TokenModel {
        val loginService = LoginService(client)
        return withContext(Dispatchers.IO) {
            loginService.login(loginModel)
        }
    }

    private fun handleLoginSuccess(tokenModel: TokenModel) {
        val sharedPreferences = getSharedPreferences("YourAppPreferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("accessToken", tokenModel.access)
            putString("refreshToken", tokenModel.refresh)
            apply()
        }

        // Navigate to DashboardActivity correctly placed inside a method
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }


}

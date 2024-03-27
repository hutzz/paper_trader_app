package com.comp4200.project.papertrader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.comp4200.project.papertrader.models.LoginModel
import com.comp4200.project.papertrader.models.TokenModel
import com.comp4200.project.papertrader.services.LoginService
import com.comp4200.project.papertrader.services.TokenService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<EditText>(R.id.username_editText)
        val passwordEditText = findViewById<EditText>(R.id.password_editText)

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val user = LoginModel(username, password)
                    val client = OkHttpClient()
                    val loginService = LoginService(client, this@LoginActivity)
                    val tokens = loginService.login(user)
                    withContext(Dispatchers.Main) {
                        handleLoginSuccess(tokens, client)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("LoginError", "Login failed: ", e)
                        Toast.makeText(this@LoginActivity, "Login failed. Invalid username and password combination.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private suspend fun handleLoginSuccess(tokenModel: TokenModel, client: OkHttpClient) {
        val tokenService = TokenService(client, this@LoginActivity)
        withContext(Dispatchers.IO) {
            tokenService.storeTokens(tokenModel)
        }

        // Navigate to the DashboardActivity
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
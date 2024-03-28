package com.comp4200.project.papertrader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.comp4200.project.papertrader.models.RegisterModel
import com.comp4200.project.papertrader.services.RegisterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val username = findViewById<EditText>(R.id.usernameEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.confirmPasswordEditText).text.toString()

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val registerModel = RegisterModel(email, username, password, confirmPassword)
            val client = OkHttpClient()
            val registerService = RegisterService(client, this@RegisterActivity)

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    registerService.register(registerModel)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        navigateToLoginActivity()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("RegisterError", "Registration failed: ", e)
                        Toast.makeText(this@RegisterActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
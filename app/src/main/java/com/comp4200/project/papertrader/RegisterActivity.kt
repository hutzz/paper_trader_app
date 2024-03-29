package com.comp4200.project.papertrader

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
                showCustomToast(this, "Passwords do not match")
                return@setOnClickListener
            }

            val registerModel = RegisterModel(email, username, password, confirmPassword)
            val client = OkHttpClient()
            val registerService = RegisterService(client, this@RegisterActivity)

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    registerService.register(registerModel)
                    withContext(Dispatchers.Main) {
                        showCustomToast(this@RegisterActivity, "Registration successful!")
                        navigateToLoginActivity()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("RegisterError", "Registration failed: ", e)
                        showCustomToast(this@RegisterActivity, "Registration failed")
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
    private fun showCustomToast(context: Context, message: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast, null)

        val text = layout.findViewById<TextView>(R.id.toast_text)
        text.text = message

        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }
}
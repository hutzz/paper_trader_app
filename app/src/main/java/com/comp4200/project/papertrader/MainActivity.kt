package com.comp4200.project.papertrader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.comp4200.project.papertrader.services.TokenService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val fadeOut = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out)
            splashScreenView.view.startAnimation(fadeOut)

            fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    splashScreenView.remove()
                    checkLoginStatus()
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
        }
    }
    private fun checkLoginStatus() {
        val tokenService = TokenService(OkHttpClient(), this)
        lifecycleScope.launch {
            if (!isUserLoggedIn(tokenService)) {
                startLoginActivity()
            } else{
                startDashboardActivity()
            }
        }
    }

    private suspend fun checkTokenExpirationMain(tokenService: TokenService): Boolean {
        return withContext(Dispatchers.IO) {
            val accessToken = tokenService.getAccessToken()
            if (accessToken != null && tokenService.isTokenExpired(accessToken)) {
                try {
                    tokenService.refreshTokens()
                    true // Token refreshed successfully
                } catch (e: Exception) {
                    Log.e("main", "Failed to refresh token: $e")
                }
            }
            false // Token is not expired or not available
        }
    }

    private suspend fun isUserLoggedIn(tokenService: TokenService): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                checkTokenExpirationMain(tokenService)
                val accessToken = tokenService.getAccessToken()
                accessToken != null && !tokenService.isTokenExpired(accessToken)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error checking login status: $e")
                false
            }
        }
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun startDashboardActivity() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
package com.comp4200.project.papertrader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.comp4200.project.papertrader.services.StockService
import com.comp4200.project.papertrader.models.StockModel
import com.comp4200.project.papertrader.models.StockDto
import okhttp3.*
import kotlinx.coroutines.*
import java.lang.Exception
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.view.animation.AnimationUtils
import android.view.animation.Animation


class MainActivity : AppCompatActivity() {

    private lateinit var testView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Load the fade-out animation
            val fadeOut = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out)

            // Start the fade-out animation
            splashScreenView.view.startAnimation(fadeOut)

            // Set a listener to remove the splash screen once the animation is done
            fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {
                    // Animation started
                }

                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    // Remove the splash screen once the animation is complete
                    splashScreenView.remove()
                }

                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {
                    // Animation repeats
                }
            })
        }

        setContentView(R.layout.activity_login)


        /*setContentView(R.layout.activity_main)
        testView = findViewById(R.id.testView)
        testView.setOnClickListener {
            val stockService = StockService(client = OkHttpClient())
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val stockDto = StockDto("msft", "1d", "1h")
                    val stockModel = fetchStockData(stockDto)
                    testView.text = stockModel.close[0].toString()
                } catch (e: Exception) {
                    Log.e("error", "lol $e")
                }
            }
        }*/
    }
    /*
    private suspend fun fetchStockData(stockDto: StockDto): StockModel {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val stockService = StockService(client)
            stockService.getStockData(stockDto)
        }
    }*/
}
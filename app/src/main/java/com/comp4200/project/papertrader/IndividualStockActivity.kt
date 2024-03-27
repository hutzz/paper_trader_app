package com.comp4200.project.papertrader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.comp4200.project.papertrader.models.StockDto
import com.comp4200.project.papertrader.models.UserStockModel
import com.comp4200.project.papertrader.services.StockService
import com.comp4200.project.papertrader.services.TokenService
//import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class IndividualStockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_stock_activity)

        lifecycleScope.launch{

            try{
                val tokenService = TokenService(OkHttpClient(), this@IndividualStockActivity)
                val token = tokenService.getAccessToken() ?: throw IllegalStateException("Access token is null")
                var ticker = intent.getStringExtra("STOCK_TICKER") ?: ""
                var dto = StockDto(ticker, "1d", "1d")                 //finish later. Use close price
                var stockService = StockService(OkHttpClient())
                var quantityTextView = findViewById<TextView>(R.id.quantity)
                var priceTextView = findViewById<TextView>(R.id.price)
                var tickerTextView = findViewById<TextView>(R.id.stockTicker)
                var totalValTextView = findViewById<TextView>(R.id.totalValue)
                var backBtn = findViewById<Button>(R.id.backButton)
                var buyBtn = findViewById<Button>(R.id.buyButton)
                var sellBtn = findViewById<Button>(R.id.sellButton)

                var stockData = stockService.getStockData(dto)

                var userStockModel = findStockInList(stockService.getUserStockList(token), ticker)

                quantityTextView.setText("Quantity owned: " + userStockModel.quantity)
                priceTextView.setText("Price per share: $" + stockData.close)
                tickerTextView.setText(ticker)
                totalValTextView.setText("Value owned: $" + userStockModel.quantity * userStockModel.price)


                backBtn.setOnClickListener{
                    val intent = Intent(this@IndividualStockActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                buyBtn.setOnClickListener{

                }

                sellBtn.setOnClickListener{

                }

            }catch(e: Exception){
                Log.e("DashboardError", "Failed to get user details: ", e)
            }
        }
    }

    private fun findStockInList(list: List<UserStockModel>, ticker: String): UserStockModel?{
        var stock: UserStockModel? = null
        list.forEach { userStockModel ->
            if(userStockModel.symbol === ticker){
                stock = userStockModel
                return@forEach
            }
        }

        return stock
    }
}
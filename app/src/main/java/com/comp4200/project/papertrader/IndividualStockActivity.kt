package com.comp4200.project.papertrader

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.comp4200.project.papertrader.models.BuySellDto
import com.comp4200.project.papertrader.models.StockDto
import com.comp4200.project.papertrader.models.StockModel
import com.comp4200.project.papertrader.models.UserStockModel
import com.comp4200.project.papertrader.services.StockService
import com.comp4200.project.papertrader.services.TokenService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.IOException

class IndividualStockActivity : AppCompatActivity() {

    private lateinit var ticker: String
    private val tokenService = TokenService(OkHttpClient(), this@IndividualStockActivity)
    private lateinit var token: String
    private lateinit var dto: StockDto
    private val stockService = StockService(OkHttpClient(), this@IndividualStockActivity)
    private lateinit var quantityTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var tickerTextView: TextView
    private lateinit var totalValTextView: TextView
    private lateinit var backBtn: Button
    private lateinit var buyBtn: Button
    private lateinit var sellBtn: Button
    private lateinit var sellQuantity: EditText
    private lateinit var buyQuantity: EditText
    private lateinit var stockData: StockModel
    private var quantityOwned = 0

    private val MAX_RETRIES = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_stock_activity)

        ticker = intent.getStringExtra("STOCK_TICKER") ?: ""

        quantityTextView = findViewById<TextView>(R.id.quantity)
        priceTextView = findViewById<TextView>(R.id.price)
        tickerTextView = findViewById<TextView>(R.id.stockTicker)
        totalValTextView = findViewById<TextView>(R.id.totalValue)
        backBtn = findViewById<Button>(R.id.backButton)
        buyBtn = findViewById<Button>(R.id.buyButton)
        sellBtn = findViewById<Button>(R.id.sellButton)
        sellQuantity = findViewById<EditText>(R.id.sellQuantity)
        buyQuantity = findViewById<EditText>(R.id.buyQuantity)

        lifecycleScope.launch {
            retryFetchUserDataAndStocks()
        }
    }

    private suspend fun retryFetchUserDataAndStocks() {
        var retryCount = 0
        var success = false
        while (!success && retryCount < MAX_RETRIES) {
            try {
                fetchDataAndStocks()
                success = true
            } catch (e: Exception) {
                Log.e("IndividualStockActivity", "Failed to fetch data and stocks: $e")
                delay(1000L)
                retryCount++
            }
        }
        if (!success) {
            Log.e("IndividualStockActivity", "Failed to fetch data and stocks after $MAX_RETRIES attempts")
        }
    }

    /*private suspend fun fetchDataAndStocks() {
        ticker = intent.getStringExtra("STOCK_TICKER") ?: ""
        token = tokenService.getAccessToken() ?: throw IllegalStateException("Access token is null")
        dto = StockDto(ticker, "1d", "1d")
        stockData = fetchStockData(dto)
        val userStockModel = findStockInList(fetchUserStockList(token), ticker)
        quantityOwned = userStockModel?.quantity ?: 0
        quantityTextView.text = "Quantity owned: $quantityOwned"
        priceTextView.text = "Price per share: $${String.format("%.2f", stockData.close.first())}"
        tickerTextView.text = ticker
        totalValTextView.text = "Value owned: $${String.format("%.2f", (quantityOwned * stockData.close.first()))}"
        setupButtons()
    }*/
    private suspend fun fetchDataAndStocks() {
        ticker = intent.getStringExtra("STOCK_TICKER") ?: ""
        token = tokenService.getAccessToken() ?: throw IllegalStateException("Access token is null")
        dto = StockDto(ticker, "1d", "1d")

        // Fetch specific stock data
        stockData = fetchStockData(dto)

        // Update the UI with stock data
        priceTextView.text = "Price per share: $${String.format("%.2f", stockData.close.first())}"
        tickerTextView.text = ticker

        // Attempt to fetch the user's stock ownership information
        try {
            val userStockModel = findStockInList(fetchUserStockList(token), ticker)
            quantityOwned = userStockModel?.quantity ?: 0
            // Update the UI with user's stock ownership information
            quantityTextView.text = "Quantity owned: $quantityOwned"
            totalValTextView.text = "Value owned: $${String.format("%.2f", (quantityOwned * stockData.close.first()))}"
        } catch (e: Exception) {
            Log.e("IndividualStockActivity", "User may not own this stock or error occurred: $e")
            // Handle case where user does not own the stock or another error occurred
            // You might want to display default or placeholder values or a specific message
            quantityTextView.text = "Quantity owned: 0"
            totalValTextView.text = "Value owned: $0.00"
        }
        setupButtons()
    }

    private fun setupButtons() {
        backBtn.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    tokenService.checkTokenExpiration(tokenService.getAccessToken())
                }
                val intent = Intent(this@IndividualStockActivity, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        buyBtn.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val quant = buyQuantity.text.toString().toInt()
                    val buyDto = BuySellDto(ticker, quant)
                    val resp = buyStock(token, buyDto)
                    if (resp.contains("Successfully purchased")) {
                        updateUI(quant, stockData.close.first())
                    }
                    Toast.makeText(this@IndividualStockActivity, resp, Toast.LENGTH_LONG).show()
                }
                catch (e: Exception) {
                    Log.e("buyerror", e.message.toString())
                    Toast.makeText(this@IndividualStockActivity, "Invalid input", Toast.LENGTH_LONG).show()
                }
            }
        }

        sellBtn.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val quant = sellQuantity.text.toString().toInt()
                    val sellDto = BuySellDto(ticker, quant)
                    val resp = sellStock(token, sellDto)
                    if (resp.contains("Successfully sold")) {
                        updateUI(-quant, stockData.close.first())
                    }
                    Toast.makeText(this@IndividualStockActivity, resp, Toast.LENGTH_LONG).show()
                }
                catch (e: Exception) {
                    Log.e("sellerror", e.message.toString())
                    Toast.makeText(this@IndividualStockActivity, "Invalid input", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun findStockInList(list: List<UserStockModel>, ticker: String): UserStockModel? {
        return list.firstOrNull { it.symbol.lowercase() == ticker.lowercase() }
    }

    private fun updateUI(quantity: Int, price: Float) {
        quantityOwned += quantity
        quantityTextView.text = "Quantity owned: $quantityOwned"
        totalValTextView.text = "Value owned: $${quantityOwned * price}"
    }

    private suspend fun fetchStockData(dto: StockDto): StockModel {
        return withContext(Dispatchers.IO) {
            stockService.getStockData(dto)
        }
    }

    private suspend fun fetchUserStockList(token: String): List<UserStockModel> {
        return withContext(Dispatchers.IO) {
            stockService.getUserStockList(token)
        }
    }

    private suspend fun buyStock(token: String, buyDto: BuySellDto): String {
        return withContext(Dispatchers.IO) {
            stockService.BuyStock(token, buyDto).msg
        }
    }

    private suspend fun sellStock(token: String, sellDto: BuySellDto): String {
        return withContext(Dispatchers.IO) {
            stockService.SellStock(token, sellDto).msg
        }
    }
}

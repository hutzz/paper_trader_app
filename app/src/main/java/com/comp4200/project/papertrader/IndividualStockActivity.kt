package com.comp4200.project.papertrader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.comp4200.project.papertrader.models.BuySellDto
import com.comp4200.project.papertrader.models.StockDto
import com.comp4200.project.papertrader.models.StockModel
import com.comp4200.project.papertrader.models.UserStockModel
import com.comp4200.project.papertrader.services.StockService
import com.comp4200.project.papertrader.services.TokenService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_stock_activity)

        quantityTextView = findViewById<TextView>(R.id.quantity)
        priceTextView = findViewById<TextView>(R.id.price)
        tickerTextView = findViewById<TextView>(R.id.stockTicker)
        totalValTextView = findViewById<TextView>(R.id.totalValue)
        backBtn = findViewById<Button>(R.id.backButton)
        buyBtn = findViewById<Button>(R.id.buyButton)
        sellBtn = findViewById<Button>(R.id.sellButton)
        sellQuantity = findViewById<EditText>(R.id.sellQuantity)
        buyQuantity = findViewById<EditText>(R.id.buyQuantity)

        lifecycleScope.launch{

            try{
                ticker = intent.getStringExtra("STOCK_TICKER") ?: ""
                this@IndividualStockActivity.token = tokenService.getAccessToken() ?: throw IllegalStateException("Access token is null")
                this@IndividualStockActivity.dto = StockDto(ticker, "1d", "1d")
                this@IndividualStockActivity.stockData = fetchStockData(dto)                                        //API CALL
                var userStockModel = findStockInList(fetchUserStockList(token), ticker)                             //API CALL
                this@IndividualStockActivity.quantityOwned = userStockModel?.quantity ?: 0
                quantityTextView.setText("Quantity owned: " + this@IndividualStockActivity.quantityOwned)
                priceTextView.setText("Price per share: $" + stockData.close.first())
                tickerTextView.setText(ticker)
                totalValTextView.setText("Value owned: $" + quantityOwned * stockData.close.first())

                backBtn.setOnClickListener{
                    val intent = Intent(this@IndividualStockActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                buyBtn.setOnClickListener{
                    lifecycleScope.launch {
                        val quant = buyQuantity.text.toString().toInt()
                        val buyDto = BuySellDto(ticker, quant)
                        val resp = buyStock(token, buyDto)
                        if(resp.contains("Successfully purchased")){
                            updateUI(quant, stockData.close.first())
                        }

                        Toast.makeText(this@IndividualStockActivity, resp, Toast.LENGTH_LONG).show()
                    }
                }

                sellBtn.setOnClickListener{
                    lifecycleScope.launch {
                        val quant = sellQuantity.text.toString().toInt()
                        val sellDto = BuySellDto(ticker, quant)
                        val resp = sellStock(token, sellDto)
                        if(resp.contains("Successfully sold")){
                            updateUI(quant, stockData.close.first())
                        }

                        Toast.makeText(this@IndividualStockActivity, resp, Toast.LENGTH_LONG).show()
                    }
                }

            }catch(e: Exception){
                Log.e("DashboardError", "Failed to get user details: ", e)
            }
        }
    }

    private fun findStockInList(list: List<UserStockModel>, ticker: String): UserStockModel?{
        Log.i("TICKER VALUE:", ticker)
        Log.i("STOCK LIST", list.toString())

        var stock: UserStockModel? = null
        list.forEach { userStockModel ->
            if(userStockModel.symbol.lowercase() === ticker.lowercase()){
                stock = userStockModel
                return@forEach
            }
        }

        return stock
    }


    private fun updateUI(quantity: Int, price: Float){
        var quantityTextView = findViewById<TextView>(R.id.quantity)
        var totalValTextView = findViewById<TextView>(R.id.totalValue)
        this@IndividualStockActivity.quantityOwned += quantity

        quantityTextView.setText("Quantity owned: " + this@IndividualStockActivity.quantityOwned)
        totalValTextView.setText("Value owned: $" + this@IndividualStockActivity.quantityOwned * price)
    }

    private suspend fun fetchStockData(dto: StockDto): StockModel{
        return withContext(Dispatchers.IO) {
            stockService.getStockData(dto)
        }
    }

    private suspend fun fetchUserStockList(token: String): List<UserStockModel>{
        return withContext(Dispatchers.IO){
            stockService.getUserStockList(token)
        }
    }

    private suspend fun buyStock(token: String, buyDto: BuySellDto): String{
        return withContext(Dispatchers.IO){
            stockService.BuyStock(token, buyDto).msg
        }
    }

    private suspend fun sellStock(token: String, sellDto: BuySellDto): String{
        return withContext(Dispatchers.IO){
            stockService.SellStock(token, sellDto).msg
        }
    }
}
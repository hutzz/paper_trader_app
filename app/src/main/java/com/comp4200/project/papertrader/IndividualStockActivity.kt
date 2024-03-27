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
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class IndividualStockActivity : AppCompatActivity() {

    private val ticker = intent.getStringExtra("STOCK_TICKER") ?: ""
    private val tokenService = TokenService(OkHttpClient(), this@IndividualStockActivity)
    private lateinit var token: String
    private lateinit var dto: StockDto
    private val stockService = StockService(OkHttpClient(), this@IndividualStockActivity)
    private var quantityTextView = findViewById<TextView>(R.id.quantity)
    private var priceTextView = findViewById<TextView>(R.id.price)
    private var tickerTextView = findViewById<TextView>(R.id.stockTicker)
    private var totalValTextView = findViewById<TextView>(R.id.totalValue)
    private var backBtn = findViewById<Button>(R.id.backButton)
    private var buyBtn = findViewById<Button>(R.id.buyButton)
    private var sellBtn = findViewById<Button>(R.id.sellButton)
    private var sellQuantity = findViewById<EditText>(R.id.sellQuantity)
    private var buyQuantity = findViewById<EditText>(R.id.buyQuantity)
    private lateinit var stockData: StockModel
    private var quantityOwned = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_stock_activity)

        lifecycleScope.launch{

            try{
                this@IndividualStockActivity.token = tokenService.getAccessToken() ?: throw IllegalStateException("Access token is null")
                this@IndividualStockActivity.dto = StockDto(ticker, "1d", "1d")                 //finish later. Use close price
                this@IndividualStockActivity.stockData = stockService.getStockData(dto)
                var userStockModel = findStockInList(stockService.getUserStockList(token), ticker)
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
                        var quant = buyQuantity.text.toString().toInt()
                        var buyDto = BuySellDto(ticker, quant)
                        val resp = stockService.BuyStock(token, buyDto).msg
                        if(resp.contains("Successfully purchased")){
                            updateUI(quant, stockData.close.first())
                        }

                        Toast.makeText(this@IndividualStockActivity, resp, Toast.LENGTH_LONG).show()
                    }
                }

                sellBtn.setOnClickListener{
                    lifecycleScope.launch {
                        var quant = sellQuantity.text.toString().toInt()
                        var sellDto = BuySellDto(ticker, quant)
                        val resp = stockService.SellStock(token, sellDto).msg
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
        var stock: UserStockModel? = null
        list.forEach { userStockModel ->
            if(userStockModel.symbol === ticker){
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
}
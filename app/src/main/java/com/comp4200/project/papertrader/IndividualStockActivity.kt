package com.comp4200.project.papertrader

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.comp4200.project.papertrader.models.StockDto
import com.comp4200.project.papertrader.services.StockService
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class IndividualStockActivity : AppCompatActivity() {

    private var stockService = StockService(OkHttpClient())
    private lateinit var token: String
    private lateinit var ticker: String
    private lateinit var dto: StockDto
    private var quantityOwned: Int = 0 // Initialize with default value
    private var stockPrice: Double = 0.0 // Initialize with default value

    private lateinit var quantityTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var tickerTextView: TextView
    private lateinit var totalValTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_stock_activity)

        this.quantityTextView = findViewById<TextView>(R.id.quantity)
        this.priceTextView = findViewById<TextView>(R.id.price)
        this.tickerTextView = findViewById<TextView>(R.id.stockTicker)
        this.totalValTextView = findViewById<TextView>(R.id.totalValue)

        this.ticker = intent.getStringExtra("STOCK_TICKER") ?: ""   //Needs to be passed when starting activity

        //initialize dto
        this.dto = StockDto("", "", "") //finish later

        var stockData = this.stockService.getStockData(dto)





    }






}
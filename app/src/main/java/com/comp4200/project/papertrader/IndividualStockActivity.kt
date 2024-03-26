package com.comp4200.project.papertrader

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
//import com.squareup.picasso.Picasso
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response

class IndividualStockActivity : AppCompatActivity() {

    private lateinit var token: String
    private var quantityOwned: Int = 0 // Initialize with default value
    private var stockPrice: Double = 0.0 // Initialize with default value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_stock_activity)

        val stockTicker = intent.getStringExtra("STOCK_TICKER") ?: ""   //Needs to be passed when starting activity


//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://api.example.com/") // Replace with actual API base URL
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val service = retrofit.create(StockService::class.java)
//        val call = service.getStockData(token, symbol) // Pass token to API call
//
//        call.enqueue(object : Callback<StockData> {
//            override fun onResponse(call: Call<StockData>, response: Response<StockData>) {
//                if (response.isSuccessful) {
//                    val stockData = response.body()
//
//                    // Update UI with stock data
//                    updateUI(stockData)
//                } else {
//                    // Handle unsuccessful response
//                }
//            }
//
//            override fun onFailure(call: Call<StockData>, t: Throwable) {
//                // Handle failure
//            }
//        })
    }






}
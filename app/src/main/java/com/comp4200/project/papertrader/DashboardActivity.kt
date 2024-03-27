package com.comp4200.project.papertrader

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.comp4200.project.papertrader.models.LoginModel
import com.comp4200.project.papertrader.models.UserModel
import com.comp4200.project.papertrader.services.TokenService
import com.comp4200.project.papertrader.services.UserService
import com.comp4200.project.papertrader.services.StockService
import com.comp4200.project.papertrader.models.UserStockModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class DashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockAdapter
    private lateinit var usernameText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        usernameText = findViewById<TextView>(R.id.username)
        recyclerView = findViewById(R.id.stocksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val userService = UserService(client)
                val tokenService = TokenService(client, this@DashboardActivity)
                val user = getUserData(userService, tokenService)
                val accessToken = tokenService.getAccessToken()
                if (accessToken != null) {
                    val stockService = StockService(client)
                    val userStocks = stockService.getUserStockList(accessToken)
                    withContext(Dispatchers.Main) {
                        adapter = StockAdapter(userStocks)
                        recyclerView.adapter = adapter
                        usernameText.text = user.username
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DashboardError", "Failed to get user stocks: ", e)
                }
            }
        }
    }
    private suspend fun getUserData(userService: UserService, tokenService: TokenService): UserModel {
        return withContext(Dispatchers.IO) {
            val token = tokenService.getAccessToken() ?: throw IllegalStateException("Access token is null")
            userService.getUserData(token)
        }
    }
    inner class StockAdapter(private val stockList: List<UserStockModel>) :
        RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

        // Inner class for the RecyclerView view holder
        inner class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textViewSymbol: TextView = view.findViewById(R.id.textViewSymbol)
            // Initialize other views here...
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
            return StockViewHolder(view)
        }

        override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
            val stock = stockList[position]
            holder.textViewSymbol.text = stock.symbol
            // Bind other data...
        }

        override fun getItemCount() = stockList.size
    }
}



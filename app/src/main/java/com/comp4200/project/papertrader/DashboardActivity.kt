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
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.comp4200.project.papertrader.models.UserModel
import com.comp4200.project.papertrader.services.TokenService
import com.comp4200.project.papertrader.services.UserService
import com.comp4200.project.papertrader.services.StockService
import com.comp4200.project.papertrader.models.UserStockModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.IOException

class DashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockAdapter
    private lateinit var usernameText: TextView
    private lateinit var balanceText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        supportActionBar?.hide()

        usernameText = findViewById<TextView>(R.id.username)
        balanceText = findViewById<TextView>(R.id.balance)
        recyclerView = findViewById(R.id.stocksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = StockAdapter(emptyList())
        recyclerView.adapter = adapter

        // Fetch and set actual data
        fetchUserDataAndStocks()

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            logoutUser()
        }
    }
    private fun fetchUserDataAndStocks() {
        lifecycleScope.launch(Dispatchers.IO) {
            var retryCount = 0
            val maxRetries = 3
            var success = false
            while (!success && retryCount < maxRetries) {
                try {
                    val client = OkHttpClient()
                    val userService = UserService(client, this@DashboardActivity)
                    val tokenService = TokenService(client, this@DashboardActivity)
                    val user = getUserData(userService, tokenService)
                    usernameText.text = user.username
                    balanceText.text = "$" + user.balance.toString()
                    val accessToken = tokenService.getAccessToken()
                    if (accessToken != null) {
                        val stockService = StockService(client, this@DashboardActivity)
                        val userStocks = stockService.getUserStockList(accessToken)
                        withContext(Dispatchers.Main) {
                            adapter.updateData(userStocks)
                        }
                        success = true
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        if (e.message == "User has no stocks!") {
                            Toast.makeText(this@DashboardActivity, "You currently have no stocks.", Toast.LENGTH_LONG).show()
                        } else {
                            Log.e("DashboardError", "Failed to get user stocks: ", e)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("DashboardError", "An unexpected error occurred: ", e)
                    }
                }
                retryCount++
            }
            if (!success) {
                Log.e("DashboardError", "Failed to fetch user data and stocks after $maxRetries attempts")
            }
        }
    }
    private suspend fun getUserData(userService: UserService, tokenService: TokenService): UserModel {
        return withContext(Dispatchers.IO) {
            val token = tokenService.getAccessToken() ?: throw IllegalStateException("Access token is null")
            userService.getUserData(token)
        }
    }
    inner class StockAdapter(private var stockList: List<UserStockModel>) :
        RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

        fun updateData(newStockList: List<UserStockModel>) {
            stockList = newStockList
            notifyDataSetChanged()
        }
        inner class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textViewSymbol: TextView = view.findViewById(R.id.textViewSymbol)
            val textViewPrice: TextView = view.findViewById(R.id.textViewPrice)
            val textViewQuantity: TextView = view.findViewById(R.id.textViewQuantity)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
            return StockViewHolder(view)
        }

        override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
            val stock = stockList[position]
            holder.textViewSymbol.text = stock.symbol
            holder.textViewPrice.text = String.format("$%.2f", stock.price)
            holder.textViewQuantity.text = stock.quantity.toString()

            holder.itemView.setOnClickListener{
                val intent = Intent(holder.itemView.context, IndividualStockActivity::class.java)
                intent.putExtra("STOCK_TICKER", stock.symbol)
                startActivity(intent)
                finish()
            }

        }

        override fun getItemCount() = stockList.size
    }
    private fun logoutUser() {
        lifecycleScope.launch {
            try {
                // Correct instantiation of TokenService with OkHttpClient and Context
                val tokenService = TokenService(OkHttpClient(), applicationContext)
                tokenService.deleteTokens()  // Correctly called without parameters

                // Navigate to Login Activity
                val intent = Intent(this@DashboardActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("LogoutError", "Logout failed: ${e.message}")
                // Optionally, show an error message to the user
            }
        }
    }
}



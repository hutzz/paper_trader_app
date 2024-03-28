package com.comp4200.project.papertrader.services

import android.content.Context
import com.comp4200.project.papertrader.models.BuySellDto
import com.comp4200.project.papertrader.models.MessageModel
import okhttp3.OkHttpClient
import com.comp4200.project.papertrader.models.StockModel
import com.comp4200.project.papertrader.models.StockDto
import com.comp4200.project.papertrader.models.UserStockModel
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class StockService(client: OkHttpClient, private val context: Context) : ServiceBase(client, context) {
    suspend fun getStockData(stockDto: StockDto): StockModel {
        val url = createUrl("/stock/${stockDto.symbol}")
        return postJson(url, stockDto, StockModel::class.java)
    }
    suspend fun getUserStockList(token: String): List<UserStockModel> {
        val url = createUrl("/stock/get")
        // Explicitly specifying the type as a list of UserStockModel
        val type = object : TypeToken<List<UserStockModel>>() {}.type
        return getJson(url, type, token)
    }
    suspend fun BuyStock(token: String, stock: BuySellDto): MessageModel {
        val url = createUrl("/stock/buy")
        return postJson(url, stock, MessageModel::class.java, token)
    }
    suspend fun SellStock(token: String, stock: BuySellDto): MessageModel {
        val url = createUrl("/stock/sell")
        return postJson(url, stock, MessageModel::class.java, token)
    }
}
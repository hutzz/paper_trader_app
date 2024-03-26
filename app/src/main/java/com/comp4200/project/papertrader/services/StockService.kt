package com.comp4200.project.papertrader.services

import com.comp4200.project.papertrader.models.MessageModel
import okhttp3.OkHttpClient
import com.comp4200.project.papertrader.models.StockModel
import com.comp4200.project.papertrader.models.StockDto
import com.comp4200.project.papertrader.models.UserStockModel
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class StockService(client: OkHttpClient) : ServiceBase(client) {
    suspend fun getStockData(stockDto: StockDto): StockModel {
        val url = createUrl("/stock/${stockDto.symbol}")
        return postJson(url, stockDto, StockModel::class.java)
    }
    suspend fun getUserStockList(token: String): List<UserStockModel> {
        val url = createUrl("/stock/get")
        val listType: Type = object : TypeToken<List<UserStockModel>>() {}.type
        return getJson(url, listType, token)
    }
//    suspend fun BuyStock(token: String, stockInfo: UserStockModel): MessageModel {
//
//    }
}
package com.comp4200.project.papertrader.services

import okhttp3.OkHttpClient
import com.comp4200.project.papertrader.models.StockModel
import com.comp4200.project.papertrader.models.StockDto

class StockService(client: OkHttpClient) : ServiceBase(client) {
    suspend fun getStockData(stockDto: StockDto): StockModel {
        val url = createUrl("/stock/${stockDto.symbol}")
        return postJson(url, stockDto, StockModel::class.java)
    }
}
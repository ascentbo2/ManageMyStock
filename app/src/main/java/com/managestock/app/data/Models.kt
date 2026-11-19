package com.managestock.app.data

data class Stock(
    val symbol: String,
    val name: String,
    val price: Double,
    val prevClose: Double,
    val high52: Double,
    val low52: Double,
    val volume: Long,
    val pe: Double,
    val mcapCr: Double
) {
    val change: Double get() = price - prevClose
    val changePct: Double get() = if (prevClose == 0.0) 0.0 else change / prevClose * 100
}

data class IndexQuote(
    val name: String,
    val value: Double,
    val prevClose: Double,
    val spark: List<Float>
) {
    val change: Double get() = value - prevClose
    val changePct: Double get() = if (prevClose == 0.0) 0.0 else change / prevClose * 100
}

data class NewsItem(val title: String, val source: String, val time: String, val category: String)

data class Idea(
    val symbol: String,
    val title: String,
    val action: String,
    val entry: Double,
    val target: Double,
    val stopLoss: Double,
    val status: String
)

data class Holding(val symbol: String, val qty: Int, val avgPrice: Double)

data class MutualFund(val name: String, val invested: Double, val current: Double)

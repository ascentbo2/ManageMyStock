package com.managestock.app.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Single seam for data. To go live, write a second implementation
 * (e.g. MotilalMarketRepository using the broker's REST/WebSocket API)
 * and swap it in MarketViewModel. The UI does not change.
 */
interface MarketRepository {
    val stocks: StateFlow<List<Stock>>
    val indices: StateFlow<List<IndexQuote>>
    val news: List<NewsItem>
    val ideas: List<Idea>
    val holdings: List<Holding>
    val funds: List<MutualFund>
    fun history(symbol: String, range: String): List<Float>
}

class MockMarketRepository(scope: CoroutineScope) : MarketRepository {
    private val _stocks = MutableStateFlow(MockData.stocks)
    private val _indices = MutableStateFlow(MockData.indices)

    override val stocks: StateFlow<List<Stock>> = _stocks
    override val indices: StateFlow<List<IndexQuote>> = _indices
    override val news = MockData.news
    override val ideas = MockData.ideas
    override val holdings = MockData.holdings
    override val funds = MockData.funds

    init {
        // Simulated tick every 3 seconds
        scope.launch {
            while (true) {
                delay(3000)
                _stocks.value = _stocks.value.map { s ->
                    val np = Math.round(s.price * (1 + Random.nextDouble(-0.002, 0.002)) * 100) / 100.0
                    s.copy(price = np)
                }
                _indices.value = _indices.value.map { i ->
                    val nv = i.value * (1 + Random.nextDouble(-0.0007, 0.0007))
                    i.copy(value = nv, spark = i.spark.drop(1) + nv.toFloat())
                }
            }
        }
    }

    override fun history(symbol: String, range: String): List<Float> {
        val n = when (range) { "1D" -> 60; "1W" -> 70; "1M" -> 90; "1Y" -> 120; else -> 150 }
        val vol = when (range) { "1D" -> 0.0015; "1W" -> 0.004; "1M" -> 0.008; "1Y" -> 0.02; else -> 0.03 }
        val current = _stocks.value.firstOrNull { it.symbol == symbol }?.price ?: 100.0
        val rnd = Random((symbol + range).hashCode())
        val out = ArrayList<Float>(n)
        var p = current
        repeat(n) {
            out.add(p.toFloat())
            p /= (1 + rnd.nextDouble(-vol, vol))
        }
        return out.reversed()
    }
}

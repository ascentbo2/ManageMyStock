package com.managestock.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MarketViewModel : ViewModel() {
    private val repo: MarketRepository = MockMarketRepository(viewModelScope)

    val stocks = repo.stocks
    val indices = repo.indices
    val news = repo.news
    val ideas = repo.ideas
    val holdings = repo.holdings
    val funds = repo.funds

    private val _watchlist = MutableStateFlow(setOf("RELIANCE", "TCS", "HDFCBANK", "INFY"))
    val watchlist: StateFlow<Set<String>> = _watchlist

    fun toggleWatch(symbol: String) {
        _watchlist.value =
            if (symbol in _watchlist.value) _watchlist.value - symbol else _watchlist.value + symbol
    }

    fun history(symbol: String, range: String) = repo.history(symbol, range)
}

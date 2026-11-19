package com.managestock.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managestock.app.data.IndexQuote
import com.managestock.app.data.NewsItem
import com.managestock.app.data.Stock
import com.managestock.app.ui.theme.Gain
import com.managestock.app.ui.theme.Loss

@Composable
fun HomeScreen(
    stocks: List<Stock>,
    indices: List<IndexQuote>,
    news: List<NewsItem>,
    watchlist: Set<String>,
    marketOpen: Boolean,
    onStockClick: (Stock) -> Unit,
    onSeeNews: () -> Unit
) {
    val filters = listOf("Watchlist", "Gainers", "Losers", "52W High", "Most Active")
    var selected by remember { mutableStateOf("Watchlist") }

    val list = when (selected) {
        "Watchlist" -> stocks.filter { it.symbol in watchlist }
        "Gainers" -> stocks.filter { it.changePct > 0 }.sortedByDescending { it.changePct }
        "Losers" -> stocks.filter { it.changePct < 0 }.sortedBy { it.changePct }
        "52W High" -> stocks.filter { it.price >= it.high52 * 0.97 }
        else -> stocks.sortedByDescending { it.volume }.take(10)
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item { MarketStatus(marketOpen) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) { items(indices, key = { it.name }) { IndexCard(it) } }
        }
        item {
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { f ->
                    FilterChip(
                        selected = selected == f,
                        onClick = { selected = f },
                        label = { Text(f) }
                    )
                }
            }
        }
        if (list.isEmpty()) {
            item {
                Text(
                    "No stocks to show here yet.",
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(list, key = { it.symbol }) { StockRow(it) { onStockClick(it) } }
        item {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Top news", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onSeeNews) { Text("See all") }
            }
        }
        items(news.take(2)) { NewsCard(it) }
    }
}

@Composable
private fun MarketStatus(open: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(8.dp).background(if (open) Gain else Loss, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (open) "Market open" else "Market closed",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IndexCard(i: IndexQuote) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.width(170.dp).padding(12.dp)) {
            Text(i.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(i.value), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            ChangeText(i.change, i.changePct)
            Spacer(Modifier.height(6.dp))
            LineChart(
                values = i.spark,
                color = changeColor(i.change),
                modifier = Modifier.fillMaxWidth().height(32.dp)
            )
        }
    }
}

@Composable
fun StockRow(stock: Stock, onClick: () -> Unit) {
    var last by remember { mutableStateOf(stock.price) }
    var up by remember { mutableStateOf(true) }
    val flash = remember { Animatable(0f) }

    LaunchedEffect(stock.price) {
        if (stock.price != last) {
            up = stock.price > last
            last = stock.price
            flash.snapTo(0.3f)
            flash.animateTo(0f, tween(800))
        }
    }

    Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background((if (up) Gain else Loss).copy(alpha = flash.value))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(stock.symbol, fontWeight = FontWeight.Bold)
                Text(stock.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(money(stock.price), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                PricePill(stock.changePct)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

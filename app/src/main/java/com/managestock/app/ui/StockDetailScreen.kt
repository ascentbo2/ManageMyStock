package com.managestock.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managestock.app.data.NewsItem
import com.managestock.app.data.Stock
import com.managestock.app.ui.theme.Gain
import com.managestock.app.ui.theme.Loss

private fun rsi(v: List<Float>, period: Int = 14): Double {
    if (v.size <= period) return 50.0
    var gain = 0.0
    var loss = 0.0
    for (i in v.size - period until v.size) {
        val d = (v[i] - v[i - 1]).toDouble()
        if (d >= 0) gain += d else loss -= d
    }
    if (loss == 0.0) return 100.0
    return 100 - 100 / (1 + gain / loss)
}

private fun sma(v: List<Float>, n: Int): Double? =
    if (v.size < n) null else v.takeLast(n).average()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    stock: Stock,
    history: (String) -> List<Float>,
    news: List<NewsItem>,
    watched: Boolean,
    onToggleWatch: () -> Unit,
    onBack: () -> Unit
) {
    val ranges = listOf("1D", "1W", "1M", "1Y", "5Y")
    var range by remember { mutableStateOf("1D") }
    val base = remember(stock.symbol, range) { history(range) }
    val series = base.dropLast(1) + stock.price.toFloat()
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Technicals", "Fundamentals", "News")
    val ctx = LocalContext.current
    val orderMsg = "Order placement will connect to your broker API"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stock.symbol, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = onToggleWatch) {
                        Icon(if (watched) Icons.Filled.Star else Icons.Filled.StarBorder, "Watchlist")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { Toast.makeText(ctx, orderMsg, Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Gain)
                    ) { Text("BUY") }
                    Button(
                        onClick = { Toast.makeText(ctx, orderMsg, Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Loss)
                    ) { Text("SELL") }
                }
            }
        }
    ) { inner ->
        Column(Modifier.padding(inner).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(16.dp)) {
                Text(stock.name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Text(money(stock.price), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                ChangeText(stock.change, stock.changePct)
            }
            LineChart(
                values = series,
                color = changeColor(stock.change),
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ranges) { r ->
                    FilterChip(selected = range == r, onClick = { range = r }, label = { Text(r) })
                }
            }
            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t, fontSize = 12.sp, maxLines = 1) })
                }
            }
            Column(Modifier.padding(16.dp)) {
                when (tab) {
                    0 -> {
                        StatRow("Previous close", money(stock.prevClose))
                        StatRow("52W high", money(stock.high52))
                        StatRow("52W low", money(stock.low52))
                        StatRow("Volume", String.format("%,d", stock.volume))
                    }
                    1 -> {
                        val s20 = sma(base, 20)
                        val s50 = sma(base, 50)
                        val r = rsi(base)
                        StatRow("SMA 20", s20?.let { money(it) } ?: "-")
                        StatRow("SMA 50", s50?.let { money(it) } ?: "-")
                        StatRow("RSI (14)", String.format("%.1f", r))
                        val trend = if (s20 != null && stock.price > s20) "Bullish" else "Bearish"
                        val mom = when {
                            r > 70 -> "Overbought"
                            r < 30 -> "Oversold"
                            else -> "Neutral"
                        }
                        StatRow("Trend", trend, if (trend == "Bullish") Gain else Loss)
                        StatRow("Momentum", mom)
                    }
                    2 -> {
                        StatRow("P/E", String.format("%.1f", stock.pe))
                        StatRow("Market cap (Rs Cr)", String.format("%,.0f", stock.mcapCr))
                        Text(
                            "Sample values. Real fundamentals arrive with the data feed.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    else -> news.take(3).forEach { NewsCard(it, horizontalPadding = 0.dp) }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

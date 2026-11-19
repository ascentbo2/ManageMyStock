package com.managestock.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managestock.app.data.Holding
import com.managestock.app.data.MutualFund
import com.managestock.app.data.Stock

@Composable
fun PortfolioScreen(
    holdings: List<Holding>,
    funds: List<MutualFund>,
    stocks: List<Stock>,
    onStockClick: (Stock) -> Unit
) {
    val priced = holdings.mapNotNull { h -> stocks.firstOrNull { it.symbol == h.symbol }?.let { h to it } }
    val invested = priced.sumOf { (h, _) -> h.qty * h.avgPrice }
    val current = priced.sumOf { (h, s) -> h.qty * s.price }
    val dayPnl = priced.sumOf { (h, s) -> h.qty * s.change }
    val totalPnl = current - invested

    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Current value", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Rs ${money(current)}", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    SummaryLine("Invested", "Rs ${money(invested)}")
                    SummaryLine("Total P&L", signed(totalPnl), changeColor(totalPnl))
                    SummaryLine("Day's P&L", signed(dayPnl), changeColor(dayPnl))
                }
            }
        }
        item { SectionTitle("Holdings") }
        items(priced, key = { it.first.symbol }) { (h, s) ->
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row {
                    Text(h.symbol, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(money(s.price * h.qty), fontWeight = FontWeight.SemiBold)
                }
                Row {
                    Text(
                        "${h.qty} @ ${money(h.avgPrice)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    val pnl = (s.price - h.avgPrice) * h.qty
                    Text(signed(pnl), fontSize = 12.sp, color = changeColor(pnl))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
        item { SectionTitle("Mutual funds") }
        items(funds) { f ->
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(f.name, fontWeight = FontWeight.SemiBold)
                Row {
                    Text(
                        "Invested ${money(f.invested)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    val gain = f.current - f.invested
                    Text("${money(f.current)}  ${signed(gain)}", fontSize = 12.sp, color = changeColor(gain))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp))
}

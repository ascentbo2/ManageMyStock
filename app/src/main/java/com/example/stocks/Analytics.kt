package com.example.stocks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PALETTE = listOf(Color(0xFFFF7A00), Color(0xFF2ECC71), Color(0xFF3498DB), Color(0xFFE91E63), Color(0xFF9B59B6), Color(0xFFF1C40F), Color(0xFF1ABC9C), Color(0xFF95A5A6))

/** Annualised return from dated cash flows (negative = money in, positive = value now). Null when it cannot be solved. */
fun xirr(flows: List<Pair<Long, Double>>): Double? {
    if (flows.size < 2 || flows.none { it.second < 0 } || flows.none { it.second > 0 }) return null
    val t0 = flows.minOf { it.first }
    fun npv(r: Double) = flows.sumOf { (t, v) -> v / Math.pow(1 + r, (t - t0) / 86_400_000.0 / 365.0) }
    var lo = -0.99; var hi = 100.0
    if (npv(lo) * npv(hi) > 0) return null
    repeat(200) { val mid = (lo + hi) / 2; if (npv(lo) * npv(mid) <= 0) hi = mid else lo = mid }
    return (lo + hi) / 2
}

@Composable
fun Donut(parts: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val total = parts.sumOf { it.second }
    if (total <= 0) return
    Canvas(modifier) {
        val stroke = size.minDimension * 0.22f; var start = -90f
        parts.forEachIndexed { i, p ->
            val sweep = (p.second / total * 360).toFloat()
            drawArc(PALETTE[i % PALETTE.size], start, maxOf(sweep - 1.5f, 0.5f), false,
                topLeft = Offset(stroke / 2, stroke / 2), size = Size(size.width - stroke, size.height - stroke), style = Stroke(stroke))
            start += sweep
        }
    }
}

@Composable
fun PortfolioAnalytics(vm: VM, rows: List<PRow>) {
    var bySector by remember { mutableStateOf(false) }
    LaunchedEffect(rows.map { it.sym }) { vm.loadSectors(rows.mapNotNull { it.sym }) }
    val stocks = rows.filter { it.sym != null }
    val dayPnl = stocks.sumOf { (vm.quotes[it.sym]?.change ?: 0.0) * it.qty }
    val prevVal = stocks.sumOf { (it.price - (vm.quotes[it.sym]?.change ?: 0.0)) * it.qty }
    val dated = rows.filter { it.date > 0 }
    val flows = dated.map { it.date to -(it.qty * it.rate) } + listOf(System.currentTimeMillis() to dated.sumOf { it.qty * it.price })
    val first = dated.minOfOrNull { it.date }
    val x = if (first != null && System.currentTimeMillis() - first > 30L * 86_400_000L) xirr(flows) else null
    val grouped = (if (bySector) rows.groupBy { r -> if (r.sym == null) "Mutual funds" else vm.sectors[r.sym] ?: "Other" } else rows.groupBy { it.title })
        .map { (k, v) -> k to v.sumOf { it.qty * it.price } }.filter { it.second > 0 }.sortedByDescending { it.second }
    val rest = grouped.drop(7).sumOf { it.second }
    val shown = grouped.take(7) + (if (rest > 0) listOf("Others" to rest) else emptyList())
    val total = shown.sumOf { it.second }
    Card(Modifier.fillMaxWidth().padding(16.dp, 0.dp, 16.dp, 8.dp)) { Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Today (stocks)")
            Text("${sign(dayPnl)} (${if (prevVal > 0) sign(dayPnl / prevVal * 100) else "0.00"}%)", color = col(dayPnl), fontWeight = FontWeight.SemiBold)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Return per year (XIRR)")
            Text(x?.let { sign(it * 100) + "%" } ?: "n/a", color = x?.let { col(it) } ?: Color.Gray, fontWeight = FontWeight.SemiBold)
        }
        if (x == null) Text("XIRR needs purchase dates and at least 30 days of holding.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(!bySector, { bySector = false }, { Text("By holding") })
            FilterChip(bySector, { bySector = true }, { Text("By sector") })
        }
        if (shown.isNotEmpty()) Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Donut(shown, Modifier.size(130.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                shown.forEachIndexed { i, (name, v) ->
                    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("● ", color = PALETTE[i % PALETTE.size], fontSize = 12.sp)
                        Text(name, Modifier.weight(1f), fontSize = 11.sp, maxLines = 1)
                        Text("%.0f%%".format(v / total * 100), fontSize = 11.sp)
                    }
                }
            }
        }
    } }
}

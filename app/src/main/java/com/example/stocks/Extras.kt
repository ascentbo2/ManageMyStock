package com.example.stocks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs

val SECTORS = listOf("Bank" to "^NSEBANK", "IT" to "^CNXIT", "Auto" to "^CNXAUTO", "Pharma" to "^CNXPHARMA", "FMCG" to "^CNXFMCG",
    "Metal" to "^CNXMETAL", "Energy" to "^CNXENERGY", "Realty" to "^CNXREALTY", "Fin Services" to "^CNXFIN", "Media" to "^CNXMEDIA",
    "PSU Bank" to "^CNXPSUBANK", "Infra" to "^CNXINFRA", "PSE" to "^CNXPSE")

val GLOBAL = listOf(
    "World indices" to listOf("^DJI" to "Dow Jones", "^IXIC" to "Nasdaq", "^GSPC" to "S&P 500", "^N225" to "Nikkei 225", "^HSI" to "Hang Seng", "^FTSE" to "FTSE 100", "^GDAXI" to "DAX"),
    "Commodities" to listOf("GC=F" to "Gold (USD/oz)", "SI=F" to "Silver (USD/oz)", "CL=F" to "Crude Oil WTI", "BZ=F" to "Brent Crude", "NG=F" to "Natural Gas", "HG=F" to "Copper"),
    "Currencies" to listOf("INR=X" to "USD / INR", "EURINR=X" to "EUR / INR", "GBPINR=X" to "GBP / INR", "JPYINR=X" to "JPY / INR", "DX-Y.NYB" to "US Dollar Index"))

@Composable
fun GlobalPane(vm: VM, open: (String) -> Unit) {
    val all = GLOBAL.flatMap { g -> g.second.map { it.first } }
    LaunchedEffect(Unit) { while (true) { vm.loadExtra(all).join(); delay(15_000) } }
    LazyColumn {
        GLOBAL.forEach { (title, list) ->
            item { Text(title, Modifier.padding(10.dp, 10.dp, 10.dp, 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            items(list) { (s, n) -> IndexRow(vm.quotes[s], n) { open(s) } }
        }
    }
}

@Composable
fun SectorPane(vm: VM, open: (String) -> Unit) {
    LaunchedEffect(Unit) { while (true) { vm.loadExtra(SECTORS.map { it.second }).join(); delay(10_000) } }
    val rows = SECTORS.map { (n, s) -> Triple(n, s, vm.quotes[s]) }.sortedByDescending { it.third?.pct ?: -999.0 }
    Column(Modifier.fillMaxSize()) {
        Text("Sector indices, today's move", Modifier.padding(10.dp, 8.dp), fontSize = 11.sp, color = Color.Gray)
        LazyVerticalGrid(GridCells.Fixed(2), Modifier.weight(1f), contentPadding = PaddingValues(4.dp)) {
            items(rows) { (n, s, q) ->
                val bg = if (q == null) Color(0xFF2A2A2A)
                         else (if (q.pct >= 0) Green else Red).copy(alpha = 0.25f + 0.6f * minOf(abs(q.pct) / 3.0, 1.0).toFloat())
                Box(Modifier.padding(4.dp).fillMaxWidth().height(76.dp).clip(RoundedCornerShape(8.dp)).background(bg).clickable { open(s) }.padding(8.dp)) {
                    Column {
                        Text(n, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text(q?.let { sign(it.pct) + "%" } ?: "–", fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

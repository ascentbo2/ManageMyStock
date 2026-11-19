package com.managestock.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managestock.app.data.NewsItem

@Composable
fun NewsScreen(news: List<NewsItem>) {
    val cats = listOf("All", "Markets", "Stocks", "Economy", "MFs")
    var cat by remember { mutableStateOf("All") }
    val list = if (cat == "All") news else news.filter { it.category == cat }

    Column {
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cats) { c ->
                FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) })
            }
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(list) { NewsCard(it) }
        }
    }
}

@Composable
fun NewsCard(item: NewsItem, horizontalPadding: Dp = 16.dp) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding, vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(item.category.uppercase(), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text(item.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 6.dp))
            Text("${item.source} · ${item.time}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

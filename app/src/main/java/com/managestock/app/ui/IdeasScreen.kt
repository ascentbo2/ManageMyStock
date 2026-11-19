package com.managestock.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managestock.app.data.Idea
import com.managestock.app.ui.theme.Gain
import com.managestock.app.ui.theme.Loss

@Composable
fun IdeasScreen(ideas: List<Idea>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ideas) { idea ->
            val buy = idea.action == "BUY"
            val actionColor = if (buy) Gain else Loss
            val statusColor = when (idea.status) {
                "Target hit" -> Gain
                "SL hit" -> Loss
                else -> MaterialTheme.colorScheme.primary
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(idea.symbol, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        Tag(idea.action, actionColor)
                        Spacer(Modifier.width(8.dp))
                        Tag(idea.status, statusColor)
                    }
                    Text(
                        idea.title,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Row {
                        Level("Entry", idea.entry, Modifier.weight(1f))
                        Level("Target", idea.target, Modifier.weight(1f))
                        Level("Stop-loss", idea.stopLoss, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun Tag(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun Level(label: String, value: Double, modifier: Modifier) {
    Column(modifier) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(money(value), fontWeight = FontWeight.SemiBold)
    }
}

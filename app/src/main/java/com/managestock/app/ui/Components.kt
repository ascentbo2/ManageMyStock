package com.managestock.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.managestock.app.ui.theme.Gain
import com.managestock.app.ui.theme.Loss

fun changeColor(change: Double): Color = if (change >= 0) Gain else Loss

@Composable
fun ChangeText(change: Double, pctValue: Double, modifier: Modifier = Modifier) {
    Text(
        text = "${signed(change)} (${pct(pctValue)})",
        color = changeColor(change),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
fun PricePill(pctValue: Double, modifier: Modifier = Modifier) {
    val c = changeColor(pctValue)
    Text(
        text = pct(pctValue),
        color = c,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(c.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun LineChart(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = if (max - min > 0f) max - min else 1f
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - (v - min) / range * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

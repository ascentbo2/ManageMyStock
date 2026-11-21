package com.example.stocks

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PriceAlert(val symbol: String, val target: Double, val above: Boolean, val fired: Boolean = false)

object Alerts {
    fun load(ctx: Context): List<PriceAlert> = try {
        val a = JSONArray(ctx.getSharedPreferences("stocks", 0).getString("al", "[]"))
        (0 until a.length()).map { a.getJSONObject(it) }.map { PriceAlert(it.getString("s"), it.getDouble("t"), it.getBoolean("a"), it.optBoolean("f")) }
    } catch (e: Exception) { emptyList() }

    fun save(ctx: Context, l: List<PriceAlert>) {
        val a = JSONArray(); l.forEach { a.put(JSONObject().put("s", it.symbol).put("t", it.target).put("a", it.above).put("f", it.fired)) }
        ctx.getSharedPreferences("stocks", 0).edit().putString("al", a.toString()).apply()
    }

    fun evaluate(list: List<PriceAlert>, price: (String) -> Double?): Pair<List<PriceAlert>, List<Pair<PriceAlert, Double>>> {
        val hits = mutableListOf<Pair<PriceAlert, Double>>()
        val upd = list.map { a ->
            val p = price(a.symbol)
            if (!a.fired && p != null && (if (a.above) p >= a.target else p <= a.target)) { hits.add(a to p); a.copy(fired = true) } else a
        }
        return upd to hits
    }

    fun schedule(ctx: Context) {
        try {
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork("price_alerts", ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<AlertWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build())
        } catch (e: Exception) { }
    }
}

object Notifier {
    fun send(ctx: Context, title: String, text: String, id: Int) {
        try {
            if (Build.VERSION.SDK_INT >= 33 && ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
            if (Build.VERSION.SDK_INT >= 26) {
                val nm = ctx.getSystemService(NotificationManager::class.java)
                nm.createNotificationChannel(NotificationChannel("alerts", "Price alerts", NotificationManager.IMPORTANCE_HIGH))
            }
            val n = NotificationCompat.Builder(ctx, "alerts").setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title).setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).build()
            NotificationManagerCompat.from(ctx).notify(id, n)
        } catch (e: Exception) { }
    }
}

/** Checks alerts about every 15 minutes while the app is closed (Android's minimum for background work). */
class AlertWorker(ctx: Context, p: WorkerParameters) : CoroutineWorker(ctx, p) {
    override suspend fun doWork(): Result {
        val list = Alerts.load(applicationContext)
        val active = list.filter { !it.fired }
        if (active.isEmpty()) return Result.success()
        val q = withContext(Dispatchers.IO) { Api.yahooQuotes(active.map { it.symbol }.distinct()) }.associateBy { it.symbol }
        val (upd, hits) = Alerts.evaluate(list) { q[it]?.price }
        if (hits.isNotEmpty()) {
            Alerts.save(applicationContext, upd)
            hits.forEach { (a, p) ->
                Notifier.send(applicationContext, "${short(a.symbol)} price alert",
                    "${short(a.symbol)} is at ₹${f(p)}, ${if (a.above) "above" else "below"} your target of ₹${f(a.target)}", (a.symbol + a.target).hashCode())
            }
        }
        return Result.success()
    }
}

@Composable
fun AlertForm(vm: VM, sym: String, price: Double?, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    var t by remember { mutableStateOf("") }
    var above by remember { mutableStateOf(true) }
    var err by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss,
        confirmButton = { TextButton({
            val v = t.toDoubleOrNull()
            if (v == null || v <= 0) err = "Enter a valid price"
            else {
                vm.addAlert(sym, v, above)
                if (Build.VERSION.SDK_INT >= 33) (ctx as? Activity)?.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                onDismiss()
            }
        }) { Text("Set alert") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
        title = { Text("Alert for ${short(sym)}") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (price != null) Text("Current price ₹${f(price)}")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(above, { above = true }, { Text("Rises to") })
                FilterChip(!above, { above = false }, { Text("Falls to") })
            }
            OutlinedTextField(t, { t = it }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), label = { Text("Target price (₹)") })
            if (err.isNotEmpty()) Text(err, color = Red, fontSize = 12.sp)
            Text("You get a notification when it is reached. While the app is closed, prices are checked about every 15 minutes.", fontSize = 11.sp, color = Color.Gray)
        } })
}

@Composable
fun AlertsDialog(vm: VM, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onDismiss) { Text("Close") } },
        title = { Text("Price alerts") },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) {
            if (vm.alerts.isEmpty()) Text("No alerts yet. Open any stock and tap the bell icon to set one.", color = Color.Gray, fontSize = 13.sp)
            vm.alerts.toList().forEach { a ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(short(a.symbol), fontWeight = FontWeight.SemiBold)
                        Text((if (a.above) "Rises to ₹" else "Falls to ₹") + f(a.target) + if (a.fired) "  ·  triggered" else "", fontSize = 12.sp, color = if (a.fired) Color.Gray else Color.Unspecified)
                    }
                    IconButton({ vm.removeAlert(a) }) { Icon(Icons.Default.Delete, "Delete alert") }
                }
                HorizontalDivider()
            }
        } })
}

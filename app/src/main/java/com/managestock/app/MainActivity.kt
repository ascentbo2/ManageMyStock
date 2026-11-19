package com.managestock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.managestock.app.data.MarketViewModel
import com.managestock.app.ui.*
import com.managestock.app.ui.theme.ManageStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ManageStockTheme { App() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(vm: MarketViewModel = viewModel()) {
    val stocks by vm.stocks.collectAsState()
    val indices by vm.indices.collectAsState()
    val watchlist by vm.watchlist.collectAsState()

    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    BackHandler(enabled = selected != null || searching) {
        if (searching) { searching = false; query = "" } else selected = null
    }

    val sel = stocks.firstOrNull { it.symbol == selected }

    if (sel != null) {
        StockDetailScreen(
            stock = sel,
            history = { range -> vm.history(sel.symbol, range) },
            news = vm.news,
            watched = sel.symbol in watchlist,
            onToggleWatch = { vm.toggleWatch(sel.symbol) },
            onBack = { selected = null }
        )
        return
    }

    val titles = listOf("Markets", "Ideas", "News", "Portfolio")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titles[tab], fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { searching = !searching; query = "" }) {
                        Icon(Icons.Filled.Search, "Search")
                    }
                    IconButton(onClick = { }) { Icon(Icons.Filled.Notifications, "Notifications") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    "Home" to Icons.Filled.Home,
                    "Ideas" to Icons.Filled.Lightbulb,
                    "News" to Icons.Filled.Newspaper,
                    "Portfolio" to Icons.Filled.PieChart
                )
                items.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i; searching = false },
                        icon = { Icon(icon, label) },
                        label = { Text(label) }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        Box(Modifier.padding(inner)) {
            if (searching) {
                Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search stocks") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                    val results = if (query.isBlank()) emptyList() else stocks.filter {
                        it.symbol.contains(query, true) || it.name.contains(query, true)
                    }
                    LazyColumn {
                        items(results, key = { it.symbol }) { s ->
                            ListItem(
                                headlineContent = { Text(s.symbol, fontWeight = FontWeight.Bold) },
                                supportingContent = { Text(s.name) },
                                modifier = Modifier.clickable {
                                    selected = s.symbol; searching = false; query = ""
                                }
                            )
                        }
                    }
                }
            } else when (tab) {
                0 -> HomeScreen(
                    stocks = stocks,
                    indices = indices,
                    news = vm.news,
                    watchlist = watchlist,
                    marketOpen = isMarketOpen(),
                    onStockClick = { selected = it.symbol },
                    onSeeNews = { tab = 2 }
                )
                1 -> IdeasScreen(vm.ideas)
                2 -> NewsScreen(vm.news)
                else -> PortfolioScreen(vm.holdings, vm.funds, stocks) { selected = it.symbol }
            }
        }
    }
}

# ManageStock

Android stock market app (Jetpack Compose) with Home / Ideas / News / Portfolio tabs,
Markets Dashboard, watchlist with 3-second simulated price refresh, stock detail with
chart + technicals, and portfolio with mutual funds. Runs on sample data.

## Build the APK (no Android Studio)
1. Push everything in this folder to the root of your ManageStock repo (main branch).
2. GitHub > Actions > "Build APK" runs automatically (or press "Run workflow").
3. When it finishes, open the run and download the artifact "ManageStock-debug-apk".
4. Unzip, copy app-debug.apk to your phone, install (allow unknown sources).

## Going live
All data goes through `data/MarketRepository.kt`. Add a second implementation that
reads your broker's API (REST + WebSocket) and swap it in `MarketViewModel`.

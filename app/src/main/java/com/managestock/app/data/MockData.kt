package com.managestock.app.data

import kotlin.random.Random

/** Sample data only. Replace through MarketRepository when the real feed is connected. */
object MockData {
    val stocks = listOf(
        Stock("RELIANCE", "Reliance Industries", 2890.40, 2874.10, 3217.90, 2220.30, 5_400_000, 27.4, 1_955_000.0),
        Stock("TCS", "Tata Consultancy Services", 4012.55, 4031.00, 4592.25, 3311.80, 1_900_000, 30.1, 1_450_000.0),
        Stock("HDFCBANK", "HDFC Bank", 1688.20, 1671.45, 1794.00, 1363.55, 9_800_000, 19.6, 1_280_000.0),
        Stock("INFY", "Infosys", 1822.75, 1830.90, 1991.90, 1351.65, 6_700_000, 26.3, 755_000.0),
        Stock("ICICIBANK", "ICICI Bank", 1245.60, 1232.15, 1362.35, 970.00, 11_200_000, 18.2, 875_000.0),
        Stock("SBIN", "State Bank of India", 812.30, 805.95, 912.00, 600.65, 14_500_000, 11.4, 725_000.0),
        Stock("BHARTIARTL", "Bharti Airtel", 1604.10, 1590.80, 1779.00, 1130.00, 3_200_000, 72.5, 975_000.0),
        Stock("ITC", "ITC Ltd", 468.90, 471.35, 528.50, 399.35, 12_100_000, 28.9, 585_000.0),
        Stock("LT", "Larsen & Toubro", 3655.00, 3620.40, 3919.90, 2965.30, 1_400_000, 34.7, 502_000.0),
        Stock("TATAMOTORS", "Tata Motors", 985.25, 1002.60, 1179.00, 606.00, 8_900_000, 9.1, 362_000.0),
        Stock("MARUTI", "Maruti Suzuki", 12410.00, 12335.50, 13680.00, 9738.00, 380_000, 27.8, 390_000.0),
        Stock("SUNPHARMA", "Sun Pharmaceutical", 1782.40, 1769.25, 1960.35, 1175.05, 1_700_000, 38.2, 428_000.0),
        Stock("ADANIENT", "Adani Enterprises", 2395.80, 2431.10, 3743.90, 2142.00, 2_300_000, 61.0, 276_000.0),
        Stock("WIPRO", "Wipro", 541.30, 538.05, 593.50, 387.00, 5_100_000, 24.6, 283_000.0)
    )

    private fun spark(base: Double): List<Float> {
        val r = Random(base.toInt())
        var p = base
        return List(30) { p *= 1 + r.nextDouble(-0.002, 0.002); p.toFloat() }
    }

    val indices = listOf(
        IndexQuote("NIFTY 50", 24850.35, 24790.10, spark(24850.0)),
        IndexQuote("SENSEX", 81320.80, 81105.45, spark(81320.0)),
        IndexQuote("BANK NIFTY", 53240.60, 53410.25, spark(53240.0)),
        IndexQuote("NIFTY IT", 41980.15, 41890.00, spark(41980.0))
    )

    val news = listOf(
        NewsItem("Sensex, Nifty end higher as banking stocks lead the rally", "Markets Desk", "15 min ago", "Markets"),
        NewsItem("IT majors steady ahead of quarterly updates", "Markets Desk", "1 hr ago", "Stocks"),
        NewsItem("RBI policy outlook: what analysts expect next", "Economy Desk", "2 hr ago", "Economy"),
        NewsItem("Mutual fund SIP inflows stay strong for another month", "MF Desk", "3 hr ago", "MFs"),
        NewsItem("Auto sales data points to a festive-season pickup", "Stocks Desk", "5 hr ago", "Stocks"),
        NewsItem("Crude oil eases; rupee holds steady against the dollar", "Economy Desk", "6 hr ago", "Economy")
    )

    val ideas = listOf(
        Idea("HDFCBANK", "Breakout above resistance with rising volume", "BUY", 1690.0, 1790.0, 1635.0, "Active"),
        Idea("TATAMOTORS", "Weak momentum below key moving average", "SELL", 990.0, 930.0, 1025.0, "Active"),
        Idea("LT", "Strong order book; pullback entry", "BUY", 3600.0, 3900.0, 3490.0, "Target hit"),
        Idea("ITC", "Range-bound; stop-loss triggered", "BUY", 475.0, 510.0, 462.0, "SL hit")
    )

    val holdings = listOf(
        Holding("RELIANCE", 20, 2510.0),
        Holding("TCS", 10, 3650.0),
        Holding("HDFCBANK", 40, 1540.0),
        Holding("ITC", 150, 410.0)
    )

    val funds = listOf(
        MutualFund("Flexi Cap Fund - Direct Growth", 120_000.0, 148_500.0),
        MutualFund("Nifty 50 Index Fund - Direct", 80_000.0, 93_200.0),
        MutualFund("Small Cap Fund - Direct Growth", 50_000.0, 61_800.0)
    )
}

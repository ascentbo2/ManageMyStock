package com.managestock.app.ui

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

fun money(v: Double): String = String.format(Locale.US, "%,.2f", v)
fun signed(v: Double): String = String.format(Locale.US, "%+,.2f", v)
fun pct(v: Double): String = String.format(Locale.US, "%+.2f%%", v)

fun isMarketOpen(): Boolean {
    val now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
    if (now.dayOfWeek == DayOfWeek.SATURDAY || now.dayOfWeek == DayOfWeek.SUNDAY) return false
    val t = now.toLocalTime()
    return !t.isBefore(LocalTime.of(9, 15)) && !t.isAfter(LocalTime.of(15, 30))
}

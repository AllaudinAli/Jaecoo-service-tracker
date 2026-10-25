package com.example.j5servicebook.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Plain formatting: "20 Nov 2026", "2,000 km", "PKR 12,500". */
object Fmt {
    private val full = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)
    private val short = DateTimeFormatter.ofPattern("d MMM", Locale.US)

    fun date(d: LocalDate): String = d.format(full)
    fun shortDate(d: LocalDate): String = d.format(short)
    fun number(n: Int): String = String.format(Locale.US, "%,d", n)
    fun km(n: Int): String = "${number(n)} km"
    fun pkr(n: Int): String = "PKR ${number(n)}"

    fun relativeDays(days: Long): String = when {
        days == 0L -> "Today"
        days == 1L -> "Tomorrow"
        days == -1L -> "Yesterday"
        days > 0 -> "In $days days"
        else -> "${-days} days ago"
    }

    /** Compact duration for countdowns: "26 days", "4 months", "6 years". */
    fun span(days: Long): String {
        val d = kotlin.math.abs(days)
        return when {
            d < 60 -> plural(d, "day")
            d < 730 -> plural(d / 30, "month")
            else -> plural(d / 365, "year")
        }
    }

    private fun plural(n: Long, unit: String) = if (n == 1L) "1 $unit" else "$n ${unit}s"
}

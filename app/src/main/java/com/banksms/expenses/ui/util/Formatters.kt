package com.banksms.expenses.ui.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val amountFormat = DecimalFormat("#,##0.##")
    private val dateTimeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun formatMoney(amount: Double, currency: String = "₹"): String {
        return "$currency${amountFormat.format(amount)}"
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }
}

package com.banksms.expenses.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val indianSymbols = DecimalFormatSymbols(Locale("en", "IN"))
    private val indianAmountFormat = DecimalFormat("#,##,##0.##", indianSymbols)
    private val dateTimeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dayHeaderFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

    fun formatMoney(amount: Double, currency: String = "₹", masked: Boolean = false): String {
        if (masked) return "$currency••••"
        val absVal = kotlin.math.abs(amount)
        val formatted = if (currency == "₹") {
            indianAmountFormat.format(absVal)
        } else {
            DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.US)).format(absVal)
        }
        return if (amount < 0) "-$currency$formatted" else "$currency$formatted"
    }

    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    fun formatShortDate(timestamp: Long): String = shortDateFormat.format(Date(timestamp))

    fun formatDayHeader(timestamp: Long): String = dayHeaderFormat.format(Date(timestamp))
}

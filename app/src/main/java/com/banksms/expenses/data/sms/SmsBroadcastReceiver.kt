package com.banksms.expenses.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.banksms.expenses.BankExpensesApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val grouped = messages.groupBy { it.displayOriginatingAddress.orEmpty() }
        val pendingResult = goAsync()
        val repository = (context.applicationContext as BankExpensesApp).repository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rawMessages = grouped.mapNotNull { (sender, parts) ->
                    val fullBody = parts.joinToString(separator = "") { it.displayMessageBody.orEmpty() }
                    val timestamp = parts.maxOfOrNull { it.timestampMillis } ?: System.currentTimeMillis()
                    if (sender.isBlank() || fullBody.isBlank()) null
                    else RawSmsMessage(id = null, sender = sender, body = fullBody, timestamp = timestamp)
                }
                repository.ingestMessages(rawMessages)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

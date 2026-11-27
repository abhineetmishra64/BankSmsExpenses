package com.banksms.expenses.data.sms

import android.content.Context
import android.provider.Telephony

data class RawSmsMessage(
    val id: Long?,
    val sender: String,
    val body: String,
    val timestamp: Long
)

class SmsReader(private val context: Context) {

    fun readInboxMessages(sinceTimestampMillis: Long? = null): List<RawSmsMessage> {
        val messages = mutableListOf<RawSmsMessage>()
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val selection = if (sinceTimestampMillis != null) "${Telephony.Sms.DATE} >= ?" else null
        val selectionArgs = if (sinceTimestampMillis != null) arrayOf(sinceTimestampMillis.toString()) else null

        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addrCol = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (cursor.moveToNext()) {
                val sender = cursor.getString(addrCol).orEmpty()
                val body = cursor.getString(bodyCol).orEmpty()
                if (sender.isBlank() || body.isBlank()) continue

                messages += RawSmsMessage(
                    id = cursor.getLong(idCol),
                    sender = sender,
                    body = body,
                    timestamp = cursor.getLong(dateCol)
                )
            }
        }

        return messages
    }

    fun buildSampleMessages(nowMillis: Long = System.currentTimeMillis()): List<RawSmsMessage> {
        val dayMs = 86_400_000L
        val minMs = 60_000L
        return listOf(
            RawSmsMessage(null, "AD-HDFCBK", "Amt Sent Rs.485.00 from HDFC Bank A/c XX1234 to Swiggy on 26-11-26 UPI Ref 432198765432. Avl Bal Rs.42,310.50", nowMillis - 2 * 3_600_000L),
            RawSmsMessage(null, "AD-HDFCBK", "Rs 2,499.00 spent on HDFC Bank Card xx8812 at Amazon India on 25-11-26. Avl Lmt Rs.1,45,000.00", nowMillis - dayMs),
            RawSmsMessage(null, "VK-ICICIB", "ICICI Bank Acct XX4521 debited for Rs 320.00 on 25-11-26; Uber India credited. Avl Bal Rs 18,900.00", nowMillis - dayMs - 7_200_000L),
            RawSmsMessage(null, "VK-ICICIB", "Dear Customer, your ICICI Bank Acct XX4521 is credited with Rs 75,000.00 on 20-11-26 towards Salary.", nowMillis - 5 * dayMs),
            RawSmsMessage(null, "JX-SBIINB", "Your A/c X7790-debited by Rs.1,850.00 on 24-11-26 transfer to BESCOM Electricity. Avl Bal Rs.29,412.20", nowMillis - 2 * dayMs),
            RawSmsMessage(null, "JX-SBIINB", "Rs.3,000.00 withdrawn at SBI ATM Indiranagar from A/c X7790 on 22-11-26. Avl Bal Rs.31,262.20", nowMillis - 4 * dayMs),
            RawSmsMessage(null, "VM-AXISBK", "INR 649.00 spent on Axis Bank Card no. XX3301 at Netflix on 23-11-26. Avl Lmt INR 92,000.00", nowMillis - 3 * dayMs),
            RawSmsMessage(null, "BP-KOTAKB", "Sent Rs.790.00 from Kotak Bank AC X9011 to Apollo Pharmacy via UPI. Bal Rs.14,210.00", nowMillis - 6 * dayMs),
            // Self-transfer / card bill payment pair (within 4 minutes)
            RawSmsMessage(null, "AD-HDFCBK", "Amt Sent Rs.12,000.00 from HDFC Bank A/c XX1234 to HDFC Card Bill Payment on 18-11-26. Avl Bal Rs.45,000.00", nowMillis - 8 * dayMs),
            RawSmsMessage(null, "AD-HDFCBK", "Thank you! Payment of Rs.12,000.00 credited to HDFC Bank Card xx8812 on 18-11-26. Avl Lmt Rs.1,48,000.00", nowMillis - 8 * dayMs + 4 * minMs),
            // SIP / Mutual Fund suggestion candidate
            RawSmsMessage(null, "AD-HDFCBK", "Rs.5,000.00 debited from HDFC Bank A/c XX1234 towards ACH D- Mutual Fund SIP Folio 99120 on 15-11-26.", nowMillis - 10 * dayMs),
            // Uncategorised merchant candidate
            RawSmsMessage(null, "VK-ICICIB", "ICICI Bank Acct XX4521 debited for Rs 1,450.00 on 14-11-26; BlueTokai Roastery credited.", nowMillis - 11 * dayMs),
            // Previous month transactions for Trends & Recurring
            RawSmsMessage(null, "VM-AXISBK", "INR 649.00 spent on Axis Bank Card no. XX3301 at Netflix on 23-10-26. Avl Lmt INR 92,649.00", nowMillis - 33 * dayMs),
            RawSmsMessage(null, "VK-ICICIB", "Dear Customer, your ICICI Bank Acct XX4521 is credited with Rs 75,000.00 on 20-10-26 towards Salary.", nowMillis - 35 * dayMs),
            RawSmsMessage(null, "AD-HDFCBK", "Amt Sent Rs.1,240.00 from HDFC Bank A/c XX1234 to Blinkit on 18-10-26. Avl Bal Rs.52,000.00", nowMillis - 37 * dayMs),
            // Unparsed bank alert (missing amount/direction keywords or unusual format)
            RawSmsMessage(null, "AD-HDFCBK", "HDFC Bank Alert: Standing Instruction reference SI90812 processed for your A/c XX1234.", nowMillis - 7 * dayMs),
            // Ignored OTP
            RawSmsMessage(null, "AD-HDFCBK", "OTP is 491822 for transaction of Rs.1,200.00 at Flipkart. Do not share with anyone.", nowMillis - 3_600_000L)
        )
    }
}

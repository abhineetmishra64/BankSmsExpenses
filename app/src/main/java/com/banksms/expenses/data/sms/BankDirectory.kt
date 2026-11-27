package com.banksms.expenses.data.sms

import com.banksms.expenses.data.model.AccountType
import java.util.Locale
import kotlin.math.abs

data class BankInfo(
    val canonicalName: String,
    val accentColorHex: Long
)

object BankDirectory {

    private data class BankRule(
        val canonicalName: String,
        val senderKeywords: List<String>,
        val bodyKeywords: List<String>,
        val accentColorHex: Long
    )

    private val rules = listOf(
        BankRule("HDFC Bank", listOf("HDFCBK", "HDFC"), listOf("hdfc bank", "hdfc card"), 0xFF004C8F),
        BankRule("ICICI Bank", listOf("ICICIB", "ICICI"), listOf("icici bank", "icici"), 0xFFB02A30),
        BankRule("SBI", listOf("SBIINB", "SBIPSG", "ATMSBI", "SBICRD", "CBSBI", "SBIUPI", "SBI"), listOf("state bank of india", " sbi ", "sbi a/c", "sbi card"), 0xFF1A56DB),
        BankRule("Axis Bank", listOf("AXISBK", "AXIS"), listOf("axis bank"), 0xFF97144D),
        BankRule("Kotak Bank", listOf("KOTAKB", "KOTAK"), listOf("kotak mahindra", "kotak bank", "kotak"), 0xFFED1C24),
        BankRule("IDFC FIRST Bank", listOf("IDFCFB", "IDFCBK", "IDFC"), listOf("idfc first", "idfc bank"), 0xFF9C1D26),
        BankRule("IndusInd Bank", listOf("INDUSB", "INDUS"), listOf("indusind"), 0xFF8C1D40),
        BankRule("Yes Bank", listOf("YESBNK", "YESBK"), listOf("yes bank"), 0xFF0051A2),
        BankRule("Punjab National Bank", listOf("PNBSMS", "PNB"), listOf("punjab national bank", "pnb"), 0xFFA20E37),
        BankRule("Bank of Baroda", listOf("BOBSMS", "BOBTXN", "BOB"), listOf("bank of baroda", "bob a/c"), 0xFFF26522),
        BankRule("Federal Bank", listOf("FEDBNK", "FEDERL"), listOf("federal bank"), 0xFF004F9F),
        BankRule("Canara Bank", listOf("CANBNK", "CANARA"), listOf("canara bank"), 0xFF0095DA),
        BankRule("Union Bank", listOf("UNIONB", "UBOI"), listOf("union bank"), 0xFF00569B),
        BankRule("Standard Chartered", listOf("SCBANK", "SCB"), listOf("standard chartered"), 0xFF0072AA),
        BankRule("Citibank", listOf("CITIBK", "CITI"), listOf("citibank", "citi card"), 0xFF003B70),
        BankRule("American Express", listOf("AMEXIN", "AMEX"), listOf("american express", "amex"), 0xFF006FCF),
        BankRule("HSBC", listOf("HSBCIN", "HSBC"), listOf("hsbc"), 0xFFDB0011),
        BankRule("RBL Bank", listOf("RBLBNK", "RBLCRD"), listOf("rbl bank"), 0xFF23397A),
        BankRule("AU Bank", listOf("AUBANK", "AUBNK"), listOf("au small finance", "au bank"), 0xFF6B2D8B),
        BankRule("Paytm Payments Bank", listOf("PYTM", "PAYTMB"), listOf("paytm payments bank"), 0xFF00BAF2),
        BankRule("Airtel Payments Bank", listOf("AIRBNK", "AIRTEL"), listOf("airtel payments bank"), 0xFFE40000),
        BankRule("Chase", listOf("CHASE"), listOf("chase"), 0xFF117ACA),
        BankRule("Bank of America", listOf("BOFA"), listOf("bank of america"), 0xFFE31837),
        BankRule("Wells Fargo", listOf("WELLSF"), listOf("wells fargo"), 0xFFD71E28),
        BankRule("Capital One", listOf("CAPONE"), listOf("capital one"), 0xFF004977)
    )

    private val fallbackPalette = listOf(
        0xFF0F766E,
        0xFF4338CA,
        0xFF7C3AED,
        0xFF0369A1,
        0xFFB45309,
        0xFFBE185D
    )

    private val dltHeaderRegex = Regex("^[A-Za-z]{2}-([A-Za-z0-9]+)(?:-[A-Za-z])?$")
    private val bodyBankRegex = Regex("\\b([A-Z][A-Za-z& ]{1,24}\\s+Bank)\\b")

    fun normalizeSenderCode(sender: String): String {
        val trimmed = sender.trim().uppercase(Locale.ROOT)
        val match = dltHeaderRegex.matchEntire(trimmed)
        return match?.groupValues?.get(1) ?: trimmed.replace(Regex("[^A-Z0-9]"), "")
    }

    fun isKnownBankSender(sender: String, body: String): Boolean {
        val normalizedSender = normalizeSenderCode(sender)
        val lowerBody = " ${body.lowercase(Locale.ROOT)} "
        return rules.any { rule ->
            rule.senderKeywords.any { normalizedSender.contains(it) } ||
                rule.bodyKeywords.any { lowerBody.contains(it) }
        }
    }

    fun detectAccountType(body: String, sender: String): AccountType {
        val lower = body.lowercase(Locale.ROOT)
        val normSender = normalizeSenderCode(sender)
        return when {
            lower.contains("credit card") ||
                lower.contains("avl lmt") ||
                lower.contains("available limit") ||
                lower.contains("credit limit") ||
                lower.contains("card xx") ||
                lower.contains("card no") ||
                lower.contains("bank card") ||
                normSender.contains("SBICRD") ||
                normSender.contains("RBLCRD") ||
                normSender.contains("AMEX") -> AccountType.CREDIT
            lower.contains("wallet") ||
                lower.contains("paytm wallet") ||
                lower.contains("amazon pay balance") -> AccountType.WALLET
            else -> AccountType.SAVINGS
        }
    }

    fun resolveBank(sender: String, body: String): BankInfo {
        val normalizedSender = normalizeSenderCode(sender)
        val lowerBody = " ${body.lowercase(Locale.ROOT)} "

        rules.firstOrNull { rule ->
            rule.senderKeywords.any { normalizedSender.contains(it) }
        }?.let { return BankInfo(it.canonicalName, it.accentColorHex) }

        rules.firstOrNull { rule ->
            rule.bodyKeywords.any { lowerBody.contains(it) }
        }?.let { return BankInfo(it.canonicalName, it.accentColorHex) }

        val bodyBankMatch = bodyBankRegex.find(body)?.groupValues?.get(1)?.trim()
        val fallbackName = when {
            !bodyBankMatch.isNullOrBlank() -> bodyBankMatch
            normalizedSender.isNotBlank() && normalizedSender.any { it.isLetter() } -> normalizedSender
            else -> "Other Bank"
        }

        val color = fallbackPalette[abs(fallbackName.hashCode()) % fallbackPalette.size]
        return BankInfo(canonicalName = fallbackName, accentColorHex = color)
    }

    fun accentColorForBank(bankName: String): Long {
        rules.firstOrNull { it.canonicalName.equals(bankName, ignoreCase = true) }?.let {
            return it.accentColorHex
        }
        return fallbackPalette[abs(bankName.hashCode()) % fallbackPalette.size]
    }
}

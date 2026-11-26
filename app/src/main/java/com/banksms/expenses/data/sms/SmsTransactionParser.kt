package com.banksms.expenses.data.sms

import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.TransactionType
import java.security.MessageDigest
import java.util.Locale

data class ParsedSmsTransaction(
    val smsId: Long?,
    val sender: String,
    val bankName: String,
    val accountLast4: String?,
    val amount: Double,
    val currency: String,
    val type: TransactionType,
    val merchant: String,
    val category: ExpenseCategory,
    val balanceAfter: Double?,
    val timestamp: Long,
    val rawBody: String,
    val fingerprint: String
)

object SmsTransactionParser {

    private val ignoreKeywords = listOf(
        "otp",
        "one time password",
        "verification code",
        "do not share",
        "mpin",
        "cvv",
        "declined",
        "failed",
        "unsuccessful",
        "could not be processed",
        "insufficient balance",
        "minimum due",
        "statement is generated",
        "e-statement",
        "pre-approved",
        "apply now",
        "will be debited",
        "scheduled to be debited",
        "mandate created",
        "auto-pay reminder",
        "due on"
    )

    private val debitKeywords = listOf(
        "debited",
        "spent",
        "paid",
        "withdrawn",
        "charged",
        "purchase",
        "sent",
        "transferred",
        "deducted",
        "payment of",
        "txn of",
        "dr."
    )

    private val creditKeywords = listOf(
        "credited",
        "received",
        "deposited",
        "refund",
        "reversed",
        "reversal",
        "cashback",
        "cr."
    )

    private val amountPattern = Regex(
        """(?i)(?:(rs\.?|inr|₹|usd|\$|eur|€|gbp|£)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)|([0-9][0-9,]*(?:\.[0-9]{1,2})?)\s*(rs\.?|inr|usd|eur|gbp))"""
    )

    private val balanceContextPattern = Regex(
        """(?i)(?:avl\.?\s*bal|available\s+bal(?:ance)?|clear\s+bal|bal(?:ance)?|avl\.?\s*lmt|available\s+limit)[:\s\-]*"""
    )

    private val accountLast4Pattern = Regex(
        """(?i)(?:a/c|acct|account|card|xx|x\*+|ending\s+(?:with|in)?)\s*(?:no\.?\s*)?(?:[xX*]{1,8}|ending\s*)?(\d{4})\b"""
    )

    private val merchantPatterns = listOf(
        Regex("""(?i)\bVPA\s+([A-Za-z0-9._-]+@[A-Za-z0-9._-]+)"""),
        Regex("""(?i);\s*([A-Za-z0-9][A-Za-z0-9 &._*/@-]{1,40}?)\s+credited\b"""),
        Regex("""(?i)\b(?:at|to|towards|for|by)\s+(?!(?:your|a/c|acct|account|card|rs\.?|inr|usd|\d{2}[-/]\d{2}))([A-Za-z0-9][A-Za-z0-9 &._*/@-]{1,40}?)(?=\s+(?:on|via|ref|utr|avl|bal|if|not|through|using|from)\b|[.,;]|$)"""),
        Regex("""(?i)\bInfo[:\-\s]+([A-Za-z0-9][A-Za-z0-9 &._*/@-]{1,40}?)(?=\s+(?:on|avl|bal|ref)\b|[.,;]|$)""")
    )

    private val categoryRules: List<Pair<ExpenseCategory, List<String>>> = listOf(
        ExpenseCategory.FOOD to listOf(
            "swiggy", "zomato", "ubereats", "dominos", "pizza", "mcdonald", "kfc",
            "starbucks", "cafe", "coffee", "restaurant", "dining", "bakery", "blinkit",
            "zepto", "instamart", "bigbasket", "grocer", "supermarket", "dmart", "food"
        ),
        ExpenseCategory.TRANSPORT to listOf(
            "uber", "ola", "rapido", "metro", "irctc", "rail", "airline", "indigo",
            "airindia", "vistara", "makemytrip", "redbus", "petrol", "diesel", "fuel",
            "hpcl", "bpcl", "iocl", "shell", "fastag", "toll", "parking", "cab"
        ),
        ExpenseCategory.SHOPPING to listOf(
            "amazon", "flipkart", "myntra", "ajio", "nykaa", "meesho", "tatacliq",
            "apple", "croma", "reliance", "decathlon", "ikea", "zara", "h&m", "mall", "store"
        ),
        ExpenseCategory.BILLS to listOf(
            "electricity", "water", "gas", "broadband", "wifi", "airtel", "jio", "vi ",
            "vodafone", "bsnl", "recharge", "billdesk", "bbps", "bescom", "tata power",
            "adani", "rent", "maintenance", "insurance", "lic", "emi", "loan"
        ),
        ExpenseCategory.ENTERTAINMENT to listOf(
            "netflix", "spotify", "prime video", "hotstar", "disney", "youtube",
            "bookmyshow", "pvr", "inox", "cinema", "movie", "steam", "playstation", "xbox"
        ),
        ExpenseCategory.HEALTH to listOf(
            "pharmacy", "apollo", "pharmeasy", "1mg", "netmeds", "hospital", "clinic",
            "diagnostic", "medplus", "dental", "doctor", "lab", "health"
        ),
        ExpenseCategory.ATM to listOf(
            "atm", "cash withdrawal", "withdrawn at"
        ),
        ExpenseCategory.TRANSFER to listOf(
            "upi", "imps", "neft", "rtgs", "@ok", "@ybl", "@ibl", "@axl", "@paytm", "vpa"
        )
    )

    fun parse(
        smsId: Long? = null,
        sender: String,
        body: String,
        timestamp: Long
    ): ParsedSmsTransaction? {
        val cleanedBody = body.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()
        if (cleanedBody.length < 12) return null

        val lowerBody = cleanedBody.lowercase(Locale.ROOT)
        if (ignoreKeywords.any { lowerBody.contains(it) }) return null

        val type = detectTransactionType(lowerBody) ?: return null
        val (amount, currency) = extractPrimaryAmount(cleanedBody) ?: return null
        if (amount <= 0.0) return null

        val bankInfo = BankDirectory.resolveBank(sender, cleanedBody)
        val accountLast4 = extractAccountLast4(cleanedBody)
        val balanceAfter = extractBalance(cleanedBody)
        val merchant = extractMerchant(cleanedBody, type)
        val category = classifyCategory(merchant, cleanedBody, type)
        val fingerprint = buildFingerprint(
            bankName = bankInfo.canonicalName,
            type = type,
            amount = amount,
            accountLast4 = accountLast4,
            timestamp = timestamp,
            rawBody = cleanedBody
        )

        return ParsedSmsTransaction(
            smsId = smsId,
            sender = sender,
            bankName = bankInfo.canonicalName,
            accountLast4 = accountLast4,
            amount = amount,
            currency = currency,
            type = type,
            merchant = merchant,
            category = category,
            balanceAfter = balanceAfter,
            timestamp = timestamp,
            rawBody = body,
            fingerprint = fingerprint
        )
    }

    private fun detectTransactionType(lowerBody: String): TransactionType? {
        val firstDebitIdx = debitKeywords
            .mapNotNull { kw -> lowerBody.indexOf(kw).takeIf { it >= 0 } }
            .minOrNull()
        val firstCreditIdx = creditKeywords
            .mapNotNull { kw -> lowerBody.indexOf(kw).takeIf { it >= 0 } }
            .minOrNull()

        return when {
            firstDebitIdx != null && firstCreditIdx != null -> {
                if (firstDebitIdx <= firstCreditIdx) TransactionType.DEBIT else TransactionType.CREDIT
            }
            firstDebitIdx != null -> TransactionType.DEBIT
            firstCreditIdx != null -> TransactionType.CREDIT
            else -> null
        }
    }

    private fun extractPrimaryAmount(body: String): Pair<Double, String>? {
        val balanceMatchStart = balanceContextPattern.find(body)?.range?.first
        val candidates = amountPattern.findAll(body).mapNotNull { match ->
            val rawCurr = match.groupValues[1].ifBlank { match.groupValues[4] }
            val rawNum = match.groupValues[2].ifBlank { match.groupValues[3] }
            val value = rawNum.replace(",", "").toDoubleOrNull() ?: return@mapNotNull null
            val currency = normalizeCurrency(rawCurr)
            Triple(match.range.first, value, currency)
        }.toList()

        if (candidates.isEmpty()) return null

        // Prefer an amount that appears before the "Avl Bal / Available Balance" section.
        val beforeBalance = if (balanceMatchStart != null) {
            candidates.firstOrNull { it.first < balanceMatchStart }
        } else {
            null
        }
        val chosen = beforeBalance ?: candidates.first()
        return chosen.second to chosen.third
    }

    private fun extractBalance(body: String): Double? {
        val balCtx = balanceContextPattern.find(body) ?: return null
        val tail = body.substring(balCtx.range.last + 1)
        val amtMatch = amountPattern.find(tail) ?: return null
        if (amtMatch.range.first > 12) return null
        val rawNum = amtMatch.groupValues[2].ifBlank { amtMatch.groupValues[3] }
        return rawNum.replace(",", "").toDoubleOrNull()
    }

    private fun extractAccountLast4(body: String): String? {
        return accountLast4Pattern.find(body)?.groupValues?.get(1)
    }

    private fun extractMerchant(body: String, type: TransactionType): String {
        for (pattern in merchantPatterns) {
            val match = pattern.find(body) ?: continue
            val candidate = match.groupValues[1]
                .trim()
                .trimEnd('-', '*', '/', '.', ':')
                .replace(Regex("\\s+"), " ")
            if (candidate.length >= 2 && !candidate.all { it.isDigit() }) {
                return candidate
            }
        }
        return if (type == TransactionType.DEBIT) "Bank Expense" else "Bank Credit"
    }

    private fun classifyCategory(
        merchant: String,
        body: String,
        type: TransactionType
    ): ExpenseCategory {
        val combined = "${merchant.lowercase(Locale.ROOT)} ${body.lowercase(Locale.ROOT)}"
        for ((category, keywords) in categoryRules) {
            if (keywords.any { combined.contains(it) }) {
                return category
            }
        }
        return if (type == TransactionType.CREDIT) ExpenseCategory.TRANSFER else ExpenseCategory.OTHER
    }

    private fun normalizeCurrency(raw: String): String {
        return when (raw.lowercase(Locale.ROOT).trim('.')) {
            "rs", "inr", "₹" -> "₹"
            "usd", "$" -> "$"
            "eur", "€" -> "€"
            "gbp", "£" -> "£"
            else -> "₹"
        }
    }

    private fun buildFingerprint(
        bankName: String,
        type: TransactionType,
        amount: Double,
        accountLast4: String?,
        timestamp: Long,
        rawBody: String
    ): String {
        val minuteBucket = timestamp / 60_000L
        val key = "$bankName|${type.name}|${"%.2f".format(Locale.US, amount)}|${accountLast4.orEmpty()}|$minuteBucket|${rawBody.take(64)}"
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

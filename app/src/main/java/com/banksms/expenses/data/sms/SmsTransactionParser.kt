package com.banksms.expenses.data.sms

import com.banksms.expenses.data.model.AccountType
import com.banksms.expenses.data.model.CategorySource
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.ParseTemplateEntity
import com.banksms.expenses.data.model.TransactionChannel
import com.banksms.expenses.data.model.TransactionType
import java.security.MessageDigest
import java.util.Locale

data class ParsedSmsTransaction(
    val smsId: Long?,
    val sender: String,
    val bankName: String,
    val accountLast4: String?,
    val accountType: AccountType = AccountType.SAVINGS,
    val amount: Double,
    val currency: String,
    val type: TransactionType,
    val merchant: String,
    val rawMerchant: String = merchant,
    val category: ExpenseCategory,
    val categoryId: Long = category.id,
    val categorySource: CategorySource = CategorySource.BUILTIN,
    val channel: TransactionChannel = TransactionChannel.OTHER,
    val refNo: String? = null,
    val balanceAfter: Double? = null,
    val creditLimitAfter: Double? = null,
    val timestamp: Long,
    val rawBody: String,
    val fingerprint: String,
    val smsHash: String = "",
    val confidence: Float = 0.85f,
    val isInternational: Boolean = false
)

sealed interface SmsParseOutcome {
    data class Parsed(val transaction: ParsedSmsTransaction) : SmsParseOutcome
    data class BalanceOnly(
        val bankName: String,
        val accountLast4: String?,
        val accountType: AccountType,
        val balance: Double,
        val creditLimit: Double?,
        val currency: String
    ) : SmsParseOutcome
    data class Ignored(val reason: String) : SmsParseOutcome
    data class Unparsed(val reason: String, val bankName: String) : SmsParseOutcome
}

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

    private val financialCandidateKeywords = listOf(
        "debited", "credited", "spent", "withdrawn", "paid", "upi",
        "a/c", "acct", "account", "card", "inr", "rs.", "rs ", "₹",
        "txn", "amt", "bal", "ac="
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
        """(?i)(?:avl\.?\s*bal|available\s+bal(?:ance)?|clear\s+bal|current\s+bal(?:ance)?|bal(?:ance)?)\s*(?:is)?[:\s\-]*"""
    )

    private val limitContextPattern = Regex(
        """(?i)(?:avl\.?\s*lmt|available\s+(?:credit\s+)?limit|credit\s+limit)\s*(?:is)?[:\s\-]*"""
    )

    private val accountLast4Pattern = Regex(
        """(?i)(?:a/c|acct|account|card|xx|x\*+|ending\s+(?:with|in)?)\s*(?:no\.?\s*)?(?:[xX*]{1,8}|ending\s*)?(\d{4})\b"""
    )

    private val refNoPattern = Regex(
        """(?i)\b(?:upi\s*ref(?:\s*no)?|utr|imps\s*ref|neft\s*ref|ref(?:\s*no)?|txn\s*(?:id|ref))[:.\s#-]*([A-Za-z0-9]{6,22})\b"""
    )

    private val merchantPatterns = listOf(
        Regex("""(?i)\bVPA\s+([A-Za-z0-9._-]+@[A-Za-z0-9._-]+)"""),
        Regex("""(?i);\s*([A-Za-z0-9][A-Za-z0-9 &._*/@-]{1,40}?)\s+credited\b"""),
        Regex("""(?i)\b(?:at|to|towards|for|by)\s+(?!(?:your|a/c|acct|account|card|rs\.?|inr|usd|\d{2}[-/]\d{2}))([A-Za-z0-9][A-Za-z0-9 &._*/@-]{1,40}?)(?=\s+(?:on|via|ref|utr|avl|bal|if|not|through|using|from)\b|[.,;]|$)"""),
        Regex("""(?i)\bInfo[:\-\s]+([A-Za-z0-9][A-Za-z0-9 &._*/@-]{1,40}?)(?=\s+(?:on|avl|bal|ref)\b|[.,;]|$)""")
    )

    fun parse(
        smsId: Long? = null,
        sender: String,
        body: String,
        timestamp: Long,
        customTemplates: List<ParseTemplateEntity> = emptyList()
    ): ParsedSmsTransaction? {
        return when (val outcome = parseDetailed(smsId, sender, body, timestamp, customTemplates)) {
            is SmsParseOutcome.Parsed -> outcome.transaction
            else -> null
        }
    }

    fun parseDetailed(
        smsId: Long? = null,
        sender: String,
        body: String,
        timestamp: Long,
        customTemplates: List<ParseTemplateEntity> = emptyList(),
        ignoredSenderCodes: Set<String> = emptySet(),
        includedSenderCodes: Set<String> = emptySet()
    ): SmsParseOutcome {
        val cleanedBody = normalizeBody(body)
        val normSender = BankDirectory.normalizeSenderCode(sender)
        if (normSender in ignoredSenderCodes) {
            return SmsParseOutcome.Ignored("Ignored sender")
        }
        if (cleanedBody.length < 12) {
            return SmsParseOutcome.Ignored("Too short")
        }

        val lowerBody = cleanedBody.lowercase(Locale.ROOT)
        if (ignoreKeywords.any { lowerBody.contains(it) }) {
            return SmsParseOutcome.Ignored("Non-transaction alert (OTP/promo/failed/reminder)")
        }

        val bankInfo = BankDirectory.resolveBank(sender, cleanedBody)
        val accountType = BankDirectory.detectAccountType(cleanedBody, sender)
        val accountLast4 = extractAccountLast4(cleanedBody)
        val balanceAfter = extractMoneyAfterContext(cleanedBody, balanceContextPattern)
        val creditLimitAfter = extractMoneyAfterContext(cleanedBody, limitContextPattern)
        val refNo = extractRefNo(cleanedBody)
        val channel = detectChannel(lowerBody)
        val smsHash = computeSmsHash(sender, timestamp, cleanedBody)

        // 1. Try custom user templates first
        tryParseWithTemplates(
            smsId = smsId,
            sender = sender,
            normSender = normSender,
            cleanedBody = cleanedBody,
            rawBody = body,
            lowerBody = lowerBody,
            timestamp = timestamp,
            bankInfo = bankInfo,
            accountType = accountType,
            accountLast4 = accountLast4,
            balanceAfter = balanceAfter,
            creditLimitAfter = creditLimitAfter,
            refNo = refNo,
            channel = channel,
            smsHash = smsHash,
            templates = customTemplates
        )?.let { return SmsParseOutcome.Parsed(it) }

        // 2. Generic & bank pattern extraction
        val type = detectTransactionType(lowerBody)
        if (type == null) {
            val balCtx = balanceContextPattern.find(cleanedBody)
            val fallbackBal = if (balCtx != null) {
                balanceAfter ?: amountPattern.find(cleanedBody.substring(balCtx.range.last + 1))?.let { m ->
                    m.groupValues[2].ifBlank { m.groupValues[3] }.replace(",", "").toDoubleOrNull()
                }
            } else {
                null
            }
            if (fallbackBal != null) {
                val currMatch = amountPattern.find(cleanedBody)?.let {
                    normalizeCurrency(it.groupValues[1].ifBlank { it.groupValues[4] })
                } ?: "₹"
                return SmsParseOutcome.BalanceOnly(
                    bankName = bankInfo.canonicalName,
                    accountLast4 = accountLast4,
                    accountType = accountType,
                    balance = fallbackBal,
                    creditLimit = creditLimitAfter,
                    currency = currMatch
                )
            }
            return classifyFailure(sender, cleanedBody, lowerBody, bankInfo.canonicalName, includedSenderCodes)
        }

        val amountResult = extractPrimaryAmount(cleanedBody)
            ?: return classifyFailure(sender, cleanedBody, lowerBody, bankInfo.canonicalName, includedSenderCodes)
        val (amount, currency) = amountResult
        if (amount <= 0.0) {
            return classifyFailure(sender, cleanedBody, lowerBody, bankInfo.canonicalName, includedSenderCodes)
        }

        val rawMerchant = extractMerchant(cleanedBody, type)
        val canonicalMerchant = CategorisationEngine.normalizeBuiltinMerchant(rawMerchant)
        val (category, categorySource) = CategorisationEngine.classifyBuiltin(canonicalMerchant, cleanedBody, type)
        val isInternational = currency != "₹" || lowerBody.contains("international") || lowerBody.contains("forex")

        val confidence = computeConfidence(
            isKnownBank = BankDirectory.isKnownBankSender(sender, cleanedBody),
            accountLast4 = accountLast4,
            merchant = canonicalMerchant,
            refNo = refNo,
            balanceAfter = balanceAfter,
            channel = channel
        )

        val fingerprint = buildFingerprint(
            bankName = bankInfo.canonicalName,
            type = type,
            amount = amount,
            accountLast4 = accountLast4,
            timestamp = timestamp,
            rawBody = cleanedBody
        )

        return SmsParseOutcome.Parsed(
            ParsedSmsTransaction(
                smsId = smsId,
                sender = sender,
                bankName = bankInfo.canonicalName,
                accountLast4 = accountLast4,
                accountType = accountType,
                amount = amount,
                currency = currency,
                type = type,
                merchant = canonicalMerchant,
                rawMerchant = rawMerchant,
                category = category,
                categoryId = category.id,
                categorySource = categorySource,
                channel = channel,
                refNo = refNo,
                balanceAfter = balanceAfter,
                creditLimitAfter = creditLimitAfter,
                timestamp = timestamp,
                rawBody = body,
                fingerprint = fingerprint,
                smsHash = smsHash,
                confidence = confidence,
                isInternational = isInternational
            )
        )
    }

    private fun tryParseWithTemplates(
        smsId: Long?,
        sender: String,
        normSender: String,
        cleanedBody: String,
        rawBody: String,
        lowerBody: String,
        timestamp: Long,
        bankInfo: BankInfo,
        accountType: AccountType,
        accountLast4: String?,
        balanceAfter: Double?,
        creditLimitAfter: Double?,
        refNo: String?,
        channel: TransactionChannel,
        smsHash: String,
        templates: List<ParseTemplateEntity>
    ): ParsedSmsTransaction? {
        for (template in templates) {
            if (!template.enabled) continue
            val senderMatch = template.senderPattern.isBlank() ||
                normSender.contains(template.senderPattern.uppercase(Locale.ROOT)) ||
                sender.contains(template.senderPattern, ignoreCase = true)
            if (!senderMatch) continue

            val regex = runCatching { Regex(template.regexPattern, RegexOption.IGNORE_CASE) }.getOrNull() ?: continue
            val match = regex.find(cleanedBody) ?: continue

            val amtStr = match.groups[template.amountGroup]?.value ?: continue
            val amount = amtStr.replace(",", "").toDoubleOrNull()?.takeIf { it > 0.0 } ?: continue
            val rawMerchant = template.merchantGroup?.let { idx ->
                match.groups[idx]?.value?.trim()
            }?.takeIf { it.isNotBlank() } ?: extractMerchant(cleanedBody, template.defaultDirection ?: TransactionType.DEBIT)
            val tplAccount = template.accountGroup?.let { idx -> match.groups[idx]?.value?.takeLast(4) } ?: accountLast4
            val tplBalance = template.balanceGroup?.let { idx ->
                match.groups[idx]?.value?.replace(",", "")?.toDoubleOrNull()
            } ?: balanceAfter
            val type = detectTransactionType(lowerBody) ?: template.defaultDirection ?: TransactionType.DEBIT
            val currency = amountPattern.find(cleanedBody)?.let {
                normalizeCurrency(it.groupValues[1].ifBlank { it.groupValues[4] })
            } ?: "₹"
            val canonicalMerchant = CategorisationEngine.normalizeBuiltinMerchant(rawMerchant)
            val (category, categorySource) = CategorisationEngine.classifyBuiltin(canonicalMerchant, cleanedBody, type)

            return ParsedSmsTransaction(
                smsId = smsId,
                sender = sender,
                bankName = bankInfo.canonicalName,
                accountLast4 = tplAccount,
                accountType = accountType,
                amount = amount,
                currency = currency,
                type = type,
                merchant = canonicalMerchant,
                rawMerchant = rawMerchant,
                category = category,
                categoryId = category.id,
                categorySource = categorySource,
                channel = channel,
                refNo = refNo,
                balanceAfter = tplBalance,
                creditLimitAfter = creditLimitAfter,
                timestamp = timestamp,
                rawBody = rawBody,
                fingerprint = buildFingerprint(bankInfo.canonicalName, type, amount, tplAccount, timestamp, cleanedBody),
                smsHash = smsHash,
                confidence = 0.95f,
                isInternational = currency != "₹"
            )
        }
        return null
    }

    private fun classifyFailure(
        sender: String,
        cleanedBody: String,
        lowerBody: String,
        bankName: String,
        includedSenderCodes: Set<String>
    ): SmsParseOutcome {
        val normSender = BankDirectory.normalizeSenderCode(sender)
        val isCandidate = normSender in includedSenderCodes ||
            BankDirectory.isKnownBankSender(sender, cleanedBody) ||
            financialCandidateKeywords.count { lowerBody.contains(it) } >= 2
        return if (isCandidate) {
            SmsParseOutcome.Unparsed("Could not extract transaction fields", bankName)
        } else {
            SmsParseOutcome.Ignored("Non-financial SMS")
        }
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
        val balStart = balanceContextPattern.find(body)?.range?.first
        val limStart = limitContextPattern.find(body)?.range?.first
        val earliestContext = listOfNotNull(balStart, limStart).minOrNull()

        val candidates = amountPattern.findAll(body).mapNotNull { match ->
            val rawCurr = match.groupValues[1].ifBlank { match.groupValues[4] }
            val rawNum = match.groupValues[2].ifBlank { match.groupValues[3] }
            val value = rawNum.replace(",", "").toDoubleOrNull() ?: return@mapNotNull null
            val currency = normalizeCurrency(rawCurr)
            Triple(match.range.first, value, currency)
        }.toList()

        if (candidates.isEmpty()) return null

        val beforeContext = if (earliestContext != null) {
            candidates.firstOrNull { it.first < earliestContext }
        } else {
            null
        }
        val chosen = beforeContext ?: candidates.first()
        return chosen.second to chosen.third
    }

    private fun extractMoneyAfterContext(body: String, contextPattern: Regex): Double? {
        val ctx = contextPattern.find(body) ?: return null
        val tail = body.substring(ctx.range.last + 1)
        val amtMatch = amountPattern.find(tail) ?: return null
        if (amtMatch.range.first > 14) return null
        val rawNum = amtMatch.groupValues[2].ifBlank { amtMatch.groupValues[3] }
        return rawNum.replace(",", "").toDoubleOrNull()
    }

    private fun extractAccountLast4(body: String): String? {
        return accountLast4Pattern.find(body)?.groupValues?.get(1)
    }

    private fun extractRefNo(body: String): String? {
        return refNoPattern.find(body)?.groupValues?.get(1)?.trim()
    }

    private fun detectChannel(lowerBody: String): TransactionChannel {
        return when {
            lowerBody.contains("atm") || lowerBody.contains("cash withdrawal") || lowerBody.contains("withdrawn at") -> TransactionChannel.ATM
            lowerBody.contains("upi") || lowerBody.contains("vpa") || lowerBody.contains("@") -> TransactionChannel.UPI
            lowerBody.contains("nach") || lowerBody.contains("ecs") || lowerBody.contains("ach") || lowerBody.contains("mandate") || lowerBody.contains("sip") -> TransactionChannel.NACH_ECS
            lowerBody.contains("neft") || lowerBody.contains("imps") || lowerBody.contains("rtgs") -> TransactionChannel.NEFT_IMPS
            lowerBody.contains("card") || lowerBody.contains("pos") || lowerBody.contains("swipe") -> TransactionChannel.CARD_POS
            lowerBody.contains("netbanking") || lowerBody.contains("net banking") -> TransactionChannel.NET_BANKING
            else -> TransactionChannel.OTHER
        }
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

    private fun computeConfidence(
        isKnownBank: Boolean,
        accountLast4: String?,
        merchant: String,
        refNo: String?,
        balanceAfter: Double?,
        channel: TransactionChannel
    ): Float {
        var score = 0.55f
        if (isKnownBank) score += 0.15f
        if (!accountLast4.isNullOrBlank()) score += 0.10f
        if (merchant != "Bank Expense" && merchant != "Bank Credit") score += 0.10f
        if (refNo != null || balanceAfter != null) score += 0.05f
        if (channel != TransactionChannel.OTHER) score += 0.05f
        return score.coerceIn(0f, 1f)
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

    fun normalizeBody(body: String): String {
        return body.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()
    }

    fun computeSmsHash(sender: String, timestamp: Long, normalizedBody: String): String {
        val normSender = BankDirectory.normalizeSenderCode(sender)
        val key = "$normSender|${timestamp / 1000L}|${normalizedBody.lowercase(Locale.ROOT)}"
        return sha256Hex(key)
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
        return sha256Hex(key)
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

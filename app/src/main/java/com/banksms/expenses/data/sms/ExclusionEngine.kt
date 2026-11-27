package com.banksms.expenses.data.sms

import com.banksms.expenses.data.model.AccountEntity
import com.banksms.expenses.data.model.AccountType
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ExclusionRuleEntity
import com.banksms.expenses.data.model.ExclusionSource
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionType
import java.util.Locale
import kotlin.math.abs

data class ExclusionEvaluationResult(
    val isExcluded: Boolean,
    val reason: ExclusionReason?,
    val source: ExclusionSource,
    val ruleId: Long? = null
)

data class ExclusionSuggestion(
    val transaction: TransactionEntity,
    val suggestedReason: ExclusionReason,
    val matchedKeyword: String,
    val suggestionKey: String
)

data class TransferPair(
    val debitTxnId: Long,
    val creditTxnId: Long,
    val reason: ExclusionReason
)

object ExclusionEngine {

    private data class BuiltinHeuristic(
        val keywords: List<String>,
        val reason: ExclusionReason
    )

    private val builtinHeuristics = listOf(
        BuiltinHeuristic(
            keywords = listOf(
                "sip", "mutual fund", "nach", "ach d-", "folio", "groww",
                "zerodha", "kuvera", "coin", "ppf", "nps", "epf", "fixed deposit", "fd created"
            ),
            reason = ExclusionReason.INVESTMENT
        ),
        BuiltinHeuristic(
            keywords = listOf(
                "credit card bill", "credit card payment", "card bill", "card payment",
                "bbps card", "cc payment", "cred club", "cred pay"
            ),
            reason = ExclusionReason.CARD_BILL_PAYMENT
        ),
        BuiltinHeuristic(
            keywords = listOf(
                "self transfer", "own account", "internal transfer", "trf to self"
            ),
            reason = ExclusionReason.SELF_TRANSFER
        ),
        BuiltinHeuristic(
            keywords = listOf(
                "loan emi", "emi principal", "loan repayment"
            ),
            reason = ExclusionReason.LOAN_EMI_PRINCIPAL
        )
    )

    fun evaluateRules(
        txn: TransactionEntity,
        userRules: List<ExclusionRuleEntity>
    ): ExclusionEvaluationResult {
        if (txn.exclusionSource == ExclusionSource.MANUAL) {
            return ExclusionEvaluationResult(
                isExcluded = txn.isExcluded,
                reason = txn.exclusionReason,
                source = ExclusionSource.MANUAL,
                ruleId = txn.exclusionRuleId
            )
        }

        for (rule in userRules) {
            if (!rule.enabled) continue
            if (matchesRule(rule, txn)) {
                return ExclusionEvaluationResult(
                    isExcluded = true,
                    reason = rule.reason,
                    source = ExclusionSource.USER_RULE,
                    ruleId = rule.id
                )
            }
        }

        if (txn.exclusionSource == ExclusionSource.AUTO_TRANSFER && txn.isExcluded) {
            return ExclusionEvaluationResult(
                isExcluded = true,
                reason = txn.exclusionReason,
                source = ExclusionSource.AUTO_TRANSFER
            )
        }

        return ExclusionEvaluationResult(
            isExcluded = false,
            reason = null,
            source = ExclusionSource.NONE
        )
    }

    fun matchesRule(rule: ExclusionRuleEntity, txn: TransactionEntity): Boolean {
        var hasCondition = false

        val merchantPat = rule.merchantPattern?.trim()
        if (!merchantPat.isNullOrEmpty()) {
            hasCondition = true
            val matched = txn.merchant.contains(merchantPat, ignoreCase = true) ||
                txn.rawMerchant.contains(merchantPat, ignoreCase = true)
            if (!matched) return false
        }

        val bodyKw = rule.bodyKeyword?.trim()
        if (!bodyKw.isNullOrEmpty()) {
            hasCondition = true
            if (!txn.rawBody.contains(bodyKw, ignoreCase = true)) return false
        }

        val regexPat = rule.regexPattern?.trim()
        if (!regexPat.isNullOrEmpty()) {
            hasCondition = true
            val regex = runCatching { Regex(regexPat, RegexOption.IGNORE_CASE) }.getOrNull() ?: return false
            if (!regex.containsMatchIn(txn.rawBody) && !regex.containsMatchIn(txn.merchant)) return false
        }

        if (rule.exactAmount != null) {
            hasCondition = true
            if (abs(txn.amount - rule.exactAmount) > rule.amountTolerance) return false
        }

        if (rule.accountId != null) {
            hasCondition = true
            if (txn.accountId != rule.accountId) return false
        }

        return hasCondition
    }

    fun findSuggestions(
        transactions: List<TransactionEntity>,
        dismissedKeys: Set<String>
    ): List<ExclusionSuggestion> {
        return transactions.mapNotNull { txn ->
            if (txn.isExcluded || txn.exclusionSource == ExclusionSource.MANUAL) return@mapNotNull null
            val text = "${txn.merchant} ${txn.rawBody}".lowercase(Locale.ROOT)
            for (heuristic in builtinHeuristics) {
                val matchedKw = heuristic.keywords.firstOrNull { text.contains(it) }
                if (matchedKw != null) {
                    val key = "${txn.fingerprint}|${heuristic.reason.name}"
                    if (key !in dismissedKeys) {
                        return@mapNotNull ExclusionSuggestion(
                            transaction = txn,
                            suggestedReason = heuristic.reason,
                            matchedKeyword = matchedKw,
                            suggestionKey = key
                        )
                    }
                }
            }
            null
        }
    }
}

object AutoTransferDetector {

    private val cardPaymentKeywords = listOf(
        "credit card", "card bill", "card payment", "cc payment", "cred"
    )

    fun detectTransfers(
        transactions: List<TransactionEntity>,
        accountsById: Map<Long, AccountEntity>,
        windowMillis: Long = 10 * 60_000L
    ): List<TransferPair> {
        val debits = transactions
            .filter {
                it.type == TransactionType.DEBIT &&
                    it.exclusionSource != ExclusionSource.MANUAL &&
                    it.matchedTransferId == null
            }
            .sortedBy { it.txnTime }
        val credits = transactions
            .filter {
                it.type == TransactionType.CREDIT &&
                    it.exclusionSource != ExclusionSource.MANUAL &&
                    it.matchedTransferId == null
            }
            .sortedBy { it.txnTime }

        val usedCreditIds = mutableSetOf<Long>()
        val pairs = mutableListOf<TransferPair>()

        for (debit in debits) {
            val debitBodyLower = debit.rawBody.lowercase(Locale.ROOT)
            val isCardPaymentDebit = cardPaymentKeywords.any { debitBodyLower.contains(it) }

            val candidate = credits.firstOrNull { credit ->
                if (credit.id in usedCreditIds) return@firstOrNull false
                if (!isDifferentAccount(debit, credit)) return@firstOrNull false
                if (abs(debit.amount - credit.amount) > 0.01) return@firstOrNull false

                val creditAccount = credit.accountId?.let { accountsById[it] }
                val isCardBillMatch = isCardPaymentDebit && creditAccount?.type == AccountType.CREDIT
                val effectiveWindow = if (isCardBillMatch) maxOf(windowMillis, 24 * 3_600_000L) else windowMillis
                abs(debit.txnTime - credit.txnTime) <= effectiveWindow
            }

            if (candidate != null) {
                usedCreditIds += candidate.id
                val creditAccount = candidate.accountId?.let { accountsById[it] }
                val reason = if (creditAccount?.type == AccountType.CREDIT || isCardPaymentDebit) {
                    ExclusionReason.CARD_BILL_PAYMENT
                } else {
                    ExclusionReason.SELF_TRANSFER
                }
                pairs += TransferPair(
                    debitTxnId = debit.id,
                    creditTxnId = candidate.id,
                    reason = reason
                )
            }
        }
        return pairs
    }

    private fun isDifferentAccount(a: TransactionEntity, b: TransactionEntity): Boolean {
        if (a.accountId != null && b.accountId != null) {
            return a.accountId != b.accountId
        }
        return !a.bankName.equals(b.bankName, ignoreCase = true) ||
            a.accountLast4 != b.accountLast4
    }
}

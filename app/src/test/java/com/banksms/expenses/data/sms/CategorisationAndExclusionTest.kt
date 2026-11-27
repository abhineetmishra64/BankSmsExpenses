package com.banksms.expenses.data.sms

import com.banksms.expenses.data.model.AccountEntity
import com.banksms.expenses.data.model.AccountType
import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.CategorySource
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ExclusionRuleEntity
import com.banksms.expenses.data.model.ExclusionSource
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.MerchantAliasEntity
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategorisationAndExclusionTest {

    private val validIds = CategorisationEngine.defaultCategoryEntities().map { it.id }.toSet()

    private fun makeTxn(
        id: Long = 1L,
        accountId: Long? = 10L,
        bankName: String = "HDFC Bank",
        last4: String? = "1234",
        amount: Double = 500.0,
        type: TransactionType = TransactionType.DEBIT,
        merchant: String = "Unknown Merchant",
        categoryId: Long = ExpenseCategory.UNCATEGORISED.id,
        categorySource: CategorySource = CategorySource.FALLBACK,
        exclusionSource: ExclusionSource = ExclusionSource.NONE,
        isExcluded: Boolean = false,
        rawBody: String = "Rs.$amount debited from $bankName A/c XX$last4 at $merchant",
        txnTime: Long = 1_700_000_000_000L
    ) = TransactionEntity(
        id = id,
        accountId = accountId,
        sender = "AD-HDFCBK",
        bankName = bankName,
        accountLast4 = last4,
        amount = amount,
        currency = "₹",
        type = type,
        merchant = merchant,
        rawMerchant = merchant,
        categoryId = categoryId,
        categorySource = categorySource,
        txnTime = txnTime,
        rawBody = rawBody,
        fingerprint = "fp-$id",
        isExcluded = isExcluded,
        exclusionSource = exclusionSource
    )

    @Test
    fun manualOverrideWinsOverUserRulesAndBuiltin() {
        val txn = makeTxn(
            merchant = "Swiggy",
            categoryId = ExpenseCategory.ENTERTAINMENT.id,
            categorySource = CategorySource.MANUAL
        )
        val rule = CategoryRuleEntity(
            id = 1L,
            name = "Swiggy is Groceries",
            priority = 1,
            categoryId = ExpenseCategory.GROCERIES.id,
            merchantPattern = "Swiggy"
        )

        val result = CategorisationEngine.categorise(txn, listOf(rule), emptyList(), validIds)
        assertEquals(ExpenseCategory.ENTERTAINMENT.id, result.categoryId)
        assertEquals(CategorySource.MANUAL, result.categorySource)
    }

    @Test
    fun userRulesAreEvaluatedInPriorityOrderAndWinOverAliasesAndBuiltin() {
        val txn = makeTxn(merchant = "SWIGGY*ORDER", amount = 1200.0)
        val lowPriorityRule = CategoryRuleEntity(
            id = 1L,
            name = "Low priority",
            priority = 20,
            categoryId = ExpenseCategory.SHOPPING.id,
            merchantPattern = "SWIGGY"
        )
        val highPriorityRule = CategoryRuleEntity(
            id = 2L,
            name = "High priority",
            priority = 5,
            categoryId = ExpenseCategory.GROCERIES.id,
            merchantPattern = "SWIGGY",
            minAmount = 1000.0
        )

        val result = CategorisationEngine.categorise(
            txn,
            listOf(lowPriorityRule, highPriorityRule),
            emptyList(),
            validIds
        )
        assertEquals(ExpenseCategory.GROCERIES.id, result.categoryId)
        assertEquals(CategorySource.RULE, result.categorySource)
        assertEquals(2L, result.matchedRuleId)
        assertEquals("Swiggy", result.resolvedMerchant)
    }

    @Test
    fun userAliasAssignsCanonicalNameAndDefaultCategory() {
        val txn = makeTxn(merchant = "BLUETOKAI*BLR", rawBody = "Paid Rs 350 to BLUETOKAI*BLR")
        val alias = MerchantAliasEntity(
            id = 1L,
            rawPattern = "BLUETOKAI",
            canonicalName = "Blue Tokai",
            defaultCategoryId = ExpenseCategory.FOOD.id
        )

        val result = CategorisationEngine.categorise(txn, emptyList(), listOf(alias), validIds)
        assertEquals("Blue Tokai", result.resolvedMerchant)
        assertEquals(ExpenseCategory.FOOD.id, result.categoryId)
        assertEquals(CategorySource.ALIAS, result.categorySource)
    }

    @Test
    fun fallsBackToUncategorisedWhenNoRuleOrKeywordMatches() {
        val txn = makeTxn(merchant = "Ramesh Enterprises", rawBody = "Paid Rs 900 to Ramesh Enterprises")
        val result = CategorisationEngine.categorise(txn, emptyList(), emptyList(), validIds)

        assertEquals(ExpenseCategory.UNCATEGORISED.id, result.categoryId)
        assertEquals(CategorySource.FALLBACK, result.categorySource)
    }

    @Test
    fun exclusionRulesRespectManualOverrideAndMatchAmountTolerance() {
        val sipTxn = makeTxn(
            id = 1L,
            merchant = "Zerodha Coin",
            amount = 5000.0,
            rawBody = "Rs.5000 debited towards ACH D- Mutual Fund Zerodha Coin"
        )
        val rule = ExclusionRuleEntity(
            id = 11L,
            name = "SIP 5000",
            merchantPattern = "Zerodha",
            exactAmount = 5000.0,
            reason = ExclusionReason.INVESTMENT
        )

        val eval = ExclusionEngine.evaluateRules(sipTxn, listOf(rule))
        assertTrue(eval.isExcluded)
        assertEquals(ExclusionReason.INVESTMENT, eval.reason)
        assertEquals(ExclusionSource.USER_RULE, eval.source)

        val manuallyIncluded = sipTxn.copy(isExcluded = false, exclusionSource = ExclusionSource.MANUAL)
        val manualEval = ExclusionEngine.evaluateRules(manuallyIncluded, listOf(rule))
        assertFalse(manualEval.isExcluded)
        assertEquals(ExclusionSource.MANUAL, manualEval.source)
    }

    @Test
    fun findsBuiltinExclusionSuggestionsAndRespectsDismissals() {
        val sip = makeTxn(id = 1L, merchant = "HDFC MF", rawBody = "Rs.5000 debited for Mutual Fund SIP")
        val food = makeTxn(id = 2L, merchant = "Swiggy", rawBody = "Rs.400 spent at Swiggy")

        val suggestions = ExclusionEngine.findSuggestions(listOf(sip, food), emptySet())
        assertEquals(1, suggestions.size)
        assertEquals(ExclusionReason.INVESTMENT, suggestions.first().suggestedReason)

        val afterDismiss = ExclusionEngine.findSuggestions(
            listOf(sip, food),
            setOf(suggestions.first().suggestionKey)
        )
        assertTrue(afterDismiss.isEmpty())
    }

    @Test
    fun autoTransferDetectorFlagsSelfTransfersAndCardBillPaymentsWithinWindow() {
        val t0 = 1_700_000_000_000L
        val accounts = mapOf(
            1L to AccountEntity(1L, "HDFC Savings ••1234", "HDFC Bank", AccountType.SAVINGS, "1234", accentColorHex = 0L),
            2L to AccountEntity(2L, "ICICI Savings ••4521", "ICICI Bank", AccountType.SAVINGS, "4521", accentColorHex = 0L),
            3L to AccountEntity(3L, "HDFC Credit ••8812", "HDFC Bank", AccountType.CREDIT, "8812", accentColorHex = 0L)
        )

        val selfDebit = makeTxn(id = 10L, accountId = 1L, bankName = "HDFC Bank", last4 = "1234", amount = 10000.0, type = TransactionType.DEBIT, txnTime = t0)
        val selfCredit = makeTxn(id = 11L, accountId = 2L, bankName = "ICICI Bank", last4 = "4521", amount = 10000.0, type = TransactionType.CREDIT, txnTime = t0 + 3 * 60_000L)

        val cardBillDebit = makeTxn(
            id = 20L, accountId = 1L, bankName = "HDFC Bank", last4 = "1234", amount = 12000.0,
            type = TransactionType.DEBIT, rawBody = "HDFC Card Bill Payment Rs.12,000", txnTime = t0 + 100_000L
        )
        val cardBillCredit = makeTxn(
            id = 21L, accountId = 3L, bankName = "HDFC Bank", last4 = "8812", amount = 12000.0,
            type = TransactionType.CREDIT, rawBody = "Payment received on Credit Card xx8812", txnTime = t0 + 100_000L + 2 * 3_600_000L
        )

        val unrelatedCredit = makeTxn(id = 30L, accountId = 2L, bankName = "ICICI Bank", last4 = "4521", amount = 5000.0, type = TransactionType.CREDIT, txnTime = t0)
        val tooLateDebit = makeTxn(id = 31L, accountId = 1L, bankName = "HDFC Bank", last4 = "1234", amount = 5000.0, type = TransactionType.DEBIT, txnTime = t0 + 30 * 60_000L)

        val pairs = AutoTransferDetector.detectTransfers(
            listOf(selfDebit, selfCredit, cardBillDebit, cardBillCredit, unrelatedCredit, tooLateDebit),
            accounts,
            windowMillis = 10 * 60_000L
        )

        assertEquals(2, pairs.size)
        assertTrue(pairs.any { it.debitTxnId == 10L && it.creditTxnId == 11L && it.reason == ExclusionReason.SELF_TRANSFER })
        assertTrue(pairs.any { it.debitTxnId == 20L && it.creditTxnId == 21L && it.reason == ExclusionReason.CARD_BILL_PAYMENT })
    }
}

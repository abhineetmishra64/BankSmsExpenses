package com.banksms.expenses.data.repository

import com.banksms.expenses.data.model.CategorySource
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ExclusionSource
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.ParseStatus
import com.banksms.expenses.data.model.ParseTemplateEntity
import com.banksms.expenses.data.model.TransactionType
import com.banksms.expenses.data.sms.RawSmsMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExpenseRepositoryTest {

    private lateinit var txDao: InMemoryTransactionDao
    private lateinit var smsRawDao: InMemorySmsRawDao
    private lateinit var accountDao: InMemoryAccountDao
    private lateinit var categoryDao: InMemoryCategoryDao
    private lateinit var rulesDao: InMemoryRulesDao
    private lateinit var budgetDao: InMemoryBudgetDao
    private lateinit var settingsDao: InMemorySettingsDao
    private lateinit var repo: ExpenseRepository

    @Before
    fun setup() {
        txDao = InMemoryTransactionDao()
        smsRawDao = InMemorySmsRawDao()
        accountDao = InMemoryAccountDao()
        categoryDao = InMemoryCategoryDao()
        rulesDao = InMemoryRulesDao()
        budgetDao = InMemoryBudgetDao()
        settingsDao = InMemorySettingsDao()
        repo = ExpenseRepository(
            transactionDao = txDao,
            smsRawDao = smsRawDao,
            accountDao = accountDao,
            categoryDao = categoryDao,
            rulesDao = rulesDao,
            budgetDao = budgetDao,
            settingsDao = settingsDao,
            smsReader = null,
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    @Test
    fun ingestsSmsAutoCreatesAccountsDeduplicatesByRefNoAndFlagsAutoTransfers() = runBlocking {
        val t0 = 1_700_000_000_000L
        val messages = listOf(
            RawSmsMessage(1L, "AD-HDFCBK", "Amt Sent Rs.500.00 from HDFC Bank A/c XX1234 to Swiggy on 26-11-26 UPI Ref 1122334455. Avl Bal Rs.10,000.00", t0),
            // Duplicate alert 2 minutes later with same RefNo + amount + account
            RawSmsMessage(2L, "AD-HDFCBK", "Rs 500.00 debited from HDFC A/c 1234 to Swiggy UPI Ref 1122334455", t0 + 120_000L),
            // Self transfer pair across HDFC XX1234 -> ICICI XX4521 within 3 mins
            RawSmsMessage(3L, "AD-HDFCBK", "Amt Sent Rs.10,000.00 from HDFC Bank A/c XX1234 to Own Account on 26-11-26.", t0 + 300_000L),
            RawSmsMessage(4L, "VK-ICICIB", "ICICI Bank Acct XX4521 credited with Rs 10,000.00 on 26-11-26 from HDFC.", t0 + 420_000L)
        )

        val res = repo.ingestMessages(messages)
        assertEquals(4, res.scannedMessages)
        assertEquals(3, res.newlyAdded)

        val txns = repo.transactions.first()
        assertEquals(3, txns.size)

        val accounts = repo.accounts.first()
        assertEquals(2, accounts.size)

        val excluded = txns.filter { it.isExcluded }
        assertEquals(2, excluded.size)
        assertTrue(excluded.all { it.exclusionSource == ExclusionSource.AUTO_TRANSFER })
    }

    @Test
    fun exclusionRuleAppliesRetroactivelyToFutureSmsAndResetRestoresAll() = runBlocking {
        val t0 = 1_700_000_000_000L
        repo.ingestMessages(
            listOf(
                RawSmsMessage(1L, "AD-HDFCBK", "Rs.5,000.00 debited from HDFC Bank A/c XX1234 towards BlueChipMF on 01-11-26.", t0),
                RawSmsMessage(2L, "AD-HDFCBK", "Rs.450.00 spent on HDFC Bank A/c XX1234 at Swiggy on 02-11-26.", t0 + 86_400_000L)
            )
        )

        val ruleId = repo.createExclusionRuleAndApply(
            name = "Exclude BlueChipMF",
            reason = ExclusionReason.INVESTMENT,
            merchantPattern = "BlueChipMF",
            applyRetroactively = true
        )

        // Past SIP excluded
        var txns = repo.transactions.first()
        assertTrue(txns.first { it.merchant == "BlueChipMF" }.isExcluded)
        assertFalse(txns.first { it.merchant == "Swiggy" }.isExcluded)

        // Future SIP SMS arrives -> automatically excluded
        repo.ingestMessages(
            listOf(
                RawSmsMessage(3L, "AD-HDFCBK", "Rs.5,000.00 debited from HDFC Bank A/c XX1234 towards BlueChipMF on 01-12-26.", t0 + 30 * 86_400_000L)
            )
        )
        txns = repo.transactions.first()
        assertEquals(2, txns.count { it.merchant == "BlueChipMF" && it.isExcluded })

        // Reset rule -> re-includes all matches
        repo.resetExclusionRule(ruleId)
        txns = repo.transactions.first()
        assertEquals(0, txns.count { it.isExcluded })
    }

    @Test
    fun recategoriseMerchantCreatesRuleDeletingRuleKeepsTransactionsAndReprocessRespectsManualOverride() = runBlocking {
        val t0 = 1_700_000_000_000L
        repo.ingestMessages(
            listOf(
                RawSmsMessage(1L, "VK-ICICIB", "ICICI Bank Acct XX4521 debited for Rs 350.00 on 10-11-26; BlueTokai credited.", t0),
                RawSmsMessage(2L, "VK-ICICIB", "ICICI Bank Acct XX4521 debited for Rs 420.00 on 12-11-26; BlueTokai credited.", t0 + 172_800_000L)
            )
        )

        var txns = repo.transactions.first()
        assertTrue(txns.all { it.categoryId == ExpenseCategory.UNCATEGORISED.id })

        // Manually override one transaction to Entertainment
        val firstId = txns.first().id
        repo.updateCategorySingle(firstId, ExpenseCategory.ENTERTAINMENT.id)

        // Always categorise BlueTokai as Food -> updates non-manual past + future, preserves manual override!
        val ruleId = repo.alwaysCategoriseMerchant("BlueTokai", ExpenseCategory.FOOD.id, applyToPast = true)

        repo.ingestMessages(
            listOf(
                RawSmsMessage(3L, "VK-ICICIB", "ICICI Bank Acct XX4521 debited for Rs 380.00 on 15-11-26; BlueTokai credited.", t0 + 400_000_000L)
            )
        )

        txns = repo.transactions.first()
        assertEquals(3, txns.size)
        assertEquals(ExpenseCategory.ENTERTAINMENT.id, txns.first { it.id == firstId }.categoryId)
        assertEquals(CategorySource.MANUAL, txns.first { it.id == firstId }.categorySource)
        assertEquals(2, txns.count { it.categoryId == ExpenseCategory.FOOD.id })

        // Reprocess never overwrites manual decision
        repo.reprocessAllSms()
        txns = repo.transactions.first()
        assertEquals(ExpenseCategory.ENTERTAINMENT.id, txns.first { it.id == firstId }.categoryId)

        // Deleting rule never deletes transactions (Spec 12)
        repo.deleteCategoryRule(ruleId)
        assertEquals(3, repo.transactions.first().size)
    }

    @Test
    fun templateBuilderAndReprocessConvertsUnparsedSmsIntoTransaction() = runBlocking {
        val t0 = 1_700_000_000_000L
        val res = repo.ingestMessages(
            listOf(
                RawSmsMessage(10L, "AX-CUSTOMBK", "CUSTOMTXN|AMT=1250.00|TO=UrbanClap|AC=7788", t0)
            )
        )
        assertEquals(0, res.newlyAdded)
        assertEquals(1, res.unparsedCount)
        assertEquals(ParseStatus.UNPARSED, repo.rawSmsMessages.first().first().parseStatus)

        repo.saveParseTemplate(
            ParseTemplateEntity(
                name = "CustomBk",
                senderPattern = "CUSTOMBK",
                regexPattern = """CUSTOMTXN\|AMT=([0-9.]+)\|TO=([A-Za-z0-9]+)\|AC=(\d{4})""",
                amountGroup = 1,
                merchantGroup = 2,
                accountGroup = 3,
                defaultDirection = TransactionType.DEBIT
            )
        )

        val reRes = repo.reprocessAllSms()
        assertEquals(1, reRes.newlyAdded)
        val txns = repo.transactions.first()
        assertEquals(1, txns.size)
        assertEquals("UrbanClap", txns.first().merchant)
        assertEquals(1250.0, txns.first().amount, 0.001)
    }

    @Test
    fun persistsSettingsAndRestoresEncryptedBackupIntoFreshRepository() = runBlocking {
        repo.setSelectedAccountIds(setOf(1L, 3L))
        repo.setMonthStartDay(25)
        repo.setPrivacyMode(true)
        repo.setFlagSecure(true)

        val prefs = ExpenseRepository.parsePreferences(repo.settings.first())
        assertEquals(setOf(1L, 3L), prefs.selectedAccountIds)
        assertEquals(25, prefs.monthStartDay)
        assertTrue(prefs.privacyMode)
        assertTrue(prefs.flagSecure)

        repo.addManualTransaction("Local Bakery", 240.0, TransactionType.DEBIT, ExpenseCategory.FOOD.id, null, 1_700_000_000_000L, "Cash", "bakery")
        val encrypted = repo.exportEncryptedBackup("my-pass")

        val freshRepo = ExpenseRepository(
            InMemoryTransactionDao(),
            InMemorySmsRawDao(),
            InMemoryAccountDao(),
            InMemoryCategoryDao(),
            InMemoryRulesDao(),
            InMemoryBudgetDao(),
            InMemorySettingsDao(),
            ioDispatcher = Dispatchers.Unconfined
        )
        val restoredCount = freshRepo.importEncryptedBackup(encrypted, "my-pass")
        assertEquals(1, restoredCount)
        assertEquals("Local Bakery", freshRepo.transactions.first().first().merchant)
    }
}

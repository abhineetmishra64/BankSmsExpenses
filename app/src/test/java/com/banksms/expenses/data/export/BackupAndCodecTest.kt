package com.banksms.expenses.data.export

import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ExclusionRuleEntity
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.MerchantAliasEntity
import com.banksms.expenses.data.model.ParseTemplateEntity
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackupAndCodecTest {

    @Test
    fun csvExporterEscapesCommasAndQuotesProperly() {
        val txn = TransactionEntity(
            id = 1L,
            sender = "AD-HDFCBK",
            bankName = "HDFC Bank",
            accountLast4 = "1234",
            amount = 485.50,
            currency = "₹",
            type = TransactionType.DEBIT,
            merchant = "Cafe, \"Test\"",
            categoryId = ExpenseCategory.FOOD.id,
            txnTime = 1_700_000_000_000L,
            rawBody = "body",
            fingerprint = "fp-1",
            note = "Lunch, team"
        )

        val csv = CsvExporter.exportTransactions(listOf(txn), mapOf(ExpenseCategory.FOOD.id to "Food & Dining"))
        assertTrue(csv.contains("\"Cafe, \"\"Test\"\"\""))
        assertTrue(csv.contains("\"Lunch, team\""))
    }

    @Test
    fun rulesJsonRoundTripPreservesAllRuleTypes() {
        val bundle = RulesBundle(
            categoryRules = listOf(
                CategoryRuleEntity(
                    name = "Coffee",
                    priority = 10,
                    categoryId = ExpenseCategory.FOOD.id,
                    merchantPattern = "Blue Tokai",
                    minAmount = 50.0,
                    maxAmount = 2000.0
                )
            ),
            exclusionRules = listOf(
                ExclusionRuleEntity(
                    name = "SIP",
                    bodyKeyword = "ACH D-",
                    exactAmount = 5000.0,
                    reason = ExclusionReason.INVESTMENT
                )
            ),
            aliases = listOf(
                MerchantAliasEntity(rawPattern = "AMZN", canonicalName = "Amazon", defaultCategoryId = ExpenseCategory.SHOPPING.id)
            ),
            templates = listOf(
                ParseTemplateEntity(name = "T1", senderPattern = "MYBNK", regexPattern = "Rs ([0-9]+)", amountGroup = 1)
            )
        )

        val json = RulesAndBackupCodec.exportRulesJson(bundle)
        val imported = RulesAndBackupCodec.importRulesJson(json)

        assertEquals(1, imported.categoryRules.size)
        assertEquals("Coffee", imported.categoryRules.first().name)
        assertEquals(1, imported.exclusionRules.size)
        assertEquals(ExclusionReason.INVESTMENT, imported.exclusionRules.first().reason)
        assertEquals(1, imported.aliases.size)
        assertEquals("Amazon", imported.aliases.first().canonicalName)
        assertEquals(1, imported.templates.size)
        assertEquals("MYBNK", imported.templates.first().senderPattern)
    }

    @Test
    fun encryptedBackupRoundTripSucceedsWithCorrectPasswordAndFailsWithWrongPassword() {
        val txn = TransactionEntity(
            id = 1L,
            sender = "AD-HDFCBK",
            bankName = "HDFC Bank",
            accountLast4 = "1234",
            amount = 999.0,
            currency = "₹",
            type = TransactionType.DEBIT,
            merchant = "Amazon",
            categoryId = ExpenseCategory.SHOPPING.id,
            txnTime = 1_700_000_000_000L,
            rawBody = "Spent Rs.999 at Amazon",
            fingerprint = "fp-999"
        )
        val json = RulesAndBackupCodec.exportBackupJson(listOf(txn), RulesBundle(emptyList(), emptyList(), emptyList(), emptyList()))
        val encrypted = BackupCrypto.encrypt(json, "strong-secret-123")

        val decrypted = BackupCrypto.decrypt(encrypted, "strong-secret-123")
        val (restoredTxns, _) = RulesAndBackupCodec.importBackupJson(decrypted)
        assertEquals(1, restoredTxns.size)
        assertEquals("Amazon", restoredTxns.first().merchant)
        assertEquals(999.0, restoredTxns.first().amount, 0.001)

        try {
            BackupCrypto.decrypt(encrypted, "wrong-password")
            fail("Expected decryption to fail with wrong password")
        } catch (_: Exception) {
            // Expected
        }
    }
}

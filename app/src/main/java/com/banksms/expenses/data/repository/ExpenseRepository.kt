package com.banksms.expenses.data.repository

import com.banksms.expenses.data.export.BackupCrypto
import com.banksms.expenses.data.export.CsvExporter
import com.banksms.expenses.data.export.RulesAndBackupCodec
import com.banksms.expenses.data.export.RulesBundle
import com.banksms.expenses.data.local.AccountDao
import com.banksms.expenses.data.local.BudgetDao
import com.banksms.expenses.data.local.CategoryDao
import com.banksms.expenses.data.local.RulesDao
import com.banksms.expenses.data.local.SettingsDao
import com.banksms.expenses.data.local.SmsRawDao
import com.banksms.expenses.data.local.TransactionDao
import com.banksms.expenses.data.model.AccountEntity
import com.banksms.expenses.data.model.AccountType
import com.banksms.expenses.data.model.AppSettingEntity
import com.banksms.expenses.data.model.BudgetEntity
import com.banksms.expenses.data.model.CategoryEntity
import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.CategorySource
import com.banksms.expenses.data.model.DismissedExclusionSuggestionEntity
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ExclusionRuleEntity
import com.banksms.expenses.data.model.ExclusionSource
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.MerchantAliasEntity
import com.banksms.expenses.data.model.ParseStatus
import com.banksms.expenses.data.model.ParseTemplateEntity
import com.banksms.expenses.data.model.SenderRuleEntity
import com.banksms.expenses.data.model.SmsRawEntity
import com.banksms.expenses.data.model.TransactionChannel
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionSplitEntity
import com.banksms.expenses.data.model.TransactionType
import com.banksms.expenses.data.sms.AutoTransferDetector
import com.banksms.expenses.data.sms.BankDirectory
import com.banksms.expenses.data.sms.CategorisationEngine
import com.banksms.expenses.data.sms.ExclusionEngine
import com.banksms.expenses.data.sms.ParsedSmsTransaction
import com.banksms.expenses.data.sms.RawSmsMessage
import com.banksms.expenses.data.sms.SmsParseOutcome
import com.banksms.expenses.data.sms.SmsReader
import com.banksms.expenses.data.sms.SmsTransactionParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.abs

data class SyncResult(
    val scannedMessages: Int,
    val parsedTransactions: Int,
    val newlyAdded: Int,
    val unparsedCount: Int = 0
)

enum class ThemePreference(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

data class AppPreferences(
    val selectedAccountIds: Set<Long> = emptySet(),
    val monthStartDay: Int = 1,
    val financialYearMode: Boolean = false,
    val autoTransferWindowMinutes: Int = 10,
    val privacyMode: Boolean = false,
    val flagSecure: Boolean = false,
    val themeMode: ThemePreference = ThemePreference.SYSTEM,
    val pinHash: String? = null,
    val onboardingDone: Boolean = false
)

object SettingKeys {
    const val SELECTED_ACCOUNT_IDS = "selected_account_ids"
    const val MONTH_START_DAY = "month_start_day"
    const val FINANCIAL_YEAR_MODE = "financial_year_mode"
    const val AUTO_TRANSFER_WINDOW_MIN = "auto_transfer_window_min"
    const val PRIVACY_MODE = "privacy_mode"
    const val FLAG_SECURE = "flag_secure"
    const val THEME_MODE = "theme_mode"
    const val PIN_HASH = "pin_hash"
    const val ONBOARDING_DONE = "onboarding_done"
}

class ExpenseRepository(
    private val transactionDao: TransactionDao,
    private val smsRawDao: SmsRawDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val rulesDao: RulesDao,
    private val budgetDao: BudgetDao,
    private val settingsDao: SettingsDao,
    private val smsReader: SmsReader? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val transactions: Flow<List<TransactionEntity>> = transactionDao.observeAllTransactions()
    val rawSmsMessages: Flow<List<SmsRawEntity>> = smsRawDao.observeAll()
    val accounts: Flow<List<AccountEntity>> = accountDao.observeAll()
    val categories: Flow<List<CategoryEntity>> = categoryDao.observeAll()
    val categoryRules: Flow<List<CategoryRuleEntity>> = rulesDao.observeCategoryRules()
    val exclusionRules: Flow<List<ExclusionRuleEntity>> = rulesDao.observeExclusionRules()
    val aliases: Flow<List<MerchantAliasEntity>> = rulesDao.observeAliases()
    val parseTemplates: Flow<List<ParseTemplateEntity>> = rulesDao.observeParseTemplates()
    val senderRules: Flow<List<SenderRuleEntity>> = rulesDao.observeSenderRules()
    val dismissedSuggestions: Flow<List<DismissedExclusionSuggestionEntity>> = rulesDao.observeDismissedSuggestions()
    val budgets: Flow<List<BudgetEntity>> = budgetDao.observeBudgets()
    val splits: Flow<List<TransactionSplitEntity>> = budgetDao.observeSplits()
    val settings: Flow<List<AppSettingEntity>> = settingsDao.observeAll()

    suspend fun ensureInitialized() = withContext(ioDispatcher) {
        if (categoryDao.getAll().isEmpty()) {
            categoryDao.insertAll(CategorisationEngine.defaultCategoryEntities())
        }
    }

    suspend fun syncFromSmsInbox(sinceTimestampMillis: Long? = null): SyncResult = withContext(ioDispatcher) {
        val reader = smsReader ?: return@withContext SyncResult(0, 0, 0)
        val rawMessages = reader.readInboxMessages(sinceTimestampMillis)
        ingestMessages(rawMessages)
    }

    suspend fun loadSampleTransactions(): SyncResult = withContext(ioDispatcher) {
        val reader = smsReader ?: return@withContext SyncResult(0, 0, 0)
        val sampleMessages = reader.buildSampleMessages()
        ingestMessages(sampleMessages)
    }

    suspend fun ingestMessages(messages: List<RawSmsMessage>): SyncResult = withContext(ioDispatcher) {
        ensureInitialized()

        val senderRulesList = rulesDao.getSenderRules()
        val ignoredSenders = senderRulesList.filter { it.action == "IGNORE" }.map { it.senderCode }.toSet()
        val includedSenders = senderRulesList.filter { it.action == "INCLUDE" }.map { it.senderCode }.toSet()
        val templates = rulesDao.getParseTemplates()
        val userCategoryRules = rulesDao.getCategoryRules().toMutableList()
        val userExclusionRules = rulesDao.getExclusionRules().toMutableList()
        val userAliases = rulesDao.getAliases()
        val validCategoryIds = categoryDao.getAll().map { it.id }.toSet()
        val existingTxns = transactionDao.getAllTransactions().toMutableList()

        var parsedCount = 0
        var newlyAdded = 0
        var unparsedCount = 0

        for (msg in messages) {
            val cleanedBody = SmsTransactionParser.normalizeBody(msg.body)
            val hash = SmsTransactionParser.computeSmsHash(msg.sender, msg.timestamp, cleanedBody)
            if (smsRawDao.getByHash(hash) != null) continue

            when (
                val outcome = SmsTransactionParser.parseDetailed(
                    smsId = msg.id,
                    sender = msg.sender,
                    body = msg.body,
                    timestamp = msg.timestamp,
                    customTemplates = templates,
                    ignoredSenderCodes = ignoredSenders,
                    includedSenderCodes = includedSenders
                )
            ) {
                is SmsParseOutcome.Ignored -> {
                    smsRawDao.insert(
                        SmsRawEntity(
                            sender = msg.sender,
                            body = msg.body,
                            receivedAt = msg.timestamp,
                            hash = hash,
                            parseStatus = ParseStatus.IGNORED,
                            confidence = 0f,
                            failureReason = outcome.reason
                        )
                    )
                }

                is SmsParseOutcome.Unparsed -> {
                    unparsedCount++
                    smsRawDao.insert(
                        SmsRawEntity(
                            sender = msg.sender,
                            body = msg.body,
                            receivedAt = msg.timestamp,
                            hash = hash,
                            parseStatus = ParseStatus.UNPARSED,
                            confidence = 0f,
                            failureReason = outcome.reason
                        )
                    )
                }

                is SmsParseOutcome.BalanceOnly -> {
                    smsRawDao.insert(
                        SmsRawEntity(
                            sender = msg.sender,
                            body = msg.body,
                            receivedAt = msg.timestamp,
                            hash = hash,
                            parseStatus = ParseStatus.BALANCE_ONLY,
                            confidence = 1f
                        )
                    )
                    resolveOrCreateAccount(
                        bankName = outcome.bankName,
                        last4 = outcome.accountLast4,
                        type = outcome.accountType,
                        balance = outcome.balance,
                        creditLimit = outcome.creditLimit
                    )
                }

                is SmsParseOutcome.Parsed -> {
                    parsedCount++
                    val p = outcome.transaction
                    val rawId = smsRawDao.insert(
                        SmsRawEntity(
                            sender = msg.sender,
                            body = msg.body,
                            receivedAt = msg.timestamp,
                            hash = hash,
                            parseStatus = ParseStatus.PARSED,
                            confidence = p.confidence
                        )
                    )

                    val account = resolveOrCreateAccount(
                        bankName = p.bankName,
                        last4 = p.accountLast4,
                        type = p.accountType,
                        balance = p.balanceAfter,
                        creditLimit = p.creditLimitAfter
                    )

                    if (isSecondaryDuplicate(p, existingTxns)) continue

                    val baseTxn = buildBaseTransaction(p, rawId.takeIf { it > 0 } ?: p.smsId, account.id)
                    val catResult = CategorisationEngine.categorise(baseTxn, userCategoryRules, userAliases, validCategoryIds)
                    if (catResult.matchedRuleId != null) {
                        bumpCategoryRuleMatch(catResult.matchedRuleId, userCategoryRules, p.timestamp)
                    }

                    var enrichedTxn = baseTxn.copy(
                        merchant = catResult.resolvedMerchant,
                        categoryId = catResult.categoryId,
                        categorySource = catResult.categorySource
                    )

                    val exclResult = ExclusionEngine.evaluateRules(enrichedTxn, userExclusionRules)
                    if (exclResult.isExcluded) {
                        enrichedTxn = enrichedTxn.copy(
                            isExcluded = true,
                            exclusionReason = exclResult.reason,
                            exclusionSource = exclResult.source,
                            exclusionRuleId = exclResult.ruleId
                        )
                        if (exclResult.ruleId != null) {
                            bumpExclusionRuleMatch(exclResult.ruleId, userExclusionRules, p.timestamp)
                        }
                    } else if (catResult.alsoExclude) {
                        enrichedTxn = enrichedTxn.copy(
                            isExcluded = true,
                            exclusionReason = catResult.exclusionReason ?: ExclusionReason.OTHER,
                            exclusionSource = ExclusionSource.USER_RULE
                        )
                    }

                    val insertedId = transactionDao.insert(enrichedTxn)
                    if (insertedId > 0L) {
                        newlyAdded++
                        existingTxns.add(enrichedTxn.copy(id = insertedId))
                    }
                }
            }
        }

        if (newlyAdded > 0) {
            runAutoTransferDetection()
        }

        SyncResult(
            scannedMessages = messages.size,
            parsedTransactions = parsedCount,
            newlyAdded = newlyAdded,
            unparsedCount = unparsedCount
        )
    }

    suspend fun reprocessAllSms(): SyncResult = withContext(ioDispatcher) {
        ensureInitialized()
        val rawList = smsRawDao.getAll()
        val senderRulesList = rulesDao.getSenderRules()
        val ignoredSenders = senderRulesList.filter { it.action == "IGNORE" }.map { it.senderCode }.toSet()
        val includedSenders = senderRulesList.filter { it.action == "INCLUDE" }.map { it.senderCode }.toSet()
        val templates = rulesDao.getParseTemplates()
        val userCategoryRules = rulesDao.getCategoryRules().toMutableList()
        val userExclusionRules = rulesDao.getExclusionRules().toMutableList()
        val userAliases = rulesDao.getAliases()
        val validCategoryIds = categoryDao.getAll().map { it.id }.toSet()
        val existingByFingerprint = transactionDao.getAllTransactions().associateBy { it.fingerprint }.toMutableMap()

        var parsedCount = 0
        var newlyAdded = 0
        var unparsedCount = 0

        for (raw in rawList) {
            when (
                val outcome = SmsTransactionParser.parseDetailed(
                    smsId = raw.id,
                    sender = raw.sender,
                    body = raw.body,
                    timestamp = raw.receivedAt,
                    customTemplates = templates,
                    ignoredSenderCodes = ignoredSenders,
                    includedSenderCodes = includedSenders
                )
            ) {
                is SmsParseOutcome.Ignored -> {
                    if (raw.parseStatus != ParseStatus.IGNORED) {
                        smsRawDao.update(raw.copy(parseStatus = ParseStatus.IGNORED, failureReason = outcome.reason))
                    }
                }

                is SmsParseOutcome.Unparsed -> {
                    unparsedCount++
                    if (raw.parseStatus != ParseStatus.UNPARSED) {
                        smsRawDao.update(raw.copy(parseStatus = ParseStatus.UNPARSED, failureReason = outcome.reason))
                    }
                }

                is SmsParseOutcome.BalanceOnly -> {
                    smsRawDao.update(raw.copy(parseStatus = ParseStatus.BALANCE_ONLY, confidence = 1f, failureReason = null))
                    resolveOrCreateAccount(outcome.bankName, outcome.accountLast4, outcome.accountType, outcome.balance, outcome.creditLimit)
                }

                is SmsParseOutcome.Parsed -> {
                    parsedCount++
                    if (raw.parseStatus != ParseStatus.PARSED) {
                        smsRawDao.update(raw.copy(parseStatus = ParseStatus.PARSED, confidence = outcome.transaction.confidence, failureReason = null))
                    }
                    val p = outcome.transaction
                    val account = resolveOrCreateAccount(p.bankName, p.accountLast4, p.accountType, p.balanceAfter, p.creditLimitAfter)
                    val existing = existingByFingerprint[p.fingerprint]
                    if (existing != null) {
                        // Re-evaluate rules WITHOUT overriding manual decisions!
                        val catResult = CategorisationEngine.categorise(existing, userCategoryRules, userAliases, validCategoryIds)
                        val exclResult = ExclusionEngine.evaluateRules(existing, userExclusionRules)
                        val updated = existing.copy(
                            accountId = existing.accountId ?: account.id,
                            merchant = if (existing.categorySource == CategorySource.MANUAL) existing.merchant else catResult.resolvedMerchant,
                            categoryId = if (existing.categorySource == CategorySource.MANUAL) existing.categoryId else catResult.categoryId,
                            categorySource = if (existing.categorySource == CategorySource.MANUAL) CategorySource.MANUAL else catResult.categorySource,
                            isExcluded = if (existing.exclusionSource == ExclusionSource.MANUAL) existing.isExcluded else exclResult.isExcluded,
                            exclusionReason = if (existing.exclusionSource == ExclusionSource.MANUAL) existing.exclusionReason else exclResult.reason,
                            exclusionSource = if (existing.exclusionSource == ExclusionSource.MANUAL) ExclusionSource.MANUAL else exclResult.source,
                            exclusionRuleId = if (existing.exclusionSource == ExclusionSource.MANUAL) existing.exclusionRuleId else exclResult.ruleId
                        )
                        if (updated != existing) {
                            transactionDao.update(updated)
                            existingByFingerprint[p.fingerprint] = updated
                        }
                    } else {
                        val base = buildBaseTransaction(p, raw.id, account.id)
                        val catResult = CategorisationEngine.categorise(base, userCategoryRules, userAliases, validCategoryIds)
                        val exclResult = ExclusionEngine.evaluateRules(base, userExclusionRules)
                        val entity = base.copy(
                            merchant = catResult.resolvedMerchant,
                            categoryId = catResult.categoryId,
                            categorySource = catResult.categorySource,
                            isExcluded = exclResult.isExcluded,
                            exclusionReason = exclResult.reason,
                            exclusionSource = exclResult.source,
                            exclusionRuleId = exclResult.ruleId
                        )
                        val id = transactionDao.insert(entity)
                        if (id > 0L) {
                            newlyAdded++
                            existingByFingerprint[p.fingerprint] = entity.copy(id = id)
                        }
                    }
                }
            }
        }

        // Also re-evaluate manual/imported/non-SMS transactions against current rules
        val allTxns = transactionDao.getAllTransactions()
        for (txn in allTxns) {
            if (txn.smsId != null && txn.fingerprint in existingByFingerprint) continue
            val catResult = CategorisationEngine.categorise(txn, userCategoryRules, userAliases, validCategoryIds)
            val exclResult = ExclusionEngine.evaluateRules(txn, userExclusionRules)
            val updated = txn.copy(
                merchant = if (txn.categorySource == CategorySource.MANUAL) txn.merchant else catResult.resolvedMerchant,
                categoryId = if (txn.categorySource == CategorySource.MANUAL) txn.categoryId else catResult.categoryId,
                categorySource = if (txn.categorySource == CategorySource.MANUAL) CategorySource.MANUAL else catResult.categorySource,
                isExcluded = if (txn.exclusionSource == ExclusionSource.MANUAL) txn.isExcluded else exclResult.isExcluded,
                exclusionReason = if (txn.exclusionSource == ExclusionSource.MANUAL) txn.exclusionReason else exclResult.reason,
                exclusionSource = if (txn.exclusionSource == ExclusionSource.MANUAL) ExclusionSource.MANUAL else exclResult.source,
                exclusionRuleId = if (txn.exclusionSource == ExclusionSource.MANUAL) txn.exclusionRuleId else exclResult.ruleId
            )
            if (updated != txn) transactionDao.update(updated)
        }

        runAutoTransferDetection()
        SyncResult(rawList.size, parsedCount, newlyAdded, unparsedCount)
    }

    suspend fun runAutoTransferDetection(): Int = withContext(ioDispatcher) {
        val windowMin = settingsDao.get(SettingKeys.AUTO_TRANSFER_WINDOW_MIN)?.toIntOrNull() ?: 10
        val txns = transactionDao.getAllTransactions()
        val accountsById = accountDao.getAll().associateBy { it.id }
        val pairs = AutoTransferDetector.detectTransfers(txns, accountsById, windowMin * 60_000L)
        if (pairs.isEmpty()) return@withContext 0

        val byId = txns.associateBy { it.id }
        val updates = mutableListOf<TransactionEntity>()
        for (pair in pairs) {
            val d = byId[pair.debitTxnId] ?: continue
            val c = byId[pair.creditTxnId] ?: continue
            updates += d.copy(
                isExcluded = true,
                exclusionReason = pair.reason,
                exclusionSource = ExclusionSource.AUTO_TRANSFER,
                matchedTransferId = c.id
            )
            updates += c.copy(
                isExcluded = true,
                exclusionReason = pair.reason,
                exclusionSource = ExclusionSource.AUTO_TRANSFER,
                matchedTransferId = d.id
            )
        }
        if (updates.isNotEmpty()) {
            transactionDao.updateAll(updates)
        }
        pairs.size
    }

    // ---- Manual & Transaction Edits ----

    suspend fun addManualTransaction(
        merchant: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        accountId: Long?,
        txnTime: Long,
        note: String = "",
        tags: String = "",
        currency: String = "₹"
    ): Long = withContext(ioDispatcher) {
        ensureInitialized()
        val account = if (accountId != null) {
            accountDao.getAll().firstOrNull { it.id == accountId }
        } else {
            resolveOrCreateAccount("Cash", null, AccountType.CASH, null, null)
        }
        val txn = TransactionEntity(
            smsId = null,
            accountId = account?.id,
            sender = "MANUAL",
            bankName = account?.bankName ?: "Cash",
            accountLast4 = account?.last4,
            amount = amount,
            currency = currency,
            type = type,
            merchant = merchant.trim(),
            rawMerchant = merchant.trim(),
            categoryId = categoryId,
            categorySource = CategorySource.MANUAL,
            channel = TransactionChannel.OTHER,
            txnTime = txnTime,
            rawBody = note.ifBlank { "Manual entry: $merchant" },
            fingerprint = "manual-${UUID.randomUUID()}",
            note = note,
            tags = tags,
            isManual = true
        )
        transactionDao.insert(txn)
    }

    suspend fun updateTransactionDetails(
        transactionId: Long,
        merchant: String,
        amount: Double,
        txnTime: Long,
        note: String,
        tags: String
    ) = withContext(ioDispatcher) {
        val existing = transactionDao.getAllTransactions().firstOrNull { it.id == transactionId } ?: return@withContext
        transactionDao.update(
            existing.copy(
                merchant = merchant.trim().ifBlank { existing.merchant },
                amount = amount.coerceAtLeast(0.01),
                txnTime = txnTime,
                note = note.trim(),
                tags = tags.trim()
            )
        )
    }

    suspend fun deleteTransaction(transactionId: Long) = withContext(ioDispatcher) {
        budgetDao.deleteSplitsForTransaction(transactionId)
        transactionDao.deleteById(transactionId)
    }

    suspend fun clearAllTransactions() = withContext(ioDispatcher) {
        transactionDao.deleteAll()
        smsRawDao.deleteAll()
        accountDao.deleteAll()
    }

    // ---- Categorisation & Rules ----

    suspend fun updateCategorySingle(transactionId: Long, categoryId: Long) = withContext(ioDispatcher) {
        val existing = transactionDao.getAllTransactions().firstOrNull { it.id == transactionId } ?: return@withContext
        transactionDao.update(
            existing.copy(
                categoryId = categoryId,
                categorySource = CategorySource.MANUAL
            )
        )
    }

    suspend fun countMatchingMerchantTransactions(merchant: String): Int = withContext(ioDispatcher) {
        val lower = merchant.trim().lowercase()
        if (lower.isEmpty()) return@withContext 0
        transactionDao.getAllTransactions().count {
            it.merchant.lowercase() == lower || it.rawMerchant.lowercase() == lower
        }
    }

    suspend fun alwaysCategoriseMerchant(
        merchant: String,
        categoryId: Long,
        applyToPast: Boolean = true
    ): Long = withContext(ioDispatcher) {
        val existingRules = rulesDao.getCategoryRules()
        val nextPriority = (existingRules.minOfOrNull { it.priority } ?: 100) - 1
        val ruleId = rulesDao.insertCategoryRule(
            CategoryRuleEntity(
                name = "Always: $merchant",
                priority = nextPriority,
                categoryId = categoryId,
                merchantPattern = merchant.trim(),
                merchantMatchMode = "EQUALS",
                enabled = true
            )
        )
        if (applyToPast) {
            val all = transactionDao.getAllTransactions()
            val updated = all.mapNotNull { txn ->
                val matches = txn.merchant.equals(merchant.trim(), ignoreCase = true) ||
                    txn.rawMerchant.equals(merchant.trim(), ignoreCase = true)
                if (matches && txn.categorySource != CategorySource.MANUAL) {
                    txn.copy(categoryId = categoryId, categorySource = CategorySource.RULE)
                } else {
                    null
                }
            }
            if (updated.isNotEmpty()) transactionDao.updateAll(updated)
        }
        ruleId
    }

    suspend fun categoriseMerchantGroup(merchant: String, categoryId: Long) = withContext(ioDispatcher) {
        alwaysCategoriseMerchant(merchant, categoryId, applyToPast = true)
        val all = transactionDao.getAllTransactions()
        val updates = all.mapNotNull { txn ->
            if (txn.merchant.equals(merchant.trim(), ignoreCase = true) &&
                txn.categoryId == ExpenseCategory.UNCATEGORISED.id
            ) {
                txn.copy(categoryId = categoryId, categorySource = CategorySource.RULE)
            } else {
                null
            }
        }
        if (updates.isNotEmpty()) transactionDao.updateAll(updates)
    }

    suspend fun saveCategoryRule(rule: CategoryRuleEntity, applyRetroactively: Boolean = true): Long = withContext(ioDispatcher) {
        val id = rulesDao.insertCategoryRule(rule)
        if (applyRetroactively && rule.enabled) {
            val savedRule = rule.copy(id = id)
            val all = transactionDao.getAllTransactions()
            val updates = all.mapNotNull { txn ->
                if (txn.categorySource != CategorySource.MANUAL && CategorisationEngine.matchesRule(savedRule, txn)) {
                    txn.copy(
                        categoryId = savedRule.categoryId,
                        categorySource = CategorySource.RULE,
                        isExcluded = if (savedRule.alsoExclude && txn.exclusionSource != ExclusionSource.MANUAL) true else txn.isExcluded,
                        exclusionReason = if (savedRule.alsoExclude && txn.exclusionSource != ExclusionSource.MANUAL) savedRule.exclusionReason ?: ExclusionReason.OTHER else txn.exclusionReason,
                        exclusionSource = if (savedRule.alsoExclude && txn.exclusionSource != ExclusionSource.MANUAL) ExclusionSource.USER_RULE else txn.exclusionSource
                    )
                } else {
                    null
                }
            }
            if (updates.isNotEmpty()) transactionDao.updateAll(updates)
        }
        id
    }

    suspend fun deleteCategoryRule(ruleId: Long) = withContext(ioDispatcher) {
        // Spec 12: Deleting a rule never deletes transactions!
        rulesDao.deleteCategoryRule(ruleId)
    }

    suspend fun toggleCategoryRule(rule: CategoryRuleEntity) = withContext(ioDispatcher) {
        rulesDao.updateCategoryRule(rule.copy(enabled = !rule.enabled))
    }

    suspend fun moveCategoryRule(ruleId: Long, moveUp: Boolean) = withContext(ioDispatcher) {
        val list = rulesDao.getCategoryRules().sortedWith(compareBy({ it.priority }, { it.id })).toMutableList()
        val idx = list.indexOfFirst { it.id == ruleId }
        if (idx < 0) return@withContext
        val targetIdx = if (moveUp) idx - 1 else idx + 1
        if (targetIdx !in list.indices) return@withContext
        val swapped = list[idx]
        list[idx] = list[targetIdx]
        list[targetIdx] = swapped
        list.forEachIndexed { index, item ->
            rulesDao.updateCategoryRule(item.copy(priority = (index + 1) * 10))
        }
    }

    suspend fun testRulesOnSampleSms(sender: String, body: String): CategorisationEngineResultPreview = withContext(ioDispatcher) {
        ensureInitialized()
        val parsed = SmsTransactionParser.parse(sender = sender, body = body, timestamp = System.currentTimeMillis())
        val categories = categoryDao.getAll().associateBy { it.id }
        if (parsed == null) {
            return@withContext CategorisationEngineResultPreview(parsed = null, categoryName = "Not parsed", source = CategorySource.FALLBACK, matchedRuleName = null)
        }
        val dummyTxn = buildBaseTransaction(parsed, null, null)
        val rules = rulesDao.getCategoryRules()
        val aliases = rulesDao.getAliases()
        val result = CategorisationEngine.categorise(dummyTxn, rules, aliases, categories.keys)
        val matchedRuleName = result.matchedRuleId?.let { id -> rules.firstOrNull { it.id == id }?.name }
        CategorisationEngineResultPreview(
            parsed = parsed,
            categoryName = categories[result.categoryId]?.name ?: "Uncategorised",
            source = result.categorySource,
            matchedRuleName = matchedRuleName
        )
    }

    // ---- Categories CRUD ----

    suspend fun addCategory(name: String, colorHex: Long, iconKey: String = "category", parentId: Long? = null): Long = withContext(ioDispatcher) {
        categoryDao.insert(
            CategoryEntity(name = name.trim(), iconKey = iconKey, colorHex = colorHex, parentId = parentId, isBuiltin = false)
        )
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(ioDispatcher) {
        categoryDao.update(category)
    }

    suspend fun mergeCategories(sourceCategoryId: Long, targetCategoryId: Long) = withContext(ioDispatcher) {
        if (sourceCategoryId == targetCategoryId) return@withContext
        val txns = transactionDao.getAllTransactions().filter { it.categoryId == sourceCategoryId }
        if (txns.isNotEmpty()) {
            transactionDao.updateAll(txns.map { it.copy(categoryId = targetCategoryId) })
        }
        val rules = rulesDao.getCategoryRules().filter { it.categoryId == sourceCategoryId }
        rules.forEach { rulesDao.updateCategoryRule(it.copy(categoryId = targetCategoryId)) }
        val sourceCat = categoryDao.getAll().firstOrNull { it.id == sourceCategoryId }
        if (sourceCat != null && !sourceCat.isBuiltin) {
            categoryDao.deleteById(sourceCategoryId)
        }
    }

    suspend fun deleteCategory(categoryId: Long) = withContext(ioDispatcher) {
        val cat = categoryDao.getAll().firstOrNull { it.id == categoryId } ?: return@withContext
        if (cat.isBuiltin) return@withContext
        mergeCategories(categoryId, ExpenseCategory.OTHER.id)
    }

    // ---- Exclusions & Rules ----

    suspend fun countExclusionMatches(
        merchantPattern: String? = null,
        bodyKeyword: String? = null,
        exactAmount: Double? = null
    ): Int = withContext(ioDispatcher) {
        val dummyRule = ExclusionRuleEntity(
            name = "preview",
            merchantPattern = merchantPattern,
            bodyKeyword = bodyKeyword,
            exactAmount = exactAmount,
            reason = ExclusionReason.OTHER
        )
        transactionDao.getAllTransactions().count { ExclusionEngine.matchesRule(dummyRule, it) }
    }

    suspend fun excludeSingleTransaction(transactionId: Long, reason: ExclusionReason) = withContext(ioDispatcher) {
        val txn = transactionDao.getAllTransactions().firstOrNull { it.id == transactionId } ?: return@withContext
        transactionDao.update(
            txn.copy(
                isExcluded = true,
                exclusionReason = reason,
                exclusionSource = ExclusionSource.MANUAL
            )
        )
    }

    suspend fun createExclusionRuleAndApply(
        name: String,
        reason: ExclusionReason,
        merchantPattern: String? = null,
        bodyKeyword: String? = null,
        regexPattern: String? = null,
        exactAmount: Double? = null,
        accountId: Long? = null,
        applyRetroactively: Boolean = true
    ): Long = withContext(ioDispatcher) {
        val rule = ExclusionRuleEntity(
            name = name,
            merchantPattern = merchantPattern?.takeIf { it.isNotBlank() },
            bodyKeyword = bodyKeyword?.takeIf { it.isNotBlank() },
            regexPattern = regexPattern?.takeIf { it.isNotBlank() },
            exactAmount = exactAmount,
            accountId = accountId,
            reason = reason,
            enabled = true,
            applyRetroactively = applyRetroactively
        )
        val ruleId = rulesDao.insertExclusionRule(rule)
        val savedRule = rule.copy(id = ruleId)

        if (applyRetroactively) {
            val all = transactionDao.getAllTransactions()
            var count = 0
            val updates = all.mapNotNull { txn ->
                if (txn.exclusionSource != ExclusionSource.MANUAL && ExclusionEngine.matchesRule(savedRule, txn)) {
                    count++
                    txn.copy(
                        isExcluded = true,
                        exclusionReason = reason,
                        exclusionSource = ExclusionSource.USER_RULE,
                        exclusionRuleId = ruleId
                    )
                } else {
                    null
                }
            }
            if (updates.isNotEmpty()) {
                transactionDao.updateAll(updates)
                rulesDao.updateExclusionRule(savedRule.copy(matchCount = count, lastMatchedAt = System.currentTimeMillis()))
            }
        }
        ruleId
    }

    suspend fun includeTransactionsAgain(transactionIds: Set<Long>) = withContext(ioDispatcher) {
        val all = transactionDao.getAllTransactions()
        val updates = all.mapNotNull { txn ->
            if (txn.id in transactionIds) {
                txn.copy(
                    isExcluded = false,
                    exclusionReason = null,
                    exclusionSource = ExclusionSource.MANUAL,
                    exclusionRuleId = null,
                    matchedTransferId = null
                )
            } else {
                null
            }
        }
        if (updates.isNotEmpty()) transactionDao.updateAll(updates)
    }

    suspend fun bulkExcludeByQuery(query: String, reason: ExclusionReason, exclude: Boolean): Int = withContext(ioDispatcher) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@withContext 0
        val updates = transactionDao.getAllTransactions().mapNotNull { txn ->
            val match = txn.merchant.lowercase().contains(q) || txn.rawBody.lowercase().contains(q)
            if (match) {
                txn.copy(
                    isExcluded = exclude,
                    exclusionReason = if (exclude) reason else null,
                    exclusionSource = ExclusionSource.MANUAL
                )
            } else {
                null
            }
        }
        if (updates.isNotEmpty()) transactionDao.updateAll(updates)
        updates.size
    }

    suspend fun resetExclusionRule(ruleId: Long) = withContext(ioDispatcher) {
        val updates = transactionDao.getAllTransactions().mapNotNull { txn ->
            if (txn.exclusionRuleId == ruleId) {
                txn.copy(
                    isExcluded = false,
                    exclusionReason = null,
                    exclusionSource = ExclusionSource.NONE,
                    exclusionRuleId = null
                )
            } else {
                null
            }
        }
        if (updates.isNotEmpty()) transactionDao.updateAll(updates)
        rulesDao.deleteExclusionRule(ruleId)
    }

    suspend fun toggleExclusionRule(rule: ExclusionRuleEntity) = withContext(ioDispatcher) {
        rulesDao.updateExclusionRule(rule.copy(enabled = !rule.enabled))
    }

    suspend fun acceptExclusionSuggestion(txnId: Long, reason: ExclusionReason) = withContext(ioDispatcher) {
        excludeSingleTransaction(txnId, reason)
    }

    suspend fun dismissExclusionSuggestion(suggestionKey: String) = withContext(ioDispatcher) {
        rulesDao.insertDismissedSuggestion(DismissedExclusionSuggestionEntity(suggestionKey = suggestionKey))
    }

    // ---- Accounts ----

    suspend fun renameAccount(accountId: Long, displayName: String) = withContext(ioDispatcher) {
        val acc = accountDao.getAll().firstOrNull { it.id == accountId } ?: return@withContext
        accountDao.update(acc.copy(displayName = displayName.trim().ifBlank { acc.displayName }))
    }

    suspend fun updateAccountType(accountId: Long, type: AccountType) = withContext(ioDispatcher) {
        val acc = accountDao.getAll().firstOrNull { it.id == accountId } ?: return@withContext
        accountDao.update(acc.copy(type = type))
    }

    suspend fun toggleAccountActive(accountId: Long) = withContext(ioDispatcher) {
        val acc = accountDao.getAll().firstOrNull { it.id == accountId } ?: return@withContext
        accountDao.update(acc.copy(isActive = !acc.isActive))
    }

    suspend fun mergeAccounts(sourceAccountId: Long, targetAccountId: Long) = withContext(ioDispatcher) {
        if (sourceAccountId == targetAccountId) return@withContext
        val allAccounts = accountDao.getAll()
        val source = allAccounts.firstOrNull { it.id == sourceAccountId } ?: return@withContext
        val target = allAccounts.firstOrNull { it.id == targetAccountId } ?: return@withContext

        val txns = transactionDao.getAllTransactions().filter { it.accountId == sourceAccountId }
        if (txns.isNotEmpty()) {
            transactionDao.updateAll(txns.map { it.copy(accountId = target.id) })
        }
        accountDao.update(source.copy(isActive = false, mergedIntoAccountId = target.id))
    }

    // ---- Budgets & Splits & Aliases & Templates & Senders ----

    suspend fun upsertBudget(categoryId: Long?, amount: Double) = withContext(ioDispatcher) {
        val existing = budgetDao.getBudgets().firstOrNull { it.categoryId == categoryId }
        if (existing != null) {
            budgetDao.updateBudget(existing.copy(amount = amount))
        } else {
            budgetDao.insertBudget(BudgetEntity(categoryId = categoryId, amount = amount))
        }
    }

    suspend fun deleteBudget(budgetId: Long) = withContext(ioDispatcher) {
        budgetDao.deleteBudget(budgetId)
    }

    suspend fun saveSplits(transactionId: Long, splits: List<TransactionSplitEntity>) = withContext(ioDispatcher) {
        budgetDao.deleteSplitsForTransaction(transactionId)
        if (splits.isNotEmpty()) {
            budgetDao.insertSplits(splits.map { it.copy(transactionId = transactionId) })
        }
    }

    suspend fun addAlias(rawPattern: String, canonicalName: String, defaultCategoryId: Long?) = withContext(ioDispatcher) {
        rulesDao.insertAlias(
            MerchantAliasEntity(
                rawPattern = rawPattern.trim(),
                canonicalName = canonicalName.trim(),
                defaultCategoryId = defaultCategoryId
            )
        )
    }

    suspend fun deleteAlias(aliasId: Long) = withContext(ioDispatcher) {
        rulesDao.deleteAlias(aliasId)
    }

    suspend fun saveParseTemplate(template: ParseTemplateEntity): Long = withContext(ioDispatcher) {
        rulesDao.insertParseTemplate(template)
    }

    suspend fun deleteParseTemplate(templateId: Long) = withContext(ioDispatcher) {
        rulesDao.deleteParseTemplate(templateId)
    }

    suspend fun setSenderRule(sender: String, action: String) = withContext(ioDispatcher) {
        val code = BankDirectory.normalizeSenderCode(sender)
        rulesDao.insertSenderRule(SenderRuleEntity(senderCode = code, action = action))
    }

    suspend fun deleteSenderRule(id: Long) = withContext(ioDispatcher) {
        rulesDao.deleteSenderRule(id)
    }

    // ---- Settings ----

    suspend fun setSelectedAccountIds(ids: Set<Long>) = withContext(ioDispatcher) {
        settingsDao.upsert(AppSettingEntity(SettingKeys.SELECTED_ACCOUNT_IDS, ids.joinToString(",")))
    }

    suspend fun setMonthStartDay(day: Int) = withContext(ioDispatcher) {
        settingsDao.upsert(AppSettingEntity(SettingKeys.MONTH_START_DAY, day.coerceIn(1, 28).toString()))
    }

    suspend fun setFinancialYearMode(enabled: Boolean) = withContext(ioDispatcher) {
        settingsDao.upsert(AppSettingEntity(SettingKeys.FINANCIAL_YEAR_MODE, enabled.toString()))
    }

    suspend fun setAutoTransferWindowMinutes(minutes: Int) = withContext(ioDispatcher) {
        settingsDao.upsert(AppSettingEntity(SettingKeys.AUTO_TRANSFER_WINDOW_MIN, minutes.coerceIn(1, 1440).toString()))
    }

    suspend fun setPrivacyMode(enabled: Boolean) = withContext(ioDispatcher) {
        settingsDao.upsert(AppSettingEntity(SettingKeys.PRIVACY_MODE, enabled.toString()))
    }

    suspend fun setFlagSecure(enabled: Boolean) = withContext(ioDispatcher) {
        settingsDao.upsert(AppSettingEntity(SettingKeys.FLAG_SECURE, enabled.toString()))
    }

    suspend fun setThemeMode(mode: ThemePreference) = withContext(ioDispatcher) {
        settingsDao.upsert(AppSettingEntity(SettingKeys.THEME_MODE, mode.name))
    }

    suspend fun setPinHash(pinHash: String?) = withContext(ioDispatcher) {
        if (pinHash == null) settingsDao.delete(SettingKeys.PIN_HASH)
        else settingsDao.upsert(AppSettingEntity(SettingKeys.PIN_HASH, pinHash))
    }

    suspend fun setOnboardingDone(done: Boolean) = withContext(ioDispatcher) {
        settingsDao.upsert(AppSettingEntity(SettingKeys.ONBOARDING_DONE, done.toString()))
    }

    // ---- Export & Backup ----

    suspend fun exportTransactionsCsv(): String = withContext(ioDispatcher) {
        val txns = transactionDao.getAllTransactions()
        val cats = categoryDao.getAll().associate { it.id to it.name }
        CsvExporter.exportTransactions(txns, cats)
    }

    suspend fun exportRulesJson(): String = withContext(ioDispatcher) {
        val bundle = RulesBundle(
            categoryRules = rulesDao.getCategoryRules(),
            exclusionRules = rulesDao.getExclusionRules(),
            aliases = rulesDao.getAliases(),
            templates = rulesDao.getParseTemplates()
        )
        RulesAndBackupCodec.exportRulesJson(bundle)
    }

    suspend fun importRulesJson(json: String): Int = withContext(ioDispatcher) {
        val bundle = RulesAndBackupCodec.importRulesJson(json)
        bundle.categoryRules.forEach { rulesDao.insertCategoryRule(it) }
        bundle.exclusionRules.forEach { rulesDao.insertExclusionRule(it) }
        bundle.aliases.forEach { rulesDao.insertAlias(it) }
        bundle.templates.forEach { rulesDao.insertParseTemplate(it) }
        bundle.categoryRules.size + bundle.exclusionRules.size + bundle.aliases.size + bundle.templates.size
    }

    suspend fun exportEncryptedBackup(password: String): String = withContext(ioDispatcher) {
        val bundle = RulesBundle(
            categoryRules = rulesDao.getCategoryRules(),
            exclusionRules = rulesDao.getExclusionRules(),
            aliases = rulesDao.getAliases(),
            templates = rulesDao.getParseTemplates()
        )
        val json = RulesAndBackupCodec.exportBackupJson(transactionDao.getAllTransactions(), bundle)
        BackupCrypto.encrypt(json, password)
    }

    suspend fun importEncryptedBackup(payload: String, password: String): Int = withContext(ioDispatcher) {
        ensureInitialized()
        val json = BackupCrypto.decrypt(payload, password)
        val (txns, bundle) = RulesAndBackupCodec.importBackupJson(json)
        bundle.categoryRules.forEach { rulesDao.insertCategoryRule(it) }
        bundle.exclusionRules.forEach { rulesDao.insertExclusionRule(it) }
        bundle.aliases.forEach { rulesDao.insertAlias(it) }
        bundle.templates.forEach { rulesDao.insertParseTemplate(it) }

        var added = 0
        for (txn in txns) {
            val acc = resolveOrCreateAccount(txn.bankName, txn.accountLast4, AccountType.SAVINGS, txn.balanceAfter, null)
            val id = transactionDao.insert(txn.copy(id = 0, accountId = acc.id))
            if (id > 0L) added++
        }
        added
    }

    // ---- Internal helpers ----

    private suspend fun resolveOrCreateAccount(
        bankName: String,
        last4: String?,
        type: AccountType,
        balance: Double?,
        creditLimit: Double?
    ): AccountEntity {
        val all = accountDao.getAll()
        val found = all.firstOrNull { acc ->
            acc.bankName.equals(bankName, ignoreCase = true) &&
                acc.last4 == last4 &&
                (last4 != null || acc.type == type)
        }
        if (found != null) {
            val resolved = if (found.mergedIntoAccountId != null) {
                all.firstOrNull { it.id == found.mergedIntoAccountId } ?: found
            } else {
                found
            }
            val updatedBalance = balance ?: resolved.lastBalance
            val updatedLimit = creditLimit ?: resolved.creditLimit
            if (updatedBalance != resolved.lastBalance || updatedLimit != resolved.creditLimit) {
                val updated = resolved.copy(lastBalance = updatedBalance, creditLimit = updatedLimit)
                accountDao.update(updated)
                return updated
            }
            return resolved
        }

        val display = buildString {
            append(bankName)
            append(" ")
            append(type.label)
            if (!last4.isNullOrBlank()) append(" ••$last4")
        }
        val newAccount = AccountEntity(
            displayName = display,
            bankName = bankName,
            type = type,
            last4 = last4,
            lastBalance = balance,
            creditLimit = creditLimit,
            accentColorHex = BankDirectory.accentColorForBank(bankName),
            isActive = true
        )
        val id = accountDao.insert(newAccount)
        return newAccount.copy(id = id)
    }

    private fun isSecondaryDuplicate(parsed: ParsedSmsTransaction, existing: List<TransactionEntity>): Boolean {
        val fiveMinutesMs = 5 * 60_000L
        return existing.any { txn ->
            if (txn.fingerprint == parsed.fingerprint) return@any true
            if (parsed.refNo != null && txn.refNo == parsed.refNo &&
                abs(txn.amount - parsed.amount) < 0.01 &&
                txn.accountLast4 == parsed.accountLast4 &&
                abs(txn.txnTime - parsed.timestamp) <= fiveMinutesMs
            ) {
                return@any true
            }
            false
        }
    }

    private fun buildBaseTransaction(
        parsed: ParsedSmsTransaction,
        smsId: Long?,
        accountId: Long?
    ): TransactionEntity {
        return TransactionEntity(
            smsId = smsId,
            accountId = accountId,
            sender = parsed.sender,
            bankName = parsed.bankName,
            accountLast4 = parsed.accountLast4,
            amount = parsed.amount,
            currency = parsed.currency,
            type = parsed.type,
            merchant = parsed.merchant,
            rawMerchant = parsed.rawMerchant,
            categoryId = parsed.categoryId,
            categorySource = parsed.categorySource,
            channel = parsed.channel,
            refNo = parsed.refNo,
            balanceAfter = parsed.balanceAfter,
            creditLimitAfter = parsed.creditLimitAfter,
            txnTime = parsed.timestamp,
            rawBody = parsed.rawBody,
            fingerprint = parsed.fingerprint,
            isInternational = parsed.isInternational
        )
    }

    private suspend fun bumpCategoryRuleMatch(
        ruleId: Long,
        rules: MutableList<CategoryRuleEntity>,
        matchedAt: Long
    ) {
        val idx = rules.indexOfFirst { it.id == ruleId }
        if (idx >= 0) {
            val updated = rules[idx].copy(matchCount = rules[idx].matchCount + 1, lastMatchedAt = matchedAt)
            rules[idx] = updated
            rulesDao.updateCategoryRule(updated)
        }
    }

    private suspend fun bumpExclusionRuleMatch(
        ruleId: Long,
        rules: MutableList<ExclusionRuleEntity>,
        matchedAt: Long
    ) {
        val idx = rules.indexOfFirst { it.id == ruleId }
        if (idx >= 0) {
            val updated = rules[idx].copy(matchCount = rules[idx].matchCount + 1, lastMatchedAt = matchedAt)
            rules[idx] = updated
            rulesDao.updateExclusionRule(updated)
        }
    }

    companion object {
        fun parsePreferences(settings: List<AppSettingEntity>): AppPreferences {
            val map = settings.associate { it.key to it.value }
            val selectedIds = map[SettingKeys.SELECTED_ACCOUNT_IDS]
                ?.split(",")
                ?.mapNotNull { it.trim().toLongOrNull() }
                ?.toSet()
                ?: emptySet()
            return AppPreferences(
                selectedAccountIds = selectedIds,
                monthStartDay = map[SettingKeys.MONTH_START_DAY]?.toIntOrNull()?.coerceIn(1, 28) ?: 1,
                financialYearMode = map[SettingKeys.FINANCIAL_YEAR_MODE]?.toBooleanStrictOrNull() ?: false,
                autoTransferWindowMinutes = map[SettingKeys.AUTO_TRANSFER_WINDOW_MIN]?.toIntOrNull()?.coerceIn(1, 1440) ?: 10,
                privacyMode = map[SettingKeys.PRIVACY_MODE]?.toBooleanStrictOrNull() ?: false,
                flagSecure = map[SettingKeys.FLAG_SECURE]?.toBooleanStrictOrNull() ?: false,
                themeMode = map[SettingKeys.THEME_MODE]?.let {
                    runCatching { ThemePreference.valueOf(it) }.getOrNull()
                } ?: ThemePreference.SYSTEM,
                pinHash = map[SettingKeys.PIN_HASH],
                onboardingDone = map[SettingKeys.ONBOARDING_DONE]?.toBooleanStrictOrNull() ?: false
            )
        }
    }
}

data class CategorisationEngineResultPreview(
    val parsed: ParsedSmsTransaction?,
    val categoryName: String,
    val source: CategorySource,
    val matchedRuleName: String?
)

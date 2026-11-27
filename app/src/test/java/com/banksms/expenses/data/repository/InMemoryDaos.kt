package com.banksms.expenses.data.repository

import com.banksms.expenses.data.local.AccountDao
import com.banksms.expenses.data.local.BudgetDao
import com.banksms.expenses.data.local.CategoryDao
import com.banksms.expenses.data.local.RulesDao
import com.banksms.expenses.data.local.SettingsDao
import com.banksms.expenses.data.local.SmsRawDao
import com.banksms.expenses.data.local.TransactionDao
import com.banksms.expenses.data.model.AccountEntity
import com.banksms.expenses.data.model.AppSettingEntity
import com.banksms.expenses.data.model.BudgetEntity
import com.banksms.expenses.data.model.CategoryEntity
import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.DismissedExclusionSuggestionEntity
import com.banksms.expenses.data.model.ExclusionRuleEntity
import com.banksms.expenses.data.model.MerchantAliasEntity
import com.banksms.expenses.data.model.ParseTemplateEntity
import com.banksms.expenses.data.model.SenderRuleEntity
import com.banksms.expenses.data.model.SmsRawEntity
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionSplitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryTransactionDao : TransactionDao {
    private val items = MutableStateFlow<List<TransactionEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAllTransactions(): Flow<List<TransactionEntity>> =
        items.map { list -> list.sortedByDescending { it.txnTime } }

    override suspend fun getAllTransactions(): List<TransactionEntity> =
        items.value.sortedByDescending { it.txnTime }

    override suspend fun insertAll(transactions: List<TransactionEntity>): List<Long> =
        transactions.map { insert(it) }

    override suspend fun insert(transaction: TransactionEntity): Long {
        if (items.value.any { it.fingerprint == transaction.fingerprint }) return -1L
        val id = if (transaction.id > 0L) transaction.id else nextId++
        items.value = items.value + transaction.copy(id = id)
        return id
    }

    override suspend fun update(transaction: TransactionEntity) {
        items.value = items.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun updateAll(transactions: List<TransactionEntity>) {
        val byId = transactions.associateBy { it.id }
        items.value = items.value.map { byId[it.id] ?: it }
    }

    override suspend fun deleteById(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        items.value = emptyList()
    }
}

class InMemorySmsRawDao : SmsRawDao {
    private val items = MutableStateFlow<List<SmsRawEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<SmsRawEntity>> = items
    override suspend fun getAll(): List<SmsRawEntity> = items.value
    override suspend fun getByHash(hash: String): SmsRawEntity? = items.value.firstOrNull { it.hash == hash }

    override suspend fun insert(sms: SmsRawEntity): Long {
        if (items.value.any { it.hash == sms.hash }) return -1L
        val id = if (sms.id > 0L) sms.id else nextId++
        items.value = items.value + sms.copy(id = id)
        return id
    }

    override suspend fun insertAll(smsList: List<SmsRawEntity>): List<Long> = smsList.map { insert(it) }

    override suspend fun update(sms: SmsRawEntity) {
        items.value = items.value.map { if (it.id == sms.id) sms else it }
    }

    override suspend fun deleteAll() {
        items.value = emptyList()
    }
}

class InMemoryAccountDao : AccountDao {
    private val items = MutableStateFlow<List<AccountEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<AccountEntity>> = items
    override suspend fun getAll(): List<AccountEntity> = items.value

    override suspend fun insert(account: AccountEntity): Long {
        val id = if (account.id > 0L) account.id else nextId++
        items.value = items.value + account.copy(id = id)
        return id
    }

    override suspend fun update(account: AccountEntity) {
        items.value = items.value.map { if (it.id == account.id) account else it }
    }

    override suspend fun deleteAll() {
        items.value = emptyList()
    }
}

class InMemoryCategoryDao : CategoryDao {
    private val items = MutableStateFlow<List<CategoryEntity>>(emptyList())
    private var nextId = 100L

    override fun observeAll(): Flow<List<CategoryEntity>> = items
    override suspend fun getAll(): List<CategoryEntity> = items.value

    override suspend fun insert(category: CategoryEntity): Long {
        val id = if (category.id > 0L) category.id else nextId++
        items.value = items.value + category.copy(id = id)
        return id
    }

    override suspend fun insertAll(categories: List<CategoryEntity>) {
        categories.forEach { insert(it) }
    }

    override suspend fun update(category: CategoryEntity) {
        items.value = items.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun deleteById(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }
}

class InMemoryRulesDao : RulesDao {
    private val catRules = MutableStateFlow<List<CategoryRuleEntity>>(emptyList())
    private val exclRules = MutableStateFlow<List<ExclusionRuleEntity>>(emptyList())
    private val aliasList = MutableStateFlow<List<MerchantAliasEntity>>(emptyList())
    private val templates = MutableStateFlow<List<ParseTemplateEntity>>(emptyList())
    private val senders = MutableStateFlow<List<SenderRuleEntity>>(emptyList())
    private val dismissed = MutableStateFlow<List<DismissedExclusionSuggestionEntity>>(emptyList())
    private var nextId = 1L

    override fun observeCategoryRules(): Flow<List<CategoryRuleEntity>> = catRules
    override suspend fun getCategoryRules(): List<CategoryRuleEntity> = catRules.value.sortedBy { it.priority }
    override suspend fun insertCategoryRule(rule: CategoryRuleEntity): Long {
        val id = if (rule.id > 0L) rule.id else nextId++
        catRules.value = catRules.value.filterNot { it.id == id } + rule.copy(id = id)
        return id
    }
    override suspend fun updateCategoryRule(rule: CategoryRuleEntity) {
        catRules.value = catRules.value.map { if (it.id == rule.id) rule else it }
    }
    override suspend fun deleteCategoryRule(id: Long) {
        catRules.value = catRules.value.filterNot { it.id == id }
    }

    override fun observeExclusionRules(): Flow<List<ExclusionRuleEntity>> = exclRules
    override suspend fun getExclusionRules(): List<ExclusionRuleEntity> = exclRules.value
    override suspend fun insertExclusionRule(rule: ExclusionRuleEntity): Long {
        val id = if (rule.id > 0L) rule.id else nextId++
        exclRules.value = exclRules.value.filterNot { it.id == id } + rule.copy(id = id)
        return id
    }
    override suspend fun updateExclusionRule(rule: ExclusionRuleEntity) {
        exclRules.value = exclRules.value.map { if (it.id == rule.id) rule else it }
    }
    override suspend fun deleteExclusionRule(id: Long) {
        exclRules.value = exclRules.value.filterNot { it.id == id }
    }

    override fun observeAliases(): Flow<List<MerchantAliasEntity>> = aliasList
    override suspend fun getAliases(): List<MerchantAliasEntity> = aliasList.value
    override suspend fun insertAlias(alias: MerchantAliasEntity): Long {
        val id = if (alias.id > 0L) alias.id else nextId++
        aliasList.value = aliasList.value.filterNot { it.rawPattern == alias.rawPattern } + alias.copy(id = id)
        return id
    }
    override suspend fun deleteAlias(id: Long) {
        aliasList.value = aliasList.value.filterNot { it.id == id }
    }

    override fun observeParseTemplates(): Flow<List<ParseTemplateEntity>> = templates
    override suspend fun getParseTemplates(): List<ParseTemplateEntity> = templates.value
    override suspend fun insertParseTemplate(template: ParseTemplateEntity): Long {
        val id = if (template.id > 0L) template.id else nextId++
        templates.value = templates.value.filterNot { it.id == id } + template.copy(id = id)
        return id
    }
    override suspend fun updateParseTemplate(template: ParseTemplateEntity) {
        templates.value = templates.value.map { if (it.id == template.id) template else it }
    }
    override suspend fun deleteParseTemplate(id: Long) {
        templates.value = templates.value.filterNot { it.id == id }
    }

    override fun observeSenderRules(): Flow<List<SenderRuleEntity>> = senders
    override suspend fun getSenderRules(): List<SenderRuleEntity> = senders.value
    override suspend fun insertSenderRule(rule: SenderRuleEntity): Long {
        val id = if (rule.id > 0L) rule.id else nextId++
        senders.value = senders.value.filterNot { it.senderCode == rule.senderCode } + rule.copy(id = id)
        return id
    }
    override suspend fun deleteSenderRule(id: Long) {
        senders.value = senders.value.filterNot { it.id == id }
    }

    override fun observeDismissedSuggestions(): Flow<List<DismissedExclusionSuggestionEntity>> = dismissed
    override suspend fun getDismissedSuggestions(): List<DismissedExclusionSuggestionEntity> = dismissed.value
    override suspend fun insertDismissedSuggestion(item: DismissedExclusionSuggestionEntity): Long {
        if (dismissed.value.any { it.suggestionKey == item.suggestionKey }) return -1L
        val id = nextId++
        dismissed.value = dismissed.value + item.copy(id = id)
        return id
    }
}

class InMemoryBudgetDao : BudgetDao {
    private val budgets = MutableStateFlow<List<BudgetEntity>>(emptyList())
    private val splits = MutableStateFlow<List<TransactionSplitEntity>>(emptyList())
    private var nextId = 1L

    override fun observeBudgets(): Flow<List<BudgetEntity>> = budgets
    override suspend fun getBudgets(): List<BudgetEntity> = budgets.value
    override suspend fun insertBudget(budget: BudgetEntity): Long {
        val id = if (budget.id > 0L) budget.id else nextId++
        budgets.value = budgets.value + budget.copy(id = id)
        return id
    }
    override suspend fun updateBudget(budget: BudgetEntity) {
        budgets.value = budgets.value.map { if (it.id == budget.id) budget else it }
    }
    override suspend fun deleteBudget(id: Long) {
        budgets.value = budgets.value.filterNot { it.id == id }
    }

    override fun observeSplits(): Flow<List<TransactionSplitEntity>> = splits
    override suspend fun getAllSplits(): List<TransactionSplitEntity> = splits.value
    override suspend fun getSplitsForTransaction(transactionId: Long): List<TransactionSplitEntity> =
        splits.value.filter { it.transactionId == transactionId }
    override suspend fun insertSplits(splits: List<TransactionSplitEntity>) {
        var idx = this.splits.value.size + 1L
        this.splits.value = this.splits.value + splits.map { if (it.id > 0L) it else it.copy(id = idx++) }
    }
    override suspend fun deleteSplitsForTransaction(transactionId: Long) {
        splits.value = splits.value.filterNot { it.transactionId == transactionId }
    }
}

class InMemorySettingsDao : SettingsDao {
    private val items = MutableStateFlow<List<AppSettingEntity>>(emptyList())

    override fun observeAll(): Flow<List<AppSettingEntity>> = items
    override suspend fun getAll(): List<AppSettingEntity> = items.value
    override suspend fun get(key: String): String? = items.value.firstOrNull { it.key == key }?.value
    override suspend fun upsert(setting: AppSettingEntity) {
        items.value = items.value.filterNot { it.key == setting.key } + setting
    }
    override suspend fun delete(key: String) {
        items.value = items.value.filterNot { it.key == key }
    }
}

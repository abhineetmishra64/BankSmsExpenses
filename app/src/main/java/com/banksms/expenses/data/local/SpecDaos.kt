package com.banksms.expenses.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import com.banksms.expenses.data.model.TransactionSplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsRawDao {
    @Query("SELECT * FROM sms_raw ORDER BY receivedAt DESC")
    fun observeAll(): Flow<List<SmsRawEntity>>

    @Query("SELECT * FROM sms_raw ORDER BY receivedAt DESC")
    suspend fun getAll(): List<SmsRawEntity>

    @Query("SELECT * FROM sms_raw WHERE hash = :hash LIMIT 1")
    suspend fun getByHash(hash: String): SmsRawEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sms: SmsRawEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(smsList: List<SmsRawEntity>): List<Long>

    @Update
    suspend fun update(sms: SmsRawEntity)

    @Query("DELETE FROM sms_raw")
    suspend fun deleteAll()
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY bankName ASC, displayName ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY bankName ASC, displayName ASC")
    suspend fun getAll(): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface RulesDao {
    @Query("SELECT * FROM category_rules ORDER BY priority ASC, id ASC")
    fun observeCategoryRules(): Flow<List<CategoryRuleEntity>>

    @Query("SELECT * FROM category_rules ORDER BY priority ASC, id ASC")
    suspend fun getCategoryRules(): List<CategoryRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryRule(rule: CategoryRuleEntity): Long

    @Update
    suspend fun updateCategoryRule(rule: CategoryRuleEntity)

    @Query("DELETE FROM category_rules WHERE id = :id")
    suspend fun deleteCategoryRule(id: Long)

    @Query("SELECT * FROM exclusion_rules ORDER BY id DESC")
    fun observeExclusionRules(): Flow<List<ExclusionRuleEntity>>

    @Query("SELECT * FROM exclusion_rules ORDER BY id DESC")
    suspend fun getExclusionRules(): List<ExclusionRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExclusionRule(rule: ExclusionRuleEntity): Long

    @Update
    suspend fun updateExclusionRule(rule: ExclusionRuleEntity)

    @Query("DELETE FROM exclusion_rules WHERE id = :id")
    suspend fun deleteExclusionRule(id: Long)

    @Query("SELECT * FROM merchant_aliases ORDER BY canonicalName ASC")
    fun observeAliases(): Flow<List<MerchantAliasEntity>>

    @Query("SELECT * FROM merchant_aliases ORDER BY canonicalName ASC")
    suspend fun getAliases(): List<MerchantAliasEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: MerchantAliasEntity): Long

    @Query("DELETE FROM merchant_aliases WHERE id = :id")
    suspend fun deleteAlias(id: Long)

    @Query("SELECT * FROM parse_templates ORDER BY id DESC")
    fun observeParseTemplates(): Flow<List<ParseTemplateEntity>>

    @Query("SELECT * FROM parse_templates ORDER BY id DESC")
    suspend fun getParseTemplates(): List<ParseTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParseTemplate(template: ParseTemplateEntity): Long

    @Update
    suspend fun updateParseTemplate(template: ParseTemplateEntity)

    @Query("DELETE FROM parse_templates WHERE id = :id")
    suspend fun deleteParseTemplate(id: Long)

    @Query("SELECT * FROM sender_rules ORDER BY senderCode ASC")
    fun observeSenderRules(): Flow<List<SenderRuleEntity>>

    @Query("SELECT * FROM sender_rules ORDER BY senderCode ASC")
    suspend fun getSenderRules(): List<SenderRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSenderRule(rule: SenderRuleEntity): Long

    @Query("DELETE FROM sender_rules WHERE id = :id")
    suspend fun deleteSenderRule(id: Long)

    @Query("SELECT * FROM dismissed_exclusion_suggestions")
    fun observeDismissedSuggestions(): Flow<List<DismissedExclusionSuggestionEntity>>

    @Query("SELECT * FROM dismissed_exclusion_suggestions")
    suspend fun getDismissedSuggestions(): List<DismissedExclusionSuggestionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDismissedSuggestion(item: DismissedExclusionSuggestionEntity): Long
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY categoryId ASC")
    fun observeBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets ORDER BY categoryId ASC")
    suspend fun getBudgets(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudget(id: Long)

    @Query("SELECT * FROM splits")
    fun observeSplits(): Flow<List<TransactionSplitEntity>>

    @Query("SELECT * FROM splits")
    suspend fun getAllSplits(): List<TransactionSplitEntity>

    @Query("SELECT * FROM splits WHERE transactionId = :transactionId")
    suspend fun getSplitsForTransaction(transactionId: Long): List<TransactionSplitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplits(splits: List<TransactionSplitEntity>)

    @Query("DELETE FROM splits WHERE transactionId = :transactionId")
    suspend fun deleteSplitsForTransaction(transactionId: Long)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings")
    fun observeAll(): Flow<List<AppSettingEntity>>

    @Query("SELECT * FROM settings")
    suspend fun getAll(): List<AppSettingEntity>

    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: AppSettingEntity)

    @Query("DELETE FROM settings WHERE `key` = :key")
    suspend fun delete(key: String)
}

package com.banksms.expenses.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_raw",
    indices = [
        Index(value = ["hash"], unique = true),
        Index(value = ["parseStatus"]),
        Index(value = ["receivedAt"])
    ]
)
data class SmsRawEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val body: String,
    val receivedAt: Long,
    val hash: String,
    val parseStatus: ParseStatus,
    val confidence: Float = 0f,
    val failureReason: String? = null
)

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["bankName", "last4", "type"])
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayName: String,
    val bankName: String,
    val type: AccountType,
    val last4: String? = null,
    val lastBalance: Double? = null,
    val creditLimit: Double? = null,
    val statementDueDate: String? = null,
    val accentColorHex: Long,
    val isActive: Boolean = true,
    val mergedIntoAccountId: Long? = null
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconKey: String = "category",
    val colorHex: Long = 0xFF475569,
    val parentId: Long? = null,
    val isBuiltin: Boolean = false
)

@Entity(
    tableName = "merchant_aliases",
    indices = [Index(value = ["rawPattern"], unique = true)]
)
data class MerchantAliasEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawPattern: String,
    val canonicalName: String,
    val defaultCategoryId: Long? = null
)

@Entity(
    tableName = "category_rules",
    indices = [Index(value = ["priority"])]
)
data class CategoryRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val priority: Int,
    val categoryId: Long,
    val merchantPattern: String? = null,
    val merchantMatchMode: String = "CONTAINS",
    val bodyKeyword: String? = null,
    val senderPattern: String? = null,
    val vpaPattern: String? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val accountId: Long? = null,
    val direction: TransactionType? = null,
    val regexPattern: String? = null,
    val alsoExclude: Boolean = false,
    val exclusionReason: ExclusionReason? = null,
    val enabled: Boolean = true,
    val matchCount: Int = 0,
    val lastMatchedAt: Long? = null
)

@Entity(tableName = "exclusion_rules")
data class ExclusionRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val merchantPattern: String? = null,
    val bodyKeyword: String? = null,
    val regexPattern: String? = null,
    val exactAmount: Double? = null,
    val amountTolerance: Double = 1.0,
    val accountId: Long? = null,
    val reason: ExclusionReason,
    val enabled: Boolean = true,
    val applyRetroactively: Boolean = true,
    val matchCount: Int = 0,
    val lastMatchedAt: Long? = null
)

@Entity(
    tableName = "dismissed_exclusion_suggestions",
    indices = [Index(value = ["suggestionKey"], unique = true)]
)
data class DismissedExclusionSuggestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val suggestionKey: String
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long? = null,
    val amount: Double,
    val period: String = "MONTHLY"
)

@Entity(
    tableName = "splits",
    indices = [Index(value = ["transactionId"])]
)
data class TransactionSplitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val categoryId: Long,
    val amount: Double,
    val note: String = ""
)

@Entity(tableName = "parse_templates")
data class ParseTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val senderPattern: String,
    val regexPattern: String,
    val amountGroup: Int = 1,
    val merchantGroup: Int? = null,
    val accountGroup: Int? = null,
    val balanceGroup: Int? = null,
    val defaultDirection: TransactionType? = null,
    val isBuiltin: Boolean = false,
    val enabled: Boolean = true
)

@Entity(
    tableName = "sender_rules",
    indices = [Index(value = ["senderCode"], unique = true)]
)
data class SenderRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderCode: String,
    val action: String // "INCLUDE" or "IGNORE"
)

@Entity(tableName = "settings")
data class AppSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

package com.banksms.expenses.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["fingerprint"], unique = true),
        Index(value = ["bankName"]),
        Index(value = ["txnTime"]),
        Index(value = ["accountId", "txnTime"]),
        Index(value = ["categoryId", "txnTime"]),
        Index(value = ["isExcluded"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val smsId: Long? = null,
    val accountId: Long? = null,
    val sender: String,
    val bankName: String,
    val accountLast4: String? = null,
    val amount: Double,
    val currency: String,
    val type: TransactionType,
    val merchant: String,
    val rawMerchant: String = merchant,
    val categoryId: Long,
    val categorySource: CategorySource = CategorySource.BUILTIN,
    val channel: TransactionChannel = TransactionChannel.OTHER,
    val refNo: String? = null,
    val balanceAfter: Double? = null,
    val creditLimitAfter: Double? = null,
    val txnTime: Long,
    val rawBody: String,
    val fingerprint: String,
    val isExcluded: Boolean = false,
    val exclusionReason: ExclusionReason? = null,
    val exclusionSource: ExclusionSource = ExclusionSource.NONE,
    val exclusionRuleId: Long? = null,
    val matchedTransferId: Long? = null,
    val note: String = "",
    val tags: String = "",
    val isManual: Boolean = false,
    val isInternational: Boolean = false,
    val linkedOriginalTxnId: Long? = null
)

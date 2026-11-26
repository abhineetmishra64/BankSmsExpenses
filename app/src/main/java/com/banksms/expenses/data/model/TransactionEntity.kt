package com.banksms.expenses.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["fingerprint"], unique = true),
        Index(value = ["bankName"]),
        Index(value = ["timestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val smsId: Long? = null,
    val sender: String,
    val bankName: String,
    val accountLast4: String? = null,
    val amount: Double,
    val currency: String,
    val type: TransactionType,
    val merchant: String,
    val category: ExpenseCategory,
    val balanceAfter: Double? = null,
    val timestamp: Long,
    val rawBody: String,
    val fingerprint: String
)

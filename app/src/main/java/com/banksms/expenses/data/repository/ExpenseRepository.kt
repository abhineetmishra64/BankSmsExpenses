package com.banksms.expenses.data.repository

import com.banksms.expenses.data.local.TransactionDao
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.sms.RawSmsMessage
import com.banksms.expenses.data.sms.SmsReader
import com.banksms.expenses.data.sms.SmsTransactionParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class SyncResult(
    val scannedMessages: Int,
    val parsedTransactions: Int,
    val newlyAdded: Int
)

class ExpenseRepository(
    private val dao: TransactionDao,
    private val smsReader: SmsReader
) {
    val transactions: Flow<List<TransactionEntity>> = dao.observeAllTransactions()

    suspend fun syncFromSmsInbox(): SyncResult = withContext(Dispatchers.IO) {
        val rawMessages = smsReader.readInboxMessages()
        ingestMessages(rawMessages)
    }

    suspend fun loadSampleTransactions(): SyncResult = withContext(Dispatchers.IO) {
        val sampleMessages = smsReader.buildSampleMessages()
        ingestMessages(sampleMessages)
    }

    suspend fun ingestMessages(messages: List<RawSmsMessage>): SyncResult = withContext(Dispatchers.IO) {
        val entities = messages.mapNotNull { msg ->
            SmsTransactionParser.parse(
                smsId = msg.id,
                sender = msg.sender,
                body = msg.body,
                timestamp = msg.timestamp
            )?.let { parsed ->
                TransactionEntity(
                    smsId = parsed.smsId,
                    sender = parsed.sender,
                    bankName = parsed.bankName,
                    accountLast4 = parsed.accountLast4,
                    amount = parsed.amount,
                    currency = parsed.currency,
                    type = parsed.type,
                    merchant = parsed.merchant,
                    category = parsed.category,
                    balanceAfter = parsed.balanceAfter,
                    timestamp = parsed.timestamp,
                    rawBody = parsed.rawBody,
                    fingerprint = parsed.fingerprint
                )
            }
        }

        val insertResults = if (entities.isEmpty()) emptyList() else dao.insertAll(entities)
        val newlyAdded = insertResults.count { it > 0L }

        SyncResult(
            scannedMessages = messages.size,
            parsedTransactions = entities.size,
            newlyAdded = newlyAdded
        )
    }

    suspend fun updateCategory(transactionId: Long, category: ExpenseCategory) {
        withContext(Dispatchers.IO) {
            dao.updateCategory(transactionId, category)
        }
    }

    suspend fun deleteTransaction(transactionId: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteById(transactionId)
        }
    }

    suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}

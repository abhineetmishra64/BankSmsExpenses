package com.banksms.expenses.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionType
import com.banksms.expenses.data.repository.ExpenseRepository
import com.banksms.expenses.data.sms.BankDirectory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TimeframeFilter(val label: String) {
    THIS_MONTH("This Month"),
    LAST_30_DAYS("Last 30 Days"),
    TODAY("Today"),
    ALL_TIME("All Time")
}

enum class TypeFilter(val label: String) {
    EXPENSES_ONLY("Expenses"),
    CREDITS_ONLY("Credits"),
    ALL("All")
}

data class BankSummary(
    val bankName: String,
    val accentColorHex: Long,
    val primaryCurrency: String,
    val totalExpenses: Double,
    val totalCredits: Double,
    val expenseCount: Int,
    val creditCount: Int,
    val latestBalance: Double?,
    val shareOfTotalExpenses: Float
)

data class CategoryBreakdownItem(
    val category: ExpenseCategory,
    val amount: Double,
    val count: Int,
    val percentage: Float
)

data class ExpensesUiState(
    val selectedBank: String? = null, // null == All Banks combined
    val timeframe: TimeframeFilter = TimeframeFilter.THIS_MONTH,
    val typeFilter: TypeFilter = TypeFilter.EXPENSES_ONLY,
    val searchQuery: String = "",
    val allBanksSummary: BankSummary = BankSummary(
        bankName = "All Banks",
        accentColorHex = 0xFF0F4C81,
        primaryCurrency = "₹",
        totalExpenses = 0.0,
        totalCredits = 0.0,
        expenseCount = 0,
        creditCount = 0,
        latestBalance = null,
        shareOfTotalExpenses = 1f
    ),
    val perBankSummaries: List<BankSummary> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdownItem> = emptyList(),
    val visibleTransactions: List<TransactionEntity> = emptyList(),
    val totalStoredTransactions: Int = 0,
    val isSyncing: Boolean = false,
    val statusMessage: String? = null
)

private data class FilterState(
    val selectedBank: String? = null,
    val timeframe: TimeframeFilter = TimeframeFilter.THIS_MONTH,
    val typeFilter: TypeFilter = TypeFilter.EXPENSES_ONLY,
    val searchQuery: String = "",
    val isSyncing: Boolean = false,
    val statusMessage: String? = null
)

class ExpensesViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val filterState = MutableStateFlow(FilterState())

    val uiState: StateFlow<ExpensesUiState> = combine(
        repository.transactions,
        filterState
    ) { allTransactions, filters ->
        val minTimestamp = computeMinTimestamp(filters.timeframe)
        val timeFiltered = if (minTimestamp == null) {
            allTransactions
        } else {
            allTransactions.filter { it.timestamp >= minTimestamp }
        }

        val totalDebitsAllBanks = timeFiltered
            .filter { it.type == TransactionType.DEBIT }
            .sumOf { it.amount }
        val totalCreditsAllBanks = timeFiltered
            .filter { it.type == TransactionType.CREDIT }
            .sumOf { it.amount }
        val dominantCurrency = timeFiltered
            .groupingBy { it.currency }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: "₹"

        val allBanksSummary = BankSummary(
            bankName = "All Banks",
            accentColorHex = 0xFF0F4C81,
            primaryCurrency = dominantCurrency,
            totalExpenses = totalDebitsAllBanks,
            totalCredits = totalCreditsAllBanks,
            expenseCount = timeFiltered.count { it.type == TransactionType.DEBIT },
            creditCount = timeFiltered.count { it.type == TransactionType.CREDIT },
            latestBalance = null,
            shareOfTotalExpenses = 1f
        )

        val perBankSummaries = timeFiltered
            .groupBy { it.bankName }
            .map { (bankName, bankTxns) ->
                val bankDebits = bankTxns.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
                val bankCredits = bankTxns.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
                val bankCurrency = bankTxns.firstOrNull()?.currency ?: dominantCurrency
                val latestBalance = bankTxns.firstOrNull { it.balanceAfter != null }?.balanceAfter
                val share = if (totalDebitsAllBanks > 0.0) {
                    (bankDebits / totalDebitsAllBanks).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }

                BankSummary(
                    bankName = bankName,
                    accentColorHex = BankDirectory.accentColorForBank(bankName),
                    primaryCurrency = bankCurrency,
                    totalExpenses = bankDebits,
                    totalCredits = bankCredits,
                    expenseCount = bankTxns.count { it.type == TransactionType.DEBIT },
                    creditCount = bankTxns.count { it.type == TransactionType.CREDIT },
                    latestBalance = latestBalance,
                    shareOfTotalExpenses = share
                )
            }
            .sortedByDescending { it.totalExpenses }

        val activeBank = filters.selectedBank?.takeIf { selected ->
            perBankSummaries.any { it.bankName == selected }
        }

        val bankScoped = if (activeBank == null) {
            timeFiltered
        } else {
            timeFiltered.filter { it.bankName == activeBank }
        }

        val debitForBreakdown = bankScoped.filter { it.type == TransactionType.DEBIT }
        val totalDebitScoped = debitForBreakdown.sumOf { it.amount }
        val categoryBreakdown = debitForBreakdown
            .groupBy { it.category }
            .map { (category, txns) ->
                val sum = txns.sumOf { it.amount }
                val pct = if (totalDebitScoped > 0.0) (sum / totalDebitScoped).toFloat() else 0f
                CategoryBreakdownItem(
                    category = category,
                    amount = sum,
                    count = txns.size,
                    percentage = pct
                )
            }
            .sortedByDescending { it.amount }

        val typeFiltered = when (filters.typeFilter) {
            TypeFilter.EXPENSES_ONLY -> bankScoped.filter { it.type == TransactionType.DEBIT }
            TypeFilter.CREDITS_ONLY -> bankScoped.filter { it.type == TransactionType.CREDIT }
            TypeFilter.ALL -> bankScoped
        }

        val query = filters.searchQuery.trim().lowercase()
        val visible = if (query.isEmpty()) {
            typeFiltered
        } else {
            typeFiltered.filter { txn ->
                txn.merchant.lowercase().contains(query) ||
                    txn.bankName.lowercase().contains(query) ||
                    txn.category.label.lowercase().contains(query) ||
                    txn.rawBody.lowercase().contains(query)
            }
        }

        ExpensesUiState(
            selectedBank = activeBank,
            timeframe = filters.timeframe,
            typeFilter = filters.typeFilter,
            searchQuery = filters.searchQuery,
            allBanksSummary = allBanksSummary,
            perBankSummaries = perBankSummaries,
            categoryBreakdown = categoryBreakdown,
            visibleTransactions = visible,
            totalStoredTransactions = allTransactions.size,
            isSyncing = filters.isSyncing,
            statusMessage = filters.statusMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpensesUiState()
    )

    fun selectBank(bankName: String?) {
        filterState.value = filterState.value.copy(selectedBank = bankName)
    }

    fun selectTimeframe(timeframe: TimeframeFilter) {
        filterState.value = filterState.value.copy(timeframe = timeframe)
    }

    fun selectTypeFilter(typeFilter: TypeFilter) {
        filterState.value = filterState.value.copy(typeFilter = typeFilter)
    }

    fun updateSearchQuery(query: String) {
        filterState.value = filterState.value.copy(searchQuery = query)
    }

    fun clearStatusMessage() {
        filterState.value = filterState.value.copy(statusMessage = null)
    }

    fun syncSmsInbox() {
        if (filterState.value.isSyncing) return
        viewModelScope.launch {
            filterState.value = filterState.value.copy(isSyncing = true, statusMessage = null)
            runCatching {
                repository.syncFromSmsInbox()
            }.onSuccess { result ->
                val msg = if (result.newlyAdded > 0) {
                    "Synced ${result.newlyAdded} new transactions from ${result.scannedMessages} messages"
                } else {
                    "Scanned ${result.scannedMessages} messages — all up to date"
                }
                filterState.value = filterState.value.copy(isSyncing = false, statusMessage = msg)
            }.onFailure { err ->
                filterState.value = filterState.value.copy(
                    isSyncing = false,
                    statusMessage = "Sync failed: ${err.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun loadSampleData() {
        if (filterState.value.isSyncing) return
        viewModelScope.launch {
            filterState.value = filterState.value.copy(isSyncing = true, statusMessage = null)
            val result = repository.loadSampleTransactions()
            filterState.value = filterState.value.copy(
                isSyncing = false,
                statusMessage = "Loaded ${result.newlyAdded} sample bank transactions"
            )
        }
    }

    fun updateTransactionCategory(transactionId: Long, category: ExpenseCategory) {
        viewModelScope.launch {
            repository.updateCategory(transactionId, category)
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.clearAll()
            filterState.value = filterState.value.copy(
                selectedBank = null,
                statusMessage = "Cleared all stored transactions"
            )
        }
    }

    private fun computeMinTimestamp(timeframe: TimeframeFilter): Long? {
        val cal = Calendar.getInstance()
        return when (timeframe) {
            TimeframeFilter.ALL_TIME -> null
            TimeframeFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            TimeframeFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            TimeframeFilter.LAST_30_DAYS -> {
                System.currentTimeMillis() - 30L * 86_400_000L
            }
        }
    }

    class Factory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExpensesViewModel(repository) as T
        }
    }
}

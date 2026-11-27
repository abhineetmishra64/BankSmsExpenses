package com.banksms.expenses.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banksms.expenses.data.analytics.AnalyticsEngine
import com.banksms.expenses.data.analytics.HomeSummary
import com.banksms.expenses.data.analytics.MonthPeriod
import com.banksms.expenses.data.analytics.PeriodHelper
import com.banksms.expenses.data.analytics.RecurringPaymentItem
import com.banksms.expenses.data.analytics.TrendRange
import com.banksms.expenses.data.analytics.TrendSummary
import com.banksms.expenses.data.model.AccountEntity
import com.banksms.expenses.data.model.AccountType
import com.banksms.expenses.data.model.BudgetEntity
import com.banksms.expenses.data.model.CategoryEntity
import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ExclusionRuleEntity
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.MerchantAliasEntity
import com.banksms.expenses.data.model.ParseStatus
import com.banksms.expenses.data.model.ParseTemplateEntity
import com.banksms.expenses.data.model.SenderRuleEntity
import com.banksms.expenses.data.model.SmsRawEntity
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionSplitEntity
import com.banksms.expenses.data.model.TransactionType
import com.banksms.expenses.data.repository.AppPreferences
import com.banksms.expenses.data.repository.CategorisationEngineResultPreview
import com.banksms.expenses.data.repository.ExpenseRepository
import com.banksms.expenses.data.repository.ThemePreference
import com.banksms.expenses.data.sms.ExclusionEngine
import com.banksms.expenses.data.sms.ExclusionSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.Calendar
import java.util.Locale

enum class MainTab(val label: String) {
    HOME("Home"),
    TRENDS("Trends"),
    TRANSACTIONS("Transactions"),
    REVIEW("Review"),
    MORE("More")
}

enum class MoreSubScreen(val title: String) {
    EXCLUSIONS("Exclusions"),
    CATEGORIES_RULES("Categories & Rules"),
    ACCOUNTS("Accounts & Cards"),
    BUDGETS("Budgets"),
    RECURRING("Recurring Payments"),
    BACKUP_SETTINGS("Backup, Export & Settings")
}

enum class TypeFilter(val label: String) {
    ALL("All"),
    EXPENSES_ONLY("Expenses"),
    CREDITS_ONLY("Income/Credits")
}

enum class ExclusionFilter(val label: String) {
    ALL("All"),
    INCLUDED_ONLY("Included"),
    EXCLUDED_ONLY("Excluded")
}

data class UncategorisedMerchantGroup(
    val merchant: String,
    val count: Int,
    val totalAmount: Double,
    val currency: String,
    val sampleTransactions: List<TransactionEntity>
)

data class ExpensesUiState(
    val currentTab: MainTab = MainTab.HOME,
    val moreSubScreen: MoreSubScreen? = null,
    val currentPeriod: MonthPeriod = PeriodHelper.currentPeriod(),
    val accounts: List<AccountEntity> = emptyList(),
    val activeAccounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val categoriesById: Map<Long, CategoryEntity> = emptyMap(),
    val homeSummary: HomeSummary = AnalyticsEngine.computeHomeSummary(
        transactions = emptyList(),
        splitsByTxnId = emptyMap(),
        categoriesById = emptyMap(),
        budgets = emptyList(),
        selectedAccountIds = emptySet(),
        period = PeriodHelper.currentPeriod()
    ),
    val trendRange: TrendRange = TrendRange.TWELVE_MONTHS,
    val trendCustomMonths: Int = 12,
    val trendCategoryId: Long? = null,
    val showNetSavingsLine: Boolean = true,
    val showStackedCategories: Boolean = false,
    val showTrendTable: Boolean = false,
    val selectedTrendPeriod: MonthPeriod? = null,
    val trendSummary: TrendSummary = TrendSummary(emptyList(), 0.0, null, 0.0, null),
    val txnSearchQuery: String = "",
    val txnDirectionFilter: TypeFilter = TypeFilter.ALL,
    val txnCategoryFilterId: Long? = null,
    val txnExclusionFilter: ExclusionFilter = ExclusionFilter.ALL,
    val txnUncategorisedOnly: Boolean = false,
    val txnMonthScoped: Boolean = false,
    val txnMinAmount: String = "",
    val txnMaxAmount: String = "",
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val excludedTransactions: List<TransactionEntity> = emptyList(),
    val uncategorisedGroups: List<UncategorisedMerchantGroup> = emptyList(),
    val unparsedSms: List<SmsRawEntity> = emptyList(),
    val exclusionSuggestions: List<ExclusionSuggestion> = emptyList(),
    val needsAttentionCount: Int = 0,
    val categoryRules: List<CategoryRuleEntity> = emptyList(),
    val exclusionRules: List<ExclusionRuleEntity> = emptyList(),
    val aliases: List<MerchantAliasEntity> = emptyList(),
    val parseTemplates: List<ParseTemplateEntity> = emptyList(),
    val senderRules: List<SenderRuleEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val splitsByTxnId: Map<Long, List<TransactionSplitEntity>> = emptyMap(),
    val recurringPayments: List<RecurringPaymentItem> = emptyList(),
    val preferences: AppPreferences = AppPreferences(),
    val backfillMonths: Int? = 3,
    val isSyncing: Boolean = false,
    val statusMessage: String? = null,
    val ruleTestPreview: CategorisationEngineResultPreview? = null
)

private data class UiControlState(
    val currentTab: MainTab = MainTab.HOME,
    val moreSubScreen: MoreSubScreen? = null,
    val currentPeriod: MonthPeriod = PeriodHelper.currentPeriod(),
    val trendRange: TrendRange = TrendRange.TWELVE_MONTHS,
    val trendCustomMonths: Int = 12,
    val trendCategoryId: Long? = null,
    val showNetSavingsLine: Boolean = true,
    val showStackedCategories: Boolean = false,
    val showTrendTable: Boolean = false,
    val selectedTrendPeriod: MonthPeriod? = null,
    val txnSearchQuery: String = "",
    val txnDirectionFilter: TypeFilter = TypeFilter.ALL,
    val txnCategoryFilterId: Long? = null,
    val txnExclusionFilter: ExclusionFilter = ExclusionFilter.ALL,
    val txnUncategorisedOnly: Boolean = false,
    val txnMonthScoped: Boolean = false,
    val txnMinAmount: String = "",
    val txnMaxAmount: String = "",
    val backfillMonths: Int? = 3,
    val isSyncing: Boolean = false,
    val statusMessage: String? = null,
    val ruleTestPreview: CategorisationEngineResultPreview? = null
)

private data class RulesBundleState(
    val categoryRules: List<CategoryRuleEntity>,
    val exclusionRules: List<ExclusionRuleEntity>,
    val aliases: List<MerchantAliasEntity>,
    val parseTemplates: List<ParseTemplateEntity>,
    val senderRules: List<SenderRuleEntity>,
    val dismissedKeys: Set<String>
)

private data class MetaBundleState(
    val budgets: List<BudgetEntity>,
    val splits: List<TransactionSplitEntity>,
    val preferences: AppPreferences,
    val rawSms: List<SmsRawEntity>
)

class ExpensesViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val controls = MutableStateFlow(UiControlState())

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    private val rulesFlow = combine(
        repository.categoryRules,
        repository.exclusionRules,
        repository.aliases,
        repository.parseTemplates,
        repository.senderRules,
        repository.dismissedSuggestions
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        RulesBundleState(
            categoryRules = arr[0] as List<CategoryRuleEntity>,
            exclusionRules = arr[1] as List<ExclusionRuleEntity>,
            aliases = arr[2] as List<MerchantAliasEntity>,
            parseTemplates = arr[3] as List<ParseTemplateEntity>,
            senderRules = arr[4] as List<SenderRuleEntity>,
            dismissedKeys = (arr[5] as List<com.banksms.expenses.data.model.DismissedExclusionSuggestionEntity>)
                .map { it.suggestionKey }
                .toSet()
        )
    }

    private val metaFlow = combine(
        repository.budgets,
        repository.splits,
        repository.settings,
        repository.rawSmsMessages
    ) { budgets, splits, settings, rawSms ->
        MetaBundleState(
            budgets = budgets,
            splits = splits,
            preferences = ExpenseRepository.parsePreferences(settings),
            rawSms = rawSms
        )
    }

    val uiState: StateFlow<ExpensesUiState> = combine(
        repository.transactions,
        repository.accounts,
        repository.categories,
        rulesFlow,
        metaFlow,
        controls
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        val allTransactions = arr[0] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val accounts = arr[1] as List<AccountEntity>
        @Suppress("UNCHECKED_CAST")
        val categories = arr[2] as List<CategoryEntity>
        val rulesState = arr[3] as RulesBundleState
        val meta = arr[4] as MetaBundleState
        val ctrl = arr[5] as UiControlState

        buildUiState(allTransactions, accounts, categories, rulesState, meta, ctrl)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpensesUiState()
    )

    private fun buildUiState(
        allTransactions: List<TransactionEntity>,
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>,
        rulesState: RulesBundleState,
        meta: MetaBundleState,
        ctrl: UiControlState
    ): ExpensesUiState {
        val prefs = meta.preferences
        val activeAccounts = accounts.filter { it.isActive && it.mergedIntoAccountId == null }
        val activeIds = activeAccounts.map { it.id }.toSet()
        val selectedAccountIds = prefs.selectedAccountIds.intersect(activeIds)

        val categoriesById = categories.associateBy { it.id }
        val splitsByTxnId = meta.splits.groupBy { it.transactionId }

        val homeSummary = AnalyticsEngine.computeHomeSummary(
            transactions = allTransactions,
            splitsByTxnId = splitsByTxnId,
            categoriesById = categoriesById,
            budgets = meta.budgets,
            selectedAccountIds = selectedAccountIds,
            period = ctrl.currentPeriod,
            monthStartDay = prefs.monthStartDay
        )

        val effectiveTrendRange = if (prefs.financialYearMode && ctrl.trendRange == TrendRange.TWELVE_MONTHS) {
            TrendRange.FINANCIAL_YEAR
        } else {
            ctrl.trendRange
        }

        val trendSummary = AnalyticsEngine.computeTrends(
            transactions = allTransactions,
            splitsByTxnId = splitsByTxnId,
            categoriesById = categoriesById,
            selectedAccountIds = selectedAccountIds,
            categoryFilterId = ctrl.trendCategoryId,
            range = effectiveTrendRange,
            referencePeriod = PeriodHelper.currentPeriod(monthStartDay = prefs.monthStartDay),
            monthStartDay = prefs.monthStartDay,
            customMonths = ctrl.trendCustomMonths
        )

        val filteredTxns = filterTransactions(allTransactions, selectedAccountIds, prefs.monthStartDay, ctrl, categoriesById)
        val excludedTxns = allTransactions.filter { it.isExcluded }

        val uncategorisedGroups = allTransactions
            .filter { !it.isExcluded && it.categoryId == ExpenseCategory.UNCATEGORISED.id }
            .groupBy { it.merchant }
            .map { (merchant, list) ->
                UncategorisedMerchantGroup(
                    merchant = merchant,
                    count = list.size,
                    totalAmount = list.sumOf { it.amount },
                    currency = list.firstOrNull()?.currency ?: "₹",
                    sampleTransactions = list
                )
            }
            .sortedByDescending { it.count }

        val unparsedSms = meta.rawSms.filter { it.parseStatus == ParseStatus.UNPARSED }
        val exclusionSuggestions = ExclusionEngine.findSuggestions(allTransactions, rulesState.dismissedKeys)
        val needsAttention = allTransactions.count { !it.isExcluded && it.categoryId == ExpenseCategory.UNCATEGORISED.id } +
            unparsedSms.size +
            exclusionSuggestions.size

        val recurring = AnalyticsEngine.detectRecurringPayments(allTransactions)

        return ExpensesUiState(
            currentTab = ctrl.currentTab,
            moreSubScreen = ctrl.moreSubScreen,
            currentPeriod = ctrl.currentPeriod,
            accounts = accounts,
            activeAccounts = activeAccounts,
            categories = categories,
            categoriesById = categoriesById,
            homeSummary = homeSummary,
            trendRange = ctrl.trendRange,
            trendCustomMonths = ctrl.trendCustomMonths,
            trendCategoryId = ctrl.trendCategoryId,
            showNetSavingsLine = ctrl.showNetSavingsLine,
            showStackedCategories = ctrl.showStackedCategories,
            showTrendTable = ctrl.showTrendTable,
            selectedTrendPeriod = ctrl.selectedTrendPeriod,
            trendSummary = trendSummary,
            txnSearchQuery = ctrl.txnSearchQuery,
            txnDirectionFilter = ctrl.txnDirectionFilter,
            txnCategoryFilterId = ctrl.txnCategoryFilterId,
            txnExclusionFilter = ctrl.txnExclusionFilter,
            txnUncategorisedOnly = ctrl.txnUncategorisedOnly,
            txnMonthScoped = ctrl.txnMonthScoped,
            txnMinAmount = ctrl.txnMinAmount,
            txnMaxAmount = ctrl.txnMaxAmount,
            filteredTransactions = filteredTxns,
            allTransactions = allTransactions,
            excludedTransactions = excludedTxns,
            uncategorisedGroups = uncategorisedGroups,
            unparsedSms = unparsedSms,
            exclusionSuggestions = exclusionSuggestions,
            needsAttentionCount = needsAttention,
            categoryRules = rulesState.categoryRules,
            exclusionRules = rulesState.exclusionRules,
            aliases = rulesState.aliases,
            parseTemplates = rulesState.parseTemplates,
            senderRules = rulesState.senderRules,
            budgets = meta.budgets,
            splitsByTxnId = splitsByTxnId,
            recurringPayments = recurring,
            preferences = prefs,
            backfillMonths = ctrl.backfillMonths,
            isSyncing = ctrl.isSyncing,
            statusMessage = ctrl.statusMessage,
            ruleTestPreview = ctrl.ruleTestPreview
        )
    }

    private fun filterTransactions(
        all: List<TransactionEntity>,
        selectedAccountIds: Set<Long>,
        monthStartDay: Int,
        ctrl: UiControlState,
        categoriesById: Map<Long, CategoryEntity>
    ): List<TransactionEntity> {
        val (startMs, endMs) = PeriodHelper.periodBoundsMillis(ctrl.currentPeriod, monthStartDay)
        val minAmt = ctrl.txnMinAmount.toDoubleOrNull()
        val maxAmt = ctrl.txnMaxAmount.toDoubleOrNull()
        val query = ctrl.txnSearchQuery.trim().lowercase(Locale.ROOT)

        return all.filter { txn ->
            if (selectedAccountIds.isNotEmpty() && txn.accountId !in selectedAccountIds) return@filter false
            if (ctrl.txnMonthScoped && txn.txnTime !in startMs until endMs) return@filter false
            when (ctrl.txnDirectionFilter) {
                TypeFilter.EXPENSES_ONLY -> if (txn.type != TransactionType.DEBIT) return@filter false
                TypeFilter.CREDITS_ONLY -> if (txn.type != TransactionType.CREDIT) return@filter false
                TypeFilter.ALL -> Unit
            }
            if (ctrl.txnCategoryFilterId != null && txn.categoryId != ctrl.txnCategoryFilterId) return@filter false
            when (ctrl.txnExclusionFilter) {
                ExclusionFilter.INCLUDED_ONLY -> if (txn.isExcluded) return@filter false
                ExclusionFilter.EXCLUDED_ONLY -> if (!txn.isExcluded) return@filter false
                ExclusionFilter.ALL -> Unit
            }
            if (ctrl.txnUncategorisedOnly && txn.categoryId != ExpenseCategory.UNCATEGORISED.id) return@filter false
            if (minAmt != null && txn.amount < minAmt) return@filter false
            if (maxAmt != null && txn.amount > maxAmt) return@filter false
            if (query.isNotEmpty()) {
                val catName = categoriesById[txn.categoryId]?.name.orEmpty().lowercase(Locale.ROOT)
                val matched = txn.merchant.lowercase(Locale.ROOT).contains(query) ||
                    txn.bankName.lowercase(Locale.ROOT).contains(query) ||
                    txn.rawBody.lowercase(Locale.ROOT).contains(query) ||
                    txn.note.lowercase(Locale.ROOT).contains(query) ||
                    txn.tags.lowercase(Locale.ROOT).contains(query) ||
                    catName.contains(query)
                if (!matched) return@filter false
            }
            true
        }
    }

    // ---- Navigation & Filters ----

    fun selectTab(tab: MainTab) {
        controls.value = controls.value.copy(
            currentTab = tab,
            moreSubScreen = if (tab == MainTab.MORE) controls.value.moreSubScreen else null
        )
    }

    fun openMoreSubScreen(subScreen: MoreSubScreen?) {
        controls.value = controls.value.copy(
            currentTab = MainTab.MORE,
            moreSubScreen = subScreen
        )
    }

    fun openExclusionsForCurrentMonth() {
        controls.value = controls.value.copy(
            currentTab = MainTab.MORE,
            moreSubScreen = MoreSubScreen.EXCLUSIONS
        )
    }

    fun openTransactionsForCategory(categoryId: Long) {
        controls.value = controls.value.copy(
            currentTab = MainTab.TRANSACTIONS,
            txnCategoryFilterId = categoryId,
            txnMonthScoped = true,
            txnDirectionFilter = TypeFilter.EXPENSES_ONLY
        )
    }

    fun openMonthOnHome(period: MonthPeriod) {
        controls.value = controls.value.copy(
            currentTab = MainTab.HOME,
            currentPeriod = period
        )
    }

    fun previousMonth() {
        controls.value = controls.value.copy(currentPeriod = controls.value.currentPeriod.previous())
    }

    fun nextMonth() {
        controls.value = controls.value.copy(currentPeriod = controls.value.currentPeriod.next())
    }

    fun toggleAccountFilter(accountId: Long?) {
        viewModelScope.launch {
            if (accountId == null) {
                repository.setSelectedAccountIds(emptySet())
            } else {
                val current = uiState.value.preferences.selectedAccountIds.toMutableSet()
                if (accountId in current) current.remove(accountId) else current.add(accountId)
                repository.setSelectedAccountIds(current)
            }
        }
    }

    fun setTrendRange(range: TrendRange) {
        controls.value = controls.value.copy(trendRange = range, selectedTrendPeriod = null)
    }

    fun setTrendCustomMonths(months: Int) {
        controls.value = controls.value.copy(trendCustomMonths = months.coerceIn(1, 36))
    }

    fun setTrendCategoryFilter(categoryId: Long?) {
        controls.value = controls.value.copy(trendCategoryId = categoryId)
    }

    fun toggleNetSavingsLine() {
        controls.value = controls.value.copy(showNetSavingsLine = !controls.value.showNetSavingsLine)
    }

    fun toggleStackedCategories() {
        controls.value = controls.value.copy(showStackedCategories = !controls.value.showStackedCategories)
    }

    fun toggleTrendTable() {
        controls.value = controls.value.copy(showTrendTable = !controls.value.showTrendTable)
    }

    fun selectTrendPeriod(period: MonthPeriod?) {
        controls.value = controls.value.copy(selectedTrendPeriod = period)
    }

    fun updateTxnSearchQuery(query: String) {
        controls.value = controls.value.copy(txnSearchQuery = query)
    }

    fun setTxnDirectionFilter(filter: TypeFilter) {
        controls.value = controls.value.copy(txnDirectionFilter = filter)
    }

    fun setTxnCategoryFilter(categoryId: Long?) {
        controls.value = controls.value.copy(txnCategoryFilterId = categoryId)
    }

    fun setTxnExclusionFilter(filter: ExclusionFilter) {
        controls.value = controls.value.copy(txnExclusionFilter = filter)
    }

    fun toggleTxnUncategorisedOnly() {
        controls.value = controls.value.copy(txnUncategorisedOnly = !controls.value.txnUncategorisedOnly)
    }

    fun toggleTxnMonthScoped() {
        controls.value = controls.value.copy(txnMonthScoped = !controls.value.txnMonthScoped)
    }

    fun setTxnAmountRange(min: String, max: String) {
        controls.value = controls.value.copy(txnMinAmount = min, txnMaxAmount = max)
    }

    fun clearTransactionFilters() {
        controls.value = controls.value.copy(
            txnSearchQuery = "",
            txnDirectionFilter = TypeFilter.ALL,
            txnCategoryFilterId = null,
            txnExclusionFilter = ExclusionFilter.ALL,
            txnUncategorisedOnly = false,
            txnMonthScoped = false,
            txnMinAmount = "",
            txnMaxAmount = ""
        )
    }

    fun setBackfillMonths(months: Int?) {
        controls.value = controls.value.copy(backfillMonths = months)
    }

    fun clearStatusMessage() {
        controls.value = controls.value.copy(statusMessage = null)
    }

    fun showMessage(msg: String) {
        controls.value = controls.value.copy(statusMessage = msg)
    }

    // ---- SMS Sync & Reprocess ----

    fun syncSmsInbox() {
        if (controls.value.isSyncing) return
        viewModelScope.launch {
            controls.value = controls.value.copy(isSyncing = true, statusMessage = null)
            val sinceMs = controls.value.backfillMonths?.let { months ->
                Calendar.getInstance().apply { add(Calendar.MONTH, -months) }.timeInMillis
            }
            runCatching {
                repository.syncFromSmsInbox(sinceMs)
            }.onSuccess { res ->
                repository.setOnboardingDone(true)
                val msg = if (res.newlyAdded > 0) {
                    "Synced ${res.newlyAdded} new transactions (${res.unparsedCount} unparsed)"
                } else {
                    "Scanned ${res.scannedMessages} messages — up to date"
                }
                controls.value = controls.value.copy(isSyncing = false, statusMessage = msg)
            }.onFailure { err ->
                controls.value = controls.value.copy(
                    isSyncing = false,
                    statusMessage = "Sync failed: ${err.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun loadSampleData() {
        if (controls.value.isSyncing) return
        viewModelScope.launch {
            controls.value = controls.value.copy(isSyncing = true, statusMessage = null)
            val res = repository.loadSampleTransactions()
            repository.setOnboardingDone(true)
            controls.value = controls.value.copy(
                isSyncing = false,
                statusMessage = "Loaded ${res.newlyAdded} sample transactions (${res.unparsedCount} unparsed)"
            )
        }
    }

    fun reprocessAllSms() {
        if (controls.value.isSyncing) return
        viewModelScope.launch {
            controls.value = controls.value.copy(isSyncing = true, statusMessage = null)
            val res = repository.reprocessAllSms()
            controls.value = controls.value.copy(
                isSyncing = false,
                statusMessage = "Re-processed ${res.scannedMessages} messages (${res.newlyAdded} newly parsed)"
            )
        }
    }

    // ---- Transactions, Categories & Rules ----

    fun addManualTransaction(
        merchant: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        accountId: Long?,
        txnTime: Long,
        note: String,
        tags: String
    ) {
        viewModelScope.launch {
            repository.addManualTransaction(merchant, amount, type, categoryId, accountId, txnTime, note, tags)
            showMessage("Added manual transaction")
        }
    }

    fun updateTransactionDetails(id: Long, merchant: String, amount: Double, txnTime: Long, note: String, tags: String) {
        viewModelScope.launch {
            repository.updateTransactionDetails(id, merchant, amount, txnTime, note, tags)
            showMessage("Updated transaction")
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            showMessage("Removed transaction")
        }
    }

    fun updateCategorySingle(transactionId: Long, categoryId: Long) {
        viewModelScope.launch {
            repository.updateCategorySingle(transactionId, categoryId)
        }
    }

    fun alwaysCategoriseMerchant(merchant: String, categoryId: Long) {
        viewModelScope.launch {
            repository.alwaysCategoriseMerchant(merchant, categoryId, applyToPast = true)
            showMessage("Created rule for $merchant")
        }
    }

    fun categoriseMerchantGroup(merchant: String, categoryId: Long) {
        viewModelScope.launch {
            repository.categoriseMerchantGroup(merchant, categoryId)
            showMessage("Categorised all $merchant transactions")
        }
    }

    fun saveCategoryRule(rule: CategoryRuleEntity) {
        viewModelScope.launch {
            repository.saveCategoryRule(rule, applyRetroactively = true)
            showMessage("Saved category rule")
        }
    }

    fun deleteCategoryRule(ruleId: Long) {
        viewModelScope.launch {
            repository.deleteCategoryRule(ruleId)
        }
    }

    fun toggleCategoryRule(rule: CategoryRuleEntity) {
        viewModelScope.launch {
            repository.toggleCategoryRule(rule)
        }
    }

    fun moveCategoryRule(ruleId: Long, moveUp: Boolean) {
        viewModelScope.launch {
            repository.moveCategoryRule(ruleId, moveUp)
        }
    }

    fun testRulesOnSampleSms(sender: String, body: String) {
        viewModelScope.launch {
            val preview = repository.testRulesOnSampleSms(sender, body)
            controls.value = controls.value.copy(ruleTestPreview = preview)
        }
    }

    fun clearRuleTestPreview() {
        controls.value = controls.value.copy(ruleTestPreview = null)
    }

    fun addCategory(name: String, colorHex: Long) {
        viewModelScope.launch {
            repository.addCategory(name, colorHex)
            showMessage("Added category $name")
        }
    }

    fun renameCategory(category: CategoryEntity, newName: String, newColor: Long) {
        viewModelScope.launch {
            repository.updateCategory(category.copy(name = newName.trim(), colorHex = newColor))
        }
    }

    fun mergeCategories(sourceId: Long, targetId: Long) {
        viewModelScope.launch {
            repository.mergeCategories(sourceId, targetId)
            showMessage("Merged categories")
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    // ---- Exclusions ----

    fun excludeSingle(transactionId: Long, reason: ExclusionReason) {
        viewModelScope.launch {
            repository.excludeSingleTransaction(transactionId, reason)
            showMessage("Excluded from summary")
        }
    }

    fun excludeSimilar(
        name: String,
        reason: ExclusionReason,
        merchantPattern: String?,
        bodyKeyword: String?,
        exactAmount: Double?
    ) {
        viewModelScope.launch {
            repository.createExclusionRuleAndApply(
                name = name,
                reason = reason,
                merchantPattern = merchantPattern,
                bodyKeyword = bodyKeyword,
                exactAmount = exactAmount,
                applyRetroactively = true
            )
            showMessage("Exclusion rule applied")
        }
    }

    fun includeTransactionsAgain(ids: Set<Long>) {
        viewModelScope.launch {
            repository.includeTransactionsAgain(ids)
            showMessage("Included ${ids.size} transaction(s) in summary")
        }
    }

    fun bulkExcludeByQuery(query: String, reason: ExclusionReason, exclude: Boolean) {
        viewModelScope.launch {
            val count = repository.bulkExcludeByQuery(query, reason, exclude)
            showMessage(if (exclude) "Excluded $count matching transactions" else "Included $count matching transactions")
        }
    }

    fun toggleExclusionRule(rule: ExclusionRuleEntity) {
        viewModelScope.launch { repository.toggleExclusionRule(rule) }
    }

    fun resetExclusionRule(ruleId: Long) {
        viewModelScope.launch {
            repository.resetExclusionRule(ruleId)
            showMessage("Reset exclusion rule and re-included matches")
        }
    }

    fun acceptExclusionSuggestion(suggestion: ExclusionSuggestion) {
        viewModelScope.launch {
            repository.acceptExclusionSuggestion(suggestion.transaction.id, suggestion.suggestedReason)
            showMessage("Excluded ${suggestion.transaction.merchant}")
        }
    }

    fun dismissExclusionSuggestion(suggestion: ExclusionSuggestion) {
        viewModelScope.launch {
            repository.dismissExclusionSuggestion(suggestion.suggestionKey)
        }
    }

    // ---- Accounts, Budgets, Splits, Aliases, Templates, Settings ----

    fun renameAccount(accountId: Long, name: String) {
        viewModelScope.launch { repository.renameAccount(accountId, name) }
    }

    fun updateAccountType(accountId: Long, type: AccountType) {
        viewModelScope.launch { repository.updateAccountType(accountId, type) }
    }

    fun toggleAccountActive(accountId: Long) {
        viewModelScope.launch { repository.toggleAccountActive(accountId) }
    }

    fun mergeAccounts(sourceId: Long, targetId: Long) {
        viewModelScope.launch {
            repository.mergeAccounts(sourceId, targetId)
            showMessage("Accounts merged")
        }
    }

    fun upsertBudget(categoryId: Long?, amount: Double) {
        viewModelScope.launch {
            repository.upsertBudget(categoryId, amount)
            showMessage("Saved budget")
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch { repository.deleteBudget(id) }
    }

    fun saveSplits(transactionId: Long, splits: List<TransactionSplitEntity>) {
        viewModelScope.launch {
            repository.saveSplits(transactionId, splits)
            showMessage("Saved category splits")
        }
    }

    fun addAlias(rawPattern: String, canonicalName: String, defaultCategoryId: Long?) {
        viewModelScope.launch {
            repository.addAlias(rawPattern, canonicalName, defaultCategoryId)
            repository.reprocessAllSms()
            showMessage("Saved merchant alias")
        }
    }

    fun deleteAlias(id: Long) {
        viewModelScope.launch { repository.deleteAlias(id) }
    }

    fun saveParseTemplateAndReprocess(template: ParseTemplateEntity) {
        viewModelScope.launch {
            repository.saveParseTemplate(template)
            val res = repository.reprocessAllSms()
            showMessage("Template saved — parsed ${res.newlyAdded} new transaction(s)")
        }
    }

    fun deleteParseTemplate(id: Long) {
        viewModelScope.launch { repository.deleteParseTemplate(id) }
    }

    fun setSenderRule(sender: String, action: String) {
        viewModelScope.launch {
            repository.setSenderRule(sender, action)
            repository.reprocessAllSms()
            showMessage("Updated sender rule ($action)")
        }
    }

    fun deleteSenderRule(id: Long) {
        viewModelScope.launch { repository.deleteSenderRule(id) }
    }

    fun setMonthStartDay(day: Int) {
        viewModelScope.launch { repository.setMonthStartDay(day) }
    }

    fun setFinancialYearMode(enabled: Boolean) {
        viewModelScope.launch { repository.setFinancialYearMode(enabled) }
    }

    fun setAutoTransferWindowMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.setAutoTransferWindowMinutes(minutes)
            repository.runAutoTransferDetection()
        }
    }

    fun togglePrivacyMode() {
        viewModelScope.launch {
            repository.setPrivacyMode(!uiState.value.preferences.privacyMode)
        }
    }

    fun setFlagSecure(enabled: Boolean) {
        viewModelScope.launch { repository.setFlagSecure(enabled) }
    }

    fun setThemeMode(mode: ThemePreference) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setPin(pin: String?) {
        viewModelScope.launch {
            val hash = pin?.takeIf { it.isNotBlank() }?.let { sha256(it) }
            repository.setPinHash(hash)
            showMessage(if (hash == null) "App PIN lock removed" else "App PIN lock enabled")
        }
    }

    fun verifyPin(pin: String): Boolean {
        val stored = uiState.value.preferences.pinHash ?: return true
        return sha256(pin) == stored
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.clearAllTransactions()
            repository.setSelectedAccountIds(emptySet())
            showMessage("Cleared all stored transactions")
        }
    }

    // ---- Export & Backup helpers ----

    suspend fun buildCsvExport(): String = repository.exportTransactionsCsv()
    suspend fun buildRulesJsonExport(): String = repository.exportRulesJson()
    suspend fun buildEncryptedBackup(password: String): String = repository.exportEncryptedBackup(password)

    fun importRulesJson(json: String) {
        viewModelScope.launch {
            runCatching { repository.importRulesJson(json) }
                .onSuccess { count ->
                    repository.reprocessAllSms()
                    showMessage("Imported $count rules/aliases")
                }
                .onFailure { showMessage("Failed to import rules: ${it.localizedMessage}") }
        }
    }

    fun importEncryptedBackup(payload: String, password: String) {
        viewModelScope.launch {
            runCatching { repository.importEncryptedBackup(payload, password) }
                .onSuccess { count -> showMessage("Restored $count transactions from backup") }
                .onFailure { showMessage("Backup restore failed: Wrong password or invalid file") }
        }
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    class Factory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExpensesViewModel(repository) as T
        }
    }
}

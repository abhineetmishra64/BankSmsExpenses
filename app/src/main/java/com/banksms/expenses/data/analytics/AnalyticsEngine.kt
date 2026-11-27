package com.banksms.expenses.data.analytics

import com.banksms.expenses.data.model.BudgetEntity
import com.banksms.expenses.data.model.CategoryEntity
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionSplitEntity
import com.banksms.expenses.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class MonthPeriod(
    val year: Int,
    val month: Int // 1..12
) {
    fun next(): MonthPeriod = if (month == 12) MonthPeriod(year + 1, 1) else MonthPeriod(year, month + 1)
    fun previous(): MonthPeriod = if (month == 1) MonthPeriod(year - 1, 12) else MonthPeriod(year, month - 1)
}

enum class TrendRange(val label: String, val monthCount: Int) {
    THREE_MONTHS("3M", 3),
    SIX_MONTHS("6M", 6),
    TWELVE_MONTHS("12M", 12),
    FINANCIAL_YEAR("FY", 12),
    CUSTOM("Custom", 12)
}

data class CategoryBreakdownRow(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: Long,
    val iconKey: String,
    val amount: Double,
    val count: Int,
    val percentage: Float
)

data class MerchantTotal(
    val merchant: String,
    val amount: Double,
    val count: Int
)

data class BudgetProgress(
    val budgetId: Long,
    val categoryId: Long?,
    val label: String,
    val budgetAmount: Double,
    val spentAmount: Double,
    val ratio: Float,
    val isWarning: Boolean,
    val isExceeded: Boolean
)

data class HomeSummary(
    val period: MonthPeriod,
    val periodLabel: String,
    val primaryCurrency: String,
    val totalExpense: Double,
    val totalIncome: Double,
    val net: Double,
    val excludedAmount: Double,
    val excludedCount: Int,
    val expenseCount: Int,
    val incomeCount: Int,
    val categoryBreakdown: List<CategoryBreakdownRow>,
    val topMerchants: List<MerchantTotal>,
    val recentTransactions: List<TransactionEntity>,
    val budgetProgress: List<BudgetProgress>,
    val uncategorisedCount: Int
)

data class TrendMonthPoint(
    val period: MonthPeriod,
    val label: String,
    val income: Double,
    val expense: Double,
    val net: Double,
    val savingsRatePct: Float,
    val categoryBreakdown: List<CategoryBreakdownRow>
)

data class TrendSummary(
    val points: List<TrendMonthPoint>,
    val averageMonthlyExpense: Double,
    val highestSpendMonthLabel: String?,
    val highestSpendAmount: Double,
    val momChangePct: Float?
)

data class RecurringPaymentItem(
    val merchant: String,
    val averageAmount: Double,
    val currency: String,
    val occurrenceCount: Int,
    val lastPaidAt: Long,
    val nextExpectedAt: Long,
    val averageIntervalDays: Int,
    val categoryId: Long
)

object PeriodHelper {

    fun currentPeriod(nowMillis: Long = System.currentTimeMillis(), monthStartDay: Int = 1): MonthPeriod {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val clampedStart = monthStartDay.coerceIn(1, 28)
        return if (clampedStart > 1 && day < clampedStart) {
            MonthPeriod(year, month).previous()
        } else {
            MonthPeriod(year, month)
        }
    }

    fun periodBoundsMillis(period: MonthPeriod, monthStartDay: Int = 1): Pair<Long, Long> {
        val clampedStart = monthStartDay.coerceIn(1, 28)
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, period.year)
            set(Calendar.MONTH, period.month - 1)
            set(Calendar.DAY_OF_MONTH, clampedStart)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    fun formatPeriodLabel(period: MonthPeriod, monthStartDay: Int = 1): String {
        val (startMs, endMs) = periodBoundsMillis(period, monthStartDay)
        val cal = Calendar.getInstance().apply { timeInMillis = startMs }
        return if (monthStartDay <= 1) {
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(startMs))
        } else {
            val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
            val inclusiveEnd = endMs - 86_400_000L
            val yr = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(inclusiveEnd))
            "${fmt.format(Date(cal.timeInMillis))} – ${fmt.format(Date(inclusiveEnd))} $yr"
        }
    }

    fun formatShortMonthLabel(period: MonthPeriod): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, period.year)
            set(Calendar.MONTH, period.month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMM yy", Locale.getDefault()).format(cal.time)
    }

    fun buildPeriodsForRange(
        range: TrendRange,
        referencePeriod: MonthPeriod,
        customMonths: Int = 12
    ): List<MonthPeriod> {
        return when (range) {
            TrendRange.THREE_MONTHS, TrendRange.SIX_MONTHS, TrendRange.TWELVE_MONTHS -> {
                buildTrailingPeriods(referencePeriod, range.monthCount)
            }
            TrendRange.FINANCIAL_YEAR -> {
                val fyStartYear = if (referencePeriod.month >= 4) referencePeriod.year else referencePeriod.year - 1
                (0 until 12).map { offset ->
                    val m = ((3 + offset) % 12) + 1
                    val y = if (m >= 4) fyStartYear else fyStartYear + 1
                    MonthPeriod(y, m)
                }
            }
            TrendRange.CUSTOM -> buildTrailingPeriods(referencePeriod, customMonths.coerceIn(1, 36))
        }
    }

    private fun buildTrailingPeriods(endPeriod: MonthPeriod, count: Int): List<MonthPeriod> {
        val list = ArrayList<MonthPeriod>(count)
        var curr = endPeriod
        repeat(count) {
            list.add(curr)
            curr = curr.previous()
        }
        return list.reversed()
    }
}

object AnalyticsEngine {

    fun computeHomeSummary(
        transactions: List<TransactionEntity>,
        splitsByTxnId: Map<Long, List<TransactionSplitEntity>>,
        categoriesById: Map<Long, CategoryEntity>,
        budgets: List<BudgetEntity>,
        selectedAccountIds: Set<Long>,
        period: MonthPeriod,
        monthStartDay: Int = 1
    ): HomeSummary {
        val (startMs, endMs) = PeriodHelper.periodBoundsMillis(period, monthStartDay)
        val periodTxns = transactions.filter { txn ->
            txn.txnTime in startMs until endMs &&
                (selectedAccountIds.isEmpty() || txn.accountId in selectedAccountIds)
        }

        val included = periodTxns.filter { !it.isExcluded }
        val excluded = periodTxns.filter { it.isExcluded }

        val debits = included.filter { it.type == TransactionType.DEBIT }
        val credits = included.filter { it.type == TransactionType.CREDIT }

        val totalExpense = debits.sumOf { it.amount }
        val totalIncome = credits.sumOf { it.amount }
        val net = totalIncome - totalExpense
        val excludedAmount = excluded.sumOf { it.amount }

        val primaryCurrency = periodTxns
            .groupingBy { it.currency }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: "₹"

        val categoryBreakdown = computeCategoryBreakdown(debits, splitsByTxnId, categoriesById, totalExpense)

        val topMerchants = debits
            .groupBy { it.merchant }
            .map { (merchant, txns) ->
                MerchantTotal(
                    merchant = merchant,
                    amount = txns.sumOf { it.amount },
                    count = txns.size
                )
            }
            .sortedByDescending { it.amount }
            .take(5)

        val recent = periodTxns.sortedByDescending { it.txnTime }.take(10)

        val spentByCategory = categoryBreakdown.associate { it.categoryId to it.amount }
        val budgetProgress = budgets.map { budget ->
            val spent = if (budget.categoryId == null) {
                totalExpense
            } else {
                spentByCategory[budget.categoryId] ?: 0.0
            }
            val label = if (budget.categoryId == null) {
                "Overall Monthly Budget"
            } else {
                categoriesById[budget.categoryId]?.name ?: "Category"
            }
            val ratio = if (budget.amount > 0.0) (spent / budget.amount).toFloat() else 0f
            BudgetProgress(
                budgetId = budget.id,
                categoryId = budget.categoryId,
                label = label,
                budgetAmount = budget.amount,
                spentAmount = spent,
                ratio = ratio,
                isWarning = ratio >= 0.8f && ratio < 1.0f,
                isExceeded = ratio >= 1.0f
            )
        }

        val uncategorisedCount = included.count { it.categoryId == ExpenseCategory.UNCATEGORISED.id }

        return HomeSummary(
            period = period,
            periodLabel = PeriodHelper.formatPeriodLabel(period, monthStartDay),
            primaryCurrency = primaryCurrency,
            totalExpense = totalExpense,
            totalIncome = totalIncome,
            net = net,
            excludedAmount = excludedAmount,
            excludedCount = excluded.size,
            expenseCount = debits.size,
            incomeCount = credits.size,
            categoryBreakdown = categoryBreakdown,
            topMerchants = topMerchants,
            recentTransactions = recent,
            budgetProgress = budgetProgress,
            uncategorisedCount = uncategorisedCount
        )
    }

    fun computeCategoryBreakdown(
        debits: List<TransactionEntity>,
        splitsByTxnId: Map<Long, List<TransactionSplitEntity>>,
        categoriesById: Map<Long, CategoryEntity>,
        totalExpense: Double
    ): List<CategoryBreakdownRow> {
        val sums = mutableMapOf<Long, Double>()
        val counts = mutableMapOf<Long, Int>()

        for (txn in debits) {
            val splits = splitsByTxnId[txn.id]
            if (!splits.isNullOrEmpty()) {
                for (split in splits) {
                    sums[split.categoryId] = (sums[split.categoryId] ?: 0.0) + split.amount
                    counts[split.categoryId] = (counts[split.categoryId] ?: 0) + 1
                }
            } else {
                sums[txn.categoryId] = (sums[txn.categoryId] ?: 0.0) + txn.amount
                counts[txn.categoryId] = (counts[txn.categoryId] ?: 0) + 1
            }
        }

        return sums.entries.map { (catId, amount) ->
            val cat = categoriesById[catId]
            val fallback = ExpenseCategory.fromId(catId) ?: ExpenseCategory.OTHER
            CategoryBreakdownRow(
                categoryId = catId,
                categoryName = cat?.name ?: fallback.label,
                colorHex = cat?.colorHex ?: fallback.colorHex,
                iconKey = cat?.iconKey ?: fallback.iconKey,
                amount = amount,
                count = counts[catId] ?: 0,
                percentage = if (totalExpense > 0.0) (amount / totalExpense).toFloat() else 0f
            )
        }.sortedByDescending { it.amount }
    }

    fun computeTrends(
        transactions: List<TransactionEntity>,
        splitsByTxnId: Map<Long, List<TransactionSplitEntity>>,
        categoriesById: Map<Long, CategoryEntity>,
        selectedAccountIds: Set<Long>,
        categoryFilterId: Long?,
        range: TrendRange,
        referencePeriod: MonthPeriod,
        monthStartDay: Int = 1,
        customMonths: Int = 12
    ): TrendSummary {
        val periods = PeriodHelper.buildPeriodsForRange(range, referencePeriod, customMonths)
        val scopedTxns = transactions.filter { txn ->
            !txn.isExcluded &&
                (selectedAccountIds.isEmpty() || txn.accountId in selectedAccountIds)
        }

        val points = periods.map { period ->
            val (startMs, endMs) = PeriodHelper.periodBoundsMillis(period, monthStartDay)
            val monthTxns = scopedTxns.filter { it.txnTime in startMs until endMs }
            val filteredMonthTxns = if (categoryFilterId == null) {
                monthTxns
            } else {
                monthTxns.filter { txn ->
                    val splits = splitsByTxnId[txn.id]
                    if (!splits.isNullOrEmpty()) splits.any { it.categoryId == categoryFilterId }
                    else txn.categoryId == categoryFilterId
                }
            }

            val debits = filteredMonthTxns.filter { it.type == TransactionType.DEBIT }
            val income = if (categoryFilterId == null) {
                filteredMonthTxns.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
            } else {
                0.0
            }

            val expense = if (categoryFilterId == null) {
                debits.sumOf { it.amount }
            } else {
                debits.sumOf { txn ->
                    val splits = splitsByTxnId[txn.id]
                    if (!splits.isNullOrEmpty()) {
                        splits.filter { it.categoryId == categoryFilterId }.sumOf { it.amount }
                    } else {
                        txn.amount
                    }
                }
            }

            val net = income - expense
            val savingsRate = if (income > 0.0) ((net / income) * 100.0).toFloat() else 0f
            val breakdown = computeCategoryBreakdown(debits, splitsByTxnId, categoriesById, expense)

            TrendMonthPoint(
                period = period,
                label = PeriodHelper.formatShortMonthLabel(period),
                income = income,
                expense = expense,
                net = net,
                savingsRatePct = savingsRate,
                categoryBreakdown = breakdown
            )
        }

        val nonZeroMonths = points.filter { it.expense > 0.0 }
        val avgExpense = if (nonZeroMonths.isNotEmpty()) nonZeroMonths.sumOf { it.expense } / nonZeroMonths.size else 0.0
        val highest = points.maxByOrNull { it.expense }

        val last = points.lastOrNull()
        val prev = if (points.size >= 2) points[points.size - 2] else null
        val momChangePct = if (last != null && prev != null && prev.expense > 0.0) {
            (((last.expense - prev.expense) / prev.expense) * 100.0).toFloat()
        } else {
            null
        }

        return TrendSummary(
            points = points,
            averageMonthlyExpense = avgExpense,
            highestSpendMonthLabel = highest?.takeIf { it.expense > 0.0 }?.label,
            highestSpendAmount = highest?.expense ?: 0.0,
            momChangePct = momChangePct
        )
    }

    fun detectRecurringPayments(transactions: List<TransactionEntity>): List<RecurringPaymentItem> {
        val debits = transactions.filter { !it.isExcluded && it.type == TransactionType.DEBIT }
        val dayMs = 86_400_000L

        return debits
            .groupBy { it.merchant.trim().lowercase(Locale.ROOT) }
            .mapNotNull { (_, group) ->
                if (group.size < 2) return@mapNotNull null
                val sorted = group.sortedBy { it.txnTime }
                val amounts = sorted.map { it.amount }.sorted()
                val medianAmount = amounts[amounts.size / 2]
                if (medianAmount <= 0.0) return@mapNotNull null

                val similar = sorted.filter { abs(it.amount - medianAmount) / medianAmount <= 0.15 }
                if (similar.size < 2) return@mapNotNull null

                val intervalsDays = similar.zipWithNext { a, b ->
                    ((b.txnTime - a.txnTime) / dayMs).toInt()
                }
                val validIntervals = intervalsDays.filter { it in 20..42 }
                if (validIntervals.isEmpty()) return@mapNotNull null

                val avgInterval = validIntervals.average().toInt().coerceIn(25, 35)
                val last = similar.last()
                RecurringPaymentItem(
                    merchant = last.merchant,
                    averageAmount = similar.sumOf { it.amount } / similar.size,
                    currency = last.currency,
                    occurrenceCount = similar.size,
                    lastPaidAt = last.txnTime,
                    nextExpectedAt = last.txnTime + avgInterval * dayMs,
                    averageIntervalDays = avgInterval,
                    categoryId = last.categoryId
                )
            }
            .sortedBy { it.nextExpectedAt }
    }
}

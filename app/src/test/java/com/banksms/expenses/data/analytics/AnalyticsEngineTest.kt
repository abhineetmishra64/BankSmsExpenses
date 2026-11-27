package com.banksms.expenses.data.analytics

import com.banksms.expenses.data.model.BudgetEntity
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionSplitEntity
import com.banksms.expenses.data.model.TransactionType
import com.banksms.expenses.data.sms.CategorisationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AnalyticsEngineTest {

    private val categoriesById = CategorisationEngine.defaultCategoryEntities().associateBy { it.id }

    private fun millisAt(year: Int, month: Int, day: Int, hour: Int = 12): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun txn(
        id: Long,
        accountId: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        merchant: String,
        txnTime: Long,
        isExcluded: Boolean = false
    ) = TransactionEntity(
        id = id,
        accountId = accountId,
        sender = "AD-HDFCBK",
        bankName = "HDFC Bank",
        accountLast4 = accountId.toString().padStart(4, '0'),
        amount = amount,
        currency = "₹",
        type = type,
        merchant = merchant,
        rawMerchant = merchant,
        categoryId = categoryId,
        txnTime = txnTime,
        rawBody = merchant,
        fingerprint = "fp-$id",
        isExcluded = isExcluded
    )

    @Test
    fun homeSummaryExcludesExcludedItemsAndMaintainsAccountConsistency() {
        val period = MonthPeriod(2026, 11)
        val txs = listOf(
            txn(1L, 1L, 500.0, TransactionType.DEBIT, ExpenseCategory.FOOD.id, "Swiggy", millisAt(2026, 11, 5)),
            txn(2L, 2L, 1500.0, TransactionType.DEBIT, ExpenseCategory.SHOPPING.id, "Amazon", millisAt(2026, 11, 6)),
            txn(3L, 1L, 5000.0, TransactionType.DEBIT, ExpenseCategory.INVESTMENTS.id, "Zerodha SIP", millisAt(2026, 11, 7), isExcluded = true),
            txn(4L, 1L, 50000.0, TransactionType.CREDIT, ExpenseCategory.SALARY.id, "Salary", millisAt(2026, 11, 1))
        )

        val allSummary = AnalyticsEngine.computeHomeSummary(
            transactions = txs,
            splitsByTxnId = emptyMap(),
            categoriesById = categoriesById,
            budgets = emptyList(),
            selectedAccountIds = emptySet(),
            period = period
        )
        val acc1Summary = AnalyticsEngine.computeHomeSummary(
            transactions = txs,
            splitsByTxnId = emptyMap(),
            categoriesById = categoriesById,
            budgets = emptyList(),
            selectedAccountIds = setOf(1L),
            period = period
        )
        val acc2Summary = AnalyticsEngine.computeHomeSummary(
            transactions = txs,
            splitsByTxnId = emptyMap(),
            categoriesById = categoriesById,
            budgets = emptyList(),
            selectedAccountIds = setOf(2L),
            period = period
        )

        assertEquals(2000.0, allSummary.totalExpense, 0.001)
        assertEquals(50000.0, allSummary.totalIncome, 0.001)
        assertEquals(48000.0, allSummary.net, 0.001)
        assertEquals(5000.0, allSummary.excludedAmount, 0.001)
        assertEquals(1, allSummary.excludedCount)

        // Spec 12: Sum across individual accounts equals "All accounts" total
        assertEquals(allSummary.totalExpense, acc1Summary.totalExpense + acc2Summary.totalExpense, 0.001)
        assertEquals(allSummary.totalIncome, acc1Summary.totalIncome + acc2Summary.totalIncome, 0.001)
    }

    @Test
    fun splitsAndBudgetsComputeAccurately() {
        val period = MonthPeriod(2026, 11)
        val txs = listOf(
            txn(1L, 1L, 1000.0, TransactionType.DEBIT, ExpenseCategory.GROCERIES.id, "BigBasket", millisAt(2026, 11, 10))
        )
        val splits = mapOf(
            1L to listOf(
                TransactionSplitEntity(1L, 1L, ExpenseCategory.GROCERIES.id, 700.0),
                TransactionSplitEntity(2L, 1L, ExpenseCategory.HEALTH.id, 300.0)
            )
        )
        val budgets = listOf(
            BudgetEntity(1L, null, 1200.0), // overall: 1000/1200 = 83.3% warning
            BudgetEntity(2L, ExpenseCategory.HEALTH.id, 250.0) // health: 300/250 = 120% exceeded
        )

        val summary = AnalyticsEngine.computeHomeSummary(
            transactions = txs,
            splitsByTxnId = splits,
            categoriesById = categoriesById,
            budgets = budgets,
            selectedAccountIds = emptySet(),
            period = period
        )

        assertEquals(2, summary.categoryBreakdown.size)
        assertEquals(700.0, summary.categoryBreakdown.first { it.categoryId == ExpenseCategory.GROCERIES.id }.amount, 0.001)
        assertEquals(300.0, summary.categoryBreakdown.first { it.categoryId == ExpenseCategory.HEALTH.id }.amount, 0.001)

        val overallBudget = summary.budgetProgress.first { it.categoryId == null }
        val healthBudget = summary.budgetProgress.first { it.categoryId == ExpenseCategory.HEALTH.id }
        assertTrue(overallBudget.isWarning)
        assertFalseOrNotExceeded(overallBudget)
        assertTrue(healthBudget.isExceeded)
    }

    private fun assertFalseOrNotExceeded(bp: BudgetProgress) {
        assertEquals(false, bp.isExceeded)
    }

    @Test
    fun customMonthStartDayResolvesBillingPeriodCorrectly() {
        // Salary on 25th: Nov 26 belongs to November period (Nov 25 - Dec 24), Nov 20 belongs to October period (Oct 25 - Nov 24)
        val nov26 = millisAt(2026, 11, 26)
        val nov20 = millisAt(2026, 11, 20)
        assertEquals(MonthPeriod(2026, 11), PeriodHelper.currentPeriod(nov26, monthStartDay = 25))
        assertEquals(MonthPeriod(2026, 10), PeriodHelper.currentPeriod(nov20, monthStartDay = 25))

        val (startMs, endMs) = PeriodHelper.periodBoundsMillis(MonthPeriod(2026, 11), monthStartDay = 25)
        assertTrue(nov26 in startMs until endMs)
        assertTrue(nov20 !in startMs until endMs)
    }

    @Test
    fun computesTrendsAndInsightsAcrossMonths() {
        val txs = listOf(
            txn(1L, 1L, 50000.0, TransactionType.CREDIT, ExpenseCategory.SALARY.id, "Salary", millisAt(2026, 10, 1)),
            txn(2L, 1L, 10000.0, TransactionType.DEBIT, ExpenseCategory.FOOD.id, "Swiggy", millisAt(2026, 10, 10)),
            txn(3L, 1L, 50000.0, TransactionType.CREDIT, ExpenseCategory.SALARY.id, "Salary", millisAt(2026, 11, 1)),
            txn(4L, 1L, 15000.0, TransactionType.DEBIT, ExpenseCategory.FOOD.id, "Swiggy", millisAt(2026, 11, 12))
        )

        val trend = AnalyticsEngine.computeTrends(
            transactions = txs,
            splitsByTxnId = emptyMap(),
            categoriesById = categoriesById,
            selectedAccountIds = emptySet(),
            categoryFilterId = null,
            range = TrendRange.THREE_MONTHS,
            referencePeriod = MonthPeriod(2026, 11)
        )

        assertEquals(3, trend.points.size)
        assertEquals(12500.0, trend.averageMonthlyExpense, 0.001)
        assertEquals(15000.0, trend.highestSpendAmount, 0.001)
        assertEquals(50.0f, trend.momChangePct!!, 0.1f)
    }

    @Test
    fun detectsMonthlyRecurringSubscriptions() {
        val txs = listOf(
            txn(1L, 1L, 649.0, TransactionType.DEBIT, ExpenseCategory.ENTERTAINMENT.id, "Netflix", millisAt(2026, 9, 23)),
            txn(2L, 1L, 649.0, TransactionType.DEBIT, ExpenseCategory.ENTERTAINMENT.id, "Netflix", millisAt(2026, 10, 23)),
            txn(3L, 1L, 649.0, TransactionType.DEBIT, ExpenseCategory.ENTERTAINMENT.id, "Netflix", millisAt(2026, 11, 23)),
            txn(4L, 1L, 1200.0, TransactionType.DEBIT, ExpenseCategory.SHOPPING.id, "OneTimeShop", millisAt(2026, 11, 5))
        )

        val recurring = AnalyticsEngine.detectRecurringPayments(txs)
        assertEquals(1, recurring.size)
        assertEquals("Netflix", recurring.first().merchant)
        assertEquals(3, recurring.first().occurrenceCount)
        assertEquals(649.0, recurring.first().averageAmount, 0.001)
    }
}

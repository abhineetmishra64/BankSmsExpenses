package com.banksms.expenses.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.banksms.expenses.data.analytics.MonthPeriod
import com.banksms.expenses.data.analytics.TrendRange
import com.banksms.expenses.ui.ExpensesUiState
import com.banksms.expenses.ui.components.AccountSelectorRow
import com.banksms.expenses.ui.components.TrendsBarChart
import com.banksms.expenses.ui.util.Formatters

@Composable
fun TrendsScreen(
    uiState: ExpensesUiState,
    onToggleAccount: (Long?) -> Unit,
    onSelectRange: (TrendRange) -> Unit,
    onSelectCategoryFilter: (Long?) -> Unit,
    onToggleNetLine: () -> Unit,
    onToggleStacked: () -> Unit,
    onToggleTable: () -> Unit,
    onSelectPeriod: (MonthPeriod) -> Unit,
    onOpenMonthOnHome: (MonthPeriod) -> Unit
) {
    val trends = uiState.trendSummary
    val masked = uiState.preferences.privacyMode
    val currency = uiState.homeSummary.primaryCurrency
    val selectedPoint = trends.points.firstOrNull { it.period == uiState.selectedTrendPeriod }
        ?: trends.points.lastOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (uiState.activeAccounts.isNotEmpty()) {
            item {
                AccountSelectorRow(
                    accounts = uiState.activeAccounts,
                    selectedAccountIds = uiState.preferences.selectedAccountIds,
                    onToggleAccount = onToggleAccount
                )
            }
        }

        // Range selector & view toggles
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TrendRange.entries) { range ->
                        FilterChip(
                            selected = uiState.trendRange == range,
                            onClick = { onSelectRange(range) },
                            label = { Text(range.label) }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.showNetSavingsLine,
                            onClick = onToggleNetLine,
                            label = { Text("Net Savings Line") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.showStackedCategories,
                            onClick = onToggleStacked,
                            label = { Text("Stacked Categories") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.showTrendTable,
                            onClick = onToggleTable,
                            label = { Text("Table View") }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.trendCategoryId == null,
                            onClick = { onSelectCategoryFilter(null) },
                            label = { Text("All Categories") }
                        )
                    }
                    items(uiState.categories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = uiState.trendCategoryId == cat.id,
                            onClick = {
                                onSelectCategoryFilter(if (uiState.trendCategoryId == cat.id) null else cat.id)
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }
        }

        // Insights Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InsightMiniCard(
                    label = "Avg Monthly",
                    value = Formatters.formatMoney(trends.averageMonthlyExpense, currency, masked),
                    modifier = Modifier.weight(1f)
                )
                InsightMiniCard(
                    label = "Highest",
                    value = trends.highestSpendMonthLabel?.let {
                        "$it\n${Formatters.formatMoney(trends.highestSpendAmount, currency, masked)}"
                    } ?: "—",
                    modifier = Modifier.weight(1f)
                )
                InsightMiniCard(
                    label = "MoM Change",
                    value = trends.momChangePct?.let {
                        val sign = if (it >= 0f) "+" else ""
                        "$sign${"%.1f".format(it)}%"
                    } ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Chart or Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (uiState.showTrendTable) "Monthly Breakdown Table" else "Income vs Expense (Tap bar for details)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    if (uiState.showTrendTable) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Month", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f))
                            Text("Income", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Expense", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Sav %", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        trends.points.reversed().forEach { pt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(pt.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.1f))
                                Text(Formatters.formatMoney(pt.income, currency, masked), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text(Formatters.formatMoney(pt.expense, currency, masked), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text("${pt.savingsRatePct.toInt()}%", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.7f))
                            }
                        }
                    } else {
                        TrendsBarChart(
                            points = trends.points,
                            selectedPeriod = selectedPoint?.period,
                            showNetSavingsLine = uiState.showNetSavingsLine,
                            stackedCategories = uiState.showStackedCategories,
                            onSelectPeriod = onSelectPeriod
                        )
                    }
                }
            }
        }

        // Selected Month Summary Card
        if (selectedPoint != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPoint.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(onClick = { onOpenMonthOnHome(selectedPoint.period) }) {
                                Text("Open on Home")
                            }
                        }
                        Text(
                            text = "Income: ${Formatters.formatMoney(selectedPoint.income, currency, masked)}  •  Expense: ${Formatters.formatMoney(selectedPoint.expense, currency, masked)}  •  Net: ${Formatters.formatMoney(selectedPoint.net, currency, masked)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (selectedPoint.categoryBreakdown.isNotEmpty()) {
                            Text(
                                text = "Top categories:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            selectedPoint.categoryBreakdown.take(4).forEach { cat ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat.categoryName, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        Formatters.formatMoney(cat.amount, currency, masked),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightMiniCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

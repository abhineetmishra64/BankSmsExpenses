package com.banksms.expenses.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionType
import com.banksms.expenses.ui.ExclusionFilter
import com.banksms.expenses.ui.ExpensesUiState
import com.banksms.expenses.ui.TypeFilter
import com.banksms.expenses.ui.components.AccountSelectorRow
import com.banksms.expenses.ui.components.TransactionRow
import com.banksms.expenses.ui.util.Formatters

@Composable
fun TransactionsScreen(
    uiState: ExpensesUiState,
    onSearchChange: (String) -> Unit,
    onSelectDirection: (TypeFilter) -> Unit,
    onSelectCategory: (Long?) -> Unit,
    onSelectExclusionFilter: (ExclusionFilter) -> Unit,
    onToggleUncategorisedOnly: () -> Unit,
    onToggleMonthScoped: () -> Unit,
    onAmountRangeChange: (String, String) -> Unit,
    onClearFilters: () -> Unit,
    onToggleAccount: (Long?) -> Unit,
    onSelectTransaction: (TransactionEntity) -> Unit,
    onAddManualClick: () -> Unit
) {
    val masked = uiState.preferences.privacyMode
    var showAmountFilter by remember { mutableStateOf(false) }

    val groupedByDay = remember(uiState.filteredTransactions) {
        uiState.filteredTransactions.groupBy { Formatters.formatDayHeader(it.txnTime) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.txnSearchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search merchant, bank, SMS, note, tag") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.txnSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.activeAccounts.isNotEmpty()) {
                item {
                    AccountSelectorRow(
                        accounts = uiState.activeAccounts,
                        selectedAccountIds = uiState.preferences.selectedAccountIds,
                        onToggleAccount = onToggleAccount
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(TypeFilter.entries) { filter ->
                            FilterChip(
                                selected = uiState.txnDirectionFilter == filter,
                                onClick = { onSelectDirection(filter) },
                                label = { Text(filter.label) }
                            )
                        }
                        items(ExclusionFilter.entries) { filter ->
                            FilterChip(
                                selected = uiState.txnExclusionFilter == filter,
                                onClick = { onSelectExclusionFilter(filter) },
                                label = { Text(filter.label) }
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = uiState.txnMonthScoped,
                                onClick = onToggleMonthScoped,
                                label = { Text(if (uiState.txnMonthScoped) uiState.homeSummary.periodLabel else "All Dates") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.txnUncategorisedOnly,
                                onClick = onToggleUncategorisedOnly,
                                label = { Text("Uncategorised") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = showAmountFilter || uiState.txnMinAmount.isNotBlank() || uiState.txnMaxAmount.isNotBlank(),
                                onClick = { showAmountFilter = !showAmountFilter },
                                label = { Text("Amount Range") }
                            )
                        }
                        items(uiState.categories, key = { it.id }) { cat ->
                            FilterChip(
                                selected = uiState.txnCategoryFilterId == cat.id,
                                onClick = {
                                    onSelectCategory(if (uiState.txnCategoryFilterId == cat.id) null else cat.id)
                                },
                                label = { Text(cat.name) }
                            )
                        }
                    }

                    if (showAmountFilter) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.txnMinAmount,
                                onValueChange = { onAmountRangeChange(it, uiState.txnMaxAmount) },
                                label = { Text("Min ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = uiState.txnMaxAmount,
                                onValueChange = { onAmountRangeChange(uiState.txnMinAmount, it) },
                                label = { Text("Max ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.filteredTransactions.size} transactions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onClearFilters) {
                        Text("Reset filters")
                    }
                }
            }

            if (groupedByDay.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No matching transactions", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Try resetting filters or tap + to add a manual cash expense.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                groupedByDay.forEach { (dayLabel, dayTxns) ->
                    val daySpend = dayTxns.filter { !it.isExcluded && it.type == TransactionType.DEBIT }.sumOf { it.amount }
                    val dayCurrency = dayTxns.firstOrNull()?.currency ?: "₹"
                    item(key = "header_$dayLabel") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (daySpend > 0.0) {
                                Text(
                                    text = "Spent ${Formatters.formatMoney(daySpend, dayCurrency, masked)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    items(dayTxns, key = { it.id }) { tx ->
                        TransactionRow(
                            transaction = tx,
                            category = uiState.categoriesById[tx.categoryId],
                            privacyMode = masked,
                            onClick = { onSelectTransaction(tx) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddManualClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add manual transaction")
        }
    }
}

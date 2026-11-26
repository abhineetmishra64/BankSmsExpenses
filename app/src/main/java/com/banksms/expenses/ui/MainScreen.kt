package com.banksms.expenses.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.ui.components.AllBanksHeroCard
import com.banksms.expenses.ui.components.CategoryBreakdownCard
import com.banksms.expenses.ui.components.PerBankSection
import com.banksms.expenses.ui.components.TransactionDetailSheet
import com.banksms.expenses.ui.components.TransactionRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: ExpensesUiState,
    hasSmsPermission: Boolean,
    onRequestSmsPermission: () -> Unit,
    onSyncSms: () -> Unit,
    onLoadSampleData: () -> Unit,
    onSelectBank: (String?) -> Unit,
    onSelectTimeframe: (TimeframeFilter) -> Unit,
    onSelectTypeFilter: (TypeFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onUpdateCategory: (Long, ExpenseCategory) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onClearAll: () -> Unit,
    onDismissStatusMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onDismissStatusMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bank SMS Expenses",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.selectedBank ?: "All Banks Combined",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        IconButton(onClick = {
                            if (hasSmsPermission) onSyncSms() else onRequestSmsPermission()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Sync SMS"
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Load sample bank SMS") },
                                onClick = {
                                    showOverflowMenu = false
                                    onLoadSampleData()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear all transactions") },
                                onClick = {
                                    showOverflowMenu = false
                                    onClearAll()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!hasSmsPermission) {
                item {
                    SmsPermissionBanner(
                        onGrantPermission = onRequestSmsPermission,
                        onLoadSampleData = onLoadSampleData
                    )
                }
            }

            item {
                AllBanksHeroCard(
                    summary = uiState.allBanksSummary,
                    bankCount = uiState.perBankSummaries.size,
                    isSelected = uiState.selectedBank == null,
                    timeframeLabel = uiState.timeframe.label,
                    onClick = { onSelectBank(null) }
                )
            }

            if (uiState.perBankSummaries.isNotEmpty()) {
                item {
                    PerBankSection(
                        banks = uiState.perBankSummaries,
                        selectedBank = uiState.selectedBank,
                        onSelectBank = onSelectBank
                    )
                }
            }

            item {
                FilterControlsSection(
                    timeframe = uiState.timeframe,
                    typeFilter = uiState.typeFilter,
                    searchQuery = uiState.searchQuery,
                    onSelectTimeframe = onSelectTimeframe,
                    onSelectTypeFilter = onSelectTypeFilter,
                    onSearchQueryChange = onSearchQueryChange
                )
            }

            if (uiState.categoryBreakdown.isNotEmpty()) {
                item {
                    val activeSummary = uiState.perBankSummaries
                        .firstOrNull { it.bankName == uiState.selectedBank }
                        ?: uiState.allBanksSummary

                    CategoryBreakdownCard(
                        scopeTitle = activeSummary.bankName,
                        currency = activeSummary.primaryCurrency,
                        items = uiState.categoryBreakdown
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.selectedBank ?: "All Banks"} · ${uiState.visibleTransactions.size} items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (uiState.visibleTransactions.isEmpty()) {
                item {
                    EmptyTransactionsCard(
                        hasStoredTransactions = uiState.totalStoredTransactions > 0,
                        hasSmsPermission = hasSmsPermission,
                        onSyncSms = onSyncSms,
                        onRequestPermission = onRequestSmsPermission,
                        onLoadSampleData = onLoadSampleData
                    )
                }
            } else {
                items(
                    items = uiState.visibleTransactions,
                    key = { it.id }
                ) { tx ->
                    TransactionRow(
                        transaction = tx,
                        onClick = { selectedTransaction = tx }
                    )
                }
            }
        }
    }

    selectedTransaction?.let { tx ->
        TransactionDetailSheet(
            transaction = tx,
            onDismiss = { selectedTransaction = null },
            onUpdateCategory = { newCategory ->
                onUpdateCategory(tx.id, newCategory)
                selectedTransaction = tx.copy(category = newCategory)
            },
            onDelete = {
                onDeleteTransaction(tx.id)
                selectedTransaction = null
            }
        )
    }
}

@Composable
private fun SmsPermissionBanner(
    onGrantPermission: () -> Unit,
    onLoadSampleData: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Sms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Read Bank Transaction SMS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Grant SMS access to scan your inbox on-device and group expenses separately for each bank and across all banks. Everything stays offline on your phone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGrantPermission) {
                    Text("Allow SMS Access")
                }
                OutlinedButton(onClick = onLoadSampleData) {
                    Text("Try Sample SMS")
                }
            }
        }
    }
}

@Composable
private fun FilterControlsSection(
    timeframe: TimeframeFilter,
    typeFilter: TypeFilter,
    searchQuery: String,
    onSelectTimeframe: (TimeframeFilter) -> Unit,
    onSelectTypeFilter: (TypeFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search merchant, bank, or SMS text") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TypeFilter.entries) { filter ->
                FilterChip(
                    selected = typeFilter == filter,
                    onClick = { onSelectTypeFilter(filter) },
                    label = { Text(filter.label) }
                )
            }
            items(TimeframeFilter.entries) { filter ->
                FilterChip(
                    selected = timeframe == filter,
                    onClick = { onSelectTimeframe(filter) },
                    label = { Text(filter.label) }
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionsCard(
    hasStoredTransactions: Boolean,
    hasSmsPermission: Boolean,
    onSyncSms: () -> Unit,
    onRequestPermission: () -> Unit,
    onLoadSampleData: () -> Unit
) {
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
            Text(
                text = if (hasStoredTransactions) {
                    "No transactions match the current filters."
                } else {
                    "No bank transactions fetched yet."
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (hasStoredTransactions) {
                    "Try clearing your search or switching to All Time."
                } else {
                    "Sync your SMS inbox to automatically detect expenses for every bank, or load sample SMS to preview."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!hasStoredTransactions) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = if (hasSmsPermission) onSyncSms else onRequestPermission) {
                        Text(if (hasSmsPermission) "Scan SMS Inbox" else "Allow SMS")
                    }
                    OutlinedButton(onClick = onLoadSampleData) {
                        Text("Load Samples")
                    }
                }
            }
        }
    }
}

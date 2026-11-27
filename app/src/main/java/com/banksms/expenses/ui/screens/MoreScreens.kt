package com.banksms.expenses.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.banksms.expenses.data.model.AccountEntity
import com.banksms.expenses.data.model.AccountType
import com.banksms.expenses.data.model.CategoryEntity
import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ExclusionRuleEntity
import com.banksms.expenses.data.repository.ThemePreference
import com.banksms.expenses.data.sms.ExclusionSuggestion
import com.banksms.expenses.ui.ExpensesUiState
import com.banksms.expenses.ui.MoreSubScreen
import com.banksms.expenses.ui.util.Formatters

@Composable
fun MoreMenuScreen(
    uiState: ExpensesUiState,
    onOpenSubScreen: (MoreSubScreen) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MoreMenuRow(
                icon = Icons.Rounded.Block,
                title = "Exclusions & Rules",
                subtitle = "${uiState.excludedTransactions.size} excluded • ${uiState.exclusionRules.size} rules",
                onClick = { onOpenSubScreen(MoreSubScreen.EXCLUSIONS) }
            )
        }
        item {
            MoreMenuRow(
                icon = Icons.Rounded.Category,
                title = "Categories, Rules & Aliases",
                subtitle = "${uiState.categories.size} categories • ${uiState.categoryRules.size} rules",
                onClick = { onOpenSubScreen(MoreSubScreen.CATEGORIES_RULES) }
            )
        }
        item {
            MoreMenuRow(
                icon = Icons.Rounded.AccountBalance,
                title = "Accounts & Cards",
                subtitle = "${uiState.activeAccounts.size} active accounts",
                onClick = { onOpenSubScreen(MoreSubScreen.ACCOUNTS) }
            )
        }
        item {
            MoreMenuRow(
                icon = Icons.Rounded.PieChart,
                title = "Monthly Budgets",
                subtitle = "${uiState.budgets.size} budget(s) configured",
                onClick = { onOpenSubScreen(MoreSubScreen.BUDGETS) }
            )
        }
        item {
            MoreMenuRow(
                icon = Icons.Rounded.Autorenew,
                title = "Recurring Payments",
                subtitle = "${uiState.recurringPayments.size} subscription(s) detected",
                onClick = { onOpenSubScreen(MoreSubScreen.RECURRING) }
            )
        }
        item {
            MoreMenuRow(
                icon = Icons.Rounded.Backup,
                title = "Backup, Export & Settings",
                subtitle = "Encrypted export, CSV, rules JSON, security, month start day",
                onClick = { onOpenSubScreen(MoreSubScreen.BACKUP_SETTINGS) }
            )
        }
    }
}

@Composable
private fun MoreMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SubScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ExclusionsScreen(
    uiState: ExpensesUiState,
    onBack: () -> Unit,
    onIncludeAgain: (Set<Long>) -> Unit,
    onBulkExclude: (query: String, reason: ExclusionReason, exclude: Boolean) -> Unit,
    onToggleRule: (ExclusionRuleEntity) -> Unit,
    onResetRule: (Long) -> Unit,
    onOpenNewRule: () -> Unit,
    onAcceptSuggestion: (ExclusionSuggestion) -> Unit,
    onDismissSuggestion: (ExclusionSuggestion) -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }
    var bulkQuery by remember { mutableStateOf("") }
    val masked = uiState.preferences.privacyMode

    Column(modifier = Modifier.fillMaxSize()) {
        SubScreenHeader("Exclusions", onBack)
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Excluded (${uiState.excludedTransactions.size})") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Rules (${uiState.exclusionRules.size})") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Suggest (${uiState.exclusionSuggestions.size})") })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (tab) {
                0 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = bulkQuery,
                                    onValueChange = { bulkQuery = it },
                                    label = { Text("Bulk exclude/include by text") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onBulkExclude(bulkQuery, ExclusionReason.OTHER, true) }) {
                                        Text("Exclude All Matches")
                                    }
                                    OutlinedButton(onClick = { onBulkExclude(bulkQuery, ExclusionReason.OTHER, false) }) {
                                        Text("Include All Matches")
                                    }
                                }
                            }
                        }
                    }

                    if (selectedIds.isNotEmpty()) {
                        item {
                            Button(
                                onClick = {
                                    onIncludeAgain(selectedIds)
                                    selectedIds = emptySet()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Include ${selectedIds.size} selected again")
                            }
                        }
                    }

                    val totalExcluded = uiState.excludedTransactions.sumOf { it.amount }
                    item {
                        Text(
                            text = "Total excluded: ${Formatters.formatMoney(totalExcluded, uiState.homeSummary.primaryCurrency, masked)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(uiState.excludedTransactions, key = { it.id }) { txn ->
                        val checked = txn.id in selectedIds
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedIds = if (checked) selectedIds - txn.id else selectedIds + txn.id
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = {
                                        selectedIds = if (it) selectedIds + txn.id else selectedIds - txn.id
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(txn.merchant, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${txn.exclusionReason?.label ?: "Other"} • ${Formatters.formatShortDate(txn.txnTime)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    Formatters.formatMoney(txn.amount, txn.currency, masked),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        Button(onClick = onOpenNewRule, modifier = Modifier.fillMaxWidth()) {
                            Text("+ Create Exclusion Rule")
                        }
                    }
                    items(uiState.exclusionRules, key = { it.id }) { rule ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(rule.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${rule.reason.label} • ${rule.matchCount} matches",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(checked = rule.enabled, onCheckedChange = { onToggleRule(rule) })
                                }
                                OutlinedButton(onClick = { onResetRule(rule.id) }) {
                                    Text("Reset & Re-include Matches")
                                }
                            }
                        }
                    }
                }

                2 -> {
                    items(uiState.exclusionSuggestions, key = { it.suggestionKey }) { sug ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("${sug.transaction.merchant} — ${Formatters.formatMoney(sug.transaction.amount, sug.transaction.currency, masked)}", fontWeight = FontWeight.Bold)
                                Text("Reason: ${sug.suggestedReason.label} (${sug.matchedKeyword})", style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { onAcceptSuggestion(sug) }) { Text("Exclude") }
                                    OutlinedButton(onClick = { onDismissSuggestion(sug) }) { Text("Dismiss") }
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
fun CategoriesAndRulesScreen(
    uiState: ExpensesUiState,
    onBack: () -> Unit,
    onOpenNewCategoryRule: () -> Unit,
    onEditCategoryRule: (CategoryRuleEntity) -> Unit,
    onToggleCategoryRule: (CategoryRuleEntity) -> Unit,
    onMoveCategoryRule: (Long, Boolean) -> Unit,
    onDeleteCategoryRule: (Long) -> Unit,
    onTestRuleSms: (String, String) -> Unit,
    onAddCategory: (String, Long) -> Unit,
    onRenameCategory: (CategoryEntity, String, Long) -> Unit,
    onMergeCategory: (Long, Long) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    onAddAlias: (String, String, Long?) -> Unit,
    onDeleteAlias: (Long) -> Unit,
    onExportRulesJson: () -> Unit,
    onImportRulesJson: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var testSender by remember { mutableStateOf("AD-HDFCBK") }
    var testBody by remember { mutableStateOf("") }
    var newCatName by remember { mutableStateOf("") }
    var aliasRaw by remember { mutableStateOf("") }
    var aliasCanon by remember { mutableStateOf("") }
    var mergeTargetFor by remember { mutableStateOf<CategoryEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        SubScreenHeader("Categories & Rules", onBack)
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Rules (${uiState.categoryRules.size})") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Categories (${uiState.categories.size})") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Aliases (${uiState.aliases.size})") })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (tab) {
                0 -> {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onOpenNewCategoryRule, modifier = Modifier.weight(1f)) {
                                Text("+ New Rule")
                            }
                            OutlinedButton(onClick = onExportRulesJson) { Text("Export") }
                            OutlinedButton(onClick = onImportRulesJson) { Text("Import") }
                        }
                    }

                    // Test rule on sample SMS
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Test Rules on Sample SMS", fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = testSender,
                                    onValueChange = { testSender = it },
                                    label = { Text("Sender") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = testBody,
                                    onValueChange = { testBody = it },
                                    label = { Text("Paste sample SMS body") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(onClick = { onTestRuleSms(testSender, testBody) }) {
                                    Text("Run Test")
                                }
                                uiState.ruleTestPreview?.let { prev ->
                                    Text(
                                        text = if (prev.parsed == null) "Not parsed"
                                        else "Merchant: ${prev.parsed.merchant} → ${prev.categoryName} (${prev.source.label}${prev.matchedRuleName?.let { ": $it" } ?: ""})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    items(uiState.categoryRules, key = { it.id }) { rule ->
                        val catName = uiState.categoriesById[rule.categoryId]?.name ?: "Category"
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditCategoryRule(rule) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rule.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        "→ $catName • matches: ${rule.matchCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onMoveCategoryRule(rule.id, true) }) {
                                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move up")
                                }
                                IconButton(onClick = { onMoveCategoryRule(rule.id, false) }) {
                                    Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move down")
                                }
                                Switch(checked = rule.enabled, onCheckedChange = { onToggleCategoryRule(rule) })
                                IconButton(onClick = { onDeleteCategoryRule(rule.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete rule")
                                }
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                label = { Text("New category name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = {
                                if (newCatName.isNotBlank()) {
                                    onAddCategory(newCatName, 0xFF0F766E)
                                    newCatName = ""
                                }
                            }) { Text("Add") }
                        }
                    }
                    items(uiState.categories, key = { it.id }) { cat ->
                        var editing by remember { mutableStateOf(false) }
                        var renameText by remember(cat.name) { mutableStateOf(cat.name) }
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cat.name, fontWeight = FontWeight.SemiBold)
                                    Text(if (cat.isBuiltin) "Built-in" else "Custom", style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(onClick = { editing = true }) { Text("Rename") }
                                TextButton(onClick = { mergeTargetFor = cat }) { Text("Merge") }
                                if (!cat.isBuiltin) {
                                    IconButton(onClick = { onDeleteCategory(cat.id) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                        if (editing) {
                            AlertDialog(
                                onDismissRequest = { editing = false },
                                title = { Text("Rename Category") },
                                text = {
                                    OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true)
                                },
                                confirmButton = {
                                    Button(onClick = {
                                        if (renameText.isNotBlank()) onRenameCategory(cat, renameText, cat.colorHex)
                                        editing = false
                                    }) { Text("Save") }
                                },
                                dismissButton = { TextButton(onClick = { editing = false }) { Text("Cancel") } }
                            )
                        }
                    }
                }

                2 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Add Merchant Alias", fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = aliasRaw,
                                    onValueChange = { aliasRaw = it },
                                    label = { Text("Raw SMS pattern (e.g. AMZN PAY)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = aliasCanon,
                                    onValueChange = { aliasCanon = it },
                                    label = { Text("Canonical name (e.g. Amazon)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(onClick = {
                                    if (aliasRaw.isNotBlank() && aliasCanon.isNotBlank()) {
                                        onAddAlias(aliasRaw, aliasCanon, null)
                                        aliasRaw = ""
                                        aliasCanon = ""
                                    }
                                }) { Text("Save Alias") }
                            }
                        }
                    }
                    items(uiState.aliases, key = { it.id }) { alias ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${alias.rawPattern} → ${alias.canonicalName}", fontWeight = FontWeight.Medium)
                                IconButton(onClick = { onDeleteAlias(alias.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete alias")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    mergeTargetFor?.let { source ->
        AlertDialog(
            onDismissRequest = { mergeTargetFor = null },
            title = { Text("Merge \"${source.name}\" into:") },
            text = {
                LazyColumn(modifier = Modifier.height(260.dp)) {
                    items(uiState.categories.filter { it.id != source.id }, key = { it.id }) { target ->
                        TextButton(
                            onClick = {
                                onMergeCategory(source.id, target.id)
                                mergeTargetFor = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(target.name)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { mergeTargetFor = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountsScreen(
    uiState: ExpensesUiState,
    onBack: () -> Unit,
    onRenameAccount: (Long, String) -> Unit,
    onUpdateAccountType: (Long, AccountType) -> Unit,
    onToggleActive: (Long) -> Unit,
    onMergeAccounts: (Long, Long) -> Unit
) {
    val masked = uiState.preferences.privacyMode
    var mergeSource by remember { mutableStateOf<AccountEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        SubScreenHeader("Accounts & Cards", onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.accounts.filter { it.mergedIntoAccountId == null }, key = { it.id }) { acc ->
                var renaming by remember { mutableStateOf(false) }
                var nameInput by remember(acc.displayName) { mutableStateOf(acc.displayName) }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(acc.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = buildString {
                                        append("${acc.bankName} • ${acc.type.label}")
                                        if (!acc.last4.isNullOrBlank()) append(" ••${acc.last4}")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(checked = acc.isActive, onCheckedChange = { onToggleActive(acc.id) })
                        }

                        if (acc.lastBalance != null || acc.creditLimit != null) {
                            Text(
                                text = buildString {
                                    acc.lastBalance?.let { append("Last known balance: ${Formatters.formatMoney(it, "₹", masked)}") }
                                    acc.creditLimit?.let {
                                        if (isNotEmpty()) append("  •  ")
                                        append("Avl limit: ${Formatters.formatMoney(it, "₹", masked)}")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AccountType.entries.forEach { t ->
                                FilterChip(
                                    selected = acc.type == t,
                                    onClick = { onUpdateAccountType(acc.id, t) },
                                    label = { Text(t.label) }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { renaming = true }) { Text("Rename") }
                            OutlinedButton(onClick = { mergeSource = acc }) { Text("Merge into...") }
                        }
                    }
                }

                if (renaming) {
                    AlertDialog(
                        onDismissRequest = { renaming = false },
                        title = { Text("Rename Account") },
                        text = { OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, singleLine = true) },
                        confirmButton = {
                            Button(onClick = {
                                onRenameAccount(acc.id, nameInput)
                                renaming = false
                            }) { Text("Save") }
                        },
                        dismissButton = { TextButton(onClick = { renaming = false }) { Text("Cancel") } }
                    )
                }
            }
        }
    }

    mergeSource?.let { src ->
        AlertDialog(
            onDismissRequest = { mergeSource = null },
            title = { Text("Merge \"${src.displayName}\" into:") },
            text = {
                Column {
                    uiState.accounts.filter { it.id != src.id && it.mergedIntoAccountId == null }.forEach { target ->
                        TextButton(
                            onClick = {
                                onMergeAccounts(src.id, target.id)
                                mergeSource = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(target.displayName)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { mergeSource = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetsScreen(
    uiState: ExpensesUiState,
    onBack: () -> Unit,
    onUpsertBudget: (Long?, Double) -> Unit,
    onDeleteBudget: (Long) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var amountStr by remember { mutableStateOf("") }
    val masked = uiState.preferences.privacyMode
    val currency = uiState.homeSummary.primaryCurrency

    Column(modifier = Modifier.fillMaxSize()) {
        SubScreenHeader("Monthly Budgets", onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Set Monthly Budget", fontWeight = FontWeight.Bold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                label = { Text("Overall Monthly") }
                            )
                            uiState.categories.take(12).forEach { cat ->
                                FilterChip(
                                    selected = selectedCategoryId == cat.id,
                                    onClick = { selectedCategoryId = cat.id },
                                    label = { Text(cat.name) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("Monthly Limit (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(onClick = {
                            val amt = amountStr.toDoubleOrNull()?.takeIf { it > 0.0 } ?: return@Button
                            onUpsertBudget(selectedCategoryId, amt)
                            amountStr = ""
                        }) { Text("Save Budget") }
                    }
                }
            }

            items(uiState.homeSummary.budgetProgress, key = { it.budgetId }) { bp ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bp.label, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onDeleteBudget(bp.budgetId) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete budget")
                            }
                        }
                        Text(
                            "${Formatters.formatMoney(bp.spentAmount, currency, masked)} / ${Formatters.formatMoney(bp.budgetAmount, currency, masked)} (${(bp.ratio * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        LinearProgressIndicator(
                            progress = { bp.ratio.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringScreen(
    uiState: ExpensesUiState,
    onBack: () -> Unit
) {
    val masked = uiState.preferences.privacyMode
    Column(modifier = Modifier.fillMaxSize()) {
        SubScreenHeader("Recurring Payments", onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.recurringPayments.isEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Text(
                            "No monthly recurring subscriptions detected yet (requires 2+ monthly payments to the same merchant).",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(uiState.recurringPayments, key = { it.merchant }) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.merchant, fontWeight = FontWeight.Bold)
                                Text(
                                    "Every ~${item.averageIntervalDays} days • ${item.occurrenceCount} payments\nNext expected: ${Formatters.formatShortDate(item.nextExpectedAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                Formatters.formatMoney(item.averageAmount, item.currency, masked),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BackupAndSettingsScreen(
    uiState: ExpensesUiState,
    onBack: () -> Unit,
    onReprocessAllSms: () -> Unit,
    onLoadSampleData: () -> Unit,
    onClearAll: () -> Unit,
    onExportCsv: () -> Unit,
    onExportBackup: (password: String) -> Unit,
    onImportBackup: (password: String) -> Unit,
    onSetMonthStartDay: (Int) -> Unit,
    onSetFinancialYear: (Boolean) -> Unit,
    onSetAutoTransferWindow: (Int) -> Unit,
    onTogglePrivacyMode: () -> Unit,
    onSetFlagSecure: (Boolean) -> Unit,
    onSetThemeMode: (ThemePreference) -> Unit,
    onSetPin: (String?) -> Unit,
    onDeleteSenderRule: (Long) -> Unit
) {
    val prefs = uiState.preferences
    var backupPasswordDialogMode by remember { mutableStateOf<String?>(null) } // "EXPORT" or "IMPORT"
    var passwordInput by remember { mutableStateOf("") }
    var pinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        SubScreenHeader("Backup & Settings", onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Backup & Export (100% Offline)", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { backupPasswordDialogMode = "EXPORT" }, modifier = Modifier.weight(1f)) {
                                Text("Encrypted Backup")
                            }
                            OutlinedButton(onClick = { backupPasswordDialogMode = "IMPORT" }, modifier = Modifier.weight(1f)) {
                                Text("Restore Backup")
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onExportCsv, modifier = Modifier.weight(1f)) {
                                Text("Export CSV")
                            }
                            OutlinedButton(onClick = onReprocessAllSms, modifier = Modifier.weight(1f)) {
                                Text("Re-scan All SMS")
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onLoadSampleData, modifier = Modifier.weight(1f)) {
                                Text("Load Sample SMS")
                            }
                            OutlinedButton(onClick = onClearAll, modifier = Modifier.weight(1f)) {
                                Text("Clear All Data")
                            }
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Accounting & Calendar", fontWeight = FontWeight.Bold)
                        Text("Month start day: ${prefs.monthStartDay}", style = MaterialTheme.typography.bodySmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 5, 10, 15, 20, 25).forEach { day ->
                                FilterChip(
                                    selected = prefs.monthStartDay == day,
                                    onClick = { onSetMonthStartDay(day) },
                                    label = { Text("${day}th") }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Financial Year (Apr–Mar)")
                            Switch(checked = prefs.financialYearMode, onCheckedChange = onSetFinancialYear)
                        }
                        Text("Self-transfer window: ${prefs.autoTransferWindowMinutes} min", style = MaterialTheme.typography.bodySmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(5, 10, 30, 60).forEach { min ->
                                FilterChip(
                                    selected = prefs.autoTransferWindowMinutes == min,
                                    onClick = { onSetAutoTransferWindow(min) },
                                    label = { Text("${min}m") }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Privacy, Security & Theme", fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Privacy mode (mask amounts)")
                            Switch(checked = prefs.privacyMode, onCheckedChange = { onTogglePrivacyMode() })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Hide in Recents (FLAG_SECURE)")
                            Switch(checked = prefs.flagSecure, onCheckedChange = onSetFlagSecure)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (prefs.pinHash != null) "App PIN Lock (Enabled)" else "App PIN Lock (Off)")
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { pinDialog = true }) {
                                    Text(if (prefs.pinHash != null) "Change" else "Set PIN")
                                }
                                if (prefs.pinHash != null) {
                                    TextButton(onClick = { onSetPin(null) }) { Text("Remove") }
                                }
                            }
                        }
                        HorizontalDivider()
                        Text("Theme", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemePreference.entries.forEach { mode ->
                                FilterChip(
                                    selected = prefs.themeMode == mode,
                                    onClick = { onSetThemeMode(mode) },
                                    label = { Text(mode.label) }
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.senderRules.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Sender Rules", fontWeight = FontWeight.Bold)
                            uiState.senderRules.forEach { sr ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${sr.senderCode} (${sr.action})")
                                    IconButton(onClick = { onDeleteSenderRule(sr.id) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Remove sender rule")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        text = "Privacy guarantee: This app declares no INTERNET permission and sends zero data off-device. Uninstalling the app permanently deletes all local database records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }

    backupPasswordDialogMode?.let { mode ->
        AlertDialog(
            onDismissRequest = { backupPasswordDialogMode = null },
            title = { Text(if (mode == "EXPORT") "Encrypt Backup" else "Decrypt Backup") },
            text = {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Backup Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val pw = passwordInput
                    passwordInput = ""
                    backupPasswordDialogMode = null
                    if (pw.isNotBlank()) {
                        if (mode == "EXPORT") onExportBackup(pw) else onImportBackup(pw)
                    }
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { backupPasswordDialogMode = null }) { Text("Cancel") }
            }
        )
    }

    if (pinDialog) {
        AlertDialog(
            onDismissRequest = { pinDialog = false },
            title = { Text("Set App PIN") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("4-6 digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (pinInput.length >= 4) {
                        onSetPin(pinInput)
                        pinInput = ""
                        pinDialog = false
                    }
                }) { Text("Save PIN") }
            },
            dismissButton = { TextButton(onClick = { pinDialog = false }) { Text("Cancel") } }
        )
    }
}

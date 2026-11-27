package com.banksms.expenses.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.banksms.expenses.data.model.CategoryEntity
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionSplitEntity
import com.banksms.expenses.data.model.TransactionType
import com.banksms.expenses.ui.util.Formatters
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionDetailSheet(
    transaction: TransactionEntity,
    allTransactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    existingSplits: List<TransactionSplitEntity>,
    privacyMode: Boolean,
    onDismiss: () -> Unit,
    onUpdateCategorySingle: (Long) -> Unit,
    onAlwaysCategoriseMerchant: (String, Long) -> Unit,
    onOpenCustomCategoryRule: (TransactionEntity, Long) -> Unit,
    onExcludeSingle: (ExclusionReason) -> Unit,
    onExcludeSimilar: (name: String, reason: ExclusionReason, merchantPattern: String?, bodyKeyword: String?, exactAmount: Double?) -> Unit,
    onOpenCustomExclusionRule: (TransactionEntity, ExclusionReason) -> Unit,
    onIncludeAgain: () -> Unit,
    onUpdateDetails: (merchant: String, amount: Double, txnTime: Long, note: String, tags: String) -> Unit,
    onSaveSplits: (List<TransactionSplitEntity>) -> Unit,
    onDelete: () -> Unit
) {
    var pendingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showExclusionReasonPicker by remember { mutableStateOf(false) }
    var pendingExclusionReason by remember { mutableStateOf<ExclusionReason?>(null) }
    var showEditDetails by remember { mutableStateOf(false) }
    var showSplitEditor by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${transaction.bankName} • ${Formatters.formatDateTime(transaction.txnTime)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val sign = if (transaction.type == TransactionType.DEBIT) "-" else "+"
                Text(
                    text = "$sign${Formatters.formatMoney(transaction.amount, transaction.currency, privacyMode)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = buildString {
                    if (!transaction.accountLast4.isNullOrBlank()) append("Account ••${transaction.accountLast4}")
                    append(if (isNotEmpty()) " • ${transaction.channel.label}" else transaction.channel.label)
                    if (!transaction.refNo.isNullOrBlank()) append(" • Ref ${transaction.refNo}")
                    if (transaction.balanceAfter != null) {
                        append("\nBalance: ${Formatters.formatMoney(transaction.balanceAfter, transaction.currency, privacyMode)}")
                    }
                    if (transaction.isInternational) append(" • International")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (transaction.isExcluded) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Excluded from summary",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "${transaction.exclusionReason?.label ?: "Other"} (${transaction.exclusionSource.label})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        TextButton(onClick = onIncludeAgain) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Text(" Include")
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showEditDetails = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                    Text(" Edit Note/Tags")
                }
                OutlinedButton(
                    onClick = { showSplitEditor = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.CallSplit, contentDescription = null)
                    Text(if (existingSplits.isEmpty()) " Split" else " Splits (${existingSplits.size})")
                }
            }

            if (transaction.note.isNotBlank() || transaction.tags.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = buildString {
                        if (transaction.note.isNotBlank()) append("Note: ${transaction.note}")
                        if (transaction.tags.isNotBlank()) {
                            if (isNotEmpty()) append("\n")
                            append("Tags: ${transaction.tags}")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Category (${transaction.categorySource.label})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = transaction.categoryId == category.id,
                        onClick = {
                            if (transaction.categoryId != category.id) {
                                pendingCategory = category
                            }
                        },
                        label = { Text(category.name) }
                    )
                }
            }

            if (!transaction.isExcluded) {
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { showExclusionReasonPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Block, contentDescription = null)
                    Text("  Don't count in summary")
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = "Original SMS (${transaction.sender})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = transaction.rawBody,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null)
                Text("  Remove Transaction")
            }
        }
    }

    // Prompt: Always categorise [Merchant] as [Category]?
    pendingCategory?.let { chosenCat ->
        val matchingCount = remember(transaction.merchant, allTransactions) {
            allTransactions.count {
                it.merchant.equals(transaction.merchant, ignoreCase = true) ||
                    it.rawMerchant.equals(transaction.rawMerchant, ignoreCase = true)
            }
        }
        AlertDialog(
            onDismissRequest = { pendingCategory = null },
            title = { Text("Always categorise ${transaction.merchant} as ${chosenCat.name}?") },
            text = {
                Text("This affects $matchingCount transaction(s) from ${transaction.merchant} and future SMS from this merchant.")
            },
            confirmButton = {
                Button(onClick = {
                    onAlwaysCategoriseMerchant(transaction.merchant, chosenCat.id)
                    pendingCategory = null
                }) {
                    Text("All past & future ($matchingCount)")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        onOpenCustomCategoryRule(transaction, chosenCat.id)
                        pendingCategory = null
                    }) {
                        Text("Custom rule")
                    }
                    TextButton(onClick = {
                        onUpdateCategorySingle(chosenCat.id)
                        pendingCategory = null
                    }) {
                        Text("This one only")
                    }
                }
            }
        )
    }

    // Reason picker for exclusion
    if (showExclusionReasonPicker) {
        AlertDialog(
            onDismissRequest = { showExclusionReasonPicker = false },
            title = { Text("Why exclude from summary?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExclusionReason.entries.forEach { reason ->
                        OutlinedButton(
                            onClick = {
                                showExclusionReasonPicker = false
                                pendingExclusionReason = reason
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(reason.label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExclusionReasonPicker = false }) { Text("Cancel") }
            }
        )
    }

    // Apply to similar transactions prompt
    pendingExclusionReason?.let { reason ->
        val merchantMatches = remember(transaction, allTransactions) {
            allTransactions.count { it.merchant.equals(transaction.merchant, ignoreCase = true) }
        }
        val sameAmtMerchantMatches = remember(transaction, allTransactions) {
            allTransactions.count {
                it.merchant.equals(transaction.merchant, ignoreCase = true) &&
                    abs(it.amount - transaction.amount) <= 1.0
            }
        }
        AlertDialog(
            onDismissRequest = { pendingExclusionReason = null },
            title = { Text("Apply to similar transactions?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Reason: ${reason.label}")
                    OutlinedButton(
                        onClick = {
                            onExcludeSingle(reason)
                            pendingExclusionReason = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("This transaction only")
                    }
                    OutlinedButton(
                        onClick = {
                            onExcludeSimilar(
                                "Exclude ${transaction.merchant}",
                                reason,
                                transaction.merchant,
                                null,
                                null
                            )
                            pendingExclusionReason = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("All from ${transaction.merchant} ($merchantMatches)")
                    }
                    OutlinedButton(
                        onClick = {
                            onExcludeSimilar(
                                "Exclude ${transaction.merchant} (${Formatters.formatMoney(transaction.amount, transaction.currency)})",
                                reason,
                                transaction.merchant,
                                null,
                                transaction.amount
                            )
                            pendingExclusionReason = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Same amount + merchant ($sameAmtMerchantMatches)")
                    }
                    OutlinedButton(
                        onClick = {
                            onOpenCustomExclusionRule(transaction, reason)
                            pendingExclusionReason = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("All matching a custom pattern...")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pendingExclusionReason = null }) { Text("Cancel") }
            }
        )
    }

    if (showEditDetails) {
        var merchant by remember { mutableStateOf(transaction.merchant) }
        var amountStr by remember { mutableStateOf(transaction.amount.toString()) }
        var note by remember { mutableStateOf(transaction.note) }
        var tags by remember { mutableStateOf(transaction.tags) }

        AlertDialog(
            onDismissRequest = { showEditDetails = false },
            title = { Text("Edit Transaction") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = merchant,
                        onValueChange = { merchant = it },
                        label = { Text("Merchant") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note") },
                        maxLines = 3
                    )
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated, e.g. trip-goa)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: transaction.amount
                    onUpdateDetails(merchant, amt, transaction.txnTime, note, tags)
                    showEditDetails = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDetails = false }) { Text("Cancel") }
            }
        )
    }

    if (showSplitEditor) {
        SplitTransactionDialog(
            totalAmount = transaction.amount,
            currency = transaction.currency,
            categories = categories,
            initialSplits = existingSplits,
            defaultCategoryId = transaction.categoryId,
            onDismiss = { showSplitEditor = false },
            onSave = { splits ->
                onSaveSplits(splits)
                showSplitEditor = false
            }
        )
    }
}

@Composable
private fun SplitTransactionDialog(
    totalAmount: Double,
    currency: String,
    categories: List<CategoryEntity>,
    initialSplits: List<TransactionSplitEntity>,
    defaultCategoryId: Long,
    onDismiss: () -> Unit,
    onSave: (List<TransactionSplitEntity>) -> Unit
) {
    val rows = remember {
        mutableStateListOf<Pair<Long, String>>().apply {
            if (initialSplits.isNotEmpty()) {
                initialSplits.forEach { add(it.categoryId to it.amount.toString()) }
            } else {
                add(defaultCategoryId to (totalAmount / 2.0).toString())
                add((categories.firstOrNull { it.id != defaultCategoryId }?.id ?: defaultCategoryId) to (totalAmount / 2.0).toString())
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Split ${Formatters.formatMoney(totalAmount, currency)}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEachIndexed { idx, (catId, amtStr) ->
                    val catName = categories.firstOrNull { it.id == catId }?.name ?: "Category"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val currIdx = categories.indexOfFirst { it.id == catId }
                                val next = categories[(currIdx + 1) % categories.size]
                                rows[idx] = next.id to amtStr
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(catName, maxLines = 1)
                        }
                        OutlinedTextField(
                            value = amtStr,
                            onValueChange = { rows[idx] = catId to it },
                            label = { Text("Amt") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { rows.add(defaultCategoryId to "0") }) {
                        Text("+ Add split")
                    }
                    if (initialSplits.isNotEmpty()) {
                        TextButton(onClick = { onSave(emptyList()) }) {
                            Text("Clear splits")
                        }
                    }
                }
                HorizontalDivider()
                val sum = rows.sumOf { it.second.toDoubleOrNull() ?: 0.0 }
                Text(
                    text = "Split total: ${Formatters.formatMoney(sum, currency)} / ${Formatters.formatMoney(totalAmount, currency)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsed = rows.mapNotNull { (cid, amtStr) ->
                    val v = amtStr.toDoubleOrNull()?.takeIf { it > 0.0 } ?: return@mapNotNull null
                    TransactionSplitEntity(transactionId = 0L, categoryId = cid, amount = v)
                }
                onSave(parsed)
            }) { Text("Save Splits") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.banksms.expenses.data.model.AccountEntity
import com.banksms.expenses.data.model.CategoryEntity
import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ParseTemplateEntity
import com.banksms.expenses.data.model.TransactionType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualTransactionDialog(
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (
        merchant: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        accountId: Long?,
        txnTime: Long,
        note: String,
        tags: String
    ) -> Unit
) {
    var merchant by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.DEBIT) }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: 1L) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Manual Transaction") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(if (t == TransactionType.DEBIT) "Expense" else "Income") }
                        )
                    }
                }
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant / Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Account", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedAccountId == null,
                        onClick = { selectedAccountId = null },
                        label = { Text("Cash") }
                    )
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = selectedAccountId == acc.id,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text(acc.displayName) }
                        )
                    }
                }
                Text("Category", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: return@Button
                    if (amt <= 0.0 || merchant.isBlank()) return@Button
                    onSave(merchant, amt, type, selectedCategoryId, selectedAccountId, System.currentTimeMillis(), note, tags)
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryRuleEditorDialog(
    initialRule: CategoryRuleEntity?,
    prefillMerchant: String? = null,
    prefillCategoryId: Long? = null,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (CategoryRuleEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialRule?.name ?: prefillMerchant?.let { "Rule: $it" }.orEmpty()) }
    var categoryId by remember { mutableStateOf(initialRule?.categoryId ?: prefillCategoryId ?: categories.firstOrNull()?.id ?: 1L) }
    var merchantPattern by remember { mutableStateOf(initialRule?.merchantPattern ?: prefillMerchant.orEmpty()) }
    var matchMode by remember { mutableStateOf(initialRule?.merchantMatchMode ?: "CONTAINS") }
    var bodyKeyword by remember { mutableStateOf(initialRule?.bodyKeyword.orEmpty()) }
    var senderPattern by remember { mutableStateOf(initialRule?.senderPattern.orEmpty()) }
    var vpaPattern by remember { mutableStateOf(initialRule?.vpaPattern.orEmpty()) }
    var minAmt by remember { mutableStateOf(initialRule?.minAmount?.toString().orEmpty()) }
    var maxAmt by remember { mutableStateOf(initialRule?.maxAmount?.toString().orEmpty()) }
    var regexPattern by remember { mutableStateOf(initialRule?.regexPattern.orEmpty()) }
    var alsoExclude by remember { mutableStateOf(initialRule?.alsoExclude ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialRule == null) "New Category Rule" else "Edit Category Rule") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rule Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Assign Category", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = categoryId == cat.id,
                            onClick = { categoryId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = merchantPattern,
                    onValueChange = { merchantPattern = it },
                    label = { Text("Merchant contains/equals") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CONTAINS", "EQUALS").forEach { mode ->
                        FilterChip(
                            selected = matchMode == mode,
                            onClick = { matchMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }
                OutlinedTextField(
                    value = bodyKeyword,
                    onValueChange = { bodyKeyword = it },
                    label = { Text("SMS body keyword") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = senderPattern,
                    onValueChange = { senderPattern = it },
                    label = { Text("Sender ID contains (e.g. HDFCBK)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = vpaPattern,
                    onValueChange = { vpaPattern = it },
                    label = { Text("UPI VPA pattern") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minAmt,
                        onValueChange = { minAmt = it },
                        label = { Text("Min ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxAmt,
                        onValueChange = { maxAmt = it },
                        label = { Text("Max ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = regexPattern,
                    onValueChange = { regexPattern = it },
                    label = { Text("Advanced Regex (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = alsoExclude, onCheckedChange = { alsoExclude = it })
                    Text("Also exclude from summary")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val rule = CategoryRuleEntity(
                    id = initialRule?.id ?: 0L,
                    name = name.ifBlank { "Rule: ${merchantPattern.ifBlank { bodyKeyword }}" },
                    priority = initialRule?.priority ?: 50,
                    categoryId = categoryId,
                    merchantPattern = merchantPattern.takeIf { it.isNotBlank() },
                    merchantMatchMode = matchMode,
                    bodyKeyword = bodyKeyword.takeIf { it.isNotBlank() },
                    senderPattern = senderPattern.takeIf { it.isNotBlank() },
                    vpaPattern = vpaPattern.takeIf { it.isNotBlank() },
                    minAmount = minAmt.toDoubleOrNull(),
                    maxAmount = maxAmt.toDoubleOrNull(),
                    regexPattern = regexPattern.takeIf { it.isNotBlank() },
                    alsoExclude = alsoExclude,
                    exclusionReason = if (alsoExclude) ExclusionReason.OTHER else null,
                    enabled = initialRule?.enabled ?: true
                )
                onSave(rule)
            }) { Text("Save Rule") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExclusionRuleEditorDialog(
    initialMerchant: String = "",
    initialKeyword: String = "",
    initialReason: ExclusionReason = ExclusionReason.OTHER,
    onDismiss: () -> Unit,
    onSave: (name: String, reason: ExclusionReason, merchant: String?, keyword: String?, exactAmount: Double?) -> Unit
) {
    var name by remember { mutableStateOf(if (initialMerchant.isNotBlank()) "Exclude $initialMerchant" else "") }
    var merchant by remember { mutableStateOf(initialMerchant) }
    var keyword by remember { mutableStateOf(initialKeyword) }
    var amountStr by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf(initialReason) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Exclusion Rule") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rule Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant contains") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("SMS text contains (e.g. ACH D-)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Optional exact amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Reason", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExclusionReason.entries.forEach { r ->
                        FilterChip(
                            selected = reason == r,
                            onClick = { reason = r },
                            label = { Text(r.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (merchant.isBlank() && keyword.isBlank()) return@Button
                onSave(
                    name.ifBlank { "Exclude ${keyword.ifBlank { merchant }}" },
                    reason,
                    merchant.takeIf { it.isNotBlank() },
                    keyword.takeIf { it.isNotBlank() },
                    amountStr.toDoubleOrNull()
                )
            }) { Text("Apply Rule") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TemplateBuilderDialog(
    initialSender: String,
    sampleBody: String,
    onDismiss: () -> Unit,
    onSave: (ParseTemplateEntity) -> Unit
) {
    var name by remember { mutableStateOf("Template: $initialSender") }
    var senderPattern by remember { mutableStateOf(initialSender) }
    var regexPattern by remember { mutableStateOf("""(?:Rs\.?|INR)\s*([0-9,]+\.?[0-9]*).*(?:at|to)\s+([A-Za-z0-9 &._-]+)""") }
    var amountGroup by remember { mutableStateOf("1") }
    var merchantGroup by remember { mutableStateOf("2") }
    var direction by remember { mutableStateOf(TransactionType.DEBIT) }

    val previewMatch = remember(regexPattern, sampleBody) {
        runCatching {
            val r = Regex(regexPattern, RegexOption.IGNORE_CASE)
            val m = r.find(sampleBody.replace('\n', ' '))
            if (m == null) "No match yet"
            else {
                val amt = amountGroup.toIntOrNull()?.let { m.groups[it]?.value }.orEmpty()
                val mer = merchantGroup.toIntOrNull()?.let { m.groups[it]?.value }.orEmpty()
                "Matched! Amount=$amt, Merchant=$mer"
            }
        }.getOrElse { "Invalid regex: ${it.message}" }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SMS Parse Template Builder") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sampleBody,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = senderPattern,
                    onValueChange = { senderPattern = it },
                    label = { Text("Sender contains") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = regexPattern,
                    onValueChange = { regexPattern = it },
                    label = { Text("Regex with capture groups") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountGroup,
                        onValueChange = { amountGroup = it },
                        label = { Text("Amount group #") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = merchantGroup,
                        onValueChange = { merchantGroup = it },
                        label = { Text("Merchant group #") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { t ->
                        FilterChip(
                            selected = direction == t,
                            onClick = { direction = t },
                            label = { Text(t.name) }
                        )
                    }
                }
                Text(
                    text = previewMatch,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (previewMatch.startsWith("Matched")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    ParseTemplateEntity(
                        name = name,
                        senderPattern = senderPattern,
                        regexPattern = regexPattern,
                        amountGroup = amountGroup.toIntOrNull() ?: 1,
                        merchantGroup = merchantGroup.toIntOrNull(),
                        defaultDirection = direction,
                        isBuiltin = false,
                        enabled = true
                    )
                )
            }) { Text("Save & Re-parse") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PinLockScreen(
    onVerifyPin: (String) -> Boolean,
    onUnlocked: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "App Locked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Enter your PIN to view your expenses",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = {
                    pin = it
                    error = false
                },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                isError = error
            )
            if (error) {
                Spacer(Modifier.height(6.dp))
                Text("Incorrect PIN", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                if (onVerifyPin(pin)) {
                    onUnlocked()
                } else {
                    error = true
                }
            }) {
                Text("Unlock")
            }
        }
    }
}

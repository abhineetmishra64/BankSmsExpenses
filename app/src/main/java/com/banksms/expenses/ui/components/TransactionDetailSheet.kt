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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionType
import com.banksms.expenses.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionDetailSheet(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onUpdateCategory: (ExpenseCategory) -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                        text = "${transaction.bankName} • ${Formatters.formatDateTime(transaction.timestamp)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val sign = if (transaction.type == TransactionType.DEBIT) "-" else "+"
                Text(
                    text = "$sign${Formatters.formatMoney(transaction.amount, transaction.currency)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!transaction.accountLast4.isNullOrBlank() || transaction.balanceAfter != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = buildString {
                        if (!transaction.accountLast4.isNullOrBlank()) {
                            append("Account/Card ••${transaction.accountLast4}")
                        }
                        if (transaction.balanceAfter != null) {
                            if (isNotEmpty()) append("   •   ")
                            append("Balance: ${Formatters.formatMoney(transaction.balanceAfter, transaction.currency)}")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExpenseCategory.entries.forEach { category ->
                    FilterChip(
                        selected = transaction.category == category,
                        onClick = { onUpdateCategory(category) },
                        label = { Text(category.label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
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

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null)
                Text("  Remove Transaction")
            }
        }
    }
}

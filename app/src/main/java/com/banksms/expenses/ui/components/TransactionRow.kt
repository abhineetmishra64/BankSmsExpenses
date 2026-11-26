package com.banksms.expenses.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionType
import com.banksms.expenses.data.sms.BankDirectory
import com.banksms.expenses.ui.util.Formatters

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bankColor = Color(BankDirectory.accentColorForBank(transaction.bankName))
    val isDebit = transaction.type == TransactionType.DEBIT
    val amountColor = if (isDebit) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color(0xFF0F766E)
    }
    val prefix = if (isDebit) "-" else "+"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bankColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(transaction.category),
                    contentDescription = transaction.category.label,
                    tint = bankColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = bankColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = transaction.bankName,
                            style = MaterialTheme.typography.labelSmall,
                            color = bankColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }
                    if (!transaction.accountLast4.isNullOrBlank()) {
                        Text(
                            text = "••${transaction.accountLast4}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = Formatters.formatDateTime(transaction.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$prefix${Formatters.formatMoney(transaction.amount, transaction.currency)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = transaction.category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun categoryIcon(category: ExpenseCategory): ImageVector {
    return when (category) {
        ExpenseCategory.FOOD -> Icons.Rounded.Restaurant
        ExpenseCategory.SHOPPING -> Icons.Rounded.ShoppingBag
        ExpenseCategory.TRANSPORT -> Icons.Rounded.DirectionsCar
        ExpenseCategory.BILLS -> Icons.Rounded.ReceiptLong
        ExpenseCategory.ENTERTAINMENT -> Icons.Rounded.Movie
        ExpenseCategory.HEALTH -> Icons.Rounded.LocalHospital
        ExpenseCategory.TRANSFER -> Icons.Rounded.SwapHoriz
        ExpenseCategory.ATM -> Icons.Rounded.LocalAtm
        ExpenseCategory.OTHER -> Icons.Rounded.AccountBalanceWallet
    }
}

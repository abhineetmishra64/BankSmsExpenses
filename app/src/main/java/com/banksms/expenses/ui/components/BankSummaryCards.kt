package com.banksms.expenses.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.banksms.expenses.ui.BankSummary
import com.banksms.expenses.ui.util.Formatters

@Composable
fun AllBanksHeroCard(
    summary: BankSummary,
    bankCount: Int,
    isSelected: Boolean,
    timeframeLabel: String,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All Banks Combined",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = contentColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "$timeframeLabel • $bankCount ${if (bankCount == 1) "bank" else "banks"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = Formatters.formatMoney(summary.totalExpenses, summary.primaryCurrency),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = "Total expenses across all banks (${summary.expenseCount} debits)",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill(
                    label = "Credits / Inflow",
                    value = Formatters.formatMoney(summary.totalCredits, summary.primaryCurrency),
                    contentColor = contentColor
                )
                MetricPill(
                    label = "Net Outflow",
                    value = Formatters.formatMoney(
                        (summary.totalExpenses - summary.totalCredits).coerceAtLeast(0.0),
                        summary.primaryCurrency
                    ),
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    contentColor: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.78f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
fun PerBankSection(
    banks: List<BankSummary>,
    selectedBank: String?,
    onSelectBank: (String?) -> Unit
) {
    if (banks.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Expenses by Bank (${banks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (selectedBank != null) {
                Text(
                    text = "Show all banks",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onSelectBank(null) }
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(banks, key = { it.bankName }) { bank ->
                BankExpenseCard(
                    summary = bank,
                    isSelected = selectedBank == bank.bankName,
                    onClick = {
                        onSelectBank(if (selectedBank == bank.bankName) null else bank.bankName)
                    }
                )
            }
        }
    }
}

@Composable
private fun BankExpenseCard(
    summary: BankSummary,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accent = Color(summary.accentColorHex)
    val border = if (isSelected) {
        BorderStroke(2.dp, accent)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    }

    Card(
        modifier = Modifier
            .width(210.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                accent.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = summary.bankName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Selected",
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = Formatters.formatMoney(summary.totalExpenses, summary.primaryCurrency),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${summary.expenseCount} debits • ${summary.creditCount} credits",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { summary.shareOfTotalExpenses },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = accent,
                trackColor = accent.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            val sharePct = (summary.shareOfTotalExpenses * 100).toInt()
            val footerText = if (summary.latestBalance != null) {
                "$sharePct% share • Bal ${Formatters.formatMoney(summary.latestBalance, summary.primaryCurrency)}"
            } else {
                "$sharePct% of total spend"
            }
            Text(
                text = footerText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

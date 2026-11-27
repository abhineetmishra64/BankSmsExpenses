package com.banksms.expenses.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.banksms.expenses.data.model.SmsRawEntity
import com.banksms.expenses.data.sms.ExclusionSuggestion
import com.banksms.expenses.ui.ExpensesUiState
import com.banksms.expenses.ui.util.Formatters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewScreen(
    uiState: ExpensesUiState,
    onCategoriseGroup: (merchant: String, categoryId: Long) -> Unit,
    onOpenTemplateBuilder: (SmsRawEntity) -> Unit,
    onIgnoreSender: (String) -> Unit,
    onAcceptSuggestion: (ExclusionSuggestion) -> Unit,
    onDismissSuggestion: (ExclusionSuggestion) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val masked = uiState.preferences.privacyMode

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Uncategorised (${uiState.uncategorisedGroups.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Unparsed (${uiState.unparsedSms.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Suggestions (${uiState.exclusionSuggestions.size})") }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    if (uiState.uncategorisedGroups.isEmpty()) {
                        item { ReviewEmptyCard("All transactions are categorised!") }
                    } else {
                        items(uiState.uncategorisedGroups, key = { it.merchant }) { group ->
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
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${group.merchant} (${group.count})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = Formatters.formatMoney(group.totalAmount, group.currency, masked),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    group.sampleTransactions.firstOrNull()?.let { sample ->
                                        Text(
                                            text = sample.rawBody,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                    Text(
                                        text = "Tap a category to classify all ${group.count} & remember rule:",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        uiState.categories
                                            .filter { it.name != "Uncategorised" }
                                            .take(12)
                                            .forEach { cat ->
                                                FilterChip(
                                                    selected = false,
                                                    onClick = { onCategoriseGroup(group.merchant, cat.id) },
                                                    label = { Text(cat.name) }
                                                )
                                            }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    if (uiState.unparsedSms.isEmpty()) {
                        item { ReviewEmptyCard("No unparsed bank SMS in queue.") }
                    } else {
                        items(uiState.unparsedSms, key = { it.id }) { sms ->
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
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = sms.sender,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = Formatters.formatDateTime(sms.receivedAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = sms.body,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { onOpenTemplateBuilder(sms) }) {
                                            Text("Build Parse Template")
                                        }
                                        OutlinedButton(onClick = { onIgnoreSender(sms.sender) }) {
                                            Text("Ignore Sender")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    if (uiState.exclusionSuggestions.isEmpty()) {
                        item { ReviewEmptyCard("No new exclusion suggestions.") }
                    } else {
                        items(uiState.exclusionSuggestions, key = { it.suggestionKey }) { sug ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = sug.transaction.merchant,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = Formatters.formatMoney(sug.transaction.amount, sug.transaction.currency, masked),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Suggested: ${sug.suggestedReason.label} (matched \"${sug.matchedKeyword}\")",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = sug.transaction.rawBody,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { onAcceptSuggestion(sug) }) {
                                            Text("Exclude")
                                        }
                                        OutlinedButton(onClick = { onDismissSuggestion(sug) }) {
                                            Text("Dismiss")
                                        }
                                    }
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
private fun ReviewEmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(24.dp)
        )
    }
}

package com.banksms.expenses.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.banksms.expenses.R
import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.SmsRawEntity
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.ui.components.CategoryRuleEditorDialog
import com.banksms.expenses.ui.components.ExclusionRuleEditorDialog
import com.banksms.expenses.ui.components.ManualTransactionDialog
import com.banksms.expenses.ui.components.PinLockScreen
import com.banksms.expenses.ui.components.TemplateBuilderDialog
import com.banksms.expenses.ui.components.TransactionDetailSheet
import com.banksms.expenses.ui.screens.AccountsScreen
import com.banksms.expenses.ui.screens.BackupAndSettingsScreen
import com.banksms.expenses.ui.screens.BudgetsScreen
import com.banksms.expenses.ui.screens.CategoriesAndRulesScreen
import com.banksms.expenses.ui.screens.ExclusionsScreen
import com.banksms.expenses.ui.screens.HomeScreen
import com.banksms.expenses.ui.screens.MoreMenuScreen
import com.banksms.expenses.ui.screens.RecurringScreen
import com.banksms.expenses.ui.screens.ReviewScreen
import com.banksms.expenses.ui.screens.TransactionsScreen
import com.banksms.expenses.ui.screens.TrendsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: ExpensesUiState,
    viewModel: ExpensesViewModel,
    hasSmsPermission: Boolean,
    onRequestSmsPermission: () -> Unit,
    onExportCsv: () -> Unit,
    onExportRulesJson: () -> Unit,
    onImportRulesJson: () -> Unit,
    onExportEncryptedBackup: (String) -> Unit,
    onImportEncryptedBackup: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTxnId by remember { mutableStateOf<Long?>(null) }
    var showManualDialog by remember { mutableStateOf(false) }
    var editingCategoryRule by remember { mutableStateOf<Triple<CategoryRuleEntity?, String?, Long?>?>(null) }
    var editingExclusionRule by remember { mutableStateOf<Triple<String, String, ExclusionReason>?>(null) }
    var templateBuilderSms by remember { mutableStateOf<SmsRawEntity?>(null) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var isPinUnlocked by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearStatusMessage()
    }

    if (uiState.preferences.pinHash != null && !isPinUnlocked) {
        Scaffold { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                PinLockScreen(
                    onVerifyPin = viewModel::verifyPin,
                    onUnlocked = { isPinUnlocked = true }
                )
            }
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "App logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SMS Expense Tracker",
                                fontWeight = FontWeight.Bold
                            )
                            val subtitle = when {
                                uiState.currentTab == MainTab.MORE && uiState.moreSubScreen != null -> uiState.moreSubScreen.title
                                uiState.preferences.selectedAccountIds.isEmpty() -> "All Accounts • Offline"
                                else -> "${uiState.preferences.selectedAccountIds.size} account(s) selected"
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::togglePrivacyMode) {
                        Icon(
                            imageVector = if (uiState.preferences.privacyMode) {
                                Icons.Rounded.VisibilityOff
                            } else {
                                Icons.Rounded.Visibility
                            },
                            contentDescription = "Toggle privacy mode"
                        )
                    }

                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        IconButton(onClick = {
                            if (hasSmsPermission) viewModel.syncSmsInbox() else onRequestSmsPermission()
                        }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Sync SMS")
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
                                text = { Text("Add manual transaction") },
                                onClick = {
                                    showOverflowMenu = false
                                    showManualDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Re-process all stored SMS") },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.reprocessAllSms()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Load sample bank SMS") },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.loadSampleData()
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    val icon = when (tab) {
                        MainTab.HOME -> Icons.Rounded.Home
                        MainTab.TRENDS -> Icons.AutoMirrored.Rounded.ShowChart
                        MainTab.TRANSACTIONS -> Icons.AutoMirrored.Rounded.ListAlt
                        MainTab.REVIEW -> Icons.Rounded.Inbox
                        MainTab.MORE -> Icons.Rounded.Menu
                    }
                    NavigationBarItem(
                        selected = uiState.currentTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            if (tab == MainTab.REVIEW && uiState.needsAttentionCount > 0) {
                                BadgedBox(badge = { Badge { Text("${uiState.needsAttentionCount}") } }) {
                                    Icon(icon, contentDescription = tab.label)
                                }
                            } else {
                                Icon(icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label, maxLines = 1) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                MainTab.HOME -> HomeScreen(
                    uiState = uiState,
                    hasSmsPermission = hasSmsPermission,
                    onRequestSmsPermission = onRequestSmsPermission,
                    onSelectBackfillMonths = viewModel::setBackfillMonths,
                    onSyncSms = viewModel::syncSmsInbox,
                    onLoadSampleData = viewModel::loadSampleData,
                    onPrevMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onToggleAccount = viewModel::toggleAccountFilter,
                    onOpenExclusions = viewModel::openExclusionsForCurrentMonth,
                    onOpenCategoryTransactions = viewModel::openTransactionsForCategory,
                    onOpenAllTransactions = { viewModel.selectTab(MainTab.TRANSACTIONS) },
                    onOpenReview = { viewModel.selectTab(MainTab.REVIEW) },
                    onSelectTransaction = { selectedTxnId = it.id }
                )

                MainTab.TRENDS -> TrendsScreen(
                    uiState = uiState,
                    onToggleAccount = viewModel::toggleAccountFilter,
                    onSelectRange = viewModel::setTrendRange,
                    onSelectCategoryFilter = viewModel::setTrendCategoryFilter,
                    onToggleNetLine = viewModel::toggleNetSavingsLine,
                    onToggleStacked = viewModel::toggleStackedCategories,
                    onToggleTable = viewModel::toggleTrendTable,
                    onSelectPeriod = viewModel::selectTrendPeriod,
                    onOpenMonthOnHome = viewModel::openMonthOnHome
                )

                MainTab.TRANSACTIONS -> TransactionsScreen(
                    uiState = uiState,
                    onSearchChange = viewModel::updateTxnSearchQuery,
                    onSelectDirection = viewModel::setTxnDirectionFilter,
                    onSelectCategory = viewModel::setTxnCategoryFilter,
                    onSelectExclusionFilter = viewModel::setTxnExclusionFilter,
                    onToggleUncategorisedOnly = viewModel::toggleTxnUncategorisedOnly,
                    onToggleMonthScoped = viewModel::toggleTxnMonthScoped,
                    onAmountRangeChange = viewModel::setTxnAmountRange,
                    onClearFilters = viewModel::clearTransactionFilters,
                    onToggleAccount = viewModel::toggleAccountFilter,
                    onSelectTransaction = { selectedTxnId = it.id },
                    onAddManualClick = { showManualDialog = true }
                )

                MainTab.REVIEW -> ReviewScreen(
                    uiState = uiState,
                    onCategoriseGroup = viewModel::categoriseMerchantGroup,
                    onOpenTemplateBuilder = { sms -> templateBuilderSms = sms },
                    onIgnoreSender = { sender -> viewModel.setSenderRule(sender, "IGNORE") },
                    onAcceptSuggestion = viewModel::acceptExclusionSuggestion,
                    onDismissSuggestion = viewModel::dismissExclusionSuggestion
                )

                MainTab.MORE -> when (uiState.moreSubScreen) {
                    null -> MoreMenuScreen(
                        uiState = uiState,
                        onOpenSubScreen = viewModel::openMoreSubScreen
                    )

                    MoreSubScreen.EXCLUSIONS -> ExclusionsScreen(
                        uiState = uiState,
                        onBack = { viewModel.openMoreSubScreen(null) },
                        onIncludeAgain = viewModel::includeTransactionsAgain,
                        onBulkExclude = viewModel::bulkExcludeByQuery,
                        onToggleRule = viewModel::toggleExclusionRule,
                        onResetRule = viewModel::resetExclusionRule,
                        onOpenNewRule = { editingExclusionRule = Triple("", "", ExclusionReason.OTHER) },
                        onAcceptSuggestion = viewModel::acceptExclusionSuggestion,
                        onDismissSuggestion = viewModel::dismissExclusionSuggestion
                    )

                    MoreSubScreen.CATEGORIES_RULES -> CategoriesAndRulesScreen(
                        uiState = uiState,
                        onBack = { viewModel.openMoreSubScreen(null) },
                        onOpenNewCategoryRule = { editingCategoryRule = Triple(null, null, null) },
                        onEditCategoryRule = { rule -> editingCategoryRule = Triple(rule, null, null) },
                        onToggleCategoryRule = viewModel::toggleCategoryRule,
                        onMoveCategoryRule = viewModel::moveCategoryRule,
                        onDeleteCategoryRule = viewModel::deleteCategoryRule,
                        onTestRuleSms = viewModel::testRulesOnSampleSms,
                        onAddCategory = viewModel::addCategory,
                        onRenameCategory = viewModel::renameCategory,
                        onMergeCategory = viewModel::mergeCategories,
                        onDeleteCategory = viewModel::deleteCategory,
                        onAddAlias = viewModel::addAlias,
                        onDeleteAlias = viewModel::deleteAlias,
                        onExportRulesJson = onExportRulesJson,
                        onImportRulesJson = onImportRulesJson
                    )

                    MoreSubScreen.ACCOUNTS -> AccountsScreen(
                        uiState = uiState,
                        onBack = { viewModel.openMoreSubScreen(null) },
                        onRenameAccount = viewModel::renameAccount,
                        onUpdateAccountType = viewModel::updateAccountType,
                        onToggleActive = viewModel::toggleAccountActive,
                        onMergeAccounts = viewModel::mergeAccounts
                    )

                    MoreSubScreen.BUDGETS -> BudgetsScreen(
                        uiState = uiState,
                        onBack = { viewModel.openMoreSubScreen(null) },
                        onUpsertBudget = viewModel::upsertBudget,
                        onDeleteBudget = viewModel::deleteBudget
                    )

                    MoreSubScreen.RECURRING -> RecurringScreen(
                        uiState = uiState,
                        onBack = { viewModel.openMoreSubScreen(null) }
                    )

                    MoreSubScreen.BACKUP_SETTINGS -> BackupAndSettingsScreen(
                        uiState = uiState,
                        onBack = { viewModel.openMoreSubScreen(null) },
                        onReprocessAllSms = viewModel::reprocessAllSms,
                        onLoadSampleData = viewModel::loadSampleData,
                        onClearAll = viewModel::clearAllTransactions,
                        onExportCsv = onExportCsv,
                        onExportBackup = onExportEncryptedBackup,
                        onImportBackup = onImportEncryptedBackup,
                        onSetMonthStartDay = viewModel::setMonthStartDay,
                        onSetFinancialYear = viewModel::setFinancialYearMode,
                        onSetAutoTransferWindow = viewModel::setAutoTransferWindowMinutes,
                        onTogglePrivacyMode = viewModel::togglePrivacyMode,
                        onSetFlagSecure = viewModel::setFlagSecure,
                        onSetThemeMode = viewModel::setThemeMode,
                        onSetPin = viewModel::setPin,
                        onDeleteSenderRule = viewModel::deleteSenderRule
                    )
                }
            }
        }
    }

    val selectedTxn: TransactionEntity? = selectedTxnId?.let { id ->
        uiState.allTransactions.firstOrNull { it.id == id }
    }
    if (selectedTxn != null) {
        TransactionDetailSheet(
            transaction = selectedTxn,
            allTransactions = uiState.allTransactions,
            categories = uiState.categories,
            existingSplits = uiState.splitsByTxnId[selectedTxn.id].orEmpty(),
            privacyMode = uiState.preferences.privacyMode,
            onDismiss = { selectedTxnId = null },
            onUpdateCategorySingle = { catId ->
                viewModel.updateCategorySingle(selectedTxn.id, catId)
            },
            onAlwaysCategoriseMerchant = { merchant, catId ->
                viewModel.alwaysCategoriseMerchant(merchant, catId)
            },
            onOpenCustomCategoryRule = { txn, catId ->
                editingCategoryRule = Triple(null, txn.merchant, catId)
            },
            onExcludeSingle = { reason ->
                viewModel.excludeSingle(selectedTxn.id, reason)
            },
            onExcludeSimilar = { name, reason, merchantPat, bodyKw, exactAmt ->
                viewModel.excludeSimilar(name, reason, merchantPat, bodyKw, exactAmt)
            },
            onOpenCustomExclusionRule = { txn, reason ->
                editingExclusionRule = Triple(txn.merchant, "", reason)
            },
            onIncludeAgain = {
                viewModel.includeTransactionsAgain(setOf(selectedTxn.id))
            },
            onUpdateDetails = { merchant, amount, time, note, tags ->
                viewModel.updateTransactionDetails(selectedTxn.id, merchant, amount, time, note, tags)
            },
            onSaveSplits = { splits ->
                viewModel.saveSplits(selectedTxn.id, splits)
            },
            onDelete = {
                viewModel.deleteTransaction(selectedTxn.id)
                selectedTxnId = null
            }
        )
    }

    if (showManualDialog) {
        ManualTransactionDialog(
            categories = uiState.categories,
            accounts = uiState.activeAccounts,
            onDismiss = { showManualDialog = false },
            onSave = { merchant, amount, type, catId, accId, time, note, tags ->
                viewModel.addManualTransaction(merchant, amount, type, catId, accId, time, note, tags)
                showManualDialog = false
            }
        )
    }

    editingCategoryRule?.let { (rule, prefillMerchant, prefillCategoryId) ->
        CategoryRuleEditorDialog(
            initialRule = rule,
            prefillMerchant = prefillMerchant,
            prefillCategoryId = prefillCategoryId,
            categories = uiState.categories,
            onDismiss = { editingCategoryRule = null },
            onSave = { saved ->
                viewModel.saveCategoryRule(saved)
                editingCategoryRule = null
            }
        )
    }

    editingExclusionRule?.let { (merchant, keyword, reason) ->
        ExclusionRuleEditorDialog(
            initialMerchant = merchant,
            initialKeyword = keyword,
            initialReason = reason,
            onDismiss = { editingExclusionRule = null },
            onSave = { name, r, m, k, amt ->
                viewModel.excludeSimilar(name, r, m, k, amt)
                editingExclusionRule = null
            }
        )
    }

    templateBuilderSms?.let { sms ->
        TemplateBuilderDialog(
            initialSender = sms.sender,
            sampleBody = sms.body,
            onDismiss = { templateBuilderSms = null },
            onSave = { template ->
                viewModel.saveParseTemplateAndReprocess(template)
                templateBuilderSms = null
            }
        )
    }
}

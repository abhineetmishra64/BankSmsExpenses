package com.banksms.expenses

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.banksms.expenses.ui.ExpensesViewModel
import com.banksms.expenses.ui.MainScreen
import com.banksms.expenses.ui.theme.BankSmsExpensesTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ExpensesViewModel by viewModels {
        ExpensesViewModel.Factory((application as BankExpensesApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BankSmsExpensesTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var hasReadSmsPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_SMS
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { grants ->
                    val granted = grants[Manifest.permission.READ_SMS] == true
                    hasReadSmsPermission = granted
                    if (granted) {
                        viewModel.syncSmsInbox()
                    }
                }

                MainScreen(
                    uiState = uiState,
                    hasSmsPermission = hasReadSmsPermission,
                    onRequestSmsPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_SMS,
                                Manifest.permission.RECEIVE_SMS
                            )
                        )
                    },
                    onSyncSms = viewModel::syncSmsInbox,
                    onLoadSampleData = viewModel::loadSampleData,
                    onSelectBank = viewModel::selectBank,
                    onSelectTimeframe = viewModel::selectTimeframe,
                    onSelectTypeFilter = viewModel::selectTypeFilter,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onUpdateCategory = viewModel::updateTransactionCategory,
                    onDeleteTransaction = viewModel::deleteTransaction,
                    onClearAll = viewModel::clearAllTransactions,
                    onDismissStatusMessage = viewModel::clearStatusMessage
                )
            }
        }
    }
}

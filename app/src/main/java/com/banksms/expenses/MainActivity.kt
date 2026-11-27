package com.banksms.expenses

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.banksms.expenses.ui.ExpensesViewModel
import com.banksms.expenses.ui.MainScreen
import com.banksms.expenses.ui.theme.BankSmsExpensesTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ExpensesViewModel by viewModels {
        ExpensesViewModel.Factory((application as BankExpensesApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()

            LaunchedEffect(uiState.preferences.flagSecure) {
                if (uiState.preferences.flagSecure) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

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

            var pendingBackupPassword by remember { mutableStateOf<String?>(null) }

            val csvExportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("text/csv")
            ) { uri ->
                if (uri == null) return@rememberLauncherForActivityResult
                scope.launch {
                    val csv = viewModel.buildCsvExport()
                    contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                    viewModel.showMessage("Exported CSV")
                }
            }

            val rulesExportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                if (uri == null) return@rememberLauncherForActivityResult
                scope.launch {
                    val json = viewModel.buildRulesJsonExport()
                    contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                    viewModel.showMessage("Exported rules JSON")
                }
            }

            val rulesImportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri == null) return@rememberLauncherForActivityResult
                val json = contentResolver.openInputStream(uri)?.bufferedReader()?.readText().orEmpty()
                if (json.isNotBlank()) viewModel.importRulesJson(json)
            }

            val backupExportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/octet-stream")
            ) { uri ->
                val pw = pendingBackupPassword
                pendingBackupPassword = null
                if (uri == null || pw.isNullOrBlank()) return@rememberLauncherForActivityResult
                scope.launch {
                    val encrypted = viewModel.buildEncryptedBackup(pw)
                    contentResolver.openOutputStream(uri)?.use { it.write(encrypted.toByteArray(Charsets.UTF_8)) }
                    viewModel.showMessage("Saved encrypted backup")
                }
            }

            val backupImportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                val pw = pendingBackupPassword
                pendingBackupPassword = null
                if (uri == null || pw.isNullOrBlank()) return@rememberLauncherForActivityResult
                val payload = contentResolver.openInputStream(uri)?.bufferedReader()?.readText().orEmpty()
                if (payload.isNotBlank()) viewModel.importEncryptedBackup(payload, pw)
            }

            BankSmsExpensesTheme(themePreference = uiState.preferences.themeMode) {
                MainScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    hasSmsPermission = hasReadSmsPermission,
                    onRequestSmsPermission = {
                        val perms = buildList {
                            add(Manifest.permission.READ_SMS)
                            add(Manifest.permission.RECEIVE_SMS)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }.toTypedArray()
                        permissionLauncher.launch(perms)
                    },
                    onExportCsv = { csvExportLauncher.launch("sms_expenses.csv") },
                    onExportRulesJson = { rulesExportLauncher.launch("sms_expense_rules.json") },
                    onImportRulesJson = { rulesImportLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                    onExportEncryptedBackup = { pw ->
                        pendingBackupPassword = pw
                        backupExportLauncher.launch("sms_expenses_backup.bksms")
                    },
                    onImportEncryptedBackup = { pw ->
                        pendingBackupPassword = pw
                        backupImportLauncher.launch(arrayOf("*/*"))
                    }
                )
            }
        }
    }
}

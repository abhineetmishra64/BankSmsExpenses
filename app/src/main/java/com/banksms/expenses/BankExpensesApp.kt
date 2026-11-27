package com.banksms.expenses

import android.app.Application
import com.banksms.expenses.data.local.AppDatabase
import com.banksms.expenses.data.repository.ExpenseRepository
import com.banksms.expenses.data.sms.SmsReader

class BankExpensesApp : Application() {
    val repository: ExpenseRepository by lazy {
        val database = AppDatabase.getInstance(this)
        val smsReader = SmsReader(this)
        ExpenseRepository(
            transactionDao = database.transactionDao(),
            smsRawDao = database.smsRawDao(),
            accountDao = database.accountDao(),
            categoryDao = database.categoryDao(),
            rulesDao = database.rulesDao(),
            budgetDao = database.budgetDao(),
            settingsDao = database.settingsDao(),
            smsReader = smsReader
        )
    }
}

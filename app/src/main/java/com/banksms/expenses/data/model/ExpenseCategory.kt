package com.banksms.expenses.data.model

enum class ExpenseCategory(val label: String) {
    FOOD("Food & Dining"),
    SHOPPING("Shopping"),
    TRANSPORT("Transport & Fuel"),
    BILLS("Bills & Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health & Pharmacy"),
    TRANSFER("Transfers & UPI"),
    ATM("Cash & ATM"),
    OTHER("Other")
}

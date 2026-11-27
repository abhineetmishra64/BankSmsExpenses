package com.banksms.expenses.data.model

enum class ExpenseCategory(
    val id: Long,
    val label: String,
    val iconKey: String,
    val colorHex: Long
) {
    FOOD(1L, "Food & Dining", "restaurant", 0xFFEA580C),
    GROCERIES(2L, "Groceries", "shopping_cart", 0xFF16A34A),
    TRANSPORT(3L, "Transport & Fuel", "directions_car", 0xFF2563EB),
    TRAVEL(4L, "Travel", "flight", 0xFF0284C7),
    HOTELS(5L, "Hotels & Stays", "hotel", 0xFF7C3AED),
    SHOPPING(6L, "Shopping", "shopping_bag", 0xFFDB2777),
    BILLS(7L, "Bills & Utilities", "receipt_long", 0xFFD97706),
    RECHARGE(8L, "Recharge & Subscriptions", "subscriptions", 0xFF4F46E5),
    RENT(9L, "Rent", "home", 0xFF9333EA),
    EMI(10L, "EMI & Loans", "account_balance", 0xFFDC2626),
    INSURANCE(11L, "Insurance", "verified_user", 0xFF0D9488),
    HEALTH(12L, "Health", "local_hospital", 0xFFE11D48),
    EDUCATION(13L, "Education", "school", 0xFF0369A1),
    ENTERTAINMENT(14L, "Entertainment", "movie", 0xFFC026D3),
    ATM(15L, "Cash Withdrawal", "local_atm", 0xFF65A30D),
    FEES(16L, "Fees & Charges", "money_off", 0xFF64748B),
    SALARY(17L, "Salary/Income", "payments", 0xFF059669),
    INTEREST(18L, "Interest", "trending_up", 0xFF10B981),
    REFUND(19L, "Refund", "replay", 0xFF14B8A6),
    TRANSFER(20L, "Transfers (personal)", "swap_horiz", 0xFF6366F1),
    INVESTMENTS(21L, "Investments", "savings", 0xFF0F766E),
    OTHER(22L, "Other", "category", 0xFF475569),
    UNCATEGORISED(23L, "Uncategorised", "help_outline", 0xFF94A3B8);

    companion object {
        fun fromId(id: Long): ExpenseCategory? = entries.firstOrNull { it.id == id }

        fun fromLegacyName(name: String): ExpenseCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}

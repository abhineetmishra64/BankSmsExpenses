package com.banksms.expenses.data.model

enum class AccountType(val label: String) {
    SAVINGS("Savings"),
    CREDIT("Credit Card"),
    WALLET("Wallet"),
    CASH("Cash")
}

enum class TransactionChannel(val label: String) {
    UPI("UPI"),
    CARD_POS("Card"),
    ATM("ATM"),
    NEFT_IMPS("NEFT/IMPS"),
    NACH_ECS("NACH/ECS"),
    NET_BANKING("Net Banking"),
    OTHER("Other")
}

enum class CategorySource(val label: String) {
    MANUAL("Manual"),
    RULE("User Rule"),
    ALIAS("Merchant Alias"),
    BUILTIN("Built-in"),
    FALLBACK("Uncategorised")
}

enum class ExclusionReason(val label: String) {
    INVESTMENT("Investment / SIP"),
    SELF_TRANSFER("Self-transfer"),
    CARD_BILL_PAYMENT("Card bill payment"),
    LOAN_EMI_PRINCIPAL("Loan EMI principal"),
    REIMBURSABLE("Reimbursable"),
    REFUND_REVERSAL("Refund / Reversal"),
    OTHER("Other")
}

enum class ExclusionSource(val label: String) {
    NONE("Included"),
    MANUAL("Manual"),
    USER_RULE("Exclusion Rule"),
    AUTO_TRANSFER("Auto Transfer"),
    BUILTIN_SUGGESTION("Suggested Rule")
}

enum class ParseStatus {
    PARSED,
    UNPARSED,
    IGNORED,
    BALANCE_ONLY
}

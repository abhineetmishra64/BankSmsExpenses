package com.banksms.expenses.data.sms

import com.banksms.expenses.data.model.CategoryEntity
import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.CategorySource
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.MerchantAliasEntity
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionType
import java.util.Locale

data class CategorisationResult(
    val categoryId: Long,
    val categorySource: CategorySource,
    val resolvedMerchant: String,
    val matchedRuleId: Long? = null,
    val alsoExclude: Boolean = false,
    val exclusionReason: ExclusionReason? = null
)

object CategorisationEngine {

    fun defaultCategoryEntities(): List<CategoryEntity> {
        return ExpenseCategory.entries.map { cat ->
            CategoryEntity(
                id = cat.id,
                name = cat.label,
                iconKey = cat.iconKey,
                colorHex = cat.colorHex,
                parentId = null,
                isBuiltin = true
            )
        }
    }

    private val builtinMerchantAliases: List<Pair<String, String>> = listOf(
        "swiggy*" to "Swiggy",
        "swiggy/" to "Swiggy",
        "zomato*" to "Zomato",
        "zomato/" to "Zomato",
        "amzn" to "Amazon",
        "amazon.in" to "Amazon",
        "flipkart*" to "Flipkart",
        "uber*" to "Uber",
        "ola*" to "Ola",
        "blinkit*" to "Blinkit",
        "zepto*" to "Zepto",
        "bigbasket*" to "BigBasket",
        "instamart" to "Swiggy Instamart",
        "netflix*" to "Netflix",
        "spotify*" to "Spotify"
    )

    private val builtinKeywordDictionary: List<Pair<ExpenseCategory, List<String>>> = listOf(
        ExpenseCategory.REFUND to listOf(
            "refund", "reversal", "reversed", "cashback"
        ),
        ExpenseCategory.SALARY to listOf(
            "salary", "payroll", "stipend", "bonus"
        ),
        ExpenseCategory.INTEREST to listOf(
            "interest", "int.pd", "int credited"
        ),
        ExpenseCategory.INVESTMENTS to listOf(
            "mutual fund", "sip ", "sip-", "groww", "zerodha", "kuvera", "coin",
            "ppf", "nps", "epf", "demat", "folio", "fixed deposit", "ach d-"
        ),
        ExpenseCategory.GROCERIES to listOf(
            "blinkit", "zepto", "instamart", "bigbasket", "grocer", "supermarket",
            "dmart", "d-mart", "reliance fresh", "more retail", "natures basket"
        ),
        ExpenseCategory.FOOD to listOf(
            "swiggy", "zomato", "ubereats", "dominos", "pizza", "mcdonald", "kfc",
            "starbucks", "cafe", "coffee", "restaurant", "dining", "bakery", "food",
            "biryani", "burger", "chaayos"
        ),
        ExpenseCategory.HOTELS to listOf(
            "hotel", "resort", "oyo", "airbnb", "marriott", "taj ", "hyatt", "itc hotel", "stay"
        ),
        ExpenseCategory.TRAVEL to listOf(
            "irctc", "airline", "indigo", "airindia", "air india", "vistara", "spicejet",
            "akasa", "makemytrip", "goibibo", "cleartrip", "redbus", "flight", "train", "railway"
        ),
        ExpenseCategory.TRANSPORT to listOf(
            "uber", "ola", "rapido", "metro", "petrol", "diesel", "fuel", "hpcl",
            "bpcl", "iocl", "shell", "fastag", "toll", "parking", "cab", "auto"
        ),
        ExpenseCategory.RENT to listOf(
            "rent", "nobroker", "mygate", "landlord"
        ),
        ExpenseCategory.EMI to listOf(
            "emi", "loan", "bajaj finserv", "home credit", "repayment"
        ),
        ExpenseCategory.INSURANCE to listOf(
            "insurance", "lic", "policybazaar", "acko", "hdfc ergo", "icici lombard", "premium"
        ),
        ExpenseCategory.EDUCATION to listOf(
            "school", "college", "university", "tuition", "udemy", "coursera", "unacademy", "education"
        ),
        ExpenseCategory.RECHARGE to listOf(
            "recharge", "airtel", "jio", "vi ", "vodafone", "bsnl", "dth", "tataplay",
            "subscription", "prime video", "hotstar", "youtube", "icloud", "google one"
        ),
        ExpenseCategory.BILLS to listOf(
            "electricity", "water", "gas", "broadband", "wifi", "billdesk", "bbps",
            "bescom", "tata power", "adani", "maintenance", "utility"
        ),
        ExpenseCategory.SHOPPING to listOf(
            "amazon", "flipkart", "myntra", "ajio", "nykaa", "meesho", "tatacliq",
            "apple", "croma", "reliance", "decathlon", "ikea", "zara", "h&m", "mall", "store"
        ),
        ExpenseCategory.ENTERTAINMENT to listOf(
            "netflix", "spotify", "bookmyshow", "pvr", "inox", "cinema", "movie",
            "steam", "playstation", "xbox", "gaming"
        ),
        ExpenseCategory.HEALTH to listOf(
            "pharmacy", "apollo", "pharmeasy", "1mg", "netmeds", "hospital", "clinic",
            "diagnostic", "medplus", "dental", "doctor", "lab", "health"
        ),
        ExpenseCategory.ATM to listOf(
            "atm", "cash withdrawal", "withdrawn at"
        ),
        ExpenseCategory.FEES to listOf(
            "annual fee", "convenience fee", "late fee", "charges", "penalty", "markup", "gst"
        ),
        ExpenseCategory.TRANSFER to listOf(
            "upi", "imps", "neft", "rtgs", "@ok", "@ybl", "@ibl", "@axl", "@paytm", "vpa"
        )
    )

    fun normalizeBuiltinMerchant(rawMerchant: String): String {
        val lower = rawMerchant.lowercase(Locale.ROOT)
        for ((pattern, canonical) in builtinMerchantAliases) {
            if (lower.contains(pattern)) {
                return canonical
            }
        }
        return rawMerchant
    }

    fun classifyBuiltin(
        merchant: String,
        body: String,
        type: TransactionType
    ): Pair<ExpenseCategory, CategorySource> {
        val combined = "${merchant.lowercase(Locale.ROOT)} ${body.lowercase(Locale.ROOT)}"
        for ((category, keywords) in builtinKeywordDictionary) {
            if (category == ExpenseCategory.SALARY && type != TransactionType.CREDIT) continue
            if (category == ExpenseCategory.INTEREST && type != TransactionType.CREDIT) continue
            if (category == ExpenseCategory.REFUND && type != TransactionType.CREDIT) continue
            if (keywords.any { combined.contains(it) }) {
                return category to CategorySource.BUILTIN
            }
        }
        return ExpenseCategory.UNCATEGORISED to CategorySource.FALLBACK
    }

    fun categorise(
        transaction: TransactionEntity,
        userRules: List<CategoryRuleEntity>,
        userAliases: List<MerchantAliasEntity>,
        validCategoryIds: Set<Long>
    ): CategorisationResult {
        // 1. Manual per-transaction override always wins
        if (transaction.categorySource == CategorySource.MANUAL && transaction.categoryId in validCategoryIds) {
            return CategorisationResult(
                categoryId = transaction.categoryId,
                categorySource = CategorySource.MANUAL,
                resolvedMerchant = transaction.merchant
            )
        }

        // Resolve canonical merchant name via user aliases first, then built-in aliases
        val lowerRawMerchant = transaction.rawMerchant.lowercase(Locale.ROOT)
        val lowerCurrentMerchant = transaction.merchant.lowercase(Locale.ROOT)
        val matchedAlias = userAliases.firstOrNull { alias ->
            val pat = alias.rawPattern.lowercase(Locale.ROOT)
            pat.isNotBlank() && (lowerRawMerchant.contains(pat) || lowerCurrentMerchant.contains(pat))
        }
        val resolvedMerchant = matchedAlias?.canonicalName ?: normalizeBuiltinMerchant(transaction.rawMerchant)

        // 2. Evaluate ordered user rules (first match wins)
        val sortedRules = userRules.filter { it.enabled }.sortedWith(compareBy({ it.priority }, { it.id }))
        for (rule in sortedRules) {
            if (rule.categoryId !in validCategoryIds) continue
            if (matchesRule(rule, transaction, resolvedMerchant)) {
                return CategorisationResult(
                    categoryId = rule.categoryId,
                    categorySource = CategorySource.RULE,
                    resolvedMerchant = resolvedMerchant,
                    matchedRuleId = rule.id,
                    alsoExclude = rule.alsoExclude,
                    exclusionReason = rule.exclusionReason
                )
            }
        }

        // 3. Merchant alias default category
        if (matchedAlias?.defaultCategoryId != null && matchedAlias.defaultCategoryId in validCategoryIds) {
            return CategorisationResult(
                categoryId = matchedAlias.defaultCategoryId,
                categorySource = CategorySource.ALIAS,
                resolvedMerchant = resolvedMerchant
            )
        }

        // 4. Built-in keyword dictionary -> 5. Fallback Uncategorised
        val (builtinCat, source) = classifyBuiltin(resolvedMerchant, transaction.rawBody, transaction.type)
        val catId = if (builtinCat.id in validCategoryIds) builtinCat.id else ExpenseCategory.UNCATEGORISED.id
        return CategorisationResult(
            categoryId = catId,
            categorySource = source,
            resolvedMerchant = resolvedMerchant
        )
    }

    fun matchesRule(
        rule: CategoryRuleEntity,
        txn: TransactionEntity,
        resolvedMerchant: String = txn.merchant
    ): Boolean {
        var hasCondition = false

        val merchantPat = rule.merchantPattern?.trim()
        if (!merchantPat.isNullOrEmpty()) {
            hasCondition = true
            val matched = if (rule.merchantMatchMode.equals("EQUALS", ignoreCase = true)) {
                resolvedMerchant.equals(merchantPat, ignoreCase = true) ||
                    txn.rawMerchant.equals(merchantPat, ignoreCase = true)
            } else {
                resolvedMerchant.contains(merchantPat, ignoreCase = true) ||
                    txn.rawMerchant.contains(merchantPat, ignoreCase = true)
            }
            if (!matched) return false
        }

        val bodyKw = rule.bodyKeyword?.trim()
        if (!bodyKw.isNullOrEmpty()) {
            hasCondition = true
            if (!txn.rawBody.contains(bodyKw, ignoreCase = true)) return false
        }

        val senderPat = rule.senderPattern?.trim()
        if (!senderPat.isNullOrEmpty()) {
            hasCondition = true
            if (!txn.sender.contains(senderPat, ignoreCase = true)) return false
        }

        val vpaPat = rule.vpaPattern?.trim()
        if (!vpaPat.isNullOrEmpty()) {
            hasCondition = true
            val inMerchant = txn.rawMerchant.contains(vpaPat, ignoreCase = true)
            val inBody = txn.rawBody.contains(vpaPat, ignoreCase = true)
            if (!inMerchant && !inBody) return false
        }

        if (rule.minAmount != null) {
            hasCondition = true
            if (txn.amount < rule.minAmount) return false
        }
        if (rule.maxAmount != null) {
            hasCondition = true
            if (txn.amount > rule.maxAmount) return false
        }

        if (rule.accountId != null) {
            hasCondition = true
            if (txn.accountId != rule.accountId) return false
        }

        if (rule.direction != null) {
            hasCondition = true
            if (txn.type != rule.direction) return false
        }

        val regexPat = rule.regexPattern?.trim()
        if (!regexPat.isNullOrEmpty()) {
            hasCondition = true
            val regex = runCatching { Regex(regexPat, RegexOption.IGNORE_CASE) }.getOrNull() ?: return false
            if (!regex.containsMatchIn(txn.rawBody) && !regex.containsMatchIn(resolvedMerchant)) return false
        }

        return hasCondition
    }
}

package com.banksms.expenses.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.banksms.expenses.data.model.AccountEntity
import com.banksms.expenses.data.model.AccountType
import com.banksms.expenses.data.model.AppSettingEntity
import com.banksms.expenses.data.model.BudgetEntity
import com.banksms.expenses.data.model.CategoryEntity
import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.CategorySource
import com.banksms.expenses.data.model.DismissedExclusionSuggestionEntity
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ExclusionRuleEntity
import com.banksms.expenses.data.model.ExclusionSource
import com.banksms.expenses.data.model.MerchantAliasEntity
import com.banksms.expenses.data.model.ParseStatus
import com.banksms.expenses.data.model.ParseTemplateEntity
import com.banksms.expenses.data.model.SenderRuleEntity
import com.banksms.expenses.data.model.SmsRawEntity
import com.banksms.expenses.data.model.TransactionChannel
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionSplitEntity
import com.banksms.expenses.data.model.TransactionType

class RoomConverters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromAccountType(type: AccountType): String = type.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun fromTransactionChannel(channel: TransactionChannel): String = channel.name

    @TypeConverter
    fun toTransactionChannel(value: String): TransactionChannel = TransactionChannel.valueOf(value)

    @TypeConverter
    fun fromCategorySource(source: CategorySource): String = source.name

    @TypeConverter
    fun toCategorySource(value: String): CategorySource = CategorySource.valueOf(value)

    @TypeConverter
    fun fromExclusionReason(reason: ExclusionReason?): String? = reason?.name

    @TypeConverter
    fun toExclusionReason(value: String?): ExclusionReason? = value?.let { ExclusionReason.valueOf(it) }

    @TypeConverter
    fun fromExclusionSource(source: ExclusionSource): String = source.name

    @TypeConverter
    fun toExclusionSource(value: String): ExclusionSource = ExclusionSource.valueOf(value)

    @TypeConverter
    fun fromParseStatus(status: ParseStatus): String = status.name

    @TypeConverter
    fun toParseStatus(value: String): ParseStatus = ParseStatus.valueOf(value)
}

@Database(
    entities = [
        TransactionEntity::class,
        SmsRawEntity::class,
        AccountEntity::class,
        CategoryEntity::class,
        MerchantAliasEntity::class,
        CategoryRuleEntity::class,
        ExclusionRuleEntity::class,
        DismissedExclusionSuggestionEntity::class,
        BudgetEntity::class,
        TransactionSplitEntity::class,
        ParseTemplateEntity::class,
        SenderRuleEntity::class,
        AppSettingEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun smsRawDao(): SmsRawDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun rulesDao(): RulesDao
    abstract fun budgetDao(): BudgetDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sms_raw` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sender` TEXT NOT NULL,
                        `body` TEXT NOT NULL,
                        `receivedAt` INTEGER NOT NULL,
                        `hash` TEXT NOT NULL,
                        `parseStatus` TEXT NOT NULL,
                        `confidence` REAL NOT NULL,
                        `failureReason` TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sms_raw_hash` ON `sms_raw` (`hash`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sms_raw_parseStatus` ON `sms_raw` (`parseStatus`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sms_raw_receivedAt` ON `sms_raw` (`receivedAt`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `accounts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `bankName` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `last4` TEXT,
                        `lastBalance` REAL,
                        `creditLimit` REAL,
                        `statementDueDate` TEXT,
                        `accentColorHex` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `mergedIntoAccountId` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_accounts_bankName_last4_type` ON `accounts` (`bankName`, `last4`, `type`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `iconKey` TEXT NOT NULL,
                        `colorHex` INTEGER NOT NULL,
                        `parentId` INTEGER,
                        `isBuiltin` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `merchant_aliases` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `rawPattern` TEXT NOT NULL,
                        `canonicalName` TEXT NOT NULL,
                        `defaultCategoryId` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_merchant_aliases_rawPattern` ON `merchant_aliases` (`rawPattern`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `category_rules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `priority` INTEGER NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `merchantPattern` TEXT,
                        `merchantMatchMode` TEXT NOT NULL,
                        `bodyKeyword` TEXT,
                        `senderPattern` TEXT,
                        `vpaPattern` TEXT,
                        `minAmount` REAL,
                        `maxAmount` REAL,
                        `accountId` INTEGER,
                        `direction` TEXT,
                        `regexPattern` TEXT,
                        `alsoExclude` INTEGER NOT NULL,
                        `exclusionReason` TEXT,
                        `enabled` INTEGER NOT NULL,
                        `matchCount` INTEGER NOT NULL,
                        `lastMatchedAt` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_category_rules_priority` ON `category_rules` (`priority`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `exclusion_rules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `merchantPattern` TEXT,
                        `bodyKeyword` TEXT,
                        `regexPattern` TEXT,
                        `exactAmount` REAL,
                        `amountTolerance` REAL NOT NULL,
                        `accountId` INTEGER,
                        `reason` TEXT NOT NULL,
                        `enabled` INTEGER NOT NULL,
                        `applyRetroactively` INTEGER NOT NULL,
                        `matchCount` INTEGER NOT NULL,
                        `lastMatchedAt` INTEGER
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `dismissed_exclusion_suggestions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `suggestionKey` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_dismissed_exclusion_suggestions_suggestionKey` ON `dismissed_exclusion_suggestions` (`suggestionKey`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `budgets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `categoryId` INTEGER,
                        `amount` REAL NOT NULL,
                        `period` TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `splits` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `transactionId` INTEGER NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `amount` REAL NOT NULL,
                        `note` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_splits_transactionId` ON `splits` (`transactionId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `parse_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `senderPattern` TEXT NOT NULL,
                        `regexPattern` TEXT NOT NULL,
                        `amountGroup` INTEGER NOT NULL,
                        `merchantGroup` INTEGER,
                        `accountGroup` INTEGER,
                        `balanceGroup` INTEGER,
                        `defaultDirection` TEXT,
                        `isBuiltin` INTEGER NOT NULL,
                        `enabled` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sender_rules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `senderCode` TEXT NOT NULL,
                        `action` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sender_rules_senderCode` ON `sender_rules` (`senderCode`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `settings` (
                        `key` TEXT NOT NULL PRIMARY KEY,
                        `value` TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL("ALTER TABLE `transactions` RENAME TO `transactions_old`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `smsId` INTEGER,
                        `accountId` INTEGER,
                        `sender` TEXT NOT NULL,
                        `bankName` TEXT NOT NULL,
                        `accountLast4` TEXT,
                        `amount` REAL NOT NULL,
                        `currency` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `merchant` TEXT NOT NULL,
                        `rawMerchant` TEXT NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `categorySource` TEXT NOT NULL,
                        `channel` TEXT NOT NULL,
                        `refNo` TEXT,
                        `balanceAfter` REAL,
                        `creditLimitAfter` REAL,
                        `txnTime` INTEGER NOT NULL,
                        `rawBody` TEXT NOT NULL,
                        `fingerprint` TEXT NOT NULL,
                        `isExcluded` INTEGER NOT NULL,
                        `exclusionReason` TEXT,
                        `exclusionSource` TEXT NOT NULL,
                        `exclusionRuleId` INTEGER,
                        `matchedTransferId` INTEGER,
                        `note` TEXT NOT NULL,
                        `tags` TEXT NOT NULL,
                        `isManual` INTEGER NOT NULL,
                        `isInternational` INTEGER NOT NULL,
                        `linkedOriginalTxnId` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `transactions` (
                        `id`, `smsId`, `accountId`, `sender`, `bankName`, `accountLast4`,
                        `amount`, `currency`, `type`, `merchant`, `rawMerchant`,
                        `categoryId`, `categorySource`, `channel`, `refNo`,
                        `balanceAfter`, `creditLimitAfter`, `txnTime`, `rawBody`, `fingerprint`,
                        `isExcluded`, `exclusionReason`, `exclusionSource`, `exclusionRuleId`,
                        `matchedTransferId`, `note`, `tags`, `isManual`, `isInternational`, `linkedOriginalTxnId`
                    )
                    SELECT
                        `id`, `smsId`, NULL, `sender`, `bankName`, `accountLast4`,
                        `amount`, `currency`, `type`, `merchant`, `merchant`,
                        CASE `category`
                            WHEN 'FOOD' THEN 1
                            WHEN 'TRANSPORT' THEN 3
                            WHEN 'SHOPPING' THEN 6
                            WHEN 'BILLS' THEN 7
                            WHEN 'HEALTH' THEN 12
                            WHEN 'ENTERTAINMENT' THEN 14
                            WHEN 'ATM' THEN 15
                            WHEN 'TRANSFER' THEN 20
                            ELSE 22
                        END,
                        'BUILTIN', 'OTHER', NULL,
                        `balanceAfter`, NULL, `timestamp`, `rawBody`, `fingerprint`,
                        0, NULL, 'NONE', NULL,
                        NULL, '', '', 0, 0, NULL
                    FROM `transactions_old`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `transactions_old`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_fingerprint` ON `transactions` (`fingerprint`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_bankName` ON `transactions` (`bankName`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_txnTime` ON `transactions` (`txnTime`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_accountId_txnTime` ON `transactions` (`accountId`, `txnTime`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId_txnTime` ON `transactions` (`categoryId`, `txnTime`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_isExcluded` ON `transactions` (`isExcluded`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bank_sms_expenses.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instance = it }
            }
        }
    }
}

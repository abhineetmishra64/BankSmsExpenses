package com.banksms.expenses.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile TransactionDao _transactionDao;

  private volatile SmsRawDao _smsRawDao;

  private volatile AccountDao _accountDao;

  private volatile CategoryDao _categoryDao;

  private volatile RulesDao _rulesDao;

  private volatile BudgetDao _budgetDao;

  private volatile SettingsDao _settingsDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `smsId` INTEGER, `accountId` INTEGER, `sender` TEXT NOT NULL, `bankName` TEXT NOT NULL, `accountLast4` TEXT, `amount` REAL NOT NULL, `currency` TEXT NOT NULL, `type` TEXT NOT NULL, `merchant` TEXT NOT NULL, `rawMerchant` TEXT NOT NULL, `categoryId` INTEGER NOT NULL, `categorySource` TEXT NOT NULL, `channel` TEXT NOT NULL, `refNo` TEXT, `balanceAfter` REAL, `creditLimitAfter` REAL, `txnTime` INTEGER NOT NULL, `rawBody` TEXT NOT NULL, `fingerprint` TEXT NOT NULL, `isExcluded` INTEGER NOT NULL, `exclusionReason` TEXT, `exclusionSource` TEXT NOT NULL, `exclusionRuleId` INTEGER, `matchedTransferId` INTEGER, `note` TEXT NOT NULL, `tags` TEXT NOT NULL, `isManual` INTEGER NOT NULL, `isInternational` INTEGER NOT NULL, `linkedOriginalTxnId` INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_fingerprint` ON `transactions` (`fingerprint`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_bankName` ON `transactions` (`bankName`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_txnTime` ON `transactions` (`txnTime`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_accountId_txnTime` ON `transactions` (`accountId`, `txnTime`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId_txnTime` ON `transactions` (`categoryId`, `txnTime`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_isExcluded` ON `transactions` (`isExcluded`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sms_raw` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sender` TEXT NOT NULL, `body` TEXT NOT NULL, `receivedAt` INTEGER NOT NULL, `hash` TEXT NOT NULL, `parseStatus` TEXT NOT NULL, `confidence` REAL NOT NULL, `failureReason` TEXT)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sms_raw_hash` ON `sms_raw` (`hash`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sms_raw_parseStatus` ON `sms_raw` (`parseStatus`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sms_raw_receivedAt` ON `sms_raw` (`receivedAt`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `displayName` TEXT NOT NULL, `bankName` TEXT NOT NULL, `type` TEXT NOT NULL, `last4` TEXT, `lastBalance` REAL, `creditLimit` REAL, `statementDueDate` TEXT, `accentColorHex` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `mergedIntoAccountId` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_accounts_bankName_last4_type` ON `accounts` (`bankName`, `last4`, `type`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `iconKey` TEXT NOT NULL, `colorHex` INTEGER NOT NULL, `parentId` INTEGER, `isBuiltin` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `merchant_aliases` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `rawPattern` TEXT NOT NULL, `canonicalName` TEXT NOT NULL, `defaultCategoryId` INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_merchant_aliases_rawPattern` ON `merchant_aliases` (`rawPattern`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `category_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `priority` INTEGER NOT NULL, `categoryId` INTEGER NOT NULL, `merchantPattern` TEXT, `merchantMatchMode` TEXT NOT NULL, `bodyKeyword` TEXT, `senderPattern` TEXT, `vpaPattern` TEXT, `minAmount` REAL, `maxAmount` REAL, `accountId` INTEGER, `direction` TEXT, `regexPattern` TEXT, `alsoExclude` INTEGER NOT NULL, `exclusionReason` TEXT, `enabled` INTEGER NOT NULL, `matchCount` INTEGER NOT NULL, `lastMatchedAt` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_category_rules_priority` ON `category_rules` (`priority`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `exclusion_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `merchantPattern` TEXT, `bodyKeyword` TEXT, `regexPattern` TEXT, `exactAmount` REAL, `amountTolerance` REAL NOT NULL, `accountId` INTEGER, `reason` TEXT NOT NULL, `enabled` INTEGER NOT NULL, `applyRetroactively` INTEGER NOT NULL, `matchCount` INTEGER NOT NULL, `lastMatchedAt` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `dismissed_exclusion_suggestions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `suggestionKey` TEXT NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_dismissed_exclusion_suggestions_suggestionKey` ON `dismissed_exclusion_suggestions` (`suggestionKey`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `categoryId` INTEGER, `amount` REAL NOT NULL, `period` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `splits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `transactionId` INTEGER NOT NULL, `categoryId` INTEGER NOT NULL, `amount` REAL NOT NULL, `note` TEXT NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_splits_transactionId` ON `splits` (`transactionId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `parse_templates` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `senderPattern` TEXT NOT NULL, `regexPattern` TEXT NOT NULL, `amountGroup` INTEGER NOT NULL, `merchantGroup` INTEGER, `accountGroup` INTEGER, `balanceGroup` INTEGER, `defaultDirection` TEXT, `isBuiltin` INTEGER NOT NULL, `enabled` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sender_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `senderCode` TEXT NOT NULL, `action` TEXT NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sender_rules_senderCode` ON `sender_rules` (`senderCode`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `settings` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1e115a89e51af5769f5594dd362802d3')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `transactions`");
        db.execSQL("DROP TABLE IF EXISTS `sms_raw`");
        db.execSQL("DROP TABLE IF EXISTS `accounts`");
        db.execSQL("DROP TABLE IF EXISTS `categories`");
        db.execSQL("DROP TABLE IF EXISTS `merchant_aliases`");
        db.execSQL("DROP TABLE IF EXISTS `category_rules`");
        db.execSQL("DROP TABLE IF EXISTS `exclusion_rules`");
        db.execSQL("DROP TABLE IF EXISTS `dismissed_exclusion_suggestions`");
        db.execSQL("DROP TABLE IF EXISTS `budgets`");
        db.execSQL("DROP TABLE IF EXISTS `splits`");
        db.execSQL("DROP TABLE IF EXISTS `parse_templates`");
        db.execSQL("DROP TABLE IF EXISTS `sender_rules`");
        db.execSQL("DROP TABLE IF EXISTS `settings`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTransactions = new HashMap<String, TableInfo.Column>(30);
        _columnsTransactions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("smsId", new TableInfo.Column("smsId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("accountId", new TableInfo.Column("accountId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("sender", new TableInfo.Column("sender", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("bankName", new TableInfo.Column("bankName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("accountLast4", new TableInfo.Column("accountLast4", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("currency", new TableInfo.Column("currency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("merchant", new TableInfo.Column("merchant", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("rawMerchant", new TableInfo.Column("rawMerchant", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("categoryId", new TableInfo.Column("categoryId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("categorySource", new TableInfo.Column("categorySource", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("channel", new TableInfo.Column("channel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("refNo", new TableInfo.Column("refNo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("balanceAfter", new TableInfo.Column("balanceAfter", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("creditLimitAfter", new TableInfo.Column("creditLimitAfter", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("txnTime", new TableInfo.Column("txnTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("rawBody", new TableInfo.Column("rawBody", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("fingerprint", new TableInfo.Column("fingerprint", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("isExcluded", new TableInfo.Column("isExcluded", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("exclusionReason", new TableInfo.Column("exclusionReason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("exclusionSource", new TableInfo.Column("exclusionSource", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("exclusionRuleId", new TableInfo.Column("exclusionRuleId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("matchedTransferId", new TableInfo.Column("matchedTransferId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("isManual", new TableInfo.Column("isManual", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("isInternational", new TableInfo.Column("isInternational", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("linkedOriginalTxnId", new TableInfo.Column("linkedOriginalTxnId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTransactions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTransactions = new HashSet<TableInfo.Index>(6);
        _indicesTransactions.add(new TableInfo.Index("index_transactions_fingerprint", true, Arrays.asList("fingerprint"), Arrays.asList("ASC")));
        _indicesTransactions.add(new TableInfo.Index("index_transactions_bankName", false, Arrays.asList("bankName"), Arrays.asList("ASC")));
        _indicesTransactions.add(new TableInfo.Index("index_transactions_txnTime", false, Arrays.asList("txnTime"), Arrays.asList("ASC")));
        _indicesTransactions.add(new TableInfo.Index("index_transactions_accountId_txnTime", false, Arrays.asList("accountId", "txnTime"), Arrays.asList("ASC", "ASC")));
        _indicesTransactions.add(new TableInfo.Index("index_transactions_categoryId_txnTime", false, Arrays.asList("categoryId", "txnTime"), Arrays.asList("ASC", "ASC")));
        _indicesTransactions.add(new TableInfo.Index("index_transactions_isExcluded", false, Arrays.asList("isExcluded"), Arrays.asList("ASC")));
        final TableInfo _infoTransactions = new TableInfo("transactions", _columnsTransactions, _foreignKeysTransactions, _indicesTransactions);
        final TableInfo _existingTransactions = TableInfo.read(db, "transactions");
        if (!_infoTransactions.equals(_existingTransactions)) {
          return new RoomOpenHelper.ValidationResult(false, "transactions(com.banksms.expenses.data.model.TransactionEntity).\n"
                  + " Expected:\n" + _infoTransactions + "\n"
                  + " Found:\n" + _existingTransactions);
        }
        final HashMap<String, TableInfo.Column> _columnsSmsRaw = new HashMap<String, TableInfo.Column>(8);
        _columnsSmsRaw.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSmsRaw.put("sender", new TableInfo.Column("sender", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSmsRaw.put("body", new TableInfo.Column("body", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSmsRaw.put("receivedAt", new TableInfo.Column("receivedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSmsRaw.put("hash", new TableInfo.Column("hash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSmsRaw.put("parseStatus", new TableInfo.Column("parseStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSmsRaw.put("confidence", new TableInfo.Column("confidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSmsRaw.put("failureReason", new TableInfo.Column("failureReason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSmsRaw = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSmsRaw = new HashSet<TableInfo.Index>(3);
        _indicesSmsRaw.add(new TableInfo.Index("index_sms_raw_hash", true, Arrays.asList("hash"), Arrays.asList("ASC")));
        _indicesSmsRaw.add(new TableInfo.Index("index_sms_raw_parseStatus", false, Arrays.asList("parseStatus"), Arrays.asList("ASC")));
        _indicesSmsRaw.add(new TableInfo.Index("index_sms_raw_receivedAt", false, Arrays.asList("receivedAt"), Arrays.asList("ASC")));
        final TableInfo _infoSmsRaw = new TableInfo("sms_raw", _columnsSmsRaw, _foreignKeysSmsRaw, _indicesSmsRaw);
        final TableInfo _existingSmsRaw = TableInfo.read(db, "sms_raw");
        if (!_infoSmsRaw.equals(_existingSmsRaw)) {
          return new RoomOpenHelper.ValidationResult(false, "sms_raw(com.banksms.expenses.data.model.SmsRawEntity).\n"
                  + " Expected:\n" + _infoSmsRaw + "\n"
                  + " Found:\n" + _existingSmsRaw);
        }
        final HashMap<String, TableInfo.Column> _columnsAccounts = new HashMap<String, TableInfo.Column>(11);
        _columnsAccounts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("bankName", new TableInfo.Column("bankName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("last4", new TableInfo.Column("last4", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("lastBalance", new TableInfo.Column("lastBalance", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("creditLimit", new TableInfo.Column("creditLimit", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("statementDueDate", new TableInfo.Column("statementDueDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("accentColorHex", new TableInfo.Column("accentColorHex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("mergedIntoAccountId", new TableInfo.Column("mergedIntoAccountId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAccounts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAccounts = new HashSet<TableInfo.Index>(1);
        _indicesAccounts.add(new TableInfo.Index("index_accounts_bankName_last4_type", false, Arrays.asList("bankName", "last4", "type"), Arrays.asList("ASC", "ASC", "ASC")));
        final TableInfo _infoAccounts = new TableInfo("accounts", _columnsAccounts, _foreignKeysAccounts, _indicesAccounts);
        final TableInfo _existingAccounts = TableInfo.read(db, "accounts");
        if (!_infoAccounts.equals(_existingAccounts)) {
          return new RoomOpenHelper.ValidationResult(false, "accounts(com.banksms.expenses.data.model.AccountEntity).\n"
                  + " Expected:\n" + _infoAccounts + "\n"
                  + " Found:\n" + _existingAccounts);
        }
        final HashMap<String, TableInfo.Column> _columnsCategories = new HashMap<String, TableInfo.Column>(6);
        _columnsCategories.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("iconKey", new TableInfo.Column("iconKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("colorHex", new TableInfo.Column("colorHex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("parentId", new TableInfo.Column("parentId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("isBuiltin", new TableInfo.Column("isBuiltin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategories = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCategories = new TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories);
        final TableInfo _existingCategories = TableInfo.read(db, "categories");
        if (!_infoCategories.equals(_existingCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "categories(com.banksms.expenses.data.model.CategoryEntity).\n"
                  + " Expected:\n" + _infoCategories + "\n"
                  + " Found:\n" + _existingCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsMerchantAliases = new HashMap<String, TableInfo.Column>(4);
        _columnsMerchantAliases.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMerchantAliases.put("rawPattern", new TableInfo.Column("rawPattern", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMerchantAliases.put("canonicalName", new TableInfo.Column("canonicalName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMerchantAliases.put("defaultCategoryId", new TableInfo.Column("defaultCategoryId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMerchantAliases = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMerchantAliases = new HashSet<TableInfo.Index>(1);
        _indicesMerchantAliases.add(new TableInfo.Index("index_merchant_aliases_rawPattern", true, Arrays.asList("rawPattern"), Arrays.asList("ASC")));
        final TableInfo _infoMerchantAliases = new TableInfo("merchant_aliases", _columnsMerchantAliases, _foreignKeysMerchantAliases, _indicesMerchantAliases);
        final TableInfo _existingMerchantAliases = TableInfo.read(db, "merchant_aliases");
        if (!_infoMerchantAliases.equals(_existingMerchantAliases)) {
          return new RoomOpenHelper.ValidationResult(false, "merchant_aliases(com.banksms.expenses.data.model.MerchantAliasEntity).\n"
                  + " Expected:\n" + _infoMerchantAliases + "\n"
                  + " Found:\n" + _existingMerchantAliases);
        }
        final HashMap<String, TableInfo.Column> _columnsCategoryRules = new HashMap<String, TableInfo.Column>(19);
        _columnsCategoryRules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("priority", new TableInfo.Column("priority", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("categoryId", new TableInfo.Column("categoryId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("merchantPattern", new TableInfo.Column("merchantPattern", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("merchantMatchMode", new TableInfo.Column("merchantMatchMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("bodyKeyword", new TableInfo.Column("bodyKeyword", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("senderPattern", new TableInfo.Column("senderPattern", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("vpaPattern", new TableInfo.Column("vpaPattern", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("minAmount", new TableInfo.Column("minAmount", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("maxAmount", new TableInfo.Column("maxAmount", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("accountId", new TableInfo.Column("accountId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("direction", new TableInfo.Column("direction", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("regexPattern", new TableInfo.Column("regexPattern", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("alsoExclude", new TableInfo.Column("alsoExclude", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("exclusionReason", new TableInfo.Column("exclusionReason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("enabled", new TableInfo.Column("enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("matchCount", new TableInfo.Column("matchCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategoryRules.put("lastMatchedAt", new TableInfo.Column("lastMatchedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategoryRules = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategoryRules = new HashSet<TableInfo.Index>(1);
        _indicesCategoryRules.add(new TableInfo.Index("index_category_rules_priority", false, Arrays.asList("priority"), Arrays.asList("ASC")));
        final TableInfo _infoCategoryRules = new TableInfo("category_rules", _columnsCategoryRules, _foreignKeysCategoryRules, _indicesCategoryRules);
        final TableInfo _existingCategoryRules = TableInfo.read(db, "category_rules");
        if (!_infoCategoryRules.equals(_existingCategoryRules)) {
          return new RoomOpenHelper.ValidationResult(false, "category_rules(com.banksms.expenses.data.model.CategoryRuleEntity).\n"
                  + " Expected:\n" + _infoCategoryRules + "\n"
                  + " Found:\n" + _existingCategoryRules);
        }
        final HashMap<String, TableInfo.Column> _columnsExclusionRules = new HashMap<String, TableInfo.Column>(13);
        _columnsExclusionRules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("merchantPattern", new TableInfo.Column("merchantPattern", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("bodyKeyword", new TableInfo.Column("bodyKeyword", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("regexPattern", new TableInfo.Column("regexPattern", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("exactAmount", new TableInfo.Column("exactAmount", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("amountTolerance", new TableInfo.Column("amountTolerance", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("accountId", new TableInfo.Column("accountId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("reason", new TableInfo.Column("reason", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("enabled", new TableInfo.Column("enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("applyRetroactively", new TableInfo.Column("applyRetroactively", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("matchCount", new TableInfo.Column("matchCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExclusionRules.put("lastMatchedAt", new TableInfo.Column("lastMatchedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExclusionRules = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesExclusionRules = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExclusionRules = new TableInfo("exclusion_rules", _columnsExclusionRules, _foreignKeysExclusionRules, _indicesExclusionRules);
        final TableInfo _existingExclusionRules = TableInfo.read(db, "exclusion_rules");
        if (!_infoExclusionRules.equals(_existingExclusionRules)) {
          return new RoomOpenHelper.ValidationResult(false, "exclusion_rules(com.banksms.expenses.data.model.ExclusionRuleEntity).\n"
                  + " Expected:\n" + _infoExclusionRules + "\n"
                  + " Found:\n" + _existingExclusionRules);
        }
        final HashMap<String, TableInfo.Column> _columnsDismissedExclusionSuggestions = new HashMap<String, TableInfo.Column>(2);
        _columnsDismissedExclusionSuggestions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDismissedExclusionSuggestions.put("suggestionKey", new TableInfo.Column("suggestionKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDismissedExclusionSuggestions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDismissedExclusionSuggestions = new HashSet<TableInfo.Index>(1);
        _indicesDismissedExclusionSuggestions.add(new TableInfo.Index("index_dismissed_exclusion_suggestions_suggestionKey", true, Arrays.asList("suggestionKey"), Arrays.asList("ASC")));
        final TableInfo _infoDismissedExclusionSuggestions = new TableInfo("dismissed_exclusion_suggestions", _columnsDismissedExclusionSuggestions, _foreignKeysDismissedExclusionSuggestions, _indicesDismissedExclusionSuggestions);
        final TableInfo _existingDismissedExclusionSuggestions = TableInfo.read(db, "dismissed_exclusion_suggestions");
        if (!_infoDismissedExclusionSuggestions.equals(_existingDismissedExclusionSuggestions)) {
          return new RoomOpenHelper.ValidationResult(false, "dismissed_exclusion_suggestions(com.banksms.expenses.data.model.DismissedExclusionSuggestionEntity).\n"
                  + " Expected:\n" + _infoDismissedExclusionSuggestions + "\n"
                  + " Found:\n" + _existingDismissedExclusionSuggestions);
        }
        final HashMap<String, TableInfo.Column> _columnsBudgets = new HashMap<String, TableInfo.Column>(4);
        _columnsBudgets.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgets.put("categoryId", new TableInfo.Column("categoryId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgets.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBudgets.put("period", new TableInfo.Column("period", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBudgets = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBudgets = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBudgets = new TableInfo("budgets", _columnsBudgets, _foreignKeysBudgets, _indicesBudgets);
        final TableInfo _existingBudgets = TableInfo.read(db, "budgets");
        if (!_infoBudgets.equals(_existingBudgets)) {
          return new RoomOpenHelper.ValidationResult(false, "budgets(com.banksms.expenses.data.model.BudgetEntity).\n"
                  + " Expected:\n" + _infoBudgets + "\n"
                  + " Found:\n" + _existingBudgets);
        }
        final HashMap<String, TableInfo.Column> _columnsSplits = new HashMap<String, TableInfo.Column>(5);
        _columnsSplits.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSplits.put("transactionId", new TableInfo.Column("transactionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSplits.put("categoryId", new TableInfo.Column("categoryId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSplits.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSplits.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSplits = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSplits = new HashSet<TableInfo.Index>(1);
        _indicesSplits.add(new TableInfo.Index("index_splits_transactionId", false, Arrays.asList("transactionId"), Arrays.asList("ASC")));
        final TableInfo _infoSplits = new TableInfo("splits", _columnsSplits, _foreignKeysSplits, _indicesSplits);
        final TableInfo _existingSplits = TableInfo.read(db, "splits");
        if (!_infoSplits.equals(_existingSplits)) {
          return new RoomOpenHelper.ValidationResult(false, "splits(com.banksms.expenses.data.model.TransactionSplitEntity).\n"
                  + " Expected:\n" + _infoSplits + "\n"
                  + " Found:\n" + _existingSplits);
        }
        final HashMap<String, TableInfo.Column> _columnsParseTemplates = new HashMap<String, TableInfo.Column>(11);
        _columnsParseTemplates.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParseTemplates.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParseTemplates.put("senderPattern", new TableInfo.Column("senderPattern", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParseTemplates.put("regexPattern", new TableInfo.Column("regexPattern", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParseTemplates.put("amountGroup", new TableInfo.Column("amountGroup", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParseTemplates.put("merchantGroup", new TableInfo.Column("merchantGroup", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParseTemplates.put("accountGroup", new TableInfo.Column("accountGroup", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParseTemplates.put("balanceGroup", new TableInfo.Column("balanceGroup", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParseTemplates.put("defaultDirection", new TableInfo.Column("defaultDirection", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParseTemplates.put("isBuiltin", new TableInfo.Column("isBuiltin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParseTemplates.put("enabled", new TableInfo.Column("enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysParseTemplates = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesParseTemplates = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoParseTemplates = new TableInfo("parse_templates", _columnsParseTemplates, _foreignKeysParseTemplates, _indicesParseTemplates);
        final TableInfo _existingParseTemplates = TableInfo.read(db, "parse_templates");
        if (!_infoParseTemplates.equals(_existingParseTemplates)) {
          return new RoomOpenHelper.ValidationResult(false, "parse_templates(com.banksms.expenses.data.model.ParseTemplateEntity).\n"
                  + " Expected:\n" + _infoParseTemplates + "\n"
                  + " Found:\n" + _existingParseTemplates);
        }
        final HashMap<String, TableInfo.Column> _columnsSenderRules = new HashMap<String, TableInfo.Column>(3);
        _columnsSenderRules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSenderRules.put("senderCode", new TableInfo.Column("senderCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSenderRules.put("action", new TableInfo.Column("action", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSenderRules = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSenderRules = new HashSet<TableInfo.Index>(1);
        _indicesSenderRules.add(new TableInfo.Index("index_sender_rules_senderCode", true, Arrays.asList("senderCode"), Arrays.asList("ASC")));
        final TableInfo _infoSenderRules = new TableInfo("sender_rules", _columnsSenderRules, _foreignKeysSenderRules, _indicesSenderRules);
        final TableInfo _existingSenderRules = TableInfo.read(db, "sender_rules");
        if (!_infoSenderRules.equals(_existingSenderRules)) {
          return new RoomOpenHelper.ValidationResult(false, "sender_rules(com.banksms.expenses.data.model.SenderRuleEntity).\n"
                  + " Expected:\n" + _infoSenderRules + "\n"
                  + " Found:\n" + _existingSenderRules);
        }
        final HashMap<String, TableInfo.Column> _columnsSettings = new HashMap<String, TableInfo.Column>(2);
        _columnsSettings.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSettings.put("value", new TableInfo.Column("value", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSettings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSettings = new TableInfo("settings", _columnsSettings, _foreignKeysSettings, _indicesSettings);
        final TableInfo _existingSettings = TableInfo.read(db, "settings");
        if (!_infoSettings.equals(_existingSettings)) {
          return new RoomOpenHelper.ValidationResult(false, "settings(com.banksms.expenses.data.model.AppSettingEntity).\n"
                  + " Expected:\n" + _infoSettings + "\n"
                  + " Found:\n" + _existingSettings);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "1e115a89e51af5769f5594dd362802d3", "b51c625fc399521c85ab502d7679258e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "transactions","sms_raw","accounts","categories","merchant_aliases","category_rules","exclusion_rules","dismissed_exclusion_suggestions","budgets","splits","parse_templates","sender_rules","settings");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `transactions`");
      _db.execSQL("DELETE FROM `sms_raw`");
      _db.execSQL("DELETE FROM `accounts`");
      _db.execSQL("DELETE FROM `categories`");
      _db.execSQL("DELETE FROM `merchant_aliases`");
      _db.execSQL("DELETE FROM `category_rules`");
      _db.execSQL("DELETE FROM `exclusion_rules`");
      _db.execSQL("DELETE FROM `dismissed_exclusion_suggestions`");
      _db.execSQL("DELETE FROM `budgets`");
      _db.execSQL("DELETE FROM `splits`");
      _db.execSQL("DELETE FROM `parse_templates`");
      _db.execSQL("DELETE FROM `sender_rules`");
      _db.execSQL("DELETE FROM `settings`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TransactionDao.class, TransactionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SmsRawDao.class, SmsRawDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AccountDao.class, AccountDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CategoryDao.class, CategoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RulesDao.class, RulesDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BudgetDao.class, BudgetDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SettingsDao.class, SettingsDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TransactionDao transactionDao() {
    if (_transactionDao != null) {
      return _transactionDao;
    } else {
      synchronized(this) {
        if(_transactionDao == null) {
          _transactionDao = new TransactionDao_Impl(this);
        }
        return _transactionDao;
      }
    }
  }

  @Override
  public SmsRawDao smsRawDao() {
    if (_smsRawDao != null) {
      return _smsRawDao;
    } else {
      synchronized(this) {
        if(_smsRawDao == null) {
          _smsRawDao = new SmsRawDao_Impl(this);
        }
        return _smsRawDao;
      }
    }
  }

  @Override
  public AccountDao accountDao() {
    if (_accountDao != null) {
      return _accountDao;
    } else {
      synchronized(this) {
        if(_accountDao == null) {
          _accountDao = new AccountDao_Impl(this);
        }
        return _accountDao;
      }
    }
  }

  @Override
  public CategoryDao categoryDao() {
    if (_categoryDao != null) {
      return _categoryDao;
    } else {
      synchronized(this) {
        if(_categoryDao == null) {
          _categoryDao = new CategoryDao_Impl(this);
        }
        return _categoryDao;
      }
    }
  }

  @Override
  public RulesDao rulesDao() {
    if (_rulesDao != null) {
      return _rulesDao;
    } else {
      synchronized(this) {
        if(_rulesDao == null) {
          _rulesDao = new RulesDao_Impl(this);
        }
        return _rulesDao;
      }
    }
  }

  @Override
  public BudgetDao budgetDao() {
    if (_budgetDao != null) {
      return _budgetDao;
    } else {
      synchronized(this) {
        if(_budgetDao == null) {
          _budgetDao = new BudgetDao_Impl(this);
        }
        return _budgetDao;
      }
    }
  }

  @Override
  public SettingsDao settingsDao() {
    if (_settingsDao != null) {
      return _settingsDao;
    } else {
      synchronized(this) {
        if(_settingsDao == null) {
          _settingsDao = new SettingsDao_Impl(this);
        }
        return _settingsDao;
      }
    }
  }
}

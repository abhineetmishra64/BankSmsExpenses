package com.banksms.expenses.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.banksms.expenses.data.model.CategoryRuleEntity;
import com.banksms.expenses.data.model.DismissedExclusionSuggestionEntity;
import com.banksms.expenses.data.model.ExclusionReason;
import com.banksms.expenses.data.model.ExclusionRuleEntity;
import com.banksms.expenses.data.model.MerchantAliasEntity;
import com.banksms.expenses.data.model.ParseTemplateEntity;
import com.banksms.expenses.data.model.SenderRuleEntity;
import com.banksms.expenses.data.model.TransactionType;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RulesDao_Impl implements RulesDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CategoryRuleEntity> __insertionAdapterOfCategoryRuleEntity;

  private final RoomConverters __roomConverters = new RoomConverters();

  private final EntityInsertionAdapter<ExclusionRuleEntity> __insertionAdapterOfExclusionRuleEntity;

  private final EntityInsertionAdapter<MerchantAliasEntity> __insertionAdapterOfMerchantAliasEntity;

  private final EntityInsertionAdapter<ParseTemplateEntity> __insertionAdapterOfParseTemplateEntity;

  private final EntityInsertionAdapter<SenderRuleEntity> __insertionAdapterOfSenderRuleEntity;

  private final EntityInsertionAdapter<DismissedExclusionSuggestionEntity> __insertionAdapterOfDismissedExclusionSuggestionEntity;

  private final EntityDeletionOrUpdateAdapter<CategoryRuleEntity> __updateAdapterOfCategoryRuleEntity;

  private final EntityDeletionOrUpdateAdapter<ExclusionRuleEntity> __updateAdapterOfExclusionRuleEntity;

  private final EntityDeletionOrUpdateAdapter<ParseTemplateEntity> __updateAdapterOfParseTemplateEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCategoryRule;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExclusionRule;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAlias;

  private final SharedSQLiteStatement __preparedStmtOfDeleteParseTemplate;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSenderRule;

  public RulesDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCategoryRuleEntity = new EntityInsertionAdapter<CategoryRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `category_rules` (`id`,`name`,`priority`,`categoryId`,`merchantPattern`,`merchantMatchMode`,`bodyKeyword`,`senderPattern`,`vpaPattern`,`minAmount`,`maxAmount`,`accountId`,`direction`,`regexPattern`,`alsoExclude`,`exclusionReason`,`enabled`,`matchCount`,`lastMatchedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CategoryRuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getPriority());
        statement.bindLong(4, entity.getCategoryId());
        if (entity.getMerchantPattern() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getMerchantPattern());
        }
        statement.bindString(6, entity.getMerchantMatchMode());
        if (entity.getBodyKeyword() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getBodyKeyword());
        }
        if (entity.getSenderPattern() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSenderPattern());
        }
        if (entity.getVpaPattern() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getVpaPattern());
        }
        if (entity.getMinAmount() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getMinAmount());
        }
        if (entity.getMaxAmount() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getMaxAmount());
        }
        if (entity.getAccountId() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getAccountId());
        }
        final String _tmp;
        if (entity.getDirection() == null) {
          _tmp = null;
        } else {
          _tmp = __roomConverters.fromTransactionType(entity.getDirection());
        }
        if (_tmp == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, _tmp);
        }
        if (entity.getRegexPattern() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getRegexPattern());
        }
        final int _tmp_1 = entity.getAlsoExclude() ? 1 : 0;
        statement.bindLong(15, _tmp_1);
        final String _tmp_2 = __roomConverters.fromExclusionReason(entity.getExclusionReason());
        if (_tmp_2 == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, _tmp_2);
        }
        final int _tmp_3 = entity.getEnabled() ? 1 : 0;
        statement.bindLong(17, _tmp_3);
        statement.bindLong(18, entity.getMatchCount());
        if (entity.getLastMatchedAt() == null) {
          statement.bindNull(19);
        } else {
          statement.bindLong(19, entity.getLastMatchedAt());
        }
      }
    };
    this.__insertionAdapterOfExclusionRuleEntity = new EntityInsertionAdapter<ExclusionRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `exclusion_rules` (`id`,`name`,`merchantPattern`,`bodyKeyword`,`regexPattern`,`exactAmount`,`amountTolerance`,`accountId`,`reason`,`enabled`,`applyRetroactively`,`matchCount`,`lastMatchedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExclusionRuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getMerchantPattern() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMerchantPattern());
        }
        if (entity.getBodyKeyword() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getBodyKeyword());
        }
        if (entity.getRegexPattern() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRegexPattern());
        }
        if (entity.getExactAmount() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getExactAmount());
        }
        statement.bindDouble(7, entity.getAmountTolerance());
        if (entity.getAccountId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getAccountId());
        }
        final String _tmp = __roomConverters.fromExclusionReason(entity.getReason());
        if (_tmp == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp);
        }
        final int _tmp_1 = entity.getEnabled() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.getApplyRetroactively() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        statement.bindLong(12, entity.getMatchCount());
        if (entity.getLastMatchedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getLastMatchedAt());
        }
      }
    };
    this.__insertionAdapterOfMerchantAliasEntity = new EntityInsertionAdapter<MerchantAliasEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `merchant_aliases` (`id`,`rawPattern`,`canonicalName`,`defaultCategoryId`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MerchantAliasEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getRawPattern());
        statement.bindString(3, entity.getCanonicalName());
        if (entity.getDefaultCategoryId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getDefaultCategoryId());
        }
      }
    };
    this.__insertionAdapterOfParseTemplateEntity = new EntityInsertionAdapter<ParseTemplateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `parse_templates` (`id`,`name`,`senderPattern`,`regexPattern`,`amountGroup`,`merchantGroup`,`accountGroup`,`balanceGroup`,`defaultDirection`,`isBuiltin`,`enabled`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ParseTemplateEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getSenderPattern());
        statement.bindString(4, entity.getRegexPattern());
        statement.bindLong(5, entity.getAmountGroup());
        if (entity.getMerchantGroup() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getMerchantGroup());
        }
        if (entity.getAccountGroup() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getAccountGroup());
        }
        if (entity.getBalanceGroup() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getBalanceGroup());
        }
        final String _tmp;
        if (entity.getDefaultDirection() == null) {
          _tmp = null;
        } else {
          _tmp = __roomConverters.fromTransactionType(entity.getDefaultDirection());
        }
        if (_tmp == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp);
        }
        final int _tmp_1 = entity.isBuiltin() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.getEnabled() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
      }
    };
    this.__insertionAdapterOfSenderRuleEntity = new EntityInsertionAdapter<SenderRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sender_rules` (`id`,`senderCode`,`action`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SenderRuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSenderCode());
        statement.bindString(3, entity.getAction());
      }
    };
    this.__insertionAdapterOfDismissedExclusionSuggestionEntity = new EntityInsertionAdapter<DismissedExclusionSuggestionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `dismissed_exclusion_suggestions` (`id`,`suggestionKey`) VALUES (nullif(?, 0),?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DismissedExclusionSuggestionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSuggestionKey());
      }
    };
    this.__updateAdapterOfCategoryRuleEntity = new EntityDeletionOrUpdateAdapter<CategoryRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `category_rules` SET `id` = ?,`name` = ?,`priority` = ?,`categoryId` = ?,`merchantPattern` = ?,`merchantMatchMode` = ?,`bodyKeyword` = ?,`senderPattern` = ?,`vpaPattern` = ?,`minAmount` = ?,`maxAmount` = ?,`accountId` = ?,`direction` = ?,`regexPattern` = ?,`alsoExclude` = ?,`exclusionReason` = ?,`enabled` = ?,`matchCount` = ?,`lastMatchedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CategoryRuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getPriority());
        statement.bindLong(4, entity.getCategoryId());
        if (entity.getMerchantPattern() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getMerchantPattern());
        }
        statement.bindString(6, entity.getMerchantMatchMode());
        if (entity.getBodyKeyword() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getBodyKeyword());
        }
        if (entity.getSenderPattern() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSenderPattern());
        }
        if (entity.getVpaPattern() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getVpaPattern());
        }
        if (entity.getMinAmount() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getMinAmount());
        }
        if (entity.getMaxAmount() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getMaxAmount());
        }
        if (entity.getAccountId() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getAccountId());
        }
        final String _tmp;
        if (entity.getDirection() == null) {
          _tmp = null;
        } else {
          _tmp = __roomConverters.fromTransactionType(entity.getDirection());
        }
        if (_tmp == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, _tmp);
        }
        if (entity.getRegexPattern() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getRegexPattern());
        }
        final int _tmp_1 = entity.getAlsoExclude() ? 1 : 0;
        statement.bindLong(15, _tmp_1);
        final String _tmp_2 = __roomConverters.fromExclusionReason(entity.getExclusionReason());
        if (_tmp_2 == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, _tmp_2);
        }
        final int _tmp_3 = entity.getEnabled() ? 1 : 0;
        statement.bindLong(17, _tmp_3);
        statement.bindLong(18, entity.getMatchCount());
        if (entity.getLastMatchedAt() == null) {
          statement.bindNull(19);
        } else {
          statement.bindLong(19, entity.getLastMatchedAt());
        }
        statement.bindLong(20, entity.getId());
      }
    };
    this.__updateAdapterOfExclusionRuleEntity = new EntityDeletionOrUpdateAdapter<ExclusionRuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `exclusion_rules` SET `id` = ?,`name` = ?,`merchantPattern` = ?,`bodyKeyword` = ?,`regexPattern` = ?,`exactAmount` = ?,`amountTolerance` = ?,`accountId` = ?,`reason` = ?,`enabled` = ?,`applyRetroactively` = ?,`matchCount` = ?,`lastMatchedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExclusionRuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getMerchantPattern() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMerchantPattern());
        }
        if (entity.getBodyKeyword() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getBodyKeyword());
        }
        if (entity.getRegexPattern() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRegexPattern());
        }
        if (entity.getExactAmount() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getExactAmount());
        }
        statement.bindDouble(7, entity.getAmountTolerance());
        if (entity.getAccountId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getAccountId());
        }
        final String _tmp = __roomConverters.fromExclusionReason(entity.getReason());
        if (_tmp == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp);
        }
        final int _tmp_1 = entity.getEnabled() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.getApplyRetroactively() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        statement.bindLong(12, entity.getMatchCount());
        if (entity.getLastMatchedAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getLastMatchedAt());
        }
        statement.bindLong(14, entity.getId());
      }
    };
    this.__updateAdapterOfParseTemplateEntity = new EntityDeletionOrUpdateAdapter<ParseTemplateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `parse_templates` SET `id` = ?,`name` = ?,`senderPattern` = ?,`regexPattern` = ?,`amountGroup` = ?,`merchantGroup` = ?,`accountGroup` = ?,`balanceGroup` = ?,`defaultDirection` = ?,`isBuiltin` = ?,`enabled` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ParseTemplateEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getSenderPattern());
        statement.bindString(4, entity.getRegexPattern());
        statement.bindLong(5, entity.getAmountGroup());
        if (entity.getMerchantGroup() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getMerchantGroup());
        }
        if (entity.getAccountGroup() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getAccountGroup());
        }
        if (entity.getBalanceGroup() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getBalanceGroup());
        }
        final String _tmp;
        if (entity.getDefaultDirection() == null) {
          _tmp = null;
        } else {
          _tmp = __roomConverters.fromTransactionType(entity.getDefaultDirection());
        }
        if (_tmp == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp);
        }
        final int _tmp_1 = entity.isBuiltin() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.getEnabled() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteCategoryRule = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM category_rules WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExclusionRule = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM exclusion_rules WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAlias = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM merchant_aliases WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteParseTemplate = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM parse_templates WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSenderRule = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sender_rules WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCategoryRule(final CategoryRuleEntity rule,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCategoryRuleEntity.insertAndReturnId(rule);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertExclusionRule(final ExclusionRuleEntity rule,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfExclusionRuleEntity.insertAndReturnId(rule);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAlias(final MerchantAliasEntity alias,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMerchantAliasEntity.insertAndReturnId(alias);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertParseTemplate(final ParseTemplateEntity template,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfParseTemplateEntity.insertAndReturnId(template);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSenderRule(final SenderRuleEntity rule,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSenderRuleEntity.insertAndReturnId(rule);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertDismissedSuggestion(final DismissedExclusionSuggestionEntity item,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfDismissedExclusionSuggestionEntity.insertAndReturnId(item);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCategoryRule(final CategoryRuleEntity rule,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCategoryRuleEntity.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateExclusionRule(final ExclusionRuleEntity rule,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfExclusionRuleEntity.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateParseTemplate(final ParseTemplateEntity template,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfParseTemplateEntity.handle(template);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCategoryRule(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCategoryRule.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCategoryRule.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExclusionRule(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExclusionRule.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExclusionRule.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAlias(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAlias.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAlias.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteParseTemplate(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteParseTemplate.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteParseTemplate.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSenderRule(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSenderRule.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSenderRule.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CategoryRuleEntity>> observeCategoryRules() {
    final String _sql = "SELECT * FROM category_rules ORDER BY priority ASC, id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"category_rules"}, new Callable<List<CategoryRuleEntity>>() {
      @Override
      @NonNull
      public List<CategoryRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfMerchantPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "merchantPattern");
          final int _cursorIndexOfMerchantMatchMode = CursorUtil.getColumnIndexOrThrow(_cursor, "merchantMatchMode");
          final int _cursorIndexOfBodyKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "bodyKeyword");
          final int _cursorIndexOfSenderPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPattern");
          final int _cursorIndexOfVpaPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "vpaPattern");
          final int _cursorIndexOfMinAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "minAmount");
          final int _cursorIndexOfMaxAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "maxAmount");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfRegexPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "regexPattern");
          final int _cursorIndexOfAlsoExclude = CursorUtil.getColumnIndexOrThrow(_cursor, "alsoExclude");
          final int _cursorIndexOfExclusionReason = CursorUtil.getColumnIndexOrThrow(_cursor, "exclusionReason");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfMatchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "matchCount");
          final int _cursorIndexOfLastMatchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMatchedAt");
          final List<CategoryRuleEntity> _result = new ArrayList<CategoryRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final String _tmpMerchantPattern;
            if (_cursor.isNull(_cursorIndexOfMerchantPattern)) {
              _tmpMerchantPattern = null;
            } else {
              _tmpMerchantPattern = _cursor.getString(_cursorIndexOfMerchantPattern);
            }
            final String _tmpMerchantMatchMode;
            _tmpMerchantMatchMode = _cursor.getString(_cursorIndexOfMerchantMatchMode);
            final String _tmpBodyKeyword;
            if (_cursor.isNull(_cursorIndexOfBodyKeyword)) {
              _tmpBodyKeyword = null;
            } else {
              _tmpBodyKeyword = _cursor.getString(_cursorIndexOfBodyKeyword);
            }
            final String _tmpSenderPattern;
            if (_cursor.isNull(_cursorIndexOfSenderPattern)) {
              _tmpSenderPattern = null;
            } else {
              _tmpSenderPattern = _cursor.getString(_cursorIndexOfSenderPattern);
            }
            final String _tmpVpaPattern;
            if (_cursor.isNull(_cursorIndexOfVpaPattern)) {
              _tmpVpaPattern = null;
            } else {
              _tmpVpaPattern = _cursor.getString(_cursorIndexOfVpaPattern);
            }
            final Double _tmpMinAmount;
            if (_cursor.isNull(_cursorIndexOfMinAmount)) {
              _tmpMinAmount = null;
            } else {
              _tmpMinAmount = _cursor.getDouble(_cursorIndexOfMinAmount);
            }
            final Double _tmpMaxAmount;
            if (_cursor.isNull(_cursorIndexOfMaxAmount)) {
              _tmpMaxAmount = null;
            } else {
              _tmpMaxAmount = _cursor.getDouble(_cursorIndexOfMaxAmount);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final TransactionType _tmpDirection;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDirection)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDirection);
            }
            if (_tmp == null) {
              _tmpDirection = null;
            } else {
              _tmpDirection = __roomConverters.toTransactionType(_tmp);
            }
            final String _tmpRegexPattern;
            if (_cursor.isNull(_cursorIndexOfRegexPattern)) {
              _tmpRegexPattern = null;
            } else {
              _tmpRegexPattern = _cursor.getString(_cursorIndexOfRegexPattern);
            }
            final boolean _tmpAlsoExclude;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfAlsoExclude);
            _tmpAlsoExclude = _tmp_1 != 0;
            final ExclusionReason _tmpExclusionReason;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfExclusionReason)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfExclusionReason);
            }
            _tmpExclusionReason = __roomConverters.toExclusionReason(_tmp_2);
            final boolean _tmpEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp_3 != 0;
            final int _tmpMatchCount;
            _tmpMatchCount = _cursor.getInt(_cursorIndexOfMatchCount);
            final Long _tmpLastMatchedAt;
            if (_cursor.isNull(_cursorIndexOfLastMatchedAt)) {
              _tmpLastMatchedAt = null;
            } else {
              _tmpLastMatchedAt = _cursor.getLong(_cursorIndexOfLastMatchedAt);
            }
            _item = new CategoryRuleEntity(_tmpId,_tmpName,_tmpPriority,_tmpCategoryId,_tmpMerchantPattern,_tmpMerchantMatchMode,_tmpBodyKeyword,_tmpSenderPattern,_tmpVpaPattern,_tmpMinAmount,_tmpMaxAmount,_tmpAccountId,_tmpDirection,_tmpRegexPattern,_tmpAlsoExclude,_tmpExclusionReason,_tmpEnabled,_tmpMatchCount,_tmpLastMatchedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getCategoryRules(final Continuation<? super List<CategoryRuleEntity>> $completion) {
    final String _sql = "SELECT * FROM category_rules ORDER BY priority ASC, id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CategoryRuleEntity>>() {
      @Override
      @NonNull
      public List<CategoryRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfMerchantPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "merchantPattern");
          final int _cursorIndexOfMerchantMatchMode = CursorUtil.getColumnIndexOrThrow(_cursor, "merchantMatchMode");
          final int _cursorIndexOfBodyKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "bodyKeyword");
          final int _cursorIndexOfSenderPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPattern");
          final int _cursorIndexOfVpaPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "vpaPattern");
          final int _cursorIndexOfMinAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "minAmount");
          final int _cursorIndexOfMaxAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "maxAmount");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
          final int _cursorIndexOfRegexPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "regexPattern");
          final int _cursorIndexOfAlsoExclude = CursorUtil.getColumnIndexOrThrow(_cursor, "alsoExclude");
          final int _cursorIndexOfExclusionReason = CursorUtil.getColumnIndexOrThrow(_cursor, "exclusionReason");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfMatchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "matchCount");
          final int _cursorIndexOfLastMatchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMatchedAt");
          final List<CategoryRuleEntity> _result = new ArrayList<CategoryRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final String _tmpMerchantPattern;
            if (_cursor.isNull(_cursorIndexOfMerchantPattern)) {
              _tmpMerchantPattern = null;
            } else {
              _tmpMerchantPattern = _cursor.getString(_cursorIndexOfMerchantPattern);
            }
            final String _tmpMerchantMatchMode;
            _tmpMerchantMatchMode = _cursor.getString(_cursorIndexOfMerchantMatchMode);
            final String _tmpBodyKeyword;
            if (_cursor.isNull(_cursorIndexOfBodyKeyword)) {
              _tmpBodyKeyword = null;
            } else {
              _tmpBodyKeyword = _cursor.getString(_cursorIndexOfBodyKeyword);
            }
            final String _tmpSenderPattern;
            if (_cursor.isNull(_cursorIndexOfSenderPattern)) {
              _tmpSenderPattern = null;
            } else {
              _tmpSenderPattern = _cursor.getString(_cursorIndexOfSenderPattern);
            }
            final String _tmpVpaPattern;
            if (_cursor.isNull(_cursorIndexOfVpaPattern)) {
              _tmpVpaPattern = null;
            } else {
              _tmpVpaPattern = _cursor.getString(_cursorIndexOfVpaPattern);
            }
            final Double _tmpMinAmount;
            if (_cursor.isNull(_cursorIndexOfMinAmount)) {
              _tmpMinAmount = null;
            } else {
              _tmpMinAmount = _cursor.getDouble(_cursorIndexOfMinAmount);
            }
            final Double _tmpMaxAmount;
            if (_cursor.isNull(_cursorIndexOfMaxAmount)) {
              _tmpMaxAmount = null;
            } else {
              _tmpMaxAmount = _cursor.getDouble(_cursorIndexOfMaxAmount);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final TransactionType _tmpDirection;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDirection)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDirection);
            }
            if (_tmp == null) {
              _tmpDirection = null;
            } else {
              _tmpDirection = __roomConverters.toTransactionType(_tmp);
            }
            final String _tmpRegexPattern;
            if (_cursor.isNull(_cursorIndexOfRegexPattern)) {
              _tmpRegexPattern = null;
            } else {
              _tmpRegexPattern = _cursor.getString(_cursorIndexOfRegexPattern);
            }
            final boolean _tmpAlsoExclude;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfAlsoExclude);
            _tmpAlsoExclude = _tmp_1 != 0;
            final ExclusionReason _tmpExclusionReason;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfExclusionReason)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfExclusionReason);
            }
            _tmpExclusionReason = __roomConverters.toExclusionReason(_tmp_2);
            final boolean _tmpEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp_3 != 0;
            final int _tmpMatchCount;
            _tmpMatchCount = _cursor.getInt(_cursorIndexOfMatchCount);
            final Long _tmpLastMatchedAt;
            if (_cursor.isNull(_cursorIndexOfLastMatchedAt)) {
              _tmpLastMatchedAt = null;
            } else {
              _tmpLastMatchedAt = _cursor.getLong(_cursorIndexOfLastMatchedAt);
            }
            _item = new CategoryRuleEntity(_tmpId,_tmpName,_tmpPriority,_tmpCategoryId,_tmpMerchantPattern,_tmpMerchantMatchMode,_tmpBodyKeyword,_tmpSenderPattern,_tmpVpaPattern,_tmpMinAmount,_tmpMaxAmount,_tmpAccountId,_tmpDirection,_tmpRegexPattern,_tmpAlsoExclude,_tmpExclusionReason,_tmpEnabled,_tmpMatchCount,_tmpLastMatchedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ExclusionRuleEntity>> observeExclusionRules() {
    final String _sql = "SELECT * FROM exclusion_rules ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"exclusion_rules"}, new Callable<List<ExclusionRuleEntity>>() {
      @Override
      @NonNull
      public List<ExclusionRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMerchantPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "merchantPattern");
          final int _cursorIndexOfBodyKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "bodyKeyword");
          final int _cursorIndexOfRegexPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "regexPattern");
          final int _cursorIndexOfExactAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "exactAmount");
          final int _cursorIndexOfAmountTolerance = CursorUtil.getColumnIndexOrThrow(_cursor, "amountTolerance");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfApplyRetroactively = CursorUtil.getColumnIndexOrThrow(_cursor, "applyRetroactively");
          final int _cursorIndexOfMatchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "matchCount");
          final int _cursorIndexOfLastMatchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMatchedAt");
          final List<ExclusionRuleEntity> _result = new ArrayList<ExclusionRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExclusionRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpMerchantPattern;
            if (_cursor.isNull(_cursorIndexOfMerchantPattern)) {
              _tmpMerchantPattern = null;
            } else {
              _tmpMerchantPattern = _cursor.getString(_cursorIndexOfMerchantPattern);
            }
            final String _tmpBodyKeyword;
            if (_cursor.isNull(_cursorIndexOfBodyKeyword)) {
              _tmpBodyKeyword = null;
            } else {
              _tmpBodyKeyword = _cursor.getString(_cursorIndexOfBodyKeyword);
            }
            final String _tmpRegexPattern;
            if (_cursor.isNull(_cursorIndexOfRegexPattern)) {
              _tmpRegexPattern = null;
            } else {
              _tmpRegexPattern = _cursor.getString(_cursorIndexOfRegexPattern);
            }
            final Double _tmpExactAmount;
            if (_cursor.isNull(_cursorIndexOfExactAmount)) {
              _tmpExactAmount = null;
            } else {
              _tmpExactAmount = _cursor.getDouble(_cursorIndexOfExactAmount);
            }
            final double _tmpAmountTolerance;
            _tmpAmountTolerance = _cursor.getDouble(_cursorIndexOfAmountTolerance);
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final ExclusionReason _tmpReason;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfReason)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfReason);
            }
            final ExclusionReason _tmp_1 = __roomConverters.toExclusionReason(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.banksms.expenses.data.model.ExclusionReason', but it was NULL.");
            } else {
              _tmpReason = _tmp_1;
            }
            final boolean _tmpEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp_2 != 0;
            final boolean _tmpApplyRetroactively;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfApplyRetroactively);
            _tmpApplyRetroactively = _tmp_3 != 0;
            final int _tmpMatchCount;
            _tmpMatchCount = _cursor.getInt(_cursorIndexOfMatchCount);
            final Long _tmpLastMatchedAt;
            if (_cursor.isNull(_cursorIndexOfLastMatchedAt)) {
              _tmpLastMatchedAt = null;
            } else {
              _tmpLastMatchedAt = _cursor.getLong(_cursorIndexOfLastMatchedAt);
            }
            _item = new ExclusionRuleEntity(_tmpId,_tmpName,_tmpMerchantPattern,_tmpBodyKeyword,_tmpRegexPattern,_tmpExactAmount,_tmpAmountTolerance,_tmpAccountId,_tmpReason,_tmpEnabled,_tmpApplyRetroactively,_tmpMatchCount,_tmpLastMatchedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getExclusionRules(
      final Continuation<? super List<ExclusionRuleEntity>> $completion) {
    final String _sql = "SELECT * FROM exclusion_rules ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExclusionRuleEntity>>() {
      @Override
      @NonNull
      public List<ExclusionRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMerchantPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "merchantPattern");
          final int _cursorIndexOfBodyKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "bodyKeyword");
          final int _cursorIndexOfRegexPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "regexPattern");
          final int _cursorIndexOfExactAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "exactAmount");
          final int _cursorIndexOfAmountTolerance = CursorUtil.getColumnIndexOrThrow(_cursor, "amountTolerance");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfApplyRetroactively = CursorUtil.getColumnIndexOrThrow(_cursor, "applyRetroactively");
          final int _cursorIndexOfMatchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "matchCount");
          final int _cursorIndexOfLastMatchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMatchedAt");
          final List<ExclusionRuleEntity> _result = new ArrayList<ExclusionRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExclusionRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpMerchantPattern;
            if (_cursor.isNull(_cursorIndexOfMerchantPattern)) {
              _tmpMerchantPattern = null;
            } else {
              _tmpMerchantPattern = _cursor.getString(_cursorIndexOfMerchantPattern);
            }
            final String _tmpBodyKeyword;
            if (_cursor.isNull(_cursorIndexOfBodyKeyword)) {
              _tmpBodyKeyword = null;
            } else {
              _tmpBodyKeyword = _cursor.getString(_cursorIndexOfBodyKeyword);
            }
            final String _tmpRegexPattern;
            if (_cursor.isNull(_cursorIndexOfRegexPattern)) {
              _tmpRegexPattern = null;
            } else {
              _tmpRegexPattern = _cursor.getString(_cursorIndexOfRegexPattern);
            }
            final Double _tmpExactAmount;
            if (_cursor.isNull(_cursorIndexOfExactAmount)) {
              _tmpExactAmount = null;
            } else {
              _tmpExactAmount = _cursor.getDouble(_cursorIndexOfExactAmount);
            }
            final double _tmpAmountTolerance;
            _tmpAmountTolerance = _cursor.getDouble(_cursorIndexOfAmountTolerance);
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final ExclusionReason _tmpReason;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfReason)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfReason);
            }
            final ExclusionReason _tmp_1 = __roomConverters.toExclusionReason(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.banksms.expenses.data.model.ExclusionReason', but it was NULL.");
            } else {
              _tmpReason = _tmp_1;
            }
            final boolean _tmpEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp_2 != 0;
            final boolean _tmpApplyRetroactively;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfApplyRetroactively);
            _tmpApplyRetroactively = _tmp_3 != 0;
            final int _tmpMatchCount;
            _tmpMatchCount = _cursor.getInt(_cursorIndexOfMatchCount);
            final Long _tmpLastMatchedAt;
            if (_cursor.isNull(_cursorIndexOfLastMatchedAt)) {
              _tmpLastMatchedAt = null;
            } else {
              _tmpLastMatchedAt = _cursor.getLong(_cursorIndexOfLastMatchedAt);
            }
            _item = new ExclusionRuleEntity(_tmpId,_tmpName,_tmpMerchantPattern,_tmpBodyKeyword,_tmpRegexPattern,_tmpExactAmount,_tmpAmountTolerance,_tmpAccountId,_tmpReason,_tmpEnabled,_tmpApplyRetroactively,_tmpMatchCount,_tmpLastMatchedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MerchantAliasEntity>> observeAliases() {
    final String _sql = "SELECT * FROM merchant_aliases ORDER BY canonicalName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"merchant_aliases"}, new Callable<List<MerchantAliasEntity>>() {
      @Override
      @NonNull
      public List<MerchantAliasEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRawPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "rawPattern");
          final int _cursorIndexOfCanonicalName = CursorUtil.getColumnIndexOrThrow(_cursor, "canonicalName");
          final int _cursorIndexOfDefaultCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultCategoryId");
          final List<MerchantAliasEntity> _result = new ArrayList<MerchantAliasEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MerchantAliasEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpRawPattern;
            _tmpRawPattern = _cursor.getString(_cursorIndexOfRawPattern);
            final String _tmpCanonicalName;
            _tmpCanonicalName = _cursor.getString(_cursorIndexOfCanonicalName);
            final Long _tmpDefaultCategoryId;
            if (_cursor.isNull(_cursorIndexOfDefaultCategoryId)) {
              _tmpDefaultCategoryId = null;
            } else {
              _tmpDefaultCategoryId = _cursor.getLong(_cursorIndexOfDefaultCategoryId);
            }
            _item = new MerchantAliasEntity(_tmpId,_tmpRawPattern,_tmpCanonicalName,_tmpDefaultCategoryId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAliases(final Continuation<? super List<MerchantAliasEntity>> $completion) {
    final String _sql = "SELECT * FROM merchant_aliases ORDER BY canonicalName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MerchantAliasEntity>>() {
      @Override
      @NonNull
      public List<MerchantAliasEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRawPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "rawPattern");
          final int _cursorIndexOfCanonicalName = CursorUtil.getColumnIndexOrThrow(_cursor, "canonicalName");
          final int _cursorIndexOfDefaultCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultCategoryId");
          final List<MerchantAliasEntity> _result = new ArrayList<MerchantAliasEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MerchantAliasEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpRawPattern;
            _tmpRawPattern = _cursor.getString(_cursorIndexOfRawPattern);
            final String _tmpCanonicalName;
            _tmpCanonicalName = _cursor.getString(_cursorIndexOfCanonicalName);
            final Long _tmpDefaultCategoryId;
            if (_cursor.isNull(_cursorIndexOfDefaultCategoryId)) {
              _tmpDefaultCategoryId = null;
            } else {
              _tmpDefaultCategoryId = _cursor.getLong(_cursorIndexOfDefaultCategoryId);
            }
            _item = new MerchantAliasEntity(_tmpId,_tmpRawPattern,_tmpCanonicalName,_tmpDefaultCategoryId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ParseTemplateEntity>> observeParseTemplates() {
    final String _sql = "SELECT * FROM parse_templates ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"parse_templates"}, new Callable<List<ParseTemplateEntity>>() {
      @Override
      @NonNull
      public List<ParseTemplateEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSenderPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPattern");
          final int _cursorIndexOfRegexPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "regexPattern");
          final int _cursorIndexOfAmountGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGroup");
          final int _cursorIndexOfMerchantGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "merchantGroup");
          final int _cursorIndexOfAccountGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "accountGroup");
          final int _cursorIndexOfBalanceGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "balanceGroup");
          final int _cursorIndexOfDefaultDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultDirection");
          final int _cursorIndexOfIsBuiltin = CursorUtil.getColumnIndexOrThrow(_cursor, "isBuiltin");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final List<ParseTemplateEntity> _result = new ArrayList<ParseTemplateEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ParseTemplateEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpSenderPattern;
            _tmpSenderPattern = _cursor.getString(_cursorIndexOfSenderPattern);
            final String _tmpRegexPattern;
            _tmpRegexPattern = _cursor.getString(_cursorIndexOfRegexPattern);
            final int _tmpAmountGroup;
            _tmpAmountGroup = _cursor.getInt(_cursorIndexOfAmountGroup);
            final Integer _tmpMerchantGroup;
            if (_cursor.isNull(_cursorIndexOfMerchantGroup)) {
              _tmpMerchantGroup = null;
            } else {
              _tmpMerchantGroup = _cursor.getInt(_cursorIndexOfMerchantGroup);
            }
            final Integer _tmpAccountGroup;
            if (_cursor.isNull(_cursorIndexOfAccountGroup)) {
              _tmpAccountGroup = null;
            } else {
              _tmpAccountGroup = _cursor.getInt(_cursorIndexOfAccountGroup);
            }
            final Integer _tmpBalanceGroup;
            if (_cursor.isNull(_cursorIndexOfBalanceGroup)) {
              _tmpBalanceGroup = null;
            } else {
              _tmpBalanceGroup = _cursor.getInt(_cursorIndexOfBalanceGroup);
            }
            final TransactionType _tmpDefaultDirection;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDefaultDirection)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDefaultDirection);
            }
            if (_tmp == null) {
              _tmpDefaultDirection = null;
            } else {
              _tmpDefaultDirection = __roomConverters.toTransactionType(_tmp);
            }
            final boolean _tmpIsBuiltin;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsBuiltin);
            _tmpIsBuiltin = _tmp_1 != 0;
            final boolean _tmpEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp_2 != 0;
            _item = new ParseTemplateEntity(_tmpId,_tmpName,_tmpSenderPattern,_tmpRegexPattern,_tmpAmountGroup,_tmpMerchantGroup,_tmpAccountGroup,_tmpBalanceGroup,_tmpDefaultDirection,_tmpIsBuiltin,_tmpEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getParseTemplates(
      final Continuation<? super List<ParseTemplateEntity>> $completion) {
    final String _sql = "SELECT * FROM parse_templates ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ParseTemplateEntity>>() {
      @Override
      @NonNull
      public List<ParseTemplateEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSenderPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "senderPattern");
          final int _cursorIndexOfRegexPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "regexPattern");
          final int _cursorIndexOfAmountGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGroup");
          final int _cursorIndexOfMerchantGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "merchantGroup");
          final int _cursorIndexOfAccountGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "accountGroup");
          final int _cursorIndexOfBalanceGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "balanceGroup");
          final int _cursorIndexOfDefaultDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultDirection");
          final int _cursorIndexOfIsBuiltin = CursorUtil.getColumnIndexOrThrow(_cursor, "isBuiltin");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final List<ParseTemplateEntity> _result = new ArrayList<ParseTemplateEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ParseTemplateEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpSenderPattern;
            _tmpSenderPattern = _cursor.getString(_cursorIndexOfSenderPattern);
            final String _tmpRegexPattern;
            _tmpRegexPattern = _cursor.getString(_cursorIndexOfRegexPattern);
            final int _tmpAmountGroup;
            _tmpAmountGroup = _cursor.getInt(_cursorIndexOfAmountGroup);
            final Integer _tmpMerchantGroup;
            if (_cursor.isNull(_cursorIndexOfMerchantGroup)) {
              _tmpMerchantGroup = null;
            } else {
              _tmpMerchantGroup = _cursor.getInt(_cursorIndexOfMerchantGroup);
            }
            final Integer _tmpAccountGroup;
            if (_cursor.isNull(_cursorIndexOfAccountGroup)) {
              _tmpAccountGroup = null;
            } else {
              _tmpAccountGroup = _cursor.getInt(_cursorIndexOfAccountGroup);
            }
            final Integer _tmpBalanceGroup;
            if (_cursor.isNull(_cursorIndexOfBalanceGroup)) {
              _tmpBalanceGroup = null;
            } else {
              _tmpBalanceGroup = _cursor.getInt(_cursorIndexOfBalanceGroup);
            }
            final TransactionType _tmpDefaultDirection;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDefaultDirection)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDefaultDirection);
            }
            if (_tmp == null) {
              _tmpDefaultDirection = null;
            } else {
              _tmpDefaultDirection = __roomConverters.toTransactionType(_tmp);
            }
            final boolean _tmpIsBuiltin;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsBuiltin);
            _tmpIsBuiltin = _tmp_1 != 0;
            final boolean _tmpEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp_2 != 0;
            _item = new ParseTemplateEntity(_tmpId,_tmpName,_tmpSenderPattern,_tmpRegexPattern,_tmpAmountGroup,_tmpMerchantGroup,_tmpAccountGroup,_tmpBalanceGroup,_tmpDefaultDirection,_tmpIsBuiltin,_tmpEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SenderRuleEntity>> observeSenderRules() {
    final String _sql = "SELECT * FROM sender_rules ORDER BY senderCode ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sender_rules"}, new Callable<List<SenderRuleEntity>>() {
      @Override
      @NonNull
      public List<SenderRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSenderCode = CursorUtil.getColumnIndexOrThrow(_cursor, "senderCode");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final List<SenderRuleEntity> _result = new ArrayList<SenderRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SenderRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSenderCode;
            _tmpSenderCode = _cursor.getString(_cursorIndexOfSenderCode);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            _item = new SenderRuleEntity(_tmpId,_tmpSenderCode,_tmpAction);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSenderRules(final Continuation<? super List<SenderRuleEntity>> $completion) {
    final String _sql = "SELECT * FROM sender_rules ORDER BY senderCode ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SenderRuleEntity>>() {
      @Override
      @NonNull
      public List<SenderRuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSenderCode = CursorUtil.getColumnIndexOrThrow(_cursor, "senderCode");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final List<SenderRuleEntity> _result = new ArrayList<SenderRuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SenderRuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSenderCode;
            _tmpSenderCode = _cursor.getString(_cursorIndexOfSenderCode);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            _item = new SenderRuleEntity(_tmpId,_tmpSenderCode,_tmpAction);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DismissedExclusionSuggestionEntity>> observeDismissedSuggestions() {
    final String _sql = "SELECT * FROM dismissed_exclusion_suggestions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"dismissed_exclusion_suggestions"}, new Callable<List<DismissedExclusionSuggestionEntity>>() {
      @Override
      @NonNull
      public List<DismissedExclusionSuggestionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSuggestionKey = CursorUtil.getColumnIndexOrThrow(_cursor, "suggestionKey");
          final List<DismissedExclusionSuggestionEntity> _result = new ArrayList<DismissedExclusionSuggestionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DismissedExclusionSuggestionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSuggestionKey;
            _tmpSuggestionKey = _cursor.getString(_cursorIndexOfSuggestionKey);
            _item = new DismissedExclusionSuggestionEntity(_tmpId,_tmpSuggestionKey);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getDismissedSuggestions(
      final Continuation<? super List<DismissedExclusionSuggestionEntity>> $completion) {
    final String _sql = "SELECT * FROM dismissed_exclusion_suggestions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DismissedExclusionSuggestionEntity>>() {
      @Override
      @NonNull
      public List<DismissedExclusionSuggestionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSuggestionKey = CursorUtil.getColumnIndexOrThrow(_cursor, "suggestionKey");
          final List<DismissedExclusionSuggestionEntity> _result = new ArrayList<DismissedExclusionSuggestionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DismissedExclusionSuggestionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSuggestionKey;
            _tmpSuggestionKey = _cursor.getString(_cursorIndexOfSuggestionKey);
            _item = new DismissedExclusionSuggestionEntity(_tmpId,_tmpSuggestionKey);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

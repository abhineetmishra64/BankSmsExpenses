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
import com.banksms.expenses.data.model.CategorySource;
import com.banksms.expenses.data.model.ExclusionReason;
import com.banksms.expenses.data.model.ExclusionSource;
import com.banksms.expenses.data.model.TransactionChannel;
import com.banksms.expenses.data.model.TransactionEntity;
import com.banksms.expenses.data.model.TransactionType;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
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
public final class TransactionDao_Impl implements TransactionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TransactionEntity> __insertionAdapterOfTransactionEntity;

  private final RoomConverters __roomConverters = new RoomConverters();

  private final EntityDeletionOrUpdateAdapter<TransactionEntity> __updateAdapterOfTransactionEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public TransactionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTransactionEntity = new EntityInsertionAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `transactions` (`id`,`smsId`,`accountId`,`sender`,`bankName`,`accountLast4`,`amount`,`currency`,`type`,`merchant`,`rawMerchant`,`categoryId`,`categorySource`,`channel`,`refNo`,`balanceAfter`,`creditLimitAfter`,`txnTime`,`rawBody`,`fingerprint`,`isExcluded`,`exclusionReason`,`exclusionSource`,`exclusionRuleId`,`matchedTransferId`,`note`,`tags`,`isManual`,`isInternational`,`linkedOriginalTxnId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransactionEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getSmsId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getSmsId());
        }
        if (entity.getAccountId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getAccountId());
        }
        statement.bindString(4, entity.getSender());
        statement.bindString(5, entity.getBankName());
        if (entity.getAccountLast4() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getAccountLast4());
        }
        statement.bindDouble(7, entity.getAmount());
        statement.bindString(8, entity.getCurrency());
        final String _tmp = __roomConverters.fromTransactionType(entity.getType());
        statement.bindString(9, _tmp);
        statement.bindString(10, entity.getMerchant());
        statement.bindString(11, entity.getRawMerchant());
        statement.bindLong(12, entity.getCategoryId());
        final String _tmp_1 = __roomConverters.fromCategorySource(entity.getCategorySource());
        statement.bindString(13, _tmp_1);
        final String _tmp_2 = __roomConverters.fromTransactionChannel(entity.getChannel());
        statement.bindString(14, _tmp_2);
        if (entity.getRefNo() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getRefNo());
        }
        if (entity.getBalanceAfter() == null) {
          statement.bindNull(16);
        } else {
          statement.bindDouble(16, entity.getBalanceAfter());
        }
        if (entity.getCreditLimitAfter() == null) {
          statement.bindNull(17);
        } else {
          statement.bindDouble(17, entity.getCreditLimitAfter());
        }
        statement.bindLong(18, entity.getTxnTime());
        statement.bindString(19, entity.getRawBody());
        statement.bindString(20, entity.getFingerprint());
        final int _tmp_3 = entity.isExcluded() ? 1 : 0;
        statement.bindLong(21, _tmp_3);
        final String _tmp_4 = __roomConverters.fromExclusionReason(entity.getExclusionReason());
        if (_tmp_4 == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, _tmp_4);
        }
        final String _tmp_5 = __roomConverters.fromExclusionSource(entity.getExclusionSource());
        statement.bindString(23, _tmp_5);
        if (entity.getExclusionRuleId() == null) {
          statement.bindNull(24);
        } else {
          statement.bindLong(24, entity.getExclusionRuleId());
        }
        if (entity.getMatchedTransferId() == null) {
          statement.bindNull(25);
        } else {
          statement.bindLong(25, entity.getMatchedTransferId());
        }
        statement.bindString(26, entity.getNote());
        statement.bindString(27, entity.getTags());
        final int _tmp_6 = entity.isManual() ? 1 : 0;
        statement.bindLong(28, _tmp_6);
        final int _tmp_7 = entity.isInternational() ? 1 : 0;
        statement.bindLong(29, _tmp_7);
        if (entity.getLinkedOriginalTxnId() == null) {
          statement.bindNull(30);
        } else {
          statement.bindLong(30, entity.getLinkedOriginalTxnId());
        }
      }
    };
    this.__updateAdapterOfTransactionEntity = new EntityDeletionOrUpdateAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `transactions` SET `id` = ?,`smsId` = ?,`accountId` = ?,`sender` = ?,`bankName` = ?,`accountLast4` = ?,`amount` = ?,`currency` = ?,`type` = ?,`merchant` = ?,`rawMerchant` = ?,`categoryId` = ?,`categorySource` = ?,`channel` = ?,`refNo` = ?,`balanceAfter` = ?,`creditLimitAfter` = ?,`txnTime` = ?,`rawBody` = ?,`fingerprint` = ?,`isExcluded` = ?,`exclusionReason` = ?,`exclusionSource` = ?,`exclusionRuleId` = ?,`matchedTransferId` = ?,`note` = ?,`tags` = ?,`isManual` = ?,`isInternational` = ?,`linkedOriginalTxnId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransactionEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getSmsId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getSmsId());
        }
        if (entity.getAccountId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getAccountId());
        }
        statement.bindString(4, entity.getSender());
        statement.bindString(5, entity.getBankName());
        if (entity.getAccountLast4() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getAccountLast4());
        }
        statement.bindDouble(7, entity.getAmount());
        statement.bindString(8, entity.getCurrency());
        final String _tmp = __roomConverters.fromTransactionType(entity.getType());
        statement.bindString(9, _tmp);
        statement.bindString(10, entity.getMerchant());
        statement.bindString(11, entity.getRawMerchant());
        statement.bindLong(12, entity.getCategoryId());
        final String _tmp_1 = __roomConverters.fromCategorySource(entity.getCategorySource());
        statement.bindString(13, _tmp_1);
        final String _tmp_2 = __roomConverters.fromTransactionChannel(entity.getChannel());
        statement.bindString(14, _tmp_2);
        if (entity.getRefNo() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getRefNo());
        }
        if (entity.getBalanceAfter() == null) {
          statement.bindNull(16);
        } else {
          statement.bindDouble(16, entity.getBalanceAfter());
        }
        if (entity.getCreditLimitAfter() == null) {
          statement.bindNull(17);
        } else {
          statement.bindDouble(17, entity.getCreditLimitAfter());
        }
        statement.bindLong(18, entity.getTxnTime());
        statement.bindString(19, entity.getRawBody());
        statement.bindString(20, entity.getFingerprint());
        final int _tmp_3 = entity.isExcluded() ? 1 : 0;
        statement.bindLong(21, _tmp_3);
        final String _tmp_4 = __roomConverters.fromExclusionReason(entity.getExclusionReason());
        if (_tmp_4 == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, _tmp_4);
        }
        final String _tmp_5 = __roomConverters.fromExclusionSource(entity.getExclusionSource());
        statement.bindString(23, _tmp_5);
        if (entity.getExclusionRuleId() == null) {
          statement.bindNull(24);
        } else {
          statement.bindLong(24, entity.getExclusionRuleId());
        }
        if (entity.getMatchedTransferId() == null) {
          statement.bindNull(25);
        } else {
          statement.bindLong(25, entity.getMatchedTransferId());
        }
        statement.bindString(26, entity.getNote());
        statement.bindString(27, entity.getTags());
        final int _tmp_6 = entity.isManual() ? 1 : 0;
        statement.bindLong(28, _tmp_6);
        final int _tmp_7 = entity.isInternational() ? 1 : 0;
        statement.bindLong(29, _tmp_7);
        if (entity.getLinkedOriginalTxnId() == null) {
          statement.bindNull(30);
        } else {
          statement.bindLong(30, entity.getLinkedOriginalTxnId());
        }
        statement.bindLong(31, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM transactions WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM transactions";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<TransactionEntity> transactions,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfTransactionEntity.insertAndReturnIdsList(transactions);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insert(final TransactionEntity transaction,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTransactionEntity.insertAndReturnId(transaction);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final TransactionEntity transaction,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTransactionEntity.handle(transaction);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAll(final List<TransactionEntity> transactions,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTransactionEntity.handleMultiple(transactions);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
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
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TransactionEntity>> observeAllTransactions() {
    final String _sql = "SELECT * FROM transactions ORDER BY txnTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSmsId = CursorUtil.getColumnIndexOrThrow(_cursor, "smsId");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfBankName = CursorUtil.getColumnIndexOrThrow(_cursor, "bankName");
          final int _cursorIndexOfAccountLast4 = CursorUtil.getColumnIndexOrThrow(_cursor, "accountLast4");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfMerchant = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant");
          final int _cursorIndexOfRawMerchant = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMerchant");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfCategorySource = CursorUtil.getColumnIndexOrThrow(_cursor, "categorySource");
          final int _cursorIndexOfChannel = CursorUtil.getColumnIndexOrThrow(_cursor, "channel");
          final int _cursorIndexOfRefNo = CursorUtil.getColumnIndexOrThrow(_cursor, "refNo");
          final int _cursorIndexOfBalanceAfter = CursorUtil.getColumnIndexOrThrow(_cursor, "balanceAfter");
          final int _cursorIndexOfCreditLimitAfter = CursorUtil.getColumnIndexOrThrow(_cursor, "creditLimitAfter");
          final int _cursorIndexOfTxnTime = CursorUtil.getColumnIndexOrThrow(_cursor, "txnTime");
          final int _cursorIndexOfRawBody = CursorUtil.getColumnIndexOrThrow(_cursor, "rawBody");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfIsExcluded = CursorUtil.getColumnIndexOrThrow(_cursor, "isExcluded");
          final int _cursorIndexOfExclusionReason = CursorUtil.getColumnIndexOrThrow(_cursor, "exclusionReason");
          final int _cursorIndexOfExclusionSource = CursorUtil.getColumnIndexOrThrow(_cursor, "exclusionSource");
          final int _cursorIndexOfExclusionRuleId = CursorUtil.getColumnIndexOrThrow(_cursor, "exclusionRuleId");
          final int _cursorIndexOfMatchedTransferId = CursorUtil.getColumnIndexOrThrow(_cursor, "matchedTransferId");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfIsManual = CursorUtil.getColumnIndexOrThrow(_cursor, "isManual");
          final int _cursorIndexOfIsInternational = CursorUtil.getColumnIndexOrThrow(_cursor, "isInternational");
          final int _cursorIndexOfLinkedOriginalTxnId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedOriginalTxnId");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpSmsId;
            if (_cursor.isNull(_cursorIndexOfSmsId)) {
              _tmpSmsId = null;
            } else {
              _tmpSmsId = _cursor.getLong(_cursorIndexOfSmsId);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final String _tmpSender;
            _tmpSender = _cursor.getString(_cursorIndexOfSender);
            final String _tmpBankName;
            _tmpBankName = _cursor.getString(_cursorIndexOfBankName);
            final String _tmpAccountLast4;
            if (_cursor.isNull(_cursorIndexOfAccountLast4)) {
              _tmpAccountLast4 = null;
            } else {
              _tmpAccountLast4 = _cursor.getString(_cursorIndexOfAccountLast4);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final TransactionType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __roomConverters.toTransactionType(_tmp);
            final String _tmpMerchant;
            _tmpMerchant = _cursor.getString(_cursorIndexOfMerchant);
            final String _tmpRawMerchant;
            _tmpRawMerchant = _cursor.getString(_cursorIndexOfRawMerchant);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final CategorySource _tmpCategorySource;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfCategorySource);
            _tmpCategorySource = __roomConverters.toCategorySource(_tmp_1);
            final TransactionChannel _tmpChannel;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfChannel);
            _tmpChannel = __roomConverters.toTransactionChannel(_tmp_2);
            final String _tmpRefNo;
            if (_cursor.isNull(_cursorIndexOfRefNo)) {
              _tmpRefNo = null;
            } else {
              _tmpRefNo = _cursor.getString(_cursorIndexOfRefNo);
            }
            final Double _tmpBalanceAfter;
            if (_cursor.isNull(_cursorIndexOfBalanceAfter)) {
              _tmpBalanceAfter = null;
            } else {
              _tmpBalanceAfter = _cursor.getDouble(_cursorIndexOfBalanceAfter);
            }
            final Double _tmpCreditLimitAfter;
            if (_cursor.isNull(_cursorIndexOfCreditLimitAfter)) {
              _tmpCreditLimitAfter = null;
            } else {
              _tmpCreditLimitAfter = _cursor.getDouble(_cursorIndexOfCreditLimitAfter);
            }
            final long _tmpTxnTime;
            _tmpTxnTime = _cursor.getLong(_cursorIndexOfTxnTime);
            final String _tmpRawBody;
            _tmpRawBody = _cursor.getString(_cursorIndexOfRawBody);
            final String _tmpFingerprint;
            _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            final boolean _tmpIsExcluded;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsExcluded);
            _tmpIsExcluded = _tmp_3 != 0;
            final ExclusionReason _tmpExclusionReason;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfExclusionReason)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfExclusionReason);
            }
            _tmpExclusionReason = __roomConverters.toExclusionReason(_tmp_4);
            final ExclusionSource _tmpExclusionSource;
            final String _tmp_5;
            _tmp_5 = _cursor.getString(_cursorIndexOfExclusionSource);
            _tmpExclusionSource = __roomConverters.toExclusionSource(_tmp_5);
            final Long _tmpExclusionRuleId;
            if (_cursor.isNull(_cursorIndexOfExclusionRuleId)) {
              _tmpExclusionRuleId = null;
            } else {
              _tmpExclusionRuleId = _cursor.getLong(_cursorIndexOfExclusionRuleId);
            }
            final Long _tmpMatchedTransferId;
            if (_cursor.isNull(_cursorIndexOfMatchedTransferId)) {
              _tmpMatchedTransferId = null;
            } else {
              _tmpMatchedTransferId = _cursor.getLong(_cursorIndexOfMatchedTransferId);
            }
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final boolean _tmpIsManual;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsManual);
            _tmpIsManual = _tmp_6 != 0;
            final boolean _tmpIsInternational;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsInternational);
            _tmpIsInternational = _tmp_7 != 0;
            final Long _tmpLinkedOriginalTxnId;
            if (_cursor.isNull(_cursorIndexOfLinkedOriginalTxnId)) {
              _tmpLinkedOriginalTxnId = null;
            } else {
              _tmpLinkedOriginalTxnId = _cursor.getLong(_cursorIndexOfLinkedOriginalTxnId);
            }
            _item = new TransactionEntity(_tmpId,_tmpSmsId,_tmpAccountId,_tmpSender,_tmpBankName,_tmpAccountLast4,_tmpAmount,_tmpCurrency,_tmpType,_tmpMerchant,_tmpRawMerchant,_tmpCategoryId,_tmpCategorySource,_tmpChannel,_tmpRefNo,_tmpBalanceAfter,_tmpCreditLimitAfter,_tmpTxnTime,_tmpRawBody,_tmpFingerprint,_tmpIsExcluded,_tmpExclusionReason,_tmpExclusionSource,_tmpExclusionRuleId,_tmpMatchedTransferId,_tmpNote,_tmpTags,_tmpIsManual,_tmpIsInternational,_tmpLinkedOriginalTxnId);
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
  public Object getAllTransactions(
      final Continuation<? super List<TransactionEntity>> $completion) {
    final String _sql = "SELECT * FROM transactions ORDER BY txnTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSmsId = CursorUtil.getColumnIndexOrThrow(_cursor, "smsId");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfBankName = CursorUtil.getColumnIndexOrThrow(_cursor, "bankName");
          final int _cursorIndexOfAccountLast4 = CursorUtil.getColumnIndexOrThrow(_cursor, "accountLast4");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfMerchant = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant");
          final int _cursorIndexOfRawMerchant = CursorUtil.getColumnIndexOrThrow(_cursor, "rawMerchant");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfCategorySource = CursorUtil.getColumnIndexOrThrow(_cursor, "categorySource");
          final int _cursorIndexOfChannel = CursorUtil.getColumnIndexOrThrow(_cursor, "channel");
          final int _cursorIndexOfRefNo = CursorUtil.getColumnIndexOrThrow(_cursor, "refNo");
          final int _cursorIndexOfBalanceAfter = CursorUtil.getColumnIndexOrThrow(_cursor, "balanceAfter");
          final int _cursorIndexOfCreditLimitAfter = CursorUtil.getColumnIndexOrThrow(_cursor, "creditLimitAfter");
          final int _cursorIndexOfTxnTime = CursorUtil.getColumnIndexOrThrow(_cursor, "txnTime");
          final int _cursorIndexOfRawBody = CursorUtil.getColumnIndexOrThrow(_cursor, "rawBody");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfIsExcluded = CursorUtil.getColumnIndexOrThrow(_cursor, "isExcluded");
          final int _cursorIndexOfExclusionReason = CursorUtil.getColumnIndexOrThrow(_cursor, "exclusionReason");
          final int _cursorIndexOfExclusionSource = CursorUtil.getColumnIndexOrThrow(_cursor, "exclusionSource");
          final int _cursorIndexOfExclusionRuleId = CursorUtil.getColumnIndexOrThrow(_cursor, "exclusionRuleId");
          final int _cursorIndexOfMatchedTransferId = CursorUtil.getColumnIndexOrThrow(_cursor, "matchedTransferId");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfIsManual = CursorUtil.getColumnIndexOrThrow(_cursor, "isManual");
          final int _cursorIndexOfIsInternational = CursorUtil.getColumnIndexOrThrow(_cursor, "isInternational");
          final int _cursorIndexOfLinkedOriginalTxnId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedOriginalTxnId");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpSmsId;
            if (_cursor.isNull(_cursorIndexOfSmsId)) {
              _tmpSmsId = null;
            } else {
              _tmpSmsId = _cursor.getLong(_cursorIndexOfSmsId);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final String _tmpSender;
            _tmpSender = _cursor.getString(_cursorIndexOfSender);
            final String _tmpBankName;
            _tmpBankName = _cursor.getString(_cursorIndexOfBankName);
            final String _tmpAccountLast4;
            if (_cursor.isNull(_cursorIndexOfAccountLast4)) {
              _tmpAccountLast4 = null;
            } else {
              _tmpAccountLast4 = _cursor.getString(_cursorIndexOfAccountLast4);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final TransactionType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __roomConverters.toTransactionType(_tmp);
            final String _tmpMerchant;
            _tmpMerchant = _cursor.getString(_cursorIndexOfMerchant);
            final String _tmpRawMerchant;
            _tmpRawMerchant = _cursor.getString(_cursorIndexOfRawMerchant);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final CategorySource _tmpCategorySource;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfCategorySource);
            _tmpCategorySource = __roomConverters.toCategorySource(_tmp_1);
            final TransactionChannel _tmpChannel;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfChannel);
            _tmpChannel = __roomConverters.toTransactionChannel(_tmp_2);
            final String _tmpRefNo;
            if (_cursor.isNull(_cursorIndexOfRefNo)) {
              _tmpRefNo = null;
            } else {
              _tmpRefNo = _cursor.getString(_cursorIndexOfRefNo);
            }
            final Double _tmpBalanceAfter;
            if (_cursor.isNull(_cursorIndexOfBalanceAfter)) {
              _tmpBalanceAfter = null;
            } else {
              _tmpBalanceAfter = _cursor.getDouble(_cursorIndexOfBalanceAfter);
            }
            final Double _tmpCreditLimitAfter;
            if (_cursor.isNull(_cursorIndexOfCreditLimitAfter)) {
              _tmpCreditLimitAfter = null;
            } else {
              _tmpCreditLimitAfter = _cursor.getDouble(_cursorIndexOfCreditLimitAfter);
            }
            final long _tmpTxnTime;
            _tmpTxnTime = _cursor.getLong(_cursorIndexOfTxnTime);
            final String _tmpRawBody;
            _tmpRawBody = _cursor.getString(_cursorIndexOfRawBody);
            final String _tmpFingerprint;
            _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            final boolean _tmpIsExcluded;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsExcluded);
            _tmpIsExcluded = _tmp_3 != 0;
            final ExclusionReason _tmpExclusionReason;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfExclusionReason)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfExclusionReason);
            }
            _tmpExclusionReason = __roomConverters.toExclusionReason(_tmp_4);
            final ExclusionSource _tmpExclusionSource;
            final String _tmp_5;
            _tmp_5 = _cursor.getString(_cursorIndexOfExclusionSource);
            _tmpExclusionSource = __roomConverters.toExclusionSource(_tmp_5);
            final Long _tmpExclusionRuleId;
            if (_cursor.isNull(_cursorIndexOfExclusionRuleId)) {
              _tmpExclusionRuleId = null;
            } else {
              _tmpExclusionRuleId = _cursor.getLong(_cursorIndexOfExclusionRuleId);
            }
            final Long _tmpMatchedTransferId;
            if (_cursor.isNull(_cursorIndexOfMatchedTransferId)) {
              _tmpMatchedTransferId = null;
            } else {
              _tmpMatchedTransferId = _cursor.getLong(_cursorIndexOfMatchedTransferId);
            }
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final boolean _tmpIsManual;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsManual);
            _tmpIsManual = _tmp_6 != 0;
            final boolean _tmpIsInternational;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsInternational);
            _tmpIsInternational = _tmp_7 != 0;
            final Long _tmpLinkedOriginalTxnId;
            if (_cursor.isNull(_cursorIndexOfLinkedOriginalTxnId)) {
              _tmpLinkedOriginalTxnId = null;
            } else {
              _tmpLinkedOriginalTxnId = _cursor.getLong(_cursorIndexOfLinkedOriginalTxnId);
            }
            _item = new TransactionEntity(_tmpId,_tmpSmsId,_tmpAccountId,_tmpSender,_tmpBankName,_tmpAccountLast4,_tmpAmount,_tmpCurrency,_tmpType,_tmpMerchant,_tmpRawMerchant,_tmpCategoryId,_tmpCategorySource,_tmpChannel,_tmpRefNo,_tmpBalanceAfter,_tmpCreditLimitAfter,_tmpTxnTime,_tmpRawBody,_tmpFingerprint,_tmpIsExcluded,_tmpExclusionReason,_tmpExclusionSource,_tmpExclusionRuleId,_tmpMatchedTransferId,_tmpNote,_tmpTags,_tmpIsManual,_tmpIsInternational,_tmpLinkedOriginalTxnId);
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

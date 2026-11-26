package com.banksms.expenses.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.banksms.expenses.data.model.ExpenseCategory;
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

  private final SharedSQLiteStatement __preparedStmtOfUpdateCategory;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public TransactionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTransactionEntity = new EntityInsertionAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `transactions` (`id`,`smsId`,`sender`,`bankName`,`accountLast4`,`amount`,`currency`,`type`,`merchant`,`category`,`balanceAfter`,`timestamp`,`rawBody`,`fingerprint`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
        statement.bindString(3, entity.getSender());
        statement.bindString(4, entity.getBankName());
        if (entity.getAccountLast4() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAccountLast4());
        }
        statement.bindDouble(6, entity.getAmount());
        statement.bindString(7, entity.getCurrency());
        final String _tmp = __roomConverters.fromTransactionType(entity.getType());
        statement.bindString(8, _tmp);
        statement.bindString(9, entity.getMerchant());
        final String _tmp_1 = __roomConverters.fromExpenseCategory(entity.getCategory());
        statement.bindString(10, _tmp_1);
        if (entity.getBalanceAfter() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getBalanceAfter());
        }
        statement.bindLong(12, entity.getTimestamp());
        statement.bindString(13, entity.getRawBody());
        statement.bindString(14, entity.getFingerprint());
      }
    };
    this.__preparedStmtOfUpdateCategory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE transactions SET category = ? WHERE id = ?";
        return _query;
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
  public Object updateCategory(final long id, final ExpenseCategory category,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateCategory.acquire();
        int _argIndex = 1;
        final String _tmp = __roomConverters.fromExpenseCategory(category);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 2;
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
          __preparedStmtOfUpdateCategory.release(_stmt);
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
    final String _sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSmsId = CursorUtil.getColumnIndexOrThrow(_cursor, "smsId");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfBankName = CursorUtil.getColumnIndexOrThrow(_cursor, "bankName");
          final int _cursorIndexOfAccountLast4 = CursorUtil.getColumnIndexOrThrow(_cursor, "accountLast4");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfMerchant = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfBalanceAfter = CursorUtil.getColumnIndexOrThrow(_cursor, "balanceAfter");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfRawBody = CursorUtil.getColumnIndexOrThrow(_cursor, "rawBody");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
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
            final ExpenseCategory _tmpCategory;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = __roomConverters.toExpenseCategory(_tmp_1);
            final Double _tmpBalanceAfter;
            if (_cursor.isNull(_cursorIndexOfBalanceAfter)) {
              _tmpBalanceAfter = null;
            } else {
              _tmpBalanceAfter = _cursor.getDouble(_cursorIndexOfBalanceAfter);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpRawBody;
            _tmpRawBody = _cursor.getString(_cursorIndexOfRawBody);
            final String _tmpFingerprint;
            _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            _item = new TransactionEntity(_tmpId,_tmpSmsId,_tmpSender,_tmpBankName,_tmpAccountLast4,_tmpAmount,_tmpCurrency,_tmpType,_tmpMerchant,_tmpCategory,_tmpBalanceAfter,_tmpTimestamp,_tmpRawBody,_tmpFingerprint);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

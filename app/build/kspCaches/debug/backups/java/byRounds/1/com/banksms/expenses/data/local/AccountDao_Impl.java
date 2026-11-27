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
import com.banksms.expenses.data.model.AccountEntity;
import com.banksms.expenses.data.model.AccountType;
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
public final class AccountDao_Impl implements AccountDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AccountEntity> __insertionAdapterOfAccountEntity;

  private final RoomConverters __roomConverters = new RoomConverters();

  private final EntityDeletionOrUpdateAdapter<AccountEntity> __updateAdapterOfAccountEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public AccountDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAccountEntity = new EntityInsertionAdapter<AccountEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `accounts` (`id`,`displayName`,`bankName`,`type`,`last4`,`lastBalance`,`creditLimit`,`statementDueDate`,`accentColorHex`,`isActive`,`mergedIntoAccountId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AccountEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDisplayName());
        statement.bindString(3, entity.getBankName());
        final String _tmp = __roomConverters.fromAccountType(entity.getType());
        statement.bindString(4, _tmp);
        if (entity.getLast4() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLast4());
        }
        if (entity.getLastBalance() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getLastBalance());
        }
        if (entity.getCreditLimit() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getCreditLimit());
        }
        if (entity.getStatementDueDate() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getStatementDueDate());
        }
        statement.bindLong(9, entity.getAccentColorHex());
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        if (entity.getMergedIntoAccountId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getMergedIntoAccountId());
        }
      }
    };
    this.__updateAdapterOfAccountEntity = new EntityDeletionOrUpdateAdapter<AccountEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `accounts` SET `id` = ?,`displayName` = ?,`bankName` = ?,`type` = ?,`last4` = ?,`lastBalance` = ?,`creditLimit` = ?,`statementDueDate` = ?,`accentColorHex` = ?,`isActive` = ?,`mergedIntoAccountId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AccountEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDisplayName());
        statement.bindString(3, entity.getBankName());
        final String _tmp = __roomConverters.fromAccountType(entity.getType());
        statement.bindString(4, _tmp);
        if (entity.getLast4() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLast4());
        }
        if (entity.getLastBalance() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getLastBalance());
        }
        if (entity.getCreditLimit() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getCreditLimit());
        }
        if (entity.getStatementDueDate() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getStatementDueDate());
        }
        statement.bindLong(9, entity.getAccentColorHex());
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        if (entity.getMergedIntoAccountId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getMergedIntoAccountId());
        }
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM accounts";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final AccountEntity account, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAccountEntity.insertAndReturnId(account);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final AccountEntity account, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAccountEntity.handle(account);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
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
  public Flow<List<AccountEntity>> observeAll() {
    final String _sql = "SELECT * FROM accounts ORDER BY bankName ASC, displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"accounts"}, new Callable<List<AccountEntity>>() {
      @Override
      @NonNull
      public List<AccountEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBankName = CursorUtil.getColumnIndexOrThrow(_cursor, "bankName");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfLast4 = CursorUtil.getColumnIndexOrThrow(_cursor, "last4");
          final int _cursorIndexOfLastBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "lastBalance");
          final int _cursorIndexOfCreditLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "creditLimit");
          final int _cursorIndexOfStatementDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "statementDueDate");
          final int _cursorIndexOfAccentColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "accentColorHex");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfMergedIntoAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "mergedIntoAccountId");
          final List<AccountEntity> _result = new ArrayList<AccountEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AccountEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpBankName;
            _tmpBankName = _cursor.getString(_cursorIndexOfBankName);
            final AccountType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __roomConverters.toAccountType(_tmp);
            final String _tmpLast4;
            if (_cursor.isNull(_cursorIndexOfLast4)) {
              _tmpLast4 = null;
            } else {
              _tmpLast4 = _cursor.getString(_cursorIndexOfLast4);
            }
            final Double _tmpLastBalance;
            if (_cursor.isNull(_cursorIndexOfLastBalance)) {
              _tmpLastBalance = null;
            } else {
              _tmpLastBalance = _cursor.getDouble(_cursorIndexOfLastBalance);
            }
            final Double _tmpCreditLimit;
            if (_cursor.isNull(_cursorIndexOfCreditLimit)) {
              _tmpCreditLimit = null;
            } else {
              _tmpCreditLimit = _cursor.getDouble(_cursorIndexOfCreditLimit);
            }
            final String _tmpStatementDueDate;
            if (_cursor.isNull(_cursorIndexOfStatementDueDate)) {
              _tmpStatementDueDate = null;
            } else {
              _tmpStatementDueDate = _cursor.getString(_cursorIndexOfStatementDueDate);
            }
            final long _tmpAccentColorHex;
            _tmpAccentColorHex = _cursor.getLong(_cursorIndexOfAccentColorHex);
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final Long _tmpMergedIntoAccountId;
            if (_cursor.isNull(_cursorIndexOfMergedIntoAccountId)) {
              _tmpMergedIntoAccountId = null;
            } else {
              _tmpMergedIntoAccountId = _cursor.getLong(_cursorIndexOfMergedIntoAccountId);
            }
            _item = new AccountEntity(_tmpId,_tmpDisplayName,_tmpBankName,_tmpType,_tmpLast4,_tmpLastBalance,_tmpCreditLimit,_tmpStatementDueDate,_tmpAccentColorHex,_tmpIsActive,_tmpMergedIntoAccountId);
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
  public Object getAll(final Continuation<? super List<AccountEntity>> $completion) {
    final String _sql = "SELECT * FROM accounts ORDER BY bankName ASC, displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AccountEntity>>() {
      @Override
      @NonNull
      public List<AccountEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBankName = CursorUtil.getColumnIndexOrThrow(_cursor, "bankName");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfLast4 = CursorUtil.getColumnIndexOrThrow(_cursor, "last4");
          final int _cursorIndexOfLastBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "lastBalance");
          final int _cursorIndexOfCreditLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "creditLimit");
          final int _cursorIndexOfStatementDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "statementDueDate");
          final int _cursorIndexOfAccentColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "accentColorHex");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfMergedIntoAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "mergedIntoAccountId");
          final List<AccountEntity> _result = new ArrayList<AccountEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AccountEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpBankName;
            _tmpBankName = _cursor.getString(_cursorIndexOfBankName);
            final AccountType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __roomConverters.toAccountType(_tmp);
            final String _tmpLast4;
            if (_cursor.isNull(_cursorIndexOfLast4)) {
              _tmpLast4 = null;
            } else {
              _tmpLast4 = _cursor.getString(_cursorIndexOfLast4);
            }
            final Double _tmpLastBalance;
            if (_cursor.isNull(_cursorIndexOfLastBalance)) {
              _tmpLastBalance = null;
            } else {
              _tmpLastBalance = _cursor.getDouble(_cursorIndexOfLastBalance);
            }
            final Double _tmpCreditLimit;
            if (_cursor.isNull(_cursorIndexOfCreditLimit)) {
              _tmpCreditLimit = null;
            } else {
              _tmpCreditLimit = _cursor.getDouble(_cursorIndexOfCreditLimit);
            }
            final String _tmpStatementDueDate;
            if (_cursor.isNull(_cursorIndexOfStatementDueDate)) {
              _tmpStatementDueDate = null;
            } else {
              _tmpStatementDueDate = _cursor.getString(_cursorIndexOfStatementDueDate);
            }
            final long _tmpAccentColorHex;
            _tmpAccentColorHex = _cursor.getLong(_cursorIndexOfAccentColorHex);
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final Long _tmpMergedIntoAccountId;
            if (_cursor.isNull(_cursorIndexOfMergedIntoAccountId)) {
              _tmpMergedIntoAccountId = null;
            } else {
              _tmpMergedIntoAccountId = _cursor.getLong(_cursorIndexOfMergedIntoAccountId);
            }
            _item = new AccountEntity(_tmpId,_tmpDisplayName,_tmpBankName,_tmpType,_tmpLast4,_tmpLastBalance,_tmpCreditLimit,_tmpStatementDueDate,_tmpAccentColorHex,_tmpIsActive,_tmpMergedIntoAccountId);
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

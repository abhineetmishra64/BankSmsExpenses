package com.banksms.expenses.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.banksms.expenses.data.model.ParseStatus;
import com.banksms.expenses.data.model.SmsRawEntity;
import java.lang.Class;
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
public final class SmsRawDao_Impl implements SmsRawDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SmsRawEntity> __insertionAdapterOfSmsRawEntity;

  private final RoomConverters __roomConverters = new RoomConverters();

  private final EntityDeletionOrUpdateAdapter<SmsRawEntity> __updateAdapterOfSmsRawEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public SmsRawDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSmsRawEntity = new EntityInsertionAdapter<SmsRawEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `sms_raw` (`id`,`sender`,`body`,`receivedAt`,`hash`,`parseStatus`,`confidence`,`failureReason`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SmsRawEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSender());
        statement.bindString(3, entity.getBody());
        statement.bindLong(4, entity.getReceivedAt());
        statement.bindString(5, entity.getHash());
        final String _tmp = __roomConverters.fromParseStatus(entity.getParseStatus());
        statement.bindString(6, _tmp);
        statement.bindDouble(7, entity.getConfidence());
        if (entity.getFailureReason() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getFailureReason());
        }
      }
    };
    this.__updateAdapterOfSmsRawEntity = new EntityDeletionOrUpdateAdapter<SmsRawEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sms_raw` SET `id` = ?,`sender` = ?,`body` = ?,`receivedAt` = ?,`hash` = ?,`parseStatus` = ?,`confidence` = ?,`failureReason` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SmsRawEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSender());
        statement.bindString(3, entity.getBody());
        statement.bindLong(4, entity.getReceivedAt());
        statement.bindString(5, entity.getHash());
        final String _tmp = __roomConverters.fromParseStatus(entity.getParseStatus());
        statement.bindString(6, _tmp);
        statement.bindDouble(7, entity.getConfidence());
        if (entity.getFailureReason() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getFailureReason());
        }
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sms_raw";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final SmsRawEntity sms, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSmsRawEntity.insertAndReturnId(sms);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<SmsRawEntity> smsList,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfSmsRawEntity.insertAndReturnIdsList(smsList);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final SmsRawEntity sms, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSmsRawEntity.handle(sms);
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
  public Flow<List<SmsRawEntity>> observeAll() {
    final String _sql = "SELECT * FROM sms_raw ORDER BY receivedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sms_raw"}, new Callable<List<SmsRawEntity>>() {
      @Override
      @NonNull
      public List<SmsRawEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfReceivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedAt");
          final int _cursorIndexOfHash = CursorUtil.getColumnIndexOrThrow(_cursor, "hash");
          final int _cursorIndexOfParseStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "parseStatus");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfFailureReason = CursorUtil.getColumnIndexOrThrow(_cursor, "failureReason");
          final List<SmsRawEntity> _result = new ArrayList<SmsRawEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SmsRawEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSender;
            _tmpSender = _cursor.getString(_cursorIndexOfSender);
            final String _tmpBody;
            _tmpBody = _cursor.getString(_cursorIndexOfBody);
            final long _tmpReceivedAt;
            _tmpReceivedAt = _cursor.getLong(_cursorIndexOfReceivedAt);
            final String _tmpHash;
            _tmpHash = _cursor.getString(_cursorIndexOfHash);
            final ParseStatus _tmpParseStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfParseStatus);
            _tmpParseStatus = __roomConverters.toParseStatus(_tmp);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final String _tmpFailureReason;
            if (_cursor.isNull(_cursorIndexOfFailureReason)) {
              _tmpFailureReason = null;
            } else {
              _tmpFailureReason = _cursor.getString(_cursorIndexOfFailureReason);
            }
            _item = new SmsRawEntity(_tmpId,_tmpSender,_tmpBody,_tmpReceivedAt,_tmpHash,_tmpParseStatus,_tmpConfidence,_tmpFailureReason);
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
  public Object getAll(final Continuation<? super List<SmsRawEntity>> $completion) {
    final String _sql = "SELECT * FROM sms_raw ORDER BY receivedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SmsRawEntity>>() {
      @Override
      @NonNull
      public List<SmsRawEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfReceivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedAt");
          final int _cursorIndexOfHash = CursorUtil.getColumnIndexOrThrow(_cursor, "hash");
          final int _cursorIndexOfParseStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "parseStatus");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfFailureReason = CursorUtil.getColumnIndexOrThrow(_cursor, "failureReason");
          final List<SmsRawEntity> _result = new ArrayList<SmsRawEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SmsRawEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSender;
            _tmpSender = _cursor.getString(_cursorIndexOfSender);
            final String _tmpBody;
            _tmpBody = _cursor.getString(_cursorIndexOfBody);
            final long _tmpReceivedAt;
            _tmpReceivedAt = _cursor.getLong(_cursorIndexOfReceivedAt);
            final String _tmpHash;
            _tmpHash = _cursor.getString(_cursorIndexOfHash);
            final ParseStatus _tmpParseStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfParseStatus);
            _tmpParseStatus = __roomConverters.toParseStatus(_tmp);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final String _tmpFailureReason;
            if (_cursor.isNull(_cursorIndexOfFailureReason)) {
              _tmpFailureReason = null;
            } else {
              _tmpFailureReason = _cursor.getString(_cursorIndexOfFailureReason);
            }
            _item = new SmsRawEntity(_tmpId,_tmpSender,_tmpBody,_tmpReceivedAt,_tmpHash,_tmpParseStatus,_tmpConfidence,_tmpFailureReason);
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
  public Object getByHash(final String hash, final Continuation<? super SmsRawEntity> $completion) {
    final String _sql = "SELECT * FROM sms_raw WHERE hash = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, hash);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SmsRawEntity>() {
      @Override
      @Nullable
      public SmsRawEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSender = CursorUtil.getColumnIndexOrThrow(_cursor, "sender");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfReceivedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "receivedAt");
          final int _cursorIndexOfHash = CursorUtil.getColumnIndexOrThrow(_cursor, "hash");
          final int _cursorIndexOfParseStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "parseStatus");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfFailureReason = CursorUtil.getColumnIndexOrThrow(_cursor, "failureReason");
          final SmsRawEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSender;
            _tmpSender = _cursor.getString(_cursorIndexOfSender);
            final String _tmpBody;
            _tmpBody = _cursor.getString(_cursorIndexOfBody);
            final long _tmpReceivedAt;
            _tmpReceivedAt = _cursor.getLong(_cursorIndexOfReceivedAt);
            final String _tmpHash;
            _tmpHash = _cursor.getString(_cursorIndexOfHash);
            final ParseStatus _tmpParseStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfParseStatus);
            _tmpParseStatus = __roomConverters.toParseStatus(_tmp);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            final String _tmpFailureReason;
            if (_cursor.isNull(_cursorIndexOfFailureReason)) {
              _tmpFailureReason = null;
            } else {
              _tmpFailureReason = _cursor.getString(_cursorIndexOfFailureReason);
            }
            _result = new SmsRawEntity(_tmpId,_tmpSender,_tmpBody,_tmpReceivedAt,_tmpHash,_tmpParseStatus,_tmpConfidence,_tmpFailureReason);
          } else {
            _result = null;
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

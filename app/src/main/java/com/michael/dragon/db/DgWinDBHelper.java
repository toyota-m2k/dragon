package com.michael.dragon.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

/**
 * @author M.TOYOTA Created on 2013/09/08 18:09
 */
public class DgWinDBHelper {
    SQLiteDatabase  mAndroid;
    SQLiteDatabase  mWin = null;
    long mBID = -1;
    boolean mUpdating = false;      // true: 既存DBの更新 / false:新規インぽーど
    ContentValues mValues = null;

    public DgWinDBHelper(SQLiteDatabase android ) {
        mAndroid = android;
    }

    public void close() {
        if( null!=mAndroid ) {
            mAndroid.close();
        }
        if( null!=mWin) {
            mWin.close();
        }
    }

    private final String[] SELECT_ID = {DgDBHelper.FIELD_ID};

    public void importFrom(SQLiteDatabase win) {
        mWin = win;
        mValues = new ContentValues();

        mAndroid.beginTransaction();
        try {
            importBookInfo();
            importQuestions();
            importNotes();
            importRecords();

            // 問題数を登録
            Cursor c = mAndroid.query(DgDBHelper.TABLE_QUESTION, SELECT_ID, null, null, null, null, null );
            int qcount = c.getCount();
            c.close();
            mValues.clear();
            mValues.put(DgDBHelper.FIELD_Q_COUNT, qcount);
            String where = DgDBHelper.FIELD_ID + "=" + Long.toString(mBID);
            mAndroid.update(DgDBHelper.TABLE_BOOK, mValues, where, null);

            mAndroid.setTransactionSuccessful();
        } finally {
            mAndroid.endTransaction();
        }

    }


    private long getBookID(String guid) {
        final String where = DgDBHelper.FIELD_GUID + "=?";
        final String[] whereArg = {guid};
        Cursor c = mAndroid.query(DgDBHelper.TABLE_BOOK, SELECT_ID, where, whereArg,null, null, null );
        try {
            c.moveToFirst();
            return c.getLong(0);
        } finally {
            c.close();
        }
    }

    private long getQuestionID(long winid) {
        final String where = DgDBHelper.FIELD_BID + "=? AND " + DgDBHelper.FIELD_WINID + "=?";
        final String[] whereArg = {Long.toString(mBID), Long.toString(winid)};
        Cursor c = mAndroid.query(DgDBHelper.TABLE_QUESTION, SELECT_ID, where, whereArg, null, null, null );
        try {
            c.moveToFirst();
            return c.getLong(0);
        } finally {
            c.close();
        }
    }

    private void importBookInfo() {
        Cursor c = mWin.query(DgDBHelper.TABLE_EXT, null, null, null, null, null, null);
        String guid = null;
        try {
            mValues.clear();
            int iName = c.getColumnIndexOrThrow(DgDBHelper.FIELD_NAME);
            int iValue = c.getColumnIndexOrThrow(DgDBHelper.FIELD_VALUE);
            for( c.moveToFirst(); !c.isAfterLast() ; c.moveToNext()) {
                String name = c.getString(iName);
                if( name.equals("guid") ) {
                    guid = c.getString((iValue));
                    mValues.put( DgDBHelper.FIELD_GUID, guid);
                } else if( name.equals("subject")) {
                    mValues.put( DgDBHelper.FIELD_SUBJECT, c.getString(iValue));
                } else if ( name.equals("username")) {
                    mValues.put( DgDBHelper.FIELD_USERNAME, c.getString(iValue));
                }
            }
        } finally {
            c.close();
        }
        Date now = new Date();
        mValues.put(DgDBHelper.FIELD_LAST_ACCESS, now.getTime());
        mBID = mAndroid.insert(DgDBHelper.TABLE_BOOK, null, mValues);
        if( mBID == -1 ) {
            // 既存データにマージする
            mBID = getBookID(guid);
            mUpdating = true;
        }
    }

    private void importQuestions() {
        String orderBy = DgDBHelper.FIELD_QID + " ASC";
        Cursor c = mWin.query(DgDBHelper.TABLE_QUESTION, null, null, null, null, null,orderBy);
        int conflictAlgorithm = ( mUpdating ) ? SQLiteDatabase.CONFLICT_REPLACE : SQLiteDatabase.CONFLICT_NONE;
        try {
            for( c.moveToFirst(); !c.isAfterLast() ; c.moveToNext()) {
                mValues.clear();
                mValues.put(DgDBHelper.FIELD_BID, mBID);
                for(int i=0, ci=c.getColumnCount(); i<ci; i++ ) {
                    String name = c.getColumnName(i);
                    if( name.equals(DgDBHelper.FIELD_ID)) {
                        continue;
                    } else if( name.equals(DgDBHelper.FIELD_QID)) {
                        mValues.put(DgDBHelper.FIELD_WINID, c.getLong(i));
                    } else if( name.equals(DgDBHelper.FIELD_SUBJECT) ||
                                name.equals(DgDBHelper.FIELD_A_TEXT)  ||
                                name.equals(DgDBHelper.FIELD_Q_TEXT) ) {
                        mValues.put(name, c.getString(i));
                    } else {
                        mValues.put(name, c.getBlob(i));
                    }
                }
                mAndroid.insertWithOnConflict(DgDBHelper.TABLE_QUESTION, null, mValues, conflictAlgorithm);
            }
        } finally {
            c.close();
        }
    }

    private void importNotes() {
        Cursor c = mWin.query(DgDBHelper.TABLE_NOTE, null, null, null, null, null,null);
        int conflictAlgorithm = ( mUpdating ) ? SQLiteDatabase.CONFLICT_REPLACE : SQLiteDatabase.CONFLICT_NONE;
        try {
            for( c.moveToFirst(); !c.isAfterLast() ; c.moveToNext()) {
                mValues.clear();
                for(int i=0, ci=c.getColumnCount(); i<ci; i++ ) {
                    String name = c.getColumnName(i);
                    if( name.equals(DgDBHelper.FIELD_ID)) {
                        mValues.put(DgDBHelper.FIELD_WINID, c.getLong(i));           // マージ時の重複回避用に、WinDB側の _id を winidとして保持しておく。
                    } else if( name.equals(DgDBHelper.FIELD_QID)) {
                        mValues.put(DgDBHelper.FIELD_QID, getQuestionID(c.getLong(i)));
                    } else if( name.equals(DgDBHelper.FIELD_SUBJECT) ||
                            name.equals(DgDBHelper.FIELD_N_TEXT)  ) {
                        mValues.put(name, c.getString(i));
                    } else if( name.equals(DgDBHelper.FIELD_MARK)) {
                        mValues.put(name, c.getInt(i));
                    } else {
                        mValues.put(name, c.getBlob(i));
                    }
                }
                mAndroid.insertWithOnConflict(DgDBHelper.TABLE_NOTE, null, mValues, conflictAlgorithm);
            }
        } finally {
            c.close();
        }

    }

    private void importRecords() {
        if( mUpdating ) {
            return;     // レコードはマージしない
        }
        Cursor c = mWin.query(DgDBHelper.TABLE_RECORD, null, null, null, null, null,null);
        try {
            for( c.moveToFirst(); !c.isAfterLast() ; c.moveToNext()) {
                mValues.clear();
                for(int i=0, ci=c.getColumnCount(); i<ci; i++ ) {
                    String name = c.getColumnName(i);
                    if( name.equals(DgDBHelper.FIELD_ID)) {
                        continue;
                    } else if( name.equals(DgDBHelper.FIELD_QID)) {
                        mValues.put(DgDBHelper.FIELD_QID, getQuestionID(c.getLong(i)));
                    } else {
                        mValues.put(name, c.getInt(i));
                    }
                }
                mAndroid.insert(DgDBHelper.TABLE_NOTE, null, mValues);
            }
        } finally {
            c.close();
        }

    }
}

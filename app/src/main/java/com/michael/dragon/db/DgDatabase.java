package com.michael.dragon.db;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.michael.dragon.ui.UiMessageBox;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author M.TOYOTA Created on 2013/09/08 15:22
 */
public class DgDatabase {
    private DgDBHelper mDBHelper;

    private DgDatabase(Context context) {
        mDBHelper = new DgDBHelper(context);
    }

    private static int sRef = 0;
    private static DgDatabase sInstance = null;
    public static void initialize(Context context) {
        if( null == sInstance) {
            sInstance = new DgDatabase(context);
        }
        sRef++;
    }
    public static void terminate() {
        sRef--;
        if( sRef==0 ) {
            sInstance.mDBHelper.close();
            sInstance = null;
        }
    }
    public static DgDatabase getInstance() {
        return sInstance;
    }

    private static final String[] COL_ID = { DgDBHelper.FIELD_ID };
    private static final String[] COL_QID = { DgDBHelper.FIELD_QID };
    private static final String ORDER_LAST_ACCESS = DgDBHelper.FIELD_LAST_ACCESS + " ASC";

//    public Cursor getBooks() {
//        SQLiteDatabase db = mDBHelper.getReadableDatabase();
//        return db.query(DgDBHelper.TABLE_BOOK, null, null,null, null, null, ORDER_LAST_ACCESS  );
//    }

    public long[] getBookIdList() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = db.query(DgDBHelper.TABLE_BOOK, COL_ID, null,null, null, null, ORDER_LAST_ACCESS  );
        try {
            int count = c.getCount();
            if( count <=0 ){
                return null;
            }
            int n = 0;
            long[] result = new long[count];
            for( c.moveToFirst() ; !c.isAfterLast() ; c.moveToNext(), n++ ) {
                result[n] = c.getLong(0);
            }
            return result;
        } finally {
            c.close();
            db.close();
        }
    }

    public boolean getBook(long bid, DgBook out) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String where = DgDBHelper.FIELD_ID + "=" + Long.toString(bid);
        Cursor c = db.query(DgDBHelper.TABLE_BOOK, null, where, null, null, null, null);
        try {
            if( c.getCount()==1 ) {
                c.moveToFirst();
                out.setCursor(c);
                return true;
            }
            return false;
        } finally {
            c.close();
            db.close();
        }
    }

    public void touchBook(long bid){
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            String where = DgDBHelper.FIELD_ID + "=" + Long.toString(bid);
            mWorkValues.clear();
            mWorkValues.put(DgDBHelper.FIELD_LAST_ACCESS, new Date().getTime());
            db.update(DgDBHelper.TABLE_BOOK, mWorkValues, where, null);
        } finally {
            db.close();
        }
    }

    public long[] getQidList(long bid) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String where = DgDBHelper.FIELD_BID + "=" + Long.toString(bid);
        String orderby = DgDBHelper.FIELD_ID + " ASC";
        Cursor c = db.query(DgDBHelper.TABLE_QUESTION, COL_ID, where, null, null, null, orderby);
        try {
            int count =  c.getCount();
            if( count <=0 ) {
                return null;
            }
            int n=0;
            long[] result = new long[count];
            for( c.moveToFirst() ; !c.isAfterLast() ; c.moveToNext(), n++ ) {
                result[n] = c.getLong(0);
            }
            return result;
        } finally {
            c.close();
            db.close();
        }
    }

    public boolean getQuestion(long qid, DgQuestion out) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String where = DgDBHelper.FIELD_ID + "=" + Long.toString(qid);
        Cursor c = db.query(DgDBHelper.TABLE_QUESTION, null, where, null, null, null, null);
        try {
            if( c.getCount()==1 ) {
                c.moveToFirst();
                out.setCursor(c);
                return true;
            }
            return false;
        } finally {
            c.close();
            db.close();
        }
    }

    public long[] getNotes(long qid) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String where = null;
        if( qid != -1 ) {
            where = DgDBHelper.FIELD_QID + "=" + Long.toString(qid);
        }
        Cursor c = db.query(DgDBHelper.TABLE_NOTE, COL_ID, where, null, null, null, null);
        try {
            int count = c.getCount();
            if( count<=0 ) {
                return null;
            }
            int n=0;
            long[] notes = new long[count];
            for( c.moveToFirst() ; !c.isAfterLast() ; c.moveToNext(), n++ ) {
                notes[n] = c.getLong(0);
            }
            return notes;
        } finally {
            c.close();
            db.close();
        }
    }

    public long[] getAllNotes() {
        return getNotes(-1);
    }

    public boolean getNote(long noteid, DgNote out ) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String where = DgDBHelper.FIELD_ID + "=" + Long.toString(noteid);
        Cursor c = db.query(DgDBHelper.TABLE_NOTE, null, where, null, null, null, null);
        try {
            int count = c.getCount();
            if( count<=0 ) {
                return false;
            }
            c.moveToFirst();
            out.setCursor(c);
            return true;
        } finally {
            c.close();
            db.close();
        }
    }

//    public DgNote[] getNotes(long qid ) {
//        SQLiteDatabase db = mDBHelper.getReadableDatabase();
//        String where = DgDBHelper.FIELD_QID + "=" + Long.toString(qid);
//        Cursor c = db.query(DgDBHelper.TABLE_NOTE, null, where, null, null, null, null);
//        try {
//            int count = c.getCount();
//            if( count<=0 ) {
//                return null;
//            }
//            int n=0;
//            DgNote[] notes = new DgNote[count];
//            for( c.moveToFirst() ; !c.isAfterLast() ; c.moveToNext(), n++ ) {
//                notes[n] = new DgNote(c);
//            }
//            return notes;
//        } finally {
//            c.close();
//            db.close();
//        }
//    }

    public boolean getRecord(long qid, boolean reverse, DgRecord out) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String where = DgDBHelper.FIELD_QID + "=" + Long.toString(qid);
        Cursor c = db.query(DgDBHelper.TABLE_RECORD, null, where, null, null, null, null);
        try {
            if( c.getCount()==1 ) {
                c.moveToFirst();
                out.setCursor(c, reverse);
                return true;
            }
            return false;
        } finally {
            c.close();
            db.close();
        }
    }

    private DgRecord mWorkRecord = new DgRecord();
    private ContentValues mWorkValues = new ContentValues();
    public void setRecordResult(long qid, boolean reverse, boolean isOk) {
        int ok = 0, ng = 0;
        if( getRecord(qid, reverse, mWorkRecord) ) {
            ok = mWorkRecord.getOk();
            ng = mWorkRecord.getNg();
        }
        if( isOk ) {
            ok++;
        } else {
            ng++;
        }

        int recent = (isOk) ? 1:0;
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            mWorkValues.clear();
            if( !reverse) {
                mWorkValues.put(DgDBHelper.FIELD_OK, ok );
                mWorkValues.put(DgDBHelper.FIELD_NG, ng );
                mWorkValues.put(DgDBHelper.FIELD_RECENT, recent);
            } else {
                mWorkValues.put(DgDBHelper.FIELD_REV_OK, ok );
                mWorkValues.put(DgDBHelper.FIELD_REV_NG, ng );
                mWorkValues.put(DgDBHelper.FIELD_REV_RECENT, recent);
            }

            String where = DgDBHelper.FIELD_QID + "=" + Long.toString(qid);
            int affected = db.update(DgDBHelper.TABLE_RECORD, mWorkValues, where, null);
            if( affected == 0 ){
                // new record
                mWorkValues.put(DgDBHelper.FIELD_QID, qid);
                db.insert(DgDBHelper.TABLE_RECORD, null, mWorkValues);
            }
        } finally {
            db.close();
        }
    }

    public boolean setNoteMark(long noteid, int mark) {
        if( mark<0 || DgDBHelper.Mark.EXTRA_VERY_IMPORTANT<mark) {
            return false;
        }

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            mWorkValues.clear();
            mWorkValues.put(DgDBHelper.FIELD_MARK, mark);
            String where = DgDBHelper.FIELD_ID + "=" + Long.toString(noteid);
            return 1 == db.update(DgDBHelper.TABLE_NOTE, mWorkValues, where, null);
        } finally {
            db.close();
        }
    }



    public void setRecordMark(long qid, boolean reverse, int mark ) {
        if( mark<0 || DgDBHelper.Mark.EXTRA_VERY_IMPORTANT<mark) {
            return;
        }

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            mWorkValues.clear();
            mWorkValues.put(DgDBHelper.FIELD_QID, qid);
            if (!reverse) {
                mWorkValues.put(DgDBHelper.FIELD_MARK, mark);
            } else {
                mWorkValues.put(DgDBHelper.FIELD_REV_MARK, mark);
            }

            String where = DgDBHelper.FIELD_QID + "=" + Long.toString(qid);
            int affected = db.update(DgDBHelper.TABLE_RECORD, mWorkValues, where, null);
            if (affected == 0) {
                // new record
                mWorkValues.put(DgDBHelper.FIELD_QID, qid);
                db.insert(DgDBHelper.TABLE_RECORD, null, mWorkValues);
            }
        } finally {
            db.close();
        }
    }

    public int getRecordMark(long qid, boolean reverse) {
        if( !getRecord(qid, reverse, mWorkRecord) ) {
            return DgDBHelper.Mark.NORMAL;
        }
        return mWorkRecord.getMark();
    }

    public static class RecordStatistics {
        public int ng;
        public int ok;
        public int recent;

        public RecordStatistics() {
            clear();
        }

        public void clear() {
            ng = 0;
            ok = 0;
            recent = 0;
        }
    }


    public int getRecordStatistics(long start, long end, boolean reverse, RecordStatistics out) {
        out.clear();

        SQLiteDatabase db = null;
        Cursor c = null;
        String where = DgDBHelper.FIELD_QID + " BETWEEN " + Long.toString(start) + " AND " + Long.toString(end);
        try {
            db = mDBHelper.getReadableDatabase();
            c = db.query(DgDBHelper.TABLE_RECORD, null, where, null, null, null, null );
            c.moveToFirst();

            int idxOk = c.getColumnIndexOrThrow(reverse ? DgDBHelper.FIELD_REV_OK : DgDBHelper.FIELD_OK);
            int idxNg = c.getColumnIndexOrThrow(reverse ? DgDBHelper.FIELD_REV_NG : DgDBHelper.FIELD_NG);
            int idxRecent = c.getColumnIndexOrThrow(reverse ? DgDBHelper.FIELD_REV_RECENT : DgDBHelper.FIELD_RECENT);

            while(!c.isAfterLast()) {
                out.ok += c.getInt(idxOk);
                out.ng += c.getInt(idxNg);
                if( c.getInt(idxRecent) == 1 ) {
                    out.recent ++;
                }
                c.moveToNext();
            }
            return c.getCount();

        } finally {
            if( c != null ) {
                c.close();
            }
            if( null != db ) {
                db.close();
            }
        }

    }


    public boolean importFrom(Activity activity, File winFile) {
        DgWinDBHelper helper = new DgWinDBHelper(mDBHelper.getWritableDatabase());
        try {
            helper.importFrom(SQLiteDatabase.openDatabase(winFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY));
            return true;

        } catch(Throwable e) {
            UiMessageBox.confirm(activity, "error", e.getMessage(), null);
            e.printStackTrace();
            return false;
        } finally {
            helper.close();
        }
    }

    public int getQuestionCount(long bid) {
        DgBook book = new DgBook();
        if( getBook(bid, book) ) {
            return book.getQuestionCount();
        }
        return 0;
    }
}

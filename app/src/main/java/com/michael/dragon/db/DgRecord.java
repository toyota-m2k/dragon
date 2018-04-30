package com.michael.dragon.db;

import android.database.Cursor;

/**
 * @author M.TOYOTA Created on 2013/09/08 17:06
 */
public class DgRecord {

    long mID;
    long mQID;
    boolean mReverse = false;

    int mMark;
    int mOk;
    int mNg;
    int mRecent;

    public DgRecord() {
    }
    public DgRecord(Cursor c, boolean reverse) {
        setCursor(c, reverse);
    }
    public static long getID(Cursor c) {
        return c.getLong( c.getColumnIndexOrThrow(DgDBHelper.FIELD_ID) );
    }
    public long getID() {
        return mID;
    }


    public static long getQid(Cursor c) {
        return c.getLong( c.getColumnIndexOrThrow(DgDBHelper.FIELD_QID) );
    }
    public long getQid() {
        return mQID;
    }

    public static int getMark(Cursor c, boolean reverse) {
        String field = (reverse) ? DgDBHelper.FIELD_REV_MARK : DgDBHelper.FIELD_MARK;
        return c.getInt( c.getColumnIndexOrThrow(field) );
    }
    public int getMark() {
        return mMark;
    }

    public static int getOk(Cursor c, boolean reverse) {
        String field = (reverse) ? DgDBHelper.FIELD_REV_OK : DgDBHelper.FIELD_OK;
        return c.getInt( c.getColumnIndexOrThrow(field) );
    }
    public int getOk() {
        return mOk;
    }

    public static int getNg(Cursor c, boolean reverse) {
        String field = (reverse) ? DgDBHelper.FIELD_REV_NG : DgDBHelper.FIELD_NG;
        return c.getInt( c.getColumnIndexOrThrow(field) );
    }
    public int getNg() {
        return mNg;
    }

    public static int getRecent(Cursor c, boolean reverse) {
        String field = (reverse) ? DgDBHelper.FIELD_REV_RECENT : DgDBHelper.FIELD_RECENT;
        return c.getInt( c.getColumnIndexOrThrow(field) );
    }
    public int getRecent() {
        return mRecent;
    }

    public void setCursor(Cursor c, boolean reverse) {
        mReverse = reverse;
        mID = getID(c);
        mQID = getQid(c);
        mMark = getMark(c, reverse);
        mNg = getNg(c,reverse);
        mOk = getOk(c,reverse);
        mRecent = getRecent(c,reverse);
    }
    public boolean ismReverse() {
        return mReverse;
    }
}

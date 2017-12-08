package com.michael.dragon.db;

import android.database.Cursor;

/**
 * @author M.TOYOTA Created on 2013/09/08 15:32
 */
public class DgQuestion {
    private long mID;
    private long mBID;
    private long mWinID;
    private String mSubject;
    private String mQText;
    private String mAText;

    public DgQuestion() {
    }
    public DgQuestion(Cursor c) {
        setCursor(c);
    }
    public static long getID(Cursor c) {
        return c.getLong( c.getColumnIndexOrThrow(DgDBHelper.FIELD_ID) );
    }
    public long getID() {
        return mID;
    }


    public static long getWinID(Cursor c) {
        return c.getLong( c.getColumnIndexOrThrow(DgDBHelper.FIELD_WINID) );
    }
    public long getWinID() {
        return mWinID;
    }

    public static long getBid(Cursor c) {
        return c.getLong( c.getColumnIndexOrThrow(DgDBHelper.FIELD_BID) );
    }
    public long getBid() {
        return mBID;
    }

    public static String getSubject(Cursor c) {
        return c.getString( c.getColumnIndexOrThrow(DgDBHelper.FIELD_SUBJECT) );
    }
    public String getSubject() {
        return mSubject;
    }

    public static String getQText(Cursor c) {
        return c.getString( c.getColumnIndexOrThrow(DgDBHelper.FIELD_Q_TEXT) );
    }
    public String getQText() {
        return mQText;
    }

    public static String getAText(Cursor c) {
        return c.getString( c.getColumnIndexOrThrow(DgDBHelper.FIELD_A_TEXT) );
    }
    public String getAText() {
        return mAText;
    }

    public void setCursor(Cursor c) {
        mID = getID(c);
        mBID = getBid(c);
        mWinID = getWinID(c);
        mSubject = getSubject(c);
        mQText = getQText(c);
        mAText = getAText(c);
    }
}

package com.michael.dragon.db;

import android.database.Cursor;

/**
 * @author M.TOYOTA Created on 2013/09/08 16:22
 */
public class DgBook {
    private long mID = -1;
    private String mSubject = null;
    private String mUsername = null;
    private String mGuid = null;
    private long mLastQid = 0;
    private int mQCount = 0;
    private long mLastAccessed = 0;

    public DgBook() {
    }
    public DgBook(Cursor c) {
        setCursor(c);
    }

    public static long getID(Cursor c) {
        return c.getLong( c.getColumnIndexOrThrow(DgDBHelper.FIELD_ID) );
    }
    public long getID() {
        return mID;
    }

    public static String getSubject(Cursor c) {
        return c.getString( c.getColumnIndexOrThrow(DgDBHelper.FIELD_SUBJECT) );
    }
    public String getSubject() {
        return mSubject;
    }

    public static String getGuid(Cursor c) {
        return c.getString( c.getColumnIndexOrThrow(DgDBHelper.FIELD_GUID) );
    }
    public String getGuid() {
        return mGuid;
    }

    public static String getUsername(Cursor c) {
        return c.getString( c.getColumnIndexOrThrow(DgDBHelper.FIELD_USERNAME) );
    }
    public String getUsername() {
        return mUsername;
    }

    public static long getLastQid(Cursor c) {
        return c.getLong( c.getColumnIndexOrThrow(DgDBHelper.FIELD_LAST_QID) );
    }
    public long getLastQid() {
        return mLastQid;
    }

    public static long getLastAccessed(Cursor c) {
        return c.getLong( c.getColumnIndexOrThrow(DgDBHelper.FIELD_LAST_ACCESS) );
    }
    public long getLastAccessed() {
        return mLastAccessed;
    }

    public static int getQuestionCount(Cursor c ) {
        return c.getInt( c.getColumnIndexOrThrow(DgDBHelper.FIELD_Q_COUNT) );
    }
    public int getQuestionCount() {
        return mQCount;
    }

    public void setCursor(Cursor c) {
        mID = getID(c);
        mGuid = getGuid(c);
        mSubject = getSubject(c);
        mUsername = getUsername(c);
        mLastQid = getLastQid(c);
        mLastAccessed = getLastAccessed(c);
        mQCount = getQuestionCount(c);
    }
}

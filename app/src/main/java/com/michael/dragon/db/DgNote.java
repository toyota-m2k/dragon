package com.michael.dragon.db;

import android.database.Cursor;

/**
 * @author M.TOYOTA Created on 2013/09/08 17:00
 */
public class DgNote {
    private long mID;
    private long mQID;
    private int mMark;
    private String mSubject;
    private String mText;

    public DgNote() {
    }
    public DgNote(Cursor c) {
        setCursor(c);
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


    public static int getMark(Cursor c) {
        return c.getInt( c.getColumnIndexOrThrow(DgDBHelper.FIELD_MARK) );
    }
    public int getMark() {
        return mMark;
    }

    public static String getSubject(Cursor c) {
        return c.getString( c.getColumnIndexOrThrow(DgDBHelper.FIELD_SUBJECT) );
    }
    public String getSubject() {
        return mSubject;
    }

    public static String getText(Cursor c) {
        return c.getString( c.getColumnIndexOrThrow(DgDBHelper.FIELD_N_TEXT) );
    }
    public String getText() {
        return mText;
    }

    public void setCursor(Cursor c){
        mID = getID(c);
        mQID = getQid(c);
        mSubject = getSubject(c);
        mMark = getMark(c);
        mText = getText(c);
    }
}

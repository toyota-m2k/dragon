package com.michael.dragon.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author M.TOYOTA Created on 2013/09/08 15:22
 */
public class DgDBHelper extends SQLiteOpenHelper {
    public static final String TABLE_EXT = "ext_table";
    public static final String TABLE_BOOK = "book_table";
    public static final String TABLE_QUESTION = "question_table";
    public static final String TABLE_NOTE = "note_table";
    public static final String TABLE_RECORD = "record_table";
    public static final String FIELD_ID = "_id";
    public static final String FIELD_WINID = "winid";
    public static final String FIELD_QID = "qid";
    public static final String FIELD_BID = "bid";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_VALUE = "value";
    public static final String FIELD_GUID = "guid";
    public static final String FIELD_SUBJECT = "subject";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_LAST_QID = "last_qid";
    public static final String FIELD_Q_TEXT = "q_text";
    public static final String FIELD_A_TEXT = "a_text";
    public static final String FIELD_N_TEXT = "n_text";
    public static final String FIELD_Q_VOICE = "q_voice";
    public static final String FIELD_A_VOICE = "a_voice";
    public static final String FIELD_N_VOICE = "n_voice";
    public static final String FIELD_Q_IMAGE = "q_image";
    public static final String FIELD_A_IMAGE = "a_image";
    public static final String FIELD_N_IMAGE = "n_image";
    public static final String FIELD_MARK = "mark";
    public static final String FIELD_NG = "ng";
    public static final String FIELD_OK = "ok";
    public static final String FIELD_RECENT = "recent";
    public static final String FIELD_REV_MARK = "rev_mark";
    public static final String FIELD_REV_NG = "rev_ng";
    public static final String FIELD_REV_OK = "rev_ok";
    public static final String FIELD_REV_RECENT = "rev_recent";
    public static final String FIELD_LAST_ACCESS = "accessed";
    public static final String FIELD_Q_COUNT = "q_count";


    static final String SQL_CreateExtTable =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EXT + " ("
                    + FIELD_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FIELD_NAME        + " TEXT UNIQUE NOT NULL,"
                    + FIELD_VALUE       + " TEXT NOT NULL"
                    + ")";

    static final String SQL_CreateBookTable =
            "CREATE TABLE IF NOT EXISTS " + TABLE_BOOK  + " ("
                    + FIELD_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FIELD_GUID        + " TEXT UNIQUE NOT NULL,"
                    + FIELD_SUBJECT    + " TEXT NOT NULL,"
                    + FIELD_USERNAME     + " TEXT,"
                    + FIELD_LAST_ACCESS + " INTEGER,"
                    + FIELD_LAST_QID   + " INTEGER,"
                    + FIELD_Q_COUNT     + " INTEGER"
                    + ")";

    static final String SQL_CreateQuestionTable =
            "CREATE TABLE IF NOT EXISTS " + TABLE_QUESTION + " ("
                    + FIELD_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FIELD_BID         + " INTEGER NOT NULL,"
                    + FIELD_WINID       + " INTEGER,"      // Windows版とのリンケージ用
                    + FIELD_SUBJECT     + " TEXT NOT NULL,"
                    + FIELD_Q_TEXT      + " TEXT NOT NULL,"
                    + FIELD_Q_VOICE     + " BLOB,"
                    + FIELD_Q_IMAGE     + " BLOB,"
                    + FIELD_A_TEXT      + " TEXT NOT NULL,"
                    + FIELD_A_VOICE     + " BLOB,"
                    + FIELD_A_IMAGE     + " BLOB,"
                    + "	 FOREIGN KEY (" + FIELD_BID + ") REFERENCES " + TABLE_BOOK + "(" + FIELD_ID + "),"
                    + "  UNIQUE(" + FIELD_BID + "," + FIELD_WINID +")"
                    + ")";

    static final String SQL_CreateNoteTable =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NOTE +  "( "
                    + FIELD_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FIELD_QID         + " INTEGER NOT NULL,"
                    + FIELD_WINID       + " INTEGER UNIQUE,"      // Windows版とのリンケージ用
                    + FIELD_SUBJECT     + " TEXT NOT NULL,"
                    + FIELD_MARK        + " INTEGER DEFAULT 0,"
                    + FIELD_N_TEXT      + " TEXT NOT NULL,"
                    + FIELD_N_VOICE     + " BLOB,"
                    + FIELD_N_IMAGE     + " BLOB,"
                    + "	FOREIGN KEY (" + FIELD_QID + ") REFERENCES " + TABLE_QUESTION +"(" + FIELD_ID + ")"
                    + ")";

    static final String SQL_CreateRecordTable =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECORD + "( "
                    + FIELD_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FIELD_QID         + " INTEGER NOT NULL UNIQUE,"
                    + FIELD_NG          + " INTEGER DEFAULT 0,"
                    + FIELD_OK          + " INTEGER DEFAULT 0,"
                    + FIELD_MARK        + " INTEGER DEFAULT 0,"
                    + FIELD_RECENT      + " INTEGER DEFAULT -1,"
                    + FIELD_REV_NG      + " INTEGER DEFAULT 0,"
                    + FIELD_REV_OK      + " INTEGER DEFAULT 0,"
                    + FIELD_REV_MARK    + " INTEGER DEFAULT 0,"
                    + FIELD_REV_RECENT  + " INTEGER DEFAULT -1,"
                    + "	FOREIGN KEY (" + FIELD_QID + ") REFERENCES " + TABLE_QUESTION +"(" + FIELD_ID + ")"
                    + ")";

    static final String SQL_CreateIndex_question_table ="CREATE INDEX IF NOT EXISTS idx_question_table ON " + TABLE_QUESTION + " (" + FIELD_WINID +")";
    static final String SQL_CreateIndex_note_table ="CREATE INDEX IF NOT EXISTS idx_note_table ON " + TABLE_NOTE + " (" + FIELD_QID + ")";
    static final String SQL_CreateIndex_record_table ="CREATE INDEX IF NOT EXISTS idx_record_table ON "+TABLE_RECORD +" (" + FIELD_QID +")";

    static final String[] SQLS_Create = {
            SQL_CreateExtTable,
            SQL_CreateBookTable,
            SQL_CreateQuestionTable,
            SQL_CreateNoteTable,
            SQL_CreateRecordTable,
            SQL_CreateIndex_question_table,
            SQL_CreateIndex_note_table,
            SQL_CreateIndex_record_table,
    };

    static final int DB_VERSION = 1;
    static final String DB_NAME = "dgbool.db";

    public static class Mark {
        public final static int NORMAL = 0;
        public final static int IMPORTANT = 1;
        public final static int VERY_IMPORTANT = 2;
        public final static int EXTRA_VERY_IMPORTANT = 3;
    }

    public DgDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for(String sql : SQLS_Create) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

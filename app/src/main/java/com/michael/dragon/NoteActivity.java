package com.michael.dragon;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.michael.dragon.db.DgDBHelper;
import com.michael.dragon.db.DgDatabase;
import com.michael.dragon.db.DgNote;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author M.TOYOTA 13/09/13 Created.
 * @author Copyright (C) 2013 MetaMoJi Corp. All Rights Reserved.
 */
public class NoteActivity extends AppCompatActivity {
    long mBid;
    long[] mNoteIds;
    int mCurrent;
    boolean mAllNotes;
    DgNote mNote = new DgNote();

    public static final String PARAM_NOTE_IDS = "NoteIds";
    public static final String PARAM_ALL_NOTES = "AllNotes";
    public static final String PARAM_BID = PracticeActivity.PARAM_BID;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note);

        ActionBar ab = getActionBar();
        ab.setHomeButtonEnabled(true);


        Intent intent = getIntent();
        mNoteIds = intent.getLongArrayExtra(PARAM_NOTE_IDS);
        mAllNotes = intent.getBooleanExtra(PARAM_ALL_NOTES, false);
        mBid = intent.getLongExtra(PARAM_BID, -1);

        if( null ==  mNoteIds || mBid==-1 ) {
            finish();
            return;
        }

        findViewById(R.id.next).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextNote();
            }
        });
        findViewById(R.id.prev).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevNote();
            }
        });
        findViewById(R.id.important).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImportance(v);

            }
        });

        if( mAllNotes ) {
            View v = findViewById(R.id.practice);
            v.setVisibility(View.VISIBLE);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doPractice();
                }
            });
        }

        if( null == savedInstanceState ) {
            setCurrentNote(0);
        }

    }

    private void doPractice() {
        if( !mAllNotes) {
            return;
        }
        if( !DgDatabase.getInstance().getNote( mNoteIds[mCurrent], mNote) ) {
            return;
        }
        Intent intent = new Intent(this, PracticeActivity.class);
        intent.putExtra(PracticeActivity.PARAM_BID, mBid );
        intent.putExtra(PracticeActivity.PARAM_SINGLE, mNote.getQid());
        startActivity(intent);
    }

    private void setImportance(View anchor) {
        // PopupMenuのインスタンスを作成
        PopupMenu popup = new PopupMenu(this, anchor);

        // PopupMenu にアイコンを表示するためのコード
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // popup.xmlで設定したメニュー項目をポップアップメニューに割り当てる
        popup.getMenuInflater().inflate(R.menu.select_mark, popup.getMenu());

        // ポップアップメニューのメニュー項目のクリック処理
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int mark = 0;
                switch( item.getItemId() ) {
                    default:
                    case R.id.normal:
                        mark = DgDBHelper.Mark.NORMAL;
                        break;
                    case R.id.important:
                        mark = DgDBHelper.Mark.IMPORTANT;
                        break;
                    case R.id.very_important:
                        mark = DgDBHelper.Mark.VERY_IMPORTANT;
                        break;
                    case R.id.extra_important:
                        mark = DgDBHelper.Mark.EXTRA_VERY_IMPORTANT;
                        break;
                }
                registImportance(mark);
                return true;
            }
        });

        // ポップアップメニューを表示
        popup.show();
    }

    /**
     * マークを登録する。
     *
     * @param mark
     */
    private void registImportance(int mark) {
        if( mNoteIds == null || mCurrent < 0 || mCurrent>=mNoteIds.length) {
            return;
        }

        DgDatabase db = DgDatabase.getInstance();
        if( db.setNoteMark(mNoteIds[mCurrent], mark) ) {
            updateImportanceIcon(mark);
        }
    }

    private void nextNote() {
        setCurrentNote(mCurrent+1);
    }

    private void prevNote() {
        setCurrentNote(mCurrent-1);
    }

    private void setCurrentNote(int index) {
        if( mNoteIds == null ){
            return;
        }
        if( index < 0 || mNoteIds.length<=index ) {
            return;
        }

        DgDatabase db = DgDatabase.getInstance();
        if( !db.getNote(mNoteIds[index], mNote) ) {
            return;
        }

        ((TextView)findViewById(R.id.subject)).setText(mNote.getSubject());
        ((TextView)findViewById(R.id.body)).setText(mNote.getText());
        updateImportanceIcon(mNote.getMark());

        mCurrent = index;

        enableButton(R.id.next, mCurrent<mNoteIds.length-1);
        enableButton(R.id.prev, 0<mCurrent);

        String title = String.format("Notes (%d/%d)", mCurrent+1, mNoteIds.length );
        getActionBar().setTitle(title);
    }

    private void updateImportanceIcon(int importance) {
        ((ImageButton)findViewById(R.id.important)).setImageResource( PracticeActivity.getIconOfImportance(importance));
    }

    /**
     * ボタンを有効/無効化する。
     *
     * @param id        ボタンのid
     * @param enable    true:有効化 / false:無効化
     */
    private void enableButton(int id, boolean enable) {
        View v = findViewById(id);
        v.setEnabled(enable);
        v.setAlpha(enable ? 1f : 0.5f);
    }


    final String SAVE_CURRENT = "Current";
    /**
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_CURRENT, mCurrent);
    }

    /**
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrent = savedInstanceState.getInt(SAVE_CURRENT);
        setCurrentNote(mCurrent);
    }

    /**
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if( item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
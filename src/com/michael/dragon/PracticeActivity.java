package com.michael.dragon;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.michael.dragon.db.*;
import com.michael.dragon.logic.DcWordSplitter;
import com.michael.dragon.logic.control.DvWordsPanel;
import com.michael.dragon.ui.UiMessageBox;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author M.TOYOTA 13/09/10 Created.
 * @author Copyright (C) 2013 MetaMoJi Corp. All Rights Reserved.
 */
public class PracticeActivity extends Activity {
    public static final int N_UNIT_COUNT_IN_GROUP = 10;            // １グループあたりの問題数
    DvWordsPanel mWordsPanel;                           // 単語パネル
    DcWordSplitter mSplitter = new DcWordSplitter();    // 問題から単語に切り出して管理するクラス
    TextView mQuestionView;                             // 問題文表示エリア
    TextView mAnswerView;                               // 解答表示エリア
    long mBid = 0;                                      // 問題集ID
    int mGroup;                                         // 現在のグループ番号
    int mGroupCount = 0;                                // 全グループ数
    long[] mQidsAll = null;                             // 全問題リスト
    long[] mQids = null;                                // 現在のグループ内の問題idリスト
    int mCurrent;                                       // 現在の問題（mQids内のインデックス）
    boolean mDone = false;                              // 現在の問題に ok/ng評価をセットしたときにtrueに設定される。
    boolean mRetrying = false;                          // NGの問題を絞り込んでリトライ中フラグ
    private DgQuestion  mQuestion = new DgQuestion();   // 問題情報を取得するためのバッファ
    private DgRecord mRecord = new DgRecord();          // レコード情報を取得するためのバッファ
    long[] mNotes = null;
    boolean mSingleMode = false;

    public static final String PARAM_BID = "RequestBID";
    public static final String PARAM_PART = "RequestPART";
    public static final String PARAM_SINGLE = "SingleMode";


    /**
     * アクティビティの初期化
     *
     * @param savedInstanceState    復元情報
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_practice);
        getActionBar().setHomeButtonEnabled(true);
        mWordsPanel = (DvWordsPanel)findViewById(R.id.wordsPanel);
        mQuestionView = (TextView)findViewById(R.id.question);
        mAnswerView = (TextView)findViewById(R.id.answer);

        mWordsPanel.setOnSelectWordListener(new DvWordsPanel.OnSelectWord() {
            @Override
            public boolean onSelectWord(String word) {
                if( !mSplitter.check(word) ) {
                    mAnswerView.setBackgroundColor(Color.rgb(0xFF,0x9F, 0xD3));
                    return false;
                }
                String s = mSplitter.getChunkAndNext();
                mAnswerView.setText(mAnswerView.getText().toString()+s);
                mAnswerView.setBackgroundColor(Color.rgb(0xca,0xff,0xdf));
                if( !mSplitter.hasValue()) {
                    // 終了
                    onAnswerCompleted();
                }
                return true;
            }
        });

        DgDatabase db = DgDatabase.getInstance();
        Intent intent = getIntent();
        mBid = intent.getLongExtra(PARAM_BID, -1);
        int group = intent.getIntExtra(PARAM_PART, -1);
        long singleQid = intent.getLongExtra(PARAM_SINGLE, -1);
        if( singleQid != -1 ) {
            mSingleMode = true;
            findViewById(R.id.prev).setVisibility(View.GONE);
            findViewById(R.id.next).setVisibility(View.GONE);
            findViewById(R.id.note).setVisibility(View.GONE);
        }

//        DgBook[] books = db.getBooks();
        if( mBid != -1 ) {

            mCurrent = 0;
            mGroup = 0;
            if( !mSingleMode ) {
                mQidsAll = db.getQidList(mBid);
            } else {
                mQidsAll = new long[] { singleQid };
            }
            mGroupCount = getGroupCount();


            findViewById(R.id.next).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextPage(true);
                }
            });
            findViewById(R.id.prev).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prevPage(true);
                }
            });
            findViewById(R.id.hint).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHint();
                }
            });
            findViewById(R.id.note).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNotes();

                }
            });
            findViewById(R.id.ok).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    registResult(true);
                }
            });
            findViewById(R.id.ng).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    registResult(false);
                }
            });
            findViewById(R.id.important).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setImportance(v);

                }
            });

            if( null == savedInstanceState ) {
                if( group == -1) {
                    shuffle(mQidsAll, new Random());
                }
                setCurrentGroup(group);
                setCurrentPage(0);
            }
        }
    }

    private void showNotes() {
        if( null == mQids ) {
            return;
        }

        if( null!=mNotes && mNotes.length>0 ) {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra(NoteActivity.PARAM_NOTE_IDS, mNotes);
            intent.putExtra(NoteActivity.PARAM_BID, mBid);
            startActivity(intent);
        }

    }

    /**
     */
    @Override
    protected void onPause() {
        super.onPause();
        DgDatabase.getInstance().touchBook(mBid);
    }

    /**
     * ポップアップメニューを開いて、Mark(importance)を設定する。
     *
     * @param anchor    表示位置を指定するアンカー
     */
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

    public static int getIconOfImportance(int importance) {
        int id;
        switch( importance ){
            default:
            case DgDBHelper.Mark.NORMAL:
                id = R.drawable.ic_normal_important;
                break;
            case DgDBHelper.Mark.IMPORTANT:
                id = R.drawable.ic_important;
                break;
            case DgDBHelper.Mark.VERY_IMPORTANT:
                id = R.drawable.ic_middle_important;
                break;
            case DgDBHelper.Mark.EXTRA_VERY_IMPORTANT:
                id = R.drawable.ic_extra_important;
                break;
        }
        return id;
    }

    /**
     * 問題（ページ）が切り替わったときに、ボタンなどのアイコンを更新する
     */
    private void updateIconsOnPageSelection() {
        int mark = 0, ng=0, ok=0, current=-1;
        if( mQids != null && 0<=mCurrent && mCurrent<mQids.length) {
            if( DgDatabase.getInstance().getRecord(mQids[mCurrent], false, mRecord) ) {
                mark = mRecord.getMark();
                ng = mRecord.getNg();
                ok = mRecord.getOk();
                current = mRecord.getRecent();
            }
        }
        // Mark(importance)ボタンのアイコン
        int id = getIconOfImportance(mark);
        ((ImageButton)findViewById(R.id.important)).setImageResource(id);

        // Recentの反映（OK/NGアイコン）
        findViewById(R.id.ok_icon).setAlpha((current == 1) ? 1f : 0.4f);
        findViewById(R.id.ng_icon).setAlpha((current == 0) ? 1f : 0.4f);

        // OK/NGの回数
        ((TextView)findViewById(R.id.ok_count)).setText(Integer.toString(ok));
        ((TextView)findViewById(R.id.ng_count)).setText(Integer.toString(ng));
    }

    /**
     * 結果(ok/ng)を設定する。
     *
     * @param ok    true:ok/false:ng
     */
    private void registResult(boolean ok) {
        if( mQids == null || mCurrent < 0 || mCurrent>=mQids.length) {
            return;
        }

        // ２回ok/ngが押せないよう、グレーアウトしておく。
        enableButton(R.id.ok, false);
        enableButton(R.id.ng, false);
        mDone = true;

        DgDatabase db = DgDatabase.getInstance();
        db.setRecordResult(mQids[mCurrent], false, ok);

        if( !mSingleMode ) {
            if( mCurrent+1 >= mQids.length ) {
                // end of pages
                nextStep(true);
            }
            else {
                setCurrentPage(mCurrent + 1);
            }
        } else {
            finish();
        }
    }

    /**
     * 次の段階へ進む。
     *  まず、最近NGを付けたアイテムに絞り込んでリトライする。すべてOKになれば、確認メッセージを出して、次のグループに進む。
     *  最後のグループが終わったら、Activityを閉じる。
     *
     * @param needsConfirm  true: 必要なら確認メッセージを表示 / false: メッセージは出さない
     */
    private void nextStep(boolean needsConfirm ) {
        if( !stripCleard() ) {
            if( mGroup < mGroupCount-1 ) {
                if( needsConfirm ) {
                    UiMessageBox.selectYesNo(this, R.string.Title_Congratulation, R.string.Msg_ConfirmNextGroup, new UiMessageBox.OnResult() {
                        @Override
                        public void onResult(boolean ok) {
                            if (ok) {
                                nextStep(false);
                            } else {
                                PracticeActivity.this.finish();
                            }
                        }
                    });
                    return;
                }
            }

            if( !setCurrentGroup(mGroup+1)) {
                finish();
            }
            setCurrentPage(0);
        }
        mCurrent = 0;
        setCurrentPage(0);
    }

    /**
     * 最近OKになったアイテムを取り除いて、mQidsを再作成する。
     *
     * @return  true:有効なmQidsが作成された /false: 該当なし
     */
    private boolean stripCleard() {
        if( null == mQids || mQids.length ==0 ) {
            return false;
        }

        DgDatabase db = DgDatabase.getInstance();
        long[] check = new long[mQids.length];
        int uncleared = 0;
        for( int i=0, ci=mQids.length ; i<ci ; i++ ) {
            if( !db.getRecord(mQids[i],false, mRecord) || mRecord.getRecent()!=1 ) {
                check[uncleared++] = mQids[i];
            }
        }
        if( uncleared == 0 ){
            return false;
        }
        if( uncleared == mQids.length ) {
            return true;
        }
        mRetrying = true;
        mQids = Arrays.copyOf(check, uncleared);
        return true;
    }

    /**
     * 総グループ数を計算する。
     *
     * @param  total 総問題数
     * @return  グループ数
     */
    public static int getGroupCount( int total ) {
        if( total == 0 ) {
            return 0;
        }

        int count = total / N_UNIT_COUNT_IN_GROUP;
        int mod = total % N_UNIT_COUNT_IN_GROUP;
        if( count > 0 && mod < N_UNIT_COUNT_IN_GROUP / 2 ) {
            return count;
        } else {
            return count+1;
        }
    }

    /**
     * 総グループ数を計算する。
     *
     * @return  グループ数
     */
    private int getGroupCount() {
        if( mQidsAll == null || mQidsAll.length==0 ) {
            return 0;
        }
        return getGroupCount(mQidsAll.length);
    }


    public static void shuffle(long[] objectList, Random random) {

        for (int i = objectList.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            long t = objectList[i];
            objectList[i] = objectList[index];
            objectList[index] = t;
        }
    }

    /**
     * カレントグループを変更する。
     *
     * @param group グループ番号
     * @return  true:変更された / false:グループ番号がレンジ外
     */
    private boolean setCurrentGroup(int group) {
        if( group == -1 ) {
            // ランダムモード
            mQids = mQidsAll;
        } else {
            if( group < 0 || mGroupCount<=group ) {
                return false;
            }
            if( group == mGroupCount-1 ){
                // 残りすべて
                mQids = Arrays.copyOfRange(mQidsAll, group* N_UNIT_COUNT_IN_GROUP, mQidsAll.length);
            } else {
                mQids = Arrays.copyOfRange(mQidsAll, group* N_UNIT_COUNT_IN_GROUP, (group+1)* N_UNIT_COUNT_IN_GROUP);
            }
        }
        mRetrying = false;
        mGroup = group;
        return true;

    }

    /**
     * マークを登録する。
     *
     * @param mark
     */
    private void registImportance(int mark) {
        if( mQids == null || mCurrent < 0 || mCurrent>=mQids.length) {
            return;
        }

        DgDatabase db = DgDatabase.getInstance();
        db.setRecordMark(mQids[mCurrent], false, mark);

        updateIconsOnPageSelection();
    }

    /**
     * 解答欄が完成した時の処理
     * ok/ngボタンを有効にする。
     */
    private void onAnswerCompleted() {
        enableButton(R.id.ok, true);
        enableButton(R.id.ng, true);
    }

    /**
     * 次の１語を開く。
     */
    private void showHint() {
        if( !mSplitter.hasValue()) {
            return;
        }

        // 次の語を取り出して、ワードパネルの表示を消す。
        String s = mSplitter.peekNextWord();
        mWordsPanel.skipWord(s);

        // チェック位置をすすめる
        s= mSplitter.getChunkAndNext();
        // 解答欄に続きを出力
        mAnswerView.setText(mAnswerView.getText().toString()+s);

        if( !mSplitter.hasValue()) {
            // 終了
            onAnswerCompleted();
        }
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

    /**
     * カレントページ（問題）を変更する。
     *
     * @param page  ページ番号（mQids内でのインデックス）
     */
    private void setCurrentPage(final int page) {
        if( page < 0 || page >= mQids.length ) {
            return;
        }

        mDone = false;

        DgDatabase db = DgDatabase.getInstance();
        db.getQuestion(mQids[page], mQuestion);

        // 問題文、見出しをセット
        mQuestionView.setText(mQuestion.getQText());
        ((TextView)findViewById(R.id.title)).setText(mQuestion.getSubject());
        // 解答文をクリア
        mAnswerView.setText("");
        // 問題文を分解して単語パネルにセット
        ArrayList<String> ary = mSplitter.purse(mQuestion.getAText());
        mWordsPanel.setWords(ary);
        mCurrent = page;

        mNotes = db.getNotes(mQids[page]);

        updateButtonState();
        updateIconsOnPageSelection();
    }

    void updateButtonState() {
        if( mGroup != -1) {
            enableButton(R.id.prev, (mCurrent != 0 || mGroup!=0) );
            enableButton(R.id.next, (mCurrent < mQids.length-1 || mGroup<mGroupCount-1) );
        } else {
            enableButton(R.id.prev, (mCurrent != 0 ));
            enableButton(R.id.next, (mCurrent < mQids.length-1));
        }

        enableButton(R.id.ok, !mDone && !mSplitter.hasValue());
        enableButton(R.id.ng, !mDone && !mSplitter.hasValue());

        enableButton(R.id.note, null!=mNotes && mNotes.length>0);
    }

    /**
     * ページを進める。
     *
     * @param needsConfirm      true:確認メッセージを表示する。
     */
    private void nextPage(boolean needsConfirm) {
        boolean moveGroup = false;
        int page = mCurrent+1;
        if( page >= mQids.length ) {
            if( mGroup==-1 || mGroup >= mGroupCount-1 ) {
                return;
            }
            moveGroup = true;
        }
        if( needsConfirm ) {
            int message = -1;
            if( moveGroup ) {
                message = R.string.Msg_ConfirmNextGroup;
            }

            if( mSplitter.getChecking()>0 && !mDone ) {
                if( !moveGroup ) {
                    message = R.string.Msg_ConfirmAbortAndNextTrial;
                } else {
                    message = R.string.Msg_ConfirmAbortAndNextGroup;
                }
            }

            if( message != -1 ){
                UiMessageBox.selectYesNo(this, R.string.Title_Confirm, message, new UiMessageBox.OnResult() {
                    @Override
                    public void onResult(boolean ok) {
                        if( ok ){
                            nextPage(false);
                        }
                    }
                });
                return;
            }
        }
        if( moveGroup ) {
            setCurrentGroup(mGroup+1);
            page = 0;
        }
        setCurrentPage(page);
    }

    /**
     * ページを戻す。
     *
     * @param needsConfirm      true:確認メッセージを表示する。
     */
    private void prevPage(boolean needsConfirm) {
        boolean moveGroup = false;
        int page = mCurrent-1;
        if( page < 0 ) {
            if( mGroup==-1 || mGroup == 0 ) {
                return;
            }
            moveGroup = true;
        }
        if( needsConfirm ) {
            int message = -1;
            if( moveGroup ) {
                message = R.string.Msg_ConfirmPregGroup;
            }

            if( mSplitter.getChecking()>0 && !mDone ) {
                if( !moveGroup ) {
                    message = R.string.Msg_ConfirmAbortAndPrevTrial;
                } else {
                    message = R.string.Msg_ConfirmAbortAndNPrevGroup;
                }
            }

            if( message != -1 ){
                UiMessageBox.selectYesNo(this, R.string.Title_Confirm, message, new UiMessageBox.OnResult() {
                    @Override
                    public void onResult(boolean ok) {
                        if( ok ){
                            nextPage(false);
                        }
                    }
                });
                return;
            }
        }
        if( moveGroup ) {
            setCurrentGroup(mGroup-1);
            page = N_UNIT_COUNT_IN_GROUP -1;
        }
        setCurrentPage(page);
    }

    final String SAVE_CURRENT = "CurrentIndex";
    final String SAVE_HIDDNBUTTONS = "HiddenButtons";
    final String SAVE_GROUP = "CurrentGroup";
    final String SAVE_RETRYING = "Retrying";
    final String SAVE_DONE = "Done";
    final String SAVE_QIDS = "Qids";


    /**
     *
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SAVE_CURRENT, mCurrent);
        outState.putInt(SAVE_GROUP, mGroup);
        outState.putBoolean(SAVE_RETRYING, mRetrying);
        outState.putBoolean(SAVE_DONE, mDone);
        mSplitter.onSaveInstanceState(outState);
        outState.putIntArray(SAVE_HIDDNBUTTONS, mWordsPanel.getDoneButtons());
        if( mGroup == -1) {
            outState.putLongArray(SAVE_QIDS, mQidsAll);
        }
    }

    /**
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mGroupCount = getGroupCount();
        int group = savedInstanceState.getInt(SAVE_GROUP,mGroupCount);
        if( group < -1 || group >= mGroupCount ) {
            return;
        }
        if( group == -1) {
            mQidsAll = savedInstanceState.getLongArray(SAVE_QIDS);
        }
        setCurrentGroup(group);
        if( savedInstanceState.getBoolean(SAVE_RETRYING,false)) {
            stripCleard();
        }

        int page = savedInstanceState.getInt(SAVE_CURRENT,-1);
        if( page < 0 || page >= mQids.length ) {
            return;
        }
        mCurrent = page;

        DgDatabase db = DgDatabase.getInstance();
        db.getQuestion(mQids[mCurrent], mQuestion);
        mQuestionView.setText(mQuestion.getQText());
        ((TextView)findViewById(R.id.title)).setText(mQuestion.getSubject());
        mSplitter.purse(mQuestion.getAText());
        mSplitter.onRestoreInstanceState(savedInstanceState);
        mAnswerView.setText(mSplitter.getCurrentAnswer());
        mWordsPanel.setWords(mSplitter.getRandomList());
        mWordsPanel.setDoneButtons(savedInstanceState.getIntArray(SAVE_HIDDNBUTTONS));
        mNotes = db.getNotes(mQids[page]);

        updateButtonState();
        updateIconsOnPageSelection();
    }

    /**
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if( item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
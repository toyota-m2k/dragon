package com.michael.dragon.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.michael.dragon.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author M.TOYOTA 13/02/19 Created.
 * @author Copyright (C) 2012 MetaMoJi Corp. All Rights Reserved.
 */
public class UiFileListView extends ListView {
    public UiFileListView(Context context) {
        super(context);
        init();
    }

    public UiFileListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UiFileListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * ファイル選択通知用リスナー
     */
    public static interface IOnFileSelected {

        /**
         * ファイルまたはディレクトリが選択された
         * @param file  選択されたファイルまたはディレクトリ
         * @return  true: 処理継続 / false: ファイル選択を決定して終了するので継続処理不要（ディレクトリの場合、中に入るかどうかの選択）
         */
        public boolean onFileSelected(File file);
    }

    /**
     * ファイルリストのタイプ
     */
    public class Type {
        public static final int FILE = 1;       // ファイル選択用（ディレクトリの変更禁止）
        public static final int DIRECTORY = 2;  // ディレクトリ選択用
        public static final int ALL = 3;        // ファイル選択用（ディレクトリの変更可能）
        public static final int ALL_DIR = 7;    // ディレクトリ選択用（ファイルも単色表示表示）
    }

    // Fields
    private File mBaseDir = null;                       // リスト対象ディレクトリ
    private ExtFilter mFilter = null;                   // 拡張子によるファイルフィルター
    private IOnFileSelected mSelectListener = null;     // ファイル選択監視リスナー
    private boolean mShowParentDir = true;


    /**
     * コンストラクタから実行される共通の初期化
     */
    private void init() {
        setScrollingCacheEnabled(false);
        setItemsCanFocus(true);
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	// 選択ファイルを取得
                File file = getFileAt(position);

                // ファイル選択リスナーを呼びだす
                if( null != mSelectListener) {
                    if( !mSelectListener.onFileSelected(file) ) {
                        return;
                    }
                }

                // 選択されたのがディレクトリなら、中に入る
                if( null != file ) {
                    if( file.isDirectory() ) {
                        setBaseDir(file);
                    }
                }
            }
        });
    }

    public void showParentDir(boolean show) {
        mShowParentDir = show;
        if( null != mFilter && null != mBaseDir) {
            updateList();
        }
    }

    /**
     * ファイル列挙フィルターをセットする
     * @param extensions 拡張子リスト（nullならすべて）
     * @param type  Type.FILE: ファイルのみ / Type.DIRECTORY: ディレクトリのみ / Type.ALL: ファイルとディレクトリ
     */
    public void setFilter(String[] extensions, int type ) {
        mFilter = new ExtFilter(extensions, type);
        if(null != mBaseDir) {
            updateList();
        }
    }

    /**
     * ファイル列挙対象ディレクトリを指定する。
     * @param fileDirectory 対象ディレクトリ
     */
    public void setBaseDir( File fileDirectory ) {
        mBaseDir = fileDirectory;
        if( null != mFilter ) {
            updateList();
        }
    }

    public void setFileSelectedListener( IOnFileSelected listener ) {
        mSelectListener = listener;
    }

    /**
     * ファイルリストを更新する
     */
    private void updateList() {
        // ファイルリスト
        File[] files =  mBaseDir.listFiles(mFilter);
        ArrayList<FileInfo> listFileInfo = new ArrayList<FileInfo>(null==files ? 1 : files.length);
        if (null != files) {
            for (int i=0, ci=files.length ; i<ci ; i++ ) {
                File file = files[i];
                listFileInfo.add(new FileInfo(file));
            }
            Collections.sort(listFileInfo);
        }
        // ソート後に親フォルダに戻るパスを先頭に追加
        if (mShowParentDir && mFilter.isDirectoryEnabled() && null != mBaseDir.getParent()) {

            /*
             * getExternalStorageDirectoryで取得したパスより上への移動を止める（#212） → 止めない（#391）
             * https://trac.metamoji.net/trac/shirasagi/ticket/212
             * https://trac.metamoji.net/trac/shirasagi/ticket/391
			 * /sdcard
			 * /mnt/sdcard
			 * /mnt/sdcard/external_sd
			 * /storage/sdcard0
			 */
        	/*
            File esd = Environment.getExternalStorageDirectory();
        	if (!esd.getPath().equals(mBaseDir.getPath())) {
                listFileInfo.add(0, new FileInfo(new File(mBaseDir.getParent()), true));
        	}
        	*/
            listFileInfo.add(0, new FileInfo(new File(mBaseDir.getParent()), true));
        }

        FileInfoArrayAdapter adapter = new FileInfoArrayAdapter(getContext(), listFileInfo);
        this.setAdapter(adapter);
    }

    /**
     * 対象ディレクトリを取得する。
     * @return  File
     */
    public File getBaseDir() {
        return mBaseDir;
    }

    /**
     * 選択されているファイルを取得
     * @return
     */
    public File getSelectedFile() {
        FileInfo info = (FileInfo)this.getSelectedItem();
        if(null != info) {
            return info.getFile();
        }
        return null;
    }

    /**
     * 指定インデックスのファイルを取得
     */
    public File getFileAt(int index ) {
        FileInfo info = (FileInfo)this.getItemAtPosition(index);
        if(null != info) {
            return info.getFile();
        }
        return null;

    }

    /**
     * ファイル列挙用のフィルタリング
     */
    private class ExtFilter implements FileFilter {

        String[] mExtensions;   // 拡張子リスト
        int mType;

        /**
         * コンストラクタ
         * @param extensions 拡張子リスト（nullならすべて）
         * @param type  Type.FILE: ファイルのみ / Type.DIRECTORY: ディレクトリのみ / Type.ALL: ファイルとディレクトリ
         */
        ExtFilter(String[] extensions, int type ) {
            mExtensions = extensions;
            mType = type;
        }

        /**
         * 列挙対象かどうかを判断
         * @param file  検査するファイルオブジェクト
         * @return  true:列挙する / false:無視する
         */
        @Override
        public boolean accept(File file) {
            if( file.isDirectory()) {
            	if (file.isHidden()) {
            		return false;
            	}
                return 0!=(mType & Type.DIRECTORY);
            } else if( 0 != (mType & Type.FILE) ) {
                if( null == mExtensions) {
                    return true;
                }
                for( int i = 0, ci=mExtensions.length ; i<ci ; i++ ) {
                    if( file.getName().endsWith(mExtensions[i]) ) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * ディレクトリは列挙対象か？
         * @return  true/false
         */
        public boolean isDirectoryEnabled() {
            return 0!=(mType & Type.DIRECTORY);
        }

        /**
         * ファイルは列挙対象か？
         * @return  true/false
         */
        public boolean isFileEnabled() {
            return 0!=(mType & Type.FILE);
        }
    }

    /**
     * ファイル情報クラス
     */
    private class FileInfo implements Comparable<FileInfo> {
        boolean mParentDirectory;   // ".."を特別扱いするためのフラグ
        private File mFile;    // ファイルオブジェクト

        /**
         * コンストラクタ
         * @param file      ファイル
         * @param parentDirectory   親ディレクトリか（..と表示するか）
         */
        public FileInfo(File file, boolean parentDirectory) {
            mParentDirectory = parentDirectory;
            mFile = file;
        }

        public FileInfo(File file) {
            this(file, false);
        }

        /**
         * ファイル名を取得
         * @return  ファイル名。ディレクトリの場合は"/"で終わることを保証
         */
        public String getName() {
            if( mParentDirectory ) {
                //return ".. (" + mFile.getName() + "/)";
                return getResources().getString(R.string.ParentFolder);
            } else {
                if( mFile.isDirectory() ) {
                    return mFile.getName() + "/";
                } else {
                    return mFile.getName();
                }
            }
        }

        /**
         * ファイルオブジェクトを取得
         * @return  ファイル名。ディレクトリの場合は"/"で終わることを保証
         */
        public File getFile() {
            return mFile;
        }

        /**
         * ソートのための比較
         * @param another   比較先
         * @return  int
         */
        public int compareTo(FileInfo another) {
            if (mFile.isDirectory() && !another.mFile.isDirectory()) {
                return -1;
            }
            if (!mFile.isDirectory() && another.mFile.isDirectory()) {
                return 1;
            }

            // 大文字・小文字は区別しないで比較する
            int result = mFile.getName().compareToIgnoreCase(another.mFile.getName());
            if( result == 0 ){
                // 大文字・小文字を区別しないで一致した場合は、区別して比較する。
                result  = mFile.getName().compareTo(another.mFile.getName());
            }
            return result;
        }
    }


    /**
     * データソース用アダプタクラス
     */
    private class FileInfoArrayAdapter extends ArrayAdapter<FileInfo> {
        private ArrayList<FileInfo> mListFileInfo; // ファイル情報リスト

        // コンストラクタ
        public FileInfoArrayAdapter(Context context,ArrayList<FileInfo> objects) {
            super(context, -1, objects);
            mListFileInfo = objects;
        }

        /**
         * 指定されたFileInfoを取得
         */
        @Override
        public FileInfo getItem(int position) {
            return mListFileInfo.get(position);
        }

        /**
         * リストの要素（ファイル名）を表示するためのビューを生成して返す
         */
        @Override
        public View getView(int position,View convertView,ViewGroup parent) {
            // レイアウトの生成
            if (null == convertView) {
                Context context = getContext();
                // レイアウト
                LinearLayout layout = new LinearLayout(context);
                layout.setTag("layout");
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setBackgroundResource(0);
                convertView = layout;
                // レイアウト（アイコン＆テキスト）
                LinearLayout layoutIconText = new LinearLayout(context);
                layoutIconText.setOrientation(LinearLayout.HORIZONTAL);
                layoutIconText.setPadding(10, 10, 10, 10);
                layoutIconText.setBackgroundResource(0);
                layout.addView(layoutIconText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1));
                // アイコン
                ImageView imageview = new ImageView(context);
                imageview.setTag("icon");
                imageview.setBackgroundResource(0);
                layoutIconText.addView(imageview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                // テキスト
                TextView textview = new TextView(context);
                textview.setTag("text");
                textview.setBackgroundResource(0);
                textview.setPadding(10, 10, 10, 10);
                //layout.addView(textview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                layoutIconText.addView(textview, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                // 区切り線
                FrameLayout line = new FrameLayout(context);
                line.setBackgroundColor(Color.LTGRAY);
                layout.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            }

            // 値の指定
            FileInfo fileinfo = mListFileInfo.get(position);
            // アイコン
            ImageView imageview = (ImageView) convertView.findViewWithTag("icon");
            if (!fileinfo.mFile.isDirectory()) {
                imageview.setImageResource(R.mipmap.ic_blank);
            }
            else if (!fileinfo.mParentDirectory) {
                imageview.setImageResource(R.mipmap.ic_folder);
            }
            else {
                imageview.setImageResource(R.mipmap.ic_folder);
            }
            // テキスト
            TextView textview = (TextView) convertView.findViewWithTag("text");
            textview.setText(fileinfo.getName());
            if( mFilter.mType == Type.ALL_DIR && !fileinfo.mFile.isDirectory() ) {
                textview.setTextColor(Color.GRAY);
            } else {
                textview.setTextColor(Color.BLACK);
            }
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }
    }

}

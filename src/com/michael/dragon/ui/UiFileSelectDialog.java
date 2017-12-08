package com.michael.dragon.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.michael.dragon.R;

import java.io.File;
import java.util.ArrayList;

/**
 * @author M.TOYOTA 13/02/19 Created.
 * @author Copyright (C) 2012 MetaMoJi Corp. All Rights Reserved.
 */
public class UiFileSelectDialog extends UiDialog {
    private File mBaseDir;
    private String[] mExtensions;
    private int mType;
    private int mTitleId;

    /**
     * デフォルトのコンストラクタ
     * 独自コンストラクタを実装している場合、これが無いとActivity復元時にRuntimeExceptionが起きる
     */
    public UiFileSelectDialog() {
    	super();
    }
    
    /**
     * コンストラクタ
     *
     * @param baseDir    ベースディレクトリ
     * @param extensions 列挙する拡張子
     * @param type       UiFileListView.Type.FILE: ファイルのみ / DIRECTORY: ディレクトリのみ / ALL: ファイルとディレクトリ
     */
    public UiFileSelectDialog(File baseDir, String[] extensions, int type, int titleId ) {
        if( baseDir != null ) {
            mBaseDir = baseDir;
        } else {
            //mBaseDir = CmUtils.getPrivateExtDirectory();
        	// sdcard直下を初期値とする 2013/03/14
            mBaseDir = Environment.getExternalStorageDirectory();
            if( mBaseDir == null ) {
                throw new IllegalStateException("maybe external storage is not mounted.");
            }
        }
        mExtensions = extensions;
        mType = type;
        mTitleId = titleId;
    }

    private UiFileListView mFileListView = null;
    private TextView mDirText = null;
    private EditText mFilenameEdit = null;
    private ImageButton mBack = null;
    private ImageButton mForward = null;
//    private ImageButton mCreate = null;
    private File mFile = null;
    private History mHistory = null;
    private boolean mFilenameField = false;
    private String mInitialFilename = null;

    /**
     * ファイル名フィールドの表示を有効にする。デフォルトは無効
     *
     * @param flag true:有効 /false:無効
     */
    public void enableFilenameField(boolean flag, String initialFilename) {
        mFilenameField = flag;
        mInitialFilename = initialFilename;
    }

    /**
     * ディレクトリ変更履歴（戻る/進む）管理
     */
    private class History {
        private ArrayList<File> mStack = new ArrayList<File>(32);
        private int mCurrent = -1;
        private int mMax = 0;

        /**
         * 履歴に積む
         */
        public void push(File file) {
            mCurrent++;
            mStack.add(mCurrent, file);
            mMax = mCurrent + 1;
        }

        /**
         * 「戻る」は有効？
         */
        public boolean hasPrev() {
            int index = mCurrent - 1;
            return (0 <= index && index < mMax);
        }

        /**
         * 戻る
         */
        public File prev() {
            int index = mCurrent - 1;
            if (0 <= index && index < mMax) {
                mCurrent = index;
                return mStack.get(index);
            }
            return null;
        }

        /**
         * 「進む」は有効？
         */
        public boolean hasNext() {
            int index = mCurrent + 1;
            return (0 <= index && index < mMax);
        }

        /**
         * 進む
         */
        public File next() {
            int index = mCurrent + 1;
            if (0 <= index && index < mMax) {
                mCurrent = index;
                return mStack.get(index);
            }
            return null;
        }
    }

    /**
     * 外部ストレージのパスを取得
     * @return  context.getExternalFilesDir()
     */
    private File getPrivateExtDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
            return getActivity().getExternalFilesDir(null);
        }
        return null;
    }

    /**
     * ダイアログ生成処理
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View contentView = inflateView(R.layout.dialog_file_selection);

        mFileListView = (UiFileListView) contentView.findViewById(R.id.file_view);
        mDirText = (TextView) contentView.findViewById(R.id.cur_dir);
        mFilenameEdit = (EditText) contentView.findViewById(R.id.file_name);
        mBack = (ImageButton) contentView.findViewById(R.id.prevHistory);
        mForward = (ImageButton) contentView.findViewById(R.id.nextHistory);
//        mCreate = (ImageButton) dlg.findViewById(R.id.createFolder);

        mDirText.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);

        if ((mType & UiFileListView.Type.DIRECTORY)==0) {
            // ディレクトリ変更がない場合はディレクトリ表示は不要
            mDirText.setVisibility(View.GONE);
        }
        if( !mFilenameField && mType != UiFileListView.Type.DIRECTORY && mType != UiFileListView.Type.ALL_DIR ){
            // ファイル名フィールド無効で、ディレクトリ選択用の場合以外は、Done を非表示にする
            contentView.findViewById(R.id.ok).setVisibility(View.INVISIBLE);
        }

        if (null == mBaseDir) {
            mBaseDir = getPrivateExtDirectory();
        }

        if (!mFilenameField) {
            contentView.findViewById(R.id.file_name_field).setVisibility(View.GONE);
        }
        if (null != mInitialFilename) {
            mFilenameEdit.setText(mInitialFilename);
        }

        if (null == mHistory) {
            mHistory = new History();
            mHistory.push(mBaseDir);
        }

        //mFileListView.showParentDir(false);
        mFileListView.setBaseDir(mBaseDir);
        mFileListView.setFilter(mExtensions, mType);
        mFileListView.setFileSelectedListener(new UiFileListView.IOnFileSelected() {
            @Override
            public boolean onFileSelected(File file) {
                if (file.isDirectory()) {
                    mBaseDir = file;
                    mDirText.setText(mBaseDir.getPath());
                    mHistory.push(mBaseDir);
                    updateButtons();
                } else {
                    mFile = file;
                    if (mFilenameField) {
                        mFilenameEdit.setText(file.getName());
                    } else {
                        if( mType != UiFileListView.Type.ALL_DIR) {
                            ok();
                            return false;
                        }
                    }
                }
                return true;
            }
        });

        updateButtons();
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = mHistory.prev();
                if (null != file) {
                    mFileListView.setBaseDir(file);
                    mDirText.setText(file.getPath());
                }
                updateButtons();
            }
        });
        mForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = mHistory.next();
                if (null != file) {
                    mFileListView.setBaseDir(file);
                    mDirText.setText(file.getPath());
                }
                updateButtons();
            }
        });
        // フォルダ作成
//        mCreate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            	FragmentManager manager = UiFileSelectDialog.this.getFragmentManager();
//
//                final String tag = "FileSelect_CreateFolder";
//                if (manager.findFragmentByTag(tag) != null) {
//                    // ダイアログ起動済みのため何もしない
//                    throw new RuntimeException( "CreateFolderDialog has already opened.");
//                }
//
//                final UiFileSelectionCreateFolder dlg = new UiFileSelectionCreateFolder();
//                dlg.setHideParentDialog(false);
//                dlg.setPreCloseConfirmListener(new IPreCloseConfirm() {
//
//                    @Override
//                    public boolean confermBeforeClose(UiDialog sdlg) {
//                        File file = makeFilePath(mBaseDir.getPath() + File.separator + dlg.getFolderName(), null);
//                        if (null==file || !file.exists()) {
//                        	if (null != file && !file.isHidden() && file.mkdir()) {
//                                return true;
//                        	}
//                        	else {
//                            	// フォルダを作成できませんでした。
//                    			CabinetUtils.showMsgDialog(getActivity(), getResources().getString(R.string.Msg_Failed_Create_Folder));
//                        	}
//                        }
//                        else {
//                        	// 同じ名前のフォルダが既に存在します。
//                			CabinetUtils.showMsgDialog(getActivity(), getResources().getString(R.string.Cabinet_CreateFolder_Msg_Alert_Error3));
//                        }
//                        return false;
//                    }
//                });
//                dlg.setOnClosedListener(new OnClosedListener() {
//
//					@Override
//					public void onDialogClosed(String tag, Bundle params, boolean done) {
//						if (done) {
//							mFileListView.setBaseDir(mBaseDir);
//						}
//					}
//				});
//                dlg.show(manager, tag);
//            }
//        });
        Dialog dlg = createDialog(contentView, R.id.ok, R.id.cancel);
        mDirText.setText(mBaseDir.getPath());
        return dlg;
    }

    /**
     * 戻る/進むボタンのグレーアウト状態を更新
     *
     * @param btn    ボタン
     * @param enable 有効無効
     */
    void updateButton(ImageButton btn, boolean enable) {
        btn.setEnabled(enable);
        btn.setAlpha((enable) ? 1.0f : 0.3f);
    }

    /**
     * 戻る/進むボタンのグレーアウト状態を更新
     */
    void updateButtons() {
        updateButton(mForward, mHistory.hasNext());
        updateButton(mBack, mHistory.hasPrev());
    }

    /**
     * Filename Fieldがtrueの場合のみ有効
     *
     * @return mBaseDir の中の mFilenameEditのファイル名
     */
    public String getResultFilePath() {
        if (!mFilenameField) {
            return null;
        }
        String name = mFilenameEdit.getText().toString();
        if (name.isEmpty()) {
            return null;
        }
        return mBaseDir.getPath() + "/" + name;
    }


    /**
     * ファイル選択の結果通知用i/f
     */
    public static interface IFileSelectionResult {
        /**
         * ファイル選択の結果を返す
         *
         * @param file    選択されたファイル・ディレクトリ
         * @param baseDir 最後に選択されていたディレクトリ（ディレクトリ選択の場合はnull）
         */
        public void fileSelected(File file, File baseDir);
    }

    /**
     * ファイルを選択する
     *
     * @param baseDir        ベースディレクトリ(nullならCmUtils.getPrivateExtDataDir()で取得）
     * @param extensions     拡張子リスト（nullならすべて）
     * @param allowChangeDir true: ディレクトリ変更を許可 / false:不許可
     * @param manager        FragmentManager（nullならUiCurrentActivityManagerから取得）
     * @param onResult       結果を返すi/f
     */
    public static void selectFile(Activity activity, File baseDir, String[] extensions, boolean allowChangeDir, int titleId, final IFileSelectionResult onResult) {
        final String tag = "selectFile";
        FragmentManager manager = activity.getFragmentManager();
        if (manager.findFragmentByTag(tag) != null) {
            // ダイアログ起動済みのため何もしない
            return;
        }

        final UiFileSelectDialog dlg = new UiFileSelectDialog(baseDir, extensions, (allowChangeDir) ? UiFileListView.Type.ALL : UiFileListView.Type.FILE, (titleId!=0)?titleId : R.string.Title_ReadFileDialog);
        dlg.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(boolean isOk) {
                if (isOk) {
                    try {
                        onResult.fileSelected(dlg.mFile, dlg.mBaseDir);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        dlg.show(manager, tag);
    }

    /**
     * ディレクトリを選択する
     *
     * @param baseDir  ベースディレクトリ(nullならCmUtils.getPrivateExtDataDir()で取得）
     * @param showFiles true:ファイルも単色表示 / false:ファイルは列挙しない
     * @param extensions ファイルを表示する場合の拡張子
     * @param manager  FragmentManager（nullならUiCurrentActivityManagerから取得）
     * @param onResult 結果を返すi/f   （fileSelected()のbaseDir引数は常にnull)
     */
    public static void selectDirectory(Activity activity, File baseDir, boolean showFiles, String[] extensions, int titleId,final IFileSelectionResult onResult) {
        final String tag = "selectDirectory";
        FragmentManager manager = activity.getFragmentManager();
        if (manager.findFragmentByTag(tag) != null) {
            // ダイアログ起動済みのため何もしない
            return;
        }

        final UiFileSelectDialog dlg = new UiFileSelectDialog(baseDir, extensions, (showFiles)?UiFileListView.Type.ALL_DIR : UiFileListView.Type.DIRECTORY, (titleId!=0)?titleId : R.string.Title_SelectFolder);
        dlg.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(boolean isOk) {
                if (isOk) {
                    try {
                        onResult.fileSelected(dlg.mBaseDir, dlg.mBaseDir);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        dlg.show(manager, tag);
    }

    private static File makeFilePath(String path, String ensureExt) {
        if( null == path ) {
            return null;
        }
        if (ensureExt != null && !ensureExt.isEmpty() && !path.endsWith(ensureExt)) {
            path = path + ensureExt;
        }
        return new File(path);
    }

    /**
     * ファイル保存用にファイルを選択する。既存ファイルが選ばれたら上書き確認する。
     *
     * @param baseDir   ベースディレクトリ(nullならCmUtils.getPrivateExtDataDir()で取得）
     * @param ensureExt ファイル名に必ずつけなければならない拡張子（nullなら気にしない）
     * @param onResult  結果を返すi/f
     */
    public static void selectFileForWrite(final Activity activity, File baseDir, String initialFilename, String[] extensions, final String ensureExt, int titleId, final IFileSelectionResult onResult) {
        FragmentManager manager = activity.getFragmentManager();

        final String tag = "selectFileForWrite";
        if (manager.findFragmentByTag(tag) != null) {
            // ダイアログ起動済みのため何もしない
            return;
        }

        final UiFileSelectDialog dlg = new UiFileSelectDialog(baseDir, extensions, UiFileListView.Type.ALL, (titleId!=0)?titleId:R.string.Title_WriteFileDialog);
        dlg.enableFilenameField(true, initialFilename);
        dlg.setOnOkListener( new OnOkListener() {
            boolean mConfirmed = false;

            @Override
            public boolean isReady() {
                if (mConfirmed) {
                    return true;
                }
                File file = makeFilePath(dlg.getResultFilePath(), ensureExt);
                if (null == file ) {
                    return false;
                }
                if(!file.exists()) {
                    return true;
                }
                Resources res = activity.getResources();
                UiMessageBox.selectYesNo(activity, res.getString(R.string.Msg_ConfirmOverwrite), file.getName(), new UiMessageBox.OnResult() {
                    @Override
                    public void onResult(boolean ok) {
                        if (ok) {
                            mConfirmed = true;
                            dlg.ok();
                        }
                    }
                });
                return false;
            }
        });


        dlg.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(boolean isOk) {
                if (isOk) {
                    File file = makeFilePath(dlg.getResultFilePath(), ensureExt);
                    if (file != null) {
                        try {
                            onResult.fileSelected(file, dlg.mBaseDir);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        dlg.show(manager, tag);
    }

}

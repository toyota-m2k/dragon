package com.michael.dragon.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import com.michael.dragon.R;

/**
 * @author M.TOYOTA 13/09/09 Created.
 * @author Copyright (C) 2013 MetaMoJi Corp. All Rights Reserved.
 */
public class UiMessageBox extends UiDialog implements DialogInterface.OnClickListener {

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TITLE = "title";
    public static final String KEY_OK = "ok";
    public static final String KEY_CANCEL = "cancel";
    public static final String KEY_NOTIFY_ON_CANCEL = "notifyCancel";

    public static interface OnResult {
        public void onResult(boolean ok);
    }

    protected OnResult mOnResult = null;

    public void setOnResultListener(OnResult listener) {
        mOnResult = listener;
    }

    public UiMessageBox() {
        super();
    }

    /**
     * カスタムな方法でカーソルをロードするためにオーバーライドする。
     * （バックグラウンドスレッドで実行される）
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String message = args.getString(KEY_MESSAGE);
        String title = args.getString(KEY_TITLE);
        String ok = args.getString(KEY_OK);
        String cancel = args.getString(KEY_CANCEL);
        boolean notifyCancel = args.getBoolean(KEY_NOTIFY_ON_CANCEL, true);

        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        Resources res = activity.getResources();
        builder.setMessage(message);
        builder.setTitle(title);
        if( ok != null) {
            builder.setPositiveButton(ok, this);
        }
        if( cancel != null ) {
            builder.setNegativeButton(cancel, notifyCancel ? this : null);
        }
        builder.setCancelable(false);
        return builder.create();
    }

    /**
     * This method will be invoked when a button in the dialog is clicked.
     *
     * @param dialog The dialog that received the click.
     * @param which  The button that was clicked (e.g.
     *               {@link android.content.DialogInterface#BUTTON1}) or the position
     *               of the item clicked.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if( null != mOnResult ) {
            mOnResult.onResult(which==DialogInterface.BUTTON_POSITIVE);
            mOnResult = null;
        }
    }

    public static String loadString(Context context, int id) {
        return context.getResources().getString(id);
    }

    public static void selectYesNo(Activity activity, int titleId, int messageId, OnResult onReslutListener ) {
        selectYesNo(activity, loadString(activity,titleId), loadString(activity,messageId), onReslutListener);
    }

    public static void selectYesNo(Activity activity, String title, String message, OnResult onReslutListener ) {
        Resources res = activity.getResources();
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        args.putString(KEY_TITLE, title);
        args.putString(KEY_OK, res.getString(R.string.Msg_YES));
        args.putString(KEY_CANCEL, res.getString(R.string.Msg_NO));
        UiMessageBox dlg = new UiMessageBox();
        dlg.setArguments(args);
        dlg.setOnResultListener(onReslutListener);
        dlg.show(activity.getFragmentManager(), "YesNo");
    }

    public static void selectOkCancel(Activity activity, String title, String message, OnResult onReslutListener ) {
        Resources res = activity.getResources();
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        args.putString(KEY_TITLE, title);
        args.putString(KEY_OK, res.getString(R.string.Msg_OK));
        args.putString(KEY_CANCEL, res.getString(R.string.Msg_CANCEL));
        UiMessageBox dlg = new UiMessageBox();
        dlg.setArguments(args);
        dlg.setOnResultListener(onReslutListener);
        dlg.show(activity.getFragmentManager(), "OkCancel");
    }

    public static void confirm(Activity activity, String title, String message, OnResult onReslutListener ) {
        Resources res = activity.getResources();
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        args.putString(KEY_TITLE, title);
        args.putString(KEY_OK, res.getString(R.string.Msg_Confirm));
        UiMessageBox dlg = new UiMessageBox();
        dlg.setArguments(args);
        dlg.setOnResultListener(onReslutListener);
        dlg.show(activity.getFragmentManager(), "Confirm");
    }
}

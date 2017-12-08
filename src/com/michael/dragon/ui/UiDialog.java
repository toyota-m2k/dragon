package com.michael.dragon.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.michael.dragon.R;

/**
 * @author M.TOYOTA 13/09/09 Created.
 * @author Copyright (C) 2013 MetaMoJi Corp. All Rights Reserved.
 */
public abstract class UiDialog extends DialogFragment {

    protected boolean mOk = false;

    public static interface OnOkListener {
        boolean isReady();
    }

    public static interface OnDismissListener {
        void onDismiss(boolean isOk);
    }

    protected OnOkListener mOnOkListener = null;
    protected OnDismissListener mOnDismissListener = null;

    /**
     * リソースからViewを生成する。
     *
     * @param viewId    viewのリソースID
     * @return  View
     */
    protected View inflateView(int viewId) {
        return getActivity().getLayoutInflater().inflate(viewId,null);
    }

    /**
     * ダイアログコンテナを作成して、contentViewをセットする。
     *
     * @param contentView   コンテントビュー
     * @return  生成したダイアログ
     */
    protected Dialog createDialog( View contentView, int idOk, int idCancel ) {
        setRetainInstance(true);

        Dialog dlg = new Dialog(getActivity(), R.style.DialogTheme);
        dlg.setContentView(contentView);

        View v;
        if( idOk != 0 && null != (v=dlg.findViewById(idOk))) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UiDialog.this.ok();
                }
            });
        }
        if( idCancel != 0 && null != (v=dlg.findViewById(idCancel))) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UiDialog.this.cancel();
                }
            });
        }
        return dlg;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        mOnDismissListener = listener;
    }
    public void setOnOkListener(OnOkListener listener) {
        mOnOkListener = listener;
    }

    public void cancel() {
        mOk = false;
        dismiss();
    }

    public void ok() {
        if( null!=mOnOkListener) {
            if( !mOnOkListener.isReady() ) {
                return;
            }
        }
        mOk = true;
        dismiss();
    }

    /**
     * デバイス回転時に消えてしまうことへの対策
     * http://stackoverflow.com/questions/12433397/android-dialogfragment-disappears-after-orientation-change
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if( mOnDismissListener!=null) {
            mOnDismissListener.onDismiss(mOk);
            mOnDismissListener = null;
        }
    }

    /**
     * カスタムな方法でカーソルをロードするためにオーバーライドする。
     * （バックグラウンドスレッドで実行される）
     */
    @Override
    public abstract Dialog onCreateDialog(Bundle savedInstanceState);
}

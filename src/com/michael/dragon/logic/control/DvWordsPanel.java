package com.michael.dragon.logic.control;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * @author M.TOYOTA 13/09/10 Created.
 * @author Copyright (C) 2013 MetaMoJi Corp. All Rights Reserved.
 */
public class DvWordsPanel extends FrameLayout {
    public DvWordsPanel(Context context) {
        super(context);
    }

    public DvWordsPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DvWordsPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    ArrayList<String> mArray;
    ArrayList<Button> mButtons = new ArrayList<Button>();;
    ArrayList<Button> mStockedButtons = new ArrayList<Button>();
    int mButtonLastId = 100;
    int mPrevWidth = 0;
    final int MARGIN = 8; // dp

    public int[] getDoneButtons() {
        int count = 0;
        for(int i=0, ci=mButtons.size() ; i<ci; i++ ){
            if( mButtons.get(i).getVisibility()==INVISIBLE) {
                count++;
            }
        }
        int[] result = new int[count];
        for(int i=0, ci=mButtons.size(), n=0 ; i<ci; i++ ){
            if( mButtons.get(i).getVisibility()==INVISIBLE) {
                result[n++] = i;
            }
        }
        return result;
    }

    public void setDoneButtons(int[] buttons) {
        for( int i=0, ci=buttons.length ; i<ci ; i++ ) {
            mButtons.get(buttons[i]).setVisibility(INVISIBLE);
        }
    }

    public void skipWord(String s) {
        for( int i=0, ci=mButtons.size() ; i<ci ; i++ ) {
            Button b = mButtons.get(i);
            if( b.getText().toString().equalsIgnoreCase(s) && b.getVisibility()!=INVISIBLE) {
                b.setVisibility(INVISIBLE);
                return;
            }
        }
    }


    public static interface OnSelectWord {
        public boolean onSelectWord( String word );
    }
    private OnSelectWord mSelectWordListener = null;
    public void setOnSelectWordListener(OnSelectWord listener) {
        mSelectWordListener = listener;
    }

    public void setWords(ArrayList<String> array) {
        mArray = array;
        removeButtons();
        createButtons();
        layoutButtons();
    }

    final int PADDING = 10;
    private Button newButton() {
        int c = mStockedButtons.size()-1;
        if( c>=0 ) {
            return mStockedButtons.remove(c);
        }
        Button b = new Button(getContext());
        b.setVisibility(INVISIBLE);
        b.setId(mButtonLastId++);
        // todo: bにプロパティをセット
        int padding = dipToPx(PADDING);
        b.setPadding(padding, 0, padding, 0);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if( v instanceof Button ) {
                    String word = ((Button) v).getText().toString();
                    if( mSelectWordListener != null ) {
                        if( mSelectWordListener.onSelectWord(word) ) {
                            v.setVisibility(INVISIBLE);
                        }
                    }
                }
            }
        });
        return b;
    }

    private void createButtons() {
        mPrevWidth = 0;
        if( null == mArray ) {
            return;
        }
        for( int i=0, ci=mArray.size() ; i<ci ; i++ ) {
            Button b = newButton();
            b.setVisibility(VISIBLE);
            b.setText(mArray.get(i));
            this.addView(b, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mButtons.add(b);
        }
    }

    private void removeButtons() {
        for( int i=0, ci=mButtons.size() ; i<ci ; i++ ){
            Button b = mButtons.get(i);
            b.setVisibility(INVISIBLE);
            this.removeView(b);
            mStockedButtons.add(b);
        }
        mButtons.clear();
        mPrevWidth = 0;
    }

    private void layoutButtons() {
        if( mButtons == null ) {
            return;
        }

//        ScrollView sv = (ScrollView)getParent();
//        Log.d("Hoge", "scroll view size = " + Integer.toString(sv.getWidth()) + "," + Integer.toString(sv.getHeight()));

        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int max_width = this.getWidth();
        if( max_width == mPrevWidth ) {
            return;
        }
        mPrevWidth = max_width;


        int width;
        int height = 0;
        int margin = dipToPx(MARGIN);
        int x = margin;
        int y = margin;
//        Log.d("Hoge", "------------------------");

        for( int i=0, ci=mButtons.size() ; i<ci; i++ ) {
            Button b = mButtons.get(i);
            b.measure(measureSpec, measureSpec);
            width = b.getMeasuredWidth();
//            Log.d("Hoge", "Width=" + Integer.toString(width));
            if( x + width + margin*2 > max_width ) {
                // 次の行に折り返す
                y += (height+margin);
                x = margin;
                height = 0;
            }

            FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(x, y,0,0);
            x += ( width + margin);
            height = Math.max(height, b.getMeasuredHeight());
            b.setLayoutParams(params);
        }
    }

//    private void layoutButtons() {
//        if( mButtons == null ) {
//            return;
//        }
//
////        ScrollView sv = (ScrollView)getParent();
////        Log.d("Hoge", "scroll view size = " + Integer.toString(sv.getWidth()) + "," + Integer.toString(sv.getHeight()));
//
//        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
//        int max_width = this.getWidth();
//        if( max_width == mPrevWidth ) {
//            return;
//        }
//        mPrevWidth = max_width;
//
//
//        int width = 0;
//        int height = 0;
//        int margin = dipToPx(MARGIN);
//        Button left = null, upper=null;
//
////        Log.d("Hoge", "begin Layout");
//
//        for( int i=0, ci=mButtons.size() ; i<ci; i++ ) {
//            Button b = mButtons.get(i);
//            b.measure(measureSpec, measureSpec);
//            width += (b.getMeasuredWidth() + margin*4);
//            height = Math.max(height, b.getMeasuredHeight());
//            if( width > max_width ) {
//                // 次の行に折り返す
//                upper = left;
//                left = null;
//                width = 0;
//            }
//
//            StringBuilder sb = new StringBuilder();
//
//            RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            if( upper == null ) {
//                // 一番上のボタン
//                params.addRule(ALIGN_PARENT_TOP,-1);
//                sb.append("Upper ");
//            } else {
//                // ２行目以降のボタン
//                params.addRule(BELOW, upper.getId());
//                sb.append("Lower ");
//            }
//
//            if( left == null) {
//                // 左端のボタン
//                params.addRule(ALIGN_PARENT_LEFT,-1);
//                sb.append("Left ");
//            } else {
//                // ２列目以降のボタン
//                params.addRule(RIGHT_OF,left.getId());
//                sb.append("Next  ");
//            }
//            left = b;
//            params.setMargins(margin, margin,margin,margin);
////            addView(b, params);
//            b.setLayoutParams(params);
//            Log.d("Hoge", sb.toString());
//        }
//        Log.d("Hoge", "end Layout");
//    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        layoutButtons();
    }

    /**
     * DisplayMetricsを取得
     */
    public DisplayMetrics getDisplayMetrics() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)getContext().getSystemService(android.content.Context.WINDOW_SERVICE);
        if(wm==null) {
            return null;
        }
        Display d = wm.getDefaultDisplay();
        if (null == d ){
            return null;
        }
        d.getMetrics(metrics);
        return metrics;
    }

    /**
     * dip(dp) を px に変換
     * @param dp
     * @return　px
     */
    public int dipToPx(float dp) {
        DisplayMetrics m = getDisplayMetrics();
        if( m==null ) {
            return Math.round(dp);
        }

        float scale = m.density;
        return Math.round(dp * scale);
    }

}

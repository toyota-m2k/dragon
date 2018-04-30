package com.michael.dragon.ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class UiTextDrawable extends Drawable {

    private final String mText;
    private final Paint mPaint;

    public UiTextDrawable(String text, int color, float size, boolean bold) {

        this.mText = text;

        this.mPaint = new Paint();
        mPaint.setColor(color);
        mPaint.setTextSize(size);
        mPaint.setAntiAlias(true);
        mPaint.setFakeBoldText(bold);
        //mPaint.setShadowLayer(6f, 0, 0, Color.BLACK);
        //mPaint.setStyle(Paint.Style.FILL);
        //mPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawText(mText, 0, 0, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public float getTextWidth() {
    	if (mPaint != null) {
        	return mPaint.measureText(mText.toString());    	
    	}
    	return 0;
    }
}

package com.michael.dragon.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class UiEditText extends EditText {

	//The image we are going to use for the Clear button
	private UiTextDrawable mCrossButton = null;
	private int mOffset = 0;

	public UiEditText(Context context) {
		super(context);

		init();
	}

	public UiEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

    public UiEditText(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	void init() {

		int color = getCurrentTextColor();
		float size = getTextSize()*1.5f;
		mCrossButton = new UiTextDrawable("✕", color, size, true);
		mOffset = Math.round(mCrossButton.getTextWidth());
		mCrossButton.setBounds(0, mOffset, mOffset, 0);
		int imeOption = getImeOptions();
		if ((imeOption & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_UNSPECIFIED) {
			setImeOptions(imeOption | EditorInfo.IME_ACTION_DONE);
		}

		// There may be initial text in the field, so we may need to display the  button
		handleClearButton();

		//if the Close image is displayed and the user remove his finger from the button, clear it. Otherwise do nothing
		this.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				UiEditText et = UiEditText.this;

				if (et.getCompoundDrawables()[2] == null)
					return false;

				if (event.getAction() != MotionEvent.ACTION_UP)
					return false;

				if (event.getX() > et.getWidth() - et.getPaddingRight() - mOffset) {

					et.setText("");

					UiEditText.this.handleClearButton();
				}

                return false;
			}
		});

		//if text changes, take care of the button
		this.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				UiEditText.this.handleClearButton();
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
	}

	void handleClearButton() {
		if (this.getText().toString().equals("")) {
			// add the clear button
			this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null, this.getCompoundDrawables()[3]);
		}
		else {
			//remove clear button
			this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], mCrossButton, this.getCompoundDrawables()[3]);
		}
	}
}

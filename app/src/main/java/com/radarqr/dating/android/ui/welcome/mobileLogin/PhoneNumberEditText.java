package com.radarqr.dating.android.ui.welcome.mobileLogin;

import android.content.Context;
import android.util.AttributeSet;


public class PhoneNumberEditText extends androidx.appcompat.widget.AppCompatEditText {
    public PhoneNumberEditText(Context context) {
        super(context);
    }

    public PhoneNumberEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhoneNumberEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        //super.onSelectionChanged(selStart, selEnd);
        super.onSelectionChanged(selStart, selEnd);
        setSelection(this.length());
    }
}

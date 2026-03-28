package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;

public class ButtonCancel extends ButtonBase {

    public ButtonCancel(Context context) {
        super(context);
        init();
    }

    public ButtonCancel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonCancel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.baseline_clear_24);
    }
}

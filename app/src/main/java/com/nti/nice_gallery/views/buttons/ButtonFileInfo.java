package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;

public class ButtonFileInfo extends ButtonBase {

    public ButtonFileInfo(Context context) {
        super(context);
        init();
    }

    public ButtonFileInfo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonFileInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.baseline_info_24);
    }

    private void onClick() {
    }
}

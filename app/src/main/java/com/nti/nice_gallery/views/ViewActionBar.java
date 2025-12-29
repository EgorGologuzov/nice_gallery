package com.nti.nice_gallery.views;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageButton;

public class ViewActionBar extends LinearLayout {

    private boolean isEnabled = true;

    public ViewActionBar(Context context) {
        super(context);
        init();
    }

    public ViewActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        if (this.isEnabled == isEnabled) {
            return;
        }

        this.isEnabled = isEnabled;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof AppCompatImageButton) {
                child.setEnabled(isEnabled);
            }
        }
    }
}

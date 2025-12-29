package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;

public class ButtonRefresh extends ButtonBase {

    private Runnable onRefresh;

    public ButtonRefresh(Context context) {
        super(context);
        init();
    }

    public ButtonRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonRefresh(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.baseline_refresh_24);
        setOnClickListener(v -> onClick());
    }

    public void setRefreshListener(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }

    private void onClick() {
        if (onRefresh != null) {
            onRefresh.run();
        }
    }
}

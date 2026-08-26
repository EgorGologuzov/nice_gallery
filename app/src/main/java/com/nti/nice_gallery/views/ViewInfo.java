package com.nti.nice_gallery.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.nti.nice_gallery.R;

public class ViewInfo extends FrameLayout {

    private ImageView iconView;
    private TextView messageView;
    private ProgressBar progressBar;

    public ViewInfo(@NonNull Context context) {
        super(context);
        init();
    }

    public ViewInfo(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewInfo(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        inflate(getContext(), R.layout.view_info, this);

        iconView = findViewById(R.id.iconView);
        messageView = findViewById(R.id.messageView);
        progressBar = findViewById(R.id.progressBar);
    }

    public void setIcon(@DrawableRes int resId) {
        iconView.setImageResource(resId);
    }

    public String getMessage() {
        return messageView.getText().toString();
    }

    public void setMessage(@StringRes int resId) {
        messageView.setText(resId);
    }

    public void setMessage(String message) {
        messageView.setText(message);
    }

    public void setIconVisibility(boolean isVisible) {
        iconView.setVisibility(isVisible ? VISIBLE : GONE);
    }

    public void setProgressBarVisibility(boolean isVisible) {
        progressBar.setVisibility(isVisible ? VISIBLE : GONE);
    }
}

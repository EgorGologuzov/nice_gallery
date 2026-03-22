package com.nti.nice_gallery.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;

public class ViewCheckBox extends FrameLayout {

    private ImageView checkSign;

    public ViewCheckBox(@NonNull Context context) {
        super(context);
        init();
    }

    public ViewCheckBox(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewCheckBox(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        inflate(getContext(), R.layout.view_check_box, this);

        checkSign = findViewById(R.id.checkSign);
        checkSign.setVisibility(View.GONE);
    }

    public boolean isChecked() {
        return checkSign.getVisibility() == View.VISIBLE;
    }

    public void setChecked(boolean isChecked) {
        checkSign.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }
}

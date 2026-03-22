package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.nti.nice_gallery.R;

public class ButtonBase extends androidx.appcompat.widget.AppCompatImageButton {

    public ButtonBase(Context context) {
        super(context);
        init();
    }

    public ButtonBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnClickListener(null);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != View.VISIBLE) {
            clearAnimation();
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        super.setOnClickListener(v -> {
            final Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_down_and_back);
            v.startAnimation(animation);
            if (listener != null) {
                listener.onClick(v);
            }
        });
    }
}

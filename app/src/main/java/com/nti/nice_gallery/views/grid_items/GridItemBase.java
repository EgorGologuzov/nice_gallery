package com.nti.nice_gallery.views.grid_items;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.GestureListener;
import com.nti.nice_gallery.views.ViewCheckBox;

import java.util.function.Consumer;

public class GridItemBase extends FrameLayout {

    protected ModelMediaFile model;
    protected ViewCheckBox checkBox;

    public GridItemBase(@NonNull Context context) {
        super(context);
        init();
    }

    public GridItemBase(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridItemBase(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener((Consumer<TouchArgs>) null);
    }

    public ModelMediaFile getModel() {
        return model;
    }

    public void setModel(ModelMediaFile model) {
        this.model = model;
        this.updateView();
    }

    protected void updateView() {}

    public boolean getIsSelected() {
        return checkBox.isChecked();
    }

    public void setIsSelected(boolean isSelected) {
        checkBox.setChecked(isSelected);
    }

    public void setCheckBoxVisibility(boolean isVisible) {
        checkBox.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        throw new IllegalStateException();
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        throw new IllegalStateException();
    }

    public void setOnTouchListener(Consumer<TouchArgs> listener) {
        Consumer<GestureListener.GestureArgs> onGestureDetected = gestureArgs -> {
            if (gestureArgs.gesture == GestureListener.Gesture.Down) {
                final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down_and_back);
                startAnimation(animation);
            }
            if (listener != null) {
                TouchArgs touchArgs = new TouchArgs(this, gestureArgs);
                listener.accept(touchArgs);
            }
        };

        View.OnTouchListener onTouch = new View.OnTouchListener() {
            final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureListener(onGestureDetected), new Handler(Looper.getMainLooper()));
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        };

        super.setOnTouchListener(onTouch);
    }

    public static class TouchArgs {
        public final GridItemBase item;
        public final GestureListener.GestureArgs gestureArgs;

        public TouchArgs(
                GridItemBase item,
                GestureListener.GestureArgs gestureArgs
        ) {
            this.item = item;
            this.gestureArgs = gestureArgs;
        }
    }
}

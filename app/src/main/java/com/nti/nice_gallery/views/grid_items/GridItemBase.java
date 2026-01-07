package com.nti.nice_gallery.views.grid_items;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelMediaFile;

import java.util.function.Consumer;

public class GridItemBase extends FrameLayout {

    protected ModelMediaFile model;

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
        setOnClickListener((Consumer<GridItemBase>) null);
    }

    public ModelMediaFile getModel() {
        return model;
    }

    public void setModel(ModelMediaFile model) {
        this.model = model;
        this.updateView();
    }

    protected void updateView() {}

    @Override
    public void setOnClickListener(OnClickListener listener) {
        throw new IllegalStateException();
    }

    public void setOnClickListener(Consumer<GridItemBase> listener) {
        super.setOnClickListener(v -> {
            final Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_down_and_back);
            v.startAnimation(animation);
            if (listener != null) {
                listener.accept(this);
            }
        });
    }
}

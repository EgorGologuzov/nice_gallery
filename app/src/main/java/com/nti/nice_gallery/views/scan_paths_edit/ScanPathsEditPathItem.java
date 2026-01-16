package com.nti.nice_gallery.views.scan_paths_edit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;

import java.util.function.Consumer;

public class ScanPathsEditPathItem extends LinearLayout {

    ImageButton buttonDelete;
    TextView pathView;

    String path;

    Consumer<ScanPathsEditPathItem> buttonDeleteClickListener;

    public ScanPathsEditPathItem(Context context) {
        super(context);
        init();
    }

    public ScanPathsEditPathItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanPathsEditPathItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setOrientation(VERTICAL);

        inflate(getContext(), R.layout.scan_paths_edit_path_item, this);

        buttonDelete = findViewById(R.id.buttonDelete);
        pathView = findViewById(R.id.pathView);

        View.OnClickListener onButtonDeleteClick = btn -> {
            if (buttonDeleteClickListener != null) {
                buttonDeleteClickListener.accept(this);
            }
        };

        buttonDelete.setOnClickListener(onButtonDeleteClick);

        updateView();
    }

    private void updateView() {
        pathView.setText(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        updateView();
    }

    public void setButtonDeleteClickListener(Consumer<ScanPathsEditPathItem> l) {
        this.buttonDeleteClickListener = l;
    }
}

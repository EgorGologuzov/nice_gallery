package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.ReadOnlyList;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ButtonSelectAll extends ButtonBase {

    private HashMap<String, ModelMediaFile> selectedFiles;
    private ReadOnlyList<ModelMediaFile> allFiles;
    private Consumer<ButtonSelectAll> onSelectedFilesChangedListener;
    private boolean checked = false;

    public ButtonSelectAll(Context context) {
        super(context);
        init();
    }

    public ButtonSelectAll(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonSelectAll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.baseline_check_box_outline_blank_24);
        setOnClickListener(v -> onClick());
    }

    public void setSelectedFiles(HashMap<String, ModelMediaFile> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    public void setAllFiles(ReadOnlyList<ModelMediaFile> allFiles) {
        this.allFiles = allFiles;
    }

    public void setSelectedFilesChangedListener(Consumer<ButtonSelectAll> l) {
        this.onSelectedFilesChangedListener = l;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        setImageResource(checked ? R.drawable.baseline_check_box_24 : R.drawable.baseline_check_box_outline_blank_24);
    }

    private void onClick() {
        setChecked(!checked);

        if (selectedFiles == null || allFiles == null) {
            return;
        }

        if (checked) {
            selectedFiles.putAll(allFiles.stream().collect(Collectors.toMap(f -> f.path, Function.identity())));
        } else {
            selectedFiles.clear();
        }

        if (onSelectedFilesChangedListener != null) {
            onSelectedFilesChangedListener.accept(this);
        }
    }
}

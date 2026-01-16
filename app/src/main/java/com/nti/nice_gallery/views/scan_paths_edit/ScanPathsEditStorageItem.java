package com.nti.nice_gallery.views.scan_paths_edit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.models.ModelStorage;
import com.nti.nice_gallery.utils.Convert;
import com.nti.nice_gallery.utils.ManagerOfDialogs;
import com.nti.nice_gallery.utils.ReadOnlyList;
import com.nti.nice_gallery.views.buttons.ButtonBase;
import com.nti.nice_gallery.views.buttons.ButtonChoiceScanMode;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ScanPathsEditStorageItem extends LinearLayout {

    ButtonBase buttonAdd;
    ButtonChoiceScanMode buttonScanMode;
    TextView storageInfoView;
    LinearLayout pathsList;

    TextView infoView;

    ModelStorage storage;
    ModelScanParams.StorageParams storageParams;
    Consumer<ScanPathsEditStorageItem> storageParamsChangeListener;

    ManagerOfDialogs managerOfDialogs;
    Convert convert;

    public ScanPathsEditStorageItem(@NonNull Context context) {
        super(context);
        init();
    }

    public ScanPathsEditStorageItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanPathsEditStorageItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.scan_paths_edit_storage_item, this);

        buttonAdd = findViewById(R.id.buttonAdd);
        buttonScanMode = findViewById(R.id.buttonScanMode);
        storageInfoView = findViewById(R.id.storageInfoView);
        pathsList = findViewById(R.id.pathsList);

        infoView = new TextView(getContext());

        managerOfDialogs = new ManagerOfDialogs(getContext());
        convert = new Convert(getContext());

        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setOrientation(VERTICAL);
        this.setPadding(0, convert.dpToPx(8), 0, convert.dpToPx(8));

        layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        infoView.setLayoutParams(layoutParams);

        View.OnClickListener onButtonAddClick = btn -> {

            Consumer<String> onOk = path -> {
                addPath(path);
                if (storageParamsChangeListener != null) {
                    storageParamsChangeListener.accept(this);
                }
            };

            Runnable onCancel = null;

            managerOfDialogs.showPrompt(
                    R.string.dialog_title_prompt_input_path,
                    storage != null ? storage.path : null,
                    onOk,
                    onCancel
            );
        };

        Consumer<ButtonChoiceScanMode> onScanModeChange = btn -> {
            if (storageParamsChangeListener != null) {
                storageParamsChangeListener.accept(this);
            }
        };

        buttonAdd.setOnClickListener(onButtonAddClick);
        buttonScanMode.setScanModeChangeListener(onScanModeChange);

        updateView();
    }

    private void updateView() {
        if (storage == null) {
            storageInfoView.setText(null);
            setNoItemsInfo();
            return;
        }

        String storageInfo = getContext().getString(R.string.format_storage_info, storage.description, storage.name);
        storageInfoView.setText(storageInfo);

        if (storageParams != null) {
            buttonScanMode.setScanMode(storageParams.scanMode);
        }

        if (storageParams == null || storageParams.paths == null || storageParams.paths.isEmpty()) {
            setNoItemsInfo();
            return;
        }

        for (String path : storageParams.paths) {
            addPath(path);
        }
    }

    public void addPath(String path) {
        if (path == null) {
            return;
        }

        path = path.trim();

        if (path.isEmpty()) {
            return;
        }

        ScanPathsEditPathItem pathItem = new ScanPathsEditPathItem(getContext());

        Consumer<ScanPathsEditPathItem> onButtonDeleteClick = item -> {
            pathsList.removeView(item);
            if (storageParamsChangeListener != null) {
                storageParamsChangeListener.accept(this);
            }
            if (pathsList.getChildCount() == 0) {
                setNoItemsInfo();
            }
        };

        pathItem.setPath(path);
        pathItem.setButtonDeleteClickListener(onButtonDeleteClick);
        pathsList.removeView(infoView);
        pathsList.addView(pathItem);
    }

    private void setNoItemsInfo() {
        pathsList.removeAllViews();
        infoView.setText(R.string.message_no_items);
        pathsList.addView(infoView);
    }

    private ModelScanParams.StorageParams parseStorageParams() {
        if (storage == null) {
            return null;
        }

        ArrayList<String> paths = new ArrayList<>();
        int childCount = pathsList.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = pathsList.getChildAt(i);
            if (child instanceof ScanPathsEditPathItem) {
                ScanPathsEditPathItem item = (ScanPathsEditPathItem) child;
                paths.add(item.getPath());
            }
        }

        ModelScanParams.StorageParams newStorageParams = new ModelScanParams.StorageParams(
                storage.name,
                buttonScanMode.getScanMode(),
                new ReadOnlyList<>(paths)
        );

        return  newStorageParams;
    }

    public ModelStorage getStorage() {
        return storage;
    }

    public void setStorage(ModelStorage storage) {
        this.storage = storage;
        updateView();
    }

    public ModelScanParams.StorageParams getStorageParams() {
        storageParams = parseStorageParams();
        return storageParams;
    }

    public void setStorageParams(ModelScanParams.StorageParams storageParams) {
        this.storageParams = storageParams;
        updateView();
    }

    public void setStorageParamsChangeListener(Consumer<ScanPathsEditStorageItem> l) {
        this.storageParamsChangeListener = l;
    }
}

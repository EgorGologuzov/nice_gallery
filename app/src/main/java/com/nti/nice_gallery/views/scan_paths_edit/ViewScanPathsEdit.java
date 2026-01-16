package com.nti.nice_gallery.views.scan_paths_edit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfFiles;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.models.ModelGetStoragesRequest;
import com.nti.nice_gallery.models.ModelGetStoragesResponse;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.models.ModelStorage;
import com.nti.nice_gallery.utils.ReadOnlyList;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public class ViewScanPathsEdit extends LinearLayout {

    TextView infoView;

    IManagerOfFiles managerOfFiles;
    IManagerOfSettings managerOfSettings;

    public ViewScanPathsEdit(Context context) {
        super(context);
        init();
    }

    public ViewScanPathsEdit(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewScanPathsEdit(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        infoView = new TextView(getContext());

        managerOfFiles = Domain.getManagerOfFiles(getContext());
        managerOfSettings = Domain.getManagerOfSettings(getContext());

        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setOrientation(VERTICAL);

        ModelGetStoragesRequest request = null;

        Consumer<ScanPathsEditStorageItem> onStorageItemParamsChange = item -> {
            saveParams();
        };

        Consumer<ModelGetStoragesResponse> handleResponse = response -> {
            this.removeAllViews();

            if (response == null || response.storages == null || response.storages.isEmpty()) {
                infoView.setText(R.string.message_no_items);
                this.addView(infoView);
            }

            ModelScanParams scanParams = managerOfSettings.getScanParams();

            for (ModelStorage storage : response.storages) {
                if (storage.error != null) {
                    continue;
                }

                ScanPathsEditStorageItem storageItem = new ScanPathsEditStorageItem(getContext());
                storageItem.setStorage(storage);
                storageItem.setStorageParamsChangeListener(onStorageItemParamsChange);

                if (scanParams != null && scanParams.storagesParams != null) {
                    ModelScanParams.StorageParams storageScanParams = scanParams.storagesParams
                            .stream()
                            .filter(sp -> Objects.equals(sp.storageName, storage.name))
                            .findFirst()
                            .orElse(null);

                    if (storageScanParams != null) {
                        storageItem.setStorageParams(storageScanParams);
                    }
                }

                this.addView(storageItem);
            }
        };

        managerOfFiles.getStoragesAsync(request, handleResponse);
    }

    private void saveParams() {
        ArrayList<ModelScanParams.StorageParams> params = new ArrayList<>();
        int childCount = this.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = this.getChildAt(i);
            if (child instanceof ScanPathsEditStorageItem) {
                ScanPathsEditStorageItem item = (ScanPathsEditStorageItem) child;
                params.add(item.getStorageParams());
            }
        }

        ModelScanParams scanParams = new ModelScanParams(
                new ReadOnlyList<>(params)
        );

        managerOfSettings.saveScanParams(scanParams);
    }
}

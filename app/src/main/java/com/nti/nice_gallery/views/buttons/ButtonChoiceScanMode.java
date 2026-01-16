package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.utils.ManagerOfDialogs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ButtonChoiceScanMode extends ButtonBase {

    ModelScanParams.ScanMode scanMode;
    List<ModelScanParams.ScanMode> variants;

    ManagerOfDialogs managerOfDialogs;

    Consumer<ButtonChoiceScanMode> scanModeChangeListener;

    public ButtonChoiceScanMode(Context context) {
        super(context);
        init();
    }

    public ButtonChoiceScanMode(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonChoiceScanMode(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        scanMode = ModelScanParams.ScanMode.ScanAll;
        variants = Arrays.stream(ModelScanParams.ScanMode.values()).collect(Collectors.toList());

        managerOfDialogs = new ManagerOfDialogs(getContext());

        setOnClickListener(v -> onClick());

        updateView();
    }

    private void updateView() {
        if (scanMode == null) {
            scanMode = ModelScanParams.ScanMode.ScanAll;
        }

        switch (scanMode) {
            case ScanAll: setImageResource(R.drawable.baseline_select_all_24); break;
            case ScanPathsInListOnly: setImageResource(R.drawable.baseline_playlist_add_check_24); break;
            case ScanPathsNotInListOnly: setImageResource(R.drawable.baseline_playlist_remove_24); break;
            case IgnoreStorage: setImageResource(R.drawable.baseline_not_interested_24); break;
        }
    }

    private void onClick() {
        int selectedModeIndex = variants.indexOf(scanMode);

        managerOfDialogs.showChooseOne(
                R.string.dialog_title_scan_mode,
                R.array.enum_scan_modes,
                selectedModeIndex,
                selectedIndex -> {
                    scanMode = variants.get(selectedIndex);
                    updateView();
                    if (scanModeChangeListener != null) {
                        scanModeChangeListener.accept(this);
                    }
                }
        );
    }

    public ModelScanParams.ScanMode getScanMode() {
        return scanMode;
    }

    public void setScanMode(ModelScanParams.ScanMode scanMode) {
        this.scanMode = scanMode;
        updateView();
    }

    public void setScanModeChangeListener(Consumer<ButtonChoiceScanMode> l) {
        this.scanModeChangeListener = l;
    }
}

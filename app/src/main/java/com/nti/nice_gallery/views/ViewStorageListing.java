package com.nti.nice_gallery.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.utils.ReadOnlyList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ViewStorageListing extends RadioGroup {
    private ModelScanParams modelScanLists;
    private String currentStorage;

    public ViewStorageListing(Context context) {
        super(context);
    }

    public ViewStorageListing(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public String getCurrentlySelectedStorage(){
        return currentStorage;
    }

    public void setScanList(ModelScanParams modelScanLists){
        this.modelScanLists=modelScanLists;
        update();
    }

    private void update() {
        removeAllViews();
        for (ModelScanParams.StorageParams storageParams : modelScanLists.storagesParams){
            RadioButton radioButton= new RadioButton(getContext());
            radioButton.setText(storageParams.storageName);
            radioButton.setId(View.generateViewId());
            addView(radioButton);
            SwitchCompat switchComponent=new SwitchCompat(getContext());
            switchComponent.setChecked(storageParams.scanMode == ModelScanParams.ScanMode.ScanAll);
            switchComponent.setOnClickListener(view -> {
                ModelScanParams.StorageParams updatedStorageParams = new ModelScanParams.StorageParams(
                        storageParams.storageName,
                        storageParams.scanMode == ModelScanParams.ScanMode.ScanAll ? ModelScanParams.ScanMode.IgnoreStorage : ModelScanParams.ScanMode.ScanAll,
                        storageParams.paths
                );

                List<ModelScanParams.StorageParams> updatedStoragesParams = modelScanLists.storagesParams
                        .stream()
                        .map(sp -> !Objects.equals(sp.storageName, storageParams.storageName) ? sp : updatedStorageParams)
                        .collect(Collectors.toList());

                modelScanLists = new ModelScanParams(new ReadOnlyList<>(updatedStoragesParams));
            });
            switchComponent.setText("Сканировать все");
            switchComponent.setId(View.generateViewId());
            addView(switchComponent);
            for(String path: storageParams.paths){
                TextView textView=new TextView(getContext());
                textView.setText(path);
                addView(textView);
            }
        }
        setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton selectedRadio = (RadioButton) findViewById(getCheckedRadioButtonId());
            currentStorage =selectedRadio.getText().toString();
        });
    }
}

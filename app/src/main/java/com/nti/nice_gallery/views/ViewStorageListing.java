package com.nti.nice_gallery.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.models.ModelScanList;

public class ViewStorageListing extends RadioGroup {
    private ModelScanList modelScanLists;
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

    public void setScanList(ModelScanList modelScanLists){
        this.modelScanLists=modelScanLists;
        update();
    }

    private void update() {
        removeAllViews();
        for (ModelScanList.StoragePathsGroup storagePathsGroup:modelScanLists.scanList){
            RadioButton radioButton= new RadioButton(getContext());
            radioButton.setText(storagePathsGroup.storageName);
            radioButton.setId(View.generateViewId());
            addView(radioButton);
            SwitchCompat switchComponent=new SwitchCompat(getContext());
            switchComponent.setChecked(storagePathsGroup.scanAllStorage);
            switchComponent.setOnClickListener(view -> {
                storagePathsGroup.scanAllStorage=!storagePathsGroup.scanAllStorage;
            });
            switchComponent.setText("Сканировать все");
            switchComponent.setId(View.generateViewId());
            addView(switchComponent);
            for(String path:storagePathsGroup.paths){
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

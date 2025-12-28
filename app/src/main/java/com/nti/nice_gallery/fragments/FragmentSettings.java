package com.nti.nice_gallery.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.nti.nice_gallery.data.ManagerOfSettings_Test1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.IManagerOfFiles;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.data.ManagerOfFiles_Test1;
import com.nti.nice_gallery.models.ModelScanList;
import com.nti.nice_gallery.models.ModelStorage;
import com.nti.nice_gallery.views.ViewStorageListing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FragmentSettings extends Fragment {
    IManagerOfSettings managerOfSettings;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ViewStorageListing storageListing=view.findViewById(R.id.view_storage_listing);
        managerOfSettings = new ManagerOfSettings_Test1(view.getContext());
        IManagerOfFiles managerOfFiles = new ManagerOfFiles_Test1(getContext());
        List<ModelStorage> storages = managerOfFiles.getAllStorages();
        ModelScanList currentScanList =new ModelScanList();
        List<ModelScanList.StoragePathsGroup> storagePathsGroups = new ArrayList<>();


        for (ModelStorage storage:storages){
            ModelScanList.StoragePathsGroup storagePathsGroup=new ModelScanList.StoragePathsGroup();
            storagePathsGroup.storageName=storage.name;
            storagePathsGroup.scanAllStorage=false;

            storagePathsGroup.paths=new ArrayList<>(Arrays.asList("/Storage1/customPath","/Storage1/customPath2"));
            storagePathsGroups.add(storagePathsGroup);
        }
        currentScanList.scanList=storagePathsGroups;
        storageListing.setScanList(currentScanList);

        Button button=view.findViewById(R.id.button);
        EditText editText=view.findViewById(R.id.editTextText);
        button.setOnClickListener(view1 -> {
            if (storageListing.getCheckedRadioButtonId()!=-1)
            {
                currentScanList.scanList.stream()
                        .filter(item -> Objects.equals(item.storageName, storageListing.getCurrentlySelectedStorage()))
                        .forEach(item1 -> item1.paths.add(editText.getText().toString()));
                storageListing.setScanList(currentScanList);
            }
        });

        Button saveButton=view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view1 -> {
            managerOfSettings=new ManagerOfSettings_Test1(getContext());
            managerOfSettings.saveScanList(new ArrayList<>((Collection) currentScanList));
            Log.d("Msg", String.valueOf(storageListing.getCurrentlySelectedStorage()));
        });

        return view;
    }
}

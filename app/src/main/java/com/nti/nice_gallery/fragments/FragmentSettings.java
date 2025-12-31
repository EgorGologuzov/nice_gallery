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
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.models.ModelStorage;
import com.nti.nice_gallery.utils.ReadOnlyList;
import com.nti.nice_gallery.views.ViewStorageListing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FragmentSettings extends Fragment {

    private static final String LOG_TAG = "FragmentSettings";

    ViewStorageListing storageListing;
    Button addPathButton;
    Button saveButton;
    EditText pathEdit;

    IManagerOfSettings managerOfSettings;
    IManagerOfFiles managerOfFiles;
    ModelScanParams currentScanParams;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        storageListing = view.findViewById(R.id.view_storage_listing);
        addPathButton = view.findViewById(R.id.button);
        pathEdit = view.findViewById(R.id.editTextText);
        saveButton = view.findViewById(R.id.saveButton);

        managerOfSettings = new ManagerOfSettings_Test1(view.getContext());
        managerOfFiles = new ManagerOfFiles_Test1(getContext());

        saveButton.setOnClickListener(v -> {
            managerOfSettings.saveScanParams(currentScanParams);
            Log.d(LOG_TAG, String.valueOf(storageListing.getCurrentlySelectedStorage()));
        });

        managerOfFiles.getStoragesAsync(null, response -> {
            List<ModelScanParams.StorageParams> storageParams = new ArrayList<>();

            for (ModelStorage storage : response.storages){
                String storageName = storage.name;
                ModelScanParams.ScanMode scanMode = ModelScanParams.ScanMode.ScanAll;
                List<String> paths = new ArrayList<>(Arrays.asList("/Storage1/customPath","/Storage1/customPath2"));

                ModelScanParams.StorageParams params = new ModelScanParams.StorageParams(
                        storageName,
                        scanMode,
                        new ReadOnlyList<>(paths)
                );

                storageParams.add(params);
            }

            currentScanParams = new ModelScanParams(new ReadOnlyList<>(storageParams));

            getActivity().runOnUiThread(() -> storageListing.setScanList(currentScanParams));
        });

        addPathButton.setOnClickListener(v -> {
            if (storageListing.getCheckedRadioButtonId() == -1) {
                return;
            }

            // находим парметры выбранного хранилища
            ModelScanParams.StorageParams selectedStorageParams = currentScanParams.storagesParams
                    .stream()
                    .filter(sp -> Objects.equals(sp.storageName, storageListing.getCurrentlySelectedStorage()))
                    .findFirst()
                    .get();

            // так как модель параметров неизменяемая, пересоздаем ее, добавляя новый путь, остальные данные просто копируем
            List<String> updatedPaths = selectedStorageParams.paths.toList();
            updatedPaths.add(pathEdit.getText().toString());

            ModelScanParams.StorageParams updatedStorageParams = new ModelScanParams.StorageParams(
                    selectedStorageParams.storageName,
                    selectedStorageParams.scanMode,
                    new ReadOnlyList<>(updatedPaths)
            );

            List<ModelScanParams.StorageParams> updatedStoragesParams = currentScanParams.storagesParams
                    .stream()
                    .map(sp -> !Objects.equals(sp.storageName, updatedStorageParams.storageName) ? sp : updatedStorageParams)
                    .collect(Collectors.toList());

            ModelScanParams updatedScanParams = new ModelScanParams(new ReadOnlyList<>(updatedStoragesParams));
            currentScanParams = updatedScanParams;

            storageListing.setScanList(currentScanParams);
        });

        return view;
    }
}

package com.nti.nice_gallery.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfFiles;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.views.ViewMediaGrid;
import com.nti.nice_gallery.views.buttons.ButtonChoiceFilters;
import com.nti.nice_gallery.views.buttons.ButtonChoiceGridVariant;
import com.nti.nice_gallery.views.buttons.ButtonChoiceSortVariant;
import com.nti.nice_gallery.views.buttons.ButtonRefresh;
import com.nti.nice_gallery.views.buttons.ButtonScanningReport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragmentMediaAll extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_all, container, false);

        ViewMediaGrid viewMediaGrid = view.findViewById(R.id.viewMediaGrid);
        ButtonChoiceGridVariant buttonGridVariant = view.findViewById(R.id.buttonGridVariant);
        ButtonChoiceSortVariant buttonSortVariant = view.findViewById(R.id.buttonSortVariant);
        ButtonChoiceFilters buttonFilters = view.findViewById(R.id.buttonFilters);
        ButtonRefresh buttonRefresh = view.findViewById(R.id.buttonRefresh);
        ButtonScanningReport buttonScanningReport = view.findViewById(R.id.buttonScanningReport);

        Runnable loadFiles = () -> {
            viewMediaGrid.trySetStateScanningInProgress(true);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                IManagerOfFiles managerOfFiles = Domain.getManagerOfFiles(getContext());
                ModelGetFilesResponse response = managerOfFiles.getFiles(new ModelGetFilesRequest(
                        null,
                        null,
                        null
                ));

                getActivity().runOnUiThread(() -> {
                    viewMediaGrid.setItems(response.files);
                    buttonScanningReport.setSource(response);
                    viewMediaGrid.trySetStateScanningInProgress(false);
                    executor.shutdown();
                });
            });
        };

        buttonGridVariant.setVariantChangeListener(viewMediaGrid::setGridVariant);
        buttonRefresh.setRefreshListener(loadFiles);

        loadFiles.run();

        return view;
    }
}

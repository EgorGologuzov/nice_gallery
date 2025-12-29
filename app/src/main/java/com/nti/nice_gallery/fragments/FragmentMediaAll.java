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
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ReadOnlyList;
import com.nti.nice_gallery.views.ViewActionBar;
import com.nti.nice_gallery.views.ViewMediaGrid;
import com.nti.nice_gallery.views.buttons.ButtonChoiceFilters;
import com.nti.nice_gallery.views.buttons.ButtonChoiceGridVariant;
import com.nti.nice_gallery.views.buttons.ButtonChoiceSortVariant;
import com.nti.nice_gallery.views.buttons.ButtonRefresh;
import com.nti.nice_gallery.views.buttons.ButtonScanningReport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragmentMediaAll extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_all, container, false);

        ViewMediaGrid viewMediaGrid = view.findViewById(R.id.viewMediaGrid);
        ViewActionBar viewActionBar = view.findViewById(R.id.viewActionBar);
        ButtonChoiceGridVariant buttonGridVariant = view.findViewById(R.id.buttonGridVariant);
        ButtonChoiceSortVariant buttonSortVariant = view.findViewById(R.id.buttonSortVariant);
        ButtonChoiceFilters buttonFilters = view.findViewById(R.id.buttonFilters);
        ButtonRefresh buttonRefresh = view.findViewById(R.id.buttonRefresh);
        ButtonScanningReport buttonScanningReport = view.findViewById(R.id.buttonScanningReport);

        Runnable loadFiles = () -> {

//            ModelGetFilesRequest.SortVariant sortVariant = null;
            ModelGetFilesRequest.SortVariant sortVariant = ModelGetFilesRequest.SortVariant.ByCreateAtDesc;
            boolean foldersFirst = true;

            boolean ignoreHidden = true;
//            List<ModelMediaFile.Type> types = null;
            List<ModelMediaFile.Type> types = new ArrayList<>();
//                types.add(ModelMediaFile.Type.Folder);
//                types.add(ModelMediaFile.Type.Image);
//                types.add(ModelMediaFile.Type.Video);
            Long minWeight = null;
            Long maxWeight = null;
//            Long minWeight = 1_000_000L;
//            Long maxWeight = 5_000_000L;
            LocalDateTime minCreateAt = null;
            LocalDateTime maxCreateAt = null;
//            LocalDateTime minCreateAt = LocalDateTime.now().minusDays(29);
//            LocalDateTime maxCreateAt = LocalDateTime.now().plusDays(0);
            LocalDateTime minUpdateAt = null;
            LocalDateTime maxUpdateAt = null;
//            LocalDateTime minUpdateAt = LocalDateTime.now().minusDays(14);
//            LocalDateTime maxUpdateAt = LocalDateTime.now().plusDays(0);
            List<String> extensions = null;
//            List<String> extensions = new ArrayList<>();
//                extensions.add("jpg");
//                extensions.add("jpeg");
//                extensions.add("png");
//                extensions.add("bmp");
//                extensions.add("mp4");
//                extensions.add("wmv");
            Integer minDuration = null;
            Integer maxDuration = null;
//            Integer minDuration = 30;
//            Integer maxDuration = 100;

            ModelFilters filters = new ModelFilters(
                    ignoreHidden,
                    new ReadOnlyList<>(types),
                    minWeight,
                    maxWeight,
                    minCreateAt,
                    maxCreateAt,
                    minUpdateAt,
                    maxUpdateAt,
                    new ReadOnlyList<>(extensions),
                    minDuration,
                    maxDuration
            );

            viewMediaGrid.trySetStateScanningInProgress(true);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                IManagerOfFiles managerOfFiles = Domain.getManagerOfFiles(getContext());
                ModelGetFilesResponse response = managerOfFiles.getFiles(new ModelGetFilesRequest(
                        filters,
                        sortVariant,
                        foldersFirst
                ));

                getActivity().runOnUiThread(() -> {
                    viewMediaGrid.setItems(response.files);
                    buttonScanningReport.setSource(response);
                    viewMediaGrid.trySetStateScanningInProgress(false);
                    executor.shutdown();
                });
            });
        };

        viewMediaGrid.setStateChangeListener(v -> viewActionBar.setIsEnabled(v.getState() == ViewMediaGrid.State.StandbyMode));
        buttonGridVariant.setVariantChangeListener(btn -> viewMediaGrid.setGridVariant(btn.getSelectedVariant()));
        buttonRefresh.setRefreshListener(loadFiles);

        loadFiles.run();

        return view;
    }
}

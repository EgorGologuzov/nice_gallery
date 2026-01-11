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
import com.nti.nice_gallery.data.ManagerOfFiles;
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.utils.ManagerOfDialogs;
import com.nti.nice_gallery.utils.ManagerOfThreads;
import com.nti.nice_gallery.utils.ReadOnlyList;
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
import java.util.function.Consumer;

public class FragmentMediaAll extends Fragment {

    private ModelGetFilesRequest request;

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

        IManagerOfFiles managerOfFiles = Domain.getManagerOfFiles(getContext());
        ManagerOfThreads managerOfThreads = new ManagerOfThreads(getContext());
        ManagerOfDialogs managerOfDialogs = new ManagerOfDialogs(getContext());

        request = getTestRequest();

        Consumer<ModelGetFilesRequest.SortVariant> updateRequestSortVariant = newSortVariant -> {
            if (request == null) {
                return;
            }

            request = new ModelGetFilesRequest(
                    request.path,
                    request.scanParams,
                    request.filters,
                    newSortVariant,
                    request.foldersFirst
            );
        };

        Runnable refreshFilesList = () -> {
            viewMediaGrid.trySetStateScanningInProgress(true);
            managerOfFiles.getFilesAsync(request, response -> {
                managerOfThreads.runOnUiThread(() -> {
                    if (response.error == null) {
                        viewMediaGrid.setItems(response.files);
                        viewMediaGrid.trySetStateScanningInProgress(false);
                        buttonScanningReport.setSource(response);
                    } else {
                        viewMediaGrid.trySetStateScanningInProgress(false);
                        managerOfDialogs.showInfo(R.string.dialog_title_something_wrong, R.string.message_error_scanning_failed);
                    }
                });
            });
        };

        Consumer<ViewMediaGrid> onViewMediaGridStateChangeListener = v -> {
            viewActionBar.setIsEnabled(v.getState() == ViewMediaGrid.State.StandbyMode);
        };

        Consumer<ButtonChoiceGridVariant> onGridVariantChange = btn -> {
            viewMediaGrid.setGridVariant(btn.getSelectedVariant());
        };

        Consumer<ButtonChoiceSortVariant> onSortVariantChange = btn -> {
            updateRequestSortVariant.accept(btn.getSelectedVariant());
            refreshFilesList.run();
        };

        viewMediaGrid.setStateChangeListener(onViewMediaGridStateChangeListener);
        buttonGridVariant.setVariantChangeListener(onGridVariantChange);
        buttonSortVariant.setVariantChangeListener(onSortVariantChange);
        buttonRefresh.setRefreshListener(refreshFilesList);

        refreshFilesList.run();

        return view;
    }

    private ModelGetFilesRequest getTestRequest() {
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

        List<ModelScanParams.StorageParams> storagesParams = new ArrayList<>();
//            storagesParams.add(new ModelScanParams.StorageParams(
//                    "[/storage/emulated/0]",
//                    ModelScanParams.ScanMode.ScanPathsInListOnly,
//                    new ReadOnlyList<>(new String[] {
//                            "/storage/emulated/0/nice_gallery_test",
//                    })
//            ));
//            storagesParams.add(new ModelScanParams.StorageParams(
//                    "[/storage/72AD-2013]",
//                    ModelScanParams.ScanMode.ScanPathsInListOnly,
//                    new ReadOnlyList<>(new String[] {
//                            "/storage/72AD-2013/nice_gallery_test",
//                    })
//            ));

        ModelScanParams scanParams = new ModelScanParams(
                new ReadOnlyList<>(storagesParams)
        );

        return new ModelGetFilesRequest(
                null,
                scanParams,
                filters,
                sortVariant,
                foldersFirst
        );
    }
}

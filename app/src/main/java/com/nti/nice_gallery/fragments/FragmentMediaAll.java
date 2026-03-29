package com.nti.nice_gallery.fragments;

import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.activities.ActivityMain;
import com.nti.nice_gallery.activities.ActivityMediaView;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfFiles;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelRequestProgress;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.utils.CallbackShell;
import com.nti.nice_gallery.utils.ManagerOfDialogs;
import com.nti.nice_gallery.utils.ManagerOfNavigation;
import com.nti.nice_gallery.utils.ManagerOfThreads;
import com.nti.nice_gallery.utils.ReadOnlyList;
import com.nti.nice_gallery.views.ViewActionBar;
import com.nti.nice_gallery.views.ViewMediaGrid;
import com.nti.nice_gallery.views.buttons.ButtonCancel;
import com.nti.nice_gallery.views.buttons.ButtonChoiceFilters;
import com.nti.nice_gallery.views.buttons.ButtonChoiceGridVariant;
import com.nti.nice_gallery.views.buttons.ButtonChoiceSortVariant;
import com.nti.nice_gallery.views.buttons.ButtonCopyFiles;
import com.nti.nice_gallery.views.buttons.ButtonDeleteFiles;
import com.nti.nice_gallery.views.buttons.ButtonRefresh;
import com.nti.nice_gallery.views.buttons.ButtonReplaceFiles;
import com.nti.nice_gallery.views.buttons.ButtonScanningReport;
import com.nti.nice_gallery.views.buttons.ButtonSelectAll;
import com.nti.nice_gallery.views.grid_items.GridItemBase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FragmentMediaAll extends Fragment {

    private static final String LOG_TAG = "FragmentMediaAll";

    private static boolean isSelectedMode = false;
    private static HashMap<String, ModelMediaFile> selectedFiles;
    private static boolean isBusy = false;
    private static String statusInfo = null;
    private static final CallbackShell.ForRunnable onActionFinishedShell = new CallbackShell.ForRunnable();
    private static final CallbackShell.ForConsumer<ModelRequestProgress> onActionProgressShell = new CallbackShell.ForConsumer<>();

    private ModelGetFilesRequest request;
    private ModelGetFilesResponse response;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_all, container, false);

        ActivityMain activityMain = (ActivityMain) getActivity();

        ViewMediaGrid viewMediaGrid = view.findViewById(R.id.viewMediaGrid);
        ViewActionBar viewActionBar = view.findViewById(R.id.viewActionBar);
        ButtonChoiceGridVariant buttonGridVariant = view.findViewById(R.id.buttonGridVariant);
        ButtonChoiceSortVariant buttonSortVariant = view.findViewById(R.id.buttonSortVariant);
        ButtonChoiceFilters buttonFilters = view.findViewById(R.id.buttonFilters);
        ButtonRefresh buttonRefresh = view.findViewById(R.id.buttonRefresh);
        ButtonScanningReport buttonScanningReport = view.findViewById(R.id.buttonScanningReport);
        ButtonSelectAll buttonSelectAll = view.findViewById(R.id.buttonSelectAll);
        ButtonReplaceFiles buttonReplaceFiles = view.findViewById(R.id.buttonReplaceFiles);
        ButtonCopyFiles buttonCopyFiles = view.findViewById(R.id.buttonCopyFiles);
        ButtonDeleteFiles buttonDeleteFiles = view.findViewById(R.id.buttonDeleteFiles);
        ButtonCancel buttonCancel = view.findViewById(R.id.buttonCancel);
        TextView textStatusInfo = view.findViewById(R.id.textStatusInfo);

        IManagerOfFiles managerOfFiles = Domain.getManagerOfFiles(getContext());
        ManagerOfThreads managerOfThreads = new ManagerOfThreads(getContext());
        ManagerOfDialogs managerOfDialogs = new ManagerOfDialogs(getContext());
        ManagerOfNavigation managerOfNavigation = new ManagerOfNavigation(getContext());
        IManagerOfSettings managerOfSettings = Domain.getManagerOfSettings(getContext());

        if (selectedFiles == null) {
            selectedFiles = new HashMap<>();
        }

        Supplier<ModelGetFilesRequest> buildRequest = () -> {
            ModelGetFilesRequest.SortVariant sortVariant = ModelGetFilesRequest.SortVariant.ByCreateAtDesc;
            boolean foldersFirst = true;

            if (request != null) {
                sortVariant = request.sortVariant;
                foldersFirst = request.foldersFirst;
            }

            ModelScanParams scanParams = managerOfSettings.getScanParams();
            ModelFilters filters = managerOfSettings.getFilters();

            return new ModelGetFilesRequest(
                    null,
                    scanParams,
                    filters,
                    sortVariant,
                    foldersFirst
            );
        };

        request = buildRequest.get();

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

        Runnable onStatusInfoChanged = () -> {
            textStatusInfo.setText(statusInfo);
        };

        Supplier<String> getStatusInfo = () -> {
            if (response == null) {
                return tryGetString(R.string.message_something_wrong);
            }
            if (response.files == null || response.files.isEmpty()) {
                return tryGetString(R.string.format_status_info_response_report, 0);
            }
            return tryGetString(R.string.format_status_info_response_report, response.files.size());
        };

        Runnable refreshFilesList = () -> {
            request = buildRequest.get();
            viewMediaGrid.trySetStateScanningInProgress(true);
            managerOfFiles.getFilesAsync(request, response -> {
                managerOfThreads.runOnUiThread(() -> {
                    FragmentMediaAll.this.response = response;
                    buttonScanningReport.setSource(response);
                    if (response.error == null) {
                        viewMediaGrid.setMediaFiles(response.files);
                        viewMediaGrid.trySetStateScanningInProgress(false);
                        buttonSelectAll.setAllFiles(response.files);
                    } else {
                        viewMediaGrid.trySetStateScanningInProgress(false);
                        managerOfDialogs.showInfo(R.string.dialog_title_something_wrong, R.string.message_error_scanning_failed);
                    }
                });
            });
        };

        Consumer<GridItemBase> onGridItemClick = item -> {
            ModelMediaFile file = item.getModel();

            if (file.isFile) {
                ActivityMediaView.Payload payload = new ActivityMediaView.Payload(
                        response,
                        file
                );

                managerOfNavigation.navigate(ActivityMediaView.class, payload);
            }
        };

        Runnable onSelectedModeChange = () -> {
            int commonButtonsVisibility = isSelectedMode ? View.GONE : View.VISIBLE;
            int selectedModeButtonsVisibility = isSelectedMode ? View.VISIBLE : View.GONE;

            buttonGridVariant.setVisibility(commonButtonsVisibility);
            buttonSortVariant.setVisibility(commonButtonsVisibility);
            buttonFilters.setVisibility(commonButtonsVisibility);
            buttonRefresh.setVisibility(commonButtonsVisibility);
            buttonScanningReport.setVisibility(commonButtonsVisibility);

            buttonSelectAll.setVisibility(selectedModeButtonsVisibility);
            buttonReplaceFiles.setVisibility(selectedModeButtonsVisibility);
            buttonCopyFiles.setVisibility(selectedModeButtonsVisibility);
            buttonDeleteFiles.setVisibility(selectedModeButtonsVisibility);
            buttonCancel.setVisibility(selectedModeButtonsVisibility);
        };

        Consumer<ViewMediaGrid> onGridSelectedModeChanged = v -> {
            isSelectedMode = v.getSelectedMode();
            selectedFiles = v.getSelectedFiles();
            onSelectedModeChange.run();
        };

        Runnable cancelSelectedMode = () -> {
            isSelectedMode = false;
            onSelectedModeChange.run();
            viewMediaGrid.setSelectedMode(isSelectedMode);
            buttonSelectAll.setChecked(false);
        };

        Runnable onSelectedFilesChanged = () -> {
            viewMediaGrid.setSelectedFiles(selectedFiles);
        };

        Consumer<ActivityMain> onBackButtonPressed = a -> {
            if (isSelectedMode) {
                cancelSelectedMode.run();
            }
        };

        Runnable onIsBusyChanged = () -> {
            if (isBusy) {
                viewActionBar.setIsEnabled(false);
                statusInfo = tryGetString(R.string.message_request_handling);
                onStatusInfoChanged.run();
            } else {
                viewActionBar.setIsEnabled(true);
                statusInfo = getStatusInfo.get();
                onStatusInfoChanged.run();
            }
        };

        Consumer<ViewMediaGrid> onViewMediaGridStateChangeListener = v -> {
            boolean currentIsBusy = isBusy;
            isBusy = v.getState() != ViewMediaGrid.CurrentWork.Standby;
            if (currentIsBusy != isBusy) {
                onIsBusyChanged.run();
            }
        };

        Consumer<ButtonChoiceGridVariant> onGridVariantChange = btn -> {
            viewMediaGrid.setGridVariant(btn.getSelectedVariant());
        };

        Consumer<ButtonChoiceSortVariant> onSortVariantChange = btn -> {
            updateRequestSortVariant.accept(btn.getSelectedVariant());
            refreshFilesList.run();
        };

        Runnable onActionFinished = () -> {
            managerOfThreads.runOnUiThread(() -> {
                cancelSelectedMode.run();
                refreshFilesList.run();
                isBusy = false;
                onIsBusyChanged.run();
            });
        };

        Consumer<ModelRequestProgress> onActionProgress = progress -> {
            managerOfThreads.runOnUiThread(() -> {
                statusInfo = tryGetString(R.string.format_status_info_action_progress, progress.currentStep, progress.numberCompletedSteps, progress.numberTotalSteps);
                onStatusInfoChanged.run();
                if (!isBusy) {
                    isBusy = true;
                    onIsBusyChanged.run();
                }
            });
        };

        activityMain.setBackButtonPressedListener(this, onBackButtonPressed);

        onActionProgressShell.setCallback(onActionProgress);
        onActionFinishedShell.setCallback(onActionFinished);

        viewMediaGrid.setStateChangeListener(onViewMediaGridStateChangeListener);
        viewMediaGrid.setItemClickListener(onGridItemClick);
        viewMediaGrid.setSelectedModeChangeListener(onGridSelectedModeChanged);

        buttonGridVariant.setVariantChangeListener(onGridVariantChange);
        buttonSortVariant.setVariantChangeListener(onSortVariantChange);
        buttonRefresh.setRefreshListener(refreshFilesList);

        buttonSelectAll.setSelectedFilesChangedListener(btn -> onSelectedFilesChanged.run());
        buttonReplaceFiles.setActionFinishedListener(btn -> onActionFinishedShell.invokeCallback());
        buttonReplaceFiles.setActionProgressListener(btn -> onActionProgressShell.invokeCallback(btn.getProgress()));
        buttonCopyFiles.setActionFinishedListener(btn -> onActionFinishedShell.invokeCallback());
        buttonCopyFiles.setActionProgressListener(btn -> onActionProgressShell.invokeCallback(btn.getProgress()));
        buttonDeleteFiles.setActionFinishedListener(btn -> onActionFinishedShell.invokeCallback());
        buttonDeleteFiles.setActionProgressListener(btn -> onActionProgressShell.invokeCallback(btn.getProgress()));
        buttonCancel.setOnClickListener(btn -> cancelSelectedMode.run());

        buttonSelectAll.setSelectedFiles(selectedFiles);
        buttonReplaceFiles.setFiles(selectedFiles);
        buttonCopyFiles.setFiles(selectedFiles);
        buttonDeleteFiles.setFiles(selectedFiles);

        viewMediaGrid.setSelectedFiles(selectedFiles);
        viewMediaGrid.setSelectedMode(isSelectedMode);

        refreshFilesList.run();

        return view;
    }

    private String tryGetString(@StringRes int resId, Object... formatArgs) {
        if (isAdded()) {
            return getString(resId, formatArgs);
        }
        return null;
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

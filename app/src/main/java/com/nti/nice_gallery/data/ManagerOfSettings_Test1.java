package com.nti.nice_gallery.data;

import android.content.Context;

import com.nti.nice_gallery.models.ModelFilesActionRequest;
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.utils.ReadOnlyList;
import com.nti.nice_gallery.views.ViewMediaGrid;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class ManagerOfSettings_Test1 implements IManagerOfSettings{

    private final Context context;

    private static ModelScanParams scanParams;
    private static ModelFilters filters;
    private static ViewMediaGrid.GridVariant gridVariant = ViewMediaGrid.GridVariant.List;
    private static ModelGetFilesRequest.SortVariant sortVariant = ModelGetFilesRequest.SortVariant.ByCreateAtDesc;
    private static ModelFilesActionRequest.FilesAction lastFilesAction = ModelFilesActionRequest.FilesAction.Copy;
    private static LinkedHashSet<String> pathsHistory = new LinkedHashSet<>();

    public ManagerOfSettings_Test1(Context context) {
        this.context = context;
    }

    @Override
    public ModelScanParams getScanParams() {
        return scanParams;
    }

    @Override
    public void saveScanParams(ModelScanParams scanParams) {
        this.scanParams = scanParams;
    }

    @Override
    public ModelFilters getFilters() {
        if (this.filters == null) {
            this.filters = new ModelFilters(
                    true,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return filters;
    }

    @Override
    public void saveFilters(ModelFilters filters) {
        this.filters = filters;
    }

    @Override
    public ViewMediaGrid.GridVariant getGridVariant() {
        return gridVariant;
    }

    @Override
    public void saveGridVariant(ViewMediaGrid.GridVariant variant) {
        this.gridVariant = variant;
    }

    @Override
    public ModelGetFilesRequest.SortVariant getSortVariant() {
        return sortVariant;
    }

    @Override
    public void saveSortVariant(ModelGetFilesRequest.SortVariant variant) {
        this.sortVariant = variant;
    }

    @Override
    public ModelFilesActionRequest.FilesAction getLastFilesAction() {
        return lastFilesAction;
    }

    @Override
    public void saveLastFilesAction(ModelFilesActionRequest.FilesAction action) {
        this.lastFilesAction = action;
    }

    @Override
    public ReadOnlyList<String> getPathsHistory() {
        return new ReadOnlyList<>(pathsHistory);
    }

    @Override
    public void savePathToHistory(String path) {
        pathsHistory.remove(path);
        pathsHistory.add(path);
    }
}

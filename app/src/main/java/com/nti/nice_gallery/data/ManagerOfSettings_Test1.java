package com.nti.nice_gallery.data;

import android.content.Context;

import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.views.ViewMediaGrid;

public class ManagerOfSettings_Test1 implements IManagerOfSettings{

    private final Context context;

    private static ModelScanParams scanParams;
    private static ModelFilters filters;
    private static ViewMediaGrid.GridVariant gridVariant = ViewMediaGrid.GridVariant.List;
    private static ModelGetFilesRequest.SortVariant sortVariant = ModelGetFilesRequest.SortVariant.ByCreateAtDesc;

    public ManagerOfSettings_Test1(Context context) {
        this.context = context;
    }

    @Override
    public ModelScanParams getScanParams() {
        return scanParams;
    }

    @Override
    public void saveScanParams(ModelScanParams scanList) {
        this.scanParams = scanList;
    }

    @Override
    public ModelFilters getFilters() {
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
}

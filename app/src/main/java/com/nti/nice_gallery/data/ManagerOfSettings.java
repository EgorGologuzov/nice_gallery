package com.nti.nice_gallery.data;

import android.content.Context;

import com.nti.nice_gallery.models.ModelFilesActionRequest;
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.views.ViewMediaGrid;

public class ManagerOfSettings implements IManagerOfSettings {

    private final Context context;

    public ManagerOfSettings(Context context) {
        this.context = context;
    }

    @Override
    public ModelScanParams getScanParams() {
        return null;
    }

    @Override
    public void saveScanParams(ModelScanParams scanList) {

    }

    @Override
    public ModelFilters getFilters() {
        return null;
    }

    @Override
    public void saveFilters(ModelFilters filters) {

    }

    @Override
    public ViewMediaGrid.GridVariant getGridVariant() {
        return null;
    }

    @Override
    public void saveGridVariant(ViewMediaGrid.GridVariant variant) {

    }

    @Override
    public ModelGetFilesRequest.SortVariant getSortVariant() {
        return null;
    }

    @Override
    public void saveSortVariant(ModelGetFilesRequest.SortVariant variant) {

    }

    @Override
    public ModelFilesActionRequest.FilesAction getLastFilesAction() {
        return null;
    }

    @Override
    public void saveLastFilesAction(ModelFilesActionRequest.FilesAction action) {

    }
}

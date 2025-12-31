package com.nti.nice_gallery.data;

import android.content.Context;

import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.views.ViewMediaGrid;

public class ManagerOfSettings_Test1 implements IManagerOfSettings{

    private final Context context;

    public ManagerOfSettings_Test1(Context context) {
        this.context = context;
    }

    private ModelScanParams scanParams;
    private ModelFilters filters;

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
        return ViewMediaGrid.GridVariant.List;
    }

    @Override
    public void saveGridVariant(ViewMediaGrid.GridVariant variant) {

    }
}

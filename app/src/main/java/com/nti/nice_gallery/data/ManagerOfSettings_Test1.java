package com.nti.nice_gallery.data;

import android.content.Context;

import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelScanList;
import com.nti.nice_gallery.views.ViewMediaGrid;

import java.util.List;

public class ManagerOfSettings_Test1 implements IManagerOfSettings{

    private final Context context;

    public ManagerOfSettings_Test1(Context context) {
        this.context = context;
    }

    private List<ModelScanList>  scanList;
    private ModelFilters filters;

    @Override
    public List<ModelScanList> getScanList() {
        return scanList;
    }

    @Override
    public void saveScanList(List<ModelScanList> scanList) {
        this.scanList = scanList;
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

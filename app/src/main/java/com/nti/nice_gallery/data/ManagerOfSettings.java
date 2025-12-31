package com.nti.nice_gallery.data;

import android.content.Context;

import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.views.ViewMediaGrid;

import java.util.Collections;
import java.util.List;

public class ManagerOfSettings implements IManagerOfSettings {

    private final Context context;

    public ManagerOfSettings(Context context) {
        this.context = context;
    }

    @Override
    public ModelScanParams getScanParams() {
        return new ModelScanParams(null);
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
}

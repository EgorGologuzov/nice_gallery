package com.nti.nice_gallery.data;

import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelScanList;
import com.nti.nice_gallery.views.ViewMediaGrid;

import java.util.List;

public interface IManagerOfSettings {

    List<ModelScanList> getScanList();
    void saveScanList(List<ModelScanList> scanList);

    ModelFilters getFilters();
    void saveFilters(ModelFilters filters);

    ViewMediaGrid.GridVariant getGridVariant();
    void saveGridVariant(ViewMediaGrid.GridVariant variant);
}

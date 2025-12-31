package com.nti.nice_gallery.data;

import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.views.ViewMediaGrid;

public interface IManagerOfSettings {

    ModelScanParams getScanParams();
    void saveScanParams(ModelScanParams scanList);

    ModelFilters getFilters();
    void saveFilters(ModelFilters filters);

    ViewMediaGrid.GridVariant getGridVariant();
    void saveGridVariant(ViewMediaGrid.GridVariant variant);
}

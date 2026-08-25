package com.nti.nice_gallery.data;

import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.utils.ReadOnlyList;
import com.nti.nice_gallery.views.ViewMediaGrid;

import java.time.LocalDateTime;

public interface IManagerOfSettings {

    ModelScanParams getScanParams();
    void saveScanParams(ModelScanParams scanList);

    ModelFilters getFilters();
    void saveFilters(ModelFilters filters);

    ViewMediaGrid.GridVariant getGridVariant();
    void saveGridVariant(ViewMediaGrid.GridVariant variant);

    ModelGetFilesRequest.SortVariant getSortVariant();
    void saveSortVariant(ModelGetFilesRequest.SortVariant variant);

    ReadOnlyList<String> getPathsHistory();
    void savePathToHistory(String path);
}

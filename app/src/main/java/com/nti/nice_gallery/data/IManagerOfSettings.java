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

    String CACHE_FILES_INFO_TXT = "cache/files_info.txt";

    TxtFile readTxt(String filePath);
    TxtFile saveTxt(String filePath, String[] content);

    class TxtFile {
        public final String filePath;
        public final LocalDateTime updatedAt;
        public final String[] strings;

        public TxtFile(
                String filePath,
                LocalDateTime updatedAt,
                String[] strings
        ) {
            this.filePath = filePath;
            this.updatedAt = updatedAt;
            this.strings = strings;
        }
    }
}

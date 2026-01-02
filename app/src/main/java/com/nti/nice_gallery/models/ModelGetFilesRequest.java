package com.nti.nice_gallery.models;

import androidx.annotation.Nullable;

public class ModelGetFilesRequest {

    public enum SortVariant {
        ByName,
        ByNameDesc,
        ByCreateAt,
        ByCreateAtDesc,
        ByUpdateAt,
        ByUpdateAtDesc,
        ByWeight,
        ByWeightDesc
    }

    @Nullable public final ModelScanParams scanParams;
    @Nullable public final ModelFilters filters;
    @Nullable public final SortVariant sortVariant;
    public final boolean foldersFirst;

    public ModelGetFilesRequest(
            @Nullable ModelScanParams scanParams,
            @Nullable ModelFilters filters,
            @Nullable SortVariant sortVariant,
            @Nullable Boolean foldersFirst
    ) {
        this.scanParams = scanParams;
        this.filters = filters;
        this.sortVariant = sortVariant;
        this.foldersFirst = foldersFirst == null ? true : foldersFirst;
    }
}

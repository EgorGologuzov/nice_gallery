package com.nti.nice_gallery.models;

import androidx.annotation.Nullable;

public class ModelGetFilesRequest {

    public enum SortVariant {
        ByName,
        ByNameDesc,
        ByCreateDate,
        ByCreateDateDesc,
        ByUpdateDate,
        ByUpdateDateDesc,
        ByWeight,
        ByWeightDesc
    }

    public enum SortByTypeVariant {
        FoldersFirst,
        FilesFirst
    }

    @Nullable public final ModelFilters filters;
    @Nullable public final SortVariant sortVariant;
    @Nullable public final SortByTypeVariant sortByTypeVariant;

    public ModelGetFilesRequest(
            @Nullable ModelFilters filters,
            @Nullable SortVariant sortVariant,
            @Nullable SortByTypeVariant sortByTypeVariant
    ) {
        this.filters = filters;
        this.sortVariant = sortVariant;
        this.sortByTypeVariant = sortByTypeVariant;
    }
}

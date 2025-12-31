package com.nti.nice_gallery.models;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.utils.ReadOnlyList;

import java.time.LocalDateTime;

public class ModelFilters {

    public final boolean ignoreHidden;
    @Nullable public final ReadOnlyList<ModelMediaFile.Type> types;
    @Nullable public final Long minWeight;
    @Nullable public final Long maxWeight;
    @Nullable public final LocalDateTime minCreateAt;
    @Nullable public final LocalDateTime maxCreateAt;
    @Nullable public final LocalDateTime minUpdateAt;
    @Nullable public final LocalDateTime maxUpdateAt;
    @Nullable public final ReadOnlyList<String> extensions;
    @Nullable public final Integer minDuration;
    @Nullable public final Integer maxDuration;

    public ModelFilters(
            @Nullable Boolean ignoreHidden,
            @Nullable ReadOnlyList<ModelMediaFile.Type> types,
            @Nullable Long minWeight,
            @Nullable Long maxWeight,
            @Nullable LocalDateTime minCreateAt,
            @Nullable LocalDateTime maxCreateAt,
            @Nullable LocalDateTime minUpdateAt,
            @Nullable LocalDateTime maxUpdateAt,
            @Nullable ReadOnlyList<String> extensions,
            @Nullable Integer minDuration,
            @Nullable Integer maxDuration
    ) {
        this.ignoreHidden = ignoreHidden == null ? true : ignoreHidden;
        this.types = types;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.minCreateAt = minCreateAt;
        this.maxCreateAt = maxCreateAt;
        this.minUpdateAt = minUpdateAt;
        this.maxUpdateAt = maxUpdateAt;
        this.extensions = extensions;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }
}

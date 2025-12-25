package com.nti.nice_gallery.models;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class ModelFilters {
    @Nullable public List<ModelMediaTreeItem.Type> types;
    @Nullable public Long minWeight;
    @Nullable public Long maxWeight;
    @Nullable public LocalDate minCreateDate;
    @Nullable public LocalDate maxCreateDate;
    @Nullable public List<String> extensions;
    @Nullable public Integer duration;
}

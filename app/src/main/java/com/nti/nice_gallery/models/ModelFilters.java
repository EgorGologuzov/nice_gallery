package com.nti.nice_gallery.models;

import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;

public class ModelFilters {
    @Nullable public List<ModelMediaFile.Type> types;
    @Nullable public Long minWeight;
    @Nullable public Long maxWeight;
    @Nullable public Date minCreateDate;
    @Nullable public Date maxCreateDate;
    @Nullable public List<String> extensions;
    @Nullable public Integer duration;
}

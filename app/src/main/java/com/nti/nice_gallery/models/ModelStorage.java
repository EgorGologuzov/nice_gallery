package com.nti.nice_gallery.models;

import androidx.annotation.Nullable;

public class ModelStorage {

    public enum Type { Primary, Removable, Else }

    public final String name;
    public final String path;
    public final Type type;
    public final String description;

    public final Long freeSpace;
    public final Long totalSpace;

    @Nullable public final Exception error;

    public ModelStorage(
            String name,
            String path,
            Type type,
            String description,
            Long freeSpace,
            Long totalSpace,
            @Nullable Exception error
    ) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.description = description;
        this.freeSpace = freeSpace;
        this.totalSpace = totalSpace;
        this.error = error;
    }
}

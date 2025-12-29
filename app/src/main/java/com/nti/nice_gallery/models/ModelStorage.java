package com.nti.nice_gallery.models;

import androidx.annotation.Nullable;

public class ModelStorage {

    public enum Type { Primary, Removable, Else}

    public final String name;
    public final String path;
    public final Type type;

    @Nullable public final Exception error;

    public ModelStorage(
            String name,
            String path,
            Type type,
            @Nullable Exception error
    ) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.error = error;
    }
}

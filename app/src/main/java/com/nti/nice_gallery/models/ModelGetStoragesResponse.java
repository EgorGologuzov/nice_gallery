package com.nti.nice_gallery.models;

import com.nti.nice_gallery.utils.ReadOnlyList;

public class ModelGetStoragesResponse {

    public final ReadOnlyList<ModelStorage> storages;

    public ModelGetStoragesResponse(
            ReadOnlyList<ModelStorage> storages
    ) {
        this.storages = storages;
    }
}

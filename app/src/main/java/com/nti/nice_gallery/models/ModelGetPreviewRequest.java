package com.nti.nice_gallery.models;

import org.jetbrains.annotations.NotNull;

public class ModelGetPreviewRequest {

    @NotNull public final ModelMediaFile file;

    public ModelGetPreviewRequest(
            @NotNull ModelMediaFile file
    ) {
        this.file = file;
    }
}

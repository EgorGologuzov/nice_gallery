package com.nti.nice_gallery.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModelGetPreviewRequest {

    @NotNull public final ModelMediaFile file;
    @Nullable public final Integer targetWidth;
    @Nullable public final Integer targetHeight;

    public ModelGetPreviewRequest(
            @NotNull ModelMediaFile file,
            @Nullable Integer targetWidth,
            @Nullable Integer targetHeight
    ) {
        this.file = file;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }
}

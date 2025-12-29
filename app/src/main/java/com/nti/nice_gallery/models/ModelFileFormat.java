package com.nti.nice_gallery.models;

import org.jetbrains.annotations.NotNull;

public class ModelFileFormat {

    @NotNull public final String mimeType;
    @NotNull public final String fileExtension;
    @NotNull public final ModelMediaFile.Type type;

    public ModelFileFormat(
            @NotNull String mimeType,
            @NotNull String fileExtension,
            @NotNull ModelMediaFile.Type type
    ) {
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.type = type;
    }
}

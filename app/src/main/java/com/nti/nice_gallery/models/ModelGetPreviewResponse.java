package com.nti.nice_gallery.models;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

public class ModelGetPreviewResponse {

    @Nullable public final Bitmap preview;

    public ModelGetPreviewResponse(
            @Nullable Bitmap preview
    ) {
        this.preview = preview;
    }
}

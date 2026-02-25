package com.nti.nice_gallery.models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

public class ModelGetPreviewResponse {

    @Nullable public final Bitmap previewBitmap;
    @Nullable public final Drawable previewDrawable;

    public ModelGetPreviewResponse(
            @Nullable Bitmap previewBitmap,
            @Nullable Drawable previewDrawable
    ) {
        this.previewBitmap = previewBitmap;
        this.previewDrawable = previewDrawable;
    }
}

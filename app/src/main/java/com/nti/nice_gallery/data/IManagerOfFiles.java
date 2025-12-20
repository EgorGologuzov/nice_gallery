package com.nti.nice_gallery.data;

import android.graphics.Bitmap;

import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelStorage;

import java.util.List;

public interface IManagerOfFiles {
    List<ModelMediaFile> getAllFiles();
    Bitmap getFilePreview(ModelMediaFile item);
    List<ModelStorage> getAllStorages();
}

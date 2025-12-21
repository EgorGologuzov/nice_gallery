package com.nti.nice_gallery.data;

import android.graphics.Bitmap;

import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelStorage;

import java.util.List;

public interface IManagerOfFiles {
    ModelGetFilesResponse getFiles(ModelGetFilesRequest request);
    Bitmap getFilePreview(ModelMediaFile item);
    List<ModelStorage> getAllStorages();
}

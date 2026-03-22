package com.nti.nice_gallery.data;

import com.nti.nice_gallery.models.ModelFilesActionRequest;
import com.nti.nice_gallery.models.ModelFilesActionResponse;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelGetPreviewRequest;
import com.nti.nice_gallery.models.ModelGetPreviewResponse;
import com.nti.nice_gallery.models.ModelGetStoragesRequest;
import com.nti.nice_gallery.models.ModelGetStoragesResponse;
import com.nti.nice_gallery.models.ModelRequestProgress;

import java.util.function.Consumer;

public interface IManagerOfFiles {
    void getStoragesAsync(ModelGetStoragesRequest request, Consumer<ModelGetStoragesResponse> callback);
    void getFilesAsync(ModelGetFilesRequest request, Consumer<ModelGetFilesResponse> callback);
    void getPreviewAsync(ModelGetPreviewRequest request, Consumer<ModelGetPreviewResponse> callback);
    void executeAction(ModelFilesActionRequest request, Consumer<ModelFilesActionResponse> callbackResult, Consumer<ModelRequestProgress> callbackProgress);
}

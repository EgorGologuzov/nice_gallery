package com.nti.nice_gallery.models;

import java.time.LocalDateTime;

public class ModelGetFilesResponse {

    public final LocalDateTime scanningStartedAt;
    public final LocalDateTime scanningFinishedAt;
    public final ReadOnlyList<ModelMediaFile> files;
    public final ReadOnlyList<ModelStorage> scannedStorages;
    public final ReadOnlyList<ModelMediaFile> filesWithErrors;
    public final ReadOnlyList<ModelStorage> storagesWithErrors;

    public ModelGetFilesResponse(
            LocalDateTime scanningStartedAt,
            LocalDateTime scanningFinishedAt,
            ReadOnlyList<ModelMediaFile> files,
            ReadOnlyList<ModelStorage> scannedStorages,
            ReadOnlyList<ModelMediaFile> filesWithErrors,
            ReadOnlyList<ModelStorage> storagesWithErrors
    ) {
        this.scanningStartedAt = scanningStartedAt;
        this.scanningFinishedAt = scanningFinishedAt;
        this.files = files;
        this.scannedStorages = scannedStorages;
        this.filesWithErrors = filesWithErrors;
        this.storagesWithErrors = storagesWithErrors;
    }
}

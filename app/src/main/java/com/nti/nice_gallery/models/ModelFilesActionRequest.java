package com.nti.nice_gallery.models;

import com.nti.nice_gallery.utils.ReadOnlyList;

public class ModelFilesActionRequest {

    public enum FilesAction { Replace, Copy, Delete, CreateFolder }
    public enum DuplicateNamePolicy { Skip, Replace, Rename }

    public final FilesAction action;
    public final ReadOnlyList<ModelMediaFile> files;
    public final String targetPath;
    public final DuplicateNamePolicy duplicateNamePolicy;

    public ModelFilesActionRequest(
            FilesAction action,
            ReadOnlyList<ModelMediaFile> files,
            String targetPath,
            DuplicateNamePolicy duplicateNamePolicy
    ) {
        this.action = action;
        this.files = files;
        this.targetPath = targetPath;
        this.duplicateNamePolicy = duplicateNamePolicy;
    }
}

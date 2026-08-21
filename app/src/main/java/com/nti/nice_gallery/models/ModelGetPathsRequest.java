package com.nti.nice_gallery.models;

public class ModelGetPathsRequest {
    public final String parentPath;
    public final boolean includeDirs;
    public final boolean includeFiles;

    public ModelGetPathsRequest(
            String parentPath,
            boolean includeDirs,
            boolean includeFiles
    ) {
        this.parentPath = parentPath;
        this.includeDirs = includeDirs;
        this.includeFiles = includeFiles;
    }
}

package com.nti.nice_gallery.models;

import com.nti.nice_gallery.utils.ReadOnlyList;

import java.io.File;

import kotlinx.coroutines.android.AndroidExceptionPreHandler;

public class ModelFilesActionResponse {

    public final Exception globalError;
    public final ReadOnlyList<FileInfo> successHandledFiles;
    public final ReadOnlyList<FileInfo> skippedFiles;
    public final ReadOnlyList<FileInfo> renamedFiles;
    public final ReadOnlyList<FileInfo> replacedFiles;
    public final ReadOnlyList<Fail> fails;

    public ModelFilesActionResponse(
            Exception globalError,
            ReadOnlyList<FileInfo> successHandledFiles,
            ReadOnlyList<FileInfo> skippedFiles,
            ReadOnlyList<FileInfo> renamedFiles,
            ReadOnlyList<FileInfo> replacedFiles,
            ReadOnlyList<Fail> fails
    ) {
        this.globalError = globalError;
        this.successHandledFiles = successHandledFiles;
        this.skippedFiles = skippedFiles;
        this.renamedFiles = renamedFiles;
        this.replacedFiles = replacedFiles;
        this.fails = fails;
    }

    public static class Fail {
        public final FileInfo file;
        public final Exception error;
        public Fail(FileInfo file, Exception error) {
            this.file = file;
            this.error = error;
        }
    }

    public static class FileInfo {
        public final String name;
        public final String path;
        public final Boolean isFolder;

        public FileInfo(String name, String path, Boolean isFolder) {
            this.name = name;
            this.path = path;
            this.isFolder = isFolder;
        }
    }
}

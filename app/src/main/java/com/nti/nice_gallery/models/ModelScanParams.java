package com.nti.nice_gallery.models;

import com.nti.nice_gallery.utils.ReadOnlyList;

public class ModelScanParams {

    public enum ScanMode { ScanAll, ScanPathsInListOnly, ScanPathsNotInListOnly, IgnoreStorage }

    public final ReadOnlyList<StorageParams> storagesParams;

    public ModelScanParams(
            ReadOnlyList<StorageParams> storagesParams
    ) {
        this.storagesParams = storagesParams;
    }

    public static class StorageParams {

        public final String storageName;
        public final ScanMode scanMode;
        public final ReadOnlyList<String> paths;

        public StorageParams(
                String storageName,
                ScanMode scanMode,
                ReadOnlyList<String> paths
        ) {
            this.storageName = storageName;
            this.scanMode = scanMode;
            this.paths = paths;
        }
    }
}

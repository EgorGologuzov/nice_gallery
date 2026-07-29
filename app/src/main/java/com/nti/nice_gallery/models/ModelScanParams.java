package com.nti.nice_gallery.models;

import android.nfc.FormatException;

import com.nti.nice_gallery.utils.JsonUtil;
import com.nti.nice_gallery.utils.ReadOnlyList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModelScanParams {

    private static final ReadOnlyList<ScanMode> scanModeVariants = new ReadOnlyList<>(Arrays.stream(ModelScanParams.ScanMode.values()).collect(Collectors.toList()));

    public enum ScanMode { ScanAll, ScanPathsInListOnly, ScanPathsNotInListOnly, IgnoreStorage }
    public final ReadOnlyList<StorageParams> storagesParams;

    public ModelScanParams(
            ReadOnlyList<StorageParams> storagesParams
    ) {
        this.storagesParams = storagesParams;
    }

    public ModelScanParams(
            String jsonStr
    ) {
        JSONObject modelJson = JsonUtil.newJsonObject(jsonStr);
        this.storagesParams = new ReadOnlyList<>(JsonUtil.getArray(modelJson, "storagesParams", json -> new StorageParams(
                JsonUtil.getString(json, "storageName", null),
                JsonUtil.getEnum(json, "scanMode", ScanMode.class, ScanMode.ScanPathsInListOnly),
                new ReadOnlyList<>(JsonUtil.getArrayOfPrimitives(json, "paths", null))
        ), new ArrayList<>()));
    }

    public String toJson() {
        JSONObject modelJson = JsonUtil.newJsonObject();

        JsonUtil.addArray(modelJson, "storagesParams", this.storagesParams, (params, json) -> {
            JsonUtil.addString(json, "storageName", params.storageName);
            JsonUtil.addEnum(json, "scanMode", params.scanMode);
            JsonUtil.addArrayOfPrimitives(json, "paths", params.paths);
        });

        return modelJson.toString();
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

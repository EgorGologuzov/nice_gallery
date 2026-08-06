package com.nti.nice_gallery.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.utils.JsonUtil;
import com.nti.nice_gallery.utils.ReadOnlyList;
import com.nti.nice_gallery.views.ViewMediaGrid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class ManagerOfSettings implements IManagerOfSettings {

    private static final String LOG_TAG = "ManagerOfSettings";
    private static final String APP_PREFERENCES = "app_preferences";

    private final Context context;
    private final SharedPreferences preferences;

    private static ModelScanParams scanParamsDefault = null;

    private static ModelFilters filtersDefault = new ModelFilters(
            true,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
    );

    private static ViewMediaGrid.GridVariant gridVariantDefault = ViewMediaGrid.GridVariant.List;
    private static ModelGetFilesRequest.SortVariant sortVariantDefault = ModelGetFilesRequest.SortVariant.ByCreateAtDesc;

    private static LinkedHashSet<String> pathsHistory = null;

    public ManagerOfSettings(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    private static final String SCAN_PARAMS_KEY = "scanParams";

    @Override
    public ModelScanParams getScanParams() {
        String jsonStr = preferences.getString(SCAN_PARAMS_KEY, null);
        Log.i(LOG_TAG + 1, jsonStr != null ? jsonStr : "null");
        return jsonStr == null ? scanParamsDefault : new ModelScanParams(jsonStr);
    }

    @Override
    public void saveScanParams(ModelScanParams scanList) {
        String jsonStr = scanList.toJson();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SCAN_PARAMS_KEY, jsonStr);
        editor.apply();
        Log.i(LOG_TAG + 2, jsonStr);
    }

    private static final String FILTERS_KEY = "filters";

    @Override
    public ModelFilters getFilters() {
        String jsonStr = preferences.getString(FILTERS_KEY, null);
        Log.i(LOG_TAG + 3, jsonStr != null ? jsonStr : "null");
        return jsonStr == null ? filtersDefault : new ModelFilters(jsonStr);
    }

    @Override
    public void saveFilters(ModelFilters filters) {
        String jsonStr = filters != null ? filters.toJson() : null;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FILTERS_KEY, jsonStr);
        editor.apply();
        Log.i(LOG_TAG + 4, jsonStr != null ? jsonStr : "null");
    }

    private static final String GRID_VARIANT_KEY = "gridVariant";

    @Override
    public ViewMediaGrid.GridVariant getGridVariant() {
        String valueStr = preferences.getString(GRID_VARIANT_KEY, null);
        return valueStr == null ? gridVariantDefault : Enum.valueOf(ViewMediaGrid.GridVariant.class, valueStr);
    }

    @Override
    public void saveGridVariant(ViewMediaGrid.GridVariant variant) {
        String valueStr = variant.name();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(GRID_VARIANT_KEY, valueStr);
        editor.apply();
    }

    private static final String SORT_VARIANT_KEY = "sortVariant";

    @Override
    public ModelGetFilesRequest.SortVariant getSortVariant() {
        String valueStr = preferences.getString(SORT_VARIANT_KEY, null);
        return valueStr == null ? sortVariantDefault : Enum.valueOf(ModelGetFilesRequest.SortVariant.class, valueStr);
    }

    @Override
    public void saveSortVariant(ModelGetFilesRequest.SortVariant variant) {
        String valueStr = variant.name();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SORT_VARIANT_KEY, valueStr);
        editor.apply();
    }

    private static final String PATHS_HISTORY_KEY = "pathsHistory";

    @Override
    public ReadOnlyList<String> getPathsHistory() {
        if (pathsHistory == null) {
            String jsonStr = preferences.getString(PATHS_HISTORY_KEY, null);
            if (jsonStr != null) pathsHistory = new LinkedHashSet<>(JsonUtil.parseArrayOfPrimitives(jsonStr));
            else pathsHistory = new LinkedHashSet<>();
        }
        return new ReadOnlyList<>(pathsHistory);
    }

    @Override
    public void savePathToHistory(String path) {
        pathsHistory.remove(path);
        pathsHistory.add(path);
        String jsonStr = JsonUtil.stringifyArrayOfPrimitives(pathsHistory);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PATHS_HISTORY_KEY, jsonStr);
        editor.apply();
    }

    @Override
    public TxtFile readTxt(String filePath) {
        Object[] dirAndName = parseDirAndFileNameFromFilePath(filePath);
        File file = new File((File) dirAndName[0], (String) dirAndName[1]);

        if (!file.exists()) return null;

        String[] content;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int linesCount = Integer.parseInt(reader.readLine());
            content = new String[linesCount];

            for (int i = 0; i < linesCount; i++) {
                content[i] = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LocalDateTime updatedAt = LocalDateTime.ofEpochSecond(
                file.lastModified() / 1000,
                0,
                java.time.ZoneOffset.UTC
        );

        return new TxtFile(filePath, updatedAt, content);
    }

    @Override
    public TxtFile saveTxt(String filePath, String[] content) {
        Object[] dirAndName = parseDirAndFileNameFromFilePath(filePath);
        File file = new File((File) dirAndName[0], (String) dirAndName[1]);

        content = content != null ? content : new String[0];

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(content.length));
            writer.newLine();

            for (String line : content) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new TxtFile(
                filePath,
                LocalDateTime.now(),
                content
        );
    }

    private Object[] parseDirAndFileNameFromFilePath(String filePath) {
        String[] dirAndName = filePath.split("/");
        String dir = dirAndName[0];
        String fileName = dirAndName[1];

        File dirAsFile = Objects.equals(dir, "data") ? context.getDataDir() :
                Objects.equals(dir, "cache") ? context.getCacheDir() :
                context.getDataDir();

        return new Object[] { dirAsFile, fileName };
    }
}

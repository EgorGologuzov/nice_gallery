package com.nti.nice_gallery.data;

import android.content.Context;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.JsonUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ManagerOfDatabase {
    private static final String CACHE_FILES_INFO_TXT = "cache/files_info.txt";
    private static final String FOLDERS_ACTUALIZATION_INFO_TXT = "cache/folders_actualization_info.txt";

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, FileData>> filesRepos = new ConcurrentHashMap<>();

    private static final Statistic statistic = new Statistic();

    private final Context context;

    public ManagerOfDatabase(Context context) {
        this.context = context;
    }

    public FileData getOrCreateFile(String path) {
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        int lastSlashIdx = path.lastIndexOf("/");
        String parentPath = path.substring(0, lastSlashIdx);
        String name = path.substring(lastSlashIdx + 1);

        ConcurrentHashMap<String, FileData> parentFiles = filesRepos.getOrDefault(parentPath, null);
        if (parentFiles == null) {
            parentFiles = new ConcurrentHashMap<>();
            filesRepos.put(parentPath, parentFiles);
        }

        FileData data = parentFiles.getOrDefault(name, null);
        if (data == null) {
            data = new FileData(parentPath, name);
            parentFiles.put(name, data);
        }

        return data;
    }

    public void forEachFile(Function<FileData, Boolean> handler) {
        Boolean stop = false;
        for (Map.Entry<String, ConcurrentHashMap<String, FileData>> outerEntry : filesRepos.entrySet()) {
            ConcurrentHashMap<String, FileData> innerMap = outerEntry.getValue();
            for (Map.Entry<String, FileData> innerEntry : innerMap.entrySet()) {
                FileData fileData = innerEntry.getValue();
                stop = handler.apply(fileData);
                if (stop) break;
            }
            if (stop) break;
        }
    }

    public void forEachFileInFolder(String parentPath, Function<FileData, Boolean> handler) {
        if (parentPath.endsWith("/")) parentPath = parentPath.substring(0, parentPath.length() - 1);

        ConcurrentHashMap<String, FileData> parentFiles = filesRepos.getOrDefault(parentPath, null);
        if (parentFiles == null) {
            return;
        }

        for (Map.Entry<String, FileData> innerEntry : parentFiles.entrySet()) {
            FileData fileData = innerEntry.getValue();
            if (handler.apply(fileData)) break;
        }
    }

    public void actualizeFiles(File parent, File[] children) {
        String parentPath = parent.getAbsolutePath();
        boolean hasFolderChildren = false;
        FileData parentFileData = getOrCreateFile(parentPath);
        LocalDateTime parentLastUpdate = Instant.ofEpochMilli(parent.lastModified())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        ConcurrentHashMap<String, FileData> parentMap = filesRepos.getOrDefault(parentPath, null);
        if (parentMap == null) return;

        HashSet<String> childrenSet = new HashSet<>();
        for (File file : children) {
            childrenSet.add(file.getName());
            if (file.isDirectory()) hasFolderChildren = true;
        }

        parentFileData.setActualizationInfo(new ActualizationInfo(parentPath, parentLastUpdate, hasFolderChildren));

        for (String name : parentMap.keySet()) {
            if (!childrenSet.contains(name)) {
                parentMap.remove(name);
                String path = parentPath + "/" + name;
                removeDirRecursive(path, filesRepos.getOrDefault(path, null));
            }
        }
    }

    public Statistic getStatistic(boolean refreshNow) {
        if (refreshNow) refreshStatistic();
        return statistic;
    }

    public void storeFilesData() {
        storeFilesInfoCache();
        storeFoldersActualizationInfo();
    }

    public void restoreFilesData() {
        restoreFilesInfoCache();
        restoreFoldersActualizationInfo();
    }

    public void clearFilesInfoCache() {
        forEachFile(file -> {
            file.setFileInfoCache(null);
            return false;
        });

        deleteTxt(ManagerOfDatabase.CACHE_FILES_INFO_TXT);
        statistic.setSavedCachedFilesCount(0);
    }

    public void clearActualizationInfo() {
        forEachFile(file -> {
            file.setActualizationInfo(null);
            return false;
        });

        deleteTxt(ManagerOfDatabase.FOLDERS_ACTUALIZATION_INFO_TXT);
        statistic.setSavedActualizationInfoCount(0);
    }

    private void storeFilesInfoCache() {
        List<FileData> cachedFiles = getCachedFiles();
        String[] filesInfoStr = new String[cachedFiles.size()];

        for (int i = 0; i < cachedFiles.size(); i++) {
            FileData data = cachedFiles.get(i);
            ModelMediaFile cache = data.getFileInfoCache();
            filesInfoStr[i] = cache != null ? cache.toJson() : "null";
        }

        TxtFile cacheTxt = saveTxt(ManagerOfDatabase.CACHE_FILES_INFO_TXT, filesInfoStr);
        statistic.setSavedCachedFilesCount(cacheTxt.strings.length);
    }

    private void restoreFilesInfoCache() {
        TxtFile cacheTxt = readTxt(ManagerOfDatabase.CACHE_FILES_INFO_TXT);

        if (cacheTxt != null && cacheTxt.strings != null && cacheTxt.strings.length > 0) {
            statistic.setSavedCachedFilesCount(cacheTxt.strings.length);

            for (int i = 0; i < cacheTxt.strings.length; i++) {
                String json = cacheTxt.strings[i];
                if (!Objects.equals(json, "null")) {
                    ModelMediaFile fileInfo = new ModelMediaFile(json);
                    FileData data = getOrCreateFile(fileInfo.path);
                    data.setFileInfoCache(fileInfo);
                }
            }
        } else {
            statistic.setSavedCachedFilesCount(0);
        }
    }

    private void storeFoldersActualizationInfo() {
        List<FileData> cachedFiles = getFoldersWithActualizationInfo();
        String[] infoStr = new String[cachedFiles.size()];

        for (int i = 0; i < cachedFiles.size(); i++) {
            FileData data = cachedFiles.get(i);
            ActualizationInfo cache = data.getActualizationInfo();
            infoStr[i] = cache != null ? cache.toJson() : "null";
        }

        TxtFile cacheTxt = saveTxt(ManagerOfDatabase.FOLDERS_ACTUALIZATION_INFO_TXT, infoStr);
        statistic.setSavedActualizationInfoCount(cacheTxt.strings.length);
    }

    private void restoreFoldersActualizationInfo() {
        TxtFile cacheTxt = readTxt(ManagerOfDatabase.FOLDERS_ACTUALIZATION_INFO_TXT);

        if (cacheTxt != null && cacheTxt.strings != null && cacheTxt.strings.length > 0) {
            statistic.setSavedActualizationInfoCount(cacheTxt.strings.length);

            for (int i = 0; i < cacheTxt.strings.length; i++) {
                String json = cacheTxt.strings[i];
                if (!Objects.equals(json, "null")) {
                    ActualizationInfo actualizationInfo = new ActualizationInfo(json);
                    FileData data = getOrCreateFile(actualizationInfo.path);
                    data.setActualizationInfo(actualizationInfo);
                }
            }
        } else {
            statistic.setSavedActualizationInfoCount(0);
        }
    }

    private void refreshStatistic() {
        int filesCount = 0;
        int cachedFilesCount = 0;
        int actualizationInfoCount = 0;

        for (ConcurrentHashMap<String, FileData> innerMap : filesRepos.values()) {
            for (FileData data : innerMap.values()) {
                filesCount++;
                if (data.getFileInfoCache() != null) cachedFilesCount++;
                if (data.getActualizationInfo() != null) actualizationInfoCount++;
            }
        }

        statistic.setFilesCount(filesCount);
        statistic.setCachedFilesCount(cachedFilesCount);
        statistic.setActualizationInfoCount(actualizationInfoCount);
    }

    private List<FileData> getCachedFiles() {
        List<FileData> cachedFiles = new ArrayList<>();

        for (ConcurrentHashMap<String, FileData> innerMap : filesRepos.values()) {
            for (FileData data : innerMap.values()) {
                if (data.getFileInfoCache() != null) {
                    cachedFiles.add(data);
                }
            }
        }

        return cachedFiles;
    }

    private List<FileData> getFoldersWithActualizationInfo() {
        List<FileData> cachedFiles = new ArrayList<>();

        for (ConcurrentHashMap<String, FileData> innerMap : filesRepos.values()) {
            for (FileData data : innerMap.values()) {
                if (data.getActualizationInfo() != null) {
                    cachedFiles.add(data);
                }
            }
        }

        return cachedFiles;
    }

    private void removeDirRecursive(String path, ConcurrentHashMap<String, FileData> children) {
        if (children == null) return;

        for (Map.Entry<String, FileData> entry : children.entrySet()) {
            FileData data = entry.getValue();
            if (data == null || data.fileInfoCache == null || data.fileInfoCache.isDirectory) {
                String childPath = path + "/" + entry.getKey();
                ConcurrentHashMap<String, FileData> childChildren = filesRepos.getOrDefault(childPath, null);
                removeDirRecursive(childPath, childChildren);
            }
        }

        filesRepos.remove(path);
    }

    private TxtFile readTxt(String filePath) {
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

    private TxtFile saveTxt(String filePath, String[] content) {
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

    private boolean deleteTxt(String filePath) {
        Object[] dirAndName = parseDirAndFileNameFromFilePath(filePath);
        File file = new File((File) dirAndName[0], (String) dirAndName[1]);
        return file.delete();
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

    public static class TxtFile {
        public final String filePath;
        public final LocalDateTime updatedAt;
        public final String[] strings;

        private TxtFile(
                String filePath,
                LocalDateTime updatedAt,
                String[] strings
        ) {
            this.filePath = filePath;
            this.updatedAt = updatedAt;
            this.strings = strings;
        }
    }

    public static class FileData {
        public final String parentPath;
        public final String name;
        public final String path;

        @Nullable
        private ModelMediaFile fileInfoCache;
        @Nullable
        private ActualizationInfo actualizationInfo;

        private FileData(String parentPath, String name) {
            this.parentPath = parentPath;
            this.name = name;
            this.path = parentPath + "/" + name;
        }

        @Nullable
        public ModelMediaFile getFileInfoCache() {
            return fileInfoCache;
        }

        public void setFileInfoCache(@Nullable ModelMediaFile fileInfoCache) {
            this.fileInfoCache = fileInfoCache;
        }

        @Nullable
        public ActualizationInfo getActualizationInfo() {
            return actualizationInfo;
        }

        private void setActualizationInfo(@Nullable ActualizationInfo info) {
            this.actualizationInfo = info;
        }
    }

    public static class ActualizationInfo {
        public final String path;
        public final LocalDateTime updatedAt;
        public final boolean hasFolderChildren;

        private ActualizationInfo(
                String path,
                LocalDateTime updatedAt,
                boolean hasFolderChildren
        ) {
            this.path = path;
            this.updatedAt = updatedAt;
            this.hasFolderChildren = hasFolderChildren;
        }

        private ActualizationInfo(String jsonStr) {
            JSONObject json = JsonUtil.newJsonObject(jsonStr);
            this.path = JsonUtil.getString(json, "path", null);
            this.updatedAt = JsonUtil.getLocalDateTime(json, "updatedAt", null);
            this.hasFolderChildren = JsonUtil.getBoolean(json, "hasFolderChildren", null);
        }

        public String toJson() {
            JSONObject json = JsonUtil.newJsonObject();
            JsonUtil.addString(json, "path", path);
            JsonUtil.addLocalDateTime(json, "updatedAt", updatedAt);
            JsonUtil.addBoolean(json, "hasFolderChildren", hasFolderChildren);
            return json.toString();
        }
    }

    public static class Statistic {
        private int filesCount;
        private int cachedFilesCount;
        private int savedCachedFilesCount;
        private int actualizationInfoCount;
        private int savedActualizationInfoCount;

        private Statistic() {
            this.filesCount = 0;
            this.cachedFilesCount = 0;
            this.savedCachedFilesCount = 0;
            this.actualizationInfoCount = 0;
            this.savedActualizationInfoCount = 0;
        }

        public int getFilesCount() { return filesCount; }
        private void setFilesCount(int value) { filesCount = value; }

        public int getCachedFilesCount() { return cachedFilesCount; };
        private void setCachedFilesCount(int value) { cachedFilesCount = value; }

        public int getSavedCachedFilesCount() { return savedCachedFilesCount; };
        private void setSavedCachedFilesCount(int value) { savedCachedFilesCount = value; }

        public int getActualizationInfoCount() { return actualizationInfoCount; }
        private void setActualizationInfoCount(int value) { actualizationInfoCount = value; }

        public int getSavedActualizationInfoCount() { return savedActualizationInfoCount; }
        private void setSavedActualizationInfoCount(int value) { savedActualizationInfoCount = value; }
    }
}

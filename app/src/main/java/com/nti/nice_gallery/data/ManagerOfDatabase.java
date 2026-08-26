package com.nti.nice_gallery.data;

import android.content.Context;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.models.ModelMediaFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ManagerOfDatabase {
    private static final String CACHE_FILES_INFO_TXT = "cache/files_info.txt";

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, FileData>> filesRepos = new ConcurrentHashMap<>();

    private static TxtFile cacheTxt;

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

    public int getFilesCount() {
        int count = 0;

        for (ConcurrentHashMap<String, FileData> innerMap : filesRepos.values()) {
            count += innerMap.size();
        }

        return count;
    }

    public List<FileData> getCachedFiles() {
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

    public TxtFile getCacheTxt() {
        return cacheTxt;
    }

    public void storeFilesInfoCache() {
        List<FileData> cachedFiles = getCachedFiles();
        String[] filesInfoStr = new String[cachedFiles.size()];

        for (int i = 0; i < cachedFiles.size(); i++) {
            FileData data = cachedFiles.get(i);
            ModelMediaFile cache = data.getFileInfoCache();
            filesInfoStr[i] = cache != null ? cache.toJson() : "null";
        }

        cacheTxt = saveTxt(ManagerOfDatabase.CACHE_FILES_INFO_TXT, filesInfoStr);
    }

    public void restoreFilesInfoCache() {
        cacheTxt = readTxt(ManagerOfDatabase.CACHE_FILES_INFO_TXT);

        if (cacheTxt != null && cacheTxt.strings != null && cacheTxt.strings.length > 0) {
            for (int i = 0; i < cacheTxt.strings.length; i++) {
                String json = cacheTxt.strings[i];
                if (!Objects.equals(json, "null")) {
                    ModelMediaFile fileInfo = new ModelMediaFile(json);
                    FileData data = getOrCreateFile(fileInfo.path);
                    data.setFileInfoCache(fileInfo);
                }
            }
        }
    }

    public void actualizeFiles(File parent, File[] children) {
        String parentPath = parent.getAbsolutePath();
        ConcurrentHashMap<String, FileData> parentMap = filesRepos.getOrDefault(parentPath, null);
        if (parentMap == null) return;

        HashSet<String> childrenSet = new HashSet<>();
        for (File file : children) {
            childrenSet.add(file.getName());
        }

        for (String name : parentMap.keySet()) {
            if (!childrenSet.contains(name)) {
                parentMap.remove(name);
                String path = parentPath + "/" + name;
                removeDirRecursive(path, filesRepos.getOrDefault(path, null));
            }
        }
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

        public TxtFile(
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

        public FileData(String parentPath, String name) {
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
    }
}

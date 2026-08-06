package com.nti.nice_gallery.models;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.utils.JsonUtil;
import com.nti.nice_gallery.utils.ReadOnlyList;

import org.json.JSONObject;

import java.time.LocalDateTime;

public class ModelMediaFile {

    public static final ReadOnlyList<ModelFileFormat> supportedMediaFormats = new ReadOnlyList<>(new ModelFileFormat[] {
            new ModelFileFormat("image/png", "png", Type.Image),
            new ModelFileFormat("image/jpeg", "jpeg", Type.Image),
            new ModelFileFormat("image/jpg", "jpg", Type.Image),
            new ModelFileFormat("image/bmp", "bmp", Type.Image),
            new ModelFileFormat("image/avif", "avif", Type.Image),
            new ModelFileFormat("image/gif", "gif", Type.Image),
            new ModelFileFormat("image/heic", "heic", Type.Image),
            new ModelFileFormat("image/webp", "webp", Type.Image),
            new ModelFileFormat("video/mp4", "mp4", Type.Video),
            new ModelFileFormat("video/x-ms-wmv", "wmv", Type.Video),
            new ModelFileFormat("video/avi", "avi", Type.Video),
            new ModelFileFormat("video/mkv", "mkv", Type.Video),
            new ModelFileFormat("video/mov", "mov", Type.Video),
            new ModelFileFormat("video/webm", "webm", Type.Video),
    });

    public enum Type { Image, Video, Folder, Storage }

    public @interface Required {}
    public @interface ForImages {}
    public @interface ForVideos {}
    public @interface ForFolders {}
    public @interface ForStorages {}

    @Required
    public final String name;
    @Required
    public final String path;
    @Required
    public final Type type;

    @ForImages
    @ForVideos
    @ForFolders
    public final LocalDateTime createdAt;
    @ForImages
    @ForVideos
    @ForFolders
    public final LocalDateTime updatedAt;
    @ForImages
    @ForVideos
    @ForFolders
    public final Boolean isHidden;

    @ForImages
    @ForVideos
    public final Long weight;
    @ForImages
    @ForVideos
    public final Integer width;
    @ForImages
    @ForVideos
    public final Integer height;
    @ForImages
    @ForVideos
    public final Integer rotation;
    @ForImages
    @ForVideos
    public final String extension;

    @ForVideos
    public final Integer duration;

    @ForFolders
    @ForStorages
    public final Integer childElementsCount;

    @ForStorages
    public final Long freeSpace;
    @ForStorages
    public final Long totalSpace;

    @Nullable public final Exception error;

    public final boolean isFile;
    public final boolean isDirectory;
    public final boolean isImage;
    public final boolean isVideo;
    public final boolean isFolder;
    public final boolean isStorage;
    public final boolean isAnimatedImage;

    public ModelMediaFile(
            String name,
            String path,
            Type type,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isHidden,
            Long weight,
            Integer width,
            Integer height,
            Integer rotation,
            String extension,
            Integer duration,
            Integer childElementsCount,
            Long freeSpace,
            Long totalSpace,
            @Nullable Exception error
    ) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isHidden = isHidden;
        this.weight = weight;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.extension = extension;
        this.duration = duration;
        this.childElementsCount = childElementsCount;
        this.freeSpace = freeSpace;
        this.totalSpace = totalSpace;
        this.error = error;

        isFile = type == Type.Image || type == Type.Video;
        isDirectory = type == Type.Folder || type == Type.Storage;
        isImage = type == Type.Image;
        isVideo = type == Type.Video;
        isFolder = type == Type.Folder;
        isStorage = type == Type.Storage;
        isAnimatedImage = type == Type.Image && (extension != null && extension.equalsIgnoreCase("gif")
                || extension != null && extension.equalsIgnoreCase("webp"));
    }

    public ModelMediaFile(String jsonStr) {
        JSONObject json = JsonUtil.newJsonObject(jsonStr);
        this.name = JsonUtil.getString(json, "name", null);
        this.path = JsonUtil.getString(json, "path", null);
        this.type = JsonUtil.getEnum(json, "type", Type.class, null);
        this.createdAt = JsonUtil.getLocalDateTime(json, "createdAt", null);
        this.updatedAt = JsonUtil.getLocalDateTime(json, "updatedAt", null);
        this.isHidden = JsonUtil.getBoolean(json, "isHidden", null);
        this.weight = JsonUtil.getLong(json, "weight", null);
        this.width = JsonUtil.getInt(json, "width", null);
        this.height = JsonUtil.getInt(json, "height", null);
        this.rotation = JsonUtil.getInt(json, "rotation", null);
        this.extension = JsonUtil.getString(json, "extension", null);
        this.duration = JsonUtil.getInt(json, "duration", null);
        this.childElementsCount = JsonUtil.getInt(json, "childElementsCount", null);
        this.freeSpace = JsonUtil.getLong(json, "freeSpace", null);
        this.totalSpace = JsonUtil.getLong(json, "totalSpace", null);
        String errorMessage = JsonUtil.getString(json, "error", null);
        this.error = errorMessage != null ? new Exception(errorMessage) : null;

        isFile = type == Type.Image || type == Type.Video;
        isDirectory = type == Type.Folder || type == Type.Storage;
        isImage = type == Type.Image;
        isVideo = type == Type.Video;
        isFolder = type == Type.Folder;
        isStorage = type == Type.Storage;
        isAnimatedImage = type == Type.Image && (extension != null && extension.equalsIgnoreCase("gif")
                || extension != null && extension.equalsIgnoreCase("webp"));
    }

    public String toJson() {
        JSONObject json = JsonUtil.newJsonObject();
        JsonUtil.addString(json, "name", name);
        JsonUtil.addString(json, "path", path);
        JsonUtil.addEnum(json, "type", type);
        JsonUtil.addLocalDateTime(json, "createdAt", createdAt);
        JsonUtil.addLocalDateTime(json, "updatedAt", updatedAt);
        JsonUtil.addBoolean(json, "isHidden", isHidden);
        JsonUtil.addLong(json, "weight", weight);
        JsonUtil.addInt(json, "width", width);
        JsonUtil.addInt(json, "height", height);
        JsonUtil.addInt(json, "rotation", rotation);
        JsonUtil.addString(json, "extension", extension);
        JsonUtil.addInt(json, "duration", duration);
        JsonUtil.addInt(json, "childElementsCount", childElementsCount);
        JsonUtil.addLong(json, "freeSpace", freeSpace);
        JsonUtil.addLong(json, "totalSpace", totalSpace);
        JsonUtil.addString(json, "error", error != null ? error.getMessage() : null);
        return json.toString();
    }
}

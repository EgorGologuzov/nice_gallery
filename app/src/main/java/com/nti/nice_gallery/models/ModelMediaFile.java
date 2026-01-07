package com.nti.nice_gallery.models;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.utils.ReadOnlyList;

import java.time.LocalDateTime;
import java.util.Date;

public class ModelMediaFile {

    public static final ReadOnlyList<ModelFileFormat> supportedMediaFormats = new ReadOnlyList<>(new ModelFileFormat[] {
            new ModelFileFormat("image/png", "png", Type.Image),
            new ModelFileFormat("image/jpeg", "jpeg", Type.Image),
            new ModelFileFormat("image/jpg", "jpg", Type.Image),
            new ModelFileFormat("image/bmp", "bmp", Type.Image),
            new ModelFileFormat("video/mp4", "mp4", Type.Video),
            new ModelFileFormat("video/x-ms-wmv", "wmv", Type.Video)
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

    public ModelMediaFile(
            String name,
            String path,
            Type type,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
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
    }
}

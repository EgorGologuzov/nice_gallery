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

    public @interface ForFilesOnly {}
    public @interface ForVideosOnly {}

    public final String name;
    public final String path;
    public final Type type;
    public final LocalDateTime createdAt;
    public final LocalDateTime updatedAt;

    @ForFilesOnly public final Long weight;
    @ForFilesOnly public final Integer width;
    @ForFilesOnly public final Integer height;
    @ForFilesOnly public final Integer rotation;
    @ForFilesOnly public final String extension;

    @ForVideosOnly public final Integer duration;

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
            @ForFilesOnly Long weight,
            @ForFilesOnly Integer width,
            @ForFilesOnly Integer height,
            @ForFilesOnly Integer rotation,
            @ForFilesOnly String extension,
            @ForVideosOnly Integer duration,
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
        this.error = error;

        isFile = type == Type.Image || type == Type.Video;
        isDirectory = type == Type.Folder || type == Type.Storage;
        isImage = type == Type.Image;
        isVideo = type == Type.Video;
        isFolder = type == Type.Folder;
        isStorage = type == Type.Storage;
    }
}

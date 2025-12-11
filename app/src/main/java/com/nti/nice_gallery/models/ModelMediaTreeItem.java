package com.nti.nice_gallery.models;

import android.util.Size;

import java.util.Date;

public class ModelMediaTreeItem {

    public static final ModelFileFormat[] supportedMediaFormats = new ModelFileFormat[] {
            new ModelFileFormat("image/png", "png", Type.Image),
            new ModelFileFormat("image/jpeg", "jpg", Type.Image),
            new ModelFileFormat("image/jpg", "jpg", Type.Image),
            new ModelFileFormat("image/bmp", "bmp", Type.Image),
            new ModelFileFormat("video/mp4", "mp4", Type.Video),
            new ModelFileFormat("video/x-ms-wmv", "wmv", Type.Video)
    };

    public enum Type { Image, Video, Folder }

    public @interface ForFilesOnly {}
    public @interface ForVideosOnly {}

    public final Long mediaId;
    public final String name;
    public final String path;
    public final Type type;
    public final Long weight;
    public final Date createdAt;
    public final Date updatedAt;

    @ForFilesOnly public final Size resolution;
    @ForFilesOnly public final String extension;

    @ForVideosOnly public final Integer duration;

    public ModelMediaTreeItem(
            Long mediaId,
            String name,
            String path,
            Type type,
            Long weight,
            Date createdAt,
            Date updatedAt,
            @ForFilesOnly Size resolution,
            @ForFilesOnly String extension,
            @ForVideosOnly Integer duration
    ) {
        this.mediaId = mediaId;
        this.name = name;
        this.path = path;
        this.type = type;
        this.weight = weight;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolution = resolution;
        this.extension = extension;
        this.duration = duration;
    }
}

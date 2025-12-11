package com.nti.nice_gallery.data;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import com.nti.nice_gallery.models.ModelMediaTreeItem;
import com.nti.nice_gallery.models.ModelStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import kotlin.jvm.functions.Function1;

public class ManagerOfFiles implements IManagerOfFiles {

    private final static Size THUMBNAIL_TARGET_SIZE = new Size(512, 512);

    private final Context context;
    private final ContentResolver contentResolver;

    public ManagerOfFiles(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    @Override
    public List<ModelMediaTreeItem> getAllFiles() {
        List<ModelMediaTreeItem> mediaList = new ArrayList<>();

        mediaList.addAll(getMediaFromMediaStore(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ModelMediaTreeItem.Type.Image));

        mediaList.addAll(getMediaFromMediaStore(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                ModelMediaTreeItem.Type.Video));

        return mediaList;
    }

    @Override
    public Bitmap getItemThumbnail(ModelMediaTreeItem item) {
        return getItemThumbnailFromMediaStore(item);
    }

    @Override
    public Uri getItemContentUri(ModelMediaTreeItem item) {
        return getContentUriFromMediaStore(item);
    }

    @Override
    public List<ModelStorage> getAllStorages() {
        return Collections.emptyList();
    }

    @SuppressLint("Range")
    private List<ModelMediaTreeItem> getMediaFromMediaStore(Uri contentUri, ModelMediaTreeItem.Type type) {

        String[] imageProjection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.ORIENTATION
        };
        String[] videoProjection = new String[] {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.ORIENTATION
        };

        String[] projection = type == ModelMediaTreeItem.Type.Image ? imageProjection : videoProjection;
        String sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";

        List<ModelMediaTreeItem> mediaList = new ArrayList<>();

        Function1<String, Boolean> isSupportedFormat = mimeType ->
                Arrays.stream(ModelMediaTreeItem.supportedMediaFormats)
                        .anyMatch(f -> f.mimeType.equalsIgnoreCase(mimeType));

        Function1<String, String> getFileExtension = fileName -> {
            int lastDot = fileName.lastIndexOf('.');
            return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
        };

        try (Cursor cursor = contentResolver.query(contentUri, projection, null, null, sortOrder)) {
            if (cursor == null || !cursor.moveToFirst()) {
                return mediaList;
            }

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
            int addedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);
            int modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED);
            int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH);
            int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT);
            int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);
            int orientationColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.ORIENTATION);

            int durationColumn = -1;
            if (type == ModelMediaTreeItem.Type.Video) {
                durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            }

            do {
                try {
                    String path = cursor.getString(pathColumn);
                    String mimeType = cursor.getString(mimeTypeColumn);

                    if (path == null || !isSupportedFormat.invoke(mimeType)) {
                        continue;
                    }

                    long mediaId = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    Long size = cursor.getLong(sizeColumn);
                    Date createAt = new Date(cursor.getLong(addedColumn) * 1000L);
                    Date updateAt = new Date(cursor.getLong(modifiedColumn) * 1000L);
                    int orientation = cursor.getInt(orientationColumn);
                    String extension = getFileExtension.invoke(name);

                    int width = cursor.getInt(widthColumn);
                    int height = cursor.getInt(heightColumn);
                    Size resolution = orientation == 90 || orientation == 270 ? new Size(height, width) : new Size(width, height);

                    Integer duration = null;
                    if (type == ModelMediaTreeItem.Type.Video && durationColumn != -1) {
                        duration = (int) (cursor.getLong(durationColumn) / 1000);
                    }

                    ModelMediaTreeItem item = new ModelMediaTreeItem(
                            mediaId,
                            name,
                            path,
                            type,
                            size,
                            createAt,
                            updateAt,
                            resolution,
                            extension,
                            duration
                    );

                    Log.d("QUILT_ALGORITHM", String.format("%s: %dx%d", item.path, item.resolution.getWidth(), item.resolution.getHeight()));

                    mediaList.add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mediaList;
    }

    private Bitmap getItemThumbnailFromMediaStore(ModelMediaTreeItem item) {
        try {
            Uri contentUri = (item.type == ModelMediaTreeItem.Type.Image)
                    ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    : MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            return contentResolver.loadThumbnail(
                    ContentUris.withAppendedId(contentUri, item.mediaId),
                    THUMBNAIL_TARGET_SIZE,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Uri getContentUriFromMediaStore(ModelMediaTreeItem item) {
        if (item.type == ModelMediaTreeItem.Type.Image) {
            return ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    item.mediaId
            );
        } else if (item.type == ModelMediaTreeItem.Type.Video) {
            return ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    item.mediaId
            );
        }

        return Uri.fromFile(new File(item.path));
    }
}
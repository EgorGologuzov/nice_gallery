package com.nti.nice_gallery.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.util.Size;

import androidx.core.content.ContextCompat;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelFileFormat;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelStorage;
import com.nti.nice_gallery.models.ReadOnlyList;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class ManagerOfFiles implements IManagerOfFiles {

    private static final String LOG_TAG = "ManagerOfFiles";

    private final Context context;

    public ManagerOfFiles(Context context) {
        this.context = context;
    }

    @Override
    public ModelGetFilesResponse getFiles(ModelGetFilesRequest request) {

        Function1<ModelStorage, List<ModelMediaFile>> getStorageFiles = storage -> {
            List<ModelMediaFile> files = new ArrayList<>();

            try {
                File storageFolder = new File(storage.path);
                recursionScanning(storageFolder, files);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            return files;
        };

        final List<ModelStorage> storages = getAllStorages();
        final List<ModelMediaFile> files = new ArrayList<>();
        final List<ModelStorage> storagesWithErrors = new ArrayList<>();
        final List<ModelMediaFile> filesWithErrors = new ArrayList<>();

        for (ModelStorage storage : storages) {
            if (storage.error == null) {
                List<ModelMediaFile> storageFiles = getStorageFiles.invoke(storage);
                files.addAll(storageFiles);
                for (ModelMediaFile file : storageFiles) {
                    if (file.error != null) {
                        filesWithErrors.add(file);
                    }
                }
            } else {
                storagesWithErrors.add(storage);
            }
        }

        return new ModelGetFilesResponse(
                new ReadOnlyList<>(files),
                new ReadOnlyList<>(storages),
                new ReadOnlyList<>(filesWithErrors),
                new ReadOnlyList<>(storagesWithErrors)
        );
    }

    @Override
    public Bitmap getFilePreview(ModelMediaFile item) {

        final Size TARGET_PREVIEW_RESOLUTION = new Size(250, 250);
        final int VIDEO_PREVIEW_TIMING = 0;

        Function1<ModelMediaFile, Integer> calcInSampleSize = _item -> {

            final int reqWidth = TARGET_PREVIEW_RESOLUTION.getWidth();
            final int reqHeight = TARGET_PREVIEW_RESOLUTION.getHeight();
            int width = _item.width;
            int height = _item.height;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        };

        Function2<Bitmap, Integer, Bitmap> rotateBitmap = (source, angle) -> {
            if (source == null || angle == 0) return source;
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        };

        Function1<ModelMediaFile, Bitmap> getImagePreview = _item -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calcInSampleSize.invoke(_item);
            options.inPreferredConfig = android.graphics.Bitmap.Config.RGB_565;
            Bitmap result = BitmapFactory.decodeFile(item.path, options);
            return rotateBitmap.invoke(result, _item.rotation);
        };

        Function1<ModelMediaFile, Bitmap> getVideoPreview = _item -> {
            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(_item.path);
                return retriever.getScaledFrameAtTime(
                        VIDEO_PREVIEW_TIMING,
                        android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                        TARGET_PREVIEW_RESOLUTION.getWidth(),
                        TARGET_PREVIEW_RESOLUTION.getHeight());
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                return null;
            }
        };

        if (item.type == ModelMediaFile.Type.Image) {
            Bitmap bitmap = getImagePreview.invoke(item);
            return bitmap;
        }

        if (item.type == ModelMediaFile.Type.Video) {
            Bitmap bitmap = getVideoPreview.invoke(item);
            return bitmap;
        }

        return null;
    }

    @Override
    public List<ModelStorage> getAllStorages() {

        Function1<StorageVolume, ModelStorage> getStorageInfo = volume -> {
            String name = null;
            String path = null;
            ModelStorage.Type type = null;
            Exception error = null;

            try {
                File storageDir = volume.getDirectory();
                path = storageDir.getAbsolutePath();

                name = String.format(context.getResources().getString(R.string.format_name_storage_name),
                        volume.getDescription(context),
                        path);

                type = volume.isPrimary()
                        ? ModelStorage.Type.Primary
                        : volume.isRemovable()
                        ? ModelStorage.Type.Removable
                        : ModelStorage.Type.Else;
            } catch (Exception e) {
                error = e;
                Log.e(LOG_TAG, e.getMessage());
            }

            return new ModelStorage(
                    name,
                    path,
                    type,
                    error
            );
        };

        List<ModelStorage> storages = new ArrayList<>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();

        for (StorageVolume volume : storageVolumes) {
            ModelStorage storage = getStorageInfo.invoke(volume);
            storages.add(storage);
        }

        return storages;
    }

    private void recursionScanning(File folder, List<ModelMediaFile> files) {
        File[] folderFiles = folder.listFiles(file -> !file.isHidden() && !file.getName().startsWith("."));

        if (folderFiles == null || folderFiles.length == 0) {
            return;
        }

        for (File file : folderFiles) {
            if (file.isDirectory()) {
                recursionScanning(file, files);
                continue;
            }

            ModelMediaFile item = getFileInfo(file);
            if (item != null) {
                files.add(item);
            }
        }
    }

    private ModelMediaFile getFileInfo(File file) {

        class ImageContentInfo {
            public int width;
            public int height;
            public int rotation;
        }

        class VideoContentInfo {
            public int width;
            public int height;
            public int rotation;
            public int duration;
        }

        Function1<File, Date> getFileCreationTime = _file -> {
            try {
                BasicFileAttributes attrs = Files.readAttributes(_file.toPath(), BasicFileAttributes.class);
                return new Date(attrs.creationTime().toMillis());
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                return null;
            }
        };

        Function2<String, Boolean, ModelMediaFile.Type> getFileType = (fileName, isDirectory) -> {
            if (isDirectory) {
                return ModelMediaFile.Type.Folder;
            }

            fileName = fileName.toLowerCase();

            for (ModelFileFormat fileFormat : ModelMediaFile.supportedMediaFormats) {
                if (fileName.endsWith(fileFormat.fileExtension)) {
                    return fileFormat.type;
                }
            }

            return null;
        };

        Function1<String, ImageContentInfo> getImageContentInfo = path -> {
            ExifInterface exif;

            try {
                exif = new ExifInterface(path);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                return null;
            }

            int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
            int height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            if (width <= 0 || height <= 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                width = options.outWidth;
                height = options.outHeight;
            }

            switch (rotation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90; break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270; break;
                default:
                    rotation = 0; break;
            }

            if (rotation == 90 || rotation == 270) {
                int tmp = width;
                width = height;
                height = tmp;
            }

            ImageContentInfo info = new ImageContentInfo();
            info.width = width;
            info.height = height;
            info.rotation = rotation;

            return info;
        };

        Function1<String, VideoContentInfo> getVideoContentInfo = path -> {
            String widthStr;
            String heightStr;
            String rotationStr;
            String durationStr;

            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(path);
                widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                rotationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                return null;
            }

            int width = 0;
            int height = 0;
            int rotation = 0;
            int duration = 0;

            if (widthStr != null) {
                width = Integer.parseInt(widthStr);
            }
            if (heightStr != null) {
                height = Integer.parseInt(heightStr);
            }
            if (rotationStr != null) {
                rotation = Integer.parseInt(rotationStr);
            }
            if (durationStr != null) {
                duration = Integer.parseInt(durationStr) / 1000; // millis to seconds
            }

            if (rotation == 90 || rotation == 270) {
                int tmp = width;
                width = height;
                height = tmp;
            }

            VideoContentInfo info = new VideoContentInfo();
            info.width = width;
            info.height = height;
            info.rotation = rotation;
            info.duration = duration;

            return info;
        };

        Function1<String, String> getFileExtension = fileName -> {
            int lastIndexOf = fileName.lastIndexOf(".");
            return lastIndexOf != -1 ? fileName.substring(lastIndexOf + 1) : null;
        };

        String name = null;
        String path = null;
        ModelMediaFile.Type type = null;
        Date createAt = null;
        Date updateAt = null;
        Long weight = null;
        Integer width = null;
        Integer height = null;
        Integer rotation = null;
        String extension = null;
        Integer duration = null;
        Exception error = null;

        try {
            name = file.getName();
            Boolean isDirectory = file.isDirectory();
            type = getFileType.invoke(name, isDirectory);

            if (type == null) return null;

            path = file.getAbsolutePath();
            createAt = getFileCreationTime.invoke(file);
            updateAt = new Date(file.lastModified());

            if (type != ModelMediaFile.Type.Folder) {
                weight = file.length();
                extension = getFileExtension.invoke(name);
            }

            if (type == ModelMediaFile.Type.Image) {
                ImageContentInfo info = getImageContentInfo.invoke(path);
                width = info.width;
                height = info.height;
                rotation = info.rotation;
            }

            if (type == ModelMediaFile.Type.Video) {
                VideoContentInfo info = getVideoContentInfo.invoke(path);
                width = info.width;
                height = info.height;
                rotation = info.rotation;
                duration = info.duration;
            }
        } catch (Exception e) {
            error = e;
            Log.e(LOG_TAG, e.getMessage());
        }

        return new ModelMediaFile(
                name,
                path,
                type,
                createAt,
                updateAt,
                weight,
                width,
                height,
                rotation,
                extension,
                duration,
                error
        );
    }
}
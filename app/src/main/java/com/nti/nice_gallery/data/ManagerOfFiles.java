package com.nti.nice_gallery.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.util.Size;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelFileFormat;
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelGetPreviewRequest;
import com.nti.nice_gallery.models.ModelGetPreviewResponse;
import com.nti.nice_gallery.models.ModelGetStoragesRequest;
import com.nti.nice_gallery.models.ModelGetStoragesResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.models.ModelStorage;
import com.nti.nice_gallery.utils.ManagerOfThreads;
import com.nti.nice_gallery.utils.ReadOnlyList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class ManagerOfFiles implements IManagerOfFiles {

    public static final String PATH_ROOT = "/";

    private static final String LOG_TAG = "ManagerOfFiles";

    private final Context context;

    private final ManagerOfThreads managerOfThreads;

    public ManagerOfFiles(Context context) {
        this.context = context;
        this.managerOfThreads = new ManagerOfThreads(context);
    }

    @Override
    public void getStoragesAsync(ModelGetStoragesRequest request, Consumer<ModelGetStoragesResponse> callback) {

        final ModelGetStoragesRequest DEFAULT_REQUEST = new ModelGetStoragesRequest();
        final ModelGetStoragesRequest requestFinal = request == null ? DEFAULT_REQUEST : request;

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
                Log.e(LOG_TAG + 1, e.getMessage());
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

        ModelGetStoragesResponse response = new ModelGetStoragesResponse(
                new ReadOnlyList<>(storages)
        );

        managerOfThreads.safeAccept(callback, response);
    }

    @Override
    public void getFilesAsync(ModelGetFilesRequest request, Consumer<ModelGetFilesResponse> callback) {

        final ModelGetFilesRequest DEFAULT_REQUEST = new ModelGetFilesRequest(null, null, null, null, null);
        final ModelGetFilesRequest requestFinal = request == null ? DEFAULT_REQUEST : request;

        Function1<ModelStorage, List<ModelMediaFile>> getStorageFiles = storage -> {
            List<ModelMediaFile> files = new ArrayList<>();

            ModelScanParams.StorageParams storageParams = null;

            if (requestFinal.scanParams != null && requestFinal.scanParams.storagesParams != null) {
                storageParams = requestFinal.scanParams.storagesParams
                        .stream()
                        .filter(sp -> Objects.equals(sp.storageName, storage.name))
                        .findFirst()
                        .orElse(null);
            }

            try {
                File storageFolder = new File(storage.path);
                storageRecursionScanning(storageFolder, files, requestFinal.filters, storageParams);
            } catch (Exception e) {
                Log.e(LOG_TAG + 2, e.getMessage());
            }

            return files;
        };

        Runnable returnStoragesList = () -> {
            final LocalDateTime startedAt = LocalDateTime.now();

            getStoragesAsync(null, getStoragesResponse -> {
                final List<ModelMediaFile> files = new ArrayList<>();
                final List<ModelStorage> storagesWithErrors = new ArrayList<>();
                final List<ModelMediaFile> filesWithErrors = new ArrayList<>();

                for (ModelStorage storage : getStoragesResponse.storages) {
                    files.add(new ModelMediaFile(
                            storage.name,
                            storage.path,
                            ModelMediaFile.Type.Folder,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    ));
                }

                final List<ModelMediaFile> sortedFiles = sortFiles(files, requestFinal.sortVariant, requestFinal.foldersFirst);

                ModelGetFilesResponse getFilesResponse = new ModelGetFilesResponse(
                        startedAt,
                        LocalDateTime.now(),
                        new ReadOnlyList<>(sortedFiles),
                        getStoragesResponse.storages,
                        new ReadOnlyList<>(filesWithErrors),
                        new ReadOnlyList<>(storagesWithErrors),
                        requestFinal.path
                );

                managerOfThreads.safeAccept(callback, getFilesResponse);
            });
        };

        Runnable returnFolderFilesList = () -> {
            final LocalDateTime startedAt = LocalDateTime.now();

            final List<ModelMediaFile> files = new ArrayList<>();
            final List<ModelMediaFile> filesWithErrors = new ArrayList<>();

            folderScanning(new File(requestFinal.path), files, requestFinal.filters);

            for (ModelMediaFile file : files) {
                if (file.error != null) {
                    filesWithErrors.add(file);
                }
            }

            final List<ModelMediaFile> sortedFiles = sortFiles(files, requestFinal.sortVariant, requestFinal.foldersFirst);

            ModelGetFilesResponse getFilesResponse = new ModelGetFilesResponse(
                    startedAt,
                    LocalDateTime.now(),
                    new ReadOnlyList<>(sortedFiles),
                    new ReadOnlyList<>(new ArrayList<>()),
                    new ReadOnlyList<>(filesWithErrors),
                    new ReadOnlyList<>(new ArrayList<>()),
                    requestFinal.path
            );

            managerOfThreads.safeAccept(callback, getFilesResponse);
        };

        Runnable scanByParams = () -> {
            final LocalDateTime startedAt = LocalDateTime.now();

            getStoragesAsync(null, getStoragesResponse -> {
                final List<ModelMediaFile> files = new ArrayList<>();
                final List<ModelStorage> storagesWithErrors = new ArrayList<>();
                final List<ModelMediaFile> filesWithErrors = new ArrayList<>();

                for (ModelStorage storage : getStoragesResponse.storages) {
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

                final List<ModelMediaFile> sortedFiles = sortFiles(files, requestFinal.sortVariant, requestFinal.foldersFirst);

                ModelGetFilesResponse getFilesResponse = new ModelGetFilesResponse(
                        startedAt,
                        LocalDateTime.now(),
                        new ReadOnlyList<>(sortedFiles),
                        getStoragesResponse.storages,
                        new ReadOnlyList<>(filesWithErrors),
                        new ReadOnlyList<>(storagesWithErrors),
                        null
                );

                managerOfThreads.safeAccept(callback, getFilesResponse);
            });
        };

        Runnable scanByPath = () -> {
            if (Objects.equals(requestFinal.path, PATH_ROOT)) {
                returnStoragesList.run();
            } else {
                returnFolderFilesList.run();
            }
        };

        if (requestFinal.path == null) {
            scanByParams.run();
        } else {
            scanByPath.run();
        }
    }

    @Override
    public void getPreviewAsync(ModelGetPreviewRequest request, Consumer<ModelGetPreviewResponse> callback) {

        final Size TARGET_PREVIEW_RESOLUTION = new Size(250, 250);
        final int VIDEO_PREVIEW_TIMING = 0;

        final ModelGetPreviewRequest DEFAULT_REQUEST = new ModelGetPreviewRequest(null);
        final ModelGetPreviewRequest requestFinal = request == null ? DEFAULT_REQUEST : request;

        if (requestFinal == DEFAULT_REQUEST) {
            managerOfThreads.safeAccept(callback, new ModelGetPreviewResponse(null));
            return;
        }

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
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = calcInSampleSize.invoke(_item);
                options.inPreferredConfig = android.graphics.Bitmap.Config.RGB_565;
                Bitmap result = BitmapFactory.decodeFile(requestFinal.file.path, options);
                return rotateBitmap.invoke(result, _item.rotation);
            } catch (Exception e) {
                Log.e(LOG_TAG + 3, e.getMessage());
                return null;
            }
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
                Log.e(LOG_TAG + 4, e.getMessage());
                return null;
            }
        };

        Bitmap bitmap = null;

        if (requestFinal.file.type == ModelMediaFile.Type.Image) {
            bitmap = getImagePreview.invoke(requestFinal.file);
        }
        if (requestFinal.file.type == ModelMediaFile.Type.Video) {
            bitmap = getVideoPreview.invoke(requestFinal.file);
        }

        ModelGetPreviewResponse response = new ModelGetPreviewResponse(
                bitmap
        );

        managerOfThreads.safeAccept(callback, response);
    }

    private void storageRecursionScanning(File folder, List<ModelMediaFile> files, ModelFilters filters, ModelScanParams.StorageParams scanParams) {

        final int PATH_IS_NOT_TARGET = 0;
        final int PATH_IS_TARGET = 1;
        final int PATH_IS_TARGET_CHILD = 2;
        final int PATH_IS_TARGET_PARENT = 3;

        Function2<String, ReadOnlyList<String>, Integer> getPathStatus = (path, targetPaths) -> {
            if (path == null || targetPaths == null) {
                return PATH_IS_NOT_TARGET;
            }

            for (String targetPath : targetPaths) {
                if (path.equals(targetPath)) {
                    return PATH_IS_TARGET;
                }
                if (path.startsWith(targetPath)) {
                    return PATH_IS_TARGET_CHILD;
                }
                if (targetPath.startsWith(path)) {
                    return PATH_IS_TARGET_PARENT;
                }
            }

            return PATH_IS_NOT_TARGET;
        };

        File[] folderFiles = null;

        if (scanParams != null) {
            int pathStatus = getPathStatus.invoke(folder.getAbsolutePath(), scanParams.paths);
            if (scanParams.scanMode == ModelScanParams.ScanMode.ScanAll) {
                folderFiles = folder.listFiles();
            } else if (scanParams.scanMode == ModelScanParams.ScanMode.ScanPathsInListOnly) {
                switch (pathStatus) {
                    case PATH_IS_TARGET_PARENT: folderFiles = folder.listFiles(f -> f.isDirectory()); break;
                    case PATH_IS_TARGET:
                    case PATH_IS_TARGET_CHILD: folderFiles = folder.listFiles(); break;
                }
            } else if (scanParams.scanMode == ModelScanParams.ScanMode.ScanPathsNotInListOnly) {
                switch (pathStatus) {
                    case PATH_IS_NOT_TARGET:
                    case PATH_IS_TARGET_PARENT: folderFiles = folder.listFiles(); break;
                }
            }
        } else {
            folderFiles = folder.listFiles();
        }

        if (folderFiles == null || folderFiles.length == 0) {
            return;
        }

        for (File file : folderFiles) {
            ModelMediaFile model = getFileInfo(file);
            if (model == null) {
                continue;
            }
            if (file.isDirectory()) {
                if (foldersFilter(file, model, filters)) {
                    storageRecursionScanning(file, files, filters, scanParams);
                }
            } else if (filesFilter(filters, file, model)) {
                files.add(model);
            }
        }
    }

    private void folderScanning(File folder, List<ModelMediaFile> files, ModelFilters filters) {
        if (folder == null) {
            return;
        }

        File[] folderFiles = folder.listFiles();

        if (folderFiles == null || folderFiles.length == 0) {
            return;
        }

        for (File file : folderFiles) {
            ModelMediaFile model = getFileInfo(file);
            if (model == null) {
                continue;
            }
            if (file.isDirectory()) {
                if (foldersFilter(file, model, filters)) {
                    files.add(model);
                }
            } else if (filesFilter(filters, file, model)) {
                files.add(model);
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

        Function1<File, LocalDateTime> getFileCreationTime = _file -> {
            try {
                if (!_file.isDirectory()) {
                    ExifInterface exif = new ExifInterface(_file.getAbsolutePath());
                    String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);

                    if (dateString != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
                        return LocalDateTime.parse(dateString, formatter);
                    }
                }

                BasicFileAttributes attrs = Files.readAttributes(Paths.get(_file.getAbsolutePath()), BasicFileAttributes.class);
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(attrs.creationTime().toMillis()), ZoneId.systemDefault());
            } catch (IOException e) {
                Log.e(LOG_TAG + 5, e.getMessage());
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(_file.lastModified()), ZoneId.systemDefault());
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
                Log.e(LOG_TAG + 6, e.getMessage());
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
                Log.e(LOG_TAG + 7, e.getMessage());
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
        LocalDateTime createAt = null;
        LocalDateTime updateAt = null;
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
            updateAt = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime();

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
            Log.e(LOG_TAG + 8, e.getMessage());
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

    private boolean foldersFilter(File folder, ModelMediaFile model, ModelFilters filters) {
        if (model == null) {
            return false;
        }

        if (filters != null) {
            if (filters.ignoreHidden && folder.isHidden()) {
                return false;
            }
        }

        return true;
    }

    private boolean filesFilter(ModelFilters filters, File file, ModelMediaFile model) {
        if (model == null) {
            return false;
        }

        if (filters != null) {
            if (filters.ignoreHidden && file.isHidden()) {
                return false;
            }
            if (filters.types != null && !filters.types.isEmpty() && (model.type == null || !filters.types.contains(model.type))) {
                return false;
            }
            if (filters.minWeight != null && (model.weight == null || filters.minWeight > model.weight)) {
                return false;
            }
            if (filters.maxWeight != null && (model.weight == null || filters.maxWeight < model.weight)) {
                return false;
            }
            if (filters.minCreateAt != null && (model.createdAt == null || model.createdAt.isBefore(filters.minCreateAt))) {
                return false;
            }
            if (filters.maxCreateAt != null && (model.createdAt == null || model.createdAt.isAfter(filters.maxCreateAt))) {
                return false;
            }
            if (filters.minUpdateAt != null && (model.updatedAt == null || model.updatedAt.isBefore(filters.minUpdateAt))) {
                return false;
            }
            if (filters.maxUpdateAt != null && (model.updatedAt == null || model.updatedAt.isAfter(filters.maxUpdateAt))) {
                return false;
            }
            if (filters.extensions != null && !filters.extensions.isEmpty() && (model.extension == null || !filters.extensions.contains(model.extension.toLowerCase()))) {
                return false;
            }
            if (filters.minDuration != null && (model.duration == null || filters.minDuration > model.duration)) {
                return false;
            }
            if (filters.maxDuration != null && (model.duration == null || filters.maxDuration < model.duration)) {
                return false;
            }
        }

        return true;
    }

    private List<ModelMediaFile> sortFiles(List<ModelMediaFile> files, ModelGetFilesRequest.SortVariant sortVariant, boolean foldersFirst) {

        Function2<Comparable, Comparable, Integer> safetyCompare = (p1, p2) -> {
            if (p1 == null && p2 == null) {
                return 0;
            }
            if (p1 == null) {
                return 1;
            }
            if (p2 == null) {
                return -1;
            }
            return p1.compareTo(p2);
        };

        Function1<List<ModelMediaFile>, List<ModelMediaFile>> sort = _files -> {
            if (sortVariant == null) {
                return _files;
            }

            switch (sortVariant) {
                case ByName: _files.sort((f1, f2) -> safetyCompare.invoke(f1.name, f2.name)); break;
                case ByNameDesc: _files.sort((f1, f2) -> -safetyCompare.invoke(f1.name, f2.name)); break;
                case ByCreateAt: _files.sort((f1, f2) -> safetyCompare.invoke(f1.createdAt, f2.createdAt)); break;
                case ByCreateAtDesc: _files.sort((f1, f2) -> -safetyCompare.invoke(f1.createdAt, f2.createdAt)); break;
                case ByUpdateAt: _files.sort((f1, f2) -> safetyCompare.invoke(f1.updatedAt, f2.updatedAt)); break;
                case ByUpdateAtDesc: _files.sort((f1, f2) -> -safetyCompare.invoke(f1.updatedAt, f2.updatedAt)); break;
                case ByWeight: _files.sort((f1, f2) -> safetyCompare.invoke(f1.weight, f2.weight)); break;
                case ByWeightDesc: _files.sort((f1, f2) -> -safetyCompare.invoke(f1.weight, f2.weight)); break;
            }

            return _files;
        };

        List<ModelMediaFile> foldersOnly = new ArrayList<>();
        List<ModelMediaFile> filesOnly = new ArrayList<>();

        for (ModelMediaFile item : files) {
            List<ModelMediaFile> list = item.type == ModelMediaFile.Type.Folder ? foldersOnly : filesOnly;
            list.add(item);
        }

        foldersOnly = sort.invoke(foldersOnly);
        filesOnly = sort.invoke(filesOnly);

        List<ModelMediaFile> result = new ArrayList<>();

        if (foldersFirst) {
            result.addAll(foldersOnly);
            result.addAll(filesOnly);
        } else {
            result.addAll(filesOnly);
            result.addAll(foldersOnly);
        }

        return result;
    }
}
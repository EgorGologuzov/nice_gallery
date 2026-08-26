package com.nti.nice_gallery.data;

import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.util.Size;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelFileFormat;
import com.nti.nice_gallery.models.ModelFilesActionRequest;
import com.nti.nice_gallery.models.ModelFilesActionResponse;
import com.nti.nice_gallery.models.ModelFilters;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelGetPathsRequest;
import com.nti.nice_gallery.models.ModelGetPathsResponse;
import com.nti.nice_gallery.models.ModelGetPreviewRequest;
import com.nti.nice_gallery.models.ModelGetPreviewResponse;
import com.nti.nice_gallery.models.ModelGetStoragesRequest;
import com.nti.nice_gallery.models.ModelGetStoragesResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelRequestProgress;
import com.nti.nice_gallery.models.ModelScanParams;
import com.nti.nice_gallery.models.ModelStorage;
import com.nti.nice_gallery.utils.ManagerOfThreads;
import com.nti.nice_gallery.utils.ReadOnlyList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class ManagerOfFiles implements IManagerOfFiles {

    public static final String PATH_ROOT = "/";

    private static final String LOG_TAG = "ManagerOfFiles";

    private final Context context;

    private final ManagerOfThreads managerOfThreads;
    private final ManagerOfCache managerOfCache;
    private final ManagerOfDatabase managerOfDatabase;

    public ManagerOfFiles(Context context) {
        this.context = context;
        this.managerOfThreads = new ManagerOfThreads(context);
        this.managerOfCache = new ManagerOfCache(context);
        this.managerOfDatabase = new ManagerOfDatabase(context);
    }

    @Override
    public void getStoragesAsync(ModelGetStoragesRequest request, Consumer<ModelGetStoragesResponse> callback) {

        final ModelGetStoragesRequest DEFAULT_REQUEST = new ModelGetStoragesRequest();
        final ModelGetStoragesRequest requestFinal = request == null ? DEFAULT_REQUEST : request;

        Function1<StorageVolume, ModelStorage> getStorageInfo = volume -> {
            String name = null;
            String path = null;
            ModelStorage.Type type = null;
            String description = null;
            Long freeSpace = null;
            Long totalSpace = null;
            Exception error = null;

            try {
                File storageDir = volume.getDirectory();
                path = storageDir.getAbsolutePath();

                name = String.format(context.getResources().getString(R.string.format_name_storage_name), path);

                type = volume.isPrimary()
                        ? ModelStorage.Type.Primary
                        : volume.isRemovable()
                        ? ModelStorage.Type.Removable
                        : ModelStorage.Type.Else;

                description = volume.getDescription(context);



                if (volume.isPrimary()) {
                    UUID uuid = StorageManager.UUID_DEFAULT;
                    StorageStatsManager statsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                    freeSpace = statsManager.getFreeBytes(uuid);
                    totalSpace = statsManager.getTotalBytes(uuid);
                } else {
                    freeSpace = storageDir.getFreeSpace();
                    totalSpace = storageDir.getTotalSpace();
                }
            } catch (Exception e) {
                error = e;
                Log.e(LOG_TAG + "-260804-1", e.getMessage());
            }

            return new ModelStorage(
                    name,
                    path,
                    type,
                    description,
                    freeSpace,
                    totalSpace,
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

        Runnable returnStoragesList = () -> {
            final LocalDateTime startedAt = LocalDateTime.now();

            getStoragesAsync(null, getStoragesResponse -> {
                final List<ModelMediaFile> files = new ArrayList<>();
                final List<ModelStorage> storagesWithErrors = new ArrayList<>();
                final List<ModelMediaFile> filesWithErrors = new ArrayList<>();

                for (ModelStorage storage : getStoragesResponse.storages) {
                    try {
                        File storageRoot = new File(storage.path);
                        String[] storageChildren = storageRoot.list();
                        int childElementsCount = 0;

                        if (storageChildren != null) {
                            childElementsCount = storageChildren.length;
                        }

                        files.add(new ModelMediaFile(
                                storage.description,
                                storage.path,
                                ModelMediaFile.Type.Storage,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                childElementsCount,
                                storage.freeSpace,
                                storage.totalSpace,
                                null
                        ));
                    } catch (Exception e) {
                        Log.e(LOG_TAG + "-260804-3", e.getMessage());
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
                        requestFinal.path,
                        null
                );

                managerOfThreads.safeAccept(callback, getFilesResponse);
            });
        };

        Runnable returnFolderFilesList = () -> {
            final LocalDateTime startedAt = LocalDateTime.now();

            final List<File> files = new ArrayList<>();
            scanFolder(files, new File(requestFinal.path));
            getFilesInfoAsync(files, filesInfo -> {
                final List<ModelMediaFile> filteredFiles = filesInfo.stream().filter(f -> filterCheck(f, requestFinal.filters))
                        .collect(Collectors.toList());

                final List<ModelMediaFile> filesWithErrors = filteredFiles.stream().filter(f -> f.error != null)
                        .collect(Collectors.toList());

                final List<ModelMediaFile> sortedFiles = sortFiles(filteredFiles, requestFinal.sortVariant, requestFinal.foldersFirst);

                ModelGetFilesResponse getFilesResponse = new ModelGetFilesResponse(
                        startedAt,
                        LocalDateTime.now(),
                        new ReadOnlyList<>(sortedFiles),
                        new ReadOnlyList<>(new ArrayList<>()),
                        new ReadOnlyList<>(filesWithErrors),
                        new ReadOnlyList<>(new ArrayList<>()),
                        requestFinal.path,
                        null
                );

                managerOfThreads.safeAccept(callback, getFilesResponse);
            });
        };

        Runnable scanByParams = () -> {
            final LocalDateTime startedAt = LocalDateTime.now();

            getStoragesAsync(null, getStoragesResponse -> {
                final List<File> files = new ArrayList<>();
                final List<ModelStorage> storagesWithErrors = new ArrayList<>();

                for (ModelStorage storage : getStoragesResponse.storages) {
                    if (storage.error == null) {
                        ModelScanParams.StorageParams storageParams = null;

                        if (requestFinal.scanParams != null && requestFinal.scanParams.storagesParams != null) {
                            storageParams = requestFinal.scanParams.storagesParams
                                    .stream()
                                    .filter(sp -> Objects.equals(sp.storageName, storage.name))
                                    .findFirst()
                                    .orElse(null);
                        }

                        Boolean ignoreHidden = requestFinal.filters != null ? requestFinal.filters.ignoreHidden : false;
                        scanStorage(files, new File(storage.path), storageParams, ignoreHidden);
                    } else {
                        storagesWithErrors.add(storage);
                    }
                }

                getFilesInfoAsync(files, filesInfo -> {
                    final List<ModelMediaFile> filteredFiles = filesInfo.stream().filter(f -> filterCheck(f, requestFinal.filters))
                            .collect(Collectors.toList());

                    final List<ModelMediaFile> filesWithErrors = filteredFiles.stream().filter(f -> f.error != null)
                            .collect(Collectors.toList());

                    final List<ModelMediaFile> sortedFiles = sortFiles(filteredFiles, requestFinal.sortVariant, requestFinal.foldersFirst);

                    ModelGetFilesResponse getFilesResponse = new ModelGetFilesResponse(
                            startedAt,
                            LocalDateTime.now(),
                            new ReadOnlyList<>(sortedFiles),
                            getStoragesResponse.storages,
                            new ReadOnlyList<>(filesWithErrors),
                            new ReadOnlyList<>(storagesWithErrors),
                            null,
                            null
                    );

                    managerOfThreads.safeAccept(callback, getFilesResponse);
                });
            });
        };

        Runnable scan = () -> {
            Log.i(LOG_TAG + "-260804-4", "Start scan");

            final LocalDateTime startedAt = LocalDateTime.now();

            try {
                if (requestFinal.path == null) {
                    scanByParams.run();
                } else if (Objects.equals(requestFinal.path, PATH_ROOT)) {
                    returnStoragesList.run();
                } else {
                    returnFolderFilesList.run();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG + "-260804-5", e.getMessage());

                ModelGetFilesResponse getFilesResponse = new ModelGetFilesResponse(
                        startedAt,
                        LocalDateTime.now(),
                        null,
                        null,
                        null,
                        null,
                        requestFinal.path,
                        e
                );

                managerOfThreads.safeAccept(callback, getFilesResponse);
            }

            Log.i(LOG_TAG + "-260804-6", "Finish scan");
        };

        managerOfThreads.executeAsync(scan);
    }

    @Override
    public void getPreviewAsync(ModelGetPreviewRequest request, Consumer<ModelGetPreviewResponse> callback) {

        final Size DEFAULT_TARGET_PREVIEW_RESOLUTION = new Size(300, 300);
        final int VIDEO_PREVIEW_TIMING = 0;

        final ModelGetPreviewRequest DEFAULT_REQUEST = new ModelGetPreviewRequest(null, null, null);
        final ModelGetPreviewRequest requestFinal = request == null ? DEFAULT_REQUEST : request;

        if (requestFinal == DEFAULT_REQUEST) {
            managerOfThreads.safeAccept(callback, new ModelGetPreviewResponse(null, null));
            return;
        }

        Supplier<Size> getCheckedAndMinimizedTargetSize = () -> {
            if (requestFinal.targetWidth != null && requestFinal.targetWidth > 0 && requestFinal.targetHeight != null && requestFinal.targetHeight > 0) {
                if (requestFinal.file.width != null && requestFinal.file.height != null) {
                    double rw = (double) requestFinal.targetWidth;
                    double rh = (double) requestFinal.targetHeight;
                    double fw = (double) requestFinal.file.width;
                    double fh = (double) requestFinal.file.height;
                    if (rw > fw) { double k = fw / rw; rw = k * rw; rh = k * rh; }
                    else if (rh > fh) { double k = fh / rh; rw = k * rw; rh = k * rh; }
                    return new Size((int) rw, (int) rh);
                } else {
                    return new Size(requestFinal.targetWidth, requestFinal.targetHeight);
                }
            }
            return null;
        };

        Size targetPreviewResolution = getCheckedAndMinimizedTargetSize.get();
        final Size targetPreviewResolutionFinal = targetPreviewResolution == null ? DEFAULT_TARGET_PREVIEW_RESOLUTION : targetPreviewResolution;

        ModelGetPreviewResponse cached = managerOfCache.getPreview(request.file, targetPreviewResolutionFinal);
        if (cached != null) {
            managerOfThreads.safeAccept(callback, cached);
            return;
        }

        Function1<ModelMediaFile, Integer> calcInSampleSize = _item -> {

            final int reqWidth = targetPreviewResolutionFinal.getWidth();
            final int reqHeight = targetPreviewResolutionFinal.getHeight();
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
                Log.e(LOG_TAG + "-260804-7", e.getMessage());
                return null;
            }
        };

        Function1<ModelMediaFile, Bitmap> getVideoPreview = _item -> {
            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(_item.path);
                return retriever.getScaledFrameAtTime(
                        VIDEO_PREVIEW_TIMING,
                        android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                        targetPreviewResolutionFinal.getWidth(),
                        targetPreviewResolutionFinal.getHeight());
            } catch (Exception e) {
                Log.e(LOG_TAG + "-260804-8", e.getMessage());
                return null;
            }
        };

        Function1<ModelMediaFile, Drawable> getAnimatedPreview = _item -> {
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(new File(_item.path));
                return ImageDecoder.decodeDrawable(source, (decoder, info, src) -> {
                    decoder.setTargetSize(targetPreviewResolutionFinal.getWidth(), targetPreviewResolutionFinal.getHeight());
                });
            } catch (Exception e) {
                Log.e(LOG_TAG + "-260804-9", e.getMessage());
                return null;
            }
        };

        Runnable loadPreview = () -> {
            Bitmap bitmap = null;
            Drawable drawable = null;

            try {
                if (requestFinal.file.isImage) {
                    bitmap = getImagePreview.invoke(requestFinal.file);
                }
                if (requestFinal.file.isVideo) {
                    bitmap = getVideoPreview.invoke(requestFinal.file);
                }
                if (requestFinal.file.isAnimatedImage) {
                    drawable = getAnimatedPreview.invoke(requestFinal.file);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG + "-260804-10", e.getMessage());
            }

            ModelGetPreviewResponse response = new ModelGetPreviewResponse(
                    bitmap,
                    drawable
            );

            managerOfCache.cachePreview(request.file, targetPreviewResolutionFinal, response);
            managerOfThreads.safeAccept(callback, response);
        };

        managerOfThreads.executeAsync(loadPreview);
    }

    @Override
    public void getPathsAsync(ModelGetPathsRequest request, Consumer<ModelGetPathsResponse> callback) {
        managerOfThreads.executeAsync(() -> {
            if (request.parentPath == null || request.parentPath.isEmpty() || request.parentPath.equals(PATH_ROOT)) {
                getStoragesAsync(null, storages -> {
                    List<String> paths = storages.storages.stream()
                            .map(s -> s.path)
                            .sorted()
                            .collect(Collectors.toList());

                    managerOfThreads.safeAccept(callback, new ModelGetPathsResponse(paths));
                });
                return;
            }

            File parent = new File(request.parentPath);
            if (!parent.exists() || !parent.isDirectory()) {
                managerOfThreads.safeAccept(callback, new ModelGetPathsResponse(null));
                return;
            }

            File[] files = parent.listFiles(file -> (request.includeDirs && file.isDirectory()) || (request.includeFiles && file.isFile()));
            if (files == null || files.length == 0) {
                managerOfThreads.safeAccept(callback, new ModelGetPathsResponse(new ArrayList<>()));
                return;
            }

            List<String> paths = Arrays.stream(files)
                    .map(File::getAbsolutePath)
                    .sorted()
                    .collect(Collectors.toList());

            managerOfThreads.safeAccept(callback, new ModelGetPathsResponse(paths));
        });
    }

    @Override
    public void executeAction(ModelFilesActionRequest request, Consumer<ModelFilesActionResponse> callbackResult, Consumer<ModelRequestProgress> callbackProgress) {
        List<ModelFilesActionResponse.FileInfo> actionFiles = new ArrayList<>();
        List<ModelFilesActionResponse.FileInfo> success = new ArrayList<>();
        List<ModelFilesActionResponse.FileInfo> skipped = new ArrayList<>();
        List<ModelFilesActionResponse.FileInfo> renamed = new ArrayList<>();
        List<ModelFilesActionResponse.FileInfo> replaced = new ArrayList<>();
        List<ModelFilesActionResponse.Fail> fails = new ArrayList<>();

        Supplier<Exception> createFolderIfNotExists = () -> {
            File targetDir = request.targetPath != null ? new File(request.targetPath) : null;

            if (targetDir == null) {
                return new Exception("Path parameter is required for this action");
            }

            boolean targetDirExists = targetDir.exists();
            boolean targetDirIsDirectory = targetDir.isDirectory();
            if (targetDirExists && !targetDirIsDirectory) {
                return new Exception("Path parameter is existing file, not folder");
            }
            if (!targetDirExists) {
                if (!targetDir.mkdirs()) {
                    return new Exception("Failed to create folder with same name. Check name correctness for target file system.");
                }
            }

            return null;
        };

        Runnable collectChildrenForRequestFiles = () -> {
            if (request.files != null && !request.files.isEmpty()) {
                for (ModelMediaFile file : request.files) {
                    if (file.isFolder) {
                        File folder = new File(file.path);
                        collectChildrenFilesInfo(folder, actionFiles);
                    }
                    if (file.isFile) {
                        actionFiles.add(new ModelFilesActionResponse.FileInfo(file.name, file.path, false));
                    }
                    if (file.isStorage) {
                        skipped.add(new ModelFilesActionResponse.FileInfo(file.name, file.path, true));
                    }
                }
            }
        };

        Function1<File, File> createUniqueFile = dest -> {
            if (!dest.exists()) return dest;

            File parent = dest.getParentFile();
            String name = dest.getName();
            String baseName = name;
            String extension = "";

            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = name.substring(0, dotIndex);
                extension = name.substring(dotIndex);
            }

            int count = 1;
            while (dest.exists()) {
                dest = new File(parent, baseName + " (" + count + ")" + extension);
                count++;
            }

            return dest;
        };

        Function1<List<String>, String> findCommonPath = paths -> {
            if (paths == null || paths.isEmpty()) return "";
            if (paths.size() == 1) return paths.get(0);

            Collections.sort(paths);

            String first = paths.get(0);
            String last = paths.get(paths.size() - 1);

            int i = 0;
            while (i < first.length() && i < last.length() && first.charAt(i) == last.charAt(i)) {
                i++;
            }

            String commonPrefix = first.substring(0, i);
            int lastSlash = commonPrefix.lastIndexOf('/');

            return lastSlash == -1 ? "" : commonPrefix.substring(0, lastSlash + 1);
        };

        Runnable deleteAction = () -> {
            int totalFiles = actionFiles.size();
            actionFiles.sort((f1, f2) -> Integer.compare(f2.path.length(), f1.path.length()));

            for (int i = 0; i < totalFiles; i++) {
                ModelFilesActionResponse.FileInfo fileInfo = actionFiles.get(i);
                File source = new File(fileInfo.path);

                managerOfThreads.safeAccept(callbackProgress, new ModelRequestProgress(i + 1, totalFiles, fileInfo.name));

                if (source.delete()) {
                    success.add(fileInfo);
                } else {
                    fails.add(new ModelFilesActionResponse.Fail(fileInfo, new Exception("Fail delete file")));
                }
            }
        };

        Runnable replaceAction = () -> {
            int totalFiles = actionFiles.size();
            actionFiles.sort(Comparator.comparingInt(f -> f.path.length()));

            List<String> allPaths = actionFiles.stream().map(fileInfo -> fileInfo.path).collect(Collectors.toList());
            String commonPath = findCommonPath.invoke(allPaths);

            HashMap<ModelFilesActionResponse.FileInfo, String> newFilePaths = new HashMap<>();
            boolean hasFoldersInActionFiles = actionFiles.stream().map(f -> f.isFolder ? 1 : 0).reduce(0, Integer::sum) != 0;
            for (ModelFilesActionResponse.FileInfo fileInfo : actionFiles) {
                String newFilePath = totalFiles > 1 && hasFoldersInActionFiles
                        ? (request.targetPath + "/" + fileInfo.path.substring(commonPath.length())).replace("//", "/")
                        : (request.targetPath + "/" + fileInfo.name).replace("//", "/");
                newFilePaths.put(fileInfo, newFilePath);
            }

            for (int i = 0; i < totalFiles; i++) {
                ModelFilesActionResponse.FileInfo fileInfo = actionFiles.get(i);
                File source = new File(fileInfo.path);
                File dest = new File(newFilePaths.get(fileInfo));
                boolean destExists = dest.exists();

                managerOfThreads.safeAccept(callbackProgress, new ModelRequestProgress(i + 1, totalFiles, fileInfo.name));

                if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Skip
                        || Objects.equals(source.getAbsolutePath(), dest.getAbsolutePath())
                ) {
                    skipped.add(fileInfo);
                    continue;
                }

                if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Rename) {
                    String oldPath = dest.getAbsolutePath();
                    dest = createUniqueFile.invoke(dest);
                    String newPath = dest.getAbsolutePath();
                    if (fileInfo.isFolder) {
                        for (ModelFilesActionResponse.FileInfo fi : actionFiles) {
                            String newFilePath = newFilePaths.get(fi);
                            if (newFilePath.startsWith(oldPath)) {
                                newFilePaths.put(fi, newPath + "/" + newFilePath.substring(oldPath.length()).replace("//", "/"));
                            }
                        }
                    }
                }

                if (source.isDirectory()) {
                    if (dest.exists()) {
                        List<ModelFilesActionResponse.FileInfo> children = new ArrayList<>();
                        collectChildrenFilesInfo(dest, children);

                        int totalFilesToDelete = children.size();
                        children.sort((f1, f2) -> Integer.compare(f2.path.length(), f1.path.length()));

                        for (int j = 0; j < totalFilesToDelete; j++) {
                            ModelFilesActionResponse.FileInfo fileInfo2 = children.get(j);
                            File source2 = new File(fileInfo2.path);

                            if (!source2.delete()) {
                                fails.add(new ModelFilesActionResponse.Fail(fileInfo2, new Exception("Fail delete file")));
                            }

                            managerOfThreads.safeAccept(callbackProgress, new ModelRequestProgress(j + 1, totalFilesToDelete, fileInfo2.name));
                        }
                    }
                    if (dest.exists() || dest.mkdirs()) {
                        success.add(fileInfo);
                        if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Rename) renamed.add(fileInfo);
                        if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Replace) replaced.add(fileInfo);
                    } else {
                        fails.add(new ModelFilesActionResponse.Fail(fileInfo, new Exception("Fail create folder")));
                    }
                } else {
                    try (InputStream in = Files.newInputStream(source.toPath());
                         OutputStream out = Files.newOutputStream(dest.toPath())
                    ) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                        success.add(fileInfo);
                        if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Rename) renamed.add(fileInfo);
                        if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Replace) replaced.add(fileInfo);
                    } catch (Exception e) {
                        fails.add(new ModelFilesActionResponse.Fail(fileInfo, e));
                    }
                }
            }

            success.sort((f1, f2) -> Integer.compare(f2.path.length(), f1.path.length()));
            int totalFilesToDelete = success.size();

            for (int i = 0; i < totalFilesToDelete; i++) {
                ModelFilesActionResponse.FileInfo fileInfo = success.get(i);
                File source = new File(fileInfo.path);

                managerOfThreads.safeAccept(callbackProgress, new ModelRequestProgress(i + 1, totalFilesToDelete, fileInfo.name));

                if (!source.delete()) {
                    fails.add(new ModelFilesActionResponse.Fail(fileInfo, new Exception("Fail delete file")));
                }
            }
        };

        Runnable copyAction = () -> {
            int totalFiles = actionFiles.size();
            actionFiles.sort(Comparator.comparingInt(f -> f.path.length()));

            List<String> allPaths = actionFiles.stream().map(fileInfo -> fileInfo.path).collect(Collectors.toList());
            String commonPath = findCommonPath.invoke(allPaths);

            HashMap<ModelFilesActionResponse.FileInfo, String> newFilePaths = new HashMap<>();
            boolean hasFoldersInActionFiles = actionFiles.stream().map(f -> f.isFolder ? 1 : 0).reduce(0, Integer::sum) != 0;
            for (ModelFilesActionResponse.FileInfo fileInfo : actionFiles) {
                String newFilePath = totalFiles > 1 && hasFoldersInActionFiles
                        ? (request.targetPath + "/" + fileInfo.path.substring(commonPath.length())).replace("//", "/")
                        : (request.targetPath + "/" + fileInfo.name).replace("//", "/");
                newFilePaths.put(fileInfo, newFilePath);
            }

            for (int i = 0; i < totalFiles; i++) {
                ModelFilesActionResponse.FileInfo fileInfo = actionFiles.get(i);
                File source = new File(fileInfo.path);
                File dest = new File(newFilePaths.get(fileInfo));
                boolean destExists = dest.exists();

                managerOfThreads.safeAccept(callbackProgress, new ModelRequestProgress(i + 1, totalFiles, fileInfo.name));

                if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Skip
                        || Objects.equals(source.getAbsolutePath(), dest.getAbsolutePath())
                ) {
                    skipped.add(fileInfo);
                    continue;
                }

                if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Rename) {
                    String oldPath = dest.getAbsolutePath();
                    dest = createUniqueFile.invoke(dest);
                    String newPath = dest.getAbsolutePath();
                    if (fileInfo.isFolder) {
                        for (ModelFilesActionResponse.FileInfo fi : actionFiles) {
                            String newFilePath = newFilePaths.get(fi);
                            if (newFilePath.startsWith(oldPath)) {
                                newFilePaths.put(fi, newPath + "/" + newFilePath.substring(oldPath.length()).replace("//", "/"));
                            }
                        }
                    }
                }

                if (source.isDirectory()) {
                    if (dest.exists()) {
                        List<ModelFilesActionResponse.FileInfo> children = new ArrayList<>();
                        collectChildrenFilesInfo(dest, children);

                        int totalFilesToDelete = children.size();
                        children.sort((f1, f2) -> Integer.compare(f2.path.length(), f1.path.length()));

                        for (int j = 0; j < totalFilesToDelete; j++) {
                            ModelFilesActionResponse.FileInfo fileInfo2 = children.get(j);
                            File source2 = new File(fileInfo2.path);

                            if (!source2.delete()) {
                                fails.add(new ModelFilesActionResponse.Fail(fileInfo2, new Exception("Fail delete file")));
                            }

                            managerOfThreads.safeAccept(callbackProgress, new ModelRequestProgress(j + 1, totalFilesToDelete, fileInfo2.name));
                        }
                    }
                    if (dest.exists() || dest.mkdirs()) {
                        success.add(fileInfo);
                        if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Rename) renamed.add(fileInfo);
                        if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Replace) replaced.add(fileInfo);
                    } else {
                        fails.add(new ModelFilesActionResponse.Fail(fileInfo, new Exception("Fail create folder")));
                    }
                } else {
                    try (InputStream in = Files.newInputStream(source.toPath());
                         OutputStream out = Files.newOutputStream(dest.toPath())
                    ) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                        success.add(fileInfo);
                        if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Rename) renamed.add(fileInfo);
                        if (destExists && request.duplicateNamePolicy == ModelFilesActionRequest.DuplicateNamePolicy.Replace) replaced.add(fileInfo);
                    } catch (Exception e) {
                        fails.add(new ModelFilesActionResponse.Fail(fileInfo, e));
                    }
                }
            }
        };

        Runnable runAction = () -> {
            Exception globalError = null;

            if (request.action == ModelFilesActionRequest.FilesAction.Delete) {
                collectChildrenForRequestFiles.run();
                deleteAction.run();
            }

            if (request.action == ModelFilesActionRequest.FilesAction.Replace) {
                globalError = createFolderIfNotExists.get();
                if (globalError == null) {
                    collectChildrenForRequestFiles.run();
                    replaceAction.run();
                }
            }

            if (request.action == ModelFilesActionRequest.FilesAction.Copy) {
                globalError = createFolderIfNotExists.get();
                if (globalError == null) {
                    collectChildrenForRequestFiles.run();
                    copyAction.run();
                }
            }

            if (request.action == ModelFilesActionRequest.FilesAction.CreateFolder) {
                globalError = createFolderIfNotExists.get();
                if (globalError == null) {
                    ModelFilesActionResponse.FileInfo folderInfo = new ModelFilesActionResponse.FileInfo(
                            request.targetPath,
                            request.targetPath.substring(request.targetPath.lastIndexOf("/")),
                            true
                    );
                    success.add(folderInfo);
                }
            }

            ModelFilesActionResponse response = new ModelFilesActionResponse(
                    globalError,
                    new ReadOnlyList<>(success),
                    new ReadOnlyList<>(skipped),
                    new ReadOnlyList<>(renamed),
                    new ReadOnlyList<>(replaced),
                    new ReadOnlyList<>(fails)
            );

            managerOfThreads.safeAccept(callbackResult, response);
        };

        managerOfThreads.executeAsync(runAction);
    }

    private void collectChildrenFilesInfo(File folder, List<ModelFilesActionResponse.FileInfo> collectList) {
        File[] children = folder.listFiles();
        collectList.add(new ModelFilesActionResponse.FileInfo(folder.getName(), folder.getAbsolutePath(), true));
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    collectChildrenFilesInfo(child, collectList);
                } else {
                    collectList.add(new ModelFilesActionResponse.FileInfo(child.getName(), child.getAbsolutePath(), false));
                }
            }
        }
    }

    // записывает в files список файлов и папок в заданной папке
    private void scanFolder(List<File> files, File folder) {
        if (folder == null) {
            return;
        }

        File[] folderFiles = folder.listFiles();

        if (folderFiles == null) {
            return;
        }

        managerOfDatabase.actualizeFiles(folder, folderFiles);
        files.addAll(Arrays.asList(folderFiles));
    }

    // записывает в files список файлов из всех вложенных подпапок из заданной папки, соотвествующих scanParams
    private void scanStorage(List<File> files, File folder, ModelScanParams.StorageParams scanParams, Boolean ignoreHidden) {

        final int PATH_IS_NOT_TARGET = 0;
        final int PATH_IS_TARGET = 1;
        final int PATH_IS_TARGET_CHILD = 2;
        final int PATH_IS_TARGET_PARENT = 3;

        Function2<String, ReadOnlyList<String>, Integer> getPathStatus = (path, targetPaths) -> {
            if (path == null || targetPaths == null) {
                return PATH_IS_NOT_TARGET;
            }

            for (String targetPath : targetPaths) {
                targetPath = targetPath.endsWith("/") ? targetPath.substring(0, targetPath.length() - 1) : targetPath;
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

        Function1<Boolean, File[]> getFolderFilesAndActualize = onlyFolders -> {
            File[] folderFiles = folder.listFiles();
            if (folderFiles == null) folderFiles = new File[0];
            managerOfDatabase.actualizeFiles(folder, folderFiles);
            return onlyFolders ? (File[]) Arrays.stream(folderFiles).filter(File::isDirectory).toArray() : folderFiles;
        };

        File[] folderFiles = null;
        int pathStatus = -1;

        if (scanParams != null) {
            pathStatus = getPathStatus.invoke(folder.getAbsolutePath(), scanParams.paths);
            if (scanParams.scanMode == ModelScanParams.ScanMode.ScanAll) {
                folderFiles = getFolderFilesAndActualize.invoke(false);
            } else if (scanParams.scanMode == ModelScanParams.ScanMode.ScanPathsInListOnly) {
                switch (pathStatus) {
                    case PATH_IS_TARGET_PARENT: folderFiles = getFolderFilesAndActualize.invoke(true); break;
                    case PATH_IS_TARGET:
                    case PATH_IS_TARGET_CHILD: folderFiles = getFolderFilesAndActualize.invoke(false); break;
                }
            } else if (scanParams.scanMode == ModelScanParams.ScanMode.ScanPathsNotInListOnly) {
                switch (pathStatus) {
                    case PATH_IS_NOT_TARGET:
                    case PATH_IS_TARGET_PARENT: folderFiles = getFolderFilesAndActualize.invoke(false); break;
                }
            }
        } else {
            folderFiles = getFolderFilesAndActualize.invoke(false);
        }

        if (folderFiles == null) {
            return;
        }

        for (File file : folderFiles) {
            if (!(ignoreHidden == true && file.isHidden())) {
                if (file.isDirectory()) {
                    scanStorage(files, file, scanParams, ignoreHidden);
                } else {
                    files.add(file);
                }
            }
        }
    }

    private void getFilesInfoAsync(List<File> files, Consumer<List<ModelMediaFile>> callback) {
        if (files == null || files.isEmpty()) {
            managerOfThreads.safeAccept(callback, new ArrayList<>());
            return;
        }

        List<Supplier<ModelMediaFile>> tasks = files.stream().map(file -> {
            return new Supplier<ModelMediaFile>() {
                @Override
                public ModelMediaFile get() {
                    return getFileInfo(file);
                }
            };
        }).collect(Collectors.toList());

        managerOfThreads.executeAsync(tasks, resultList -> {
            List<ModelMediaFile> clearList = resultList.stream().filter(Objects::nonNull).collect(Collectors.toList());
            managerOfThreads.safeAccept(callback, clearList);
        });
    }

    private ModelMediaFile getFileInfo(File file) {

        ModelMediaFile cached = managerOfCache.getFileInfo(file);
        if (cached != null) {
            return cached;
        }

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

        Function2<File, ModelMediaFile.Type, LocalDateTime> getFileCreationTime = (_file, type) -> {
            try {
                if (type == ModelMediaFile.Type.Image) {
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
                Log.e(LOG_TAG  + "-260804-11", e.getMessage());
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
                Log.e(LOG_TAG + "-260804-12", e.getMessage());
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
                Log.e(LOG_TAG + "-260804-13", e.getMessage());
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
                duration = Integer.parseInt(durationStr);
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
        Boolean isHidden = null;
        Long weight = null;
        Integer width = null;
        Integer height = null;
        Integer rotation = null;
        String extension = null;
        Integer duration = null;
        Integer childElementsCount = null;
        Exception error = null;

        try {
            name = file.getName();
            Boolean isDirectory = file.isDirectory();
            type = getFileType.invoke(name, isDirectory);

            if (type == null) return null;

            path = file.getAbsolutePath();
            createAt = getFileCreationTime.invoke(file, type);
            updateAt = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            isHidden = file.isHidden();

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

            if (type == ModelMediaFile.Type.Folder) {
                File folder = new File(path);
                String[] folderFiles = folder.list();
                if (folderFiles != null) {
                    childElementsCount = folderFiles.length;
                }
            }
        } catch (Exception e) {
            error = e;
            Log.e(LOG_TAG + "-260804-14", e.getMessage());
        }

        ModelMediaFile fileInfo = new ModelMediaFile(
                name,
                path,
                type,
                createAt,
                updateAt,
                isHidden,
                weight,
                width,
                height,
                rotation,
                extension,
                duration,
                childElementsCount,
                null,
                null,
                error
        );

        managerOfCache.cacheFileInfo(fileInfo);

        return fileInfo;
    }

    private boolean filterCheck(ModelMediaFile model, ModelFilters filters) {
        if (model == null) {
            return false;
        }

        if (filters != null) {
            if (model.isFile) {
                if (filters.ignoreHidden && model.isHidden) {
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
            if (model.isFolder) {
                if (filters.ignoreHidden && model.isHidden) {
                    return false;
                }
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
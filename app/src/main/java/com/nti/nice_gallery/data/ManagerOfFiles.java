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

import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelStorage;

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
    public List<ModelMediaFile> getAllFiles() {
        List<ModelMediaFile> mediaList = new ArrayList<>();

        // Существующий код для внутреннего хранилища и SD-карты
        mediaList.addAll(getMediaFromMediaStore(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ModelMediaFile.Type.Image));

        mediaList.addAll(getMediaFromMediaStore(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                ModelMediaFile.Type.Video));

        // Новый код для USB флешки
//        mediaList.addAll(getMediaFromExternalVolumes(ModelMediaTreeItem.Type.Image));
//        mediaList.addAll(getMediaFromExternalVolumes(ModelMediaTreeItem.Type.Video));

        // Дополнительно: сканируем USB напрямую через файловую систему
        mediaList.addAll(scanUsbStorageDirectly());

        return mediaList;
    }

    private List<ModelMediaFile> scanUsbStorageDirectly() {
        List<ModelMediaFile> mediaList = new ArrayList<>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Для Android 11+ с MANAGE_EXTERNAL_STORAGE
            if (!android.provider.Settings.canDrawOverlays(context)) {
                // Проверяем, есть ли разрешение на полный доступ
                try {
                    if (!android.os.Environment.isExternalStorageManager()) {
                        Log.w(getClass().getName(), "No MANAGE_EXTERNAL_STORAGE permission");
                        return mediaList;
                    }
                } catch (Exception e) {
                    Log.e(getClass().getName(), "Error checking storage permission: " + e.getMessage());
                    return mediaList;
                }
            }
        }

        // Ищем USB устройства
        List<java.io.File> usbRoots = findUsbStorageRoots();

        for (java.io.File usbRoot : usbRoots) {
            Log.d(getClass().getName(), "Scanning USB root: " + usbRoot.getAbsolutePath());
            scanDirectoryForMediaFiles(usbRoot, mediaList);
        }

        return mediaList;
    }

    private boolean isUsbStorage(String path) {
        // Проверяем по ключевым словам в пути
        String lowerPath = path.toLowerCase();
        return lowerPath.contains("usb") ||
                lowerPath.contains("otg") ||
                lowerPath.contains("udisk") ||
                lowerPath.contains("media_rw");
    }

    private List<java.io.File> findUsbStorageRoots() {
        List<java.io.File> usbRoots = new ArrayList<>();

        try {
            // Способ 1: Получаем все точки монтирования через getExternalFilesDirs
            java.io.File[] externalDirs = context.getExternalFilesDirs(null);

            for (int i = 1; i < externalDirs.length; i++) { // Начинаем с 1, т.к. 0 - внутреннее хранилище
                java.io.File dir = externalDirs[i];
                if (dir != null) {
                    // Получаем корневой каталог хранилища
                    java.io.File storageRoot = getStorageRootFromAppDir(dir);
                    if (storageRoot != null && storageRoot.exists() && isUsbStorage(storageRoot.getPath())) {
                        usbRoots.add(storageRoot);
                    }
                }
            }

            // Способ 2: Проверяем известные пути монтирования USB
            String[] knownUsbPaths = {
                    "/storage/usb",
                    "/mnt/usb",
                    "/storage/usb0",
                    "/storage/usb1",
                    "/storage/usbotg",
                    "/mnt/usbotg",
                    "/storage/udisk",
                    "/mnt/udisk",
                    "/storage/USB",
                    "/mnt/USB",
                    "/storage/USB0",
                    "/storage/USB1"
            };

            for (String path : knownUsbPaths) {
                java.io.File usbDir = new java.io.File(path);
                if (usbDir.exists() && usbDir.canRead() && usbDir.isDirectory()) {
                    usbRoots.add(usbDir);
                }
            }

            // Способ 3: Сканируем /storage и /mnt
            scanForStorageInDirectory(new java.io.File("/storage"), usbRoots);
            scanForStorageInDirectory(new java.io.File("/mnt"), usbRoots);

        } catch (Exception e) {
            Log.e(getClass().getName(), "Error finding USB roots: " + e.getMessage());
        }

        // Убираем дубликаты
        return new ArrayList<>(new java.util.LinkedHashSet<>(usbRoots));
    }

    private void scanForStorageInDirectory(java.io.File dir, List<java.io.File> storageList) {
        if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
            return;
        }

        try {
            java.io.File[] files = dir.listFiles();
            if (files == null) return;

            for (java.io.File file : files) {
                if (file.isDirectory() && file.canRead()) {
                    String name = file.getName();

                    // Пропускаем системные директории
                    if (name.startsWith(".") ||
                            "emulated".equals(name) ||
                            "self".equals(name) ||
                            "android".equals(name)) {
                        continue;
                    }

                    // Проверяем, похоже ли это на USB хранилище
                    if (isLikelyUsbStorage(file)) {
                        storageList.add(file);
                    }

                    // Также проверяем поддиректории типа /storage/XXXX-XXXX
                    if (name.matches("[0-9A-F]{4}-[0-9A-F]{4}")) { // Паттерн типа 1234-ABCD
                        storageList.add(file);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Error scanning directory: " + e.getMessage());
        }
    }

    private boolean isLikelyUsbStorage(java.io.File dir) {
        String path = dir.getAbsolutePath().toLowerCase();
        String name = dir.getName().toLowerCase();

        // Проверяем по ключевым словам
        boolean hasUsbKeyword = path.contains("usb") || name.contains("usb") ||
                path.contains("otg") || name.contains("otg") ||
                path.contains("udisk") || name.contains("udisk");

        // Проверяем, есть ли типичные папки в хранилище
        if (!hasUsbKeyword) {
            java.io.File[] subDirs = dir.listFiles();
            if (subDirs != null) {
                for (java.io.File subDir : subDirs) {
                    String subName = subDir.getName().toLowerCase();
                    if (subName.equals("dcim") || subName.equals("pictures") ||
                            subName.equals("movies") || subName.equals("download")) {
                        return true;
                    }
                }
            }
        }

        return hasUsbKeyword;
    }

    private java.io.File getStorageRootFromAppDir(java.io.File appDir) {
        try {
            String path = appDir.getAbsolutePath();

            // Паттерн: /storage/XXXX-XXXX/Android/data/package/files
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(/storage/[^/]+)/Android/data/");
            java.util.regex.Matcher matcher = pattern.matcher(path);

            if (matcher.find()) {
                return new java.io.File(matcher.group(1));
            }

            // Альтернативный путь
            pattern = java.util.regex.Pattern.compile("(/mnt/[^/]+)/Android/data/");
            matcher = pattern.matcher(path);
            if (matcher.find()) {
                return new java.io.File(matcher.group(1));
            }

            // Простой способ: поднимаемся на 3 уровня вверх
            java.io.File current = appDir;
            for (int i = 0; i < 3 && current != null; current = current.getParentFile(), i++);

            return current;

        } catch (Exception e) {
            return null;
        }
    }

    private void scanDirectoryForMediaFiles(java.io.File dir, List<ModelMediaFile> mediaList) {
        if (!dir.exists() || !dir.canRead()) {
            return;
        }

        // Ограничиваем сканирование определенными папками для производительности
        String[] mediaFolders = {"DCIM", "Pictures", "Photos", "Camera", "Movies", "Videos", "Download"};

        for (String folder : mediaFolders) {
            java.io.File mediaDir = new java.io.File(dir, folder);
            if (mediaDir.exists() && mediaDir.isDirectory()) {
                scanDirectoryRecursiveForMedia(mediaDir, mediaList, 0);
            }
        }

        // Также сканируем корень, но с меньшей глубиной
        scanDirectoryRecursiveForMedia(dir, mediaList, 2);
    }

    private void scanDirectoryRecursiveForMedia(java.io.File dir, List<ModelMediaFile> mediaList, int depth) {
        if (depth > 5 || dir == null || !dir.exists() || !dir.canRead()) {
            return;
        }

        try {
            java.io.File[] files = dir.listFiles();
            if (files == null) return;

            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    // Пропускаем скрытые и системные папки
                    String name = file.getName();
                    if (!name.startsWith(".") && !name.equals("Android") && !name.equals("Lost+Found")) {
                        scanDirectoryRecursiveForMedia(file, mediaList, depth + 1);
                    }
                } else if (file.isFile()) {
                    // Проверяем и добавляем медиафайл
                    ModelMediaFile mediaItem = createMediaItemFromFile(file);
                    if (mediaItem != null) {
                        mediaList.add(mediaItem);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Error scanning directory: " + e.getMessage());
        }
    }

    private ModelMediaFile createMediaItemFromFile(java.io.File file) {
        try {
            String name = file.getName();
            String path = file.getAbsolutePath();
            long size = file.length();
            Date createAt = new Date(file.lastModified());
            Date updateAt = new Date(file.lastModified());

            // Определяем тип файла по расширению
            ModelMediaFile.Type type = getMediaTypeFromFile(file);
            if (type == null) {
                return null;
            }

            String extension = "";
            int lastDot = name.lastIndexOf('.');
            if (lastDot > 0) {
                extension = name.substring(lastDot + 1);
            }

            // Получаем размеры для изображений (для видео это сложнее без декодирования)
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeFile(path, options);

            android.util.Size resolution = new android.util.Size(options.outWidth, options.outHeight);

            return new ModelMediaFile(
                    -1L, // ID для файлов, найденных напрямую
                    name,
                    path,
                    type,
                    size,
                    createAt,
                    updateAt,
                    resolution,
                    extension,
                    null // duration - для видео нужно отдельно определять
            );

        } catch (Exception e) {
            Log.e(getClass().getName(), "Error creating media item from file: " + e.getMessage());
            return null;
        }
    }

    private ModelMediaFile.Type getMediaTypeFromFile(java.io.File file) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp") || name.endsWith(".webp")) {
            return ModelMediaFile.Type.Image;
        }

        if (name.endsWith(".mp4") || name.endsWith(".avi") ||
                name.endsWith(".mkv") || name.endsWith(".mov") ||
                name.endsWith(".wmv") || name.endsWith(".flv") ||
                name.endsWith(".3gp") || name.endsWith(".m4v")) {
            return ModelMediaFile.Type.Video;
        }

        return null;
    }

    @Override
    public Bitmap getFilePreview(ModelMediaFile item) {
        return getItemThumbnailFromMediaStore(item);
    }

    @Override
    public List<ModelStorage> getAllStorages() {
        return Collections.emptyList();
    }

    @SuppressLint("Range")
    private List<ModelMediaFile> getMediaFromMediaStore(Uri contentUri, ModelMediaFile.Type type) {

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

        String[] projection = type == ModelMediaFile.Type.Image ? imageProjection : videoProjection;
        String sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";

        List<ModelMediaFile> mediaList = new ArrayList<>();

        Function1<String, Boolean> isSupportedFormat = mimeType ->
                Arrays.stream(ModelMediaFile.supportedMediaFormats)
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
            if (type == ModelMediaFile.Type.Video) {
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
                    if (type == ModelMediaFile.Type.Video && durationColumn != -1) {
                        duration = (int) (cursor.getLong(durationColumn) / 1000);
                    }

                    mediaList.add(new ModelMediaFile(
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
                    ));
                } catch (Exception e) {
                    Log.e(getClass().getName(), "Failed to read file properties: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        } catch (Exception e) {
            Log.e(getClass().getName(), "Failed to read files: " + e.getMessage());
        }

        return mediaList;
    }

    private Bitmap getItemThumbnailFromMediaStore(ModelMediaFile item) {
        try {
            Uri contentUri = (item.type == ModelMediaFile.Type.Image)
                    ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    : MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            return contentResolver.loadThumbnail(
                    ContentUris.withAppendedId(contentUri, item.mediaId),
                    THUMBNAIL_TARGET_SIZE,
                    null
            );
        } catch (Exception e) {
            Log.e(getClass().getName(), "Failed to get thumbnail: " + e.getMessage());
            return null;
        }
    }

}
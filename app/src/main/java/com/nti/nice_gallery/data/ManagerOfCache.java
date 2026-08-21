package com.nti.nice_gallery.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Size;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelGetPreviewResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.Convert;
import com.nti.nice_gallery.utils.ManagerOfThreads;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ManagerOfCache {

    private static HashMap<String, ModelMediaFile> filesInfoCache = new HashMap<>();
    private static final Map<String, PreviewCacheList> previewsCache = new LinkedHashMap<>();
    private static final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();

    private static final long MAX_CACHE_SIZE_BYTES = 100 * 1024 * 1024;
    private static long currentPreviewCacheWeightBytes = 0;
    private static IManagerOfSettings.TxtFile filesCacheTxt;

    private final Context context;
    private IManagerOfSettings managerOfSettings;
    private ManagerOfThreads managerOfThreads;

    public ManagerOfCache(Context context) {
        this.context = context;
    }

    public void initManagerOfSettings() {
        if (managerOfSettings == null) {
            managerOfSettings = Domain.getManagerOfSettings(context);
        }
    }

    public void initManagerOfThreads() {
        if (managerOfThreads == null) {
            managerOfThreads = new ManagerOfThreads(context);
        }
    }

    public void clearFilesInfoCache() {
        cacheLock.writeLock().lock();
        initManagerOfSettings();
        try {
            filesInfoCache.clear();
            filesCacheTxt = managerOfSettings.saveTxt(IManagerOfSettings.CACHE_FILES_INFO_TXT, null);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public void clearPreviewCache() {
        cacheLock.writeLock().lock();
        try {
            previewsCache.clear();
            currentPreviewCacheWeightBytes = 0;
            System.gc();
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public void storeFilesInfoCache() {
        initManagerOfSettings();

        ModelMediaFile[] filesInfo = filesInfoCache.values().toArray(new ModelMediaFile[0]);
        String[] filesInfoStr = new String[filesInfo.length];

        for (int i = 0; i < filesInfo.length; i++) {
            filesInfoStr[i] = filesInfo[i].toJson();
        }

        filesCacheTxt = managerOfSettings.saveTxt(IManagerOfSettings.CACHE_FILES_INFO_TXT, filesInfoStr);
    }

    public void restoreFilesInfoCache() {
        initManagerOfSettings();
        initManagerOfThreads();

        managerOfThreads.executeAsync(() -> {
            HashMap<String, ModelMediaFile> cache = new HashMap<>();
            IManagerOfSettings.TxtFile file = managerOfSettings.readTxt(IManagerOfSettings.CACHE_FILES_INFO_TXT);

            if (file != null && file.strings != null && file.strings.length > 0) {
                for (int i = 0; i < file.strings.length; i++) {
                    ModelMediaFile fileInfo = new ModelMediaFile(file.strings[i]);
                    cache.put(fileInfo.path, fileInfo);
                }
            }

            cacheLock.writeLock().lock();
            cache.putAll(filesInfoCache);
            filesInfoCache = cache;
            filesCacheTxt = file;
            cacheLock.writeLock().unlock();
        });
    }

    public String getFilesCacheInfo() {
        String cachedFilesCount = String.valueOf(filesInfoCache.size());
        String storedFilesCount = filesCacheTxt != null && filesCacheTxt.strings != null ? String.valueOf(filesCacheTxt.strings.length) : "0";
        return context.getString(R.string.format_info_files_cache, cachedFilesCount, storedFilesCount);
    }

    public String getPreviewsCacheInfo() {
        Convert convert = new Convert(context);
        String cachedWeightStr = convert.weightToString(currentPreviewCacheWeightBytes);
        return context.getString(R.string.format_info_previews_cache, String.valueOf(previewsCache.size()), cachedWeightStr);
    }

    public IManagerOfSettings.TxtFile getFilesCacheTxt() {
        return filesCacheTxt;
    }

    public void cacheFileInfo(ModelMediaFile fileInfo) {
        if (fileInfo == null) return;

        cacheLock.writeLock().lock();
        try {
            filesInfoCache.put(fileInfo.path, fileInfo);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Nullable
    public ModelMediaFile getFileInfo(File file) {
        if (file == null) return null;

        String absolutPath = file.getAbsolutePath();
        LocalDateTime lastUpdate = Instant.ofEpochMilli(file.lastModified())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        cacheLock.readLock().lock();
        try {
            ModelMediaFile cachedInfo = filesInfoCache.get(absolutPath);
            if (cachedInfo != null && lastUpdate.isEqual(cachedInfo.updatedAt)) {
                return cachedInfo;
            }
        } finally {
            cacheLock.readLock().unlock();
        }

        return null;
    }

    public void cachePreview(ModelMediaFile fileInfo, Size targetSize, ModelGetPreviewResponse response) {
        if (fileInfo == null || targetSize == null || response == null) return;
        if (response.previewBitmap == null && response.previewDrawable == null) return;

        cacheLock.writeLock().lock();
        try {
            PreviewCacheList cacheList = previewsCache.get(fileInfo.path);
            if (cacheList == null) {
                cacheList = new PreviewCacheList(fileInfo);
                previewsCache.put(fileInfo.path, cacheList);
            }

            long oldWeight = cacheList.getListWeight();
            cacheList.addItem(targetSize, response);
            long newWeight = cacheList.getListWeight();

            currentPreviewCacheWeightBytes += (newWeight - oldWeight);

            enforceMemoryLimit();
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Nullable
    public ModelGetPreviewResponse getPreview(ModelMediaFile fileInfo, Size targetSize) {
        if (fileInfo == null || targetSize == null) return null;

        cacheLock.readLock().lock();
        try {
            PreviewCacheList cacheList = previewsCache.get(fileInfo.path);
            if (cacheList == null) return null;

            PreviewCacheItem cacheItem = cacheList.findClosestItem(targetSize);
            if (cacheItem != null) {
                if (cacheItem.bitmap != null && cacheItem.bitmap.isRecycled() || cacheItem.bitmap == null && cacheItem.drawable == null) {
                    return null;
                }
                return new ModelGetPreviewResponse(cacheItem.bitmap, cacheItem.drawable);
            }
        } finally {
            cacheLock.readLock().unlock();
        }

        return null;
    }

    private void enforceMemoryLimit() {
        while (currentPreviewCacheWeightBytes > MAX_CACHE_SIZE_BYTES && !previewsCache.isEmpty()) {
            String oldestKey = previewsCache.keySet().iterator().next();
            PreviewCacheList oldestList = previewsCache.remove(oldestKey);
            if (oldestList != null) {
                currentPreviewCacheWeightBytes -= oldestList.getListWeight();
            }
        }
    }

    private static class PreviewCacheList {
        public final ModelMediaFile fileInfo;
        private final List<PreviewCacheItem> items;
        private long listWeight = 0;

        public PreviewCacheList(ModelMediaFile fileInfo) {
            this.fileInfo = fileInfo;
            this.items = new ArrayList<>();
        }

        public void addItem(Size targetSize, ModelGetPreviewResponse response) {
            final int MAX_ITEMS_COUNT = 3;

            PreviewCacheItem oldItem = findItemBySize(targetSize);
            if (oldItem != null) {
                removeItem(oldItem);
            }

            if (items.size() >= MAX_ITEMS_COUNT) {
                removeItem(items.get(0));
            }

            PreviewCacheItem newItem = new PreviewCacheItem(this, targetSize, response);
            items.add(newItem);
            listWeight += newItem.calcWeight();
        }

        public void removeItem(int index) {
            if (index >= 0) {
                PreviewCacheItem removed = items.remove(index);
                if (removed != null) {
                    listWeight -= removed.calcWeight();
                }
            }
        }

        public void removeItem(PreviewCacheItem item) {
            int index = items.indexOf(item);
            removeItem(index);
        }

        public PreviewCacheItem findItemBySize(Size targetSize) {
            for (PreviewCacheItem item : items) {
                if (item.targetSize.getWidth() == targetSize.getWidth() && item.targetSize.getHeight() == targetSize.getHeight()) {
                    return item;
                }
            }
            return null;
        }

        public PreviewCacheItem findClosestItem(Size targetSize) {
            final float EQUAL_OR_NOT_MORE_THEN_PERCENT_BIGGER = 0.20f;

            if (items.isEmpty()) {
                return null;
            }

            PreviewCacheItem closestItem = null;
            float closestDiff = Float.MAX_VALUE;

            for (PreviewCacheItem item : items) {
                if (fileInfo.width != null && fileInfo.width <= targetSize.getWidth() && fileInfo.width <= item.targetSize.getWidth()
                        && fileInfo.height != null && fileInfo.height <= targetSize.getHeight() && fileInfo.height <= item.targetSize.getHeight()
                ) {
                    closestItem = item;
                    break;
                }

                float widthDiff = ((float) item.targetSize.getWidth() - targetSize.getWidth()) / targetSize.getWidth();
                float heightDiff = ((float) item.targetSize.getHeight() - targetSize.getHeight()) / targetSize.getHeight();
                float totalDiff = widthDiff + heightDiff;

                if (totalDiff >= 0 && totalDiff <= EQUAL_OR_NOT_MORE_THEN_PERCENT_BIGGER * 2) {
                    if (totalDiff < closestDiff) {
                        closestDiff = totalDiff;
                        closestItem = item;
                    }
                }
            }

            return closestItem;
        }

        public long getListWeight() {
            return listWeight;
        }
    }

    private static class PreviewCacheItem {
        public final PreviewCacheList parentList;
        public final Size targetSize;
        public final Bitmap bitmap;
        public final Drawable drawable;

        public PreviewCacheItem(PreviewCacheList parentList, Size targetSize, ModelGetPreviewResponse response) {
            this.parentList = parentList;
            this.targetSize = targetSize;
            this.bitmap = response.previewBitmap;

            if (response.previewDrawable != null) {
                Drawable.ConstantState state = response.previewDrawable.getConstantState();
                if (state != null) this.drawable = state.newDrawable();
                else this.drawable = null;
            } else {
                this.drawable = null;
            }
        }

        public long calcWeight() {
            long weight = 0;

            if (bitmap != null && !bitmap.isRecycled()) {
                weight += bitmap.getAllocationByteCount();
            }
            if (parentList != null && parentList.fileInfo != null && parentList.fileInfo.weight != null) {
                weight += parentList.fileInfo.weight;
            }

            return weight;
        }
    }
}
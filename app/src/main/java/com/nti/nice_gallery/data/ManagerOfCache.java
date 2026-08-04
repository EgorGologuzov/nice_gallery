package com.nti.nice_gallery.data;

import android.content.Context;
import android.util.Size;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.models.ModelGetPreviewResponse;
import com.nti.nice_gallery.models.ModelMediaFile;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManagerOfCache {

    private static final HashMap<String, ModelMediaFile> filesInfoCache = new HashMap<>();
    private static final Map<String, PreviewCacheList> previewsCache = new LinkedHashMap<>();;

    private final Context context;

    public ManagerOfCache(Context context) {
        this.context = context;
    }

    public void cacheFileInfo(ModelMediaFile fileInfo) {
        filesInfoCache.put(fileInfo.path, fileInfo);
    }

    @Nullable
    public ModelMediaFile getFileInfo(File file) {
        String absolutPath = file.getAbsolutePath();
        LocalDateTime lastUpdate = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (filesInfoCache.containsKey(absolutPath)) {
            ModelMediaFile cachedInfo = filesInfoCache.get(absolutPath);
            if (cachedInfo != null && lastUpdate.isEqual(cachedInfo.updatedAt)) {
                return cachedInfo;
            }
        }

        return null;
    }

    public void cachePreview(ModelMediaFile fileInfo, Size targetSize, ModelGetPreviewResponse response) {
        final int MAX_LISTS_COUNT = 100;

        if (previewsCache.size() >= MAX_LISTS_COUNT) {
            String oldestKey = previewsCache.keySet().iterator().next();
            previewsCache.remove(oldestKey);
        }

        PreviewCacheList cacheList = previewsCache.get(fileInfo.path);
        if (cacheList == null) {
            cacheList = new PreviewCacheList(fileInfo);
            previewsCache.put(fileInfo.path, cacheList);
        }

        cacheList.addItem(targetSize, response);
    }

    @Nullable
    public ModelGetPreviewResponse getPreview(ModelMediaFile fileInfo, Size targetSize) {
        if (previewsCache.containsKey(fileInfo.path)) {
            PreviewCacheList cacheList = previewsCache.get(fileInfo.path);
            ModelGetPreviewResponse cachedResponse = cacheList.getClosestItem(targetSize);
            if (cachedResponse != null) {
                return cachedResponse;
            }
        }

        return null;
    }

    private static class PreviewCacheList {
        public final ModelMediaFile fileInfo;
        private final List<PreviewCacheItem> items;

        public PreviewCacheList(ModelMediaFile fileInfo) {
            this.fileInfo = fileInfo;
            this.items = new ArrayList<>();
        }

        public void addItem(Size targetSize, ModelGetPreviewResponse response) {
            final int MAX_ITEMS_COUNT = 3;

            for (PreviewCacheItem item : items) {
                if (item.targetSize.getWidth() == targetSize.getWidth() &&
                        item.targetSize.getHeight() == targetSize.getHeight()) {
                    item.response = response;
                    return;
                }
            }

            if (items.size() >= MAX_ITEMS_COUNT) {
                items.remove(0);
            }

            items.add(new PreviewCacheItem(targetSize, response));
        }

        public ModelGetPreviewResponse getClosestItem(Size targetSize) {
            final float EQUAL_OR_NOT_MORE_THEN_PERCENT_BIGGER = 0.2f;

            if (items.isEmpty()) {
                return null;
            }

            PreviewCacheItem closestItem = null;
            float closestDiff = Float.MAX_VALUE;

            for (PreviewCacheItem item : items) {
                float widthDiff = ((float) item.targetSize.getWidth() - targetSize.getWidth()) / targetSize.getWidth();
                float heightDiff = ((float) item.targetSize.getHeight() - targetSize.getHeight()) / targetSize.getHeight();
                float totalDiff = widthDiff + heightDiff;

                // Проверяем, подходит ли размер (не больше указанного процента)
                if (totalDiff >= 0 && totalDiff <= EQUAL_OR_NOT_MORE_THEN_PERCENT_BIGGER * 2) {
                    if (totalDiff < closestDiff) {
                        closestDiff = totalDiff;
                        closestItem = item;
                    }
                }
            }

            return closestItem != null ? closestItem.response : null;
        }
    }

    private static class PreviewCacheItem {
        public final Size targetSize;
        public ModelGetPreviewResponse response;

        public PreviewCacheItem(Size targetSize, ModelGetPreviewResponse response) {
            this.targetSize = targetSize;
            this.response = response;
        }
    }
}

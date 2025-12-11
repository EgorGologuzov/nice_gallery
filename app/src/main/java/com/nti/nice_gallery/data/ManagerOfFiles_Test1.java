package com.nti.nice_gallery.data;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Size;

import androidx.core.content.ContextCompat;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelMediaTreeItem;
import com.nti.nice_gallery.models.ModelStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;

// Менеджер файлов для теста интерфейса
public class ManagerOfFiles_Test1 implements IManagerOfFiles {

    private final Context context;
    private final ContentResolver contentResolver;

    public ManagerOfFiles_Test1(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    // Возвращает спсиок случайно сгенерированных файлов
    @Override
    public List<ModelMediaTreeItem> getAllFiles() {

        final int RANDOM_SEED = 42;
        final int ITEMS_COUNT = 100;
        final int MAX_NUMBER_OF_DAYS_AGO = 30;
        final int MAX_IMAGE_WEIGHT = 10_000_000;
        final int MAX_VIDEO_WEIGHT = 2_000_000_000;
        final int MAX_FOLDER_WEIGHT = 2_000_000_000;
        final int MAX_VIDEO_DURATION = 7_200;
        final int SHARE_OF_IMAGES = 50;
        final int SHARE_OF_VIDEOS = 30;
        final int SHARE_OF_FOLDERS = 20;
        final int SHARE_OF_1920x1080_FILES = 50;
        final int SHARE_OF_1080x1920_FILES = 50;

        List<ModelMediaTreeItem> items = new ArrayList<>();
        Random random = new Random(RANDOM_SEED);

        Supplier<Date> randomDate = () -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -random.nextInt(MAX_NUMBER_OF_DAYS_AGO));
            cal.add(Calendar.HOUR_OF_DAY, -random.nextInt(24));
            cal.add(Calendar.MINUTE, -random.nextInt(60));
            return cal.getTime();
        };

        Supplier<ModelMediaTreeItem.Type> randomType = () -> {
            float randomFloat = random.nextFloat();
            int sumShare = SHARE_OF_IMAGES + SHARE_OF_VIDEOS + SHARE_OF_FOLDERS;
            float imagesPercent = (float)SHARE_OF_IMAGES / (float)sumShare;
            float videosPercent = (float)SHARE_OF_VIDEOS / (float)sumShare;
            float foldersPercent = (float)SHARE_OF_FOLDERS / (float)sumShare;

            float currentPercent = imagesPercent;
            if (randomFloat <= currentPercent) {
                return ModelMediaTreeItem.Type.Image;
            }

            currentPercent += videosPercent;
            if (randomFloat <= currentPercent) {
                return ModelMediaTreeItem.Type.Video;
            }

            return ModelMediaTreeItem.Type.Folder;
        };

        Function1<ModelMediaTreeItem.Type, String> randomExtension = (type) -> {
            if (type == ModelMediaTreeItem.Type.Folder) {
                return null;
            }

            String[] imageExtensions = Arrays.stream(ModelMediaTreeItem.supportedMediaFormats)
                    .filter(f -> f.type == ModelMediaTreeItem.Type.Image)
                    .map(f -> f.fileExtension)
                    .toArray(String[]::new);

            String[] videoExtensions = Arrays.stream(ModelMediaTreeItem.supportedMediaFormats)
                    .filter(f -> f.type == ModelMediaTreeItem.Type.Video)
                    .map(f -> f.fileExtension)
                    .toArray(String[]::new);

            if (type == ModelMediaTreeItem.Type.Image) {
                return imageExtensions[random.nextInt(imageExtensions.length)];
            }

            return videoExtensions[random.nextInt(videoExtensions.length)];
        };

        Function3<ModelMediaTreeItem.Type, Integer, String, String> randomName = (type, i, extension) -> {
            return type == ModelMediaTreeItem.Type.Folder ? "folder_" + i : "file_" + i + "." + extension;
        };

        Function1<String, String> randomPath = (fileName) -> {
            return "internal_storage/DCIM/" + fileName;
        };

        Function1<ModelMediaTreeItem.Type, Long> randomWeight = (type) -> {
            if (type == ModelMediaTreeItem.Type.Image) {
                return (long)random.nextInt(MAX_IMAGE_WEIGHT);
            }
            if (type == ModelMediaTreeItem.Type.Video) {
                return (long)random.nextInt(MAX_VIDEO_WEIGHT);
            }

            return (long)random.nextInt(MAX_FOLDER_WEIGHT);
        };

        Function1<ModelMediaTreeItem.Type, Size> randomSize = (type) -> {
            if (type == ModelMediaTreeItem.Type.Folder) {
                return null;
            }

            float randomFloat = random.nextFloat();
            int sumShare = SHARE_OF_1920x1080_FILES + SHARE_OF_1080x1920_FILES;
            float files1920x1080Percent = (float)SHARE_OF_1920x1080_FILES / (float)sumShare;
            float files1080x1920Percent = (float)SHARE_OF_1080x1920_FILES / (float)sumShare;

            float currentPercent = files1920x1080Percent;
            if (randomFloat <= currentPercent) {
                return new Size(1920, 1080);
            }

            return new Size(1080, 1920);
        };

        Function1<ModelMediaTreeItem.Type, Integer> randomDuration = (type) -> {
            return type == ModelMediaTreeItem.Type.Video ? random.nextInt(MAX_VIDEO_DURATION) : null;
        };

        for (int i = 0; i < ITEMS_COUNT; i++) {

            ModelMediaTreeItem.Type type = randomType.get();
            String extension = randomExtension.invoke(type);
            String name = randomName.invoke(type, i, extension);
            String path = randomPath.invoke(name);
            Long weight = randomWeight.invoke(type);
            Date createdAt = randomDate.get();
            Date updatedAt = randomDate.get();
            Size resolution = randomSize.invoke(type);
            Integer duration = randomDuration.invoke(type);

            items.add(new ModelMediaTreeItem((long)i, name, path, type, weight, createdAt, updatedAt, resolution, extension, duration));
        }

        return items;
    }

    @Override
    public Bitmap getItemThumbnail(ModelMediaTreeItem item) {
        if (item.resolution == null) {
            return ((BitmapDrawable)(ContextCompat.getDrawable(context, R.drawable.placeholder_960x960))).getBitmap();
        } else if (item.resolution.getWidth() == 1920) {
            return ((BitmapDrawable)(ContextCompat.getDrawable(context, R.drawable.placeholder_1920x1080))).getBitmap();
        } else {
            return ((BitmapDrawable)(ContextCompat.getDrawable(context, R.drawable.placeholder_1080x1920))).getBitmap();
        }
    }

    @Override
    public Uri getItemContentUri(ModelMediaTreeItem item) {
        int drawableId;

        if (item.resolution == null) {
            drawableId = R.drawable.placeholder_960x960;
        } else if (item.resolution.getWidth() == 1920) {
            drawableId = R.drawable.placeholder_1920x1080;
        } else {
            drawableId = R.drawable.placeholder_1080x1920;
        }

        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getPackageName() + "/" + drawableId);
    }

    // Возращает примерный список хранилищ
    @Override
    public List<ModelStorage> getAllStorages() {
        List<ModelStorage> list = new ArrayList<>();

        list.add(new ModelStorage("Внутреннее хранилище"));
        list.add(new ModelStorage("SD карта"));
        list.add(new ModelStorage("USB накопитель"));

        return list;
    }
}

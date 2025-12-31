package com.nti.nice_gallery.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Size;

import androidx.core.content.ContextCompat;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelGetFilesRequest;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelGetPreviewRequest;
import com.nti.nice_gallery.models.ModelGetPreviewResponse;
import com.nti.nice_gallery.models.ModelGetStoragesRequest;
import com.nti.nice_gallery.models.ModelGetStoragesResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelStorage;
import com.nti.nice_gallery.utils.ManagerOfThreads;
import com.nti.nice_gallery.utils.ReadOnlyList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;

// Менеджер файлов для теста интерфейса
public class ManagerOfFiles_Test1 implements IManagerOfFiles {

    private static Bitmap placeholder_960x960;
    private static Bitmap placeholder_1920x1080;
    private static Bitmap placeholder_1080x1920;

    private final Context context;
    private final ManagerOfThreads managerOfThreads;

    public ManagerOfFiles_Test1(Context context) {
        this.context = context;
        this.managerOfThreads = new ManagerOfThreads(context);

        if (placeholder_960x960 == null) {
            placeholder_960x960 = ((BitmapDrawable)(ContextCompat.getDrawable(context, R.drawable.placeholder_960x960))).getBitmap();
        }
        if (placeholder_1920x1080 == null) {
            placeholder_1920x1080 = ((BitmapDrawable)(ContextCompat.getDrawable(context, R.drawable.placeholder_1920x1080))).getBitmap();
        }
        if (placeholder_1080x1920 == null) {
            placeholder_1080x1920 = ((BitmapDrawable)(ContextCompat.getDrawable(context, R.drawable.placeholder_1080x1920))).getBitmap();
        }
    }

    // Возвращает спсиок случайно сгенерированных файлов
    @Override
    public void getFilesAsync(ModelGetFilesRequest request, Consumer<ModelGetFilesResponse> callback) {

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

        List<ModelMediaFile> items = new ArrayList<>();
        Random random = new Random(RANDOM_SEED);

        Supplier<LocalDateTime> randomDate = () -> {
            int randomInt = random.nextInt(MAX_NUMBER_OF_DAYS_AGO);
            return LocalDateTime.now().minusDays(randomInt);
        };

        Supplier<ModelMediaFile.Type> randomType = () -> {
            float randomFloat = random.nextFloat();
            int sumShare = SHARE_OF_IMAGES + SHARE_OF_VIDEOS + SHARE_OF_FOLDERS;
            float imagesPercent = (float)SHARE_OF_IMAGES / (float)sumShare;
            float videosPercent = (float)SHARE_OF_VIDEOS / (float)sumShare;
            float foldersPercent = (float)SHARE_OF_FOLDERS / (float)sumShare;

            float currentPercent = imagesPercent;
            if (randomFloat <= currentPercent) {
                return ModelMediaFile.Type.Image;
            }

            currentPercent += videosPercent;
            if (randomFloat <= currentPercent) {
                return ModelMediaFile.Type.Video;
            }

            return ModelMediaFile.Type.Folder;
        };

        Function1<ModelMediaFile.Type, String> randomExtension = (type) -> {
            if (type == ModelMediaFile.Type.Folder) {
                return null;
            }

            String[] imageExtensions = Arrays.stream(ModelMediaFile.supportedMediaFormats)
                    .filter(f -> f.type == ModelMediaFile.Type.Image)
                    .map(f -> f.fileExtension)
                    .toArray(String[]::new);

            String[] videoExtensions = Arrays.stream(ModelMediaFile.supportedMediaFormats)
                    .filter(f -> f.type == ModelMediaFile.Type.Video)
                    .map(f -> f.fileExtension)
                    .toArray(String[]::new);

            if (type == ModelMediaFile.Type.Image) {
                return imageExtensions[random.nextInt(imageExtensions.length)];
            }

            return videoExtensions[random.nextInt(videoExtensions.length)];
        };

        Function3<ModelMediaFile.Type, Integer, String, String> randomName = (type, i, extension) -> {
            return type == ModelMediaFile.Type.Folder ? "folder_" + i : "file_" + i + "." + extension;
        };

        Function1<String, String> randomPath = (fileName) -> {
            return "internal_storage/DCIM/" + fileName;
        };

        Function1<ModelMediaFile.Type, Long> randomWeight = (type) -> {
            if (type == ModelMediaFile.Type.Image) {
                return (long)random.nextInt(MAX_IMAGE_WEIGHT);
            }
            if (type == ModelMediaFile.Type.Video) {
                return (long)random.nextInt(MAX_VIDEO_WEIGHT);
            }

            return (long)random.nextInt(MAX_FOLDER_WEIGHT);
        };

        Function1<ModelMediaFile.Type, Size> randomSize = (type) -> {
            if (type == ModelMediaFile.Type.Folder) {
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

        Function1<ModelMediaFile.Type, Integer> randomDuration = (type) -> {
            return type == ModelMediaFile.Type.Video ? random.nextInt(MAX_VIDEO_DURATION) : null;
        };

        for (int i = 0; i < ITEMS_COUNT; i++) {

            ModelMediaFile.Type type = randomType.get();
            String extension = randomExtension.invoke(type);
            String name = randomName.invoke(type, i, extension);
            String path = randomPath.invoke(name);
            Long weight = randomWeight.invoke(type);
            LocalDateTime createdAt = randomDate.get();
            LocalDateTime updatedAt = randomDate.get();
            Size resolution = randomSize.invoke(type);
            Integer duration = randomDuration.invoke(type);

            int width = -1, height = -1, rotation = 0;

            if (resolution != null) {
                width = resolution.getWidth();
                height = resolution.getHeight();
            }

            items.add(new ModelMediaFile(
                    name,
                    path,
                    type,
                    createdAt,
                    updatedAt,
                    weight,
                    width,
                    height,
                    rotation,
                    extension,
                    duration,
                    null
            ));
        }

        ModelGetFilesResponse response = new ModelGetFilesResponse(
                LocalDateTime.now(),
                LocalDateTime.now().plusSeconds(1),
                new ReadOnlyList<>(items),
                null,
                null,
                null
        );

        managerOfThreads.safeAccept(callback, response);
    }

    @Override
    public void getPreviewAsync(ModelGetPreviewRequest request, Consumer<ModelGetPreviewResponse> callback) {
        if (request == null) {
            managerOfThreads.safeAccept(callback, new ModelGetPreviewResponse(null));
            return;
        }

        Bitmap bitmap = null;

        if (request.file.width == null || request.file.width <= 0) {
            bitmap = placeholder_960x960;
        } else if (request.file.width == 1920) {
            bitmap =  placeholder_1920x1080;
        } else {
            bitmap =  placeholder_1080x1920;
        }

        ModelGetPreviewResponse response = new ModelGetPreviewResponse(
                bitmap
        );

        managerOfThreads.safeAccept(callback, response);
    }

    // Возращает примерный список хранилищ
    @Override
    public void getStoragesAsync(ModelGetStoragesRequest request, Consumer<ModelGetStoragesResponse> callback) {
        List<ModelStorage> storages = new ArrayList<>();

        storages.add(new ModelStorage("Внутреннее хранилище", "context://internal_storage", ModelStorage.Type.Primary, null));
        storages.add(new ModelStorage("SD карта", "context://SD-card/hk78cJG435", ModelStorage.Type.Removable, null));
        storages.add(new ModelStorage("USB накопитель", "context://USB/hgTd67Hm3", ModelStorage.Type.Removable, null));

        ModelGetStoragesResponse response = new ModelGetStoragesResponse(
                new ReadOnlyList<>(storages)
        );

        managerOfThreads.safeAccept(callback, response);
    }
}

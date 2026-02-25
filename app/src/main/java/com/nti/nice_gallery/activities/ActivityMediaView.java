package com.nti.nice_gallery.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.ManagerOfFiles;
import com.nti.nice_gallery.models.ModelGetFilesResponse;
import com.nti.nice_gallery.models.ModelGetPreviewRequest;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.Convert;
import com.nti.nice_gallery.utils.GestureListener;
import com.nti.nice_gallery.utils.ManagerOfNavigation;
import com.nti.nice_gallery.utils.ManagerOfNotifications;
import com.nti.nice_gallery.utils.ManagerOfThreads;
import com.nti.nice_gallery.utils.ManagerOfTime;
import com.nti.nice_gallery.utils.ScaleGestureListener;
import com.nti.nice_gallery.views.ViewActionBar;
import com.nti.nice_gallery.views.buttons.ButtonFileInfo;
import com.nti.nice_gallery.views.buttons.ButtonPlay;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import kotlin.jvm.functions.Function3;

public class ActivityMediaView extends AppCompatActivity {

    private static final String LOG_TAG = "ActivityMediaView";
    private static final Object EMPTY = new Object();

    private static int currentFileIndex;
    private static int pastFileIndex;
    private static List<ModelMediaFile> files;
    private static boolean isToolPanelsVisible;
    private static boolean isBusy;
    private static ConcurrentHashMap<Integer, Object> cachedPreviews;
    private static ConcurrentHashMap<Integer, Object> previewsLoadingInProgress;
    private static Consumer<Integer> previewLoadedListener;
    private static boolean isVideoPaused;
    private static MediaPlayer mediaPlayer;
    private static Handler videoTickHandler;

    private Runnable onActivityDestroy;
    private Runnable onStartActivity;
    private Runnable onStopActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);
        init();
    }

    private void init() {
        ViewActionBar topActionBar = findViewById(R.id.topActionBar);
        ViewActionBar bottomActionBar = findViewById(R.id.bottomActionBar);
        LinearLayout bottomPanel = findViewById(R.id.bottomPanel);
        ButtonFileInfo buttonFileInfo = findViewById(R.id.buttonFileInfo);
        ButtonPlay buttonPlay = findViewById(R.id.buttonPlay);
        SeekBar seekBar = findViewById(R.id.seekBar);
        TextView timeView = findViewById(R.id.timeView);
        TextView infoView = findViewById(R.id.infoView);
        TextView actionInfoView = findViewById(R.id.actionInfoView);
        FrameLayout previewLayout = findViewById(R.id.previewLayout);
        ImageView imageView = findViewById(R.id.imageView);
        ImageView imageView2 = findViewById(R.id.imageView2);
        SurfaceView videoView = findViewById(R.id.surfaceView);

        Convert convert = new Convert(this);
        ManagerOfNavigation managerOfNavigation = new ManagerOfNavigation(this);
        ManagerOfFiles managerOfFiles = new ManagerOfFiles(this);
        ManagerOfThreads managerOfThreads = new ManagerOfThreads(this);
        ManagerOfNotifications managerOfNotifications = new ManagerOfNotifications(this);
        ManagerOfTime managerOfTime = new ManagerOfTime(this);

        Runnable setDefaultState = () -> {
            files = null;
            currentFileIndex = -1;
            pastFileIndex = -1;
            isToolPanelsVisible = true;
            isBusy = false;
            cachedPreviews = new ConcurrentHashMap<>();
            previewsLoadingInProgress = new ConcurrentHashMap<>();
            previewLoadedListener = null;
            isVideoPaused = false;
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if (videoTickHandler != null) {
                videoTickHandler.removeCallbacksAndMessages(null);
                videoTickHandler = null;
            }
        };

        Runnable refreshState = () -> {
            Payload newPayload = (Payload) managerOfNavigation.getPayload();

            if (newPayload != null) {
                setDefaultState.run();

                files = newPayload.response != null && newPayload.response.files != null
                        ? newPayload.response.files
                        .stream()
                        .filter(f -> f.isFile)
                        .collect(Collectors.toList())
                        : null;

                if (files != null && newPayload.startFile != null) {
                    int fileIndex = files.indexOf(newPayload.startFile);
                    currentFileIndex = fileIndex >= 0 ? fileIndex : -1;
                }
            }
        };

        Consumer<String> showActionInfo = new Consumer<String>() {
            final int ACTION_MESSAGE_SHOW_DELAY_MS = 700;
            Runnable lastAction;

            @Override
            public void accept(String message) {
                if (message != null) {
                    actionInfoView.setText(message);
                    actionInfoView.setVisibility(View.VISIBLE);
                    managerOfTime.cancelAction(lastAction);
                    lastAction = () -> managerOfThreads.runOnUiThread(() -> actionInfoView.setVisibility(View.GONE));
                    managerOfTime.doAction(lastAction, ACTION_MESSAGE_SHOW_DELAY_MS);
                } else {
                    actionInfoView.setVisibility(View.GONE);
                    managerOfTime.cancelAction(lastAction);
                }
            }
        };

        Runnable onUpdateIsBusy = () -> {
            topActionBar.setIsEnabled(!isBusy);
            bottomActionBar.setIsEnabled(!isBusy);
        };

        Runnable onUpdateToolPanelsVisibility = () -> {
            int visibility = isToolPanelsVisible ? View.VISIBLE : View.GONE;
            topActionBar.setVisibility(visibility);
            bottomPanel.setVisibility(visibility);
        };

        Runnable onCurrentFileChange = () -> {
            if (currentFileIndex < 0 || files == null || currentFileIndex >= files.size()) {
                return;
            }

            ModelMediaFile currentFile = files.get(currentFileIndex);

            bottomActionBar.setVisibility(currentFile.isVideo && currentFile.error == null ? View.VISIBLE : View.GONE);
            buttonFileInfo.setFile(currentFile);

            String info = getString(R.string.format_counter_file_index_from_all_files, currentFileIndex + 1, files.size());

            try {
                ArrayList<String> infoItems = new ArrayList<>();
                infoItems.add(info);

                if (currentFile.isVideo) {
                    infoItems.add(convert.durationToTimeString(currentFile.duration));
                }

                if (currentFile.isFile) {
                    infoItems.add(currentFile.extension.toUpperCase());
                    infoItems.add(convert.weightToString(currentFile.weight));
                    infoItems.add(convert.dateToFullNumericDateString(currentFile.createdAt));
                }

                info = String.join(getString(R.string.symbol_dot_separator), infoItems);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            infoView.setText(info);
        };

        Runnable adjustSurfaceSizeToVideoSize = () -> {
            ModelMediaFile currentFile = files.get(currentFileIndex);

            if (!currentFile.isVideo) {
                return;
            }

            int layoutWidth = previewLayout.getWidth();
            int layoutHeight = previewLayout.getHeight();

            int videoWidth = currentFile.width != null ? currentFile.width : layoutWidth;
            int videoHeight = currentFile.height != null ? currentFile.height : layoutHeight;

            float layoutRation = (float) layoutWidth / layoutHeight;
            float videoRatio = (float) videoWidth / videoHeight;

            ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();

            if (videoRatio > layoutRation) {
                layoutParams.width = layoutWidth;
                layoutParams.height = (int) (layoutWidth / videoRatio);
            } else {
                layoutParams.width = (int) (layoutHeight * videoRatio);
                layoutParams.height = layoutHeight;
            }

            videoView.setLayoutParams(layoutParams);
            videoView.requestLayout();
        };

        Runnable updateTimeBar = () -> {
            if (mediaPlayer == null) {
                return;
            }

            int currentTick = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(currentTick);
            timeView.setText(convert.durationToTimeString(currentTick));
        };

        SeekBar.OnSeekBarChangeListener onSeekBarChange = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    int progress = seekBar.getProgress();
                    mediaPlayer.seekTo(progress, MediaPlayer.SEEK_CLOSEST);
                }
            }
        };

        Runnable initTimeBar = () -> {
            ModelMediaFile currentFile = files.get(currentFileIndex);

            if (!currentFile.isVideo || currentFile.duration == null) {
                return;
            }

            Runnable onVideoTick = new Runnable() {
                @Override
                public void run() {
                    updateTimeBar.run();
                    videoTickHandler.postDelayed(this, 100);
                }
            };

            seekBar.setVisibility(View.VISIBLE);
            seekBar.setMin(0);
            seekBar.setMax(currentFile.duration);
            seekBar.setProgress(0);
            seekBar.setOnSeekBarChangeListener(onSeekBarChange);

            timeView.setVisibility(View.VISIBLE);

            updateTimeBar.run();

            videoTickHandler = new Handler(Looper.getMainLooper());
            videoTickHandler.post(onVideoTick);
        };

        Runnable destroyTimeBar = () -> {
            seekBar.setVisibility(View.GONE);
            timeView.setVisibility(View.GONE);
            if (videoTickHandler != null) {
                videoTickHandler.removeCallbacksAndMessages(null);
                videoTickHandler = null;
            }
        };

        Runnable stopVideoIfPlaying = () -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            isVideoPaused = false;
            videoView.setVisibility(View.GONE);
            buttonPlay.setState(ButtonPlay.State.Stop);
            destroyTimeBar.run();
        };

        Runnable onVideoPlayingError = () -> {
            stopVideoIfPlaying.run();
            managerOfNotifications.showToast(R.string.message_error_video_playing_failed);
        };

        Runnable bindSurfaceToMediaPlayer = () -> {
            if (mediaPlayer == null) {
                return;
            }

            mediaPlayer.setDisplay(videoView.getHolder());
            mediaPlayer.setOnVideoSizeChangedListener((mp, videoWidth, videoHeight) -> adjustSurfaceSizeToVideoSize.run());
            mediaPlayer.setOnCompletionListener(mediaPlayer -> stopVideoIfPlaying.run());
            mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> { onVideoPlayingError.run(); return true; });
        };

        Runnable unbindSurfaceFromMediaPlayer = () -> {
            if (mediaPlayer == null) {
                return;
            }

            mediaPlayer.setDisplay(null);
            mediaPlayer.setOnVideoSizeChangedListener(null);
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.setOnErrorListener(null);
        };

        Runnable resetPreviewScale = () -> {
            if (imageView.getScaleType() != ImageView.ScaleType.FIT_CENTER) {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                showActionInfo.accept(getString(R.string.format_scale, 100));
            }
        };

        Runnable startVideo = () -> {
            ModelMediaFile currentFile = files.get(currentFileIndex);

            if (!currentFile.isVideo || currentFile.duration == null) {
                return;
            }

            resetPreviewScale.run();

            if (mediaPlayer != null) {
                if (!mediaPlayer.isPlaying()) {
                    isVideoPaused = false;
                    mediaPlayer.start();
                }
                return;
            }

            initTimeBar.run();

            videoView.setVisibility(View.VISIBLE);
            videoView.post(() -> {
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(currentFile.path);
                    bindSurfaceToMediaPlayer.run();
                    mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                    onVideoPlayingError.run();
                }
            });
        };

        Runnable restoreVideoIfPlaying = () -> {
            if (mediaPlayer == null) {
                return;
            }

            initTimeBar.run();

            videoView.setVisibility(View.VISIBLE);
            videoView.post(() -> {
                bindSurfaceToMediaPlayer.run();
                if (!isVideoPaused) {
                    mediaPlayer.start();
                }
            });
        };

        Runnable pauseVideoIfPlaying = () -> {
            if (mediaPlayer != null && !isVideoPaused) {
                isVideoPaused = true;
                mediaPlayer.pause();
            }
        };

        Consumer<Integer> rewindVideo = new Consumer<Integer>() {
            final int ACTION_MESSAGE_SHOW_DELAY_MS = 700;
            private Long lastRewindTime;
            private int accumulatedTime = 0;

            @Override
            public void accept(Integer millis) {
                if (mediaPlayer == null) {
                    return;
                }

                if (lastRewindTime == null || (System.currentTimeMillis() - lastRewindTime) < ACTION_MESSAGE_SHOW_DELAY_MS) {
                    accumulatedTime += Math.round((float) millis / 1000);
                } else {
                    accumulatedTime = Math.round((float) millis / 1000);
                }

                String accumulatedTimeStr;
                if (accumulatedTime >= 0) {
                    accumulatedTimeStr = getString(R.string.format_rewind_time_forward, Math.abs(accumulatedTime));
                } else {
                    accumulatedTimeStr = getString(R.string.format_rewind_time_back, Math.abs(accumulatedTime));
                }

                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + millis);
                lastRewindTime = System.currentTimeMillis();
                showActionInfo.accept(accumulatedTimeStr);
            }
        };

        Consumer<Bitmap> setPreviewAndAnimate = preview -> {
            final int ANIMATION_DURATION = 200;

            Consumer<ImageView> setPreview = iv -> {
                if (preview != null) {
                    iv.setBackgroundColor(Color.TRANSPARENT);
                    iv.setImageBitmap(preview);
                } else {
                    iv.setBackgroundColor(getColor(R.color.orange_200));
                    iv.setImageResource(R.drawable.baseline_error_24_orange_700);
                }
            };

            Runnable runAnimation = () -> {
                int translationX1, translationX2, startX2, finishX1;
                int width = imageView.getWidth();

                if (currentFileIndex > pastFileIndex) {
                    translationX1 = -width;
                    translationX2 = 0;
                    startX2 = width;
                    finishX1 = 0;
                } else if (currentFileIndex < pastFileIndex) {
                    translationX1 = width;
                    translationX2 = 0;
                    startX2 = -width;
                    finishX1 = 0;
                } else {
                    setPreview.accept(imageView);
                    return;
                }

                setPreview.accept(imageView2);
                imageView2.setTranslationX(startX2);
                imageView2.setVisibility(View.VISIBLE);

                imageView.animate()
                        .translationX(translationX1)
                        .setDuration(ANIMATION_DURATION)
                        .start();

                imageView2.animate()
                        .translationX(translationX2)
                        .setDuration(ANIMATION_DURATION)
                        .withEndAction(() -> {
                            setPreview.accept(imageView);
                            imageView.setTranslationX(finishX1);
                            imageView2.setVisibility(View.INVISIBLE);
                        })
                        .start();
            };

            if (pastFileIndex < 0 || pastFileIndex == currentFileIndex) {
                setPreview.accept(imageView);
            } else {
                runAnimation.run();
            }

            pastFileIndex = currentFileIndex;
        };

        Runnable showCurrentFile = () -> {
            if (files == null || currentFileIndex < 0 || currentFileIndex >= files.size()) {
                return;
            }

            Consumer<Integer> loadFileToCache = fileIndex -> {
                if (fileIndex < 0 ||
                        fileIndex >= files.size() ||
                        cachedPreviews.containsKey(fileIndex) ||
                        previewsLoadingInProgress.containsKey(fileIndex)
                ) {
                    return;
                }

                final int indexFinal = fileIndex;
                final ModelMediaFile file = files.get(indexFinal);

                ModelGetPreviewRequest previewRequest = new ModelGetPreviewRequest(
                        files.get(indexFinal),
                        file.width,
                        file.height
                );

                previewsLoadingInProgress.put(indexFinal, new Object());

                managerOfFiles.getPreviewAsync(previewRequest, response -> {
                    Bitmap preview = response != null ? response.preview : null;
                    cachedPreviews.put(indexFinal, preview != null ? preview : EMPTY);
                    previewsLoadingInProgress.remove(indexFinal);
                    if (previewLoadedListener != null) {
                        previewLoadedListener.accept(indexFinal);
                    }
                });
            };

            Runnable updateCache = () -> {
                final int MAX_PREVIEW_CASH_SIZE = 4;
                final boolean CLEAR_CACHE = false;

                final int staleCacheBoundary = MAX_PREVIEW_CASH_SIZE / 2;

                if (CLEAR_CACHE) {
                    cachedPreviews.entrySet().removeIf(e -> Math.abs(currentFileIndex - e.getKey()) > staleCacheBoundary);
                }

                final int startIndex = currentFileIndex - (MAX_PREVIEW_CASH_SIZE / 2);
                final int endIndex = currentFileIndex + (MAX_PREVIEW_CASH_SIZE / 2);

                loadFileToCache.accept(currentFileIndex);
                for (int fileIndex = startIndex; fileIndex <= endIndex; fileIndex++) {
                    loadFileToCache.accept(fileIndex);
                }
            };

            Runnable loadCurrentFilePreview = () -> {

                if (cachedPreviews.containsKey(currentFileIndex)) {
                    Object cachedValue = cachedPreviews.get(currentFileIndex);
                    Bitmap preview = cachedValue instanceof Bitmap ? (Bitmap) cachedValue : null;
                    setPreviewAndAnimate.accept(preview);
                    return;
                }

                isBusy = true;
                onUpdateIsBusy.run();

                previewLoadedListener = fileIndex -> {
                    if (fileIndex != currentFileIndex) {
                        return;
                    }

                    Object cachedValue = cachedPreviews.get(currentFileIndex);
                    Bitmap preview = cachedValue instanceof Bitmap ? (Bitmap) cachedValue : null;

                    managerOfThreads.runOnUiThread(() -> {
                        setPreviewAndAnimate.accept(preview);

                        isBusy = false;
                        onUpdateIsBusy.run();
                    });
                };

                updateCache.run();
            };

            imageView.post(() -> {
                loadCurrentFilePreview.run();
                updateCache.run();
            });
        };

        Runnable stepForward = () -> {
            if (files == null || currentFileIndex >= files.size() - 1) {
                return;
            }

            currentFileIndex++;

            stopVideoIfPlaying.run();
            onCurrentFileChange.run();
            showCurrentFile.run();
        };

        Runnable stepBack = () -> {
            if (files == null || currentFileIndex < 1) {
                return;
            }

            currentFileIndex--;

            stopVideoIfPlaying.run();
            onCurrentFileChange.run();
            showCurrentFile.run();
        };

        Function3<Float, Float, Float, Object> scalePreview = new Function3<Float, Float, Float, Object>() {
            private final float MAX_SCALE_FACTOR = 5f;
            private float minScale;
            private float maxScale;

            @Override
            public Object invoke(Float scaleFactor, Float focusX, Float focusY) {
                Matrix imageMatrix;
                float[] matrixValues = new float[9];

                // 1. Инициализация при первом жесте
                if (imageView.getScaleType() != ImageView.ScaleType.MATRIX) {
                    imageMatrix = setupInitialMatrix();
                } else {
                    imageMatrix = imageView.getImageMatrix();
                }

                imageMatrix.getValues(matrixValues);
                float currentScale = matrixValues[Matrix.MSCALE_X];

                // 2. Вычисляем новый масштаб
                float newScale = currentScale * scaleFactor;

                // Ограничиваем масштаб
                if (newScale < minScale) newScale = minScale;
                if (newScale > maxScale) newScale = maxScale;

                // 3. Логика сброса (если масштаб вернулся к минимальному)
                if (newScale <= minScale) {
                    resetPreviewScale.run();
                    return null;
                }

                // 4. Применяем трансформацию
                float adjustedScaleFactor = newScale / currentScale;
                imageMatrix.postScale(adjustedScaleFactor, adjustedScaleFactor, focusX, focusY);

                imageView.setImageMatrix(imageMatrix);

                // Показываем процент относительно базового размера (FIT_CENTER = 100%)
                int displayPercent = Math.round((newScale / minScale) * 100);
                showActionInfo.accept(getString(R.string.format_scale, displayPercent));

                return null;
            }

            private Matrix setupInitialMatrix() {
                Matrix imageMatrix = new Matrix();

                if (imageView.getDrawable() == null) return imageMatrix;

                float dWidth = imageView.getDrawable().getIntrinsicWidth();
                float dHeight = imageView.getDrawable().getIntrinsicHeight();
                float vWidth = imageView.getWidth();
                float vHeight = imageView.getHeight();

                // Вычисляем масштаб для FIT_CENTER
                float scaleX = vWidth / dWidth;
                float scaleY = vHeight / dHeight;
                float baseScale = Math.min(scaleX, scaleY);

                minScale = baseScale;
                maxScale = baseScale * MAX_SCALE_FACTOR;

                // Устанавливаем начальную матрицу равной текущему FIT_CENTER
                imageMatrix.reset();
                imageMatrix.postScale(baseScale, baseScale);

                // Центрируем изображение
                float dx = (vWidth - dWidth * baseScale) / 2f;
                float dy = (vHeight - dHeight * baseScale) / 2f;
                imageMatrix.postTranslate(dx, dy);

                imageView.setScaleType(ImageView.ScaleType.MATRIX);

                return imageMatrix;
            }
        };

        Consumer<GestureListener.GestureArgs> onGestureDetected = new Consumer<GestureListener.GestureArgs>() {
            private final float DOUBLE_TAP_SCALE_FACTOR = 3f;
            private final int DOUBLE_TAP_VIDEO_REWIND_MILLIS = 10_000;

            @Override
            public void accept(GestureListener.GestureArgs gestureArgs) {
                if (isBusy) {
                    return;
                }
                if (gestureArgs.gesture == GestureListener.Gesture.Tap) {
                    isToolPanelsVisible = !isToolPanelsVisible;
                    onUpdateToolPanelsVisibility.run();
                    return;
                }
                if (imageView.getScaleType() != ImageView.ScaleType.MATRIX) {
                    if (gestureArgs.gesture == GestureListener.Gesture.SwipeRight) {
                        stepBack.run();
                        return;
                    }
                    if (gestureArgs.gesture == GestureListener.Gesture.SwipeLeft) {
                        stepForward.run();
                        return;
                    }
                    if (gestureArgs.gesture == GestureListener.Gesture.SwipeUp) {
                        buttonFileInfo.showInfo();
                        return;
                    }
                    if (gestureArgs.gesture == GestureListener.Gesture.SwipeDown) {
                        managerOfNavigation.navigateBack();
                        return;
                    }
                }
                if (videoView.getVisibility() == View.VISIBLE) {
                    if (gestureArgs.gesture == GestureListener.Gesture.DoubleTap) {
                        int screenWidth = previewLayout.getWidth();
                        int rewindMillis = screenWidth / 2f < gestureArgs.tapX ? DOUBLE_TAP_VIDEO_REWIND_MILLIS : -DOUBLE_TAP_VIDEO_REWIND_MILLIS;
                        rewindVideo.accept(rewindMillis);
                        return;
                    }
                }
                if (imageView.getScaleType() != ImageView.ScaleType.MATRIX && videoView.getVisibility() != View.VISIBLE) {
                    if (gestureArgs.gesture == GestureListener.Gesture.DoubleTap) {
                        scalePreview.invoke(DOUBLE_TAP_SCALE_FACTOR, (float) gestureArgs.tapX, (float) gestureArgs.tapY);
                        return;
                    }
                }
                if (imageView.getScaleType() == ImageView.ScaleType.MATRIX && videoView.getVisibility() != View.VISIBLE) {
                    if (gestureArgs.gesture == GestureListener.Gesture.Scroll) {
                        moveImage(gestureArgs.scrollDistX, gestureArgs.scrollDistY);
                        return;
                    }
                    if (gestureArgs.gesture == GestureListener.Gesture.DoubleTap) {
                        resetPreviewScale.run();
                        return;
                    }
                }
            }

            private void moveImage(float deltaX, float deltaY) {
                Matrix matrix = new Matrix(imageView.getImageMatrix());
                float[] values = new float[9];
                matrix.getValues(values);

                float transX = values[Matrix.MTRANS_X];
                float transY = values[Matrix.MTRANS_Y];
                float scaleX = values[Matrix.MSCALE_X];
                float scaleY = values[Matrix.MSCALE_Y];

                float imageWidth = imageView.getDrawable().getIntrinsicWidth() * scaleX;
                float imageHeight = imageView.getDrawable().getIntrinsicHeight() * scaleY;
                float viewWidth = imageView.getWidth();
                float viewHeight = imageView.getHeight();

                // Логика по горизонтали
                if (imageWidth > viewWidth) {
                    // Ограничиваем, чтобы не вылезти за левый и правый края
                    if (transX + deltaX > 0) deltaX = -transX;
                    else if (transX + deltaX < viewWidth - imageWidth) deltaX = viewWidth - imageWidth - transX;
                } else {
                    deltaX = 0; // Если картинка уже меньше экрана, не двигаем
                }

                // Логика по вертикали
                if (imageHeight > viewHeight) {
                    // Ограничиваем, чтобы не вылезти за верхний и нижний края
                    if (transY + deltaY > 0) deltaY = -transY;
                    else if (transY + deltaY < viewHeight - imageHeight) deltaY = viewHeight - imageHeight - transY;
                } else {
                    deltaY = 0; // Если картинка меньше экрана, не двигаем
                }

                matrix.postTranslate(deltaX, deltaY);
                imageView.setImageMatrix(matrix);
            }
        };

        Consumer<ScaleGestureListener.PinchArgs> onScaleGestureDetected = pinchArgs -> {
            scalePreview.invoke(pinchArgs.scaleFactor, pinchArgs.focusX, pinchArgs.focusY);
        };

        View.OnTouchListener onPreviewLayoutTouch = new View.OnTouchListener() {
            final GestureDetector gestureDetector = new GestureDetector(ActivityMediaView.this, new GestureListener(onGestureDetected));
            final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(ActivityMediaView.this, new ScaleGestureListener(onScaleGestureDetected));

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                scaleGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        };

        Consumer<ButtonPlay> onButtonPlayStateChange = btn -> {
            if (btn.getState() == ButtonPlay.State.Play) {
                startVideo.run();
            }
            if (btn.getState() == ButtonPlay.State.Pause) {
                pauseVideoIfPlaying.run();
            }
        };

        Runnable onFinishingActivity = () -> {
            setDefaultState.run();
        };

        Runnable onRecreateActivity = () -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                unbindSurfaceFromMediaPlayer.run();
            }

            showActionInfo.accept(null);
        };

        onActivityDestroy = () -> {
            if (isFinishing()) {
                onFinishingActivity.run();
            } else {
                onRecreateActivity.run();
            }
        };

        onStartActivity = () -> {
            restoreVideoIfPlaying.run();
        };

        onStopActivity = () -> {
            onRecreateActivity.run();
        };

        refreshState.run();

        previewLayout.setOnTouchListener(onPreviewLayoutTouch);
        buttonPlay.setState(mediaPlayer == null ? ButtonPlay.State.Stop : isVideoPaused ? ButtonPlay.State.Pause : ButtonPlay.State.Play);
        buttonPlay.setStateChangeListener(onButtonPlayStateChange);

        showCurrentFile.run();
        onUpdateToolPanelsVisibility.run();
        onCurrentFileChange.run();
        restoreVideoIfPlaying.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (onActivityDestroy != null) {
            onActivityDestroy.run();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (onStartActivity != null) {
            onStartActivity.run();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (onStopActivity != null) {
            onStopActivity.run();
        }
    }

    public static class Payload {
        public final ModelGetFilesResponse response;
        public final ModelMediaFile startFile;

        public Payload(
                ModelGetFilesResponse response,
                ModelMediaFile startFile
        ) {
            this.response = response;
            this.startFile = startFile;
        }
    }
}

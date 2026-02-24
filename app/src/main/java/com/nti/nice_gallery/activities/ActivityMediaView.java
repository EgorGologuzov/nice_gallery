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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private static boolean isScaleModeEnabled;
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
        SurfaceView surfaceView = findViewById(R.id.surfaceView);

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
            isScaleModeEnabled = false;
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

            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();

            if (videoRatio > layoutRation) {
                layoutParams.width = layoutWidth;
                layoutParams.height = (int) (layoutWidth / videoRatio);
            } else {
                layoutParams.width = (int) (layoutHeight * videoRatio);
                layoutParams.height = layoutHeight;
            }

            surfaceView.setLayoutParams(layoutParams);
            surfaceView.requestLayout();
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
            surfaceView.setVisibility(View.GONE);
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

            mediaPlayer.setDisplay(surfaceView.getHolder());
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

        Runnable startVideo = () -> {
            ModelMediaFile currentFile = files.get(currentFileIndex);

            if (!currentFile.isVideo || currentFile.duration == null) {
                return;
            }

            if (mediaPlayer != null) {
                if (!mediaPlayer.isPlaying()) {
                    isVideoPaused = false;
                    mediaPlayer.start();
                }
                return;
            }

            initTimeBar.run();

            surfaceView.setVisibility(View.VISIBLE);
            surfaceView.post(() -> {
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

            surfaceView.setVisibility(View.VISIBLE);
            surfaceView.post(() -> {
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

            Runnable updateCache = () -> {
                final int MAX_PREVIEW_CASH_SIZE = 4;
                final boolean CLEAR_CACHE = false;

                final int staleCacheBoundary = MAX_PREVIEW_CASH_SIZE / 2;

                if (CLEAR_CACHE) {
                    cachedPreviews.entrySet().removeIf(e -> Math.abs(currentFileIndex - e.getKey()) > staleCacheBoundary);
                }

                final int startIndex = currentFileIndex - (MAX_PREVIEW_CASH_SIZE / 2);
                final int endIndex = currentFileIndex + (MAX_PREVIEW_CASH_SIZE / 2);

                for (int fileIndex = startIndex; fileIndex <= endIndex; fileIndex++) {

                    if (fileIndex < 0 ||
                            fileIndex >= files.size() ||
                            cachedPreviews.containsKey(fileIndex) ||
                            previewsLoadingInProgress.containsKey(fileIndex)
                    ) {
                        continue;
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

        Consumer<GestureListener.Gesture> onGestureDetected = gesture -> {
            if (isBusy) {
                return;
            }
            if (gesture == GestureListener.Gesture.SwipeRight) {
                stepBack.run();
            }
            if (gesture == GestureListener.Gesture.SwipeLeft) {
                stepForward.run();
            }
            if (gesture == GestureListener.Gesture.Tap) {
                isToolPanelsVisible = !isToolPanelsVisible;
                onUpdateToolPanelsVisibility.run();
            }
        };

        Consumer<ScaleGestureListener.PinchArgs> onScaleGestureDetected = new Consumer<ScaleGestureListener.PinchArgs>() {
            private float SCALE_THRESHOLD = 0.05f;

            private Matrix imageMatrix = new Matrix();
            private float[] matrixValues = new float[9];
            private float minScale = 1f;
            private float maxScale = 3.0f;

            @Override
            public void accept(ScaleGestureListener.PinchArgs pinchArgs) {
                imageMatrix.getValues(matrixValues);
                float currentScale = matrixValues[Matrix.MSCALE_X];

                float newScale = currentScale * pinchArgs.scaleFactor;
                newScale = Math.max(Math.min(newScale, maxScale), minScale);

                if (newScale == minScale) {
                    if (imageView.getScaleType() == ImageView.ScaleType.MATRIX) {
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        imageView.setImageMatrix(null); // Сбрасываем матрицу
                        isScaleModeEnabled = false;
                    }

                    showActionInfo.accept(getString(R.string.format_scale, Math.round(minScale * 100)));

                } else if (newScale >= minScale + SCALE_THRESHOLD || newScale < currentScale) {
                    float adjustedScaleFactor = newScale / currentScale;

                    imageMatrix.postScale(adjustedScaleFactor, adjustedScaleFactor,
                            pinchArgs.focusX, pinchArgs.focusY);

                    imageView.setScaleType(ImageView.ScaleType.MATRIX);
                    imageView.setImageMatrix(imageMatrix);

                    isScaleModeEnabled = true;

                    showActionInfo.accept(getString(R.string.format_scale, Math.round(newScale * 100)));
                }
            }
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
            isScaleModeEnabled = false;
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

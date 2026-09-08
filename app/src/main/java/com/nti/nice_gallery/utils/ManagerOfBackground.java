package com.nti.nice_gallery.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.models.ModelProgress;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ManagerOfBackground {
    private static final String LOG_TAG = "ManagerOfBackground";
    private static final String CHANNEL_ID = "background_work_channel";
    private static final String KEY_TASK_ID = "process_id";
    private static final String KEY_PROGRESS = "progress";
    private static final String KEY_ACTION_START_SERVICE = "start_service";
    private static final String KEY_ACTION_UPDATE_SERVICE = "update_service";
    private static final String KEY_ACTION_STOP_SERVICE = "stop_service";

    private static final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final TaskQueue taskQueue = new TaskQueue();

    private final Context context;
    private final ManagerOfNotifications managerOfNotifications;

    public ManagerOfBackground(Context context) {
        this.context = context;
        managerOfNotifications = new ManagerOfNotifications(context);
        managerOfNotifications.createChannel(CHANNEL_ID);
    }

    public void startTask(BackgroundTask task) {
        taskQueue.add(task);
        startNextTask();
    }

    private void startNextTask() {
        if (taskQueue.isEmpty() || isProcessing.get()) {
            return;
        }

        isProcessing.set(true);
        int taskId = taskQueue.peek();
        BackgroundTask task = taskQueue.get(taskId);

        startForegroundService(taskId);

        task.setOnProgressListener(progress -> {
            updateForegroundService(taskId, progress);
        });

        task.setOnCancelListener(message -> {
            showResultNotification(taskId, message);
            stopForegroundService();
            taskQueue.remove(taskId);
            isProcessing.set(false);
            startNextTask();
        });

        task.setOnFinishListener(message -> {
            showResultNotification(taskId, message);
            stopForegroundService();
            taskQueue.remove(taskId);
            isProcessing.set(false);
            startNextTask();
        });

        Future<?> future = executor.submit(() -> {
            try {
                task.start();
            } catch (Exception e) {
                Log.e(LOG_TAG + "-260908-1", "Runtime error", e);
            } finally {
                isProcessing.set(false);
                taskQueue.remove(taskId);
            }
        });

        task.setFuture(future);
    }

    private void showResultNotification(int taskId, String message) {
        managerOfNotifications.cancelNotification(taskId);
        managerOfNotifications.showNotification(
                CHANNEL_ID,
                -taskId,
                R.string.notif_title_task_finished,
                message,
                builder -> {
                    builder.setAutoCancel(true);
                }
        );
    }

    private void startForegroundService(int taskId) {
        Intent intent = new Intent(context, BackgroundTaskService.class);
        intent.setAction(KEY_ACTION_START_SERVICE);
        intent.putExtra(KEY_TASK_ID, taskId);
        context.startService(intent);
    }

    private void updateForegroundService(int taskId, ModelProgress progress) {
        Intent intent = new Intent(context, BackgroundTaskService.class);
        intent.setAction(KEY_ACTION_UPDATE_SERVICE);
        intent.putExtra(KEY_TASK_ID, taskId);
        intent.putExtra(KEY_PROGRESS, progress.toJson());
        context.startService(intent);
    }

    private void stopForegroundService() {
        Intent intent = new Intent(context, BackgroundTaskService.class);
        intent.setAction(KEY_ACTION_STOP_SERVICE);
        context.startService(intent);
    }

    public static abstract class BackgroundTask {
        protected AtomicBoolean isCancelled = new AtomicBoolean(false);
        protected Consumer<ModelProgress> onProgressListener;
        protected Consumer<String> onCancelListener;
        protected Consumer<String> onFinishListener;
        private Future<?> future;

        public abstract void start();

        public void cancel() {
            isCancelled.set(true);
//            if (future != null && !future.isDone()) {
//                future.cancel(true);
//            }
        }

        protected void notifyProgress(ModelProgress progress) {
            if (onProgressListener != null) {
                onProgressListener.accept(progress);
            }
        }

        protected void notifyCanceled(String message) {
            if (onCancelListener != null) {
                onCancelListener.accept(message);
            }
        }

        protected void notifyFinished(String message) {
            if (onFinishListener != null) {
                onFinishListener.accept(message);
            }
        }

        protected boolean isCancelled() {
            return isCancelled.get();
        }

        private void setOnProgressListener(Consumer<ModelProgress> listener) {
            this.onProgressListener = listener;
        }

        private void setOnCancelListener(Consumer<String> listener) {
            this.onCancelListener = listener;
        }

        private void setOnFinishListener(Consumer<String> listener) {
            this.onFinishListener = listener;
        }

        private void setFuture(Future<?> future) {
            this.future = future;
        }
    }

    public static class TaskInQueue {
        public final int id;
        public final BackgroundTask task;
        public TaskInQueue(int id, BackgroundTask task) {
            this.id = id;
            this.task = task;
        }
    }

    public static class TaskQueue {

        private final Queue<TaskInQueue> tasksQueue = new ConcurrentLinkedQueue<>();

        public int add(BackgroundTask task) {
            int id = ManagerOfNotifications.getNextId();
            tasksQueue.add(new TaskInQueue(id, task));
            return id;
        }

        public BackgroundTask get(int id) {
            TaskInQueue tq = getProcessInQueue(id);
            return tq != null ? tq.task : null;
        }

        public void remove(int id) {
            TaskInQueue tq = getProcessInQueue(id);
            if (tq != null) {
                tasksQueue.remove(tq);
            }
        }

        public Integer peek() {
            TaskInQueue pq = tasksQueue.peek();
            return pq != null ? pq.id : null;
        }

        public boolean isEmpty() {
            return tasksQueue.isEmpty();
        }

        public int size() {
            return tasksQueue.size();
        }

        private TaskInQueue getProcessInQueue(int id) {
            for (TaskInQueue pq : tasksQueue) {
                if (id == pq.id) {
                    return pq;
                }
            }
            return null;
        }
    }

    public static class CancelReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int taskId = intent.getIntExtra(KEY_TASK_ID, 0);
            BackgroundTask task = taskQueue.get(taskId);
            if (task != null) {
                task.cancel();
            }
        }
    }

    // ----- Foreground Service -----
    public static class BackgroundTaskService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            String action = intent.getAction();
            int taskId = intent.getIntExtra(KEY_TASK_ID, 0);

            if (KEY_ACTION_STOP_SERVICE.equals(action)) {
                stopForeground(true);
                stopSelf();
            }

            if (KEY_ACTION_UPDATE_SERVICE.equals(action)) {
                ModelProgress progress = new ModelProgress(intent.getStringExtra(KEY_PROGRESS));
                updateNotification(taskId, progress);
            }

            if (KEY_ACTION_START_SERVICE.equals(action)) {
                Notification notification = ManagerOfNotifications.createForegroundInitNotification(this, CHANNEL_ID);
                startForeground(taskId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            }

            return START_NOT_STICKY;
        }

        private void updateNotification(int taskId, ModelProgress progress) {
            String title = taskQueue.size() > 1 ?
                    getString(R.string.format_background_operation_status, taskQueue.size() - 1) :
                    getString(R.string.notif_title_background_operation);

            Intent cancelIntent = new Intent(this, CancelReceiver.class);
            cancelIntent.putExtra(KEY_TASK_ID, taskId);
            PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(
                    this,
                    taskId,
                    cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            ManagerOfNotifications.updateForegroundNotification(
                    this,
                    CHANNEL_ID,
                    taskId,
                    title,
                    progress.getMessage(),
                    builder -> {
                        builder.setProgress(progress.numberTotalSteps, progress.numberCompletedSteps, false);
                        builder.addAction(R.drawable.baseline_clear_24, getString(R.string.notif_button_cancel), cancelPendingIntent);
                    }
            );
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}

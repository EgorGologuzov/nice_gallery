package com.nti.nice_gallery.utils;

import android.app.Activity;

import android.app.Application;
import android.os.Bundle;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.ManagerOfDatabase;
import com.nti.nice_gallery.models.ModelProgress;

import java.time.LocalDateTime;

public class App extends Application implements Application.ActivityLifecycleCallbacks {

    private int startedActivities = 0;

    private Convert convert;
    private ManagerOfDatabase managerOfDatabase;
    private ManagerOfThreads managerOfThreads;
    private ManagerOfNotifications managerOfNotifications;
    private ManagerOfBackground managerOfBackground;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        onAppStart();
        registerActivityLifecycleCallbacks(this);
    }

    private void init() {
        ManagerOfNotifications.appStartInit(this);

        convert = new Convert(this);
        managerOfDatabase = new ManagerOfDatabase(this);
        managerOfThreads = new ManagerOfThreads(this);
        managerOfNotifications = new ManagerOfNotifications(this);
        managerOfBackground = new ManagerOfBackground(this);
    }

    private void onAppStart() {
        final String pattern = getString(R.string.format_message_database_restored);

        Runnable restoreDatabase = () -> {
            final LocalDateTime start = LocalDateTime.now();
            managerOfDatabase.restoreFilesData();
            String timeStr = convert.timeIntervalToTimeString(start, LocalDateTime.now());
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                managerOfNotifications.showToast(String.format(pattern, timeStr));
            });
        };

        managerOfThreads.executeAsync(restoreDatabase);
    }

    private void onAppEnterForeground() {
        // Если приложение вернулось из фона на экран
    }

    private void onAppEnterBackground() {
        final String progressMessage = getString(R.string.notif_message_store_file_data);
        final String finishMessage = getString(R.string.notif_message_store_file_data_finished);

        Runnable storeFilesData = () -> {
            managerOfBackground.startTask(new ManagerOfBackground.BackgroundTask() {
                @Override
                public void start() {
                    ModelProgress progress = new ModelProgress(0, 1, progressMessage);
                    notifyProgress(progress);
                    managerOfDatabase.storeFilesData();
                    progress = new ModelProgress(1, 1, progressMessage);
                    notifyProgress(progress);
                    notifyFinished(finishMessage);
                }
            });
        };

        storeFilesData.run();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (startedActivities == 0) {
            onAppEnterForeground();
        }
        startedActivities++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        startedActivities--;
        if (startedActivities == 0) {
            onAppEnterBackground();
        }
    }

    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override public void onActivityResumed(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}

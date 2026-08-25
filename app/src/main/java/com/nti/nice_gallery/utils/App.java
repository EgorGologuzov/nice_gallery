package com.nti.nice_gallery.utils;

import android.app.Activity;

import android.app.Application;
import android.os.Bundle;

import com.nti.nice_gallery.data.ManagerOfDatabase;

public class App extends Application implements Application.ActivityLifecycleCallbacks {

    private int startedActivities = 0;

    private ManagerOfDatabase managerOfDatabase;
    private ManagerOfThreads managerOfThreads;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        onAppStart();
        registerActivityLifecycleCallbacks(this);
    }

    private void init() {
        managerOfDatabase = new ManagerOfDatabase(this);
        managerOfThreads = new ManagerOfThreads(this);
    }

    private void onAppStart() {
        managerOfThreads.executeAsync(() -> managerOfDatabase.restoreFilesInfoCache());
    }

    private void onAppEnterForeground() {
        // Если приложение вернулось из фона на экран
    }

    private void onAppEnterBackground() {
        managerOfDatabase.storeFilesInfoCache();
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

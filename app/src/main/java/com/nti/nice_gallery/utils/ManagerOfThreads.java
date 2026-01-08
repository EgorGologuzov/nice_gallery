package com.nti.nice_gallery.utils;

import android.app.Activity;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ManagerOfThreads {

    private static final String LOG_TAG = "ManagerOfThreads";

    private static ExecutorService executor;

    private final Context context;

    public ManagerOfThreads(Context context) {
        this.context = context;
    }

    public <T> void safeAccept(Consumer<T> callback, T payload) {
        if (callback != null) {
            callback.accept(payload);
        }
    }

    public void runOnUiThread(Runnable task) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.runOnUiThread(task);
            return;
        }

        throw new IllegalStateException();
    }

    public <T> void executeAsync(Runnable task) {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(4);
        }

        executor.execute(() -> {
            if (task != null) {
                task.run();
            }
        });
    }

    public <T> void executeAsync(Supplier<T> task, Consumer<T> callback) {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(4);
        }

        executor.execute(() -> {
            if (task != null) {
                T result = task.get();
                safeAccept(callback, result);
            }
        });
    }
}

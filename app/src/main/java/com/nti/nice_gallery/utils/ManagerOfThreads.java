package com.nti.nice_gallery.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    public synchronized void initThreadPool() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(4);
        }
    }

    public <T> void executeAsync(Runnable task) {
        initThreadPool();
        executor.execute(() -> {
            if (task != null) {
                task.run();
            }
        });
    }

    public <T> void executeAsync(Supplier<T> task, Consumer<T> callback) {
        initThreadPool();
        executor.execute(() -> {
            if (task != null) {
                T result = task.get();
                safeAccept(callback, result);
            }
        });
    }

    public <T> void executeAsync(List<Supplier<T>> tasks, Consumer<List<T>> callback) {
        initThreadPool();

        // 1. Создаем список асинхронных задач для каждого Supplier
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(task, executor))
                .collect(Collectors.toList());

        // 2. Объединяем их в одну общую задачу
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // 3. Когда ВСЕ задачи завершатся, собираем результаты и вызываем callback
        allFutures.thenAccept(v -> {
            List<T> resultList = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            safeAccept(callback, resultList);
        });
    }

    private static class Counter {
        private int count = 0;

        public int getCount() {
            return count;
        }

        public void addOne() {
            count++;
        }
    }
}

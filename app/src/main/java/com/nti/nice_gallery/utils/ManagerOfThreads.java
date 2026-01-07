package com.nti.nice_gallery.utils;

import android.content.Context;
import android.util.Log;

import java.util.function.Consumer;

public class ManagerOfThreads {

    private static final String LOG_TAG = "ManagerOfThreads";

    private final Context context;

    public ManagerOfThreads(Context context) {
        this.context = context;
    }

    public <T> void safeAccept(Consumer<T> callback, T payload) {
        if (callback != null) {
            callback.accept(payload);
        }
    }
}

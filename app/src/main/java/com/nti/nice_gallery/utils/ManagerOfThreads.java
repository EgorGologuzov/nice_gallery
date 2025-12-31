package com.nti.nice_gallery.utils;

import android.content.Context;

import java.util.function.Consumer;

public class ManagerOfThreads {

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

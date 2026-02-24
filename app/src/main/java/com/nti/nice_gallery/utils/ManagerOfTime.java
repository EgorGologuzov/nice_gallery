package com.nti.nice_gallery.utils;

import android.content.Context;

import android.os.Handler;
import android.os.Looper;

public class ManagerOfTime {

    private static Handler mainHandler;

    private final Context context;

    public ManagerOfTime(Context context) {
        this.context = context;
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
    }

    /**
     * Выполняет действие с указанной задержкой
     * @param action действие для выполнения
     * @param delay задержка в миллисекундах
     */
    public void doAction(Runnable action, int delay) {
        if (action == null) {
            return;
        }

        delay = Math.max(delay, 0);

        mainHandler.postDelayed(action, delay);
    }

    /**
     * Отменяет все запланированные, но еще не выполненные действия
     */
    public void cancelAllActions() {
        mainHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Отменяет конкретное запланированное действие
     * @param action действие для отмены
     */
    public void cancelAction(Runnable action) {
        if (action != null) {
            mainHandler.removeCallbacks(action);
        }
    }
}

package com.nti.nice_gallery.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;

public class ManagerOfNotifications {

    private final Context context;

    public ManagerOfNotifications(Context context) {
        this.context = context;
    }

    public void showToast(@StringRes int message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}

package com.nti.nice_gallery.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfSettings;

import java.util.function.Consumer;

public class ManagerOfNotifications {

    private static final int MIN_ID = 1_000;
    private static final int MAX_ID = Integer.MAX_VALUE;
    private static int lastId = MIN_ID;

    private static IManagerOfSettings managerOfSettings;

    private final Context context;
    private final NotificationManager notificationManager;

    public ManagerOfNotifications(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void appStartInit(Context context) {
        managerOfSettings = Domain.getManagerOfSettings(context);
        int savedLastId = managerOfSettings.getLastNotificationId();
        if (savedLastId > lastId) {
            lastId = savedLastId;
        }
    }

    public static int getNextId() {
        int id = lastId;
        if (id == MAX_ID) throw new RuntimeException("The value is outside the acceptable range: ManagerOfNotifications.getNextId.id");
        lastId++;
        managerOfSettings.saveLastNotificationId(lastId);
        return id;
    }

    public void showToast(@StringRes int message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void createChannel(String channelId) {
        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelId,
                NotificationManager.IMPORTANCE_LOW
        );

        channel.setDescription(channelId);
        notificationManager.createNotificationChannel(channel);
    }

    public void showNotification(
            String channelId,
            int notificationId,
            String title,
            String message,
            Consumer<NotificationCompat.Builder> postHandler
    ) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        if (postHandler != null) {
            postHandler.accept(builder);
        }

        notificationManager.notify(notificationId, builder.build());
    }

    public void showNotification(
            String channelId,
            int notificationId,
            @StringRes int title,
            @StringRes int message,
            Consumer<NotificationCompat.Builder> postHandler
    ) {
        String titleStr = context.getString(title);
        String messageStr = context.getString(message);
        showNotification(channelId, notificationId, titleStr, messageStr, postHandler);
    }

    public void showNotification(
            String channelId,
            int notificationId,
            @StringRes int title,
            @StringRes String message,
            Consumer<NotificationCompat.Builder> postHandler
    ) {
        String titleStr = context.getString(title);
        showNotification(channelId, notificationId, titleStr, message, postHandler);
    }

    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    public static Notification createForegroundInitNotification(Context foregroundContext, String channelId) {
        return new NotificationCompat.Builder(foregroundContext, channelId)
                .setContentTitle(foregroundContext.getString(R.string.notif_title_prepare))
                .setContentText(foregroundContext.getString(R.string.message_starting_operation))
                .setOngoing(true)
                .build();
    }

    public static void updateForegroundNotification(
            Context foregroundContext,
            String channelId,
            int notificationId,
            String title,
            String message,
            Consumer<NotificationCompat.Builder> postHandler
    ) {
        NotificationManager nm = (NotificationManager) foregroundContext.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(foregroundContext, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true);

        if (postHandler != null) {
            postHandler.accept(builder);
        }

        nm.notify(notificationId, builder.build());
    }
}

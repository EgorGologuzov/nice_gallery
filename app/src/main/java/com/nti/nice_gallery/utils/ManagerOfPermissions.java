package com.nti.nice_gallery.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.activities.ActivityMain;

public class ManagerOfPermissions {

    private final ActivityMain activityMain;

    public ManagerOfPermissions(ActivityMain activityMain) {
        this.activityMain = activityMain;
        initManageExternalStoragePermissionLauncher();
        initPostNotificationLauncher();
    }

    private ActivityResultLauncher<Intent> onManageExternalStorageLauncher;
    private Runnable onManageExternalStorageGranted;
    private Runnable onManageExternalStorageDenied;

    private void initManageExternalStoragePermissionLauncher() {
        onManageExternalStorageLauncher = activityMain.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (hasManageExternalStoragePermission()) {
                        if (onManageExternalStorageGranted != null) onManageExternalStorageGranted.run();
                        return;
                    }
                    if (onManageExternalStorageDenied != null) onManageExternalStorageDenied.run();
                }
        );
    }

    public boolean hasManageExternalStoragePermission() {
        return Environment.isExternalStorageManager();
    }

    public void requestExternalStorageManagerPermission(Runnable onGranted, Runnable onDenied) {
        if (hasManageExternalStoragePermission()) {
            if (onGranted != null) onGranted.run();
            return;
        }

        onManageExternalStorageGranted = onGranted;
        onManageExternalStorageDenied = onDenied;

        final Runnable openPermissionSettings = () -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + activityMain.getPackageName()));
            onManageExternalStorageLauncher.launch(intent);
        };

        ManagerOfDialogs managerOfDialogs = new ManagerOfDialogs(activityMain);
        managerOfDialogs.showYesNo(
                R.string.dialog_title_permission_required,
                R.string.message_request_manage_external_storage,
                openPermissionSettings,
                () -> { if (onDenied != null) onDenied.run(); }
        );
    }

    private ActivityResultLauncher<String> postNotificationLauncher;
    private Runnable onPostNotificationGranted;
    private Runnable onPostNotificationDenied;

    private void initPostNotificationLauncher() {
        postNotificationLauncher = activityMain.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        if (onPostNotificationGranted != null) onPostNotificationGranted.run();
                    } else {
                        if (onPostNotificationDenied != null) onPostNotificationDenied.run();
                    }
                }
        );
    }

    public boolean hasPostNotificationsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return androidx.core.content.ContextCompat.checkSelfPermission(
                    activityMain,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    public void requestPostNotificationsPermission(Runnable onGranted, Runnable onDenied) {
        if (hasPostNotificationsPermission()) {
            if (onGranted != null) onGranted.run();
            return;
        }

        onPostNotificationGranted = onGranted;
        onPostNotificationDenied = onDenied;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            postNotificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        } else {
            if (onGranted != null) onGranted.run();
        }
    }
}

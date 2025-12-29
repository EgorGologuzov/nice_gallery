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

    private final Context context;

    public ManagerOfPermissions(Context context) {
        this.context = context;
    }

    public boolean hasManageExternalStoragePermission() {
        return Environment.isExternalStorageManager();
    }

    public void requestExternalStorageManagerPermission(Runnable onGranted, Runnable onDenied) {
        ActivityMain activityMain = (ActivityMain) context;

        if (hasManageExternalStoragePermission()) {
            if (onGranted != null) onGranted.run();
            return;
        }

        final ActivityResultLauncher<Intent> storageLauncher = activityMain.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (hasManageExternalStoragePermission()) {
                        if (onGranted != null) onGranted.run();
                        return;
                    }
                    if (onDenied != null) onDenied.run();
                }
        );

        final Runnable openPermissionSettings = () -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + activityMain.getPackageName()));
            storageLauncher.launch(intent);
        };

        ManagerOfDialogs managerOfDialogs = new ManagerOfDialogs(context);
        managerOfDialogs.showYesNo(
                R.string.dialog_title_permission_required,
                R.string.message_request_manage_external_storage,
                openPermissionSettings,
                () -> { if (onDenied != null) onDenied.run(); }
        );
    }
}

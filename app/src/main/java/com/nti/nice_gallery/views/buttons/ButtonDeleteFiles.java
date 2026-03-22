package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.ManagerOfFiles;
import com.nti.nice_gallery.models.ModelFilesActionRequest;
import com.nti.nice_gallery.models.ModelFilesActionResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelRequestProgress;
import com.nti.nice_gallery.utils.ManagerOfDialogs;
import com.nti.nice_gallery.utils.ManagerOfNotifications;
import com.nti.nice_gallery.utils.ManagerOfThreads;
import com.nti.nice_gallery.utils.ReadOnlyList;

import java.util.HashMap;
import java.util.function.Consumer;

public class ButtonDeleteFiles extends ButtonBase {

    private HashMap<String, ModelMediaFile> files;
    private Consumer<ButtonDeleteFiles> actionFinishedListener;
    private Consumer<ButtonDeleteFiles> actionProgressListener;
    private ModelFilesActionRequest request;
    private ModelRequestProgress progress;

    private ManagerOfDialogs managerOfDialogs;
    private ManagerOfNotifications managerOfNotifications;
    private ManagerOfFiles managerOfFiles;
    private ManagerOfThreads managerOfThreads;

    public ButtonDeleteFiles(Context context) {
        super(context);
        init();
    }

    public ButtonDeleteFiles(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonDeleteFiles(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        managerOfDialogs = new ManagerOfDialogs(getContext());
        managerOfNotifications = new ManagerOfNotifications(getContext());
        managerOfFiles = new ManagerOfFiles(getContext());
        managerOfThreads = new ManagerOfThreads(getContext());
        setImageResource(R.drawable.baseline_delete_forever_24);
        setOnClickListener(v -> onClick());
    }

    public void setFiles(HashMap<String, ModelMediaFile> files) {
        this.files = files;
    }

    public ModelRequestProgress getProgress() {
        return progress;
    }

    public void setActionFinishedListener(Consumer<ButtonDeleteFiles> l) {
        this.actionFinishedListener = l;
    }

    public void setActionProgressListener(Consumer<ButtonDeleteFiles> l) {
        this.actionProgressListener = l;
    }

    private void onClick() {
        if (files == null || files.isEmpty()) {
            managerOfNotifications.showToast(R.string.toast_select_files);
            return;
        }

        Consumer<ModelFilesActionResponse> onActionExecuted = response -> {
            managerOfThreads.runOnUiThread(() -> {
                managerOfDialogs.showActionReport(response);
                if (actionFinishedListener != null) {
                    actionFinishedListener.accept(this);
                }
            });
        };

        Consumer<ModelRequestProgress> onProgress = progress -> {
            this.progress = progress;
            if (actionProgressListener != null) {
                actionProgressListener.accept(this);
            }
        };

        Runnable onActionConfirm = () -> {
            managerOfFiles.executeAction(request, onActionExecuted, onProgress);
        };

        request = new ModelFilesActionRequest(
                ModelFilesActionRequest.FilesAction.Delete,
                new ReadOnlyList<>(files.values()),
                null,
                null
        );

        managerOfDialogs.showActionConfirm(request, onActionConfirm, null);
    }
}

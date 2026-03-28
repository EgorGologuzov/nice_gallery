package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.ManagerOfFiles;
import com.nti.nice_gallery.models.ModelFilesActionRequest;
import com.nti.nice_gallery.models.ModelFilesActionResponse;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.models.ModelRequestProgress;
import com.nti.nice_gallery.utils.Convert;
import com.nti.nice_gallery.utils.ManagerOfDialogs;
import com.nti.nice_gallery.utils.ManagerOfNotifications;
import com.nti.nice_gallery.utils.ManagerOfThreads;
import com.nti.nice_gallery.utils.ReadOnlyList;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

public class ButtonReplaceFiles extends ButtonBase {

    private String basePath = "/storage/emulated/0";
    private HashMap<String, ModelMediaFile> files;
    private Consumer<ButtonReplaceFiles> actionFinishedListener;
    private Consumer<ButtonReplaceFiles> actionProgressListener;
    private ModelFilesActionRequest request;
    private ModelRequestProgress progress;

    private ManagerOfDialogs managerOfDialogs;
    private ManagerOfNotifications managerOfNotifications;
    private ManagerOfFiles managerOfFiles;
    private ManagerOfThreads managerOfThreads;
    private Convert convert;

    public ButtonReplaceFiles(Context context) {
        super(context);
        init();
    }

    public ButtonReplaceFiles(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonReplaceFiles(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        managerOfDialogs = new ManagerOfDialogs(getContext());
        managerOfNotifications = new ManagerOfNotifications(getContext());
        managerOfFiles = new ManagerOfFiles(getContext());
        managerOfThreads = new ManagerOfThreads(getContext());
        convert = new Convert(getContext());
        setImageResource(R.drawable.baseline_drive_file_move_rtl_24);
        setOnClickListener(v -> onClick());
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setFiles(HashMap<String, ModelMediaFile> files) {
        this.files = files;
    }

    public ModelRequestProgress getProgress() {
        return progress;
    }

    public void setActionFinishedListener(Consumer<ButtonReplaceFiles> l) {
        this.actionFinishedListener = l;
    }

    public void setActionProgressListener(Consumer<ButtonReplaceFiles> l) {
        this.actionProgressListener = l;
    }

    private void onClick() {
        if (files == null || files.isEmpty()) {
            managerOfNotifications.showToast(R.string.toast_select_files);
            return;
        }

        class RequestParams { String path; ModelFilesActionRequest.DuplicateNamePolicy namePolicy; }
        RequestParams params = new RequestParams();

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

        Runnable buildRequestAndShowConfirm = () -> {
            request = new ModelFilesActionRequest(
                    ModelFilesActionRequest.FilesAction.Replace,
                    new ReadOnlyList<>(files.values()),
                    params.path,
                    params.namePolicy
            );

            managerOfDialogs.showActionConfirm(request, onActionConfirm, null);
        };

        Consumer<Integer> onNamePolicyChosen = policyIndex -> {
            params.namePolicy = convert.indexToEnumValue(ModelFilesActionRequest.DuplicateNamePolicy.class, policyIndex);
            buildRequestAndShowConfirm.run();
        };

        Consumer<String> onPathChosen = path -> {
            params.path = path;

            managerOfDialogs.showChooseOne(
                    R.string.dialog_title_duplicate_name_policy,
                    R.array.enum_duplicate_name_policy,
                    0,
                    onNamePolicyChosen
            );
        };

        managerOfDialogs.showChoicePath((basePath + "/").replace("//", "/"), onPathChosen, null);
    }
}

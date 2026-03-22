package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.ManagerOfFiles;
import com.nti.nice_gallery.models.ModelFilesActionRequest;
import com.nti.nice_gallery.models.ModelFilesActionResponse;
import com.nti.nice_gallery.utils.ManagerOfDialogs;
import com.nti.nice_gallery.utils.ManagerOfThreads;
import com.nti.nice_gallery.utils.ReadOnlyList;

import java.util.Objects;
import java.util.function.Consumer;

public class ButtonCreateFolder extends ButtonBase {

    private String basePath;
    private ModelFilesActionRequest request;
    private Consumer<ButtonCreateFolder> actionFinishedListener;

    private ManagerOfFiles managerOfFiles;
    private ManagerOfDialogs managerOfDialogs;
    private ManagerOfThreads managerOfThreads;

    public ButtonCreateFolder(Context context) {
        super(context);
        init();
    }

    public ButtonCreateFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonCreateFolder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        managerOfFiles = new ManagerOfFiles(getContext());
        managerOfDialogs = new ManagerOfDialogs(getContext());
        managerOfThreads = new ManagerOfThreads(getContext());
        setImageResource(R.drawable.baseline_add_24);
        setOnClickListener(v -> onClick());
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
        int visibility = Objects.equals(basePath, ManagerOfFiles.PATH_ROOT) ? View.GONE : View.VISIBLE;
        setVisibility(visibility);
    }

    public void setActionFinishedListener(Consumer<ButtonCreateFolder> l) {
        this.actionFinishedListener = l;
    }

    private void onClick() {
        Consumer<ModelFilesActionResponse> onActionExecuted = response -> {
            managerOfThreads.runOnUiThread(() -> {
                managerOfDialogs.showActionReport(response);
                if (actionFinishedListener != null) {
                    actionFinishedListener.accept(this);
                }
            });
        };

        Runnable onActionConfirm = () -> {
            managerOfFiles.executeAction(request, onActionExecuted, null);
        };

        Consumer<String> onPathChosen = path -> {
            request = new ModelFilesActionRequest(
                    ModelFilesActionRequest.FilesAction.CreateFolder,
                    null,
                    path,
                    null
            );

            managerOfDialogs.showActionConfirm(request, onActionConfirm, null);
        };

        managerOfDialogs.showChoicePath((basePath + "/").replace("//", "/"), onPathChosen, null);
    }
}

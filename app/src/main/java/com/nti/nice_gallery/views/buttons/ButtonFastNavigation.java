package com.nti.nice_gallery.views.buttons;

import android.content.Context;
import android.util.AttributeSet;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.utils.ManagerOfDialogs;
import com.nti.nice_gallery.utils.ManagerOfNotifications;

import java.io.File;
import java.util.function.Consumer;

public class ButtonFastNavigation extends ButtonBase {

    private String basePath = "/storage/emulated/0";
    private String selectedPath = null;
    private Consumer<ButtonFastNavigation> onSelectedListener;

    private ManagerOfDialogs managerOfDialogs;
    private ManagerOfNotifications managerOfNotifications;

    public ButtonFastNavigation(Context context) {
        super(context);
        init();
    }

    public ButtonFastNavigation(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonFastNavigation(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        managerOfDialogs = new ManagerOfDialogs(getContext());
        managerOfNotifications = new ManagerOfNotifications(getContext());
        setImageResource(R.drawable.baseline_folder_24);
        setOnClickListener(v -> onClick());
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setOnSelectedListener(Consumer<ButtonFastNavigation> l) {
        this.onSelectedListener = l;
    }

    public String getSelectedPath() {
        return selectedPath;
    }

    private void onClick() {
        Consumer<String> onChoice = path -> {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                selectedPath = path;
                if (onSelectedListener != null) {
                    onSelectedListener.accept(this);
                }
            } else {
                managerOfNotifications.showToast(R.string.toast_folder_is_not_exist);
            }
        };

        managerOfDialogs.showChoicePath(
                (basePath + "/").replace("//", "/"),
                onChoice,
                null
        );
    }
}

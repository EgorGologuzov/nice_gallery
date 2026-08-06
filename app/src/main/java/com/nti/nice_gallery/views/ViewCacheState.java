package com.nti.nice_gallery.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.IManagerOfSettings;
import com.nti.nice_gallery.data.ManagerOfCache;
import com.nti.nice_gallery.utils.ManagerOfDialogs;
import com.nti.nice_gallery.utils.ManagerOfNotifications;
import com.nti.nice_gallery.views.buttons.ButtonBase;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Arrays;

public class ViewCacheState extends LinearLayout {

    private ManagerOfCache managerOfCache;
    private ManagerOfDialogs managerOfDialogs;
    private ManagerOfNotifications managerOfNotifications;

    public ViewCacheState(Context context) {
        super(context);
        init();
    }

    public ViewCacheState(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewCacheState(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_cache_state, this);

        managerOfCache = new ManagerOfCache(getContext());
        managerOfDialogs = new ManagerOfDialogs(getContext());
        managerOfNotifications = new ManagerOfNotifications(getContext());

        TextView textFilesCacheInfo = findViewById(R.id.textFilesCacheInfo);
        ButtonBase buttonFilesCacheDetails = findViewById(R.id.buttonFilesCacheDetails);
        ButtonBase buttonFilesCacheClear = findViewById(R.id.buttonFilesCacheClear);
        TextView textPreviewCacheInfo = findViewById(R.id.textPreviewsCacheInfo);
        ButtonBase buttonPreviewCacheClear = findViewById(R.id.buttonPreviewsCacheClear);

        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setOrientation(VERTICAL);

        View.OnClickListener onClickButtonFilesCacheClear = btn -> {
              managerOfDialogs.showYesNo(
                      R.string.dialog_title_submit_deletion,
                      R.string.message_question_submit_cache_clear,
                      () -> {
                          managerOfCache.clearFilesInfoCache();
                          managerOfNotifications.showToast(R.string.message_cache_cleared);
                          textFilesCacheInfo.setText(managerOfCache.getFilesCacheInfo());
                      },
                      null
              );
        };

        View.OnClickListener onClickButtonPreviewCacheClear = btn -> {
            managerOfDialogs.showYesNo(
                    R.string.dialog_title_submit_deletion,
                    R.string.message_question_submit_cache_clear,
                    () -> {
                        managerOfCache.clearPreviewCache();
                        managerOfNotifications.showToast(R.string.message_cache_cleared);
                        textPreviewCacheInfo.setText(managerOfCache.getPreviewsCacheInfo());
                    },
                    null
            );
        };

        View.OnClickListener onClickButtonFilesCacheDetails = btn -> {
            IManagerOfSettings.TxtFile cacheTxt = managerOfCache.getFilesCacheTxt();

            String details = null;

            if (cacheTxt == null) {
                details = getContext().getString(R.string.message_file_not_exists);
            } else if (cacheTxt.strings != null) {
                int rowsCount = cacheTxt.strings.length;
                LocalDateTime updatedAt = cacheTxt.updatedAt;
                String updatedAtStr = updatedAt != null ? updatedAt.toString() : null;
                details = getContext().getString(R.string.format_info_file_txt, rowsCount, updatedAtStr);
            }

            managerOfDialogs.showInfo(
                    R.string.dialog_title_details,
                    details
            );
        };

        textFilesCacheInfo.setText(managerOfCache.getFilesCacheInfo());
        textPreviewCacheInfo.setText(managerOfCache.getPreviewsCacheInfo());

        buttonFilesCacheDetails.setOnClickListener(onClickButtonFilesCacheDetails);
        buttonFilesCacheClear.setOnClickListener(onClickButtonFilesCacheClear);
        buttonPreviewCacheClear.setOnClickListener(onClickButtonPreviewCacheClear);
    }
}

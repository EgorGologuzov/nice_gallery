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
import com.nti.nice_gallery.data.ManagerOfCache;
import com.nti.nice_gallery.data.ManagerOfDatabase;
import com.nti.nice_gallery.utils.ManagerOfDialogs;
import com.nti.nice_gallery.utils.ManagerOfNotifications;
import com.nti.nice_gallery.views.buttons.ButtonBase;

import java.util.function.Function;

public class ViewCacheState extends LinearLayout {

    private ManagerOfCache managerOfCache;
    private ManagerOfDialogs managerOfDialogs;
    private ManagerOfNotifications managerOfNotifications;
    private ManagerOfDatabase managerOfDatabase;

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
        managerOfDatabase = new ManagerOfDatabase(getContext());

        TextView textFilesCacheInfo = findViewById(R.id.textFilesCacheInfo);
        ButtonBase buttonFilesCacheClear = findViewById(R.id.buttonFilesCacheClear);
        TextView textActualizationCacheInfo = findViewById(R.id.textActualizationCacheInfo);
        ButtonBase buttonActualizationCacheClear = findViewById(R.id.buttonActualizationCacheClear);
        TextView textPreviewCacheInfo = findViewById(R.id.textPreviewsCacheInfo);
        ButtonBase buttonPreviewCacheClear = findViewById(R.id.buttonPreviewsCacheClear);

        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setOrientation(VERTICAL);

        Function<ManagerOfDatabase.Statistic, String> buildCachedFilesInfoString = stat -> {
            return getContext().getString(
                    R.string.format_info_files_cache,
                    stat.getCachedFilesCount(),
                    stat.getSavedCachedFilesCount()
            );
        };

        Function<ManagerOfDatabase.Statistic, String> buildActualizationInfoString = stat -> {
            return getContext().getString(
                    R.string.format_info_actualization_cache,
                    stat.getActualizationInfoCount(),
                    stat.getSavedActualizationInfoCount()
            );
        };

        View.OnClickListener onClickButtonFilesCacheClear = btn -> {
              managerOfDialogs.showYesNo(
                      R.string.dialog_title_submit_deletion,
                      R.string.message_question_submit_cache_clear,
                      () -> {
                          managerOfCache.clearFilesInfoCache();
                          managerOfNotifications.showToast(R.string.message_cache_cleared);
                          ManagerOfDatabase.Statistic stat = managerOfDatabase.getStatistic(true);
                          textFilesCacheInfo.setText(buildCachedFilesInfoString.apply(stat));
                      },
                      null
              );
        };

        View.OnClickListener onClickButtonActualizationCacheClear = btn -> {
            managerOfDialogs.showYesNo(
                    R.string.dialog_title_submit_deletion,
                    R.string.message_question_submit_cache_clear,
                    () -> {
                        managerOfDatabase.clearActualizationInfo();
                        managerOfNotifications.showToast(R.string.message_cache_cleared);
                        ManagerOfDatabase.Statistic stat = managerOfDatabase.getStatistic(true);
                        textActualizationCacheInfo.setText(buildActualizationInfoString.apply(stat));
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

        ManagerOfDatabase.Statistic stat = managerOfDatabase.getStatistic(true);

        textFilesCacheInfo.setText(buildCachedFilesInfoString.apply(stat));
        textActualizationCacheInfo.setText(buildActualizationInfoString.apply(stat));
        textPreviewCacheInfo.setText(managerOfCache.getPreviewsCacheInfo());

        buttonFilesCacheClear.setOnClickListener(onClickButtonFilesCacheClear);
        buttonActualizationCacheClear.setOnClickListener(onClickButtonActualizationCacheClear);
        buttonPreviewCacheClear.setOnClickListener(onClickButtonPreviewCacheClear);
    }
}

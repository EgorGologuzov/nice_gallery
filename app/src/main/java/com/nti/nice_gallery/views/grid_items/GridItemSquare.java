package com.nti.nice_gallery.views.grid_items;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfFiles;
import com.nti.nice_gallery.models.ModelGetPreviewRequest;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.Convert;
import com.nti.nice_gallery.utils.ManagerOfThreads;

import java.util.ArrayList;

public class GridItemSquare extends GridItemBase {

    private static final String LOG_TAG = "GridItemSquare";

    private boolean isInfoHidden;

    private TextView infoView;
    private ImageView imageView;

    private IManagerOfFiles managerOfFiles;
    private ManagerOfThreads managerOfThreads;
    private Convert convert;

    public GridItemSquare(@NonNull Context context) {
        super(context);
        init();
    }

    public GridItemSquare(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridItemSquare(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    private void init() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        setLayoutParams(layoutParams);
        inflate(getContext(), R.layout.grid_item_square, this);

        infoView = findViewById(R.id.infoView);
        imageView = findViewById(R.id.imageView);
        managerOfFiles = Domain.getManagerOfFiles(getContext());
        managerOfThreads = new ManagerOfThreads(getContext());
        convert = new Convert(getContext());
    }

    public boolean getIsInfoHidden() {
        return isInfoHidden;
    }

    public void setIsInfoHidden(boolean isInfoHidden) {
        this.isInfoHidden = isInfoHidden;
    }

    @Override
    protected void updateView() {
        String info = null;
        int infoVisibility = VISIBLE;

        try {
            ArrayList<String> infoItems = new ArrayList<>();

            if (model.isDirectory) {
                infoItems.add(model.name);
            } else if (model.isImage) {
                infoItems.add(model.extension.toUpperCase());
                infoItems.add(convert.weightToString(model.weight));
            } else if (model.isVideo) {
                infoItems.add(getContext().getResources().getString(R.string.symbol_play_video));
                infoItems.add(convert.durationToTimeString(model.duration));
                infoItems.add(convert.weightToString(model.weight));
            }

            if (isInfoHidden) {
                if (model.type == ModelMediaFile.Type.Video) {
                    infoItems.clear();
                    infoItems.add(getContext().getResources().getString(R.string.symbol_play_video));
                } else {
                    infoVisibility = GONE;
                }
            }

            info = String.join(getContext().getResources().getString(R.string.symbol_dot_separator), infoItems);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            if (info == null) { info = getContext().getResources().getString(R.string.message_error_load_file_info_failed); }
        }

        infoView.setText(info);
        infoView.setVisibility(infoVisibility);

        if (model.isFolder) {
            imageView.setImageResource(R.drawable.baseline_folder_24_orange_700);
        } else if (model.isStorage) {
            imageView.setImageResource(R.drawable.baseline_storage_24);
        } else {
            imageView.post(() -> {
                ModelGetPreviewRequest previewRequest = new ModelGetPreviewRequest(
                        model,
                        imageView.getWidth(),
                        imageView.getHeight()
                );

                managerOfFiles.getPreviewAsync(previewRequest, response -> {
                    managerOfThreads.runOnUiThread(() -> {
                        if (response != null && response.preview != null) {
                            imageView.setImageBitmap(response.preview);
                        } else {
                            imageView.setImageResource(R.drawable.baseline_error_24_orange_700);
                        }
                    });
                });
            });
        }
    }
}

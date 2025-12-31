package com.nti.nice_gallery.views.grid_items;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
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

import java.util.ArrayList;

public class GridItemLine extends GridItemBase {

    private static final String LOG_TAG = "GridItemView";

    private ModelMediaFile model;

    private ImageView imageView;
    private TextView nameView;
    private TextView pathView;
    private TextView infoView;
    private TextView infoView2;

    private IManagerOfFiles managerOfFiles;
    private Convert convert;

    public GridItemLine(@NonNull Context context) {
        super(context);
        init();
    }

    public GridItemLine(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridItemLine(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        inflate(getContext(), R.layout.grid_item_line, this);

        imageView = findViewById(R.id.imageView);
        nameView = findViewById(R.id.itemNameView);
        pathView = findViewById(R.id.itemPathView);
        infoView = findViewById(R.id.infoView);
        infoView2 = findViewById(R.id.infoView2);
        managerOfFiles = Domain.getManagerOfFiles(getContext());
        convert = new Convert(getContext());
    }

    public ModelMediaFile getModel() {
        return model;
    }

    public void setModel(ModelMediaFile model) {
        this.model = model;
        updateView();
    }

    private void updateView() {
        String name = null;
        String path = null;
        String info = null;
        String info2 = null;
        int infoView2Visibility = GONE;

        try {
            name = model.name;
            path = model.path;

            ArrayList<String> infoItems = new ArrayList<>();

            if (model.type != ModelMediaFile.Type.Folder) {
                infoItems.add(convert.weightToString(model.weight));
                infoItems.add(convert.sizeToString(model.width, model.height));
            }

            infoItems.add(convert.dateToFullNumericDateString(model.createdAt));

            info = String.join(getContext().getResources().getString(R.string.symbol_dot_separator), infoItems);

            if (model.type == ModelMediaFile.Type.Video) {
                infoItems.clear();
                infoItems.add(getContext().getResources().getString(R.string.symbol_play_video));
                infoItems.add(convert.durationToTimeString(model.duration));
                info2 = String.join(getContext().getResources().getString(R.string.symbol_dot_separator), infoItems);
                infoView2Visibility = VISIBLE;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            if (info == null) { info = getContext().getResources().getString(R.string.message_error_load_file_info_failed); }
        }

        nameView.setText(name);
        pathView.setText(path);
        infoView.setText(info);
        infoView2.setText(info2);
        infoView2.setVisibility(infoView2Visibility);

        try {
            if (model.type != ModelMediaFile.Type.Folder) {
                ModelGetPreviewRequest previewRequest = new ModelGetPreviewRequest(
                        model
                );

                managerOfFiles.getPreviewAsync(previewRequest, response -> {
                    if (response != null && response.preview != null) {
                        post(() -> imageView.setImageBitmap(response.preview));
                    } else {
                        post(() -> imageView.setImageResource(R.drawable.baseline_error_24_orange_700));
                    }
                });
            } else {
                imageView.setImageResource(R.drawable.baseline_folder_24_orange_700);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.baseline_error_24_orange_700);
        }
    }
}

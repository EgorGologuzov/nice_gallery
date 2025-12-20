package com.nti.nice_gallery.views.grid_items;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfFiles;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.Convert;

import java.util.ArrayList;

public class GridItemQuilt extends GridItemBase {

    private ModelMediaFile model;

    private TextView infoView;
    private ImageView imageView;

    private IManagerOfFiles managerOfFiles;
    private Convert convert;

    public GridItemQuilt(@NonNull Context context, float weight) {
        super(context);
        init(weight);
    }

    private void init(float weight) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = weight;
        setLayoutParams(layoutParams);
        inflate(getContext(), R.layout.grid_item_quilt, this);

        infoView = findViewById(R.id.infoView);
        imageView = findViewById(R.id.imageView);
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
        ArrayList<String> infoItems = new ArrayList<>();

        if (model.type == ModelMediaFile.Type.Folder) {
            infoItems.add(model.name);
            infoItems.add(convert.weightToString(model.weight));
        } else if (model.type == ModelMediaFile.Type.Image) {
            infoItems.add(model.extension.toUpperCase());
            infoItems.add(convert.weightToString(model.weight));
        } else if (model.type == ModelMediaFile.Type.Video) {
            infoItems.add(getContext().getResources().getString(R.string.symbol_play_video));
            infoItems.add(convert.durationToTimeString(model.duration));
            infoItems.add(convert.weightToString(model.weight));
        }

        String infoString = String.join(getContext().getResources().getString(R.string.symbol_dot_separator), infoItems);

        imageView.setImageBitmap(managerOfFiles.getFilePreview(model));
//        imageView.setImageURI(managerOfFiles.getItemContentUri(model));
        infoView.setText(infoString);
    }
}

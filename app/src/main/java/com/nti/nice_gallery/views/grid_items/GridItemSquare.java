package com.nti.nice_gallery.views.grid_items;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfFiles;
import com.nti.nice_gallery.models.ModelMediaTreeItem;
import com.nti.nice_gallery.utils.Convert;

import java.util.ArrayList;

public class GridItemSquare extends GridItemBase {

    private ModelMediaTreeItem model;
    private boolean isInfoHidden;

    private TextView infoView;
    private ImageView imageView;

    private IManagerOfFiles managerOfFiles;
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
        convert = new Convert(getContext());
    }

    public ModelMediaTreeItem getModel() {
        return model;
    }

    public void setModel(ModelMediaTreeItem model) {
        this.model = model;
        updateView();
    }

    public boolean getIsInfoHidden() {
        return isInfoHidden;
    }

    public void setIsInfoHidden(boolean isInfoHidden) {
        this.isInfoHidden = isInfoHidden;
        updateView();
    }

    private void updateView() {
        ArrayList<String> infoItems = new ArrayList<>();

        if (model.type == ModelMediaTreeItem.Type.Folder) {
            infoItems.add(model.name);
            infoItems.add(convert.weightToString(model.weight));
        } else if (model.type == ModelMediaTreeItem.Type.Image) {
            infoItems.add(model.extension.toUpperCase());
            infoItems.add(convert.weightToString(model.weight));
        } else if (model.type == ModelMediaTreeItem.Type.Video) {
            infoItems.add(getContext().getResources().getString(R.string.symbol_play_video));
            infoItems.add(convert.durationToTimeString(model.duration));
            infoItems.add(convert.weightToString(model.weight));
        }

        if (isInfoHidden) {
            if (model.type == ModelMediaTreeItem.Type.Video) {
                infoItems.clear();
                infoItems.add(getContext().getResources().getString(R.string.symbol_play_video));
            } else {
                infoView.setVisibility(GONE);
            }
        }

        String infoString = String.join(getContext().getResources().getString(R.string.symbol_dot_separator), infoItems);

        imageView.setImageBitmap(managerOfFiles.getItemThumbnail(model));
//        imageView.setImageURI(managerOfFiles.getItemContentUri(model));
        infoView.setText(infoString);
    }
}

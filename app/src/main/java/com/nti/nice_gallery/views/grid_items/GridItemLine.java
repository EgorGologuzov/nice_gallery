package com.nti.nice_gallery.views.grid_items;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.data.Domain;
import com.nti.nice_gallery.data.IManagerOfFiles;
import com.nti.nice_gallery.models.ModelMediaFile;
import com.nti.nice_gallery.utils.Convert;

import java.util.ArrayList;

public class GridItemLine extends GridItemBase {

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
        ArrayList<String> infoItems = new ArrayList<>();

        infoItems.add(convert.weightToString(model.weight));
        infoItems.add(convert.dateToFullNumericDateString(model.createdAt));

        if (model.resolution != null) {
            infoItems.add(convert.sizeToString(model.resolution));
        }

        String info = String.join(getContext().getResources().getString(R.string.symbol_dot_separator), infoItems);
        infoItems.clear();

        infoItems.add(getContext().getResources().getString(R.string.symbol_play_video));
        infoItems.add(convert.durationToTimeString(model.duration));

        String info2 = String.join(getContext().getResources().getString(R.string.symbol_dot_separator), infoItems);

        if (model.type != ModelMediaFile.Type.Video) {
            infoView2.setVisibility(GONE);
        }

        imageView.setImageBitmap(managerOfFiles.getFilePreview(model));
//        imageView.setImageURI(managerOfFiles.getItemContentUri(model));
        nameView.setText(model.name);
        pathView.setText(model.path);
        infoView.setText(info);
        infoView2.setText(info2);
    }
}
